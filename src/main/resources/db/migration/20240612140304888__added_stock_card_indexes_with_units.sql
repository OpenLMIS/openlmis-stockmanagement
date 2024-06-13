-- Drop old indices
DROP INDEX uniq_stock_card_facility_program_product_lot;
DROP INDEX uniq_stock_card_facility_program_product;

-- https://stackoverflow.com/a/8289253
-- Create indexes for nullable lot and nullable unit
CREATE UNIQUE INDEX uniq_stock_card_facility_program_product_lot_unit
ON stock_cards(facilityId, programId, orderableId, lotId, unitOfOrderableId)
WHERE lotId IS NOT NULL AND unitOfOrderableId IS NOT NULL;

CREATE UNIQUE INDEX uniq_stock_card_facility_program_product_lot
ON stock_cards(facilityId, programId, orderableId, lotId)
WHERE lotId IS NOT NULL AND unitOfOrderableId IS NULL;

CREATE UNIQUE INDEX uniq_stock_card_facility_program_product_unit
ON stock_cards(facilityId, programId, orderableId, unitOfOrderableId)
WHERE lotId IS NULL AND unitOfOrderableId IS NOT NULL;

CREATE UNIQUE INDEX uniq_stock_card_facility_program_product
ON stock_cards(facilityId, programId, orderableId)
WHERE lotId IS NULL AND unitOfOrderableId IS NULL;
