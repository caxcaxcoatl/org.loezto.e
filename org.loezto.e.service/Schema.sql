 CREATE SEQUENCE Topic_Seq AS INTEGER START WITH 2 INCREMENT BY 1;

 create table topic (
 id integer not null,
 root boolean default false not null,
 creationDate timestamp default current_timestamp not null,
 parent integer not null references topic (id),
 Name varchar(256),
 primary key (id)
 );

 insert into topic (id, parent, name, root) values (0, 0, 'eRoot', true);
 insert into topic (id, parent, name, root) values (1, 0, 'Topic Root', true);