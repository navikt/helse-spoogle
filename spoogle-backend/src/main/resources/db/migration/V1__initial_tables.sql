CREATE TABLE node(
    node_id BIGSERIAL PRIMARY KEY,
    id VARCHAR NOT NULL,
    id_type VARCHAR NOT NULL
);

CREATE UNIQUE INDEX ON node(id, id_type);

CREATE TABLE edge(
    node_A BIGINT NOT NULL,
    node_B BIGINT NOT NULL,
    ugyldig TIMESTAMP DEFAULT null,
    PRIMARY KEY (node_A, node_B)
);

CREATE INDEX ON edge(node_A, node_B);
