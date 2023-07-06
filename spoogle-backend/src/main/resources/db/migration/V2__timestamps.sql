ALTER TABLE node ADD COLUMN opprettet TIMESTAMP default now();
ALTER TABLE edge ADD COLUMN opprettet TIMESTAMP default now();