#! /bin/bash
#See readme.adoc
# Modify your /etc/hosts to include biodiversity.local and id.biodiversity.local at 127.0.0.1

HOST_ADDR="$(ip -4 addr show docker0 | grep -Po 'inet \K[\d.]+')"
export HOST_ADDR
export BASE_DOMAIN=biodiversity.local
export DOMAIN_PREFIX=""
export DOMAIN_PREFIX_DASH=""
export DB_HOST="postgresql://${HOST_ADDR}:5432"

VOLUME_ROOT="$(pwd)"
export VOLUME_ROOT

echo $VOLUME_ROOT
echo $BASE_DOMAIN
echo $DOMAIN_PREFIX
echo $DB_HOST

#update the mapper and nsl db so they point back to biodiversity.local
psql -f etc/repoint.sql nsl

#test ! -d etc/nginx/conf.d/sites-enabled && mkdir etc/nginx/conf.d/sites-enabled
#envsubst '${DOMAIN_PREFIX} ${DOMAIN_PREFIX_DASH} ${BASE_DOMAIN}' < etc/nginx/conf.d/site-template/all-shards.conf > etc/nginx/conf.d/sites-enabled/shards.conf

#unpack the LDAP instance files
cd etc && test -d instances && rm -rf instances
unzip -q instances.zip
cd ..
#deploy the reverse proxy and mapper
docker-compose -f etc/devel-docker-compose.yml up -d