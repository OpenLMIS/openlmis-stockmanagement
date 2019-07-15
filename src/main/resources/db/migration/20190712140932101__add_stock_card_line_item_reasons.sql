-- WHEN COMMITTING OR REVIEWING THIS FILE: Make sure that the timestamp in the file name (that serves as a version) is the latest timestamp, and that no new migration have been added in the meanwhile.
-- Adding migrations out of order may cause this migration to never execute or behave in an unexpected way.
-- Migrations should NOT BE EDITED. Add a new migration to apply changes.

INSERT INTO stockmanagement.stock_card_line_item_reasons (id, name, description, reasoncategory, reasontype, isfreetextallowed)
    SELECT 'e3fc3cf3-da18-44b0-a220-77c985202e06','Transfer In','Transfer In','TRANSFER','CREDIT','false'
WHERE
    NOT EXISTS (
        SELECT id FROM stockmanagement.stock_card_line_item_reasons WHERE name = 'Transfer In' OR id = 'e3fc3cf3-da18-44b0-a220-77c985202e06'
    );