drop table system_data;
create table system_data (
  id                     NUMBER(3) not null,
  version                varchar2(20) not null,
  creationDate           date not null
);
alter table system_data
  add constraint system_data_pk
  primary key ( id )
  using index;


drop table seeds;
create table seeds (
  name                   varchar2(20) not null,
  seed                   NUMBER(28) not null
);
alter table seeds
  add constraint seeds_pk
  primary key ( name )
  using index;


drop table destinations;
create table destinations (
  name                   varchar2(255) not null,
  isQueue                NUMBER(3) not null,
  destinationId          NUMBER(28) not null
);
alter table destinations
  add constraint destinations_pk
  primary key ( name )
  using index;


drop table messages;
create table messages (
  messageId             varchar2(64) not null,
  destinationId         NUMBER(28) not null,
  priority              NUMBER(3),
  createTime            NUMBER(28) not null,
  expiryTime            NUMBER(28),
  processed             NUMBER(3),
  messageBlob           long raw not null
);

alter table messages
  add constraint messages_pk
  primary key ( messageid )
  using index;

create index messages_x1 on messages(
  expiryTime
);

create index messages_x2 on messages(
  destinationId
);


drop table message_handles;
create table message_handles (
  messageId             varchar2(64) NOT NULL,
  destinationId         NUMBER(28) NOT NULL,
  consumerId            NUMBER(28) NOT NULL,
  priority              NUMBER(3),
  acceptedTime          NUMBER(28),
  sequenceNumber        NUMBER(28),
  expiryTime            NUMBER(28),
  delivered             NUMBER(3)
);
alter table message_handles
  add constraint message_handles_pk
  primary key ( destinationId, consumerId, messageId )
  using index;
create index message_handles_x1 on message_handles(expirytime);
create index message_handles_x2 on message_handles(messageid);
create index message_handles_x3 on message_handles(destinationid);
create index message_handles_x4 on message_handles(consumerid,acceptedtime);


drop table consumers;
create table consumers (
  name                 varchar2(255) not null,
  destinationId        NUMBER(28) not null,
  consumerId           NUMBER(28) not NULL,
  created              NUMBER(28) NOT NULL
);
alter table consumers
  add constraint consumers_pk
  primary key ( name, destinationid )
  using index;


drop table users;
create table users (
  username             varchar2(50) not null,
  password             varchar2(50) not null
);
alter table users
  add constraint users_pk
  primary key ( username )
  using index;
