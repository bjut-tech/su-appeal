alter table announcement
    add column pinned bit not null default _binary '\0';

alter table question
    add column campus varchar(255) null;
