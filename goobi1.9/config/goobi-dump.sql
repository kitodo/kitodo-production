-- MySQL dump 10.13  Distrib 5.1.41, for debian-linux-gnu (i486)
--
-- Host: localhost    Database: goobi
-- ------------------------------------------------------
-- Server version	5.1.41-3ubuntu12.6

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `goobi`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `goobi` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `goobi`;

--
-- Table structure for table `benutzer`
--

DROP TABLE IF EXISTS `benutzer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `benutzer` (
  `BenutzerID` int(11) NOT NULL AUTO_INCREMENT,
  `Vorname` varchar(255) DEFAULT NULL,
  `Nachname` varchar(255) DEFAULT NULL,
  `login` varchar(255) DEFAULT NULL,
  `passwort` varchar(255) DEFAULT NULL,
  `IstAktiv` bit(1) DEFAULT NULL,
  `Standort` varchar(255) DEFAULT NULL,
  `metadatensprache` varchar(255) DEFAULT NULL,
  `css` varchar(255) DEFAULT NULL,
  `mitMassendownload` bit(1) DEFAULT NULL,
  `confVorgangsdatumAnzeigen` bit(1) DEFAULT NULL,
  `Tabellengroesse` int(11) DEFAULT NULL,
  `sessiontimeout` int(11) DEFAULT NULL,
  `ldapgruppenID` int(11) DEFAULT NULL,
  `isVisible` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`BenutzerID`),
  KEY `FK6564F1FD78EC6B0F` (`ldapgruppenID`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `benutzer`
--

LOCK TABLES `benutzer` WRITE;
/*!40000 ALTER TABLE `benutzer` DISABLE KEYS */;
INSERT INTO `benutzer` VALUES (1,'Detlev','Engel','testadmin','OvEJ00yyYZQ=','','Göttingen','de',NULL,'\0','\0',10,NULL,2,NULL),(2,'Wolfgang','Fürstlich','testscanning','OvEJ00yyYZQ=','','Göttingen','de',NULL,'\0','\0',10,NULL,2,NULL),(3,'Veronika','Lichthaus','testqc','OvEJ00yyYZQ=','','Göttingen','de',NULL,'\0','\0',10,NULL,2,NULL),(4,'André','Hermelich','testimaging','OvEJ00yyYZQ=','','Göttingen','de',NULL,'\0','\0',10,NULL,2,NULL),(5,'Christine','Gross','testmetadata','OvEJ00yyYZQ=','','Göttingen','de',NULL,'\0','\0',10,NULL,2,NULL),(6,'Cecilia','von Darst','testprojectmanagement','OvEJ00yyYZQ=','','Göttingen','de',NULL,'\0','\0',10,NULL,2,NULL);
/*!40000 ALTER TABLE `benutzer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `benutzereigenschaften`
--

DROP TABLE IF EXISTS `benutzereigenschaften`;
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
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `benutzereigenschaften`
--

