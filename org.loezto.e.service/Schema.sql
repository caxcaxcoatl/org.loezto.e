-- Topic
 
CREATE SEQUENCE Topic_Seq AS INTEGER START WITH 2 INCREMENT BY 1

 create table topic (
 id integer not null,
 root boolean default false not null,
 creationDate timestamp default current_timestamp not null,
 parent integer not null references topic (id),
 Name varchar(256),
 primary key (id)
 )

 insert into topic (id, parent, name, root) values (0, 0, 'eRoot', true)
 
 insert into topic (id, parent, name, root) values (1, 0, 'Topic Root', true)
 
 
 -- Task


CREATE SEQUENCE Task_Seq AS INTEGER START WITH 1 INCREMENT BY 2

create table task (
 id integer not null,
 parent integer references task (id),
 topic integer not null references topic (id),
 placement integer,
 creationDate timestamp default current_timestamp not null,
 dueDate timestamp,
 completionDate timestamp,
 Name varchar(256),
 primary key (id)
 )
 
 
-- Entry
create table entry (
 id integer not null,
 type char(3),
 creationDate timestamp default current_timestamp not null,
 topic integer not null references topic (id),
 task integer references task(id),
 line varchar(1024),
 text clob,
 primary key (id)
 )

CREATE SEQUENCE Entry_Seq AS INTEGER START WITH 5 INCREMENT BY 5


create table DBProps (
 name varchar(256) unique,
 value varchar(1024)
 )

insert into DBProps values ( 'DBName', 'é' )
 
insert into DBProps values ( 'DBVersion', '0.0.1' )

 
 -- insert into task (id, topic, placement, name) values (next value for task_seq, 367, 1, 'First task');
 
 


