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
 
 

create table entry (
 id integer not null,
 type char(3),
 creationDate timestamp default current_timestamp not null,
 topic integer not null references topic (id),
 line varchar(1024),
 text clob,
 primary key (id)
 )

CREATE SEQUENCE Entry_Seq AS INTEGER START WITH 0 INCREMENT BY 5;