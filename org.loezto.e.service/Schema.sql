 CREATE SEQUENCE Topic_Seq AS INTEGER START WITH 1 INCREMENT BY 50;

 create table topic (
 id integer not null,
 root boolean default false,
 creationDate timestamp default current_timestamp,
 parent integer not null references topic (id),
 Name varchar(256),
 primary key (id)
 );

 insert into topic (id, parent, name, root) values (0, 0, 'eRoot', true);