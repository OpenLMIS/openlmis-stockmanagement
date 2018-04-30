CREATE TABLE stock_card_line_item_reason_tags
(
    tag VARCHAR(255) NOT NULL,
    reasonId UUID NOT NULL
);

CREATE UNIQUE INDEX stock_card_line_item_reason_tags_unique_idx
ON stock_card_line_item_reason_tags (tag, reasonId);

ALTER TABLE stock_card_line_item_reason_tags
    ADD CONSTRAINT stock_card_line_item_reason_fkey FOREIGN KEY (reasonId) REFERENCES stock_card_line_item_reasons(id);