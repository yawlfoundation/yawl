DROP TABLE system_data;
create table system_data (
  id                     int not null,
  version                varchar(20) not null,
  creationDate           DATE not null
);
create unique index system_data_pk on system_data(id);

DROP TABLE seeds;
create table seeds (
  name                   varchar(20) not null,
  seed                   numeric(18) not null
);
create unique index seeds_pk on seeds(name);

DROP TABLE destinations;
create table destinations (
  name                   varchar(255) not null,
  isQueue                int not null,
  destinationId          numeric(18) not null
);
create unique index destinations_pk on destinations(destinationId);

DROP TABLE messages;
create table messages (
  messageId             varchar(64) not null,
  destinationId         numeric(18) not null,
  priority              int,
  createTime            numeric(18) not null,
  expiryTime            numeric(18),
  processed             int,
  messageBlob           blob not null
);
create index messages_pk on messages(messageId);

DROP TABLE message_handles;
CREATE TABLE message_handles (
   messageId            varchar(64) not null,
   destinationId        numeric(18) not null,
   consumerId           numeric(18) not null,
   priority             int,
   acceptedTime         numeric(18),
   sequenceNumber       numeric(18),
   expiryTime           numeric(18),
   delivered            int
);
CREATE INDEX message_handles_pk ON message_handles(destinationId, consumerId, messageId);

DROP TABLE consumers;
create table consumers (
  name                 varchar(255) not null,
  destinationId        numeric(18) not null,
  consumerId           numeric(18) not null,
  created              numeric(18) not null
);
create unique index consumers_pk on consumers(name, destinationId);

DROP TABLE users;
create table users (
  username            varchar(50) not null,
  passwd              varchar(50) not null
);
create unique index users_pk on users(username);