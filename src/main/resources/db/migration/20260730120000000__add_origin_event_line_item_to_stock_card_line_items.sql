-- Record, on each stock card line item, the stock event line item it was created from.
-- This gives reads a stable, unique stockEventLineItemId - needed to cancel a specific line
-- (partial cancellation) and to resolve the cancellation cross-links reliably, instead of a
-- non-unique (event, orderable, lot) match. Nullable; populated for rows created going forward.
ALTER TABLE stockmanagement.stock_card_line_items
    ADD COLUMN origineventlineitemid UUID;

ALTER TABLE stockmanagement.stock_card_line_items
    ADD FOREIGN KEY (origineventlineitemid)
    REFERENCES stockmanagement.stock_event_line_items (id);
