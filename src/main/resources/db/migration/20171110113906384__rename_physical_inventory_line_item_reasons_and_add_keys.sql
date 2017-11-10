ALTER TABLE physical_inventory_line_item_reasons RENAME TO physical_inventory_line_item_adjustments;

ALTER TABLE physical_inventory_line_item_adjustments ADD PRIMARY KEY (id);

ALTER TABLE physical_inventory_line_item_adjustments
  ADD CONSTRAINT fk_phys_inv_adj_phys_inv_item
  FOREIGN KEY (physicalinventorylineitemid)
  REFERENCES physical_inventory_line_items(id);

ALTER TABLE physical_inventory_line_item_adjustments
  ADD CONSTRAINT fk_phys_inv_adj_stock_card_item
  FOREIGN KEY (stockcardlineitemid)
  REFERENCES stock_card_line_items(id);

ALTER TABLE physical_inventory_line_item_adjustments
  ADD CONSTRAINT fk_phys_inv_adj_stock_event_item
  FOREIGN KEY (stockeventlineitemid)
  REFERENCES stock_event_line_items(id);

ALTER TABLE physical_inventory_line_item_adjustments
  ADD CONSTRAINT fk_phys_inv_adj_reason
  FOREIGN KEY (reasonid)
  REFERENCES stock_card_line_item_reasons(id);
