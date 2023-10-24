alter table "announcement"
    add column "pinned" BOOLEAN not null default false;

alter table "question"
    add column "campus" CHARACTER VARYING(255);
