-- Reasons used to cancel Issue/Receive movements. A cancellation creates an ADJUSTMENT stock event
-- whose line items use one of these reasons to counter the original movement: a cancelled Issue
-- (a DEBIT that removed stock) is reversed with a CREDIT, and a cancelled Receive (a CREDIT that
-- added stock) is reversed with a DEBIT. The 'cancel' tag marks them as usable only for
-- cancellation and keeps them filtered out of the regular Issue/Receive/Adjust reason lists; they
-- are intentionally not added to valid_reason_assignments.
INSERT INTO stockmanagement.stock_card_line_item_reasons (
    id, isfreetextallowed, name, reasoncategory, reasontype, description
) VALUES
    ('6997c774-2f91-4e36-8938-d3003fe2c203', 'true', 'Cancelled issue', 'ADJUSTMENT', 'CREDIT',
        'Reverses a cancelled issue movement'),
    ('46aa9d3e-0ce4-4138-b086-172cefb58360', 'true', 'Cancelled receipt', 'ADJUSTMENT', 'DEBIT',
        'Reverses a cancelled receive movement');

INSERT INTO stockmanagement.stock_card_line_item_reason_tags (
    tag, reasonId
) VALUES
    ('cancel', '6997c774-2f91-4e36-8938-d3003fe2c203'),
    ('cancel', '46aa9d3e-0ce4-4138-b086-172cefb58360');
