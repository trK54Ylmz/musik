CREATE KEYSPACE musik WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}
    AND durable_writes = false;

CREATE TABLE signals (
    hash ASCII PRIMARY KEY,
    name TEXT,
    idx INT
);