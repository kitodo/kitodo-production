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

--
-- Insert default data and test accounts
--

/*!40000 ALTER TABLE `benutzer` DISABLE KEYS */;
INSERT INTO `ldapgruppen` (
  `ldapgruppenID`, `titel`) VALUES 
  (2, 'test'); /* id 2 has to be inserted to match users (benutzer)*/
/*!40000 ALTER TABLE `benutzer` ENABLE KEYS */;

--
-- Password for test users is "test"
-- NOTICE: Disable those users in production environment!
--

/*!40000 ALTER TABLE `benutzer` DISABLE KEYS */;
INSERT INTO `benutzer` (`BenutzerID`,`Login`,`Passwort`,`Vorname`,`Nachname`,`IstAktiv`,`Standort`,`mitMassendownload`,`Tabellengroesse`,`metadatensprache`,`confVorgangsdatumAnzeigen`) VALUES
 (1,'testAdmin','OvEJ00yyYZQ=','test','Admin',1,'Göttingen',0,10,'de',0);
INSERT INTO `benutzer` (`BenutzerID`,`Login`,`Passwort`,`Vorname`,`Nachname`,`IstAktiv`,`Standort`,`mitMassendownload`,`Tabellengroesse`,`metadatensprache`,`confVorgangsdatumAnzeigen`) VALUES
 (2,'testScanning','OvEJ00yyYZQ=','test','Scanning',1,'Göttingen',0,10,'de',0);
INSERT INTO `benutzer` (`BenutzerID`,`Login`,`Passwort`,`Vorname`,`Nachname`,`IstAktiv`,`Standort`,`mitMassendownload`,`Tabellengroesse`,`metadatensprache`,`confVorgangsdatumAnzeigen`) VALUES
 (3,'testQC','OvEJ00yyYZQ=','test','QC',1,'Göttingen',0,10,'de',0);
INSERT INTO `benutzer` (`BenutzerID`,`Login`,`Passwort`,`Vorname`,`Nachname`,`IstAktiv`,`Standort`,`mitMassendownload`,`Tabellengroesse`,`metadatensprache`,`confVorgangsdatumAnzeigen`) VALUES
 (4,'testImaging','OvEJ00yyYZQ=','test','Imaging',1,'Göttingen',0,10,'de',0);
INSERT INTO `benutzer` (`BenutzerID`,`Login`,`Passwort`,`Vorname`,`Nachname`,`IstAktiv`,`Standort`,`mitMassendownload`,`Tabellengroesse`,`metadatensprache`,`confVorgangsdatumAnzeigen`) VALUES
 (5,'testMetaData','OvEJ00yyYZQ=','test','MetaData',1,'Göttingen',0,10,'de',0);
INSERT INTO `benutzer` (`BenutzerID`,`Login`,`Passwort`,`Vorname`,`Nachname`,`IstAktiv`,`Standort`,`mitMassendownload`,`Tabellengroesse`,`metadatensprache`,`confVorgangsdatumAnzeigen`) VALUES
 (6,'testProjectmanagement','OvEJ00yyYZQ=','test','Projectmanagement',1,'Göttingen',0,10,'de',0);
/*!40000 ALTER TABLE `benutzer` ENABLE KEYS */;

/*!40000 ALTER TABLE `benutzergruppen` DISABLE KEYS */;
INSERT INTO `benutzergruppen` (`BenutzerGruppenID`,`Titel`,`Berechtigung`) VALUES
 (1,'Administration',1);
INSERT INTO `benutzergruppen` (`BenutzerGruppenID`,`Titel`,`Berechtigung`) VALUES
 (2,'Scanning',3);
INSERT INTO `benutzergruppen` (`BenutzerGruppenID`,`Titel`,`Berechtigung`) VALUES
 (3,'QualityControl',3);
INSERT INTO `benutzergruppen` (`BenutzerGruppenID`,`Titel`,`Berechtigung`) VALUES
 (4,'Imaging',3);
INSERT INTO `benutzergruppen` (`BenutzerGruppenID`,`Titel`,`Berechtigung`) VALUES
 (5,'Metadata',3);
INSERT INTO `benutzergruppen` (`BenutzerGruppenID`,`Titel`,`Berechtigung`) VALUES
 (6,'Projectmanagement',2);
 /*!40000 ALTER TABLE `benutzergruppen` ENABLE KEYS */;

/*!40000 ALTER TABLE `benutzergruppenmitgliedschaft` DISABLE KEYS */;
INSERT INTO `benutzergruppenmitgliedschaft` (`BenutzerID`,`BenutzerGruppenID`) VALUES
 (1,1);
