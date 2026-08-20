-- Tags each cancellation reason with the movement tag it counters, so a cancellation nets out of
-- the column its original was counted in. Requires cancellations dated on the movement cancelled.
INSERT INTO stockmanagement.stock_card_line_item_reason_tags (tag, reasonid)
    SELECT 'distributed', '6997c774-2f91-4e36-8938-d3003fe2c203'
WHERE
    NOT EXISTS (
        SELECT tag FROM stockmanagement.stock_card_line_item_reason_tags
        WHERE tag = 'distributed' AND reasonid = '6997c774-2f91-4e36-8938-d3003fe2c203'
    );

INSERT INTO stockmanagement.stock_card_line_item_reason_tags (tag, reasonid)
    SELECT 'received', '46aa9d3e-0ce4-4138-b086-172cefb58360'
WHERE
    NOT EXISTS (
        SELECT tag FROM stockmanagement.stock_card_line_item_reason_tags
        WHERE tag = 'received' AND reasonid = '46aa9d3e-0ce4-4138-b086-172cefb58360'
    );
