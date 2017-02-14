const commandLineArgs = require('command-line-args');
const sleep = require('system-sleep');
const raml = require('raml-parser');
const deasync = require('deasync');
const http = require('http');
const uuid = require('uuid');
const util = require('util');
const fs = require('fs');
const ip = require('ip');

function ServiceRAMLParser() {
  var self = this;

  self.extractResources = function(filename) {
    // Parse RAML file to retrieve available resources list
    return raml.loadFile(filename).then(function(data) {
      var resources = [];
      data.resources.forEach(function(node) {
        self.extractNodeResources(node).forEach(function(resource) {
          resources.push(resource);
        })
      });
      return resources;
    }, function(error) {
      throw new Error(error);
    });
  }

  self.extractNodeResources = function(node) {
    var paths = [];

    if (node.resources) {
      node.resources.forEach(function(childNode) {
        self.extractNodeResources(childNode).forEach(function(path) {
          paths.push(node.relativeUri + path);
        });
      });
    }

    if (node.methods instanceof Array && node.methods.length > 0){
      paths.push(node.relativeUri);
    }

    return paths;
  }
}
function ServiceConsulRegistrator(host, port) {
  self = this;
  self.host = host;
  self.port = port;
  self.attempts = 10;
  self.attemptTimeout = 500;

  self.registerService = function(serviceData) {
    return registerServiceBase(serviceData, 'PUT');
  }

  self.deregisterService = function(serviceData) {
    return registerServiceBase(serviceData, 'DELETE');
  }

  self.registerResources = function(serviceData, resourceArray) {
    return registerResourcesBase(serviceData, resourceArray, 'PUT');
  }

  self.deregisterResources = function(serviceData, resourceArray) {
    return registerResourcesBase(serviceData, resourceArray, 'DELETE');
  }

  function registerServiceBase(serviceData, mode) {
    var requestPath = mode === 'PUT' ? 'register' : 'deregister/' + serviceData.ID;
    var data = JSON.stringify(serviceData);
    var settings = {
      host: self.host,
      port: self.port,
      path: '/v1/agent/service/' + requestPath,
      method: 'PUT'
    }

    // Send the request
    for (var i = 0; i < self.attempts; i++) {
      var response = awaitRequest(settings, data);

      if (response.statusCode === 200) {
        break;
      }

      sleep(self.attemptTimeout);
    }
  }

  function registerResourcesBase(serviceData, resourceArray, mode) {
    var settings = [];
    var data = [];

    // Prepare settings and data for requests
    resourceArray.forEach(function(resource) {
      var setting = {
        host: self.host,
        port: self.port,
        path: '/v1/kv/resources' + resource,
        method: mode
      }

      settings.push(setting);
      data.push(serviceData.Name);
    });

    // Send all requests at once
    for (var i = 0; i < self.attempts; i++) {
      var responses = awaitRequests(settings, data);
      var success = !responses.some(function(response) { return response.statusCode !== 200 });

      if (success) {
        break;
      }

      sleep(self.attemptTimeout);
    }
  }

  function awaitRequests(settingArray, dataArray) {
    // Runs multiple parallel HTTP requests and waits for their completion
    var size = settingArray.length;
    var results = settingArray.map(function() { return null; });
    var completed = 0;

    for (var i = 0; i < size; i++) {
      (function(i) {
        var request = http.request(settingArray[i], function(response) {
          // This operation is safe in node.js
          completed++;
          results[i] = response;
        });

        request.write(dataArray[i]);
        request.end();
      })(i);
    };

    while(completed !== size) {
      deasync.runLoopOnce();
    }

    return results;
  }

  function awaitRequest(settings, data) {
    // Runs a single HTTP request synchronously
    var result;
    var request = http.request(settings, function(response) {
      result = response;
    });

    request.write(data);
    request.end();

    while(!result) {
      deasync.runLoopOnce();
    }

    return result;
  }
}
function RegistrationService(host, port) {
  var self = this;

  self.consulHost = host;
  self.consulPort = port;
  self.filename = '.consul_service_id~';

  self.registrator = new ServiceConsulRegistrator(self.consulHost, self.consulPort);
  self.parser = new ServiceRAMLParser();

  self.register = function(args) {
    console.log("Registering service...");
    registrationBase(args, 'register');
    console.log("Registration finished!");
  }
  self.deregister = function(args) {
    console.log("Deregistering service...");
    registrationBase(args, 'deregister');
    console.log("Deregistration finished!");
  }

  function registrationBase(args, mode) {
    registerService(args.service, mode);

    if (args.raml) {
      registerRaml(args.service, args.raml, mode);
    }

    if (args.path) {
      registerPath(args.service, args.path, mode);
    }
  }
  function registerService(service, mode) {
    service.ID = generateServiceId(service.Name);

    if (mode === 'register') {
      self.registrator.registerService(service);
    } else {
      self.registrator.deregisterService(service);
    }
  }
  function registerRaml(service, filename, mode) {
    var completed = false;
    service.ID = generateServiceId(service.Name);

    self.parser.extractResources(filename).then(function(resources) {
      if (mode === 'register') {
        self.registrator.registerResources(service, resources);
      } else {
        self.registrator.deregisterResources(service, resources);
      }

      completed = true;
    });

    // Wait for RAML parsing
    while(!completed) {
      deasync.runLoopOnce();
    }
  }
  function registerPath(service, paths, mode) {
    service.ID = generateServiceId(service.Name);

    for (var i = 0; i < paths.length; i++) {
      if (paths[i].indexOf("/") !== 0) {
        paths[i] = "/" + paths[i];
      }
    }

    if (mode === 'register') {
      self.registrator.registerResources(service, paths);
    } else {
      self.registrator.deregisterResources(service, paths);
    }
  }

  function generateServiceId(serviceName) {
    var serviceId = null;

    try {
      fs.accessSync(self.filename, fs.R_OK | fs.W_OK);
      serviceId = fs.readFileSync(fs.openSync(self.filename, 'r+')).toString();
    } catch (err) {
      serviceId = uuid() + '-' + serviceName;
      fs.writeSync(fs.openSync(self.filename, 'w+'), serviceId);
    }

    return serviceId;
  }
  function clearServiceId() {
    try {
      fs.unlinkSync(self.filename);
    } catch (err) {
      console.error("Service ID file could not be found or accessed.");
    }
  }
}
function CommandLineResolver() {
  var self = this;

  self.processArgs = function(mapping) {
    mapping = mapping || getCommandArgsMapping();

    var args = commandLineArgs(mapping);
    var settings = getSettings(args);
    validateSettings(settings);

    return settings;
  }

  function validateSettings(settings) {
    if (!settings.command) {
      throw new Error("Command parameter is missing.");
    } else if (!(settings.raml || settings.path)) {
      throw new Error("You must either provide path or file parameter.");
    }
  }
  function getSettings(args) {
    var settings = {
      service: getDefaultServiceValues()
    };

    // Fill config file data
    if (args['config-file']) {
      var config = JSON.parse(fs.readFileSync(args['config-file']).toString());
      fillBaseSettings(settings, config);

      if (config['service']) {
        for (var key in config.service) {
          settings.service[key] = config.service[key];
        }
      }
    }

    // Fill command line arguments
    fillBaseSettings(settings, args);
    if (args['name']) {
      settings.service.Name = args.name;
    }

    return settings;
  }
  function getCommandArgsMapping() {
    return [
      { name: 'config-file', alias: 'f', type: String },
      { name: 'command', alias: 'c', type: String },
      { name: 'name', alias: 'n', type: String },
      { name: 'raml', alias: 'r', type: String },
      { name: 'path', alias: 'p', type: String, multiple: true }
    ];
  }
  function getDefaultServiceValues() {
    return {
      'ID': null,
      'Name': null,
      'Port': 80,
      'Address': ip.address(),
      'Tags': [],
      'EnableTagOverride': false
    };
  }
  function fillBaseSettings(settings, args) {
    var keys = ['command', 'path', 'raml'];
    for (var i = 0; i < keys.length; i++) {
      var keyword = keys[i];
      if (args[keyword]) {
        settings[keyword] = args[keyword];
      }
    }
  }
}

