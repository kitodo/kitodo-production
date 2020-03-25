# Allgemein

* Arbeit in GIT nach dem [Forking Workflow](https://www.atlassian.com/git/tutorials/comparing-workflows/forking-workflow)
* Einhaltung der [Coding Guidelines](https://www.kitodo.org/fileadmin/groups/kitodo/Dokumente/Kitodo-EntwicklerLeitfaden_2017-06.pdf)
* zu entwickelnde Features sollten als Issue vor dem Pull-Request bekannt gemacht werden
* Fork Branch Hinweise:
 * Jeder Branch sollte in sich geschlossen sein und nur genau die Änderungen beinhalten, die nötig sind
 * können zum Bearbeiten eines Features entweder in privaten, persönlichen Forks oder in einem Fork einer GitHub-Organisation  durch mehrere Personen durchgeführt werden

# Commits

* in englischer Sprache, Orientierung an bspw. <http://chris.beams.io/posts/git-commit/>
* Commits sollten nur die Änderungen enthalten, die auch in der Commit Nachricht beschrieben sind
* eher viele kleine Commits mit jeweils wenigen Änderungen als wenige, große / umfangreiche Commits

# Pull-Requests

* sollten idealerweise von einer anderen Person als dem Ersteller auf GitHub begutachtet (review changes) werden.
* müssen zum Zeitpunkt des Merges fehlerfrei integrierbar sein. Konflikte müssen vom Ersteller gelöst werden.

# Branch Unterscheidung

* Unterscheidung bezieht sich auf die GitHub Projekte [Kitodo.ContentServer](https://github.com/kitodo/kitodo-contentserver), [Kitodo.Production](https://github.com/kitodo/kitodo-production) und [Kitodo.UGH](https://github.com/kitodo/kitodo-ugh)
* **Branch 2.x**: ist die Weiterentwicklung der alten Goobi.Production Community Edition (Version 1.11.x) unter dem neuen Namen Kitodo.Production und wird als Version 2.x weiter geführt
* **Branch master**: die unter dem DFG Projekt geförderten Weiterentwicklung von Kitodo.Production findet hier statt und enthält auch die darauf basierenden betriebenen Entwicklungen
