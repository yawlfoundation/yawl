drop table system_data;
create table system_data (
  id                     int not null,
  version                varchar(20) not null,
  creationDate           datetime not null
);
create unique index system_data_pk on system_data(id);

drop table seeds;
create table seeds (
  name                   varchar(20) not null,
  seed                   numeric(28) not null
);
create unique index seeds_pk on seeds(name);

drop table txids;
CREATE TABLE txids (
  id                    numeric(28) not null,
  xid                   binary not null,
  status                numeric(3) not null,
  timeout               numeric(28) not null
);
CREATE UNIQUE INDEX txids_pk ON txids(id);

drop table tx_messages;
CREATE TABLE tx_messages (
  messageId             varchar(64) not null,
  txid                  numeric(28) not null,
  messageBlob           binary not null
);
CREATE UNIQUE INDEX tx_messages_pk ON tx_messages(messageId)

drop table destinations;
create table destinations (
  name                   varchar(255) not null,
  isQueue                bit not null,
  destinationId          numeric(28) not null
);
create unique index destinations_pk on destinations(name);

drop table messages;
create table messages (
  messageId             varchar(64) not null,
  destinationId         numeric(28) not null,
  priority              numeric(3),
  createTime            numeric(28) not null,
  expiryTime            numeric(28),
  processed             numeric(3),
  messageBlob           binary not null
);
create index messages_pk on messages(messageId);

drop table message_handles;
CREATE TABLE message_handles (
   messageId            varchar(64) not null,
   destinationId        numeric(28) not null,
   consumerId           numeric(28) not null,
   priority             numeric(3),
   acceptedTime         numeric(28),
   sequenceNumber       numeric(28),
   expiryTime           numeric(28),
   delivered            numeric(3)
);
CREATE INDEX message_handles_pk ON message_handles(destinationId, consumerId, messageId);

drop table consumers;
create table consumers (
  name                 varchar(255) not null,
  destinationId        numeric(28) not null,
  consumerId           numeric(28) not null,
  created              numeric(28) not null
);
create unique index consumers_pk on consumers(name, destinationId);

drop table users;
create table users (
  username             varchar(50) not null,
  password             varchar(50) not null
);
create unique index users_pk on users(username);


