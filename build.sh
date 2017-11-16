#!/bin/bash
mvn install
cp target/ENA-HarvesterService_5.0.0-SNAPSHOT.war bin/build/harvester.war
cd bin/build
docker build -t harvester-container:5.0 . 
docker rm -f ena_harvester
docker run --name ena_harvester -d -p 8080:8080 --net elasticsearchmapping_elk harvester-container:5.0
docker ps