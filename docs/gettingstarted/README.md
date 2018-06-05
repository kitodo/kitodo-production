# Building

The software is written in Java and using Java Server Faces web technology to run on a [Tomcat Servlet container](http://tomcat.apache.org/) backed up by a [MySQL](http://www.mysql.com) database accessed utilizing the [Hibernate framework](http://www.hibernate.org). It uses (Unix/Windows) shell scripts and is often used with Windows shares in [Samba](http://www.samba.org/) environments authenticated via LDAP.

The project structure is IDE independent. The tool for issue building, testing
and packaging of the application is [Maven](https://maven.apache.org/).

## Dependencies

Available dependencies are fetched from Maven Central. Further dependencies (not available in Maven Central) are located in Kitodo/src/main/webapp/WEB-INF/lib.
Two things that are not shipped are a Tomcat specific servlet API and an ElasticSearch server. First thing should come along with the Tomcat distribution you are building against. Second, you need to download by yourself from [ElasticSearch 5.4.3](https://www.elastic.co/downloads/past-releases/elasticsearch-5-4-3) and start before Maven build. On the Windows system ElasticSearch starts after open elasticsearch.bat file from bin folder. Additional information about running of ElasticSearch can be found [here](https://www.elastic.co/downloads/elasticsearch).

## Building manuals

* [Build integrated demo](demo.md)
* [Build development version](development-version.md)
* [Eclipse on Windows](eclipse-windows.md)
* [Create VirtualBox Appliance](virtualbox.md)

## Configuring your distribution

Basic configuration files are located under `src/main/resources/` directory. To provide a custom (local) configuration, create a directory `config-local` and put there your specific configuration files just before you create a distribution via the `mvn` command. The build script will then replace every default configuration file with the configuration file it finds in your `config-local` directory.

Most probably, you will have to adjust these four files:
* kitodo_config.properties
* contentServerConfig.xml
* hibernate.cfg.xml
* log4j2.properties

Setting up a Kitodo instance can be quite tricky. For more help on how to configure Kitodo, please check the [installation guides](https://github.com/kitodo/kitodo-production/wiki/Installationsanleitung), the [GitHub Wiki](https://github.com/kitodo/kitodo-production/wiki) or ask questions on the [mailing lists](https://github.com/kitodo/kitodo-production/wiki#Mailingliste).
