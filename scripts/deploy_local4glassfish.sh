#!/bin/bash
mvn install
cp target/OAIPMH-HarvesterService_*.war bin/build/harvester.war
cd bin/build
/usr/local/opt/glassfish/libexec/bin/asadmin redeploy --name harvester --contextroot "/harvester" harvester.war
