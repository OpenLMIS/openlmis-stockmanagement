-- The transaction history detail and the bin-card document-number link resolve the stock cards
-- touched by an event with WHERE origineventid = :eventId. origineventid only had a foreign key
-- (no index), so that lookup scanned the whole stock_card_line_items table. Mirror the existing
-- stockcardid FK index so the lookup uses an index instead.
CREATE INDEX ON stock_card_line_items (origineventid);
