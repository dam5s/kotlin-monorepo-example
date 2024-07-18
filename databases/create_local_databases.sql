drop database if exists greetings_dev;
drop database if exists greetings_test;

drop user monorepodev;

create user monorepodev with password 'monorepodev';

create database greetings_dev;
create database greetings_test;

grant all privileges on database greetings_dev to monorepodev;
grant all privileges on database greetings_test to monorepodev;

\c greetings_dev
grant create on schema public to monorepodev;

\c greetings_test
grant create on schema public to monorepodev;
