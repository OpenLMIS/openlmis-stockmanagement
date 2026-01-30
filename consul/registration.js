const commandLineArgs = require('command-line-args');
const raml = require('raml-parser');
const axios = require('axios');
const uuid = require('uuid');
const fs = require('fs');
const ip = require('ip');

// --- HELPER FUNCTIONS ---

const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

// --- LOGIC CLASSES ---

class ServiceRAMLParser {
  async extractResources(filename) {
    try {
      const data = await raml.loadFile(filename);
      const resources = [];

      data.resources.forEach(node => {
        this.extractNodeResources(node).forEach(res => resources.push(res));
      });
      return resources;
    } catch (error) {
      throw new Error(`RAML Parsing failed: ${error}`);
    }
  }

  extractNodeResources(node) {
    const paths = [];
    if (node.resources) {
      node.resources.forEach(childNode => {
        this.extractNodeResources(childNode).forEach(path => {
          paths.push(node.relativeUri + path);
        });
      });
    }
    if (node.methods && node.methods.length > 0) {
      paths.push(node.relativeUri);
    }
    return paths;
  }
}

class ServiceConsulRegistrator {
  constructor(host, port) {
    this.baseUrl = `http://${host}:${port}/v1`;
    this.attempts = 10;
    this.attemptTimeout = 1000;
  }

  async registerService(serviceData) {
    return this._requestWithRetry('PUT', '/agent/service/register', serviceData);
  }

  async deregisterService(serviceId) {
    return this._requestWithRetry('PUT', `/agent/service/deregister/${serviceId}`);
  }

  async registerResources(serviceName, resourceArray) {
    const promises = resourceArray.map(resource => {
      const path = `/kv/resources${resource}`;
      return this._requestWithRetry('PUT', path, serviceName);
    });
    return Promise.all(promises);
  }

  async deregisterResources(resourceArray) {
    const promises = resourceArray.map(resource => {
      const path = `/kv/resources${resource}`;
      return this._requestWithRetry('DELETE', path);
    });
    return Promise.all(promises);
  }

  async _requestWithRetry(method, endpoint, data) {
    let lastError;
    for (let i = 0; i < this.attempts; i++) {
      try {
        const response = await axios({
          method: method,
          url: `${this.baseUrl}${endpoint}`,
          data: data
        });
        if (response.status >= 200 && response.status < 300) {
          return response.data;
        }
      } catch (err) {
        lastError = err;
        if (err.response && err.response.status < 500) throw err;

        console.log(`Attempt ${i + 1}/${this.attempts} connecting to Consul failed. Retrying...`);
        await sleep(this.attemptTimeout);
      }
    }
    throw lastError;
  }
}

class RegistrationService {
  constructor(host, port) {
    this.registrator = new ServiceConsulRegistrator(host, port);
    this.parser = new ServiceRAMLParser();
    this.filename = '.consul_service_id~';
  }

  async run(args) {
    if (args.command === 'register') {
      await this.register(args);
    } else if (args.command === 'deregister') {
      await this.deregister(args);
    } else {
      throw new Error("Invalid command. Use 'register' or 'deregister'.");
    }
  }

  async register(args) {
    console.log("Starting Registration...");
    const service = args.service;

    const actualHost = service.Address || ip.address();
    const actualPort = service.Port;

    if (service.check && service.check.http) {
      service.check.http = service.check.http
          .replace('HOST', actualHost)
          .replace('PORT', actualPort);

      console.log(`Configured Health Check URL: ${service.check.http}`);
    }

    service.ID = this._generateServiceId(service);

    await this.registrator.registerService(service);
    console.log(`Service '${service.Name}' (ID: ${service.ID}) registered.`);

    if (args.raml) {
      console.log(`Parsing RAML from ${args.raml}...`);
      const resources = await this.parser.extractResources(args.raml);
      await this.registrator.registerResources(service.Name, resources);
    }

    if (args.path) {
      const normalizedPaths = args.path.map(p => p.startsWith('/') ? p : '/' + p);
      await this.registrator.registerResources(service.Name, normalizedPaths);
    }

    console.log("Registration finished successfully!");
  }

  async deregister(args) {
    console.log("Starting Deregistration...");
    const service = args.service;
    service.ID = this._generateServiceId(service);

    try {
      await this.registrator.deregisterService(service.ID);
      console.log(`Service '${service.ID}' deregistered.`);
    } catch (e) {
      console.warn(`Warning: Could not deregister service (it might not exist): ${e.message}`);
    }

    if (args.raml) {
      const resources = await this.parser.extractResources(args.raml);
      await this.registrator.deregisterResources(resources);
    }

    if (args.path) {
      const normalizedPaths = args.path.map(p => p.startsWith('/') ? p : '/' + p);
      await this.registrator.deregisterResources(normalizedPaths);
    }

    this._clearServiceId();
    console.log("Deregistration finished!");
  }

  _generateServiceId(service) {
    if (service.ID) {
      return service.ID;
    }

    try {
      if (fs.existsSync(this.filename)) {
        return fs.readFileSync(this.filename, 'utf8').trim();
      }
    } catch (err) {
      // ignore
    }

    const newId = `${service.Name}-${uuid.v4()}`;
    fs.writeFileSync(this.filename, newId);
    return newId;
  }

  _clearServiceId() {
    if (fs.existsSync(this.filename)) {
      fs.unlinkSync(this.filename);
    }
  }
}

class CommandLineResolver {
  processArgs() {
    const optionDefinitions = [
      { name: 'config-file', alias: 'f', type: String },
      { name: 'command', alias: 'c', type: String },
      { name: 'name', alias: 'n', type: String },
      { name: 'raml', alias: 'r', type: String },
      { name: 'path', alias: 'p', type: String, multiple: true }
    ];

    const args = commandLineArgs(optionDefinitions);
    const settings = this._getSettings(args);

    if (!settings.command) throw new Error("Command parameter is missing.");
    if (!(settings.raml || settings.path)) throw new Error("You must provide either 'path' or 'raml' parameter.");

    return settings;
  }

  _getSettings(args) {
    let settings = {
      service: {
        'ID': null,
        'Name': null,
        'Port': 80,
        'Address': ip.address(),
        'Tags': [],
        'EnableTagOverride': false
      }
    };

    if (args['config-file']) {
      const config = JSON.parse(fs.readFileSync(args['config-file'], 'utf8'));
      if (config.service) {
        const normalizedService = {};
        Object.keys(config.service).forEach(key => {
          normalizedService[key.toUpperCase() === 'ID' ? 'ID' : key] = config.service[key];
        });
        Object.assign(settings.service, normalizedService);
      }
      ['command', 'path', 'raml'].forEach(k => { if (config[k]) settings[k] = config[k]; });
    }

    ['command', 'path', 'raml'].forEach(k => { if (args[k]) settings[k] = args[k]; });
    if (args['name']) settings.service.Name = args['name'];

    return settings;
  }
}

// --- MAIN EXECUTION ---

(async () => {
  const consulHost = process.env.CONSUL_HOST || 'consul';
  const consulPort = process.env.CONSUL_PORT || '8500';

  try {
    console.log(`Connecting to Consul at ${consulHost}:${consulPort}...`);
    const registration = new RegistrationService(consulHost, consulPort);

    const resolver = new CommandLineResolver();
    const args = resolver.processArgs();

    await registration.run(args);

  } catch (err) {
    console.error("\n[FATAL ERROR]:");
    console.error(err.message || err);
    if (err.response) {
      console.error(`Status: ${err.response.status}`);
      console.error(`Data: ${JSON.stringify(err.response.data)}`);
    }
    process.exit(1);
  }
})();
