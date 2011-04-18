#!/bin/bash

# erste Variable: Benutzer
# zweite Variable: Verzeichnis
Benutzer="$1"
Verzeichnis="$2"

#echo $Benutzer 
#echo $Verzeichnis

#sudo /bin/mkdir -m 0775 "$Verzeichnis"
sudo /bin/mkdir "$Verzeichnis"
sudo /bin/chmod g+w "$Verzeichnis"
sudo chown $Benutzer "$Verzeichnis"
sudo /bin/chgrp tomcat "$Verzeichnis"

#sudo -u $Benutzer /bin/mkdir -m 0775 "$Verzeichnis"
#sudo /bin/chgrp tomcat "$Verzeichnis" 
