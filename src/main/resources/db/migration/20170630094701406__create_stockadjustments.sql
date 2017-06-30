CREATE TABLE stock_adjustments (
    id uuid NOT NULL,
    quantity integer NOT NULL,
    reasonId uuid NOT NULL,
    physicalInventoryLineItemId uuid
);
