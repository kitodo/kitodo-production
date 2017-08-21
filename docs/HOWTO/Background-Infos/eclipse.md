Checking out Kitodo.Production in Eclipse
=========================================

Prerequisites:
--------------
* Make sure you have [Java SE Development Kit](http://www.oracle.com/technetwork/java/javase/downloads/index.html) in at least version 8 installed
* Make sure you have [Eclipse IDE for Java EE Developers](https://www.eclipse.org/downloads/) installed


Create a new workspace
----------------------
Importing Kitodo Production into Eclipse will clutter your workspace with a lot of projects. If you feel uncomfortable about this, select *File → Switch workspace → Other…* to create a new workspace.


Install m2e-egit Team provider
------------------------------
Select *File → Import…*. From the *Maven* folder, choose *Check out Mavan Projects from SCM*  and click *Next >*. (If the drop-down box next to *SCM URL* on the next screen contains the option *git*, you can skip this step.) Select *Find more SCM connectors in the m2e Marketplace* on the bottom of the window. Scroll down the list now appearing and select *m2e-egit* from the *m2e Team providers* section. Install it and restart Eclipse.


Check out Kitodo.Production source
----------------------------------
Select *File → Import…*. From the *Maven* folder, choose *Check out Mavan Projects from SCM*  and click *Next >*. Select *git* from the drop-down box next to *SCM URL* and paste the following string into the box aside:

    https://github.com/kitodo/kitodo-production.git

Click *Finish*. Be prepared to wait (six minutes, in my case) for the system to complete.



Developing Kitodo Production using Eclipse
==========================================

How not to commit the Eclipse `.project` and runtime files
----------------------------------------------------------

 * Go to the file system, find the `.git` folder in your Eclipse Kitodo Production project.
 * Find the `info` subfolder (or create it if missing).
 * Create a file named `exclude` (no extension).
 * List any files and directories you need to exclude in that file.
 * Sometimes you need to restart Eclipse for changes to take effect.
 
Example content:
```
/.project
/.classpath
/.settings/
/Goobi/META-INF/
/Goobi/src/*.properties
/Goobi/src/*.txt
/Goobi/src/*.xml
/Goobi/src/*.xsl
```
