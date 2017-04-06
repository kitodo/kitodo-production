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
/Kitodo/META-INF/
/Kitodo/src/*.properties
/Kitodo/src/*.txt
/Kitodo/src/*.xml
/Kitodo/src/*.xsl
```
