create table "attachment"
(
    "id"         UUID   not null
        primary key,
    "created_at" TIMESTAMP WITH TIME ZONE,
    "name"       CHARACTER VARYING(255),
    "size"       BIGINT not null
);

create table "user"
(
    "id"         BIGINT auto_increment
        primary key,
    "admin"      BOOLEAN not null default false,
    "created_at" TIMESTAMP WITH TIME ZONE,
    "name"       CHARACTER VARYING(255),
    "role"       TINYINT               not null,
    "uid"        CHARACTER VARYING(16) not null
        constraint "user_uid_unique"
            unique,
    "updated_at" TIMESTAMP WITH TIME ZONE
);

create table "announcement"
(
    "id"         BIGINT auto_increment
        primary key,
    "content"    CHARACTER VARYING(65535) not null,
    "created_at" TIMESTAMP WITH TIME ZONE,
    "title"      CHARACTER VARYING(255)   not null,
    "updated_at" TIMESTAMP WITH TIME ZONE,
    "user_id"    BIGINT,
        constraint "announcement_user_id"
            foreign key ("user_id") references "user"
);

create table "announcement_attachments"
(
    "announcement_id" BIGINT not null,
    "attachments_id"  UUID   not null,
        constraint "announcement_attachments_announcement_id"
            foreign key ("announcement_id") references "announcement",
        constraint "announcement_attachments_attachments_id"
            foreign key ("attachments_id") references "attachment"
);

create table "answer"
(
    "id"         BIGINT auto_increment
        primary key,
    "content"    CHARACTER VARYING(65535) not null,
    "created_at" TIMESTAMP WITH TIME ZONE,
    "updated_at" TIMESTAMP WITH TIME ZONE,
    "user_id"    BIGINT,
        constraint "user_id"
            foreign key ("user_id") references "user"
);

create table "answer_attachments"
(
    "answer_id"      BIGINT not null,
    "attachments_id" UUID   not null,
        constraint "answer_attachments_answer_id"
            foreign key ("answer_id") references "answer",
        constraint "answer_attachments_attachments_id"
            foreign key ("attachments_id") references "attachment"
);

create table "question"
(
    "id"         BIGINT auto_increment
        primary key,
    "contact"    CHARACTER VARYING(255),
    "content"    CHARACTER VARYING(65535) not null,
    "created_at" TIMESTAMP WITH TIME ZONE,
    "published"  BOOLEAN not null default false,
    "updated_at" TIMESTAMP WITH TIME ZONE,
    "answer_id"  BIGINT
        constraint "question_answer_id_unique"
            unique,
    "user_id"    BIGINT,
        constraint "question_answer_id"
            foreign key ("answer_id") references "answer",
        constraint "question_user_id"
            foreign key ("user_id") references "user"
);

create table "question_attachments"
(
    "question_id"    BIGINT not null,
    "attachments_id" UUID   not null,
        constraint "question_attachments_attachments_id"
            foreign key ("attachments_id") references "attachment",
        constraint "question_attachments_question_id"
            foreign key ("question_id") references "question"
);
