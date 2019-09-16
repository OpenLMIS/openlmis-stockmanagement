-- WHEN COMMITTING OR REVIEWING THIS FILE: Make sure that the timestamp in the file name (that serves as a version) is the latest timestamp, and that no new migration have been added in the meanwhile.
-- Adding migrations out of order may cause this migration to never execute or behave in an unexpected way.
-- Migrations should NOT BE EDITED. Add a new migration to apply changes.

CREATE OR REPLACE FUNCTION populateFactTable() RETURNS INTEGER AS $$
DECLARE r RECORD;
DECLARE soh INTEGER;
BEGIN
FOR r IN
  SELECT line_items.stockcardid, line_items.sourceid, line_items.destinationid, reasons.reasontype, line_items.quantity, line_items.occurreddate, line_items.processeddate
	FROM stockmanagement.stock_card_line_items AS line_items
	LEFT JOIN stockmanagement.stock_card_line_item_reasons AS reasons
	ON line_items.reasonid = reasons.id
	ORDER BY occurreddate, processeddate, reasons.reasontype
    LOOP
      -- Find out last Stock on Hand for this stockcard
	    SELECT stockonhand INTO soh FROM stockmanagement.calculated_stocks_on_hand WHERE stockcardid = r.stockcardid ORDER BY occurreddate DESC LIMIT 1;
	    IF NOT FOUND THEN
		    SELECT 0 INTO soh;
	    END IF;

      -- Determine whether it's physical inventory, receive or issue and modify SOH accordingly
	    IF r.reasontype IS NULL AND r.sourceid IS NULL AND r.destinationid IS NULL THEN
		    soh := r.quantity;
	    ELSIF r.sourceid IS NOT NULL OR r.reasontype = 'CREDIT' THEN
		    soh := soh + r.quantity;
	    ELSE
		    soh := soh - r.quantity;
	    END IF;

      -- Insert new entry or update if there's one for the given stock card and date
	    IF NOT EXISTS (SELECT 1 FROM stockmanagement.calculated_stocks_on_hand WHERE stockcardid = r.stockcardid AND occurreddate = r.occurreddate) THEN
		    INSERT INTO stockmanagement.calculated_stocks_on_hand VALUES (uuid_generate_v4(), soh, r.occurreddate, r.stockcardid, r.processeddate);
	    ELSE
		    UPDATE stockmanagement.calculated_stocks_on_hand SET stockonhand = soh WHERE stockcardid = r.stockcardid AND occurreddate = r.occurreddate;
	    END IF;
    END LOOP;

    RETURN 1;
END;
$$ LANGUAGE 'plpgsql';


SELECT populateFactTable();

DROP FUNCTION populateFactTable();