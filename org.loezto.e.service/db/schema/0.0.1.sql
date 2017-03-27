-- Topic
 
CREATE SEQUENCE e.Topic_Seq AS INTEGER START WITH 2 INCREMENT BY 1

 create table e.topic (
 id integer not null,
 root boolean default false not null,
 creationDate timestamp default current_timestamp not null,
 parent integer not null references topic (id),
 Name varchar(256),
 primary key (id)
 )

 insert into e.topic (id, parent, name, root) values (0, 0, 'eRoot', true)
 
 insert into e.topic (id, parent, name, root) values (1, 0, 'Topic Root', true)
 
 
 -- Task

CREATE SEQUENCE e.Task_Seq AS INTEGER START WITH 1 INCREMENT BY 2

create table e.task (
 id integer not null,
 parent integer references e.task (id),
 topic integer not null references e.topic (id),
 placement integer,
 creationDate timestamp default current_timestamp not null,
 dueDate timestamp,
 completionDate timestamp,
 Name varchar(256),
 primary key (id)
 )
 
 
-- Entry
create table e.entry (
 id integer not null,
 type char(3),
 creationDate timestamp default current_timestamp not null,
 topic integer not null references e.topic (id),
 task integer references e.task(id),
 line varchar(1024),
 text clob,
 primary key (id)
 )

 CREATE SEQUENCE e.Entry_Seq AS INTEGER START WITH 5 INCREMENT BY 5

-- DBProps
create table e.DBProps (
 name varchar(256) unique,
 value varchar(1024)
 )

insert into e.DBProps values ( 'DBName', 'é' )
 
insert into e.DBProps values ( 'DBVersion', '0.0.1' )

 

