-- This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
-- 
-- Visit the websites for more information. 
--     		- http://www.goobi.org
--    		- http://launchpad.net/goobi-production
-- 		    - http://gdz.sub.uni-goettingen.de
--			- http://www.intranda.com
--			- http://digiverso.com 
--
-- This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
-- Software Foundation; either version 2 of the License, or (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
-- FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
-- Temple Place, Suite 330, Boston, MA 02111-1307 USA
--
-- Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
-- of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
-- link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
-- distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
-- conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
-- library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
-- exception statement from your version.

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `benutzer` (
  `BenutzerID` int(11) NOT NULL AUTO_INCREMENT,
  `Vorname` varchar(255) DEFAULT NULL,
  `Nachname` varchar(255) DEFAULT NULL,
  `login` varchar(255) DEFAULT NULL,
  `passwort` varchar(255) DEFAULT NULL,
  `IstAktiv` bit(1) DEFAULT NULL,
  `isVisible` varchar(255) DEFAULT NULL,
  `Standort` varchar(255) DEFAULT NULL,
  `metadatensprache` varchar(255) DEFAULT NULL,
  `css` varchar(255) DEFAULT NULL,
  `mitMassendownload` bit(1) DEFAULT NULL,
  `confVorgangsdatumAnzeigen` bit(1) DEFAULT NULL,
  `Tabellengroesse` int(11) DEFAULT NULL,
  `sessiontimeout` int(11) DEFAULT NULL,
  `ldapgruppenID` int(11) DEFAULT NULL,
  PRIMARY KEY (`BenutzerID`),
  KEY `FK6564F1FD78EC6B0F` (`ldapgruppenID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `benutzereigenschaften` (
  `benutzereigenschaftenID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `Wert` varchar(255) DEFAULT NULL,
  `IstObligatorisch` bit(1) DEFAULT NULL,
  `DatentypenID` int(11) DEFAULT NULL,
  `Auswahl` varchar(255) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `BenutzerID` int(11) DEFAULT NULL,
  PRIMARY KEY (`benutzereigenschaftenID`),
  KEY `FK963DAE0F8896477B` (`BenutzerID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `benutzergruppen` (
  `BenutzergruppenID` int(11) NOT NULL AUTO_INCREMENT,
  `titel` varchar(255) DEFAULT NULL,
  `berechtigung` int(11) DEFAULT NULL,
  PRIMARY KEY (`BenutzergruppenID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `benutzergruppenmitgliedschaft` (
  `BenutzerGruppenID` int(11) NOT NULL,
  `BenutzerID` int(11) NOT NULL,
  PRIMARY KEY (`BenutzerID`,`BenutzerGruppenID`),
  KEY `FK45CBE5781843242F` (`BenutzerGruppenID`),
  KEY `FK45CBE5788896477B` (`BenutzerID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `history` (
  `historyid` int(11) NOT NULL AUTO_INCREMENT,
  `numericvalue` double DEFAULT NULL,
  `stringvalue` varchar(255) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `processID` int(11) DEFAULT NULL,
  PRIMARY KEY (`historyid`),
  KEY `FK373FE4946640305C` (`processID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ldapgruppen` (
  `ldapgruppenID` int(11) NOT NULL AUTO_INCREMENT,
  `titel` varchar(255) DEFAULT NULL,
  `homeDirectory` varchar(255) DEFAULT NULL,
  `gidNumber` varchar(255) DEFAULT NULL,
  `userDN` varchar(255) DEFAULT NULL,
  `objectClasses` varchar(255) DEFAULT NULL,
  `sambaSID` varchar(255) DEFAULT NULL,
  `sn` varchar(255) DEFAULT NULL,
  `uid` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `displayName` varchar(255) DEFAULT NULL,
  `gecos` varchar(255) DEFAULT NULL,
  `loginShell` varchar(255) DEFAULT NULL,
  `sambaAcctFlags` varchar(255) DEFAULT NULL,
  `sambaLogonScript` varchar(255) DEFAULT NULL,
  `sambaPrimaryGroupSID` varchar(255) DEFAULT NULL,
  `sambaPwdMustChange` varchar(255) DEFAULT NULL,
  `sambaPasswordHistory` varchar(255) DEFAULT NULL,
  `sambaLogonHours` varchar(255) DEFAULT NULL,
  `sambaKickoffTime` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ldapgruppenID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `metadatenkonfigurationen` (
  `MetadatenKonfigurationID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `Datei` varchar(255) DEFAULT NULL,
  `orderMetadataByRuleset` bit(1) DEFAULT NULL,
  PRIMARY KEY (`MetadatenKonfigurationID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `projectfilegroups` (
  `ProjectFileGroupID` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `mimetype` varchar(255) DEFAULT NULL,
  `suffix` varchar(255) DEFAULT NULL,
  `ProjekteID` int(11) DEFAULT NULL,
  PRIMARY KEY (`ProjectFileGroupID`),
  KEY `FK51AAC229327F143A` (`ProjekteID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `projektbenutzer` (
  `BenutzerID` int(11) NOT NULL,
  `ProjekteID` int(11) NOT NULL,
  PRIMARY KEY (`BenutzerID`,`ProjekteID`),
  KEY `FKEC749D0E327F143A` (`ProjekteID`),
  KEY `FKEC749D0E8896477B` (`BenutzerID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `projekte` (
  `ProjekteID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `useDmsImport` bit(1) DEFAULT NULL,
  `dmsImportTimeOut` int(11) DEFAULT NULL,
  `dmsImportRootPath` varchar(255) DEFAULT NULL,
  `dmsImportImagesPath` varchar(255) DEFAULT NULL,
  `dmsImportSuccessPath` varchar(255) DEFAULT NULL,
  `dmsImportErrorPath` varchar(255) DEFAULT NULL,
  `dmsImportCreateProcessFolder` bit(1) DEFAULT NULL,
  `fileFormatInternal` varchar(255) DEFAULT NULL,
  `fileFormatDmsExport` varchar(255) DEFAULT NULL,
  `metsRightsOwner` varchar(255) DEFAULT NULL,
  `metsRightsOwnerLogo` varchar(255) DEFAULT NULL,
  `metsRightsOwnerSite` varchar(255) DEFAULT NULL,
  `metsRightsOwnerMail` varchar(255) DEFAULT NULL,
  `metsDigiprovReference` varchar(255) DEFAULT NULL,
  `metsDigiprovPresentation` varchar(255) DEFAULT NULL,
  `metsDigiprovReferenceAnchor` varchar(255) DEFAULT NULL,
  `metsDigiprovPresentationAnchor` varchar(255) DEFAULT NULL,
  `metsPointerPath` varchar(255) DEFAULT NULL,
  `metsPointerPathAnchor` varchar(255) DEFAULT NULL,
  `metsPurl` varchar(255) DEFAULT NULL,
  `metsContentIDs` varchar(255) DEFAULT NULL,
  `startDate` datetime DEFAULT NULL,
  `endDate` datetime DEFAULT NULL,
  `numberOfPages` int(11) DEFAULT NULL,
  `numberOfVolumes` int(11) DEFAULT NULL,
  PRIMARY KEY (`ProjekteID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `prozesse` (
  `ProzesseID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `ausgabename` varchar(255) DEFAULT NULL,
  `IstTemplate` bit(1) DEFAULT NULL,
  `swappedOut` bit(1) DEFAULT NULL,
  `inAuswahllisteAnzeigen` bit(1) DEFAULT NULL,
  `sortHelperStatus` varchar(255) DEFAULT NULL,
  `sortHelperImages` int(11) DEFAULT NULL,
  `sortHelperArticles` int(11) DEFAULT NULL,
  `sortHelperDocstructs` int(11) DEFAULT NULL,
  `sortHelperMetadata` int(11) DEFAULT NULL,
  `erstellungsdatum` datetime DEFAULT NULL,
  `wikifield` varchar(255) DEFAULT NULL,
  `ProjekteID` int(11) DEFAULT NULL,
  `MetadatenKonfigurationID` int(11) DEFAULT NULL,
  PRIMARY KEY (`ProzesseID`),
  KEY `FKC55ACC6D327F143A` (`ProjekteID`),
  KEY `FKC55ACC6DACAFE8C7` (`MetadatenKonfigurationID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `prozesseeigenschaften` (
  `prozesseeigenschaftenID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `Wert` varchar(255) DEFAULT NULL,
  `IstObligatorisch` bit(1) DEFAULT NULL,
  `DatentypenID` int(11) DEFAULT NULL,
  `Auswahl` varchar(255) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `container` int(11) DEFAULT NULL,
  `prozesseID` int(11) DEFAULT NULL,
  PRIMARY KEY (`prozesseeigenschaftenID`),
  KEY `FK3B22499F815A56DA` (`prozesseID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schritte` (
  `SchritteID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `Prioritaet` int(11) DEFAULT NULL,
  `Reihenfolge` int(11) DEFAULT NULL,
  `Bearbeitungsstatus` int(11) DEFAULT NULL,
  `edittype` int(11) DEFAULT NULL,
  `BearbeitungsZeitpunkt` datetime DEFAULT NULL,
  `BearbeitungsBeginn` datetime DEFAULT NULL,
  `BearbeitungsEnde` datetime DEFAULT NULL,
  `homeverzeichnisNutzen` smallint(6) DEFAULT NULL,
  `typMetadaten` bit(1) DEFAULT NULL,
  `typAutomatisch` bit(1) DEFAULT NULL,
  `typImportFileUpload` bit(1) DEFAULT NULL,
  `typExportRus` bit(1) DEFAULT NULL,
  `typImagesLesen` bit(1) DEFAULT NULL,
  `typImagesSchreiben` bit(1) DEFAULT NULL,
  `typExportDMS` bit(1) DEFAULT NULL,
  `typBeimAnnehmenModul` bit(1) DEFAULT NULL,
  `typBeimAnnehmenAbschliessen` bit(1) DEFAULT NULL,
  `typBeimAnnehmenModulUndAbschliessen` bit(1) DEFAULT NULL,
  `typScriptStep` bit(1) DEFAULT NULL,
  `scriptName1` varchar(255) DEFAULT NULL,
  `typAutomatischScriptpfad` varchar(255) DEFAULT NULL,
  `scriptName2` varchar(255) DEFAULT NULL,
  `typAutomatischScriptpfad2` varchar(255) DEFAULT NULL,
  `scriptName3` varchar(255) DEFAULT NULL,
  `typAutomatischScriptpfad3` varchar(255) DEFAULT NULL,
  `scriptName4` varchar(255) DEFAULT NULL,
  `typAutomatischScriptpfad4` varchar(255) DEFAULT NULL,
  `scriptName5` varchar(255) DEFAULT NULL,
  `typAutomatischScriptpfad5` varchar(255) DEFAULT NULL,
  `typBeimAbschliessenVerifizieren` bit(1) DEFAULT NULL,
  `typModulName` varchar(255) DEFAULT NULL,
  `BearbeitungsBenutzerID` int(11) DEFAULT NULL,
  `ProzesseID` int(11) DEFAULT NULL,
  PRIMARY KEY (`SchritteID`),
  KEY `FKD7200736815A56DA` (`ProzesseID`),
  KEY `FKD72007365B4F6962` (`BearbeitungsBenutzerID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schritteberechtigtebenutzer` (
  `BenutzerID` int(11) NOT NULL,
  `schritteID` int(11) NOT NULL,
  PRIMARY KEY (`schritteID`,`BenutzerID`),
  KEY `FK4BB889CFBB6FCB7A` (`schritteID`),
  KEY `FK4BB889CF8896477B` (`BenutzerID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schritteberechtigtegruppen` (
  `BenutzerGruppenID` int(11) NOT NULL,
  `schritteID` int(11) NOT NULL,
  PRIMARY KEY (`schritteID`,`BenutzerGruppenID`),
  KEY `FKA5A0CC81BB6FCB7A` (`schritteID`),
  KEY `FKA5A0CC811843242F` (`BenutzerGruppenID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schritteeigenschaften` (
  `schritteeigenschaftenID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `Wert` longtext,
  `IstObligatorisch` bit(1) DEFAULT NULL,
  `DatentypenID` int(11) DEFAULT NULL,
  `Auswahl` varchar(255) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `container` int(11) DEFAULT NULL,
  `schritteID` int(11) DEFAULT NULL,
  PRIMARY KEY (`schritteeigenschaftenID`),
  KEY `FK884E9D76BB6FCB7A` (`schritteID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vorlagen` (
  `VorlagenID` int(11) NOT NULL AUTO_INCREMENT,
  `Herkunft` varchar(255) DEFAULT NULL,
  `ProzesseID` int(11) DEFAULT NULL,
  PRIMARY KEY (`VorlagenID`),
  KEY `FK9A466882815A56DA` (`ProzesseID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vorlageneigenschaften` (
  `vorlageneigenschaftenID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `Wert` longtext,
  `IstObligatorisch` bit(1) DEFAULT NULL,
  `DatentypenID` int(11) DEFAULT NULL,
  `Auswahl` varchar(255) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `container` int(11) DEFAULT NULL,
  `vorlagenID` int(11) DEFAULT NULL,
  PRIMARY KEY (`vorlageneigenschaftenID`),
  KEY `FKAA25B7AA239F423` (`vorlagenID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `werkstuecke` (
  `WerkstueckeID` int(11) NOT NULL AUTO_INCREMENT,
  `ProzesseID` int(11) DEFAULT NULL,
  PRIMARY KEY (`WerkstueckeID`),
  KEY `FK98DED745815A56DA` (`ProzesseID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `werkstueckeeigenschaften` (
  `werkstueckeeigenschaftenID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `Wert` longtext,
  `IstObligatorisch` bit(1) DEFAULT NULL,
  `DatentypenID` int(11) DEFAULT NULL,
  `Auswahl` varchar(255) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `container` int(11) DEFAULT NULL,
  `werkstueckeID` int(11) DEFAULT NULL,
  PRIMARY KEY (`werkstueckeeigenschaftenID`),
  KEY `FK7B209DC7C9900466` (`werkstueckeID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;