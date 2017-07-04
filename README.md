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
Two things that are not shipped are a Tomcat specific servlet API and an ElasticSearch server. First thing should come along with the Tomcat distribution you are building against. Second, you need to download by yourself from [ElasticSearch 5.1.1](https://www.elastic.co/downloads/past-releases/elasticsearch-5-1-1) and start before Maven build. On the Windows system ElasticSearch starts after open elasticsearch.bat file from bin folder. Additional information about running of ElasticSearch can be found [here](https://www.elastic.co/downloads/elasticsearch).

### Vanilla build using Maven

Execute "mvn clean package" and put generated war file in Tomcat.

### Configuring your distribution

Basic configuration files are located under `src/main/resources/` directory. To provide a custom (local) configuration, create a directory `config-local` and put there your specific configuration files just before you create a distribution via the `mvn` command. The build script will then replace every default configuration file with the configuration file it finds in your `config-local` directory.

Most probably, you will have to adjust these four files:
* kitodo_config.properties
* contentServerConfig.xml
* hibernate.cfg.xml
* log4j2.properties

Setting up a Kitodo instance can be quite tricky. For more help on how to configure Kitodo, please check the [installation guides](https://github.com/kitodo/kitodo-production/wiki/Installationsanleitung), the [GitHub Wiki](https://github.com/kitodo/kitodo-production/wiki) or ask questions on the [mailing lists](https://github.com/kitodo/kitodo-production/wiki#Mailingliste).

### Generating Javadocs

The generation of Javadocs is issued by calling 
``` 
mvn javadoc:aggregate
```
inside the Kitodo.Production main folder. This call produces the documentation inside the default directory beneath *${project.basedir}/target/site*. 

To transport the generated file to the correct place (*${project.basedir}/docs*) issue
```
mvn --non-recursive antrun:run
```

Afterwards the contents of the "docs" directory may be committed.

To make sure that Javadocs are up to date at the time of the committing the call of
```
mvn clean
```
also deletes the contents of _JavaDocs/JavaDocsGenerationDir_. This makes sure that the directory does not contain parts of files 
that came into existence during earlier calls of the generation. To get a fresh Javadoc you may issue subsequently:

```
mvn clean
mvn javadoc:aggregate 
mvn --non-recursive antrun:run
```
Be careful on committing. Look twice what you commit. It may be the case that you checked out the JavaDocs and deleted them
during the build process locally. It is possible you don't want to commit them (as they are locally deleted).

#### Javadoc is graceful
The configuration of the Javadoc-plugin is set to be graceful. The config tells Javadoc not to stop if there is an error and not
to fail if there was one. This gives the developer the chance to generate "incorrect" Javadocs which also includes
Javadocs that are incomplete (for example because of missing docs for a parameter of a function). 

Changing these parameters may result in Javadocs of a higher quality as lots of documentation is requested but also may
result in no documentation at all because the compilation just stops because of bad or insufficient Javadoc comments.

```xml
<configuration>
	<!-- setting to true halts the generation at the first error -->
	<failOnError>false</failOnError>
	<!-- commenting this line enables the doclint checks on the sources - this 
	     leads to fail with badly documented sources -->
	<additionalparam>-Xdoclint:none</additionalparam>
</configuration>
```