LOCK TABLES `benutzereigenschaften` WRITE;
/*!40000 ALTER TABLE `benutzereigenschaften` DISABLE KEYS */;
/*!40000 ALTER TABLE `benutzereigenschaften` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `benutzergruppen`
--

DROP TABLE IF EXISTS `benutzergruppen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `benutzergruppen` (
  `BenutzergruppenID` int(11) NOT NULL AUTO_INCREMENT,
  `titel` varchar(255) DEFAULT NULL,
  `berechtigung` int(11) DEFAULT NULL,
  PRIMARY KEY (`BenutzergruppenID`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `benutzergruppen`
--

LOCK TABLES `benutzergruppen` WRITE;
/*!40000 ALTER TABLE `benutzergruppen` DISABLE KEYS */;
INSERT INTO `benutzergruppen` VALUES (1,'Administration',1),(2,'Scanpersonal',3),(3,'Qualitätskontrolle',3),(4,'Imagenachbearbeiter',3),(5,'Metadatatengruppe',3),(6,'Projektmanagement',2);
/*!40000 ALTER TABLE `benutzergruppen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `benutzergruppenmitgliedschaft`
--

DROP TABLE IF EXISTS `benutzergruppenmitgliedschaft`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `benutzergruppenmitgliedschaft` (
  `BenutzerGruppenID` int(11) NOT NULL,
  `BenutzerID` int(11) NOT NULL,
  PRIMARY KEY (`BenutzerID`,`BenutzerGruppenID`),
  KEY `FK45CBE5781843242F` (`BenutzerGruppenID`),
  KEY `FK45CBE5788896477B` (`BenutzerID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `benutzergruppenmitgliedschaft`
--

LOCK TABLES `benutzergruppenmitgliedschaft` WRITE;
/*!40000 ALTER TABLE `benutzergruppenmitgliedschaft` DISABLE KEYS */;
INSERT INTO `benutzergruppenmitgliedschaft` VALUES (1,1),(2,2),(3,3),(4,4),(5,5),(6,6);
/*!40000 ALTER TABLE `benutzergruppenmitgliedschaft` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `history`
--

DROP TABLE IF EXISTS `history`;
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
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `history`
--

LOCK TABLES `history` WRITE;
/*!40000 ALTER TABLE `history` DISABLE KEYS */;
/*!40000 ALTER TABLE `history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ldapgruppen`
--

DROP TABLE IF EXISTS `ldapgruppen`;
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
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ldapgruppen`
--

LOCK TABLES `ldapgruppen` WRITE;
/*!40000 ALTER TABLE `ldapgruppen` DISABLE KEYS */;
INSERT INTO `ldapgruppen` VALUES (2,'test',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `ldapgruppen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `metadatenkonfigurationen`
--

DROP TABLE IF EXISTS `metadatenkonfigurationen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `metadatenkonfigurationen` (
  `MetadatenKonfigurationID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `Datei` varchar(255) DEFAULT NULL,
  `orderMetadataByRuleset` bit(1) DEFAULT NULL,
  PRIMARY KEY (`MetadatenKonfigurationID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `metadatenkonfigurationen`
--

LOCK TABLES `metadatenkonfigurationen` WRITE;
/*!40000 ALTER TABLE `metadatenkonfigurationen` DISABLE KEYS */;
/*!40000 ALTER TABLE `metadatenkonfigurationen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `projectfilegroups`
--

DROP TABLE IF EXISTS `projectfilegroups`;
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
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `projectfilegroups`
--

LOCK TABLES `projectfilegroups` WRITE;
/*!40000 ALTER TABLE `projectfilegroups` DISABLE KEYS */;
/*!40000 ALTER TABLE `projectfilegroups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `projektbenutzer`
--

DROP TABLE IF EXISTS `projektbenutzer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `projektbenutzer` (
  `BenutzerID` int(11) NOT NULL,
  `ProjekteID` int(11) NOT NULL,
  PRIMARY KEY (`BenutzerID`,`ProjekteID`),
  KEY `FKEC749D0E327F143A` (`ProjekteID`),
  KEY `FKEC749D0E8896477B` (`BenutzerID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `projektbenutzer`
--

LOCK TABLES `projektbenutzer` WRITE;
/*!40000 ALTER TABLE `projektbenutzer` DISABLE KEYS */;
/*!40000 ALTER TABLE `projektbenutzer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `projekte`
--

DROP TABLE IF EXISTS `projekte`;
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
  PRIMARY KEY (`ProjekteID`),
  KEY `FKC8539A94327F143A` (`ProjekteID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `projekte`
--

LOCK TABLES `projekte` WRITE;
/*!40000 ALTER TABLE `projekte` DISABLE KEYS */;
/*!40000 ALTER TABLE `projekte` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `prozesse`
--

DROP TABLE IF EXISTS `prozesse`;
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
  `erstellungsdatum` datetime DEFAULT NULL,
  `ProjekteID` int(11) DEFAULT NULL,
  `MetadatenKonfigurationID` int(11) DEFAULT NULL,
  `sortHelperDocstructs` int(11) DEFAULT NULL,
  `sortHelperMetadata` int(11) DEFAULT NULL,
  `wikifield` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ProzesseID`),
  KEY `FKC55ACC6D327F143A` (`ProjekteID`),
  KEY `FKC55ACC6DACAFE8C7` (`MetadatenKonfigurationID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prozesse`
--

LOCK TABLES `prozesse` WRITE;
/*!40000 ALTER TABLE `prozesse` DISABLE KEYS */;
/*!40000 ALTER TABLE `prozesse` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `prozesseeigenschaften`
--

DROP TABLE IF EXISTS `prozesseeigenschaften`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `prozesseeigenschaften` (
  `prozesseeigenschaftenID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `WERT` text,
  `IstObligatorisch` bit(1) DEFAULT NULL,
  `DatentypenID` int(11) DEFAULT NULL,
  `Auswahl` varchar(255) DEFAULT NULL,
  `prozesseID` int(11) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `container` int(11) DEFAULT NULL,
  PRIMARY KEY (`prozesseeigenschaftenID`),
  KEY `FK3B22499F815A56DA` (`prozesseID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prozesseeigenschaften`
--

LOCK TABLES `prozesseeigenschaften` WRITE;
/*!40000 ALTER TABLE `prozesseeigenschaften` DISABLE KEYS */;
/*!40000 ALTER TABLE `prozesseeigenschaften` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schritte`
--

DROP TABLE IF EXISTS `schritte`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schritte` (
  `SchritteID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `Prioritaet` int(11) DEFAULT NULL,
  `Reihenfolge` int(11) DEFAULT NULL,
  `Bearbeitungsstatus` int(11) DEFAULT NULL,
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
  `typAutomatischScriptpfad` varchar(255) DEFAULT NULL,
  `typBeimAbschliessenVerifizieren` bit(1) DEFAULT NULL,
  `typModulName` varchar(255) DEFAULT NULL,
  `BearbeitungsBenutzerID` int(11) DEFAULT NULL,
  `ProzesseID` int(11) DEFAULT NULL,
  `edittype` int(11) DEFAULT NULL,
  `typScriptStep` bit(1) DEFAULT NULL,
  `scriptName1` varchar(255) DEFAULT NULL,
  `scriptName2` varchar(255) DEFAULT NULL,
  `typAutomatischScriptpfad2` varchar(255) DEFAULT NULL,
  `scriptName3` varchar(255) DEFAULT NULL,
  `typAutomatischScriptpfad3` varchar(255) DEFAULT NULL,
  `scriptName4` varchar(255) DEFAULT NULL,
  `typAutomatischScriptpfad4` varchar(255) DEFAULT NULL,
  `scriptName5` varchar(255) DEFAULT NULL,
  `typAutomatischScriptpfad5` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`SchritteID`),
  KEY `FKD7200736815A56DA` (`ProzesseID`),
  KEY `FKD72007365B4F6962` (`BearbeitungsBenutzerID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schritte`
--

LOCK TABLES `schritte` WRITE;
/*!40000 ALTER TABLE `schritte` DISABLE KEYS */;
/*!40000 ALTER TABLE `schritte` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schritteberechtigtebenutzer`
--

DROP TABLE IF EXISTS `schritteberechtigtebenutzer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schritteberechtigtebenutzer` (
  `BenutzerID` int(11) NOT NULL,
  `schritteID` int(11) NOT NULL,
  PRIMARY KEY (`schritteID`,`BenutzerID`),
  KEY `FK4BB889CF8896477B` (`BenutzerID`),
  KEY `FK4BB889CFBB6FCB7A` (`schritteID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schritteberechtigtebenutzer`
--

LOCK TABLES `schritteberechtigtebenutzer` WRITE;
/*!40000 ALTER TABLE `schritteberechtigtebenutzer` DISABLE KEYS */;
/*!40000 ALTER TABLE `schritteberechtigtebenutzer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schritteberechtigtegruppen`
--

DROP TABLE IF EXISTS `schritteberechtigtegruppen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schritteberechtigtegruppen` (
  `BenutzerGruppenID` int(11) NOT NULL,
  `schritteID` int(11) NOT NULL,
  PRIMARY KEY (`schritteID`,`BenutzerGruppenID`),
  KEY `FKA5A0CC811843242F` (`BenutzerGruppenID`),
  KEY `FKA5A0CC81BB6FCB7A` (`schritteID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schritteberechtigtegruppen`
--

LOCK TABLES `schritteberechtigtegruppen` WRITE;
/*!40000 ALTER TABLE `schritteberechtigtegruppen` DISABLE KEYS */;
/*!40000 ALTER TABLE `schritteberechtigtegruppen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schritteeigenschaften`
--

DROP TABLE IF EXISTS `schritteeigenschaften`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schritteeigenschaften` (
  `schritteeigenschaftenID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `WERT` text,
  `IstObligatorisch` bit(1) DEFAULT NULL,
  `DatentypenID` int(11) DEFAULT NULL,
  `Auswahl` varchar(255) DEFAULT NULL,
  `schritteID` int(11) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `container` int(11) DEFAULT NULL,
  PRIMARY KEY (`schritteeigenschaftenID`),
  KEY `FK884E9D76BB6FCB7A` (`schritteID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schritteeigenschaften`
--

LOCK TABLES `schritteeigenschaften` WRITE;
/*!40000 ALTER TABLE `schritteeigenschaften` DISABLE KEYS */;
/*!40000 ALTER TABLE `schritteeigenschaften` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `urn_table`
--

DROP TABLE IF EXISTS `urn_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `urn_table` (
  `urn_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `werk_id` varchar(20) DEFAULT NULL,
  `struktur_typ` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`urn_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `urn_table`
--

LOCK TABLES `urn_table` WRITE;
/*!40000 ALTER TABLE `urn_table` DISABLE KEYS */;
/*!40000 ALTER TABLE `urn_table` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vorlagen`
--

DROP TABLE IF EXISTS `vorlagen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vorlagen` (
  `VorlagenID` int(11) NOT NULL AUTO_INCREMENT,
  `Herkunft` varchar(255) DEFAULT NULL,
  `ProzesseID` int(11) DEFAULT NULL,
  PRIMARY KEY (`VorlagenID`),
  KEY `FK9A466882815A56DA` (`ProzesseID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vorlagen`
--

LOCK TABLES `vorlagen` WRITE;
/*!40000 ALTER TABLE `vorlagen` DISABLE KEYS */;
/*!40000 ALTER TABLE `vorlagen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vorlageneigenschaften`
--

DROP TABLE IF EXISTS `vorlageneigenschaften`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vorlageneigenschaften` (
  `vorlageneigenschaftenID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `WERT` text,
  `IstObligatorisch` bit(1) DEFAULT NULL,
  `DatentypenID` int(11) DEFAULT NULL,
  `Auswahl` varchar(255) DEFAULT NULL,
  `vorlagenID` int(11) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `container` int(11) DEFAULT NULL,
  PRIMARY KEY (`vorlageneigenschaftenID`),
  KEY `FKAA25B7AA239F423` (`vorlagenID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vorlageneigenschaften`
--

LOCK TABLES `vorlageneigenschaften` WRITE;
/*!40000 ALTER TABLE `vorlageneigenschaften` DISABLE KEYS */;
/*!40000 ALTER TABLE `vorlageneigenschaften` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `werkstuecke`
--

DROP TABLE IF EXISTS `werkstuecke`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `werkstuecke` (
  `WerkstueckeID` int(11) NOT NULL AUTO_INCREMENT,
  `ProzesseID` int(11) DEFAULT NULL,
  PRIMARY KEY (`WerkstueckeID`),
  KEY `FK98DED745815A56DA` (`ProzesseID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `werkstuecke`
--

LOCK TABLES `werkstuecke` WRITE;
/*!40000 ALTER TABLE `werkstuecke` DISABLE KEYS */;
/*!40000 ALTER TABLE `werkstuecke` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `werkstueckeeigenschaften`
--

DROP TABLE IF EXISTS `werkstueckeeigenschaften`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `werkstueckeeigenschaften` (
  `werkstueckeeigenschaftenID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `WERT` text,
  `IstObligatorisch` bit(1) DEFAULT NULL,
  `DatentypenID` int(11) DEFAULT NULL,
  `Auswahl` varchar(255) DEFAULT NULL,
  `werkstueckeID` int(11) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `container` int(11) DEFAULT NULL,
  PRIMARY KEY (`werkstueckeeigenschaftenID`),
  KEY `FK7B209DC7C9900466` (`werkstueckeID`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `werkstueckeeigenschaften`
--

LOCK TABLES `werkstueckeeigenschaften` WRITE;
/*!40000 ALTER TABLE `werkstueckeeigenschaften` DISABLE KEYS */;
/*!40000 ALTER TABLE `werkstueckeeigenschaften` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-09-16 13:39:38