INSERT INTO `benutzergruppenmitgliedschaft` (`BenutzerID`,`BenutzerGruppenID`) VALUES
 (2,2);
INSERT INTO `benutzergruppenmitgliedschaft` (`BenutzerID`,`BenutzerGruppenID`) VALUES
 (3,3);
INSERT INTO `benutzergruppenmitgliedschaft` (`BenutzerID`,`BenutzerGruppenID`) VALUES
 (4,4);
INSERT INTO `benutzergruppenmitgliedschaft` (`BenutzerID`,`BenutzerGruppenID`) VALUES
 (5,5);
INSERT INTO `benutzergruppenmitgliedschaft` (`BenutzerID`,`BenutzerGruppenID`) VALUES
 (6,6);
/*!40000 ALTER TABLE `benutzergruppenmitgliedschaft` ENABLE KEYS */;

/*!40000 ALTER TABLE `metadatenkonfigurationen` DISABLE KEYS */;
INSERT INTO `metadatenkonfigurationen` (`MetadatenKonfigurationID`, `Titel`, `Datei`, `orderMetadataByRuleset`) VALUES
(1, 'Simple book', 'simple-book.xml', b'0'),
(2, 'SUBHH', 'subhh.xml', b'0'),
(3, 'DFG-Viewer', 'dfg-viewer.xml', b'0');
/*!40000 ALTER TABLE `metadatenkonfigurationen` ENABLE KEYS */;

/*!40000 ALTER TABLE `dockets` DISABLE KEYS */;
INSERT INTO `dockets` (`docketID`, `name`, `file`) VALUES
(1, 'default', 'docket.xsl');
/*!40000 ALTER TABLE `dockets` ENABLE KEYS */;

/*!40000 ALTER TABLE `projekte` DISABLE KEYS */;
INSERT INTO `projekte` (`ProjekteID`, `Titel`, `useDmsImport`, `dmsImportTimeOut`, `dmsImportRootPath`, `dmsImportImagesPath`, `dmsImportSuccessPath`, `dmsImportErrorPath`, `dmsImportCreateProcessFolder`, `fileFormatInternal`, `fileFormatDmsExport`, `metsRightsOwner`, `metsRightsOwnerLogo`, `metsRightsOwnerSite`, `metsDigiprovReference`, `metsDigiprovPresentation`, `metsDigiprovReferenceAnchor`, `metsDigiprovPresentationAnchor`, `metsPointerPath`, `metsPointerPathAnchor`, `metsPurl`, `metsContentIDs`, `metsRightsOwnerMail`, `startDate`, `endDate`, `numberOfPages`, `numberOfVolumes`, `projectIsArchived`) VALUES
(1, 'Example Project', b'1', 3600000, '/usr/local/kitodo/hotfolder/', '/usr/local/kitodo/hotfolder/', '/usr/local/kitodo/success/', '/usr/local/kitodo/error_mets/', b'0', 'Mets', 'Mets', 'Digital Library Kitodo', 'http://www.example.com/fileadmin/groups/kitodo/Logo/kitodo_logo_rgb.png', 'http://www.example.com', 'http://www.example.com/DB=1/PPN?PPN=$(meta.topstruct.CatalogIDDigital)', 'http://www.example.com/resolver?id=$(meta.topstruct.CatalogIDDigital)', 'http://www.example.com/DB=1/PPN?PPN=$(meta.topstruct.CatalogIDDigital)', 'http://www.example.com/resolver?id=$(meta.topstruct.CatalogIDDigital)', 'http://www.example.com/content/$(meta.CatalogIDDigital)/$(meta.topstruct.CatalogIDDigital).xml', 'http://www.example.com/content/$(meta.firstchild.CatalogIDDigital)/$(meta.topstruct.CatalogIDDigital).xml ', 'http://www.example.com/resolver?id=$(meta.CatalogIDDigital)', '', 'info@kitodo.org', '2016-01-01 00:00:00', '2019-12-31 00:00:00', 0, 0, b'0');
/*!40000 ALTER TABLE `projekte` ENABLE KEYS */;

/*!40000 ALTER TABLE `projektbenutzer` DISABLE KEYS */;
INSERT INTO `projektbenutzer` (`BenutzerID`, `ProjekteID`) VALUES
(1, 1),
(2, 1),
(3, 1),
(4, 1),
(5, 1),
(6, 1);
/*!40000 ALTER TABLE `projektbenutzer` ENABLE KEYS */;

