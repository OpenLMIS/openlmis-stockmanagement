5.2.0 / wip
==================

**Requires referencedata:15.3.0 or later**

Improvements:
* [OE-86](https://openlmis.atlassian.net/browse/OE-86): Added /api/public/stockCardSummaries endpoint which is
 an equivalent of /api/v2/stockCardSummaries endpoint tailored for external integrations
* [OE-87](https://openlmis.atlassian.net/browse/OE-87): Added /api/public/stockEvents endpoint which is 
 an equivalent of /api/stockEvents endpoint tailored for external integrations

5.1.12 / 19.11.2024
==================

Patch release with performance improvements.

Improvements:
* [SELV3-770](https://openlmis.atlassian.net/browse/SELV3-770): Improve performance of filtering for Valid Sources and
 Valid Destinations

5.1.11 / 31.10.2024
==================

Bug fixes:
* [OLMIS-8020](https://openlmis.atlassian.net/browse/OLMIS-8020): Fix issue where it was not possible to create a requisition 
when a program had `Enable Stock on Hand to populate from stock cards` flag checked

Improvements:
* [OLMIS-7895](https://openlmis.atlassian.net/browse/OLMIS-7895): Add demo data for BUQ and TB Monthly
* [OLMIS-7953](https://openlmis.atlassian.net/browse/OLMIS-7953): Improve some API calls performance
* [OIS-14](https://openlmis.atlassian.net/browse/OIS-14): Upgrade Transifex API version
* [OIS-48](https://openlmis.atlassian.net/browse/OIS-48): Update service base images to versions without known vulnerabilities
* [SELV3-718](https://openlmis.atlassian.net/browse/SELV3-718): Add filtering by geographic zone for valid sources and destinations

5.1.10 / 2024-04-19
==================

Bug fixes:
* [OLMIS-7910](https://openlmis.atlassian.net/browse/OLMIS-7910): Fixed wrong stock on hand on first Receive

5.1.9 / 2023-06-26
==================

Bug fixes:
* [OD-37](https://openlmis.atlassian.net/browse/OD-37): Fixed wrong stockout days calculation

5.1.8 / 2023-04-05
==================

Bug fixes:
* [OLMIS-7373](https://openlmis.atlassian.net/browse/OLMIS-7373): Fixed filtering by lot code in stock on hand
* [OLMIS-7711](https://openlmis.atlassian.net/browse/OLMIS-7711): Fixed wrong stockout days calculation

5.1.7 / 2022-10-07
==================

Bug fixes:
* [OLMIS-7577](https://openlmis.atlassian.net/browse/OLMIS-7577): Fixed issue with adjustment  not shown in requisition
* [OLMIS-7590](https://openlmis.atlassian.net/browse/OLMIS-7590): Fixed wrong stockOutDays calculation

5.1.6 / 2022-04-21
==================

Improvements:
* [OLMIS-7430](https://openlmis.atlassian.net/browse/OLMIS-7430): Add logic to set flag to inactive, filter all inactive items on physical inventory and stock on hand pages
* [OLMIS-7433](https://openlmis.atlassian.net/browse/OLMIS-7433): Add logic to set flag to active - on stock events
* [OLMIS-7568](https://openlmis.atlassian.net/browse/OLMIS-7568): Use openlmis/dev:7 and openlmis/service-base:6.1

Breaking changes:
* [OLMIS-7472](https://openlmis.atlassian.net/browse/OLMIS-7472): Upgrade postgres to v12

5.1.5 / 2021-12-13
==================

Bug fixes:
* [OLMIS-7442](https://openlmis.atlassian.net/browse/OLMIS-7442): Fix timeout on /api/validDestinations endpoint
* [OLMIS-7387](https://openlmis.atlassian.net/browse/OLMIS-7387): Fix pagination for filtered /api/validSources endpoint

5.1.4 / 2021-10-29
==================

Improvements:
* [OLMIS-7298](https://openlmis.atlassian.net/browse/OLMIS-7298): Added page and size parameters for /validSources and /validDestinations endpoints.
* [OLMIS-7370](https://openlmis.atlassian.net/browse/OLMIS-7370): Added flag for active/inactive for stock card.

5.1.3 / 2020-12-17
==================

Bug fixes:
* [OLMIS-7169](https://openlmis.atlassian.net/browse/OLMIS-7169): Fixed issue with not closing a connection after generating Jasper reports.

5.1.2 / 2020-11-16
==================

New functionality added in a backwards-compatible manner:
* [OLMIS-6911](https://openlmis.atlassian.net/browse/OLMIS-6911): Added extension points for AdjustmentReason FreeText and UnpackKit validators.
* [OLMIS-6954](https://openlmis.atlassian.net/browse/OLMIS-6954): Published Stock Management service to Maven.

Improvements:
* [OLMIS-6901](https://openlmis.atlassian.net/browse/OLMIS-6901): Updated StockCardSummaryV2Dto to include versioned object reference of orderable.
* [OLMIS-6899](https://openlmis.atlassian.net/browse/OLMIS-6899): Physical Inventory submission will no longer accept drafts that are already marked as submitted in the database.

Bug fixes:
* [OLMIS-6848](https://openlmis.atlassian.net/browse/OLMIS-6848): Fixed NPE during sending notification of near expiry
* [OLMIS-6849](https://openlmis.atlassian.net/browse/OLMIS-6849): Fixed subject and content of the notification of near expiry


5.1.1 / 2020-06-01
==================

Bug fixes:
* [OLMIS-6860](https://openlmis.atlassian.net/browse/OLMIS-6860): Fixed duplicated Physical Inventory Line Items by reverting OLMIS-6574

5.1.0 / 2020-05-20
==================

New functionality added in a backwards-compatible manner:
* [OLMIS-6772](https://openlmis.atlassian.net/browse/OLMIS-6772): Update Spring Boot version to 2.x:
  * Spring Boot version is 2.2.2.
  * Flyway is at 6.0.8, new mechanism for loading Spring Security for OAuth2 (matching Spring Boot version), new versions for REST Assured, RAML tester, RAML parser, PowerMock, Mockito (so tests will pass) and Java callback mechanism has changed to a general handle() method.
  * Spring application properties for Flyway have changed.
  * Re-implement generation of Jasper reports.
  * Fix repository method signatures (findOne is now findById, etc.); additionally they return Optional.
  * Fix unit tests.
  * Fix integration tests.
  * API definitions require "Keep-Alive" header for web integration tests.

Bug fixes:
* [OLMIS-6853](https://openlmis.atlassian.net/browse/OLMIS-6853): Fixed missing products by reverting OLMIS-6614

5.0.2 / 2020-04-14
==================

Bug fixes:
* [OLMIS-6722](https://openlmis.atlassian.net/browse/OLMIS-6722): Fixed slow page load on Physical Inventory page by adding indexes to the database.
* [OLMIS-6679](https://openlmis.atlassian.net/browse/OLMIS-6679): Fixed Internal Server Error for GET /stockCardRangeSummaries by adding a new method.
* [OLMIS-6728](https://openlmis.atlassian.net/browse/OLMIS-6728): Fixed incorrect calculation of Stock on Hand.
* [OLMIS-6574](https://openlmis.atlassian.net/browse/OLMIS-6574): Fixed issues that Printed Stock on Hand report contained more products than were displayed on the Stock on hand screen and Physical Inventory screen:
    *the screens now display Stock Card Summaries of Orderables both with and without Identifiers.
* [OLMIS-6559](https://openlmis.atlassian.net/browse/OLMIS-6559): Fixed long product codes being cut in Physical Inventory printout.
* [OLMIS-6745](https://openlmis.atlassian.net/browse/OLMIS-6745): Fixed incorrect calculation of Stock on Hand for edge cases.

Improvements:
* [OLMIS-3490](https://openlmis.atlassian.net/browse/OLMIS-3490): Performance improvements of Validate step in stock event.
* [OLMIS-6759](https://openlmis.atlassian.net/browse/OLMIS-6759): Improved the error message when SoH is below zero.

5.0.1 / 2019-10-21
==================

Bug fixes:
* [OLMIS-6630](https://openlmis.atlassian.net/browse/OLMIS-6630): Fixed bug with retrieving stock card summaries.

5.0.0 / 2019-10-17
==================

Contract breaking changes:
* [OLMIS-6556](https://openlmis.atlassian.net/browse/OLMIS-6556): Add support for limiting sources and destinations
  * During work on this ticket parameters for /api/validSources and /api/validDestinations changed.

New functionality:
* [OLMIS-6368](https://openlmis.atlassian.net/browse/OLMIS-6368): Create SoH fact table.
* [OLMIS-6434](https://openlmis.atlassian.net/browse/OLMIS-6434): Calculated and populated Stock on Hand values to a new table.
* [OLMIS-6483](https://openlmis.atlassian.net/browse/OLMIS-6483): Added saving calculated SoH value while sensing stock event and using it during getting stock card info.
* [OLMIS-6558](https://openlmis.atlassian.net/browse/OLMIS-6558): Add new environment variable - PUBLIC_URL and use to for email generated links
* [OLMIS-6573](https://openlmis.atlassian.net/browse/OLMIS-6573): Added possibility to fetch all valid sources/destinations via API.

Improvements:
* [OLMIS-5569](https://openlmis.atlassian.net/browse/OLMIS-5569): Moved reason TRANSFER_IN from demo data to bootstrap data.
* [OLMIS-6408](https://openlmis.atlassian.net/browse/OLMIS-6408): Added pageable validator.
* [OLMIS-6474](https://openlmis.atlassian.net/browse/OLMIS-6474): Performance improvements of `GET /api/orderableFulfills` endpoint.
* [OLMIS-6564](https://openlmis.atlassian.net/browse/OLMIS-6564): Changed wiremock dependency configuration to avoid issue with HTTP response compression.
* [OLMIS-6614](https://openlmis.atlassian.net/browse/OLMIS-6614): Replaced multiple calls to reference data by one call to the `GET /api/orderableFulfills` endpoint.

Bug fixes:
* [OLMIS-6582](https://openlmis.atlassian.net/browse/OLMIS-6582): Changed wiremock dependency configuration to fix printing Physical Inventory.
* [OLMIS-6590](https://openlmis.atlassian.net/browse/OLMIS-6590): Fixed recalculating existing calculated stock on hand for physical inventory.

4.1.1 / 2019-05-27
==================

Improvements:
* [OLMIS-6335](https://openlmis.atlassian.net/browse/OLMIS-6335): Improved profiling for /api/v2/stockCardSummaries endpoint.

Bug fixes, security and performance improvements, also backwards-compatible:
* [OLMIS-6304](https://openlmis.atlassian.net/browse/OLMIS-6304): Fixed displaying product names in reports.
* [OLMIS-6531](https://openlmis.atlassian.net/browse/OLMIS-6531): Fix: only the newest version of a product is visible in the Physical Inventory printout.

4.1.0 / 2019-05-27
==================

New functionality added in a backwards-compatible manner:
* [OLMIS-3186](https://openlmis.atlassian.net/browse/OLMIS-3186): Add check for lots that are near expiry (6 months) and send notification.
* [OLMIS-682](https://openlmis.atlassian.net/browse/OLMIS-682): Unpack kits into stock.

Bug fixes, security and performance improvements, also backwards-compatible:
* [OLMIS-5544](https://openlmis.atlassian.net/browse/OLMIS-5544): Fixed issue with stockout notification being sent for some stock events incorrectly.
* [OLMIS-3210](https://openlmis.atlassian.net/browse/OLMIS-3210): Physical inventory PUT endpoint should create draft with given id from path.
* [OLMIS-6005](https://openlmis.atlassian.net/browse/OLMIS-6005): Moved searching for rights to avoild multiple requests for the same data.
* [OLMIS-4531](https://openlmis.atlassian.net/browse/OLMIS-4531): Added compressing HTTP POST responses.
* [OLMIS-6176](https://openlmis.atlassian.net/browse/OLMIS-6176): Added missing migration with Physical Inventory report.
* [OLMIS-6193](https://openlmis.atlassian.net/browse/OLMIS-6193): Fixed translations for stockout notifications.

4.0.0 / 2018-12-12
==================

Contract breaking changes:
* [OLMIS-4756](https://openlmis.atlassian.net/browse/OLMIS-4756): Can't change `type` or `category` for stock card line item reason on update.

Improvements:
* [OLMIS-4940](https://openlmis.atlassian.net/browse/OLMIS-4940): Ensured that the microservice gets system time zone from configuration settings on startup.
* [OLMIS-4295](https://openlmis.atlassian.net/browse/OLMIS-4295): Updated checkstyle to use newest google style.
* [OLMIS-5083](https://openlmis.atlassian.net/browse/OLMIS-5083): Added the correct date format in the message when date format error occurred.
* [OLMIS-4942](https://openlmis.atlassian.net/browse/OLMIS-4942): Added currency, number and date settings to application properties.
* [OLMIS-4943](https://openlmis.atlassian.net/browse/OLMIS-4943): Fixed Jasper reports to use service locale settings.
* [OLMIS-5635](https://openlmis.atlassian.net/browse/OLMIS-5635): Adjusted supervisory node structure
* [OLMIS-5601](https://openlmis.atlassian.net/browse/OLMIS-5601): Categorise physical inventory line item under program product category for PDF export
* [OLMIS-4583](https://openlmis.atlassian.net/browse/OLMIS-4583): Added valid source for Balaka District Warehouse

3.1.0 / 2018-08-16
==================

New functionality added in a backwards-compatible manner:
* [OLMIS-4599](https://openlmis.atlassian.net/browse/OLMIS-4599): Add ability to retrieve a stock card reason by id
* [OLMIS-4598](https://openlmis.atlassian.net/browse/OLMIS-4598): Add tags to StockCardLineItemReason
* [OLMIS-4597](https://openlmis.atlassian.net/browse/OLMIS-4597): Add an endpoint to retrieve all tags associated with reasons.
* [OLMIS-4622](https://openlmis.atlassian.net/browse/OLMIS-4622): Updated valid reason search endpoint to not require program and facility type and accept reason parameter.
* [OLMIS-4746](https://openlmis.atlassian.net/browse/OLMIS-4746): Added Stock Card Range Summaries endpoint.
* [OLMIS-4404](https://openlmis.atlassian.net/browse/OLMIS-4404): Added nonEmptyOnly flag to the v2 stockCardSummaries endpoint.

Improvements:
* [OLMIS-4648](https://openlmis.atlassian.net/browse/OLMIS-4648): Added Jenkinsfile
* [OLMIS-2923](https://openlmis.atlassian.net/browse/OLMIS-2923): Updated demo data loading approach
* [OLMIS-4717](https://openlmis.atlassian.net/browse/OLMIS-4717): Added default tags for bootstrap reasons.
* [OLMIS-4905](https://openlmis.atlassian.net/browse/OLMIS-4905): Updated notification service to use v2 endpoint.
* [OLMIS-4866](https://openlmis.atlassian.net/browse/OLMIS-4866): Added demo data for Stock Based Requisitions.

3.0.0 / 2018-04-24
==================

Contract breaking changes:
* [OLMIS-3295](https://openlmis.atlassian.net/browse/OLMIS-3295): Modified stock event structure
  * adjustments in each stock event line item contains only reasonId and quantity fields
* [OLMIS-3921](https://openlmis.atlassian.net/browse/OLMIS-3921): Removed validations from the Stock Management Physical Inventory API to allow reasons that do not account for the entire quantity. There may be a positive or negative unaccounted for quantity

New functionality:
* [OLMIS-4052](https://openlmis.atlassian.net/browse/OLMIS-4052): Added version 2 of stock card summaries endpoint that combines both regular and "no cards" endpoints.

Improvements:
* [OLMIS-3614](https://openlmis.atlassian.net/browse/OLMIS-3614): Added extraData field to Stock Card Summary.
* [OLMIS-3996](https://openlmis.atlassian.net/browse/OLMIS-3996): Added reasonType parameter to GET /validReasons endpoint.
* [OLMIS-4227](https://openlmis.atlassian.net/browse/OLMIS-4227): Added stock cards for Lurio in demo data.

Bug fixes, security and performance improvements, also backwards-compatible:

* [OLMIS-3533](https://openlmis.atlassian.net/browse/OLMIS-3533): Avoid creating duplicate stock cards for the same pair of orderable and lot
* [OLMIS-3485](https://openlmis.atlassian.net/browse/OLMIS-3485): Fixed jasper report for stock view and summaries
* [OLMIS-3135](https://openlmis.atlassian.net/browse/OLMIS-3135): Handle API Key requests.
  * For now all requests are blocked.
* [OLMIS-3874](https://openlmis.atlassian.net/browse/OLMIS-3874): When getting orderables for stock cards, do not use FTAPs, but all orderables, to avoid a NullPointerException.
* [OLMIS-3820](https://openlmis.atlassian.net/browse/OLMIS-3820): Add lot stock cards to Cuamba and Assumane in demo data.
* [OLMIS-3778](https://openlmis.atlassian.net/browse/OLMIS-3778): Fixed service checks the rights of a wrong user
* [OLMIS-4310](https://openlmis.atlassian.net/browse/OLMIS-4310): All valid reasons, sources and destinations can now be retrieved by any authenticated user.
* [OLMIS-4335](https://openlmis.atlassian.net/browse/OLMIS-4335): Split huge requests to other services into smaller chunks
* [OLMIS-4281](https://openlmis.atlassian.net/browse/OLMIS-4281): Updated Orderable service to use new reference data API

2.0.0 / 2017-11-09
==================

Contract breaking changes:

* [OLMIS-2732](https://openlmis.atlassian.net/browse/OLMIS-2732): Print submitted physical inventory
  * During work on this ticket physical inventory API was redesigned to be RESTful.

New functionality that are backwards-compatible
* [OLMIS-3246](https://openlmis.atlassian.net/browse/OLMIS-3246): Add column hidden to valid reasons
  * Default value is false.
  * Updated demo data so reason assignments for Consumed, Receipts, Beginning Balance Excess and Beginning Balance Insufficiency are hidden.

Bug fixes, security and performance improvements, also backwards-compatible:

* [OLMIS-3148](https://openlmis.atlassian.net/browse/OLMIS-3148): Added missing messages for error keys
* [OLMIS-3346](https://openlmis.atlassian.net/browse/OLMIS-3346): Increase performance of POST /stockEvents endpoint by reducing db calls and use lazy-loading in the stock event process context. Also changed logic for notification of stockout to asynchronous.

1.0.0 / 2017-09-01
==================

* Released openlmis-stockmanagement 1.0.0 as part of openlmis-ref-distro 3.2.0.
 * This was the first stable release of openlmis-stockmanagement.
