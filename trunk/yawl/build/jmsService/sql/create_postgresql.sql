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
  seed                   int not null
);
create unique index seeds_pk on seeds(name);

DROP TABLE destinations;
create table destinations (
  name                   varchar(255) not null,
  isQueue                char,
  destinationId          int not null
);
create unique index destinations_pk on destinations(destinationId);

DROP TABLE messages;
create table messages (
  messageId             varchar(64) not null,
  destinationId         int8 not null,
  priority              int,
  createTime            int8,
  expiryTime            int8,
  processed             int,
  messageBlob           bytea not null
);
create index messages_pk on messages(messageId);

DROP TABLE message_handles;
CREATE TABLE message_handles (
   messageId            varchar(64) not null,
   destinationId        int8 NOT NULL,
   consumerId           int8 NOT NULL,
   priority             int,
   acceptedTime         int8,
   sequenceNumber       int8,
   expiryTime           int8,
   delivered            int
);
CREATE INDEX message_handles_pk ON message_handles(destinationId, consumerId, messageId);

DROP TABLE consumers;
create table consumers (
  name                 varchar(255) not null,
  destinationId        int8 not null,
  consumerId           int8 not NULL,
  created              int8 NOT NULL
);
create unique index consumers_pk on consumers(name, destinationId);

drop table users;
create table users (
  username             varchar(50) not null,
  password             varchar(50) not null
);
create unique index users_pk on users(username);