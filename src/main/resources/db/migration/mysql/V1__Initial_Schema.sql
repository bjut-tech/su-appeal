create table if not exists attachment
(
    id         binary(16)   not null
    primary key,
    created_at datetime(6)  null,
    name       varchar(255) null,
    size       bigint       not null
    );

create table if not exists user
(
    id         bigint auto_increment
    primary key,
    admin      bit          not null default _binary '\0',
    created_at datetime(6)  null,
    name       varchar(255) null,
    role       tinyint      not null,
    uid        varchar(16)  not null,
    updated_at datetime(6)  null,
    constraint user_uid
    unique (uid)
    );

create table if not exists announcement
(
    id         bigint auto_increment
    primary key,
    content    text         not null,
    created_at datetime(6)  null,
    title      varchar(255) not null,
    updated_at datetime(6)  null,
    user_id    bigint       null,
    constraint announcement_user_id
    foreign key (user_id) references user (id)
    );

create table if not exists announcement_attachments
(
    announcement_id bigint     not null,
    attachments_id  binary(16) not null,
    constraint announcement_attachments_announcement_id
    foreign key (announcement_id) references announcement (id),
    constraint announcement_attachments_attachments_id
    foreign key (attachments_id) references attachment (id)
    );

create table if not exists answer
(
    id         bigint auto_increment
    primary key,
    content    text        not null,
    created_at datetime(6) null,
    updated_at datetime(6) null,
    user_id    bigint      null,
    constraint answer_user_id
    foreign key (user_id) references user (id)
    );

create table if not exists answer_attachments
(
    answer_id      bigint     not null,
    attachments_id binary(16) not null,
    constraint answer_attachments_answer_id
    foreign key (answer_id) references answer (id),
    constraint answer_attachments_attachments_id
    foreign key (attachments_id) references attachment (id)
    );

create table if not exists question
(
    id         bigint auto_increment
    primary key,
    contact    varchar(255) null,
    content    text         not null,
    created_at datetime(6)  null,
    published  bit          not null default _binary '\0',
    updated_at datetime(6)  null,
    answer_id  bigint       null,
    user_id    bigint       null,
    constraint question_answer_id_unique
    unique (answer_id),
    constraint question_answer_id
    foreign key (answer_id) references answer (id),
    constraint question_user_id
    foreign key (user_id) references user (id)
    );

create table if not exists question_attachments
(
    question_id    bigint     not null,
    attachments_id binary(16) not null,
    constraint question_attachments_attachments_id
    foreign key (attachments_id) references attachment (id),
    constraint question_attachments_question_id
    foreign key (question_id) references question (id)
    );
