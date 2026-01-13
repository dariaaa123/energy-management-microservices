-- Create databases for each microservice
CREATE DATABASE "example-db";
CREATE DATABASE "device-db";
CREATE DATABASE "auth-db";
CREATE DATABASE "monitoring-db";

-- Grant permissions (optional, but good practice)
GRANT ALL PRIVILEGES ON DATABASE "example-db" TO postgres;
GRANT ALL PRIVILEGES ON DATABASE "device-db" TO postgres;
GRANT ALL PRIVILEGES ON DATABASE "auth-db" TO postgres;
GRANT ALL PRIVILEGES ON DATABASE "monitoring-db" TO postgres;