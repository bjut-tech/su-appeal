alter table `like`
    modify column user_id bigint not null;

create table `announcement_category` (
    id bigint auto_increment
        primary key,
    name varchar(255) not null,
    description text null,
    created_at datetime(6) null,
    updated_at datetime(6) null
);

alter table `announcement`
    add column category_id bigint null,
    add constraint announcement_category_id
        foreign key (category_id) references "announcement_category" (id);
