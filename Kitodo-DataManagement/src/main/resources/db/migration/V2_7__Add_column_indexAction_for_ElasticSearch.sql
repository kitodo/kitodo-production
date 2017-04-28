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
-- Migration: Add column indexed to tables from which data is indexed in ElasticSearch


-- 1. Add columns
--

ALTER TABLE batch ADD indexAction VARCHAR(6);
ALTER TABLE docket ADD indexAction VARCHAR(6);
ALTER TABLE history ADD indexAction VARCHAR(6);
ALTER TABLE process ADD indexAction VARCHAR(6);
ALTER TABLE project ADD indexAction VARCHAR(6);
ALTER TABLE property ADD indexAction VARCHAR(6);
ALTER TABLE ruleset ADD indexAction VARCHAR(6);
ALTER TABLE task ADD indexAction VARCHAR(6);
ALTER TABLE template ADD indexAction VARCHAR(6);
ALTER TABLE user ADD indexAction VARCHAR(6);
ALTER TABLE userGroup ADD indexAction VARCHAR(6);
ALTER TABLE workpiece ADD indexAction VARCHAR(6);

-- 2. Add columns
--

UPDATE batch SET indexAction = 'INDEX';
UPDATE docket SET indexAction = 'INDEX';
UPDATE history SET indexAction = 'INDEX';
UPDATE process SET indexAction = 'INDEX';
UPDATE project SET indexAction = 'INDEX';
UPDATE property SET indexAction = 'INDEX';
UPDATE ruleset SET indexAction = 'INDEX';
UPDATE task SET indexAction = 'INDEX';
UPDATE template SET indexAction = 'INDEX';
UPDATE user SET indexAction = 'INDEX';
UPDATE userGroup SET indexAction = 'INDEX';
UPDATE workpiece SET indexAction = 'INDEX';
