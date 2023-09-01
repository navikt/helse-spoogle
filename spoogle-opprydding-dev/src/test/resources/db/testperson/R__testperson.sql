INSERT INTO node(id, id_type) VALUES ('${fødselsnummer}', 'FØDSELSNUMMER');
INSERT INTO node(id, id_type) VALUES ('${organisasjonsnummer}', 'ORGANISASJONSNUMMER');

INSERT INTO sti(forelder, barn, ugyldig)
VALUES (
        (SELECT node_id FROM node WHERE id = '${fødselsnummer}'),
        (SELECT node_id FROM node WHERE id = '${organisasjonsnummer}'),
        null
);
