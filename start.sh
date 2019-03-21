#!/bin/bash

set -e

DIR="/opt/perimeter"


# Prepare config files
mkdir -p ${DIR}

cp examples/config/*.yml ${DIR}/
cp examples/haproxy.cfg ${DIR}/
cp config_templates ${DIR}/

# Create Docker networks
docker network create themis --subnet 10.255.255.0/24 || true
docker network create team01 --subnet 10.255.0.0/24 || true
docker network create team02 --subnet 10.255.1.0/24 || true

# Enable communication between Docker subnets
sudo iptables -I FORWARD -s 10.255.0.0/16 -d 10.255.0.0/16 -j ACCEPT
# Disable IP masquerade
sudo iptables -t nat -I POSTROUTING -s 10.255.0.0/16 -d 10.255.255.0/24 -j RETURN

# PostreSQL
docker rm -f postgres || true
docker run -d --net team01 --name postgres --restart=always \
  -p 5432:5432/tcp -e POSTGRES_PASSWORD=P@ssw0rd \
  postgres

POSTGRES_IP=($(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}} {{end}}' postgres))

# Wait for PostgreSQL
while ! nc -z -vvv -w 3 ${POSTGRES_IP[0]} 5432; do
    sleep 1
done

docker exec -it postgres psql -U postgres -c 'CREATE DATABASE perimeter;'

# HAProxy
docker rm -f haproxy || true
touch ${DIR}/haproxy.cfg
docker run -d --net team01 --name haproxy --restart=always \
  -v ${DIR}/haproxy.cfg:/usr/local/etc/haproxy/haproxy.cfg \
  haproxy

HAPROXY_IP=($(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}} {{end}}' haproxy))

# Wait for HAProxy
while ! nc -z -vvv -w 3 ${HAPROXY_IP[0]} 1936; do
    sleep 1
done

# Themis
docker rm -f themis || true
docker run -d --net themis --name themis --restart=always \
  -v ${DIR}/themis.yml:/opt/themis/themis.yml \
  v0rt3x/themis-mock-server

THEMIS_IP=($(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}} {{end}}' themis))

# Wait for Themis
while ! nc -z -vvv -w 3 ${THEMIS_IP[0]} 5000; do
    sleep 1
done

sed -i "s/host: .*$/host: ${THEMIS_IP[0]}/g" perimeter.yml
sed -i "s/internal-ip: .*$/internal-ip: ${HAPROXY_IP[0]}/g" perimeter.yml

# Perimeter Server
docker rm -f perimeter || true

touch ${DIR}/auth.storage
touch ${DIR}/master.key

docker create --net team01 --name perimeter --restart=always \
  -v ${DIR}/perimeter.yml:/opt/perimeter/perimeter.yml \
  -v ${DIR}/sshd:/opt/perimeter/sshd \
  v0rt3x/perimeter-server
docker start perimeter

PERIMETER_IP=($(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}} {{end}}' perimeter))

# Wait for Perimeter
while ! nc -z -vvv -w 3 ${PERIMETER_IP[0]} 8080; do
    sleep 1
done

sed -i "s/host: .*$/host: ${PERIMETER_IP[0]}/g" perimeter-executor.yml
sed -i "s/host: .*$/host: ${PERIMETER_IP[0]}/g" perimeter-configurator.yml

# Perimeter Executor Agent
docker rm -f perimeter-executor || true
docker run -d --net team01 --name perimeter-executor --restart=always \
  -v ${DIR}/perimeter-executor.yml:/opt/perimeter/perimeter-executor.yml \
  v0rt3x/perimeter-executor

# Perimeter Configurator Agent
docker rm -f perimeter-configurator || true
docker run -d --net team01 --name perimeter-configurator --restart=always \
  -v ${DIR}/perimeter-configurator.yml:/opt/perimeter/perimeter-configurator.yml \
  -v /develop/perimeter/config_templates:/opt/perimeter/config_templates \
  -v ${DIR}/haproxy.cfg:/etc/haproxy/haproxy.cfg \
  -v $(which docker):$(which docker) -v /var/run/docker.sock:/var/run/docker.sock \
  v0rt3x/perimeter-configurator

echo "Perimeter Server is UP"
echo ""
echo "Themis IP: ${THEMIS_IP[0]}"
echo "Perimeter Server IP: ${PERIMETER_IP[0]}"
