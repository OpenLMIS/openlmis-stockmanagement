-- WHEN COMMITTING OR REVIEWING THIS FILE: Make sure that the timestamp in the file name (that serves as a version) is the latest timestamp, and that no new migration have been added in the meanwhile.
-- Adding migrations out of order may cause this migration to never execute or behave in an unexpected way.
-- Migrations should NOT BE EDITED. Add a new migration to apply changes.

CREATE INDEX phys_inv_adj_phys_inv_item_idx ON stockmanagement.physical_inventory_line_item_adjustments(physicalinventorylineitemid);
CREATE INDEX phys_inv_adj_reason_idx ON stockmanagement.physical_inventory_line_item_adjustments(reasonid);
CREATE INDEX phys_inv_adj_stock_card_item_idx ON stockmanagement.physical_inventory_line_item_adjustments(stockcardlineitemid);
CREATE INDEX physical_inventories_stockeventid_idx ON stockmanagement.physical_inventories(stockeventid);