# Error Handling Conventions

OpenLMIS would like to follow error handling best practices, this document covers the
conventions we'd _like_ to see followed in the various OpenLMIS components.

## Java and Spring

The Java community has a long-standing debate about the proper use of Exceptions.  This section 
attempts to be pragmatic about the use of exceptions - especially understanding the Spring 
community's exception handling techniques.

Exceptions in Java are broken down into two categories: those that are recovearable (checked) and 
those where client code can in no-way recover from the Exception (runtime).  OpenLMIS *strongly* 
discourages the use of checked exceptions, and the following section discusses what is encouraged
and why checked exceptions should be avoided.

### A pattern for normal error-handling

Normal errors for the purpose of this document are things like input validation or other business 
logic constraints.  There are a number of sources that make the claim that these types of errors 
are not exceptional (i.e. bad user input is to be expected normally) and therefore Java 
Exception's shouldn't be used.  While that's generally *very* good advice, we will be using 
runtime exceptions (not checked exceptions) as long as they follow the best practices laid out here.

The reasoning behind this approach is two-fold:

* Runtime exceptions are used when client code *can't* recover from their use.  Typically 
this has been used for the class of programming errors that indicate that the software encountered a 
completely unexpected programming error for which it should immediately terminate.  We 
expand this definition to include user-input validation and business logic constraints 
for which further user-action is required.  In that case the code can't recover - it has to 
receive something else before it could ever proceed, and while we don't want the program to 
terminate, we do want the current execution to cease so that it may pop back to a Controller level 
component that will convert these exceptions into the relevant (non-500) HTTP response.
* Using Runtime exceptions implies that we *never* write code that catches them. 
We will use Spring's `@ControllerAdvice` which will catch them for us, but our code should have 
less "clutter" as it'll be largely devoid of routine error-validation handling.

Effectively using this pattern requires the following rules:

1. The Exception type (class) that's thrown will map one-to-one with an HTTP Status code that we 
want to return, and this mapping will be true across the Service.  e.g. a 
`throw ValidationException` will always result in the HTTP Status code 400 being returned with the 
body containing a "nice message" (and not a stacktrace).
2. The exception thrown is a sub-type of `java.lang.RuntimeException`. 
3. Client code to a method that returns RuntimeException's should never try to handle the 
exception.  i.e. it should **not** `try {...} catch ...`
4. The only place that these RuntimeExceptions are handled is by a class annotated 
`@ControllerAdvice` that lives along-side all of the Controllers.
5. If the client code needs to report multiple errors (e.g. multiple issues in validating user 
input), then that collection of errors needs to be grouped before the exception is thrown.
6. A Handler should never be taking one of our exception types, and returning a HTTP 500 level 
status.  This class is reserved specifically to indicate that a programming error has occurred.  
Reserving this directly allows for easier searching of the logs for program-crashing type of errors.

#### Example

The exception
```java
public class ValidationException extends RuntimeException { ... }
```

A controller which uses the exception
```java
@Controller
public class WorkflowController {

  @RequestMapping(...)
  public WorkflowDraft doSomeWorkflow() {
    ...
    
    if (someError)
      throw new ValidationException(...);
    
    ...
    
    return new WorkflowDraft(...);
  }
}
```

The exception handler that's called by Spring should the `WorkflowController` throw 
`ValidationException`.
```java
@ControllerAdvice
public class WorkflowExceptionHandler {
 @ExceptionHandler(ValidationException.class)
 @ResponseStatus(HttpStatus.BAD_REQUEST)
 private Message.LocalizedMessage handleValidationException(ValidationException ve) { 
   ...
   return ve.getTheLocalizedMessage();
  }
}
```

### Exceptions - what we don't want

Lets look at a simple example that is indicative of the sort of code we've been writing using
exceptions.  This example consists of a web-endpoint that returns a setting for a given key, which
hands off the work to an application service layer that uses the key provided to find the given
setting.

