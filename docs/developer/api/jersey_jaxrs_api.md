Jersey WebAPI
=============

The Jersey based Webapi has been introduced to quickly query process status and
configuration via Web interface. It currently exposes three resources:
Processes, Projects and CatalogConfiguration under the URL root /rest.

Implementation and Configuration
--------------------------------

The web resources belong to the org.goobi.webapi package and use standard Java
JAX-RS API annotations to denote resource routing. Coming from different people,
data access is implemented in different ways. The Processes resources uses a DAO
to query Hibernate and deliver POJO Bean instances, whereas Projects and
CatalogConfiguration resources directly access to internal Kitodo API.

### Jersey Servlet Configuration

The Jersey Container servlet is responsible for translating requests to Java
calls and for serializing the returned Java objects. It is configured in the
applications web.xml file by default:

 	<servlet>
        <servlet-name>Kitodo REST Service based on Jersey</servlet-name>
        <servlet-class>com.sun.jersey.spi.container.servlet.ServletContainer</servlet-class>
        <init-param>
            <param-name>com.sun.jersey.config.property.packages</param-name>
            <param-value>org.goobi.webapi.resources; org.goobi.webapi.provider</param-value>
        </init-param>
        <!-- explanation of load-on-startup: http://stackoverflow.com/a/1298984 -->
        <load-on-startup>1</load-on-startup>
    </servlet>

    <servlet-mapping>
        <servlet-name>Kitodo REST Service based on Jersey</servlet-name>
        <url-pattern>/rest/*</url-pattern>
    </servlet-mapping>

If you do not want to provide that service remove the corresponding Servlet definition
and mapping.

Processes and Steps
-------------------

### /processes

To get JSON/XML formatted list of all current processes in the system a GET
request to /rest/processes:

$ curl http://localhost:8080/kitodo/rest/processes

    {
        "goobiProcess":
        [ 
            {
                "identifier":"1118749846",
                "title":"Abbildungen von Dresdens alten und neuen Pracht-Gebäuden, Volks- und Hof-Festen"
            },
            {
                "identifier":"118765094",
                "title":"Dresden mit seinen Prachtgebäuden und schönsten Umgebungen" 
            }
        ]
    }

$ curl -H 'Accept: application/xml' http://localhost:8080/kitodo/rest/processes

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?> 
    <goobiProcesses>
        <goobiProcess>
            <identifier>118749846</identifier>
            <title>Abbildungen von Dresdens alten und neuen Pracht-Gebäuden, Volks- und Hof-Festen</title>
        </goobiProcess>
        <goobiProcess>
             <identifier>118765094</identifier>
             <title>Dresden mit seinen Prachtgebäuden und schönsten Umgebungen</title>
        </goobiProcess>
    </goobiProcesses>

Note that the amount of data returned can be very big depending on the number of
processes in your system. The output appears to be more performant.

### /processes/\<Identifier\>
Get XML formatted information about a single process:

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <goobiProcess>
        <identifier>319329496</identifier>
        <title>An das Königliche Ministerium des Innern, Abteilung für Ackerbau, Gewerbe und Handel zu Dresden</title>
    </goobiProcess>

### /process/\<Identifier\>/steps
Get XML formatted information about the steps of a specific process and their current state.

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <goobiProcessSteps>
        <goobiProcessStep>
            <sequence>1</sequence>
            <state>3</state>
            <title>Anlegen eines Vorganges</title>
        </goobiProcessStep>
         <goobiProcessStep>
            <sequence>2</sequence>
            <state>3</state>
            <title>Scannen</title> 
        </goobiProcessStep>
        <goobiProcessStep>
             <sequence>3</sequence>
             <state>3</state>
             <title>Erfassen der Meta- und Strukturdaten</title>
        </goobiProcessStep>
         <goobiProcessStep>
            <sequence>4</sequence>
            <state>3</state>
            <title>Export / Import in das DMS</title>
        </goobiProcessStep>
    </goobiProcessSteps>

CatalogConfiguration
--------------------

// TODO Add resource documentation for /rest/CatalogConfiguration.

Projects
--------

// TODO Add resource documentation for /rest/projects.

