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

package org.kitodo.production.ldap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;
import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.beans.LdapServer;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.PasswordEncryption;

public class LdapUserTest {

    @Test
    public void configureUserPasswordWithSSHA() throws Exception {
        LdapUser ldapUser = createLdapUser(PasswordEncryption.SHA);
        Attributes attrs = ldapUser.getAttributes("");
        Object userPassword = attrs.get("userPassword").get();
        String value = userPassword.toString();
        assertTrue(value.startsWith("{SSHA}"), "SHA password should start with {SSHA}, got: " + value);
        assertTrue(value.length() > 6, "SSHA password should have content after prefix");
        verifySaltedHash(value.substring(6), "SHA-1");
    }

    @Test
    public void configureUserPasswordWithSMD5() throws Exception {
        LdapUser ldapUser = createLdapUser(PasswordEncryption.MD5);
        Attributes attrs = ldapUser.getAttributes("");
        Object userPassword = attrs.get("userPassword").get();
        String value = userPassword.toString();
        assertTrue(value.startsWith("{SMD5}"), "MD5 password should start with {SMD5}, got: " + value);
        assertTrue(value.length() > 6, "SMD5 password should have content after prefix");
        verifySaltedHash(value.substring(6), "MD5");
    }

    @Test
    public void configureUserPasswordWithSSHA256() throws Exception {
        LdapUser ldapUser = createLdapUser(PasswordEncryption.SHA_256);
        Attributes attrs = ldapUser.getAttributes("");
        Object userPassword = attrs.get("userPassword").get();
        String value = userPassword.toString();
        assertTrue(value.startsWith("{SSHA-256}"), "SHA-256 password should start with {SSHA-256}, got: " + value);
        assertTrue(value.length() > 10, "SSHA-256 password should have content after prefix");
        verifySaltedHash(value.substring(10), "SHA-256");
    }

    @Test
    public void configureSambaLMPasswordAndNTPasswordAreSet() throws Exception {
        LdapUser ldapUser = createLdapUser(PasswordEncryption.SHA);
        Attributes attrs = ldapUser.getAttributes("");
        assertNotNull(attrs.get("sambaLMPassword"), "sambaLMPassword should be set");
        assertNotNull(attrs.get("sambaNTPassword"), "sambaNTPassword should be set");
    }

    private LdapUser createLdapUser(PasswordEncryption passwordEncryption) throws NamingException, NoSuchAlgorithmException {
        LdapServer ldapServer = mock(LdapServer.class);
        when(ldapServer.isReadOnly()).thenReturn(false);
        when(ldapServer.getPasswordEncryption()).thenReturn(passwordEncryption);

        LdapGroup ldapGroup = mock(LdapGroup.class);
        when(ldapGroup.getLdapServer()).thenReturn(ldapServer);
        when(ldapGroup.getObjectClasses()).thenReturn("top,person,organizationalPerson,inetOrgPerson");
        when(ldapGroup.getUid()).thenReturn("uid");
        when(ldapGroup.getDisplayName()).thenReturn("");
        when(ldapGroup.getDescription()).thenReturn("");
        when(ldapGroup.getGecos()).thenReturn("");
        when(ldapGroup.getLoginShell()).thenReturn("");
        when(ldapGroup.getSn()).thenReturn("Surname");
        when(ldapGroup.getHomeDirectory()).thenReturn("/home/test");
        when(ldapGroup.getSambaAcctFlags()).thenReturn("[U          ]");
        when(ldapGroup.getSambaLogonScript()).thenReturn("");
        when(ldapGroup.getSambaPrimaryGroupSID()).thenReturn("");
        when(ldapGroup.getSambaSID()).thenReturn("");
        when(ldapGroup.getSambaPwdMustChange()).thenReturn("");
        when(ldapGroup.getSambaPasswordHistory()).thenReturn("");
        when(ldapGroup.getSambaLogonHours()).thenReturn("");
        when(ldapGroup.getSambaKickoffTime()).thenReturn("");
        when(ldapGroup.getGidNumber()).thenReturn("");

        User user = mock(User.class);
        when(user.getLdapLogin()).thenReturn("testuser");
        when(user.getLogin()).thenReturn("testuser");
        when(user.getName()).thenReturn("Test");
        when(user.getSurname()).thenReturn("User");
        when(user.getLdapGroup()).thenReturn(ldapGroup);

        LdapUser ldapUser = new LdapUser();
        ldapUser.configure(user, "testPassword123", "1000");
        return ldapUser;
    }

    private void verifySaltedHash(String base64Content, String algorithm) throws NoSuchAlgorithmException {
        byte[] decoded = Base64.getDecoder().decode(base64Content);
        int hashLength = switch (algorithm) {
            case "SHA-1" -> 20;
            case "SHA-256" -> 32;
            case "MD5" -> 16;
            default -> throw new NoSuchAlgorithmException(algorithm);
        };
        assertTrue(decoded.length > hashLength, "Hash+salt should be longer than hash alone");

        byte[] storedHash = new byte[hashLength];
        byte[] storedSalt = new byte[decoded.length - hashLength];
        System.arraycopy(decoded, 0, storedHash, 0, hashLength);
        System.arraycopy(decoded, hashLength, storedSalt, 0, storedSalt.length);

        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update("testPassword123".getBytes(StandardCharsets.UTF_8));
        md.update(storedSalt);
        byte[] computedHash = md.digest();

        assertEquals(Hex.encodeHexString(storedHash), Hex.encodeHexString(computedHash),
                "Computed hash should match stored hash for " + algorithm);
    }
}
