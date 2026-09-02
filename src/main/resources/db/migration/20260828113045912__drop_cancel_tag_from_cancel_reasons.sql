-- The scope tag now identifies a cancellation reason on its own.
DELETE FROM stockmanagement.stock_card_line_item_reason_tags
 WHERE tag = 'cancel'
   AND reasonId IN (
       '6997c774-2f91-4e36-8938-d3003fe2c203',
       '46aa9d3e-0ce4-4138-b086-172cefb58360',
       '0f9a0b4c-2d59-4d3a-9ca1-3c6b8f4d2e71',
       'c3b6f5a2-8e14-4a77-b0d6-5e2a9c7f1b48');
