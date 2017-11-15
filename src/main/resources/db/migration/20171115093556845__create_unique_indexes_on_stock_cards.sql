-- https://stackoverflow.com/a/8289253
CREATE UNIQUE INDEX ON stock_cards(facilityId, programId, orderableId, lotId) WHERE lotId IS NOT NULL;
CREATE UNIQUE INDEX ON stock_cards(facilityId, programId, orderableId) WHERE lotId IS NULL;
