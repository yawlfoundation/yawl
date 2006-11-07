create table system_data (
  id                     int primary key,
  version                varchar(20) not null,
  creationDate           timestamp not null
)
//

create table seeds (
  name                   varchar(20) primary key,
  seed                   numeric(28) not null
)
//

create table destinations (
  name                   varchar(255) primary key,
  isQueue                int not null,
  destinationId          numeric(28) not null
)
//

create table messages (
  messageId             varchar(64) primary key,
  destinationId         numeric(28) not null,
  priority              numeric(3),
  createTime            numeric(28) not null,
  expiryTime            numeric(28),
  processed             numeric(3),
  messageBlob           long byte not null
)
//

CREATE TABLE message_handles (
   messageId            varchar(64) not null,
   destinationId        numeric(28) not null,
   consumerId           numeric(28) not null,
   priority             numeric(3),
   acceptedTime         numeric(28),
   sequenceNumber       numeric(28),
   expiryTime           numeric(28),
   delivered            numeric(3)
)
//

create table consumers (
  name                 varchar(255) not null,
  destinationId        numeric(28) not null,
  consumerId           numeric(28) not null,
  created              numeric(28) not null,
  primary key (name, destinationId)
)
//

create table users (
  username             varchar(50) primary key,
  password             varchar(50) not null
)
