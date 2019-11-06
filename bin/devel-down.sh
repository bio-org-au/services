#! /bin/bash

#just exporting these environment variables to stop docker-compose complaining
HOST_ADDR="$(ip -4 addr show docker0 | grep -Po 'inet \K[\d.]+')"
export HOST_ADDR
export BASE_DOMAIN=biodiversity.local
export DOMAIN_PREFIX=""
export DOMAIN_PREFIX_DASH=""
export DB_HOST="postgresql://${HOST_ADDR}:5432"

VOLUME_ROOT="$(pwd)"
export VOLUME_ROOT

docker-compose -f etc/devel-docker-compose.yml down
