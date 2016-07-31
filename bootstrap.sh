#!/usr/bin/env bash

APP_DB_USER=myapp
APP_DB_PASS=dbpass
APP_DB_NAME=contactInfo


apt-get update
apt-get install -y software-properties-common python-software-properties

#Installing Java 8
add-apt-repository -y ppa:webupd8team/java
apt-get update

echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections
echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections
apt-get -y install oracle-java8-installer 

#Installing Maven
apt-get install -y maven

#Installing PostgreSQL
apt-get install -y postgresql postgresql-contrib

#Installing Git
apt install -y git

#Creating database
#Restart so that all new config is loaded:
service postgresql restart

cat << EOF | su - postgres -c psql
-- Create the database user:
CREATE USER $APP_DB_USER WITH PASSWORD '$APP_DB_PASS';

-- Create the database:
CREATE DATABASE $APP_DB_NAME WITH OWNER=$APP_DB_USER
                                  LC_COLLATE='en_US.utf8'
                                  LC_CTYPE='en_US.utf8'
                                  ENCODING='UTF8'
                                  TEMPLATE=template0;
EOF

# Tag the provision time:
date > "$PROVISIONED_ON"

echo "Successfully created PostgreSQL dev virtual machine."

git clone https://github.com/CatherineNezhumira/DBTask.git

cd ./DBTask

mvn spring-boot:run -Dspring.datasource.username=$APP_DB_USER -Dspring.datasource.password=$APP_DB_PASS -Dspring.datasource.url=jdbc:postgresql://localhost:5432/$APP_DB_NAME
