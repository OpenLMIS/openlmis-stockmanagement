ALTER TABLE stock_cards
ALTER COLUMN unitoforderableid SET NOT NULL;

ALTER TABLE stock_event_line_items
ALTER COLUMN unitoforderableid SET NOT NULL;

ALTER TABLE physical_inventory_line_items
ALTER COLUMN unitoforderableid SET NOT NULL;
