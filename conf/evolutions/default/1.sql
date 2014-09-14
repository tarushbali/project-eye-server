  # Users schema

# --- !Ups

CREATE TABLE LatestVersion (
    id bigint(20) NOT NULL
);

# --- !Downs

DROP TABLE LatestVersion;