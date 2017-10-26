# Data Management - work with different data types

## Modification of data

Scenario: New column to database was inserted.
1. Add new attribute to Entity class (XxxxBean) with get/set methods.
2. Write flyway migration.
3. Add new JSON key in createDocument method of XxxxType class.
4. Adjust test for this method.
5. Update mapping for ElasticSearch index.
6. Add new attribute to DTO class (XxxxDTO) with get/set methods.
7. Add usage of the setter in method convertJSONObjectToDTO of XxxxService class.
8 Update frontend page with usage of the new attribute.
9. Run application and recreate index (drop mapping, create new mapping, reindex data).
