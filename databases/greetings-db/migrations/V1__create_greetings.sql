create table greetings (
    id   uuid primary key not null default gen_random_uuid(),
    text varchar
);
