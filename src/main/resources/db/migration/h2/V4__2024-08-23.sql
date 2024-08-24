alter table "like"
    alter column "user_id" set not null;

create table "announcement_category" (
    "id" BIGINT auto_increment
        primary key,
    "name" CHARACTER VARYING(255) not null,
    "description" CHARACTER VARYING(65535) null,
    "created_at" TIMESTAMP WITH TIME ZONE,
    "updated_at" TIMESTAMP WITH TIME ZONE
);

alter table "announcement"
    add column "category_id" BIGINT;

alter table "announcement"
    add constraint "announcement_category_id"
        foreign key ("category_id") references "announcement_category";
