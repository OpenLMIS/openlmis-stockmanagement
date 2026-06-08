ALTER TABLE stockmanagement.stock_events
    ADD COLUMN eventorigin TEXT NULL;

CREATE INDEX stock_events_facility_origin_processed_idx
    ON stockmanagement.stock_events (facilityid, eventorigin, processeddate DESC);

CREATE TABLE stockmanagement.document_number_sequences (
    id UUID PRIMARY KEY,
    facilityid UUID NOT NULL,
    year INTEGER NOT NULL,
    month INTEGER NOT NULL,
    lastsequencenumber INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT document_number_sequences_facility_year_month_unique
        UNIQUE (facilityid, year, month)
);
