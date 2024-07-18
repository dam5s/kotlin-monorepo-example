FROM debian:bookworm-slim

RUN apt-get update
RUN apt-get install -y postgresql-client

WORKDIR /scripts

COPY provision_database_and_user.sh .
RUN chmod u+x provision_database_and_user.sh

CMD [ "./provision_database_and_user.sh" ]
