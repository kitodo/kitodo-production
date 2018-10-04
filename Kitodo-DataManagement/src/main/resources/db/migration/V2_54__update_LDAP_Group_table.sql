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

UPDATE ldapGroup SET homeDirectory = '/home/test/users/{login}',
gidNumber = '242',
userDN = 'cn={login},ou=user,o=TestOrg,dc=kitodo,dc=org',
objectClasses = 'top,inetOrgPerson,posixAccount,shadowAccount,sambaSamAccount',
sambaSID = 'S-1-5-21-1234567890-123456789-1234567890-{uidnumber*2+1001}',
sn = '{login}',
uid = '{login}',
description = 'description',
displayName = '{user full name}',
gecos = 'gecos',
loginShell = 'loginShell',
sambaAcctFlags = '[U          ]',
sambaLogonScript = '_{login}.bat',
sambaPrimaryGroupSID = 'S-1-5-21-1234567890-123456789-1234567890-513',
sambaPasswordMustChange = '2147483647',
sambaPasswordHistory = '00000000000000000000000000000000000000',
sambaLogonHours = 'FFFFFFFFFFFFFFFFFFFF',
sambaKickoffTime = '0'
WHERE id = 2 ;
