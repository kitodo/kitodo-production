Kitodo.Production
=================

Kitodo.Production is a workflow tool suite for the support of mass digitization. Kitodo.Production is part of the Kitodo Digital library Suite.

Kitodo is an open source software suite intended to support mass digitization projects for cultural heritage institutions. Kitodo is widely-used and cooperatively maintained by major German libraries and digitization service providers. The software implements international standards such as METS, MODS and other formats maintained by the Library of Congress. Kitodo consists of several independent modules serving different purposes such as controlling the digitization workflow, enriching descriptive and structural metadata, and presenting the results to the public in a modern and convenient way.

To get more information, visit the following web sites:
http://www.kitodo.org

You can also follow Kitodo News on Twitter at @Kitodo\_org


Building
--------

The software is written in Java and using Java Server Faces web technology to run on a [Tomcat Servlet container](http://tomcat.apache.org/) backed up by a [MySQL](http://www.mysql.com) database accessed utilizing the [Hibernate framework](http://www.hibernate.org). It uses (Unix/Windows) shell scripts and is often used with Windows shares in [Samba](http://www.samba.org/) environments authenticated via LDAP.

The project structure is IDE independent. The tool for issue building, testing
and packaging of the application is [Maven](https://maven.apache.org/).

### Dependencies

Available dependencies are fetched from Maven Central. Further dependencies (not available in Maven Central) are located in Kitodo/src/main/webapp/WEB-INF/lib.
The only thing that is not shipped is a Tomcat specific servlet API. This should come along with the Tomcat distribution you are building against.

### Vanilla build using Maven

Execute "mvn clean package" and put generated war file in Tomcat.

### Configuring your distribution

Basic configuration files are located under `src/main/resources/` directory. To provide a custom (local) configuration, create a directory `config-local` and put there your specific configuration files just before you create a distribution via the `mvn` command. The build script will then replace every default configuration file with the configuration file it finds in your `config-local` directory.

Most probably, you will have to adjust these four files:
* goobi_config.properties
* contentServerConfig.xml
* hibernate.cfg.xml
* log4j.properties

Setting up a Kitodo instance can be quite tricky. For more help on how to configure Kitodo, please check the [installation guides](https://github.com/kitodo/kitodo-production/wiki/Installationsanleitung), the [GitHub Wiki](https://github.com/kitodo/kitodo-production/wiki) or ask questions on the (mailing lists)[https://github.com/kitodo/kitodo-production/wiki#Mailingliste].
