CREATE TABLE available_stock_card_fields
(
    id UUID PRIMARY KEY NOT NULL,
    name VARCHAR(255) NOT NULL
);
CREATE TABLE available_stock_card_line_item_fields
(
    id UUID PRIMARY KEY NOT NULL,
    name VARCHAR(255) NOT NULL
);
CREATE TABLE nodes
(
    id UUID PRIMARY KEY NOT NULL,
    isrefdatafacility BOOLEAN NOT NULL,
    referenceid UUID NOT NULL
);
CREATE TABLE organizations
(
    id UUID PRIMARY KEY NOT NULL,
    name TEXT NOT NULL
);
CREATE TABLE physical_inventories
(
    id UUID PRIMARY KEY NOT NULL,
    documentnumber VARCHAR(255),
    facilityid UUID NOT NULL,
    isdraft BOOLEAN NOT NULL,
    occurreddate TIMESTAMP,
    programid UUID NOT NULL,
    signature VARCHAR(255),
    stockeventid UUID
);
CREATE TABLE physical_inventory_line_items
(
    id UUID PRIMARY KEY NOT NULL,
    lotid UUID,
    orderableid UUID NOT NULL,
    quantity INTEGER,
    physicalinventoryid UUID NOT NULL
);
CREATE TABLE stock_card_fields
(
    id UUID PRIMARY KEY NOT NULL,
    displayorder INTEGER NOT NULL,
    isdisplayed BOOLEAN NOT NULL,
    availablestockcardfieldsid UUID,
    stockcardtemplateid UUID
);
CREATE TABLE stock_card_line_item_fields
(
    id UUID PRIMARY KEY NOT NULL,
    displayorder INTEGER NOT NULL,
    isdisplayed BOOLEAN NOT NULL,
    availablestockcardlineitemfieldsid UUID,
    stockcardtemplateid UUID
);
CREATE TABLE stock_card_line_item_reasons
(
    id UUID PRIMARY KEY NOT NULL,
    description TEXT,
    isfreetextallowed BOOLEAN NOT NULL,
    name TEXT NOT NULL,
    reasoncategory TEXT NOT NULL,
    reasontype TEXT NOT NULL
);
CREATE TABLE stock_card_line_items
(
    id UUID PRIMARY KEY NOT NULL,
    destinationfreetext VARCHAR(255),
    documentnumber VARCHAR(255),
    occurreddate TIMESTAMP NOT NULL,
    processeddate TIMESTAMP NOT NULL,
    quantity INTEGER NOT NULL,
    reasonfreetext VARCHAR(255),
    signature VARCHAR(255),
    sourcefreetext VARCHAR(255),
    userid UUID NOT NULL,
    destinationid UUID,
    origineventid UUID NOT NULL,
    reasonid UUID,
    sourceid UUID,
    stockcardid UUID NOT NULL
);
CREATE TABLE stock_card_templates
(
    id UUID PRIMARY KEY NOT NULL,
    facilitytypeid UUID NOT NULL,
    programid UUID NOT NULL
);
CREATE TABLE stock_cards
(
    id UUID PRIMARY KEY NOT NULL,
    facilityid UUID NOT NULL,
    lotid UUID,
    orderableid UUID NOT NULL,
    programid UUID NOT NULL,
    origineventid UUID NOT NULL
);
CREATE TABLE stock_event_line_items
(
    id UUID PRIMARY KEY NOT NULL,
    destinationfreetext VARCHAR(255),
    destinationid UUID,
    lotid UUID,
    occurreddate TIMESTAMP NOT NULL,
    orderableid UUID NOT NULL,
    quantity INTEGER NOT NULL,
    reasonfreetext VARCHAR(255),
    reasonid UUID,
    sourcefreetext VARCHAR(255),
    sourceid UUID,
    stockeventid UUID NOT NULL
);
CREATE TABLE stock_events
(
    id UUID PRIMARY KEY NOT NULL,
    documentnumber VARCHAR(255),
    facilityid UUID NOT NULL,
    processeddate TIMESTAMP NOT NULL,
    programid UUID NOT NULL,
    signature VARCHAR(255),
    userid UUID NOT NULL
);
CREATE TABLE valid_destination_assignments
(
    id UUID PRIMARY KEY NOT NULL,
    facilitytypeid UUID NOT NULL,
    programid UUID NOT NULL,
    nodeid UUID NOT NULL
);
CREATE TABLE valid_reason_assignments
(
    id UUID PRIMARY KEY NOT NULL,
    facilitytypeid UUID NOT NULL,
    programid UUID NOT NULL,
    reasonid UUID NOT NULL
);
CREATE TABLE valid_source_assignments
(
    id UUID PRIMARY KEY NOT NULL,
    facilitytypeid UUID NOT NULL,
    programid UUID NOT NULL,
    nodeid UUID NOT NULL
);
ALTER TABLE physical_inventories ADD FOREIGN KEY (stockeventid) REFERENCES stock_events (id);
ALTER TABLE physical_inventory_line_items ADD FOREIGN KEY (physicalinventoryid) REFERENCES physical_inventories (id);
ALTER TABLE stock_card_fields ADD FOREIGN KEY (availablestockcardfieldsid) REFERENCES available_stock_card_fields (id);
ALTER TABLE stock_card_fields ADD FOREIGN KEY (stockcardtemplateid) REFERENCES stock_card_templates (id);
ALTER TABLE stock_card_line_item_fields ADD FOREIGN KEY (availablestockcardlineitemfieldsid) REFERENCES available_stock_card_line_item_fields (id);
ALTER TABLE stock_card_line_item_fields ADD FOREIGN KEY (stockcardtemplateid) REFERENCES stock_card_templates (id);
CREATE UNIQUE INDEX uk_6uy5au82jp04x5wcg5wgj1j1k ON stock_card_line_item_reasons (name);
ALTER TABLE stock_card_line_items ADD FOREIGN KEY (destinationid) REFERENCES nodes (id);
ALTER TABLE stock_card_line_items ADD FOREIGN KEY (origineventid) REFERENCES stock_events (id);
ALTER TABLE stock_card_line_items ADD FOREIGN KEY (reasonid) REFERENCES stock_card_line_item_reasons (id);
ALTER TABLE stock_card_line_items ADD FOREIGN KEY (sourceid) REFERENCES nodes (id);
ALTER TABLE stock_card_line_items ADD FOREIGN KEY (stockcardid) REFERENCES stock_cards (id);
ALTER TABLE stock_cards ADD FOREIGN KEY (origineventid) REFERENCES stock_events (id);
CREATE INDEX idxn1cmkkm4m6eseyofm6789vic8 ON stock_cards (facilityid, programid, orderableid);
ALTER TABLE stock_event_line_items ADD FOREIGN KEY (stockeventid) REFERENCES stock_events (id);
ALTER TABLE valid_destination_assignments ADD FOREIGN KEY (nodeid) REFERENCES nodes (id);
ALTER TABLE valid_reason_assignments ADD FOREIGN KEY (reasonid) REFERENCES stock_card_line_item_reasons (id);
ALTER TABLE valid_source_assignments ADD FOREIGN KEY (nodeid) REFERENCES nodes (id);