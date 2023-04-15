IF NOT EXISTS(SELECT 1 FROM sysobjects WHERE name = 'users' AND type = 'U')
EXECUTE ('CREATE TABLE users (
    id   varchar(36) DEFAULT newid(1) NOT NULL,
    name VARCHAR(100)                 NOT NULL,
    PRIMARY KEY (id)
)')
GO