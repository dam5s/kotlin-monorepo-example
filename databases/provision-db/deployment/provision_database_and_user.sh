#!/usr/bin/env sh

set -e

if [ -z "$PGHOST" ]; then
  echo "Missing PGHOST"
  exit 1
fi

if [ -z "$PGUSER" ]; then
  echo "Missing PGUSER"
  exit 1
fi

if [ -z "$PGPASSWORD" ]; then
  echo "Missing PGPASSWORD"
  exit 1
fi

if [ -z "$PROVISION_USER" ]; then
  echo "Missing PROVISION_USER"
  exit 1
fi

if [ -z "$PROVISION_PASSWORD" ]; then
  echo "Missing PROVISION_PASSWORD"
  exit 1
fi

if [ -z "$PROVISION_DATABASE" ]; then
  echo "Missing PROVISION_DATABASE"
  exit 1
fi

echo "Creating user"
psql postgres -c "create user $PROVISION_USER with password '$PROVISION_PASSWORD'";

echo "Creating database"
psql postgres -c "create database $PROVISION_DATABASE";

echo "Setting database privileges"
psql postgres -c "grant all privileges on database $PROVISION_DATABASE to $PROVISION_USER;"

echo "Setting public schema privileges"
psql $PROVISION_DATABASE -c "grant create on schema public to $PROVISION_USER";
