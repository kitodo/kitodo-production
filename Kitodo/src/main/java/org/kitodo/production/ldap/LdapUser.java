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

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.Objects;
import java.util.StringTokenizer;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.digests.MD4Digest;
import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.beans.User;

/**
 * This class is used by the DirObj example. It is a DirContext class that can
 * be stored by service providers like the LDAP system providers.
 */
public class LdapUser implements DirContext {
    private static final Logger logger = LogManager.getLogger(LdapUser.class);
    private String ldapLogin;
    private Attributes attributes;

    /**
     * Constructor of LdapUser.
     */
    public LdapUser() {
        this.attributes = new BasicAttributes(true);
    }

    /**
     * configure LdapUser with User data.
     *
     * @param user
     *            User object
     * @param inPassword
     *            String
     * @param inUidNumber
     *            String
     */
    public void configure(User user, String inPassword, String inUidNumber)
            throws NamingException, NoSuchAlgorithmException {
        MD4Digest digester = new MD4Digest();
        if (!user.getLdapGroup().getLdapServer().isReadOnly()) {

            if (Objects.nonNull(user.getLdapLogin())) {
                this.ldapLogin = user.getLdapLogin();

            } else {
                this.ldapLogin = user.getLogin();
            }

            LdapGroup ldapGroup = user.getLdapGroup();
            if (Objects.isNull(ldapGroup.getObjectClasses())) {
                throw new NamingException("no objectclass defined");
            }

            prepareAttributes(ldapGroup, user, inUidNumber);

            /*
             * Samba passwords
             */
            /* LanMgr */
            try {
                this.attributes.put("sambaLMPassword", toHexString(lmHash(inPassword)));
            } catch (InvalidKeyException | NoSuchPaddingException | BadPaddingException
                    | IllegalBlockSizeException | RuntimeException e) {
                logger.error(e.getMessage(), e);
            }
            /* NTLM */
            byte[] unicodePassword = inPassword.getBytes(StandardCharsets.UTF_16LE);
            byte[] hmm = new byte[digester.getDigestSize()];
            digester.update(unicodePassword, 0, unicodePassword.length);
            digester.doFinal(hmm, 0);
            this.attributes.put("sambaNTPassword", toHexString(hmm));

            /*
             * Encryption of password und Base64-Enconding
             */

            String passwordEncrytion = ldapGroup.getLdapServer().getPasswordEncryption().getTitle();

            MessageDigest md = MessageDigest.getInstance(passwordEncrytion);
            md.update(inPassword.getBytes(StandardCharsets.UTF_8));
            String encodedDigest = new String(Base64.encodeBase64(md.digest()), StandardCharsets.UTF_8);
            this.attributes.put("userPassword", "{" + passwordEncrytion + "}" + encodedDigest);
        }
    }

    private void prepareAttributes(LdapGroup ldapGroup, User user, String inUidNumber) {
        Attribute oc = new BasicAttribute("objectclass");
        StringTokenizer tokenizer = new StringTokenizer(ldapGroup.getObjectClasses(), ",");
        while (tokenizer.hasMoreTokens()) {
            oc.add(tokenizer.nextToken());
        }
        this.attributes.put(oc);

        this.attributes.put("uid", replaceVariables(ldapGroup.getUid(), user, inUidNumber));
        this.attributes.put("cn", replaceVariables(ldapGroup.getUid(), user, inUidNumber));
        this.attributes.put("displayName", replaceVariables(ldapGroup.getDisplayName(), user, inUidNumber));
        this.attributes.put("description", replaceVariables(ldapGroup.getDescription(), user, inUidNumber));
        this.attributes.put("gecos", replaceVariables(ldapGroup.getGecos(), user, inUidNumber));
        this.attributes.put("loginShell", replaceVariables(ldapGroup.getLoginShell(), user, inUidNumber));
        this.attributes.put("sn", replaceVariables(ldapGroup.getSn(), user, inUidNumber));
        this.attributes.put("homeDirectory", replaceVariables(ldapGroup.getHomeDirectory(), user, inUidNumber));

        this.attributes.put("sambaAcctFlags", replaceVariables(ldapGroup.getSambaAcctFlags(), user, inUidNumber));
        this.attributes.put("sambaLogonScript", replaceVariables(ldapGroup.getSambaLogonScript(), user, inUidNumber));
        this.attributes.put("sambaPrimaryGroupSID",
                replaceVariables(ldapGroup.getSambaPrimaryGroupSID(), user, inUidNumber));
        this.attributes.put("sambaSID", replaceVariables(ldapGroup.getSambaSID(), user, inUidNumber));

        this.attributes.put("sambaPwdMustChange",
                replaceVariables(ldapGroup.getSambaPwdMustChange(), user, inUidNumber));
        this.attributes.put("sambaPasswordHistory",
                replaceVariables(ldapGroup.getSambaPasswordHistory(), user, inUidNumber));
        this.attributes.put("sambaLogonHours", replaceVariables(ldapGroup.getSambaLogonHours(), user, inUidNumber));
        this.attributes.put("sambaKickoffTime", replaceVariables(ldapGroup.getSambaKickoffTime(), user, inUidNumber));
        this.attributes.put("sambaPwdLastSet", String.valueOf(System.currentTimeMillis() / 1000L));

        this.attributes.put("uidNumber", inUidNumber);
        this.attributes.put("gidNumber", replaceVariables(ldapGroup.getGidNumber(), user, inUidNumber));
    }

