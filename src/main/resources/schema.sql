CREATE TABLE note
(
    ID BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    TITLE VARCHAR(50) NOT NULL DEFAULT '',
    CONTENT VARCHAR(20000) NOT NULL DEFAULT ''
);