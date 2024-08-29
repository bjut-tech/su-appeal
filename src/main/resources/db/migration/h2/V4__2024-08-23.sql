alter table "like"
    alter column "user_id" set not null;

create table "announcement_carousel" (
    "id" BIGINT auto_increment
        primary key,
    "announcement_id" BIGINT not null,
    "cover_id" UUID null,
    "created_at" TIMESTAMP WITH TIME ZONE,
    "updated_at" TIMESTAMP WITH TIME ZONE,
    constraint "announcement_carousel_announcement_id"
        foreign key ("announcement_id") references "announcement",
    constraint "announcement_carousel_cover"
        foreign key ("cover_id") references "attachment"
);

create table "announcement_category" (
    "id" BIGINT auto_increment
        primary key,
    "name" CHARACTER VARYING(255) not null,
    "description" CHARACTER VARYING(65535) null,
    "created_at" TIMESTAMP WITH TIME ZONE,
    "updated_at" TIMESTAMP WITH TIME ZONE
);

alter table "announcement"
    add column "category_id" BIGINT null;

alter table "announcement"
    add column "hidden" BOOLEAN not null default false;

alter table "announcement"
    add constraint "announcement_category_id"
        foreign key ("category_id") references "announcement_category";

create index "announcement_pinned_id"
    on "announcement" ("pinned" desc, "id" desc);
