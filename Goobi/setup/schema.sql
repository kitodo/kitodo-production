--
-- (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
--
-- This file is part of the Kitodo project.
--
-- It is licensed under GNU General Public License version 3 or later.
--
-- For the full copyright and license information, please read the
-- GPL3-License.txt file that was distributed with this source code.
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `benutzer` (
  `BenutzerID` int(11) NOT NULL AUTO_INCREMENT,
  `Vorname` varchar(255) DEFAULT NULL,
  `Nachname` varchar(255) DEFAULT NULL,
  `login` varchar(255) DEFAULT NULL,
  `ldaplogin` varchar(255) DEFAULT NULL,
  `passwort` varchar(255) DEFAULT NULL,
  `IstAktiv` tinyint(1) DEFAULT NULL,
  `isVisible` varchar(255) DEFAULT NULL,
  `Standort` varchar(255) DEFAULT NULL,
  `metadatensprache` varchar(255) DEFAULT NULL,
  `css` varchar(255) DEFAULT NULL,
  `mitMassendownload` tinyint(1) DEFAULT NULL,
  `confVorgangsdatumAnzeigen` tinyint(1) DEFAULT NULL,
  `Tabellengroesse` int(11) DEFAULT NULL,
  `sessiontimeout` int(11) DEFAULT NULL,
  `ldapgruppenID` int(11) DEFAULT NULL,
  PRIMARY KEY (`BenutzerID`),
  KEY `FK_LdapgruppenID` (`ldapgruppenID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `benutzereigenschaften` (
  `benutzereigenschaftenID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `Wert` varchar(255) DEFAULT NULL,
  `IstObligatorisch` tinyint(1) DEFAULT NULL,
  `DatentypenID` int(11) DEFAULT NULL,
  `Auswahl` varchar(255) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `BenutzerID` int(11) DEFAULT NULL,
  PRIMARY KEY (`benutzereigenschaftenID`),
  KEY `FK_BenutzerID` (`BenutzerID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `benutzergruppen` (
  `BenutzergruppenID` int(11) NOT NULL AUTO_INCREMENT,
  `titel` varchar(255) DEFAULT NULL,
  `berechtigung` int(11) DEFAULT NULL,
  PRIMARY KEY (`BenutzergruppenID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `benutzergruppenmitgliedschaft` (
  `BenutzerGruppenID` int(11) NOT NULL,
  `BenutzerID` int(11) NOT NULL,
  PRIMARY KEY (`BenutzerID`,`BenutzerGruppenID`),
  KEY `FK_BenutzerGruppenID` (`BenutzerGruppenID`),
  KEY `FK_BenutzerID` (`BenutzerID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dockets` (
  `docketID` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `file` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`docketID`)
) DEFAULT CHARSET=latin1;
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
  KEY `FK_ProzesseID` (`processID`)
) DEFAULT CHARSET=latin1;
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
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `metadatenkonfigurationen` (
  `MetadatenKonfigurationID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `Datei` varchar(255) DEFAULT NULL,
  `orderMetadataByRuleset` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`MetadatenKonfigurationID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `projectfilegroups` (
  `ProjectFileGroupID` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `mimetype` varchar(255) DEFAULT NULL,
  `suffix` varchar(255) DEFAULT NULL,
  `folder` varchar(255) DEFAULT NULL,
 `previewImage` tinyint(1) DEFAULT NULL,
  `ProjekteID` int(11) DEFAULT NULL,
  PRIMARY KEY (`ProjectFileGroupID`),
  KEY `FK_ProjekteID` (`ProjekteID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `projektbenutzer` (
  `BenutzerID` int(11) NOT NULL,
  `ProjekteID` int(11) NOT NULL,
  PRIMARY KEY (`BenutzerID`,`ProjekteID`),
  KEY `FK_ProjekteID` (`ProjekteID`),
  KEY `FK_BenutzerID` (`BenutzerID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `projekte` (
  `ProjekteID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `useDmsImport` tinyint(1) DEFAULT NULL,
  `dmsImportTimeOut` int(11) DEFAULT NULL,
  `dmsImportRootPath` varchar(255) DEFAULT NULL,
  `dmsImportImagesPath` varchar(255) DEFAULT NULL,
  `dmsImportSuccessPath` varchar(255) DEFAULT NULL,
  `dmsImportErrorPath` varchar(255) DEFAULT NULL,
  `dmsImportCreateProcessFolder` tinyint(1) DEFAULT NULL,
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
  `projectIsArchived` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`ProjekteID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `prozesse` (
  `ProzesseID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `ausgabename` varchar(255) DEFAULT NULL,
  `IstTemplate` tinyint(1) DEFAULT NULL,
  `swappedOut` tinyint(1) DEFAULT NULL,
  `inAuswahllisteAnzeigen` tinyint(1) DEFAULT NULL,
  `sortHelperStatus` varchar(255) DEFAULT NULL,
  `sortHelperImages` int(11) DEFAULT NULL,
  `sortHelperArticles` int(11) DEFAULT NULL,
  `sortHelperDocstructs` int(11) DEFAULT NULL,
  `sortHelperMetadata` int(11) DEFAULT NULL,
  `erstellungsdatum` datetime DEFAULT NULL,
  `wikifield` longtext,
  `ProjekteID` int(11) DEFAULT NULL,
  `MetadatenKonfigurationID` int(11) DEFAULT NULL,
  `docketID` int(11) DEFAULT NULL,
  PRIMARY KEY (`ProzesseID`),
  KEY `FK_ProjekteID` (`ProjekteID`),
  KEY `FK_MetadatenKonfigurationID` (`MetadatenKonfigurationID`),
  KEY `FK_DocketID` (`docketID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `prozesseeigenschaften` (
  `prozesseeigenschaftenID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `Wert` longtext,
  `IstObligatorisch` tinyint(1) DEFAULT NULL,
  `DatentypenID` int(11) DEFAULT NULL,
  `Auswahl` varchar(255) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `container` int(11) DEFAULT NULL,
  `prozesseID` int(11) DEFAULT NULL,
  PRIMARY KEY (`prozesseeigenschaftenID`),
  KEY `FK_ProzesseID` (`prozesseID`)
) DEFAULT CHARSET=latin1;
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
  `typMetadaten` tinyint(1) DEFAULT NULL,
  `typAutomatisch` tinyint(1) DEFAULT NULL,
  `typImportFileUpload` tinyint(1) DEFAULT NULL,
  `typExportRus` tinyint(1) DEFAULT NULL,
  `typImagesLesen` tinyint(1) DEFAULT NULL,
  `typImagesSchreiben` tinyint(1) DEFAULT NULL,
  `typExportDMS` tinyint(1) DEFAULT NULL,
  `typBeimAnnehmenModul` tinyint(1) DEFAULT NULL,
  `typBeimAnnehmenAbschliessen` tinyint(1) DEFAULT NULL,
  `typBeimAnnehmenModulUndAbschliessen` tinyint(1) DEFAULT NULL,
  `typScriptStep` tinyint(1) DEFAULT NULL,
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
  `typBeimAbschliessenVerifizieren` tinyint(1) DEFAULT NULL,
  `typModulName` varchar(255) DEFAULT NULL,
  `batchStep` tinyint(1) DEFAULT NULL,
  `stepPlugin` varchar(255) DEFAULT NULL,
  `validationPlugin` varchar(255) DEFAULT NULL,
  `BearbeitungsBenutzerID` int(11) DEFAULT NULL,
  `ProzesseID` int(11) DEFAULT NULL,
  PRIMARY KEY (`SchritteID`),
  KEY `FK_ProzesseID` (`ProzesseID`),
  KEY `FK_BearbeitungsBenutzerID` (`BearbeitungsBenutzerID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schritteberechtigtebenutzer` (
  `BenutzerID` int(11) NOT NULL,
  `schritteID` int(11) NOT NULL,
  PRIMARY KEY (`schritteID`,`BenutzerID`),
  KEY `FK_SchritteID` (`schritteID`),
  KEY `FK_BenutzerID` (`BenutzerID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schritteberechtigtegruppen` (
  `BenutzerGruppenID` int(11) NOT NULL,
  `schritteID` int(11) NOT NULL,
  PRIMARY KEY (`schritteID`,`BenutzerGruppenID`),
  KEY `FK_SchritteID` (`schritteID`),
  KEY `FK_BenutzerGruppenID` (`BenutzerGruppenID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schritteeigenschaften` (
  `schritteeigenschaftenID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `Wert` longtext,
  `IstObligatorisch` tinyint(1) DEFAULT NULL,
  `DatentypenID` int(11) DEFAULT NULL,
  `Auswahl` varchar(255) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `container` int(11) DEFAULT NULL,
  `schritteID` int(11) DEFAULT NULL,
  PRIMARY KEY (`schritteeigenschaftenID`),
  KEY `FK_SchritteID` (`schritteID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vorlagen` (
  `VorlagenID` int(11) NOT NULL AUTO_INCREMENT,
  `Herkunft` varchar(255) DEFAULT NULL,
  `ProzesseID` int(11) DEFAULT NULL,
  PRIMARY KEY (`VorlagenID`),
  KEY `FK_ProzesseID` (`ProzesseID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vorlageneigenschaften` (
  `vorlageneigenschaftenID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `Wert` longtext,
  `IstObligatorisch` tinyint(1) DEFAULT NULL,
  `DatentypenID` int(11) DEFAULT NULL,
  `Auswahl` varchar(255) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `container` int(11) DEFAULT NULL,
  `vorlagenID` int(11) DEFAULT NULL,
  PRIMARY KEY (`vorlageneigenschaftenID`),
  KEY `FK_VorlagenID` (`vorlagenID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `werkstuecke` (
  `WerkstueckeID` int(11) NOT NULL AUTO_INCREMENT,
  `ProzesseID` int(11) DEFAULT NULL,
  PRIMARY KEY (`WerkstueckeID`),
  KEY `FK_ProzesseID` (`ProzesseID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `werkstueckeeigenschaften` (
  `werkstueckeeigenschaftenID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `Wert` longtext,
  `IstObligatorisch` tinyint(1) DEFAULT NULL,
  `DatentypenID` int(11) DEFAULT NULL,
  `Auswahl` varchar(255) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `container` int(11) DEFAULT NULL,
  `werkstueckeID` int(11) DEFAULT NULL,
  PRIMARY KEY (`werkstueckeeigenschaftenID`),
  KEY `FK_WerkstueckeID` (`werkstueckeID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `batchesprozesse` (
  `ProzesseID` int(11) NOT NULL,
  `BatchID` int(11) NOT NULL,
  PRIMARY KEY (`ProzesseID`,`BatchID`),
  KEY `FK_ProzesseID` (`ProzesseID`),
  KEY `FK_BatchID` (`BatchID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `batches` (
  `BatchID` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `type` varchar(9) DEFAULT NULL,
  PRIMARY KEY (`BatchID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
