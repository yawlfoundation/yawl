use openjms;

create table system_data (
  id                     bigint not null,
  version                varchar(20) not null,
  creationDate           datetime not null
);
create unique index system_data_pk on system_data(id);

create table seeds (
  name                   varchar(20) not null,
  seed                   bigint not null
);
create unique index seeds_pk on seeds(name);

create table destinations (
  name                   varchar(255) not null,
  isQueue                int not null,
  destinationId          bigint not null
);
create unique index destinations_pk on destinations(name);

create table messages (
  messageId             varchar(64) not null,
  destinationId         bigint not null,
  priority              int,
  createTime            bigint not null,
  expiryTime            bigint,
  processed             int,
  messageBlob           image not null
);
create index messages_pk on messages(messageId);

CREATE TABLE message_handles (
   messageId            varchar(64) not null,
   destinationId        bigint NOT NULL,
   consumerId           bigint NOT NULL,
   priority             int,
   acceptedTime         bigint,
   sequenceNumber       bigInt,
   expiryTime           bigint,
   delivered            int
);
CREATE INDEX message_handles_pk ON message_handles(destinationId, consumerId, messageId);

create table consumers (
  name                 varchar(255) not null,
  destinationId        bigint not null,
  consumerId           bigint not NULL,
  created              bigint NOT NULL
);
create unique index consumers_pk on consumers(name, destinationId);

create table users (
  username             varchar(50) not null,
  password             varchar(50) not null
);
create unique index users_pk on users(username);
