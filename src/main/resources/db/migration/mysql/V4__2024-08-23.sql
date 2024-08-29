alter table `like`
    modify column user_id bigint not null;

create table announcement_carousel (
    id bigint auto_increment
        primary key,
    announcement_id bigint not null,
    cover_id binary(16) null,
    created_at datetime(6) null,
    updated_at datetime(6) null,
    constraint announcement_carousel_announcement_id
        foreign key (announcement_id) references announcement (id),
    constraint announcement_carousel_cover
        foreign key (cover_id) references attachment (id)
);

create table announcement_category (
    id bigint auto_increment
        primary key,
    name varchar(255) not null,
    description text null,
    created_at datetime(6) null,
    updated_at datetime(6) null
);

alter table announcement
    add column category_id bigint null,
    add column hidden bit not null default _binary '\0',
    add constraint announcement_category_id
        foreign key (category_id) references announcement_category (id);

create index announcement_pinned_id
    on announcement (pinned desc, id desc);
