DROP TABLE IF EXISTS event;

CREATE TABLE event
(
    event_id      INT PRIMARY KEY AUTO_INCREMENT,
    group_id      VARCHAR(36)   NOT NULL,
    group_version INT           NOT NULL,
    exec_id       VARCHAR(50)   NOT NULL,
    target_id     VARCHAR(50),
    event_type    VARCHAR(32)   NOT NULL,
    event_payload VARCHAR(2500) NOT NULL
);
