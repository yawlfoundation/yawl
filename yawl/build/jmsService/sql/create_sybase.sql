drop table system_data
go
create table system_data (
  id                     int not null,
  version                varchar(20) not null,
  creationDate           datetime not null
)
go
create unique index system_data_pk on system_data(id)
go
alter table system_data lock datarows
go

drop table seeds
go
create table seeds (
  name                   varchar(20) not null,
  seed                   numeric(28) not null
)
go
create unique index seeds_pk on seeds(name)
go
alter table seeds lock datarows
go

drop table destinations
go
create table destinations (
  name                   varchar(255) not null,
  isQueue                int not null,
  destinationId          numeric(28) not null
)
go
create unique index destinations_pk on destinations(name)
go
alter table destinations lock datarows
go

drop table messages
go
create table messages (
  messageId             varchar(64) not null,
  destinationId         numeric(28) not null,
  priority              int,
  createTime            numeric(28) not null,
  expiryTime            numeric(28),
  processed             int,
  messageBlob           image not null
)
go
create index messages_pk on messages(messageId)
go
alter table messages lock datarows
go

drop table message_handles
go
create table message_handles (
  messageId             varchar(64) not null,
  destinationId         numeric(28) not null,
  consumerId            numeric(28) NOT NULL,
  priority              int,
  acceptedTime          numeric(28) not null,
  sequenceNumber        numeric(28),
  expiryTime            numeric(28),
  delivered             int
)
go
create index message_handles_pk on message_handles(messageId)
go
alter table message_handles lock datarows
go

drop table consumers
go
create table consumers (
  name                 varchar(255) not null,
  destinationId        numeric(28) not null,
  consumerId           numeric(28) not null,
  created              numeric(28) not null
)
go
create unique index consumers_pk on consumers(name, destinationId)
go
alter table consumers lock datarows
go

drop table users
go
create table users (
  username             varchar(50) not null,
  password             varchar(50) not null
)
go
create unique index users_pk on users(username)
go
alter table users lock datarows
go

