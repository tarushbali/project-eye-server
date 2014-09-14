# Users schema

# --- !Ups

CREATE TABLE LatestVersion (
  id bigint
);

# --- !Downs

DROP TABLE LatestVersion;