ALTER TABLE physical_inventory_line_items ADD COLUMN extradata jsonb;
ALTER TABLE stock_event_line_items ADD COLUMN extradata jsonb;