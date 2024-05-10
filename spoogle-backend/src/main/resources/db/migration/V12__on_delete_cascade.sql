ALTER TABLE relasjon DROP CONSTRAINT relasjon_forelder_fkey,
    ADD CONSTRAINT relasjon_forelder_fkey FOREIGN KEY (forelder)
    REFERENCES relasjon(node)
    ON DELETE CASCADE;
