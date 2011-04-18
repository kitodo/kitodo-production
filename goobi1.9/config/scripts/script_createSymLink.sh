#!/bin/bash

# erste Variable: Ausgangsverzeichnis
# zweite Variable: Linkname
Ausgangsverzeichnis="$1"
Linkname="$2"
Benutzer="$3"

echo $Ausgangsverzeichnis 
echo $Linkname

ln -s "$Ausgangsverzeichnis" "$Linkname"
sudo /bin/chown -R "$Benutzer" "$Ausgangsverzeichnis" 
