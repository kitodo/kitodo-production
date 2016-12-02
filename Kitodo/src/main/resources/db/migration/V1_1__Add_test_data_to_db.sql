-- This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
-- 
-- Visit the websites for more information. 
--     		- http://www.kitodo.org
--    		- https://github.com/goobi/goobi-production
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
-- You should have received a copy of the GNU General Public License
-- along with this program.  If not, see <http://www.gnu.org/licenses/>.
--
-- Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
-- of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
-- link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
-- distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
-- conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
-- library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
-- exception statement from your version.

--
-- Insert default data and test accounts
--

/* id 2 has to be inserted to match users (benutzer)*/
INSERT INTO `ldapgruppen` (`ldapgruppenID`, `titel`) VALUES (2, 'test');

--
-- Password for test users is "test"
-- NOTICE: Disable those users in production environment!
--

INSERT INTO `benutzer` (`BenutzerID`,`Login`,`Passwort`,`Vorname`,`Nachname`,`IstAktiv`,`Standort`,`mitMassendownload`,`Tabellengroesse`,`ldapgruppenID`,`metadatensprache`,`confVorgangsdatumAnzeigen`) VALUES
 (1,'testAdmin','OvEJ00yyYZQ=','test','Admin',1,'Göttingen',0,10,2,'de',0);
INSERT INTO `benutzer` (`BenutzerID`,`Login`,`Passwort`,`Vorname`,`Nachname`,`IstAktiv`,`Standort`,`mitMassendownload`,`Tabellengroesse`,`ldapgruppenID`,`metadatensprache`,`confVorgangsdatumAnzeigen`) VALUES
 (2,'testScanning','OvEJ00yyYZQ=','test','Scanning',1,'Göttingen',0,10,2,'de',0);
INSERT INTO `benutzer` (`BenutzerID`,`Login`,`Passwort`,`Vorname`,`Nachname`,`IstAktiv`,`Standort`,`mitMassendownload`,`Tabellengroesse`,`ldapgruppenID`,`metadatensprache`,`confVorgangsdatumAnzeigen`) VALUES
 (3,'testQC','OvEJ00yyYZQ=','test','QC',1,'Göttingen',0,10,2,'de',0);
INSERT INTO `benutzer` (`BenutzerID`,`Login`,`Passwort`,`Vorname`,`Nachname`,`IstAktiv`,`Standort`,`mitMassendownload`,`Tabellengroesse`,`ldapgruppenID`,`metadatensprache`,`confVorgangsdatumAnzeigen`) VALUES
 (4,'testImaging','OvEJ00yyYZQ=','test','Imaging',1,'Göttingen',0,10,2,'de',0);
INSERT INTO `benutzer` (`BenutzerID`,`Login`,`Passwort`,`Vorname`,`Nachname`,`IstAktiv`,`Standort`,`mitMassendownload`,`Tabellengroesse`,`ldapgruppenID`,`metadatensprache`,`confVorgangsdatumAnzeigen`) VALUES
 (5,'testMetaData','OvEJ00yyYZQ=','test','MetaData',1,'Göttingen',0,10,2,'de',0);
INSERT INTO `benutzer` (`BenutzerID`,`Login`,`Passwort`,`Vorname`,`Nachname`,`IstAktiv`,`Standort`,`mitMassendownload`,`Tabellengroesse`,`ldapgruppenID`,`metadatensprache`,`confVorgangsdatumAnzeigen`) VALUES
 (6,'testProjectmanagement','OvEJ00yyYZQ=','test','Projectmanagement',1,'Göttingen',0,10,2,'de',0);

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

INSERT INTO `metadatenkonfigurationen` (`MetadatenKonfigurationID`, `Titel`, `Datei`, `orderMetadataByRuleset`) VALUES
(1, 'SLUBDD', 'ruleset_slubdd.xml', b'0'),
(2, 'SUBHH', 'ruleset_subhh.xml', b'0');

INSERT INTO `dockets` (`docketID`, `name`, `file`) VALUES
(1, 'default', 'docket.xsl');

INSERT INTO `projekte` (`ProjekteID`, `Titel`, `useDmsImport`, `dmsImportTimeOut`, `dmsImportRootPath`, `dmsImportImagesPath`, `dmsImportSuccessPath`, `dmsImportErrorPath`, `dmsImportCreateProcessFolder`, `fileFormatInternal`, `fileFormatDmsExport`, `metsRightsOwner`, `metsRightsOwnerLogo`, `metsRightsOwnerSite`, `metsDigiprovReference`, `metsDigiprovPresentation`, `metsDigiprovReferenceAnchor`, `metsDigiprovPresentationAnchor`, `metsPointerPath`, `metsPointerPathAnchor`, `metsPurl`, `metsContentIDs`, `metsRightsOwnerMail`, `startDate`, `endDate`, `numberOfPages`, `numberOfVolumes`, `projectIsArchived`) VALUES
(1, 'Example Project', b'1', 3600000, '/usr/local/kitodo/hotfolder/', '/usr/local/kitodo/hotfolder/', '/usr/local/kitodo/success/', '/usr/local/kitodo/error_mets/', b'0', 'Mets', 'Mets', 'Digital Library Kitodo', 'http://www.example.com/fileadmin/groups/kitodo/Logo/kitodo_logo_rgb.png', 'http://www.example.com', 'http://www.example.com/DB=1/PPN?PPN=$(meta.topstruct.CatalogIDDigital)', 'http://www.example.com/resolver?id=$(meta.topstruct.CatalogIDDigital)', 'http://www.example.com/DB=1/PPN?PPN=$(meta.topstruct.CatalogIDDigital)', 'http://www.example.com/resolver?id=$(meta.topstruct.CatalogIDDigital)', 'http://www.example.com/content/$(meta.CatalogIDDigital)/$(meta.topstruct.CatalogIDDigital).xml', 'http://www.example.com/content/$(meta.firstchild.CatalogIDDigital)/$(meta.topstruct.CatalogIDDigital).xml ', 'http://www.example.com/resolver?id=$(meta.CatalogIDDigital)', '', 'info@kitodo.org', '2016-01-01 00:00:00', '2019-12-31 00:00:00', 0, 0, b'0');

