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

/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `ldapgroup` (
  `id`, `title`) VALUES
  (2, 'test'); /* id 2 has to be inserted to match users (user)*/
/*!40000 ALTER TABLE `user` ENABLE KEYS */;

--
-- Password for test users is "test"
-- NOTICE: Disable those users in production environment!
--

/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` (`id`,`login`,`password`,`name`,`surname`,`active`,`location`,`withMassDownload`,`tableSize`,`ldapGroup_id`,`metadataLanguage`,`configProductionDateShow`) VALUES
 (1,'testAdmin','OvEJ00yyYZQ=','test','Admin',1,'Göttingen',0,10,2,'de',0);
INSERT INTO `user` (`id`,`login`,`password`,`name`,`surname`,`active`,`location`,`withMassDownload`,`tableSize`,`ldapGroup_id`,`metadataLanguage`,`configProductionDateShow`) VALUES
 (2,'testScanning','OvEJ00yyYZQ=','test','Scanning',1,'Göttingen',0,10,2,'de',0);
INSERT INTO `user` (`id`,`login`,`password`,`name`,`surname`,`active`,`location`,`withMassDownload`,`tableSize`,`ldapGroup_id`,`metadataLanguage`,`configProductionDateShow`) VALUES
 (3,'testQC','OvEJ00yyYZQ=','test','QC',1,'Göttingen',0,10,2,'de',0);
INSERT INTO `user` (`id`,`login`,`password`,`name`,`surname`,`active`,`location`,`withMassDownload`,`tableSize`,`ldapGroup_id`,`metadataLanguage`,`configProductionDateShow`) VALUES
 (4,'testImaging','OvEJ00yyYZQ=','test','Imaging',1,'Göttingen',0,10,2,'de',0);
INSERT INTO `user` (`id`,`login`,`password`,`name`,`surname`,`active`,`location`,`withMassDownload`,`tableSize`,`ldapGroup_id`,`metadataLanguage`,`configProductionDateShow`) VALUES
 (5,'testMetaData','OvEJ00yyYZQ=','test','MetaData',1,'Göttingen',0,10,2,'de',0);
INSERT INTO `user` (`id`,`login`,`password`,`name`,`surname`,`active`,`location`,`withMassDownload`,`tableSize`,`ldapGroup_id`,`metadataLanguage`,`configProductionDateShow`) VALUES
 (6,'testProjectmanagement','OvEJ00yyYZQ=','test','Projectmanagement',1,'Göttingen',0,10,2,'de',0);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;

/*!40000 ALTER TABLE `usergroup` DISABLE KEYS */;
INSERT INTO `usergroup` (`id`,`title`,`permission`) VALUES
 (1,'Administration',1),
 (2,'Scanning',3),
 (3,'QualityControl',3),
 (4,'Imaging',3),
 (5,'Metadata',3),
 (6,'Projectmanagement',2);
 /*!40000 ALTER TABLE `usergroup` ENABLE KEYS */;

/*!40000 ALTER TABLE `user_x_userGroup` DISABLE KEYS */;
INSERT INTO `user_x_userGroup` (`userGroup_id`,`user_id`) VALUES
 (1,1),
 (2,2),
 (3,3),
 (4,4),
 (5,5),
 (6,6);
/*!40000 ALTER TABLE `user_x_userGroup` ENABLE KEYS */;

/*!40000 ALTER TABLE `ruleset` DISABLE KEYS */;
INSERT INTO `ruleset` (`id`, `title`, `file`, `orderMetadataByRuleset`) VALUES
(1, 'SLUBDD', 'ruleset_slubdd.xml', b'0'),
(2, 'SUBHH', 'ruleset_subhh.xml', b'0');
/*!40000 ALTER TABLE `ruleset` ENABLE KEYS */;

/*!40000 ALTER TABLE `docket` DISABLE KEYS */;
INSERT INTO `docket` (`id`, `name`, `file`) VALUES
(1, 'default', 'docket.xsl');
/*!40000 ALTER TABLE `docket` ENABLE KEYS */;

/*!40000 ALTER TABLE `project` DISABLE KEYS */;
INSERT INTO `project` (`id`, `title`, `useDmsImport`, `dmsImportTimeOut`, `dmsImportRootPath`, `dmsImportImagesPath`, `dmsImportSuccessPath`, `dmsImportErrorPath`, `dmsImportCreateProcessFolder`, `fileFormatInternal`, `fileFormatDmsExport`, `metsRightsOwner`, `metsRightsOwnerLogo`, `metsRightsOwnerSite`, `metsDigiprovReference`, `metsDigiprovPresentation`, `metsDigiprovReferenceAnchor`, `metsDigiprovPresentationAnchor`, `metsPointerPath`, `metsPointerPathAnchor`, `metsPurl`, `metsContentId`, `metsRightsOwnerMail`, `startDate`, `endDate`, `numberOfPages`, `numberOfVolumes`, `projectIsArchived`) VALUES
(1, 'Example Project', b'1', 3600000, '/usr/local/kitodo/hotfolder/', '/usr/local/kitodo/hotfolder/', '/usr/local/kitodo/success/', '/usr/local/kitodo/error_mets/', b'0', 'Mets', 'Mets', 'Digital Library Kitodo', 'http://www.example.com/fileadmin/groups/kitodo/Logo/kitodo_logo_rgb.png', 'http://www.example.com', 'http://www.example.com/DB=1/PPN?PPN=$(meta.topstruct.CatalogIDDigital)', 'http://www.example.com/resolver?id=$(meta.topstruct.CatalogIDDigital)', 'http://www.example.com/DB=1/PPN?PPN=$(meta.topstruct.CatalogIDDigital)', 'http://www.example.com/resolver?id=$(meta.topstruct.CatalogIDDigital)', 'http://www.example.com/content/$(meta.CatalogIDDigital)/$(meta.topstruct.CatalogIDDigital).xml', 'http://www.example.com/content/$(meta.firstchild.CatalogIDDigital)/$(meta.topstruct.CatalogIDDigital).xml ', 'http://www.example.com/resolver?id=$(meta.CatalogIDDigital)', '', 'info@kitodo.org', '2016-01-01 00:00:00', '2019-12-31 00:00:00', 0, 0, b'0');
/*!40000 ALTER TABLE `project` ENABLE KEYS */;

/*!40000 ALTER TABLE `project_x_user` DISABLE KEYS */;
INSERT INTO `project_x_user` (`user_id`, `project_id`) VALUES
(1, 1),
(2, 1),
(3, 1),
(4, 1),
(5, 1),
(6, 1);
/*!40000 ALTER TABLE `project_x_user` ENABLE KEYS */;

/*!40000 ALTER TABLE `projectfilegroup` DISABLE KEYS */;
INSERT INTO `projectfilegroup` (`id`, `name`, `path`, `mimeType`, `suffix`, `project_id`, `folder`, `previewImage`) VALUES
(1, 'MAX', 'http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/max/', 'image/jpeg', 'jpg', 1, '', 0),
(2, 'DEFAULT', 'http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/default/', 'image/jpeg', 'jpg', 1, '', 0),
(3, 'THUMBS', 'http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/thumbs/', 'image/jpeg', 'jpg', 1, '', 0),
(4, 'FULLTEXT', 'http://www.example.com/content/$(meta.CatalogIDDigital)/ocr/alto/', 'text/xml', 'xml', 1, '', 0),
(5, 'DOWNLOAD', 'http://www.example.com/content/$(meta.CatalogIDDigital)/pdf/', 'application/pdf', 'pdf', 1, '', 0);
/*!40000 ALTER TABLE `projectfilegroup` ENABLE KEYS */;

/*!40000 ALTER TABLE `process` DISABLE KEYS */;
INSERT INTO `process` (`id`, `title`, `outputName`, `template`, `swappedOut`, `choiceListShown`, `sortHelperStatus`, `sortHelperImages`, `sortHelperArticles`, `creationDate`, `project_id`, `ruleset_id`, `sortHelperDocstructs`, `sortHelperMetadata`, `wikiField`, `docket_id`) VALUES
(1, 'Example_Workflow', NULL, b'1', b'0', b'0', '000014086', 0, 0, '2016-10-01 16:49:48', 1, 1, 0, 0, NULL, 1);
/*!40000 ALTER TABLE `process` ENABLE KEYS */;

/*!40000 ALTER TABLE `task` DISABLE KEYS */;
INSERT INTO `task` (`id`, `title`, `priority`, `ordering`, `processingStatus`, `processingTime`, `processingBegin`, `processingEnd`, `homeDirectory`, `typeMetadata`, `typeAutomatic`, `typeImportFileUpload`, `typeExportRussian`, `typeImagesRead`, `typeImagesWrite`, `typeExportDMS`, `typeAcceptModule`, `typeAcceptClose`, `typeAcceptModuleAndClose`, `typeAutomaticScriptPath`, `typeCloseVerify`, `typeModuleName`, `user_id`, `process_id`, `editType`, `scriptName1`, `scriptName2`, `typeAutomaticScriptPath2`, `scriptName3`, `typeAutomaticScriptPath3`, `scriptName4`, `typeAutomaticScriptPath4`, `scriptName5`, `typeAutomaticScriptPath5`, `typeScriptStep`, `batchStep`, `stepPlugin`, `validationPlugin`) VALUES
(3, 'Structure and metadata', 0, 3, 0, '2016-02-03 16:49:48', NULL, NULL, 0, b'1', b'0', b'0', b'0', b'0', b'0', b'0', b'1', b'0', b'0', '', b'0', NULL, 1, 1, 4, '', '', '', '', '', '', '', '', '', b'0', b'0', '', ''),
(1, 'Scanning', 0, 1, 1, '2016-03-02 11:38:28', NULL, NULL, 0, b'0', b'0', b'0', b'0', b'1', b'1', b'0', b'0', b'0', b'0', '', b'0', NULL, 1, 1, 4, '', '', '', '', '', '', '', '', '', b'0', b'0', '', ''),
(2, 'QC', 0, 2, 0, '2016-02-03 16:49:48', NULL, NULL, 0, b'0', b'0', b'0', b'0', b'1', b'0', b'0', b'0', b'0', b'0', '', b'0', NULL, 1, 1, 4, '', '', '', '', '', '', '', '', '', b'0', b'0', '', ''),
(4, 'Export DMS', 0, 4, 0, '2016-02-03 16:49:48', NULL, NULL, 0, b'0', b'0', b'0', b'0', b'0', b'0', b'1', b'0', b'0', b'0', '', b'0', NULL, 1, 1, 4, '', '', '', '', '', '', '', '', '', b'0', b'0', '', '');
/*!40000 ALTER TABLE `task` ENABLE KEYS */;

/*!40000 ALTER TABLE `task_x_userGroup` DISABLE KEYS */;
INSERT INTO `task_x_userGroup` (`userGroup_id`, `task_id`) VALUES
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(2, 1),
(3, 2),
(5, 3),
(6, 4);
/*!40000 ALTER TABLE `task_x_userGroup` ENABLE KEYS */;