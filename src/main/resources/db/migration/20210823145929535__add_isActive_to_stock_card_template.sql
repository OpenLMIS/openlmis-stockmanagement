ALTER TABLE stockmanagement.stock_cards ADD COLUMN IF NOT EXISTS isactive boolean DEFAULT TRUE;
ALTER TABLE stockmanagement.stock_events ADD COLUMN IF NOT EXISTS isactive boolean DEFAULT TRUE;
ALTER TABLE stockmanagement.stock_card_templates ADD COLUMN IF NOT EXISTS isactive boolean DEFAULT TRUE;
