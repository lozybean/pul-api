CREATE TABLE job_info
(
    id           serial8 primary key,
    token        varchar(24),
    container_id varchar(64),
    status       varchar(64),
    retry_times  int       default 0,
    create_time  timestamp default current_timestamp,
    update_time  timestamp default current_timestamp
);
CREATE INDEX job_info_token_index ON job_info (token);
CREATE INDEX job_info_container_id_index ON job_info (container_id);
CREATE INDEX job_info_status_index ON job_info (status);

CREATE TABLE container_state
(
    id         varchar(64) primary key,
    status     varchar(32),
    running    boolean,
    paused     boolean,
    restarting boolean,
    oomKilled  boolean,
    dead       boolean,
    pid        int,
    exitcode   int,
    error      text,
    startedAt  varchar(36),
    finishedAt varchar(36)
);