How to create a developer workspace for Kitodo.Production with Eclipse on Windows
=================================================================================

Prerequisites:
--------------
* Make sure you have [Java SE Development Kit](http://www.oracle.com/technetwork/java/javase/downloads/index.html) in at least version 11 installed
* Make sure you have [Git](https://git-scm.com/downloads) installed
* Make sure you have [Maven](https://maven.apache.org/download.cgi) installed
* Make sure you have [Eclipse IDE for Java EE Developers](https://www.eclipse.org/downloads/) installed


Configure the `PATH`s
---------------------

Make sure the Maven `bin` directory is on your `PATH`, and that `JAVA_HOME` points to the root directory of the JDK (not the JRE). Right-click the start menu, select *System*, *Extended system settings*, *Environment variables* to do so. Be careful when you try to add an entry to the path: Click *New*, then type at least one character into the appearing text box before clicking *Browse…*. Otherwise, instead, the last entry in the list will be replaced. You need to restart an open `cmd.exe` window for the changes to take effect.

Make sure the path of your Eclipse Workspace does not contain any dot. To check this, right-click on your Eclipse workspace folder in Windows Explorer, select Properties, Security, and examine the object name. If necessary, move your workspace or create a new one in a suitable location.


Get the source code
-------------------
If you have not done yet, [fork](https://help.github.com/articles/fork-a-repo/) the [project repository](https://github.com/kitodo/kitodo-production) on GitHub. Use Git (using a tool like [TortoiseGit](https://tortoisegit.org/), or the command line) to clone the repository as a subdirectory into your Eclipse workspace.


Create the data-file structure
------------------------------
Create the data-file structure required by Kitodo.Production (outside the Eclipse workspace):

    mkdir config debug logs messages metadata modules plugins plugins\command plugins\import plugins\opac plugins\step plugins\validation rulesets scripts temp users xslt
	
Copy all `kitodo_*.xml` from `Kitodo\src\main\resources\` into the `config` directory.

Copy the contents of `Kitodo\rulesets\` to the `rulesets` directory.

Create the following four batch files with the following content in the in the `scripts` directory:

File                         | Content
---------------------------- | -----------------
script_createDirMeta.bat     | `mkdir %1`
script_createDirUserHome.bat | `mkdir %2`
script_createSymLink.bat     | `mklink /d %2 %1`
script_deleteSymLink.bat     | `rmdir %1`

Copy `*.xsl` from `Kitodo\src\main\resources\` to the folder `xslt`.


Create your `config-local`
--------------------------
In the source directory that you just cloned, create a folder named `config-local`. Copy the following files there:

*From `Kitodo\src\main\resources\`:*
* `contentServerConfig.xml`
* `hibernate.cfg.xml`
* `kitodo_config.properties`

*From `Kitodo-DataManagement\src\main\resources\db\config\`:*
* `flyway.properties`

Edit the following parameters your copy of `kitodo_config.properties` to point to the data directories you created. Use forward slashes as separators, and end the path entries with a forward slash. You need to edit the following parameters:
* `directory.config=D:/path to/config/`
* `directory.rulesets=D:/path to/rulesets/`
* `directory.xslt=D:/path to/xslt/`
* `directory.metadata=D:/path to/metadata/`
* `directory.users=D:/path to/users/`
* `directory.temp=D:/path to/temp/`
* `script_createDirUserHome=D:/path to/scripts/script_createDirUserHome.bat`
* `script_createDirMeta=D:/path to/scripts/script_createDirMeta.bat`
* `script_createSymLink=D:/path to/scripts/script_createSymLink.bat`
* `script_deleteSymLink=D:/path to/scripts/script_deleteSymLink.bat`
* `directory.messages=D:/path to/messages/`
* `directory.debug=D:/path to/debug/`
* `directory.modules=D:/path to/modules/`
* `directory.plugins=D:/path to/plugins/`

Edit `Kitodo\src\main\resources\log4j.xml` so that `<Property name="filename">` points to your `D:/path to/logs`.


Run Maven
---------
On the command line, change to the directory you just cloned and execute the following command:

    mvn clean install


Set up the database
-------------------
Create the MqSQL Database and the user:

    create database kitodo;
    grant all privileges on kitodo.* to kitodo@localhost identified by ´kitodo´;
    flush privileges;
	
First load `schema.sql`, then `default.sql` from the folder `Kitodo\setup` into the database. There will be warnings because the scripts handle some cases for backward compatibility. You can safely ignore them.

Then, change into the subfolder Kitodo-DataManagement and execute the following command to migrate your database:

    mvn clean install -Pflyway


Exclude the Eclipse `.project` and runtime files from Git
---------------------------------------------------------

 * Go to the file system and find the `.git` folder in the project folder in your Eclipse workspace. By default, this folder is hidden, so you need to configure your Explorer to show hidden files and folders to see it.
 * Find the `info` subfolder in that folder, o create it if it is missing.
 * Find or create a file named `exclude` (no extension).
 * List any files and directories you need to exclude in that file.
 * Sometimes you need to restart Eclipse for changes to take effect.
 
Example content:
```
**/.classpath
**/.project
**/.settings
```


Configure Eclipse
-----------------
Eclipse’s Maven import will create a lot of projects in your workspace which all need a bit of configuration, as listed below. You can simplify this by creating a new workspace and only configure the workspace settings once accordingly.

Add projects: In Eclipse, select *File*, *Import*, *Maven*, *Existing Maven project*. Point the *root directory* to your project directory and import all projects.

**Tomcat:** You have to increase the Tomcat startup time-out. You can do so from the server in the *Servers* view. If the Tomcat is not yet listed, select *Window*, *Preferences*, *Server*, *Runtime Environments* and add it there. Double-click the server in the *Servers* view, extend the time-out on the appearing page under *Timeouts*. You have to click *save* for the changes to take effect. I also recommend to open the *launch configuration* (from the same page), *Arguments*, and add `-Xmx3g` to *VM arguments*.

**Code formatter:** Import `config\Kitodo-IDE-formatting-Eclipse.xml` and set the code formatter to use the imported *Kitodo-Java Formatter Settings*.

**Imports:** The project has decided not to import packages, but only to explicitly import used classes, and to sort them alphabetically. Go to the Organize Imports page, remove all packages to be handled specially from the list, and set the both *Number of inputs needed* values to something like `2147483647`.

**Checkstyle:** Get the Checkstyle plug-in and configure it to use the config file `config\checkstyle.xml`.

Prepare the web application
---------------------------
For any further steps and throughout all development, make sure Elasticsearch is running. You start it by running `elasticsearch.bat` in the bin folder of the unzipped installation.

In Eclipse, right-click on the kitodo project, select run as, run on server, to launch Kitodo.Production in your Tomcat.

Access the web application under *http://<i></i>localhost:8080/kitodo* and log in as user `testadmin` with password `test`.

From the navigation menu, select *Indexing*. First, create the elasticsearch mapping by clicking the corresponding button on the top of the page. Then, create the whole index by clicking on the start indexing button on the bottom of the page. When this has finished, you have to log out and back into the web application.