// Function ran while executing as script
(function register() {
  function formatError(error) {
    var templates = {
      "original": "%s",
      "formatted": "%s\n(original message: \"%s\")"
    };

    var messages = {
      "ECONNRESET": "Could not reach Consul host: " + consulHost + ":" + consulPort
    };

    var errorMessage = error.message;
    var formatMessage = messages[error.code];

    return formatMessage ?
      util.format(templates.formatted, formatMessage, errorMessage) : errorMessage;
  }

  function awaitConsul(consulHost, consulPort) {
    var result;

    var settings = {
      host: consulHost,
      port: consulPort,
      path: '/v1/catalog/services'
    }

    for (var i = 0; i < 5; i++) {
      sleep(1000);
      var request = http.get(settings, function(response) {
        result = response;
      });

      while(!result) {
        deasync.runLoopOnce();
      }

      if (result.statusCode === 200) {
        return true;
      }
    }
    return false;
  }

  var consulHost = process.env.CONSUL_HOST || 'consul';
  var consulPort = process.env.CONSUL_PORT || '8500';

  if (!awaitConsul(consulHost, consulPort)) {
    throw new Error("The Consul service has not started up properly.");
  }

  var registration = new RegistrationService(consulHost, consulPort);

  try {
    // Retrieve arguments passed to script
    var args = new CommandLineResolver().processArgs();

    if (args.command === 'register') {
      registration.register(args);
    } else if (args.command === 'deregister') {
      registration.deregister(args);
    } else {
      throw new Error("Invalid command. It should be either 'register' or 'deregister'.")
    }
  } catch(err) {
    console.error("[ERROR]:")
    console.error(formatError(err));
    process.exit(1);
  }
})();
