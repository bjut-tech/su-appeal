alter table "answer"
    add column "likes_count" BIGINT not null default 0;

create table "like"
(
    "id"         BIGINT auto_increment
        primary key,
    "type"       CHARACTER VARYING(32) not null,
    "user_id"    BIGINT,
    "answer_id"  BIGINT,
    "created_at" TIMESTAMP WITH TIME ZONE,
    constraint "like_user_id"
        foreign key ("user_id") references "user",
    constraint "like_answer_id"
        foreign key ("answer_id") references "answer"
);
