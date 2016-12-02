--
-- Test scenario
-- Rename German table names to English
--

RENAME TABLE batches TO Batch, benutzer TO User,
             benutzereigenschaften TO UserProperty, benutzergruppen TO UserGroup,
             benutzergruppenmitgliedschaft TO User_x_UserGroup, dockets TO Docket,
             ldapgruppen TO LdapGroup, metadatenkonfigurationen TO MetadataConfig;
