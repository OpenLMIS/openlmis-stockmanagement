CREATE TABLE physical_inventory_line_item_reasons (
    id uuid NOT NULL,
    quantity integer NOT NULL,
    reasonId uuid NOT NULL,
    physicalInventoryLineItemId uuid,
    stockCardLineItemId uuid,
    stockEventLineItemId uuid
);
