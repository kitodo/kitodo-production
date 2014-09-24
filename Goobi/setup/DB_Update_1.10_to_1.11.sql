/* First we create the table "batches".
 * This table contains an ID (BatchID) and a possible title (title)
 */

CREATE TABLE IF NOT EXISTS `batches` (
  `BatchID` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`BatchID`)
) DEFAULT CHARSET=latin1;

/* Second we create the table "batchesprozesse".
 * This table forms the m:n relationship between table "prozesse" and "batches"
 */

CREATE TABLE IF NOT EXISTS `batchesprozesse` (
  `ProzesseID` int(11) NOT NULL,
  `BatchID` int(11) NOT NULL,
  PRIMARY KEY (`ProzesseID`,`BatchID`)
) DEFAULT CHARSET=latin1;

/* We have to migrate the already existing entries of batchID 
 * in table "prozesse" to the new table "batches"
 */

INSERT INTO batches( BatchID )
SELECT DISTINCT batchID
FROM prozesse
WHERE batchID IS NOT NULL;

/* After the migration of the "batches" we use
 * the table "batchesprozesse" to link "prozesse" and "batches"
 */

INSERT INTO batchesprozesse( ProzesseID, BatchID )
SELECT ProzesseID, batchID
FROM prozesse
WHERE batchID IS NOT NULL;

/* In the end the old column "batchID" in table "prozesse" */

ALTER TABLE `prozesse` DROP `batchID`;

/* Create the column "previewImage" in table "projectfilegroups" and set it to false for existing rows */

ALTER TABLE `projectfilegroups` ADD `previewImage` tinyint(1) DEFAULT NULL;
UPDATE `projectfilegroups` SET `previewImage` = '0';
