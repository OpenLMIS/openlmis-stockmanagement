# Demo Data for OpenLMIS Stock Management Service
This folder holds demo data for the stock management service. The demo data is used by 
developers, QA staff, and is automatically loaded into some environments for demo and testing 
purposes. It is not for use in production environments.

Each .json file contains demo data that corresponds to one database table.

## Reasons

Defined in stockmanagement.stock_card_line_item_reasons.json and 
stockmanagement.valid_reason_assignments.json.

These reasons are also used by the requisition service for a requisition line item's losses and 
adjustments.

* Transfer In
* Transfer Out
* Facility return
* Damage
* Expired
* Stolen
* Lost
* Passed open-vial time limit
* Cold chain failure

Reasons are not visible until they are assigned in stockmanagement.valid_reason_assignments.json.

For the EPI program, all reasons are mapped to all facility types in the system.

## Sources, Destinations, Nodes and Organizations

Sources and destinations are for issuing and receiving stock. These are defined in 
stockmanagement.valid_destination_assignments.json and 
stockmanagement.valid_source_assignments.json. They are by facility type, program and node. Nodes
(stockmanagement.nodes.json) are mostly facilities, but can also be organizations 
(stockmanagement.organizations.json) configured in the system.

For the EPI program:
* Destinations
  * For the Regional Store type, the two district stores are valid destinations
  * For the District Store type, the health facilities are valid destinations
* Sources
  * For the Health Center type, the two district stores are valid sources
  * For the District Store type, the one regional store is a valid source
  * For the Regional Store type, the national warehouse is a valid source

## Stock Cards

Defined in stockmanagement.stock_cards.json and stockmanagement.stock_card_line_items.json.

For the EPI program, all vaccine products have a stock card at the Cuamba district store, and the
facilities Cuamba (N003) in the Cuamba district and Assumane (N036) in the Lichinga district. An 
initial stock card was created for each product and a few stock movements are recorded.

## Physical Inventories

Defined in stockmanagement.physical_inventories.json and 
stockmanagement.physical_inventory_line_items.json.

For the EPI program, two physical inventories are recorded for two health facilities (Cuamba in the
Cuamba district and Assumane in the Lichinga district).

## Stock Events

Defined in stockmanagement.stock_events.json and stockmanagement.stock_event_line_items.json.

All stock movements in the system are recorded as stock events. There are demo stock events that 
correspond to the recorded stock movements.

Facilities, Facility Types, Programs, Products, and User Roles & Rights come from the
[Reference Data service's demo data](https://github.com/OpenLMIS/openlmis-referencedata/tree/master/demo-data).
