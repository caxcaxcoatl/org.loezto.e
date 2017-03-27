
-- CronoPlan
 create table e.cronoPlan (
 cronoType char(10) not null,
 start date not null,
 finish date  not null,
 primary key (cronoType, start, finish)
 )
 
-- CronoItem
 create table e.cronoItem (
 cronoType char (10) not null,
 start date  not null,
 finish date  not null,
 place integer not null,
 task integer not null references e.task (id),
 CONSTRAINT crono_pk PRIMARY KEY (cronoType, start, finish, task),
 CONSTRAINT crono_plan_fk FOREIGN KEY (cronoType, start, finish) REFERENCES e.cronoPlan (cronoType, start, finish),
 CONSTRAINT task_fk FOREIGN KEY (task) REFERENCES e.task(id) 
)

-- TopicPlanItem
create table e.topicPlanItem (
topic integer not null,
task integer not null,
place integer not null,
CONSTRAINT topic_plan_task_fk FOREIGN KEY (task) REFERENCES e.task(id),
CONSTRAINT topic_plan_topic_fk FOREIGN KEY (topic) REFERENCES e.topic(id),
CONSTRAINT topic_plan_pk PRIMARY Key (topic, task)
)

update e.DBProps set value = '0.2.0' where name = 'DBVersion'

