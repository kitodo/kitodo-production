How to run Kitodo.Production in demo mode
=================================================================================

Prerequisites:
--------------
* Make sure you have [Java SE Development Kit](http://www.oracle.com/technetwork/java/javase/downloads/index.html) in at least version 11 installed
* Make sure you have [Git](https://git-scm.com/downloads) installed
* Make sure you have [Maven](https://maven.apache.org/download.cgi) installed

Get the source code
-------------------
If you have not done yet, [fork](https://help.github.com/articles/fork-a-repo/) the [project repository](https://github.com/kitodo/kitodo-production) on GitHub. Use Git (using a tool like [TortoiseGit](https://tortoisegit.org/), or the command line) to clone the repository as a subdirectory.

Start the application
-------------------

On the command line, change to the directory you just cloned and execute the following command:


    mvn clean install '-Pdemo,!development'

(Without ' ' on windows cmd)

This will set up the environment (elastic search, H2 database, Apache Tomcat webserver) and provides some example data.
To stop the application just hit Ctrl + c at the command line window. Be aware that every data which has been inserted is lost when the application is stopped.
Do not use in a production system!

