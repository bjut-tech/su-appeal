create table "question_category" (
    "id" BIGINT auto_increment
        primary key,
    "name" CHARACTER VARYING(255) not null,
    "description" CHARACTER VARYING(65535) null,
    "created_at" TIMESTAMP WITH TIME ZONE,
    "updated_at" TIMESTAMP WITH TIME ZONE
);

insert into "question_category" ("name")
    values ('饮食服务'),
           ('生活设施'),
           ('师德师风'),
           ('通勤交通'),
           ('学生活动'),
           ('心理健康'),
           ('学生组织'),
           ('其它');

alter table "question"
    add column "category_id" BIGINT null;

alter table "question"
    add constraint "question_category_id"
        foreign key ("category_id") references "question_category";
