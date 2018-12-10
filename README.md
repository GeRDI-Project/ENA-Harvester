# The ENA Harvester

This is a harvester for the [ENA Repository][6] build by Jan Frömberg ([jan.froemberg@tu-dresden.de](mailto:jan.froemberg@tu-dresden.de)).
This harvester was build on top of a [RESTful-Harvester Library][1] provided by the University of Kiel ([Robin Weiss](mailto:row@informatik.uni-kiel.de)).

## Prerequisites

[Docker][3] if you plan to build the harvester within a Docker-Image.
Otherwise you need a Java Application Server like [Glassfish][2], [Tomcat][5] or [Jetty][4]
There you have to deploy the created war-File.

## How to build?

You can easily build a docker image by using the terminal and typing:

    $ mvn clean verify -DdockerBuild

There is also a utility script for building and running a Jetty docker container via:

    $ mvn clean verify -DdockerRun

## How to run?

Base-URL: [http://localhost:8080/ena](http://localhost:8080/ena). 
You have two options for harvesting the ENA DB. Option 1 is by Accession-Number und Option 2 is by Taxonomy via a Taxon-Key like 10088.

Requests on Resource : /harvest

    * GET			Overview
    * POST			Starts the harvest
    * POST/abort	Aborts an ongoing harvest, save, or submission
    * POST/submit	Submits harvested documents to a DataBase
    * POST/save		Saves harvested documents to disk

Request on Resource : /harvest/config

    * GET		Overview
    * POST		Saves the current configuration to disk.
    * PUT 		Sets x-www-form-urlencoded parameters for the harvester.
    (PUT) Valid values: harvestFrom, harvestTo, from, until, hostUrl, metadataPrefix, autoSave, autoSubmit, submissionUrl,
    submissionUserName, submissionPassword, submissionSize, readFromDisk, writeToDisk, keepCachedDocuments, deleteFailedSaves.

All libraries and bundles included in this build are
released under the Apache license.

Enjoy!

[1]: https://code.gerdi-project.de/projects/HAR/repos/harvesterbaselibrary
[2]: https://javaee.github.io/glassfish
[3]: https://www.docker.com
[4]: https://www.eclipse.org/jetty
[5]: https://tomcat.apache.org
[6]: https://www.ebi.ac.uk/ena