A controller (HTTP end-point) that is asked to return some setting for a given "key"
```Java
@RequestMapping(value = "/settings/{key}",  method = RequestMethod.GET)
public ResponseEntity<?> getByKey(@PathVariable(value = "key") String key) {
  try {
    ConfigurationSetting setting = configurationSettingService.getByKey(key);
    return new ResponseEntity<>(setting, HttpStatus.OK);
  } catch (ConfigurationSettingException ex) {
    return new ResponseEntity(HttpStatus.NOT_FOUND);
  }
}
```

The service logic that finds the key and returns it (i.e. configurationSettingService above):
```Java
public ConfigurationSetting getByKey(String key) throws ConfigurationSettingException {
  ConfigurationSetting setting = configurationSettingRepository.findOne(key);
  if (setting == null) {
    throw new ConfigurationSettingException("Configuration setting '" + key + "' not found");
  }
  return setting;
}
```

In this example we see that the expected end-point behavior is to either return the setting asked
for and an HTTP 200 (success), or to respond with HTTP 404 - the setting was not found.

This usage of an Exception here is not what we want for a few reasons:

* The Controller directly handles the exception - it has a try-catch block.  It should only 
handle the successful path which is when the exception isn't thrown.  We should have a Handler 
which is `@ControllerAdvice`.
* The exception `ConfigurationSettingException` doesn't add anything - either semantically or 
functionally.  We know that this type of error isn't that there's some type of Configuration 
Setting problem, but rather that something wasn't found.  This could more generically and more 
accurately be named a `NotFoundException`.  It conveys the semantics of the error and one single 
Handler method for the entire Spring application could handle all `NotFoundExceptions` by 
returning a HTTP 404.
* It's worth noting that this type of null return is handled well in Java 8's Optional.  We would
still throw an exception at the Controller so that the Handler could handle the error, however 
an author of middle-ware code should be aware that they could use Optional instead of throwing 
an exception on a null immediately.  This would be most useful if many errors could occur - i.e.
in processing a stream.
* This code is flagged by static analysis 
[tools](http://sonar.openlmis.org/issues/search#issues=AVc18ErL0QRqkcp89olY) with the error that 
this exception should be "Either log or re-throw this exception".  A lazy programmer might 
"correct" this by logging the exception, however this would result in the log being permeated 
with noise from bad user input - which should be avoided.
 
## How the API responds with validation error messages

### What are Validation Error Messages?

In OpenLMIS APIs, validation errors can happen on PUT, POST, DELETE or even GET. When validation or
permissions are not accepted by the API, invalid requests should respond with a helpful validation
error message. This response has an HTTP response body with a simple JSON object that wraps the
message. Different clients may use this message as they wish, and may display it to end-users.

The Goal: We want the APIs to respond with validation error messages in a standard way. This will
allow the APIs and the UI components to all be coded and tested against one standard.

#### When does this pattern apply?

When does this "validation error message" pattern apply? We want to apply this pattern for all of
the error situations where we return a HTTP response body with an error message. For more details
about which HTTP status codes this aligns with, see the 'HTTP Status Codes' section below.

#### What do we return on Success?

In general, success responses should not include a validation message of the type specified here.
This will eliminate the practice which was done in OpenLMIS v2, EG:

```Javascript
PUT /requisitions/75/save.json
Response: HTTP 200 OK
Body: {"success":"R&R saved successfully!"}
```

On success of a PUT or POST, the API should usually return the updated resource with a HTTP 200
OK or HTTP 201 Created response code. On DELETE, if there is nothing appropriate to return, then
an empty response body is appropriate with a HTTP 204 No Content response code.

#### HTTP Status Codes

Success is generally a 2xx HTTP status code and we don't return validation error messages on
success. Generally, validation errors are 4xx HTTP status codes (client errors). Also, we don't
return these validation error messages for 5xx HTTP status codes (server or network errors).
We do not address 5xx errors because OpenLMIS software does not always have control over what the
stack returns for 5xx responses (those could come from NGINX or even a load balancer).

Examples below show appropriate use of HTTP 403 and 422 status codes with validation error messages.
The [OpenLMIS Service Style Guide](https://github.com/OpenLMIS/openlmis-template-service/blob/master/STYLE-GUIDE.md)
includes further guidance on HTTP Status Codes that comes from
[Best Practices for Designing a Pragmatic RESTful API](http://www.vinaysahni.com/best-practices-for-a-pragmatic-restful-api#http-status).

### Example: Permissions/RBAC
The API does a lot of permission checks in case a user tries to make a request without the needed
permissions. For example, a user may try to initiate a requisition at a facility where they don't
have permissions. That should generate a HTTP 403 Forbidden response with a JSON body like this:

```Javascript
{
  "message" : "Action prohibited because user does not have permission at the facility",
  "messageKey" : "requisition.error.prohibited.no-facility-permission"
}
```

When creating these error validation messages, we encourage developers to avoid repeating code.
It may be appropriate to write a helper class that generates these JSON validation error responses
with a simple constructor.

We also don't want developers to spend lots of time authoring wordy messages. It's best to keep the
messages short, clear and simple.

### Translation/i18n

Message keys are used for translations. Keys should follow our
[Style Guide i18n Naming Conventions](https://github.com/OpenLMIS/openlmis-template-service/blob/master/STYLE-GUIDE.md#i18n-naming-conventions).

The "messageKey" is the key into a property translation file such as a
[.properties file](http://docs.transifex.com/formats/java-properties/) maintained using Transifex
or a similar tool.

The "messageKey" will be used with translation files in order to conduct translation, which we
allow and support on the server-side and/or the client-side. Any OpenLMIS instance may configure
translation to happen in its services or its clients.

A service will use the "messageKey" to translate responses into a different language server-side in
order to respond in the language of choice for that OpenLMIS implementation instance. And/or a
client/consumer may use the "messageKey" to translate responses into a language of choice.

The source code where a validation error is handled should have the "messageKey" only. The source
code should not have hard-coded message strings in English or any language.

#### Future: Messages with Placeholders for Translation

In the future, we may want to extend this pattern to support placeholder variables in a message.
For example, "Action prohibited because user {0} does not have permission {1} at facility {2}".

The Transifex tool appears to support different types of placeholders, such as {0} or %s and %d.
In OpenLMIS v2, the MessageService (called the Notification Service in v3) uses placeholders to
make email messages translate-able. For an example, see the
[StatusChangeEventService](https://github.com/OpenLMIS/open-lmis/blob/master/modules/core/src/main/java/org/openlmis/core/service/StatusChangeEventService.java#L62).

#### Future: Arrays of Messages

In the future, we may extend these guidelines to support an array of multiple messages.

#### Future: Identifying Fields Where Validation Was Not Accepted

In the future, it may also be helpful to extend this to allow the error messages to be associated
with a specific piece of data. For example, if a Requisition Validation finds that line item
quantities do not add up correctly, it could provide an error message tied to a specific product
(line item) and field. Often this kind of validation may be done by the client (such as in the
AngularJS UI app), and the client can immediately let the end-user know about a specific field
with a validation error.

### Proposed RAML

```Javascript
schemas:
  - errorResponse: |
    { "type": "object",
      "$schema": "http://json-schema.org/draft-03/schema",
      "title": "ErrorResponse",
      "description": "Error response",
      "properties": {
        "message": { "type": "string", "required": true, "title": "error message" },
        "messageKey": { "type": "string", "required": true, "title": "key for translations" }
      }
    }

/requisitions:
  /{id}:
    put:
      description: Save a requisition with its line items
      responses:
        403:
        422:
          body:
            application/json:
              schema: errorResponse
```
