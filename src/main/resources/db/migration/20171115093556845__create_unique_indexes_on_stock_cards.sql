-- https://stackoverflow.com/a/8289253
CREATE UNIQUE INDEX uniq_stock_card_facility_program_product_lot
ON stock_cards(facilityId, programId, orderableId, lotId)
WHERE lotId IS NOT NULL;

CREATE UNIQUE INDEX uniq_stock_card_facility_program_product
ON stock_cards(facilityId, programId, orderableId)
WHERE lotId IS NULL;
