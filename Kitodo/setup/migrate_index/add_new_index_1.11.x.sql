--
-- Add new indexes for Kitodo.Production CE newer than 1.11.2
--
ALTER TABLE `batchesprozesse`
  ADD KEY `FK_ProzesseID` (`ProzesseID`),
  ADD KEY `FK_BatchID` (`BatchID`);

ALTER TABLE `benutzer`
  ADD KEY `FK_LdapgruppenID` (`ldapgruppenID`);

ALTER TABLE `benutzereigenschaften`
  ADD KEY `FK_BenutzerID` (`BenutzerID`);

ALTER TABLE `benutzergruppenmitgliedschaft`
  ADD KEY `FK_BenutzerID` (`BenutzerID`),
  ADD KEY `FK_BenutzerGruppenID` (`BenutzerGruppenID`);

ALTER TABLE `history`
  ADD KEY `FK_ProzesseID` (`processID`);

ALTER TABLE `projectfilegroups`
  ADD KEY `FK_ProjekteID` (`ProjekteID`);

ALTER TABLE `projektbenutzer`
  ADD KEY `FK_ProjekteID` (`ProjekteID`),
  ADD KEY `FK_BenutzerID` (`BenutzerID`);

ALTER TABLE `prozesse`
  ADD KEY `FK_ProjekteID` (`ProjekteID`),
  ADD KEY `FK_MetadatenKonfigurationID` (`MetadatenKonfigurationID`),
  ADD KEY `FK_DocketID` (`docketID`);

ALTER TABLE `prozesseeigenschaften`
  ADD KEY `FK_ProzesseID` (`prozesseID`);

ALTER TABLE `schritte`
  ADD KEY `FK_BearbeitungsBenutzerID` (`BearbeitungsBenutzerID`),
  ADD KEY `FK_ProzesseID` (`ProzesseID`);

ALTER TABLE `schritteberechtigtebenutzer`
  ADD KEY `FK_SchritteID` (`schritteID`),
  ADD KEY `FK_BenutzerID` (`BenutzerID`);

ALTER TABLE `schritteberechtigtegruppen`
  ADD KEY `FK_SchritteID` (`schritteID`),
  ADD KEY `FK_BenutzerGruppenID` (`BenutzerGruppenID`);

ALTER TABLE `vorlagen`
  ADD KEY `FK_ProzesseID` (`ProzesseID`);

ALTER TABLE `vorlageneigenschaften`
  ADD KEY `FK_VorlagenID` (`vorlagenID`);

ALTER TABLE `werkstuecke`
  ADD KEY `FK_ProzesseID` (`ProzesseID`);

ALTER TABLE `werkstueckeeigenschaften`
  ADD KEY `FK_WerkstueckeID` (`werkstueckeID`);
