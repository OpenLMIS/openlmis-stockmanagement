-- Cleanup indexes
DROP INDEX IF EXISTS uniq_stock_card_facility_program_product_lot_unit;
DROP INDEX IF EXISTS uniq_stock_card_facility_program_product_lot;
DROP INDEX IF EXISTS uniq_stock_card_facility_program_product_unit;
DROP INDEX IF EXISTS uniq_stock_card_facility_program_product;

-- Create new indexes assuming unitOfOrderableId never null
CREATE UNIQUE INDEX uniq_stock_card_facility_program_product_lot_unit
    ON stock_cards (facilityId, programId, orderableId, lotId, unitOfOrderableId) WHERE lotId IS NOT NULL;

CREATE UNIQUE INDEX uniq_stock_card_facility_program_product_unit
    ON stock_cards (facilityId, programId, orderableId, unitOfOrderableId) WHERE lotId IS NULL;
