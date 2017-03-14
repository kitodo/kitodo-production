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
-- Migration: Renaming tables to English
--
-- 1. Rename tables
--

RENAME TABLE
  batches TO batch,
  batchesprozesse TO batch_x_process,
  benutzer TO user,
  benutzereigenschaften TO userProperty,
  benutzergruppen TO userGroup,
  benutzergruppenmitgliedschaft TO user_x_userGroup,
  dockets TO docket,
  ldapgruppen TO ldapGroup,
  metadatenkonfigurationen TO ruleset,
  projectfilegroups TO projectFileGroup,
  projektbenutzer TO project_x_user,
  projekte TO project,
  prozesse TO process,
  prozesseeigenschaften TO processProperty,
  schritte TO task,
  schritteberechtigtebenutzer TO task_x_user,
  schritteberechtigtegruppen TO task_x_userGroup,
  vorlagen TO template,
  vorlageneigenschaften TO templateProperty,
  werkstuecke TO workpiece,
  werkstueckeeigenschaften TO workpieceProperty;

--
-- 2. Check if table exists, if yes, remove it
--
DROP TABLE IF EXISTS schritteeigenschaften;
