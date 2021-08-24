ALTER TABLE stockmanagement.stock_cards ADD COLUMN isShowed boolean DEFAULT TRUE;
ALTER TABLE stockmanagement.stock_events ADD COLUMN isShowed boolean DEFAULT TRUE;
ALTER TABLE stockmanagement.stock_card_templates ADD COLUMN isShowed boolean DEFAULT TRUE;
