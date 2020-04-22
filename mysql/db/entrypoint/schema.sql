CREATE TABLE event
(
    event_id      INT PRIMARY KEY AUTO_INCREMENT,
    group_id      VARCHAR(36) NOT NULL,
    group_version INT         NOT NULL,
    exec_id       VARCHAR(32) NOT NULL,
    target_id     VARCHAR(32),
    event_date    DATETIME    NOT NULL,
    event_payload JSON        NOT NULL
);
