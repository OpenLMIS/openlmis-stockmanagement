# OpenLMIS Service Style Guide
This is a WIP as a style guide for an Independent Service. Clones of this file should reference 
this definition.

For the original style guide please see:
https://github.com/OpenLMIS/open-lmis/blob/master/STYLE-GUIDE.md

---

## Java
OpenLMIS has [adopted](https://groups.google.com/d/msg/openlmis-dev/CCwBglBFbpk/pY406WbkAAAJ) the
[Google Java Styleguide](https://google.github.io/styleguide/javaguide.html).  These checks are
*mostly* encoded in Checkstyle and should be enforced for all contributions.

Some additional guidance:

* Try to keep the number of packages to a minimum. An Independent Service's Java code should 
generally all be in one package under `org.openlmis` (e.g. `org.openlmis.requisition`).
* Sub-packages below that should generally follow layered-architecture conventions; most (if not 
all) classes should fit in these four: `domain`, `repository`, `service`, `web`. To give specific
 guidance:
    * Things that do not strictly deal with the domain should NOT go in the `domain` package.
    * Serializers/Deserializers of domain classes should go under `domain`, since they have 
    knowledge of domain object details.
    * DTO classes, belonging to serialization/deserialization for endpoints, should go under `web`.
    * Exception classes should go with the classes that throw the exception.
    * We do not want separate sub-packages called `exception`, `dto`, `serializer` for these 
    purposes.
* When wanting to convert a domain object to/from a DTO, define Exporter/Importer interfaces for 
the domain object, and export/import methods in the domain that use the interface methods. Then 
create a DTO class that implements the interface methods. (See [Right](https://github.com/OpenLMIS/openlmis-referencedata/blob/master/src/main/java/org/openlmis/referencedata/domain/Right.java)
 and [RightDto](https://github.com/OpenLMIS/openlmis-referencedata/blob/master/src/main/java/org/openlmis/referencedata/dto/RightDto.java)
for details.)
    * Additionally, when Exporter/Importer interfaces reference relationships to other domain 
    objects, their Exporter/Importer interfaces should also be used, not DTOs. (See [example](https://github.com/OpenLMIS/openlmis-referencedata/blob/master/src/main/java/org/openlmis/referencedata/domain/Role.java#L198).)
* Even though the no-argument constructor is required by Hibernate for entity objects, do not use
it for object construction; use provided constructors or static factory methods. If one does not 
exist, create one using common sense parameters.

## RESTful Interface Design & Documentation
Designing and documenting 

Note: many of these guidelines come from
[Best Practices for Designing a Pragmatic RESTful API](http://www.vinaysahni.com/best-practices-for-a-pragmatic-restful-api).

* Result filtering, sorting and searching should be done by query parameters.
[Details](http://www.vinaysahni.com/best-practices-for-a-pragmatic-restful-api#advanced-queries)
* Return a resource representation after a create/update.
[Details](http://www.vinaysahni.com/best-practices-for-a-pragmatic-restful-api#useful-post-responses)
* Use camelCase (vs. snake_case) for names, since we are using Java and JSON.
[Details](http://www.vinaysahni.com/best-practices-for-a-pragmatic-restful-api#snake-vs-camel)
* Don't use response envelopes as default (if not using Spring Data REST).
[Details](http://www.vinaysahni.com/best-practices-for-a-pragmatic-restful-api#envelope)
* Use JSON encoded bodies for create/update.
[Details](http://www.vinaysahni.com/best-practices-for-a-pragmatic-restful-api#json-requests)
* Use a clear and consistent error payload.
[Details](http://www.vinaysahni.com/best-practices-for-a-pragmatic-restful-api#errors)
* Use the HTTP status codes effectively.
[Details](http://www.vinaysahni.com/best-practices-for-a-pragmatic-restful-api#http-status)
* Resource names should be pluralized and consistent.  e.g. prefer `requisitions`, never 
`requisition`.
* A PUT on a single resource (e.g. PUT /facilities/{id}) is not strictly an update; if the 
resource does not exist, one should be created using the specified identity (assuming the 
identity is a valid UUID).
* Exceptions, being thrown in exceptional circumstances (according to *Effective Java* by Joshua 
Bloch), should return 500-level HTTP codes from REST calls.
* Not all domain objects in the services need to be exposed as REST resources. Care should be 
taken to design the endpoints in a way that makes sense for clients. Examples:
    * `RoleAssignment`s are managed under the users resource. Clients just care that users have 
    roles; they do not care about the mapping.
    * `RequisitionGroupProgramSchedule`s are managed under the requisitionGroups resource. 
    Clients just care that requisition groups have schedules (based on program).
* RESTful endpoints that simply wish to return a JSON value (boolean, number, string) should wrap
 that value in a JSON object, with the value assigned to the property "result". (e.g. `{ 
 "result": true }`)
    * Note: this is to ensure compliance with all JSON parsers, especially ones that adhere to 
    RFC4627, which do not consider JSON values to be valid JSON. See the discussion
    [here](http://stackoverflow.com/questions/18419428/what-is-the-minimum-valid-json).

We use RAML (0.8) to document our RESTful APIs, which are then converted into HTML for static API 
documentation or Swagger UI for live documentation. Some guidelines for defining APIs in RAML:

* JSON schemas for the RAML should be defined in a separate JSON file, and placed in a `schemas` 
subfolder in relation to the RAML file. These JSON schema files would then be referenced in the 
RAML file like this (using role as an example):
    ```
    - role: !include schemas/role.json
    
    - roleArray: |
      {
        "type": "array",
        "items": { "type": "object", "$ref": "schemas/role.json" }
      }
    ```

    * (Note: this practice has been established because RAML 0.8 cannot define an array of a JSON
    schema for a request/response body ([details](http://forums.raml.org/t/set-body-to-be-array-of-defined-schema-objects/1566/3)).
    If the project moves to the RAML 1.0 spec and our [RAML testing tool](https://github.com/nidi3/raml-tester)
    adds support for RAML 1.0, this practice might be revised.)

## Postgres Database
In most cases, the Hibernate DefaultNamingStrategy follows these conventions. Schemas and table 
names will however need to be specified.

* Each Independent Service should store it's tables in its own schema.  The convention is to use 
the Service's name as the schema.  e.g. The Requistion Service uses the `requisition` schema
* Tables, Columns, constraints etc should be all lower case.
* Table names should be pluralized.  This is to avoid *most* used words. e.g. orders instead of 
order
* Table names with multiple words should be snake_case.
* Column names with multiple words should be merged together.  e.g. `getFirstName()` would map to
 `firstname`

## i18n Naming Conventions
These naming conventions will be applicable for the messages property files.

* Keys for the messages property files should follow a hierarchy. However, since there is no 
official hierarchy support for property files, keys should follow a naming convention of most to 
least significant.
* Key hierarchy should be delimited with a period (.).
* The first portion of the key should be the name of the Independent Service.
* The second portion of the key should indicate the type of message; error for error messages, 
message for anything not an error.
* The third and following portions will further describe the key.
* Keys should use only lowercase characters (NO camelCase).
* If multiple words are necessary, they should be separated with a dash (-).

Examples:

* `requisition.error.product.code.invalid` - an alternative could be `requisition.error
.product-code.invalid` if code is not a sub-section of product.
* `requisition.message.requisition.created` - requisition successfully created.
* `reference-data.error.facility.not-found` - facility not found.

Note: UI-related keys (labels, buttons, etc.) are not addressed here, as they would be owned by the
UI, and not the Independent Service.

## Testing

See the [Testing Guide](TESTING.md).

## Docker <a href="docker"></a>

Everything deployed in the reference distribution needs to be a Docker container.  Official OpenLMIS containers are made from their respective containers that are published for all to see on our [Docker Hub](https://hub.docker.com/u/openlmis/).

* Dockerfile (Image) [best practices](https://docs.docker.com/engine/userguide/eng-image/dockerfile_best-practices/)
* Keep Images portable & one-command focused.  You should be comfortable publishing these images publicly and openly to the DockerHub.
* Keep Containers ephemeral.  You shouldn't have to worry about throwing one away and starting a new one.
* Utilize docker compose to launch containers as services and map resources
* An OpenLMIS Service should be published in one image found on Docker Hub
* Services and Infrastructure that the OpenLMIS tech committee owns are published under the "openlmis" namespace of docker and on the Docker Hub.
* Avoid [Docker Host Mounting](https://docs.docker.com/engine/tutorials/dockervolumes/#/mount-a-host-directory-as-a-data-volume), as this doesn't work well when deploying to remote hosts (e.g. in CI/CD)

## Gradle Build
Pertaining to the build process performed by Gradle.

* Anything generated by the Gradle build process should go under the `build` folder (nothing 
generated should be in the `src` folder).