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

-- 1. Update the dummy LDAP group

SET SQL_SAFE_UPDATES = 0;

UPDATE ldapGroup SET homeDirectory = '/usr/local/kitodo/users/{login}',
title = 'Local LDAP',
gidNumber = '100',
userDN = 'cn={login},ou=users,dc=nodomain',
objectClasses = 'top,inetOrgPerson,posixAccount,shadowAccount,sambaSamAccount',
sambaSID = 'S-1-5-21-1234567890-123456789-1234567890-{uidnumber*2+1001}',
sn = '{login}',
uid = '{login}',
description = 'Exemplary configuration of an LDAP group',
displayName = '{user full name}',
gecos = '{user full name}',
loginShell = '/bin/false',
sambaAcctFlags = '[U          ]',
sambaLogonScript = '_{login}.bat',
sambaPrimaryGroupSID = 'S-1-5-21-1234567890-123456789-1234567890-1000',
sambaPasswordMustChange = '2147483647',
sambaPasswordHistory = '00000000000000000000000000000000000000',
sambaLogonHours = 'FFFFFFFFFFFFFFFFFFFF',
sambaKickoffTime = '0'
WHERE title = 'test' AND homeDirectory IS NULL AND gidNumber IS NULL AND sambaSID IS NULL;

SET SQL_SAFE_UPDATES = 1;
