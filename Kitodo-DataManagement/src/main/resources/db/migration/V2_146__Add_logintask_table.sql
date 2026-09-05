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

-- Add table "logintask"
CREATE TABLE IF NOT EXISTS logintask
(
    id INT(11) NOT NULL AUTO_INCREMENT,
    type VARCHAR(255) NOT NULL COMMENT 'e.g. save_user_to_ldap',
    status VARCHAR(255) NOT NULL COMMENT 'e.g. pending, completed, failed',
    createdAt DATETIME NOT NULL COMMENT 'creation date',
    executedAt DATETIME DEFAULT NULL COMMENT 'time and date of task execution',
    error VARCHAR(255) DEFAULT NULL COMMENT 'error message in case execution failed',
    user_id INT(11) NOT NULL COMMENT 'user id of associated user',
    PRIMARY KEY(id),
    CONSTRAINT FK_logintask_user_id 
        FOREIGN KEY (user_id) REFERENCES user (id),
    KEY index_pending_tasks_for_user (user_id, status) COMMENT 'index for finding tasks for a specific user'
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE utf8mb4_unicode_ci;