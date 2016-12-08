--
-- First real migration
-- Add foreign keys of the certain tables according to naming convention.
--

alter table batchesprozesse add constraint `FK_BatchesProzesse_BatchID`
foreign key (BatchID) REFERENCES batches(BatchID);

alter table batchesprozesse add constraint `FK_BatchesProzesse_ProzesseID`
foreign key (ProzesseID) REFERENCES prozesse(ProzesseID);

alter table benutzer add constraint `FK_Benutzer_LdapgruppenID`
foreign key (ldapgruppenID) REFERENCES ldapgruppen(ldapgruppenID);

alter table benutzereigenschaften add constraint `FK_Benutzereigenschaft_BenutzerID`
foreign key (BenutzerID) REFERENCES benutzer(BenutzerID);

alter table benutzergruppenmitgliedschaft add constraint `FK_Benutzergruppenmitgliedschaft_BenutzerID`
foreign key (BenutzerID) REFERENCES benutzer(BenutzerID);

alter table benutzergruppenmitgliedschaft add constraint `FK_Benutzergruppenmitgliedschaft_BenutzerGruppenID`
foreign key (BenutzerGruppenID) REFERENCES benutzergruppen(BenutzergruppenID);

alter table history add constraint `FK_History_ProzesseID`
foreign key (processID) REFERENCES prozesse(ProzesseID);

alter table projectfilegroups add constraint `FK_ProjectFileGroups_ProjekteID`
foreign key (ProjekteID) REFERENCES projekte(ProjekteID);

alter table projektbenutzer add constraint `FK_ProjektBenutzer_BenutzerID`
foreign key (BenutzerID) REFERENCES benutzer(BenutzerID);

alter table projektbenutzer add constraint `FK_ProjektBenutzer_ProjekteID`
foreign key (ProjekteID) REFERENCES projekte(ProjekteID);

alter table prozesse add constraint `FK_Prozess_ProjekteID`
foreign key (ProjekteID) REFERENCES projekte(ProjekteID);

alter table prozesse add constraint `FK_Prozess_MetadatenKonfigurationID`
foreign key (MetadatenKonfigurationID) REFERENCES metadatenkonfigurationen(MetadatenKonfigurationID);

alter table prozesse add constraint `FK_Prozess_DocketID`
foreign key (docketID) REFERENCES dockets(docketID);

alter table prozesseeigenschaften add constraint `FK_Prozesseigenschaft_ProzesseID`
foreign key (ProzesseID) REFERENCES prozesse(ProzesseID);

alter table schritte add constraint `FK_Schritte_BearbeitungsBenutzerID`
foreign key (BearbeitungsBenutzerID) REFERENCES benutzer(BenutzerID);

alter table schritte add constraint `FK_Schritte_ProzesseID`
foreign key (ProzesseID) REFERENCES prozesse(ProzesseID);

alter table schritteberechtigtebenutzer add constraint `FK_SchritteBerechtigteBenutzer_BenutzerID`
foreign key (BenutzerID) REFERENCES benutzer(BenutzerID);

alter table schritteberechtigtebenutzer add constraint `FK_SchritteBerechtigteBenutzer_SchritteID`
foreign key (SchritteID) REFERENCES schritte(SchritteID);

alter table schritteberechtigtegruppen add constraint `FK_SchritteBerechtigtegruppen_BenutzerGruppenID`
foreign key (BenutzergruppenID) REFERENCES benutzergruppen(BenutzergruppenID);

alter table schritteberechtigtegruppen add constraint `FK_SchritteBerechtigtegruppen_SchritteID`
foreign key (SchritteID) REFERENCES schritte(SchritteID);

alter table vorlagen add constraint `FK_Vorlagen_ProzesseID`
foreign key (ProzesseID) REFERENCES prozesse(ProzesseID);

alter table vorlageneigenschaften add constraint `FK_Vorlageneigenschaften_VorlagenID`
foreign key (vorlagenID) REFERENCES vorlagen(VorlagenID);

alter table werkstuecke add constraint `FK_Werkstuecke_ProzesseID`
foreign key (ProzesseID) REFERENCES prozesse(ProzesseID);

alter table werkstueckeeigenschaften add constraint `FK_Werkstueckeeigenschaften_WerkstueckeID`
foreign key (werkstueckeID) REFERENCES werkstuecke(WerkstueckeID);
