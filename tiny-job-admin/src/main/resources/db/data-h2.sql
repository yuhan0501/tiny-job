INSERT INTO job_config (execute_service, execute_method, execute_param, service_type)
VALUES ('http://localhost:9000/jobs/trigger', 'POST', 'jobId=1', 'http');

INSERT INTO job_info (job_cron, job_desc, author, job_type, config_id, execute_block_strategy,
                      execute_timeout, execute_fail_retry_count, child_job_id, job_status,
                      trigger_last_time, trigger_next_time, job_version, update_time, create_time)
VALUES ('0/30 * * * * ?', 'Sample HTTP job', 'dev', 'http', 1, 'SERIAL',
        60000, 0, NULL, 1,
        DATEDIFF('MILLISECOND', TIMESTAMP '1970-01-01 00:00:00', CURRENT_TIMESTAMP()) - 60000,
        DATEDIFF('MILLISECOND', TIMESTAMP '1970-01-01 00:00:00', CURRENT_TIMESTAMP()) + 30000,
        1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO job_lock (lock_name, owner, expires_at) VALUES ('scheduler_lock', NULL, 0);
INSERT INTO job_control_state (id, paused, paused_at) VALUES (1, 0, -1);
