# GeRDI Harvester Image for ENA Harvesters

FROM jetty:9.4.7-alpine

COPY \/target\/*.war $JETTY_BASE\/webapps\/ena.war

EXPOSE 8080