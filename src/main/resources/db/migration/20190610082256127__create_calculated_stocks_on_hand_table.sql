CREATE TABLE calculated_stocks_on_hand
(
    id UUID PRIMARY KEY NOT NULL,
    stockonhand INT4 NOT NULL,
    date DATE NOT NULL,
    stockcardid UUID NOT NULL
);

ALTER TABLE calculated_stocks_on_hand ADD FOREIGN KEY (stockcardid) REFERENCES stock_cards (id);

CREATE INDEX calculated_stocks_on_hand_index ON calculated_stocks_on_hand
(
    stockcardid
);
