ALTER SYSTEM SET shared_preload_libraries = 'pglogical';
ALTER SYSTEM SET wal_level = 'logical';
ALTER SYSTEM SET wal_sender_timeout = 0;
