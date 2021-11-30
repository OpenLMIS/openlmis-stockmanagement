ALTER TABLE stockmanagement.stock_cards ADD COLUMN is_active boolean DEFAULT TRUE;
ALTER TABLE stockmanagement.stock_events ADD COLUMN is_active boolean DEFAULT TRUE;
ALTER TABLE stockmanagement.stock_card_templates ADD COLUMN is_active boolean DEFAULT TRUE;
