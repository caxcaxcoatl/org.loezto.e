
-- CronoPlan
 create table cronoPlan (
 cronoType char(10) not null,
 start date not null,
 finish date  not null,
 primary key (cronoType, start, finish)
 )
 
-- CronoItem
 create table cronoItem (
 cronoType char (10) not null,
 start date  not null,
 finish date  not null,
 place integer not null,
 task integer not null references task (id),
 CONSTRAINT crono_pk PRIMARY KEY (cronoType, start, finish, task),
 CONSTRAINT crono_plan_fk FOREIGN KEY (cronoType, start, finish) REFERENCES cronoPlan (cronoType, start, finish),
 CONSTRAINT task_fk FOREIGN KEY (task) REFERENCES task(id) 
)

-- TopicPlanItem
create table topicPlanItem (
topic integer not null,
task integer not null,
place integer not null,
CONSTRAINT topic_plan_task_fk FOREIGN KEY (task) REFERENCES task(id),
CONSTRAINT topic_plan_topic_fk FOREIGN KEY (topic) REFERENCES topic(id),
CONSTRAINT topic_plan_pk PRIMARY Key (topic, task)
)

update DBProps set value = '0.2.0' where name = 'DBVersion'

