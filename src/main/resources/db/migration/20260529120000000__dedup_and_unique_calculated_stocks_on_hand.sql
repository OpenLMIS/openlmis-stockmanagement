-- OLMIS-8206: enforce one calculated_stocks_on_hand row per (stockcardid, occurreddate) by
-- de-duplicating existing rows, adding a unique index, and dropping the redundant single-column index.

DELETE FROM calculated_stocks_on_hand a
    USING calculated_stocks_on_hand b
WHERE a.stockcardid = b.stockcardid
  AND a.occurreddate = b.occurreddate
  AND (a.processeddate < b.processeddate
      OR (a.processeddate = b.processeddate AND a.id < b.id));

CREATE UNIQUE INDEX IF NOT EXISTS calculated_stocks_on_hand_card_date_unique_idx
    ON calculated_stocks_on_hand (stockcardid, occurreddate);

DROP INDEX IF EXISTS calculated_stocks_on_hand_index;
