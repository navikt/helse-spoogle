DO $$ BEGIN
    IF EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'spoogle-opprydding-dev')
    THEN
        ALTER DEFAULT PRIVILEGES FOR USER spoogle IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO "spoogle-opprydding-dev";
        ALTER DEFAULT PRIVILEGES FOR USER spoogle IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO "spoogle-opprydding-dev";
    END IF;
END $$;
