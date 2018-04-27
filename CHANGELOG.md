3.1.0 / WIP
==================

New functionality added in a backwards-compatible manner:
* [OLMIS-4599](https://openlmis.atlassian.net/browse/OLMIS-4599): Add ability to retrieve a stock card reason by id

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
