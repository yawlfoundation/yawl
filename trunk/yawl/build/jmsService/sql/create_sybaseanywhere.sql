drop table system_data
go
create table system_data (
  id                     bigint not null,
  version                varchar(20) not null,
  creationDate           datetime not null
)
go
create unique index system_data_pk on system_data(id)
go

drop table seeds
go
create table seeds (
  name                   varchar(20) not null,
  seed                   bigint not null
)
go
create unique index seeds_pk on seeds(name)
go

drop table destinations
go
create table destinations (
  name                   varchar(255) not null,
  isQueue                int not null,
  destinationId          bigint not null
)
go
create unique index destinations_pk on destinations(name)
go

drop table messages
go
create table messages (
  messageId             varchar(64) not null,
  destinationId         bigint not null,
  priority              int,
  createTime            bigint not null,
  expiryTime            bigint,
  processed             int,
  messageBlob           image not null
)
go
create index messages_pk on messages(messageId)
go

drop table message_handles
go
create table message_handles (
  messageId             varchar(64) not null,
  destinationId         bigint not null,
  consumerId            bigint NOT NULL,
  priority              int,
  acceptedTime          bigint not null,
  sequenceNumber        bigint,
  expiryTime            bigint,
  delivered             int
)
go
create index message_handles_pk on message_handles(messageId)
go

drop table consumers
go
create table consumers (
  name                 varchar(255) not null,
  destinationId        bigint not null,
  consumerId           bigint not null,
  created              bigint not null
)
go
create unique index consumers_pk on consumers(name, destinationId)
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
