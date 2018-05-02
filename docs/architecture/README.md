# System architecture

This documentation is work in progress. Please consult the [javadoc](../javadoc/README.md) or [contact the developer team](https://maillist.slub-dresden.de/cgi-bin/mailman/listinfo/kitodo-developer).

Below you will find some fragments regarding the system architecture of Kitodo.production.

## Data types

### Entity

This data type is used for communication with database.
There are defined two abstract classes: BaseBean and BaseIndexedBean
BaseBean is extended by entities which are stored only in database and by abstract
BaseIndexedBean. All beans classes which are stored both in database and index
extend BaseIndexedBean class.

<img alt='Bean.puml' src='http://www.plantuml.com/plantuml/png/jP7DYi8m4CVlUOevMefzWE9bnR0Kn1NiOQ-n6Kh0JIMPBaBntJSVhabHZ-QI-VZF_6OISRHaH4VGdOIri1OMroWqJAniD66F1fHaPO_Ko4LHtsAij26G94NzEpZXbJ7nsB7H4YyeKsFdVdIa1WqgsP6I_83BidQUVT4b3HBvModEdsOOpvvy8ADq3wJeXAogge1ilUTWCHauCPIhRssztjKjYY2BrXsrDqE7xduZ2FNu0_dc4mDtcjs6VmThmxUyvT7ysd5VS__JrNBvy1qru8LGIpkNfLUR2_9Ih_jVEQdouPu3Huj_' >

### Type

This data type is used for communication with ElasticSearch index. All type classes extend
BaseType class. Type classes produce JSON objects which are converted to HTTP entities 
and in that form send to the ES server.

<img alt='Type.puml' src='http://www.plantuml.com/plantuml/png/nPBHIiCm58Rl-nJdiiFj1KOfEERGeKvSN-0qEPiPRL9oZh0ovhERfbRTP7S3Rm89___bFtzYh93Ve28eseOUXbrMv718Rkv5cZihHbfGdb1exjHxJLRI0ahaeDUq9pqZjAEWHdvxmYRhA5loO_YnvsT-JXyDItVB6OP7oJ59i6Jng0HKXSmm2vekNbO0xOMixez-sM9QE0o-8_nByeziSJdWShrwllKUMq2jL-MEbBnIXMASxP6B75myvZ6KpbN11I51HTS31z9QlB5Rs3XVeyp3O6ctW0vBhBvl_CKxHSnTwR7PotL-AV-M6bZ3kq4scPmWfyCqoSclYxflqnHCtLGKI_y4'>

### Data Transfer Object

This data type is used for communication with frontend. All DTO classes extend
BaseDTO class. Only indexed objects have DTO classes (indexed - beans which extend
BaseIndexedBean). Data used for them is extracted from JSON objects returned
as search results from ES index. All attributes are set up in data service classes
(e.g. ProcessService).

### UML Diagram
<img alt='DTO.puml' src='http://www.plantuml.com/plantuml/png/JOx12i8m38RlUOeSLyhs0CMJUDb9mRr0RKCfZ0wQ88BuxdPjnTsIFr--n3vOidM2RhNCUBPCb-MARz1p4WgKCNhGWMJsWp8z0yqr-2FAFBS2VW2QvvOtHynTTPSSKq29iX9CqrI5ozuLPnjMPOkETFVkVDaRLCLQLMxzlZsDu_6YGNe99SzV'>

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

## FileManagement

FileManagementInterface is used for file management. It contains two methods:
    *retrieve*
        it takes a file path and returns the file to which this path has pointed.
    *save*
        it takes a file and saves it to the given path and it returns a boolean if saving was successful or not.

<img alt='filemanagementInterface.puml' src='http://www.plantuml.com/plantuml/png/dP31IiGm48RlUOgX9qKVG6HNzYHuMF1kFCmsqpQGJ4AIzQ3stKrJKbeA2avXQER__mzcJZ5XI5ThXD9zG3_0ipPaaR4dIvpWIq2kpd5Yj0H7QCwENVJOKhD8KNzEqeCe4tQAuAty_f7TvUim3kXB853hWfhTxpEULFz_VAMKDS_BcY-SA7JOhi46BsIEpNo0Ml8W3xgQvLF-UD5_mMuytz9itdSGA0LD7tJpYca0dzljOq7jd16pQrAWo-qUre6BSuQGLvcn7cuK9s9rjUO7'>
