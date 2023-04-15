IF NOT EXISTS(SELECT 1 FROM sysobjects WHERE name = 'user_infos' AND type = 'U')
EXECUTE ('CREATE TABLE user_infos (
    id   varchar(36) DEFAULT newid(1) NOT NULL,
    email VARCHAR(100)                 NOT NULL,
    NDR varchar(36) DEFAULT newid(1) NOT NULL,
    user_id varchar(36) NOT NULL,
    constraint user_fk foreign key (user_id) REFERENCES users(id)
)')
GO