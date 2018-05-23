/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.selenium.testframework.generators;

import org.kitodo.data.database.beans.LdapGroup;

public class LdapGroupGenerator {

    public static LdapGroup generateLdapGroup() {
        LdapGroup ldapGroup = new LdapGroup();
        ldapGroup.setTitle("Mock-Ldap");
        ldapGroup.setUserDN("cn={login},ou=user,o=TestOrg,dc=kitodo,dc=org");
        ldapGroup.setHomeDirectory("/home/test/users/{login}");
        ldapGroup.setGidNumber("242");
        ldapGroup.setObjectClasses("top,inetOrgPerson,posixAccount,shadowAccount,sambaSamAccount");
        ldapGroup.setSambaSID("S-1-5-21-1234567890-123456789-1234567890-{uidnumber*2+1001}");
        ldapGroup.setSn("{login}");
        ldapGroup.setUid("{login}");
        ldapGroup.setDescription("description");
        ldapGroup.setDisplayName("{user full name}");
        ldapGroup.setGecos("gecos");
        ldapGroup.setLoginShell("loginShell");
        ldapGroup.setSambaAcctFlags("[U          ]");
        ldapGroup.setSambaLogonScript("_{login}.bat");
        ldapGroup.setSambaPrimaryGroupSID("S-1-5-21-1234567890-123456789-1234567890-513");
        ldapGroup.setSambaPwdMustChange("2147483647");
        ldapGroup.setSambaPasswordHistory("0000000000000000000000000000000000000000000000000000000000000000");
        ldapGroup.setSambaLogonHours("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
        ldapGroup.setSambaKickoffTime("0");

        return ldapGroup;
    }
}