    /**
     * Replace Variables with current user details.
     *
     * @param inString
     *            String
     * @param inUser
     *            User object
     * @param inUidNumber
     *            String
     * @return String with replaced variables
     */
    private String replaceVariables(String inString, User inUser, String inUidNumber) {
        if (Objects.isNull(inString)) {
            return "";
        }
        String replaced = inString.replaceAll("\\{login\\}", inUser.getLogin());
        replaced = replaced.replaceAll("\\{user full name\\}", inUser.getName() + " " + inUser.getSurname());
        replaced = replaced.replaceAll("\\{uidnumber\\*2\\+1000\\}",
            String.valueOf(Integer.parseInt(inUidNumber) * 2 + 1000));
        replaced = replaced.replaceAll("\\{uidnumber\\*2\\+1001\\}",
            String.valueOf(Integer.parseInt(inUidNumber) * 2 + 1001));
        logger.debug("Replace instring: {} - {} - {}", inString, inUser, inUidNumber);
        logger.debug("replaced: {}", replaced);
        return replaced;
    }

    /**
     * Creates the LM Hash of the user's password.
     *
     * @param password
     *            The password.
     * @return The LM Hash of the given password, used in the calculation of the LM
     *         Response.
     */
    public static byte[] lmHash(String password) throws BadPaddingException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException {

        byte[] oemPassword = password.toUpperCase().getBytes(StandardCharsets.US_ASCII);
        int length = Math.min(oemPassword.length, 14);
        byte[] keyBytes = new byte[14];
        System.arraycopy(oemPassword, 0, keyBytes, 0, length);
        Key lowKey = createDESKey(keyBytes, 0);
        Key highKey = createDESKey(keyBytes, 7);
        byte[] magicConstant = "KGS!@#$%".getBytes(StandardCharsets.US_ASCII);
        Cipher des = Cipher.getInstance("DES/ECB/NoPadding");
        des.init(Cipher.ENCRYPT_MODE, lowKey);
        byte[] lowHash = des.doFinal(magicConstant);
        des.init(Cipher.ENCRYPT_MODE, highKey);
        byte[] highHash = des.doFinal(magicConstant);
        byte[] lmHash = new byte[16];
        System.arraycopy(lowHash, 0, lmHash, 0, 8);
        System.arraycopy(highHash, 0, lmHash, 8, 8);
        return lmHash;
    }

    /**
     * Creates a DES encryption key from the given key material.
     *
     * @param bytes
     *            A byte array containing the DES key material.
     * @param offset
     *            The offset in the given byte array at which the 7-byte key
     *            material starts.
     * @return A DES encryption key created from the key material starting at the
     *         specified offset in the given byte array.
     */
    private static Key createDESKey(byte[] bytes, int offset) {
        byte[] keyBytes = new byte[7];
        System.arraycopy(bytes, offset, keyBytes, 0, 7);
        byte[] material = new byte[8];
        material[0] = keyBytes[0];
        material[1] = (byte) (keyBytes[0] << 7 | (keyBytes[1] & 0xff) >>> 1);
        material[2] = (byte) (keyBytes[1] << 6 | (keyBytes[2] & 0xff) >>> 2);
        material[3] = (byte) (keyBytes[2] << 5 | (keyBytes[3] & 0xff) >>> 3);
        material[4] = (byte) (keyBytes[3] << 4 | (keyBytes[4] & 0xff) >>> 4);
        material[5] = (byte) (keyBytes[4] << 3 | (keyBytes[5] & 0xff) >>> 5);
        material[6] = (byte) (keyBytes[5] << 2 | (keyBytes[6] & 0xff) >>> 6);
        material[7] = (byte) (keyBytes[6] << 1);
        oddParity(material);
        return new SecretKeySpec(material, "DES");
    }

