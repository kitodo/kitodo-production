--
-- First real migration
-- Add foreign keys of the certain tables according to naming convention.
--

alter table Batchesprozesse add constraint `FK_BatchesProzesse_BatchID`
foreign key (BatchID) REFERENCES Batches(BatchID);

alter table Batchesprozesse add constraint `FK_BatchesProzesse_ProzesseID`
foreign key (ProzesseID) REFERENCES Prozesse(ProzesseID);

alter table Benutzer add constraint `FK_Benutzer_LdapgruppenID`
foreign key (ldapgruppenID) REFERENCES Ldapgruppen(ldapgruppenID);

alter table Benutzereigenschaften add constraint `FK_Benutzereigenschaft_BenutzerID`
foreign key (BenutzerID) REFERENCES Benutzer(BenutzerID);

alter table Benutzergruppenmitgliedschaft add constraint `FK_Benutzergruppenmitgliedschaft_BenutzerID`
foreign key (BenutzerID) REFERENCES Benutzer(BenutzerID);

alter table Benutzergruppenmitgliedschaft add constraint `FK_Benutzergruppenmitgliedschaft_BenutzerGruppenID`
foreign key (BenutzerGruppenID) REFERENCES BenutzerGruppen(BenutzergruppenID);

alter table History add constraint `FK_History_ProzesseID`
foreign key (processID) REFERENCES Prozesse(ProzesseID);

alter table ProjectFileGroups add constraint `FK_ProjectFileGroups_ProjekteID`
foreign key (ProjekteID) REFERENCES Projekte(ProjekteID);

alter table ProjektBenutzer add constraint `FK_ProjektBenutzer_BenutzerID`
foreign key (BenutzerID) REFERENCES Benutzer(BenutzerID);

alter table ProjektBenutzer add constraint `FK_ProjektBenutzer_ProjekteID`
foreign key (ProjekteID) REFERENCES Projekte(ProjekteID);

alter table Prozesse add constraint `FK_Prozess_ProjekteID`
foreign key (ProjekteID) REFERENCES Projekte(ProjekteID);

alter table Prozesse add constraint `FK_Prozess_MetadatenKonfigurationID`
foreign key (MetadatenKonfigurationID) REFERENCES MetadatenKonfigurationen(MetadatenKonfigurationID);

alter table Prozesse add constraint `FK_Prozess_DocketID`
foreign key (docketID) REFERENCES Dockets(docketID);

alter table Prozesseeigenschaften add constraint `FK_Prozesseigenschaft_ProzesseID`
foreign key (ProzesseID) REFERENCES Prozesse(ProzesseID);

alter table Schritte add constraint `FK_Schritte_BearbeitungsBenutzerID`
foreign key (BearbeitungsBenutzerID) REFERENCES Benutzer(BenutzerID);

alter table Schritte add constraint `FK_Schritte_ProzesseID`
foreign key (ProzesseID) REFERENCES Prozesse(ProzesseID);

alter table Schritteberechtigtebenutzer add constraint `FK_SchritteBerechtigteBenutzer_BenutzerID`
foreign key (BenutzerID) REFERENCES Benutzer(BenutzerID);

alter table Schritteberechtigtebenutzer add constraint `FK_SchritteBerechtigteBenutzer_SchritteID`
foreign key (SchritteID) REFERENCES Schritte(SchritteID);

alter table Schritteberechtigtegruppen add constraint `FK_SchritteBerechtigtegruppen_BenutzerGruppenID`
foreign key (BenutzergruppenID) REFERENCES Benutzergruppen(BenutzergruppenID);

alter table Schritteberechtigtegruppen add constraint `FK_SchritteBerechtigtegruppen_SchritteID`
foreign key (SchritteID) REFERENCES Schritte(SchritteID);

alter table Vorlagen add constraint `FK_Vorlagen_ProzesseID`
foreign key (ProzesseID) REFERENCES Prozesse(ProzesseID);

alter table Vorlageneigenschaften add constraint `FK_Vorlageneigenschaften_VorlagenID`
foreign key (vorlagenID) REFERENCES Vorlagen(VorlagenID);

alter table Werkstuecke add constraint `FK_Werkstuecke_ProzesseID`
foreign key (ProzesseID) REFERENCES Prozesse(ProzesseID);

alter table Werkstueckeeigenschaften add constraint `FK_Werkstueckeeigenschaften_WerkstueckeID`
foreign key (werkstueckeID) REFERENCES Werkstuecke(WerkstueckeID);