/*!40000 ALTER TABLE `projectfilegroups` DISABLE KEYS */;
INSERT INTO `projectfilegroups` (`ProjectFileGroupID`, `name`, `path`, `mimetype`, `suffix`, `ProjekteID`, `folder`, `previewImage`) VALUES
(1, 'MAX', 'http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/max/', 'image/jpeg', 'jpg', 1, '', 0),
(2, 'DEFAULT', 'http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/default/', 'image/jpeg', 'jpg', 1, '', 0),
(3, 'THUMBS', 'http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/thumbs/', 'image/jpeg', 'jpg', 1, '', 0),
(4, 'FULLTEXT', 'http://www.example.com/content/$(meta.CatalogIDDigital)/ocr/alto/', 'application/alto+xml', 'xml', 1, '', 0),
(5, 'DOWNLOAD', 'http://www.example.com/content/$(meta.CatalogIDDigital)/pdf/', 'application/pdf', 'pdf', 1, '', 0);
/*!40000 ALTER TABLE `projectfilegroups` ENABLE KEYS */;

/*!40000 ALTER TABLE `prozesse` DISABLE KEYS */;
INSERT INTO `prozesse` (`ProzesseID`, `Titel`, `ausgabename`, `IstTemplate`, `swappedOut`, `inAuswahllisteAnzeigen`, `sortHelperStatus`, `sortHelperImages`, `sortHelperArticles`, `erstellungsdatum`, `ProjekteID`, `MetadatenKonfigurationID`, `sortHelperDocstructs`, `sortHelperMetadata`, `wikifield`, `docketID`) VALUES
(1, 'Example_Workflow', NULL, b'1', b'0', b'0', '000014086', 0, 0, '2016-10-01 16:49:48', 1, 1, 0, 0, NULL, 1);
/*!40000 ALTER TABLE `prozesse` ENABLE KEYS */;

/*!40000 ALTER TABLE `schritte` DISABLE KEYS */;
INSERT INTO `schritte` (`SchritteID`, `Titel`, `Prioritaet`, `Reihenfolge`, `Bearbeitungsstatus`, `BearbeitungsZeitpunkt`, `BearbeitungsBeginn`, `BearbeitungsEnde`, `homeverzeichnisNutzen`, `typMetadaten`, `typAutomatisch`, `typImportFileUpload`, `typExportRus`, `typImagesLesen`, `typImagesSchreiben`, `typExportDMS`, `typBeimAnnehmenModul`, `typBeimAnnehmenAbschliessen`, `typBeimAnnehmenModulUndAbschliessen`, `typAutomatischScriptpfad`, `typBeimAbschliessenVerifizieren`, `typModulName`, `BearbeitungsBenutzerID`, `ProzesseID`, `edittype`, `scriptName1`, `scriptName2`, `typAutomatischScriptpfad2`, `scriptName3`, `typAutomatischScriptpfad3`, `scriptName4`, `typAutomatischScriptpfad4`, `scriptName5`, `typAutomatischScriptpfad5`, `typScriptStep`, `batchStep`, `stepPlugin`, `validationPlugin`) VALUES
(3, 'Structure and metadata', 0, 3, 0, '2016-02-03 16:49:48', NULL, NULL, 0, b'1', b'0', b'0', b'0', b'0', b'0', b'0', b'1', b'0', b'0', '', b'0', NULL, 1, 1, 4, '', '', '', '', '', '', '', '', '', b'0', b'0', '', ''),
(1, 'Scanning', 0, 1, 1, '2016-03-02 11:38:28', NULL, NULL, 0, b'0', b'0', b'0', b'0', b'1', b'1', b'0', b'0', b'0', b'0', '', b'0', NULL, 1, 1, 4, '', '', '', '', '', '', '', '', '', b'0', b'0', '', ''),
(2, 'QC', 0, 2, 0, '2016-02-03 16:49:48', NULL, NULL, 0, b'0', b'0', b'0', b'0', b'1', b'0', b'0', b'0', b'0', b'0', '', b'0', NULL, 1, 1, 4, '', '', '', '', '', '', '', '', '', b'0', b'0', '', ''),
(4, 'Export DMS', 0, 4, 0, '2016-02-03 16:49:48', NULL, NULL, 0, b'0', b'0', b'0', b'0', b'0', b'0', b'1', b'0', b'0', b'0', '', b'0', NULL, 1, 1, 4, '', '', '', '', '', '', '', '', '', b'0', b'0', '', '');
/*!40000 ALTER TABLE `schritte` ENABLE KEYS */;

/*!40000 ALTER TABLE `schritteberechtigtegruppen` DISABLE KEYS */;
INSERT INTO `schritteberechtigtegruppen` (`BenutzerGruppenID`, `schritteID`) VALUES
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(2, 1),
(3, 2),
(5, 3),
(6, 4);
/*!40000 ALTER TABLE `schritteberechtigtegruppen` ENABLE KEYS */;