    /**
     * Applies odd parity to the given byte array.
     *
     * @param bytes
     *            The data whose parity bits are to be adjusted for odd parity.
     */
    private static void oddParity(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            boolean needsParity = (((b >>> 7) ^ (b >>> 6) ^ (b >>> 5) ^ (b >>> 4) ^ (b >>> 3) ^ (b >>> 2) ^ (b >>> 1))
                    & 0x01) == 0;
            if (needsParity) {
                bytes[i] |= (byte) 0x01;
            } else {
                bytes[i] &= (byte) 0xfe;
            }
        }
    }

    /**
     * To HEX String.
     *
     * @param bytes
     *            byte
     * @return String
     */
    public static String toHexString(byte[] bytes) {
        StringBuilder retString = new StringBuilder();
        for (byte aByte : bytes) {
            retString.append(Integer.toHexString(0x0100 + (aByte & 0x00FF)).substring(1));
        }
        return retString.toString().toUpperCase();
    }

    @Override
    public Attributes getAttributes(String name) throws NamingException {
        if (StringUtils.isBlank(name)) {
            throw new NameNotFoundException();
        }
        return (Attributes) this.attributes.clone();
    }

    @Override
    public Attributes getAttributes(Name name) throws NamingException {
        return getAttributes(name.toString());
    }

    @Override
    public Attributes getAttributes(String name, String[] ids) throws NamingException {
        if (StringUtils.isBlank(name)) {
            throw new NameNotFoundException();
        }

        Attributes answer = new BasicAttributes(true);
        Attribute target;
        for (String id : ids) {
            target = this.attributes.get(id);
            if (Objects.nonNull(target)) {
                answer.put(target);
            }
        }
        return answer;
    }

    @Override
    public Attributes getAttributes(Name name, String[] ids) throws NamingException {
        return getAttributes(name.toString(), ids);
    }

    @Override
    public String toString() {
        return this.ldapLogin;
    }

    // not used for this example

    @Override
    public Object lookup(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public Object lookup(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void bind(Name name, Object obj) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void bind(String name, Object obj) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void bind(Name name, Object obj, Attributes attrs) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void bind(String name, Object obj, Attributes attrs) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void rebind(Name name, Object obj) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void rebind(String name, Object obj) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void rebind(Name name, Object obj, Attributes attrs) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void rebind(String name, Object obj, Attributes attrs) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void unbind(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void unbind(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void rename(Name oldName, Name newName) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void rename(String oldName, String newName) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void destroySubcontext(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void destroySubcontext(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public Context createSubcontext(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public Context createSubcontext(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public DirContext createSubcontext(Name name, Attributes attrs) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public DirContext createSubcontext(String name, Attributes attrs) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public Object lookupLink(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public Object lookupLink(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NameParser getNameParser(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NameParser getNameParser(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public String composeName(String name, String prefix) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public Name composeName(Name name, Name prefix) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public Object removeFromEnvironment(String propName) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void close() throws NamingException {
        throw new OperationNotSupportedException();
    }

    // -- DirContext
    @Override
    public void modifyAttributes(Name name, int modOp, Attributes attrs) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void modifyAttributes(String name, int modOp, Attributes attrs) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void modifyAttributes(Name name, ModificationItem[] mods) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public void modifyAttributes(String name, ModificationItem[] mods) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public DirContext getSchema(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public DirContext getSchema(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public DirContext getSchemaClassDefinition(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public DirContext getSchemaClassDefinition(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes, String[] attributesToReturn)
            throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes,
            String[] attributesToReturn) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<SearchResult> search(Name name, String filter, SearchControls cons)
            throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<SearchResult> search(String name, String filter, SearchControls cons)
            throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<SearchResult> search(Name name, String filterExpr, Object[] filterArgs,
            SearchControls cons) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public NamingEnumeration<SearchResult> search(String name, String filterExpr, Object[] filterArgs,
            SearchControls cons) throws NamingException {
        throw new OperationNotSupportedException();
    }

    @Override
    public String getNameInNamespace() throws NamingException {
        throw new OperationNotSupportedException();
    }
}
