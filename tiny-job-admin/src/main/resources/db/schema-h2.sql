DROP TABLE IF EXISTS job_log;
DROP TABLE IF EXISTS job_info;
DROP TABLE IF EXISTS job_config;
DROP TABLE IF EXISTS job_lock;

CREATE TABLE job_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    execute_service VARCHAR(256) NOT NULL,
    execute_method VARCHAR(128),
    execute_param VARCHAR(512),
    service_type VARCHAR(64)
);

CREATE TABLE job_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_cron VARCHAR(128) NOT NULL,
    job_desc VARCHAR(512),
    author VARCHAR(128),
    job_type VARCHAR(64) NOT NULL,
    config_id BIGINT,
    execute_block_strategy VARCHAR(64),
    execute_timeout BIGINT DEFAULT 0,
    execute_fail_retry_count BIGINT DEFAULT 0,
    child_job_id VARCHAR(64),
    job_status INT DEFAULT 0,
    trigger_last_time BIGINT DEFAULT 0,
    trigger_next_time BIGINT DEFAULT 0,
    job_version BIGINT DEFAULT 0,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_job_config FOREIGN KEY (config_id) REFERENCES job_config (id)
);

CREATE TABLE job_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT,
    execute_service VARCHAR(256),
    execute_method VARCHAR(128),
    execute_param VARCHAR(512),
    service_type VARCHAR(64),
    execute_timeout BIGINT,
    execute_fail_retry_count BIGINT,
    trigger_time TIMESTAMP,
    handle_time TIMESTAMP,
    handle_code VARCHAR(32),
    handle_msg VARCHAR(1024)
);

CREATE TABLE job_lock (
    lock_name VARCHAR(64) PRIMARY KEY
);
