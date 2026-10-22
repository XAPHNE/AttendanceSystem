-- The ID an RFID card, a barcode or a QR card gives when it is scanned.
-- NULL means "no card"; H2 lets any number of rows share NULL under UNIQUE.

ALTER TABLE student ADD COLUMN card_id VARCHAR(64);
ALTER TABLE student ADD CONSTRAINT student_card_id_unique UNIQUE (card_id);

-- Cards for the demo students, so a fresh install can be tried by typing DEMO-1001
UPDATE student SET card_id = 'DEMO-' || studentNum WHERE studentNum BETWEEN 1001 AND 1006;
