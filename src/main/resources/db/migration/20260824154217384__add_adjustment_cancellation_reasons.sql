-- Cancel reasons for Adjustment movements, the counterpart of the Cancelled issue/receipt pair.
-- The second tag scopes a cancel reason to the kind of line it may undo.
INSERT INTO stockmanagement.stock_card_line_item_reasons (
    id, isfreetextallowed, name, reasoncategory, reasontype, description
) VALUES
    ('0f9a0b4c-2d59-4d3a-9ca1-3c6b8f4d2e71', 'true', 'Cancelled debit adjustment', 'ADJUSTMENT',
        'CREDIT', 'Reverses a cancelled adjustment that removed stock'),
    ('c3b6f5a2-8e14-4a77-b0d6-5e2a9c7f1b48', 'true', 'Cancelled credit adjustment', 'ADJUSTMENT',
        'DEBIT', 'Reverses a cancelled adjustment that added stock');

INSERT INTO stockmanagement.stock_card_line_item_reason_tags (
    tag, reasonId
) VALUES
    ('cancel', '0f9a0b4c-2d59-4d3a-9ca1-3c6b8f4d2e71'),
    ('cancelAdjustment', '0f9a0b4c-2d59-4d3a-9ca1-3c6b8f4d2e71'),
    ('cancel', 'c3b6f5a2-8e14-4a77-b0d6-5e2a9c7f1b48'),
    ('cancelAdjustment', 'c3b6f5a2-8e14-4a77-b0d6-5e2a9c7f1b48'),
    ('cancelMovement', '6997c774-2f91-4e36-8938-d3003fe2c203'),
    ('cancelMovement', '46aa9d3e-0ce4-4138-b086-172cefb58360');
