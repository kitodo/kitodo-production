--
-- Migration: Renaming to English
--
-- 1. Rename tables
--

RENAME TABLE
  batches TO batch,
  batchesprozesse TO batch_x_process,
  benutzer TO user,
  benutzereigenschaften TO userProperty,
  benutzergruppen TO userGroup,
  benutzergruppenmitgliedschaft TO user_x_userGroup,
  dockets TO docket,
  ldapgruppen TO ldapGroup,
  metadatenkonfigurationen TO ruleset,
  projectfilegroups TO projectFileGroup,
  projektbenutzer TO project_x_user,
  projekte TO project,
  prozesse TO process,
  prozesseeigenschaften TO processProperty,
  schritte TO step,
  schritteberechtigtebenutzer TO step_x_user,
  schritteberechtigtegruppen TO step_x_userGroup,
  vorlagen TO template,
  vorlageneigenschaften TO templateProperty,
  werkstuecke TO workpiece,
  werkstueckeeigenschaften TO workpieceProperty;

--
-- 2. Rename columns in tables
--

ALTER TABLE batch
  CHANGE BatchID id INT(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE batch_x_process
  CHANGE BatchID batch_id INT(11) NOT NULL,
  CHANGE ProzesseID process_id INT(11) NOT NULL;

ALTER TABLE docket
  CHANGE docketID id INT(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE history
  CHANGE historyid id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE stringvalue stringValue VARCHAR(255),
  CHANGE processID process_id INT(11);

ALTER TABLE ldapGroup
  CHANGE ldapgruppenID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE titel title VARCHAR(255),
  CHANGE userDN userDn VARCHAR(255),
  CHANGE sambaSID sambaSid VARCHAR(255),
  CHANGE sambaPrimaryGroupSID sambaPrimaryGroupSid VARCHAR(255),
  CHANGE sambaPwdMustChange sambaPasswordMustChange VARCHAR(255);

ALTER TABLE process
  CHANGE ProzesseID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE Titel title VARCHAR(255),
  CHANGE ausgabename outputName VARCHAR(255),
  CHANGE IstTemplate isTemplate TINYINT(1),
  CHANGE inAuswahllisteAnzeigen isChoiceListShown TINYINT(1),
  CHANGE erstellungsdatum creationDate DATETIME,
  CHANGE wikifield wikiField LONGTEXT,
  CHANGE ProjekteID project_id INT(11),
  CHANGE MetadatenKonfigurationID ruleset_id INT(11),
  CHANGE docketID docket_id INT(11);

ALTER TABLE processProperty
  CHANGE prozesseeigenschaftenID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE Titel title VARCHAR(255),
  CHANGE Wert value LONGTEXT,
  CHANGE IstObligatorisch isObligatory TINYINT(1),
  CHANGE DatentypenID dataType INT(11),
  CHANGE Auswahl choice VARCHAR(255),
  CHANGE prozesseID process_id INT(11);

ALTER TABLE project
  CHANGE ProjekteID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE Titel title VARCHAR(255),
  CHANGE metsContentIDs metsContentId VARCHAR(255);

ALTER TABLE project_x_user
  CHANGE BenutzerID user_id INT(11) NOT NULL,
  CHANGE ProjekteID project_id INT(11) NOT NULL;

ALTER TABLE projectFileGroup
  CHANGE ProjectFileGroupID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE mimetype mimeType VARCHAR(255),
  CHANGE ProjekteID project_id INT(11);

ALTER TABLE ruleset
  CHANGE MetadatenKonfigurationID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE Titel title VARCHAR(255),
  CHANGE Datei file VARCHAR(255);

ALTER TABLE step
  CHANGE SchritteID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE Titel title VARCHAR(255),
  CHANGE Prioritaet priority INT(11),
  CHANGE Reihenfolge ordering INT(11),
  CHANGE Bearbeitungsstatus processingStatus INT(11),
  CHANGE edittype editType INT(11),
  CHANGE BearbeitungsZeitpunkt processingTime DATETIME,
  CHANGE BearbeitungsBeginn processingBegin DATETIME,
  CHANGE BearbeitungsEnde processingEnd DATETIME,
  CHANGE homeverzeichnisNutzen homeDirectory SMALLINT(6),
  CHANGE typMetadaten typeMetadata TINYINT(1),
  CHANGE typAutomatisch typeAutomatic TINYINT(1),
  CHANGE typImportFileUpload typeImportFileUpload TINYINT(1),
  CHANGE typExportRus typeExportRussian TINYINT(1),
  CHANGE typImagesLesen typeImagesRead TINYINT(1),
  CHANGE typImagesSchreiben typeImagesWrite TINYINT(1),
  CHANGE typExportDMS typeExportDms TINYINT(1),
  CHANGE typBeimAnnehmenModul typeAcceptModule TINYINT(1),
  CHANGE typBeimAnnehmenAbschliessen typeAcceptClose TINYINT(1),
  CHANGE typBeimAnnehmenModulUndAbschliessen typeAcceptModuleAndClose TINYINT(1),
  CHANGE typScriptStep typeScriptStep TINYINT(1),
  CHANGE typAutomatischScriptpfad typeAutomaticScriptPath VARCHAR(255),
  CHANGE typAutomatischScriptpfad2 typeAutomaticScriptPath2 VARCHAR(255),
  CHANGE typAutomatischScriptpfad3 typeAutomaticScriptPath3 VARCHAR(255),
  CHANGE typAutomatischScriptpfad4 typeAutomaticScriptPath4 VARCHAR(255),
  CHANGE typAutomatischScriptpfad5 typeAutomaticScriptPath5 VARCHAR(255),
  CHANGE typBeimAbschliessenVerifizieren typeCloseVerify TINYINT(1),
  CHANGE typModulName typeModuleName VARCHAR(255),
  CHANGE BearbeitungsBenutzerID processingUser_id INT(11),
  CHANGE ProzesseID process_id INT(11);

ALTER TABLE step_x_user
  CHANGE BenutzerID user_id INT(11) NOT NULL,
  CHANGE schritteID step_id INT(11) NOT NULL;

ALTER TABLE step_x_userGroup
  CHANGE BenutzerGruppenID userGroup_id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE schritteID step_id INT(11);

ALTER TABLE template
  CHANGE VorlagenID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE Herkunft origin VARCHAR(255),
  CHANGE ProzesseID process_id INT(11);

ALTER TABLE templateProperty
  CHANGE vorlageneigenschaftenID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE Titel title VARCHAR(255),
  CHANGE Wert value LONGTEXT,
  CHANGE IstObligatorisch isObligatory TINYINT(1),
  CHANGE DatentypenID dataType INT(11),
  CHANGE Auswahl choice VARCHAR(255),
  CHANGE vorlagenID template_id INT(11);

ALTER TABLE user
  CHANGE BenutzerID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE Vorname name VARCHAR(255),
  CHANGE Nachname surname VARCHAR(255),
  CHANGE ldaplogin ldapLogin VARCHAR(255),
  CHANGE passwort password VARCHAR(255),
  CHANGE IstAktiv isActive TINYINT(1),
  CHANGE Standort location VARCHAR(255),
  CHANGE metadatensprache metadataLanguage VARCHAR(255),
  CHANGE mitMassendownload withMassDownload TINYINT(1),
  CHANGE confVorgangsdatumAnzeigen configProductionDateShow TINYINT(1),
  CHANGE Tabellengroesse tableSize INT(11),
  CHANGE sessiontimeout sessionTimeout INT(11),
  CHANGE ldapgruppenID ldapGroup_id INT(11);

ALTER TABLE user_x_userGroup
  CHANGE BenutzerID user_id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE BenutzerGruppenID userGroup_id INT(11);

ALTER TABLE userGroup
  CHANGE BenutzergruppenID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE titel title VARCHAR(255),
  CHANGE berechtigung permission INT(11);

ALTER TABLE userProperty
  CHANGE benutzereigenschaftenID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE Titel title VARCHAR(255),
  CHANGE Wert value VARCHAR(255),
  CHANGE IstObligatorisch isObligatory TINYINT(1),
  CHANGE DatentypenID dataType INT(11),
  CHANGE Auswahl choice VARCHAR(255),
  CHANGE BenutzerID user_id INT(11);

ALTER TABLE workpiece
  CHANGE WerkstueckeID id INT(11) NOT NULL,
  CHANGE ProzesseID process_id INT(11) NOT NULL;

ALTER TABLE workpieceProperty
  CHANGE werkstueckeeigenschaftenID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE Titel title VARCHAR(255),
  CHANGE Wert value LONGTEXT,
  CHANGE IstObligatorisch isObligatory TINYINT(1),
  CHANGE DatentypenID dataType INT(11),
  CHANGE Auswahl choice VARCHAR(255),
  CHANGE werkstueckeID workpiece_id INT(11);

--
-- 3. Add foreign keys
--

ALTER TABLE batch_x_process add constraint `FK_batch_x_process_batch_id`
foreign key (batch_id) REFERENCES batch(id);

ALTER TABLE batch_x_process add constraint `FK_batch_x_process_process_id`
foreign key (process_id) REFERENCES process(id);

ALTER TABLE history add constraint `FK_history_process_id`
foreign key (process_id) REFERENCES process(id);

ALTER TABLE process add constraint `FK_process_project_id`
foreign key (project_id) REFERENCES project(id);

ALTER TABLE process add constraint `FK_process_ruleset_id`
foreign key (ruleset_id) REFERENCES ruleset(id);

ALTER TABLE process add constraint `FK_process_docket_id`
foreign key (docket_id) REFERENCES docket(id);

ALTER TABLE processProperty add constraint `FK_processProperty_process_id`
foreign key (process_id) REFERENCES process(id);

ALTER TABLE project_x_user add constraint `FK_project_x_user_project_id`
foreign key (project_id) REFERENCES project(id);

ALTER TABLE project_x_user add constraint `FK_project_x_user_user_id`
foreign key (user_id) REFERENCES user(id);

ALTER TABLE projectFileGroup add constraint `FK_projectFileGroup_project_id`
foreign key (project_id) REFERENCES project(id);

ALTER TABLE step add constraint `FK_step_processingUser_id`
foreign key (processingUser_id) REFERENCES user(id);

ALTER TABLE step add constraint `FK_step_process_id`
foreign key (process_id) REFERENCES process(id);

ALTER TABLE step_x_user add constraint `FK_step_x_user_step_id`
foreign key (step_id) REFERENCES step(id);

ALTER TABLE step_x_user add constraint `FK_step_x_user_user_id`
foreign key (user_id) REFERENCES user(id);

ALTER TABLE step_x_userGroup add constraint `FK_step_x_userGroup_step_id`
foreign key (step_id) REFERENCES step(id);

ALTER TABLE step_x_userGroup add constraint `FK_step_x_userGroup_userGroup_id`
foreign key (userGroup_id) REFERENCES userGroup(id);

ALTER TABLE template add constraint `FK_template_process_id`
foreign key (process_id) REFERENCES process(id);

ALTER TABLE templateProperty add constraint `FK_templateProperty_template_id`
foreign key (template_id) REFERENCES template(id);

ALTER TABLE user add constraint `FK_user_ldapGroup_id`
foreign key (ldapGroup_id) REFERENCES ldapGroup(id);

ALTER TABLE user_x_userGroup add constraint `FK_user_x_userGroup_user_id`
foreign key (user_id) REFERENCES user(id);

ALTER TABLE user_x_userGroup add constraint `FK_user_x_userGroup_userGroup_id`
foreign key (userGroup_id) REFERENCES userGroup(id);

ALTER TABLE userProperty add constraint `FK_userProperty_user_id`
foreign key (user_id) REFERENCES user(id);

ALTER TABLE workpiece add constraint `FK_workpiece_process_id`
foreign key (process_id) REFERENCES process(id);

ALTER TABLE workpieceProperty add constraint `FK_workpieceProperty_workpiece_id`
foreign key (workpiece_id) REFERENCES workpiece(id);

--
-- 4. Check if table exists, if yes, remove it
--
DROP TABLE IF EXISTS schritteeigenschaften
