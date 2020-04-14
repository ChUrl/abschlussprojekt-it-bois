CREATE TABLE event
(
    event_id      INT PRIMARY KEY AUTO_INCREMENT,
    group_id      VARCHAR(36) NOT NULL,
    user_id       VARCHAR(50) NOT NULL,
    event_type    VARCHAR(32) NOT NULL,
    event_payload JSON
);

CREATE TABLE invite
(
    invite_id   INT PRIMARY KEY AUTO_INCREMENT,
    group_id    VARCHAR(36) NOT NULL,
    invite_link VARCHAR(36) NOT NULL
);
