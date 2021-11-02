ALTER TABLE stockmanagement.stock_cards ADD COLUMN isActive boolean DEFAULT TRUE;
ALTER TABLE stockmanagement.stock_events ADD COLUMN isActive boolean DEFAULT TRUE;
ALTER TABLE stockmanagement.stock_card_templates ADD COLUMN isActive boolean DEFAULT TRUE;
