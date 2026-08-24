-- Run this only if tbl_config was already created with CONFIG_FOR as the primary key.
-- New deployments using Hibernate ddl-auto=update/create will use the AppConfig entity shape.

ALTER TABLE tbl_config
    DROP PRIMARY KEY;

ALTER TABLE tbl_config
    ADD COLUMN config_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;

CREATE INDEX idx_tbl_config_config_for_active
    ON tbl_config (CONFIG_FOR, is_active);
