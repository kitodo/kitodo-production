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
-- Migration: Create table comment

-- 1. Create table comment-- Add columns table

CREATE TABLE comment
(
  id                int(11) NOT NULL AUTO_INCREMENT,
  message           varchar(255) DEFAULT NULL,
  type              varchar(9)   DEFAULT NULL,
  isCorrected       tinyint(1)   DEFAULT NULL,
  creationDate      datetime     DEFAULT NULL,
  correctionDate    datetime     DEFAULT NULL,
  user_id           int(11)      DEFAULT NULL,
  currentTask_id    int(11)      DEFAULT NULL,
  correctionTask_id int(11)      DEFAULT NULL,
  process_id        int(11)      DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY FK_user_id (user_id),
  KEY FK_currentTask_id (currentTask_id),
  KEY FK_correctionTask_id (correctionTask_id),
  KEY FK_process_id (process_id),
  CONSTRAINT FK_comment_user_id
    FOREIGN KEY (user_id) REFERENCES user (id),
  CONSTRAINT FK_comment_currentTask_id
    FOREIGN KEY (currentTask_id) REFERENCES task (id),
  CONSTRAINT FK_comment_correctionTask_id
    FOREIGN KEY (correctionTask_id) REFERENCES task (id),
  CONSTRAINT FK_comment_process_id
    FOREIGN KEY (process_id) REFERENCES process (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE utf8mb4_unicode_ci;
