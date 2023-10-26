alter table answer
    add column likes_count bigint not null default 0;

create table if not exists `like`
(
    id         bigint auto_increment
    primary key,
    type       varchar(32) not null,
    user_id    bigint null,
    answer_id  bigint null,
    created_at datetime(6) null,
    constraint like_user_id
    foreign key (user_id) references user (id),
    constraint like_answer_id
    foreign key (answer_id) references answer (id)
);
