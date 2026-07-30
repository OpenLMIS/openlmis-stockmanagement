-- Cancelling an Issue/Receive movement creates an ADJUSTMENT stock event whose line
-- items point back to the reversed original line item via reverseseventlineitemid. This reference
-- drives the line-level "cancelled" flag and partial cancellation. The partial unique index
-- guarantees a given original line item can be reversed at most once (and also serves as the lookup
-- index for the already-cancelled validation check).
ALTER TABLE stockmanagement.stock_event_line_items
    ADD COLUMN reverseseventlineitemid UUID;

ALTER TABLE stockmanagement.stock_event_line_items
    ADD FOREIGN KEY (reverseseventlineitemid)
    REFERENCES stockmanagement.stock_event_line_items (id);

CREATE UNIQUE INDEX stock_event_line_items_reverses_unique_idx
    ON stockmanagement.stock_event_line_items (reverseseventlineitemid)
    WHERE reverseseventlineitemid IS NOT NULL;
