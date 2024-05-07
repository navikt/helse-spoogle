CREATE TYPE id_type AS ENUM (
    'FØDSELSNUMMER',
    'AKTØR_ID',
    'ORGANISASJONSNUMMER',
    'VEDTAKSPERIODE_ID',
    'BEHANDLING_ID',
    'OPPGAVE_ID',
    'UTBETALING_ID',
    'SØKNAD_ID',
    'INNTEKTSMELDING_ID'
    );

DROP TABLE sti;
DROP TABLE node;

CREATE TABLE relasjon
(
    node      VARCHAR PRIMARY KEY NOT NULL,
    forelder  VARCHAR REFERENCES relasjon (node),
    type      id_type             NOT NULL,
    opprettet timestamp           NOT NULL,
    ugyldig   timestamp
);
