# Contents of this directory

This directory is to contain a link to the generated JavaDoc-Documentation that is intentionally produced as a result
of a separate build process. The idea is to set up a nightly process that cares for that (script). This process 
builds the Javadocs outside the kitodo.production root directory. As a result the generated sources do not become 
part of the sources under revision control (makes merging easier).

## how to build
First of all (and probably only once) there must be a decision where to host the generated documentation files. The 
directory location found must be entered in kitodo-production/docs/JavaDocs/javadoc_index.md 
and in the script that triggers the build. These changes may be checked in.

To produce the Javadocs a script should be set up that performs these steps:
* check out kitodo.production and change to the checkout-directory
* run ```mvn javadoc:aggregate -P generate_developer_docs -Ddoctarget=path_to_desired_dir -Ddocdir=desired_dir```
The "path_to_desired_dir" parameter names the base directory the documentation is created in. The parameter
"desired_dir" names the directory where the documentation is created. These paths combined give the path to the "index.html" 
entry point of the Javadoc. This path must be put into kitodo-production/docs/JavaDocs/javadoc_index.md.

## how to check locally
The generated documentation is published by ReadTheDocs. Calling ```http://kitodo-production.readthedocs.io/en/latest/``` 
should open the latest documentation.

If the Javadocs are generated locally they may be viewed easily by setting up a simple MkDocs environment. To do so
just follow the instructions given here : ```http://www.mkdocs.org/#installation```

Thereafter go to the kitodo.production base directory (the one with the subdirecory "doc") and start Mkdocs 
by typing "mkdocs serve". To see the documentation point your browser to http://localhost:8000.

## further information
There should be further investigations concerning the generation of UML diagrams as part of the JavaDoc-generation. Many of the 
UML diagrams provided in the documentation just seem to illustrate the results of the JavaDoc generation.

There seem to be a lot of projects that cover the subject of UML generation. EX.: [generate UMLs in JavaDoc automatically](http://gochev.blogspot.de/2011/03/generate-javadoc-with-uml-diagrams.html)  
