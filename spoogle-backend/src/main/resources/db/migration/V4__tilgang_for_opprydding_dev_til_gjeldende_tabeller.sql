DO $$BEGIN
    IF EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'spoogle-opprydding-dev')
    THEN GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO "spoogle-opprydding-dev";
    END IF;
END$$;

DO $$BEGIN
    IF EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'spoogle-opprydding-dev')
    THEN GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO "spoogle-opprydding-dev";
    END IF;
END$$;
