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

-- Delete passwords for users authenticating by LDAP
--
-- If the user has an LDAP group set, but the LDAP group has no LDAP server
-- assigned (a configuration mismatch, but a legacy default on systems not
-- using LDAP authentication), do not void the password.

UPDATE user
    LEFT JOIN ldapgroup ON user.ldapGroup_id = ldapgroup.id
    SET user.password = NULL
    WHERE (ldapgroup.ldapserver_id IS NOT NULL AND user.id > -1);
