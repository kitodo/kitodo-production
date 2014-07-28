Goobi.Production
================

Goobi.Production is a workflow tool suite for the support of mass digitization. Goobi.Production is part of the Goobi Digital library Suite.

Goobi is an open source software suite intended to support mass digitization projects for cultural heritage institutions. Goobi is widely-used and cooperatively maintained by major German libraries and digitization service providers. The software implements international standards such as METS, MODS and other formats maintained by the Library of Congress. Goobi consists of several independent modules serving different purposes such as controlling the digitization workflow, enriching descriptive and structural metadata, and presenting the results to the public in a modern and convenient way.

To get more information, visit the following web sites:
http://www.goobi.org
http://wiki.goobi.org

You can also follow Goobi News on Twitter at @Goobi\_org


Building
--------

The software is written in Java and using Java Server Faces web technology to run on a [Tomcat Servlet container](http://tomcat.apache.org/) backed up by a [MySQL](http://www.mysql.com) database accessed utilizing the [Hibernate framework](http://www.hibernate.org). It uses (Unix/Windows) shell scripts and is often used with Windows shares in [Samba](http://www.samba.org/) environments authenticated via LDAP.

The project structure is IDE independent. The tool for issue building, testing
and packaging of the application is [Ant](http://ant.apache.org/). Ant gets
configured by providing a build.properties file next to the build.xml build-file.

### Dependencies

All dependent libraries are shipped with the source code. They can be found in the top level directory /lib.
The only thing that is not shipped is a Tomcat specific servlet API. This should come along with the Tomcat distribution you are building against.
Its location has to be specified either in the build configuration file build.properties or as a command line parameter to the ant command.

### Vanilla build using Ant

In order to build the application with (the rather useless) default configuration, you have to ensure that there is a Tomcat distribution ready. Then follow these simple steps in the project top-level directory:

1. Use the example `build.properties.templates` file to create an actual `build.properties` file.
	cp build.properties.template build.properties
2. Edit the file and provide the path to your Tomcat distributions `lib/` directory in the `tomcat.dir.lib` parameter.
3. Execute ant to generate Goobi.Production distributable WAR and JAR files. All build artifacts (class files, javadoc, test results) will end up in the `build/` directory. All distributables end up in the `dist/` directory.

### Configuring your distribution

Basic configuration files are located under `config/` directory. To provide a custom (local) configuration, create a directory `config-local` and put there your specific configuration files just before you create a distribution via the `ant` command. The build script will then replace every default configuration file with the configuration file it finds in your `config-local` directory.

Most probably, you will have to adjust these four files:
* goobi_config.properties
* contentServerConfig.xml
* hibernate.cfg.xml
* log4j.properties
* propertyTemplates.xml

Setting up a Goobi instance can be quite tricky. For more help on how to configure Goobi, please check the web sites above or ask questions on the mailing lists.
