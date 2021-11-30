ALTER TABLE stockmanagement.stock_cards ADD COLUMN isactive boolean DEFAULT TRUE;
ALTER TABLE stockmanagement.stock_events ADD COLUMN isactive boolean DEFAULT TRUE;
ALTER TABLE stockmanagement.stock_card_templates ADD COLUMN isactive boolean DEFAULT TRUE;
