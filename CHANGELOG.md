4.1.2 / Work in progress
==================

New functionality:
* [OLMIS-6368](https://openlmis.atlassian.net/browse/OLMIS-6368): Create SoH fact table.
* [OLMIS-6434](https://openlmis.atlassian.net/browse/OLMIS-6434): Calculated and populated Stock on Hand values to a new table.

Improvements:
* [OLMIS-5569](https://openlmis.atlassian.net/browse/OLMIS-5569): Moved reason TRANSFER_IN from demo data to bootstrap data.
* [OLMIS-6408](https://openlmis.atlassian.net/browse/OLMIS-6408): Added pageable validator.
* [OLMIS-6474](https://openlmis.atlassian.net/browse/OLMIS-6474): Performance improvements of `GET /api/orderableFulfills` endpoint.

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
