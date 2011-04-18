#!/bin/bash

# erste Variable: Verzeichnis
Verzeichnis="$1"

echo $Verzeichnis
/bin/mkdir -m 0775 "$Verzeichnis"

