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
-- Migration: Renaming columns to English
--
-- 1. Rename columns in tables
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

ALTER TABLE task
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
  CHANGE BearbeitungsBenutzerID user_id INT(11)
    COMMENT 'This field contains information about user, which works on this task.',
  CHANGE ProzesseID process_id INT(11);

ALTER TABLE task_x_user
  CHANGE BenutzerID user_id INT(11) NOT NULL
    COMMENT 'This field contains information about users, which are allowed to work on this task.',
  CHANGE schritteID task_id INT(11) NOT NULL;

ALTER TABLE task_x_userGroup
  CHANGE BenutzerGruppenID userGroup_id INT(11) NOT NULL
    COMMENT 'This field contains information about user''s groups, which are allowed to work on this task.',
  CHANGE schritteID task_id INT(11);

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
