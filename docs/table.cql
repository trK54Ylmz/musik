CREATE KEYSPACE musik WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}
    AND durable_writes = false;

CREATE TABLE signals (
    hash ASCII,
    name TEXT,
    idx INT,
    PRIMARY KEY (hash, idx)
);