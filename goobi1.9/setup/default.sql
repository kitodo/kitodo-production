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
