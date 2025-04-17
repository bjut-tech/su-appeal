create table question_category (
    id bigint auto_increment
        primary key,
    name varchar(255) not null,
    description text null,
    created_at datetime(6) null,
    updated_at datetime(6) null
);

insert into question_category (name)
    values ('饮食服务'),
           ('生活设施'),
           ('师德师风'),
           ('通勤交通'),
           ('学生活动'),
           ('心理健康'),
           ('学生组织'),
           ('其它');

alter table appeal.question
    add column category_id bigint null,
    add constraint question_category_id
        foreign key (category_id) references question_category (id);
