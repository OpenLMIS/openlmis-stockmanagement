-- WHEN COMMITTING OR REVIEWING THIS FILE: Make sure that the timestamp in the file name (that serves as a version) is the latest timestamp, and that no new migration have been added in the meanwhile.
-- Adding migrations out of order may cause this migration to never execute or behave in an unexpected way.
-- Migrations should NOT BE EDITED. Add a new migration to apply changes.

INSERT INTO stockmanagement.stock_card_line_item_reasons (
  id, isfreetextallowed, name, reasoncategory, reasontype
) VALUES
('9b4b653a-f319-4a1b-bb80-8d6b4dd6cc12', 'false', 'Unpack Kit', 'AGGREGATION','DEBIT'),
('0676fdea-9ba8-4e6d-ae26-bb14f0dcfecd', 'false', 'Unpacked From Kit', 'AGGREGATION','CREDIT');

INSERT INTO stockmanagement.stock_card_line_item_reason_tags (
    tag, reasonId
) VALUES
    ('received', '0676fdea-9ba8-4e6d-ae26-bb14f0dcfecd'),
    ('consumed', '9b4b653a-f319-4a1b-bb80-8d6b4dd6cc12');