INSERT INTO `projektbenutzer` (`BenutzerID`, `ProjekteID`) VALUES
(1, 1),
(2, 1),
(3, 1),
(4, 1),
(5, 1),
(6, 1);

INSERT INTO `projectfilegroups` (`ProjectFileGroupID`, `name`, `path`, `mimetype`, `suffix`, `ProjekteID`, `folder`, `previewImage`) VALUES
(1, 'MAX', 'http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/max/', 'image/jpeg', 'jpg', 1, '', 0),
(2, 'DEFAULT', 'http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/default/', 'image/jpeg', 'jpg', 1, '', 0),
(3, 'THUMBS', 'http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/thumbs/', 'image/jpeg', 'jpg', 1, '', 0),
(4, 'FULLTEXT', 'http://www.example.com/content/$(meta.CatalogIDDigital)/ocr/alto/', 'text/xml', 'xml', 1, '', 0),
(5, 'DOWNLOAD', 'http://www.example.com/content/$(meta.CatalogIDDigital)/pdf/', 'application/pdf', 'pdf', 1, '', 0);

INSERT INTO `prozesse` (`ProzesseID`, `Titel`, `ausgabename`, `IstTemplate`, `swappedOut`, `inAuswahllisteAnzeigen`, `sortHelperStatus`, `sortHelperImages`, `sortHelperArticles`, `erstellungsdatum`, `ProjekteID`, `MetadatenKonfigurationID`, `sortHelperDocstructs`, `sortHelperMetadata`, `wikifield`, `docketID`) VALUES
(1, 'Example_Workflow', NULL, b'1', b'0', b'0', '000014086', 0, 0, '2016-10-01 16:49:48', 1, 1, 0, 0, NULL, 1);

INSERT INTO `schritte` (`SchritteID`, `Titel`, `Prioritaet`, `Reihenfolge`, `Bearbeitungsstatus`, `BearbeitungsZeitpunkt`, `BearbeitungsBeginn`, `BearbeitungsEnde`, `homeverzeichnisNutzen`, `typMetadaten`, `typAutomatisch`, `typImportFileUpload`, `typExportRus`, `typImagesLesen`, `typImagesSchreiben`, `typExportDMS`, `typBeimAnnehmenModul`, `typBeimAnnehmenAbschliessen`, `typBeimAnnehmenModulUndAbschliessen`, `typAutomatischScriptpfad`, `typBeimAbschliessenVerifizieren`, `typModulName`, `BearbeitungsBenutzerID`, `ProzesseID`, `edittype`, `scriptName1`, `scriptName2`, `typAutomatischScriptpfad2`, `scriptName3`, `typAutomatischScriptpfad3`, `scriptName4`, `typAutomatischScriptpfad4`, `scriptName5`, `typAutomatischScriptpfad5`, `typScriptStep`, `batchStep`, `stepPlugin`, `validationPlugin`) VALUES
(3, 'Structure and metadata', 0, 3, 0, '2016-02-03 16:49:48', NULL, NULL, 0, b'1', b'0', b'0', b'0', b'0', b'0', b'0', b'1', b'0', b'0', '', b'0', NULL, 1, 1, 4, '', '', '', '', '', '', '', '', '', b'0', b'0', '', ''),
(1, 'Scanning', 0, 1, 1, '2016-03-02 11:38:28', NULL, NULL, 0, b'0', b'0', b'0', b'0', b'1', b'1', b'0', b'0', b'0', b'0', '', b'0', NULL, 1, 1, 4, '', '', '', '', '', '', '', '', '', b'0', b'0', '', ''),
(2, 'QC', 0, 2, 0, '2016-02-03 16:49:48', NULL, NULL, 0, b'0', b'0', b'0', b'0', b'1', b'0', b'0', b'0', b'0', b'0', '', b'0', NULL, 1, 1, 4, '', '', '', '', '', '', '', '', '', b'0', b'0', '', ''),
(4, 'Export DMS', 0, 4, 0, '2016-02-03 16:49:48', NULL, NULL, 0, b'0', b'0', b'0', b'0', b'0', b'0', b'1', b'0', b'0', b'0', '', b'0', NULL, 1, 1, 4, '', '', '', '', '', '', '', '', '', b'0', b'0', '', '');

INSERT INTO `schritteberechtigtegruppen` (`BenutzerGruppenID`, `schritteID`) VALUES
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(2, 1),
(3, 2),
(5, 3),
(6, 4);