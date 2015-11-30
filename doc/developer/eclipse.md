Developing Goobi Production using Eclipse
=========================================

How not to commit the Eclipse `.project` files
------------------------------------------------

 * Go to the file system, find the `.git` folder in your Eclipse Goobi Production project.
 * Find the `info` subfolder (or create it if missing)
 * Create a file named `exclude` (no extension).
 * Put any files and directories you need to exclude there.
 
Example content:

```txt
/.project
/.classpath
/.settings/
/Goobi/META-INF/
```