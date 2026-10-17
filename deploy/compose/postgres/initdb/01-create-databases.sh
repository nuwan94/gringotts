#!/bin/bash
# Database per service: each service gets its own database and owning role, so no service can
# read another's tables. Local development only: each role's password equals its name.
# REPLICATION lets Debezium stream the service's outbox table using that role.
set -euo pipefail

for db in keycloak ${GRINGOTTS_DATABASES}; do
  echo "Creating database and role: ${db}"
  psql -v ON_ERROR_STOP=1 --username "${POSTGRES_USER}" --dbname postgres <<EOSQL
CREATE ROLE "${db}" LOGIN PASSWORD '${db}' REPLICATION;
CREATE DATABASE "${db}" OWNER "${db}";
EOSQL
done
