DROP TABLE system_data;
DROP TABLE seeds;
DROP TABLE destinations;
DROP TABLE messages;
DROP TABLE message_handles;
DROP TABLE consumers;
DROP TABLE users;

create table system_data (
  id                     bigint not null,
  version                varchar(20) not null,
  creationDate           timestamp not null,
  CONSTRAINT system_data_pk PRIMARY KEY (id)
);

create table seeds (
  name                   varchar(20) not null,
  seed                   bigint not null,
  CONSTRAINT seeds_pk PRIMARY KEY (name)
);

create table destinations (
  name                  varchar(255) not null,
  isQueue               int not null,
  destinationId         bigint not null,
  CONSTRAINT destinations_pk PRIMARY KEY (name)
);

create table messages (
  messageId             varchar(64) not null,
  destinationId         bigint not null,
  priority              int,
  createTime            bigint not null,
  expiryTime            bigint,
  processed             int,
  messageBlob           blob not null,
  CONSTRAINT messages_pk PRIMARY KEY (messageId)
);

CREATE TABLE message_handles (
   messageId            varchar(64) not null,
   destinationId        bigint NOT NULL,
   consumerId           bigint NOT NULL,
   priority             int,
   acceptedTime         bigint,
   sequenceNumber       bigInt,
   expiryTime           bigint,
   delivered            int,
   CONSTRAINT message_handles_pk PRIMARY KEY (destinationId, consumerId, 
                                              messageId)
);

create table consumers (
  name                 varchar(255) not null,
  destinationId        bigint not null,
  consumerId           bigint not NULL,
  created              bigint NOT NULL,
  CONSTRAINT consumers_pk PRIMARY KEY (name, destinationId)
);

create table users (
  username             varchar(50) not null,
  password             varchar(50) not null
  CONSTRAINT users_pk PRIMARY KEY (username)
);
