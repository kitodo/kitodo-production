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

package org.kitodo.production.services.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.digests.MD4Digest;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.LdapServer;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.PasswordEncryption;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.LdapServerDAO;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.ldap.LdapUser;
import org.kitodo.production.security.AESUtil;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.primefaces.model.SortOrder;

public class LdapServerService extends SearchDatabaseService<LdapServer, LdapServerDAO> {

    private static final Logger logger = LogManager.getLogger(LdapServerService.class);
    private static volatile LdapServerService instance = null;

    /**
     * Return singleton variable of type LdapServerService.
     *
     * @return unique instance of LdapServerService
     */
    public static LdapServerService getInstance() {
        LdapServerService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (LdapServerService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new LdapServerService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    private LdapServerService() {
        super(new LdapServerDAO());
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM LdapServer");
    }

    @Override
    public Long countResults(Map filters) throws DAOException {
        return countDatabaseRows();
    }

    @Override
    public List<LdapServer> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        return new ArrayList<>();
    }

    private String buildUserDN(User inUser) {
        String userDN = inUser.getLdapGroup().getUserDN();
        userDN = userDN.replaceAll("\\{login\\}", inUser.getLogin());
        if (Objects.nonNull(inUser.getLdapLogin())) {
            userDN = userDN.replaceAll("\\{ldaplogin\\}", inUser.getLdapLogin());
        }
        userDN = userDN.replaceAll("\\{firstname\\}", inUser.getName());
        userDN = userDN.replaceAll("\\{lastname\\}", inUser.getSurname());
        return userDN;
    }

    private Hashtable<String, String> initializeWithLdapConnectionSettings(LdapServer ldapServer) {
        Hashtable<String, String> env = new Hashtable<>(11);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapServer.getUrl());
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, ldapServer.getManagerLogin());

        String managerPassword = ldapServer.getManagerPassword();
        if (AESUtil.isEncrypted(managerPassword)) {
            String securitySecret = ConfigCore.getParameterOrDefaultValue(ParameterCore.SECURITY_SECRET_LDAPMANAGERPASSWORD);

            if (StringUtils.isBlank(securitySecret)) {
                logger.error("The security.secret.ldapManagerPassword parameter was not configured in kitodo_config.properties file.");
            }

            try {
                managerPassword = AESUtil.decrypt(managerPassword, securitySecret);
            } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException
                    | InvalidKeyException | BadPaddingException | IllegalBlockSizeException
                    | InvalidKeySpecException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
        env.put(Context.SECURITY_CREDENTIALS, managerPassword);

        if (ldapServer.isUseSsl()) {
            String keystorepath = ldapServer.getKeystore();
            String keystorepasswd = ldapServer.getKeystorePassword();

            // add all necessary certificates first
            loadCertificates(keystorepath, keystorepasswd, ldapServer);

            // set properties, so that the current keystore is used for SSL
            System.setProperty("javax.net.ssl.keyStore", keystorepath);
            System.setProperty("javax.net.ssl.trustStore", keystorepath);
            System.setProperty("javax.net.ssl.keyStorePassword", keystorepasswd);
            env.put(Context.SECURITY_PROTOCOL, "ssl");
        }
        return env;
    }

    /**
     * create new user in LDAP-directory.
     *
     * @param user
     *            User object
     * @param password
     *            String
     */
    public void createNewUser(User user, String password)
            throws NamingException, NoSuchAlgorithmException, IOException {

        if (Objects.isNull(user.getLdapGroup())) {
            Helper.setErrorMessage(Helper.getTranslation("noLdapGroupAssignedToUser"));
            return;
        }

        if (Objects.isNull(user.getLdapGroup().getLdapServer())) {
            Helper.setErrorMessage(Helper.getTranslation("noLdapServerAssignedToLdapGroup"));
            return;
        }

        if (!user.getLdapGroup().getLdapServer().isReadOnly()) {
            Hashtable<String, String> ldapEnvironment = initializeWithLdapConnectionSettings(
                user.getLdapGroup().getLdapServer());

            LdapUser ldapUser = new LdapUser();
            ldapUser.configure(user, password, getNextUidNumber(user.getLdapGroup().getLdapServer()));
            DirContext ctx = new InitialDirContext(ldapEnvironment);
            ctx.bind(buildUserDN(user), ldapUser);
            ctx.close();
            setNextUidNumber(user.getLdapGroup().getLdapServer());
            Helper.setMessage(
                Helper.getTranslation("ldapWritten") + " " + ServiceManager.getUserService().getFullName(user));
            /*
             * check if HomeDir exists, else create it
             */
            logger.debug("HomeVerzeichnis pruefen");

            URI homePath = getUserHomeDirectory(user);

            if (!new File(homePath).exists()) {
                logger.debug("HomeVerzeichnis existiert noch nicht");
                ServiceManager.getFileService().createDirectoryForUser(homePath, user.getLogin());
                logger.debug("HomeVerzeichnis angelegt");
            } else {
                logger.debug("HomeVerzeichnis existiert schon");
            }
        } else {
            Helper.setMessage("ldapIsReadOnly");
        }
    }

    /**
     * Check if connection with login and password possible.
     *
     * @param user
     *            User object
     * @param password
     *            String
     * @return Login correct or not
     */
    public boolean isUserPasswordCorrect(User user, String password) {
        logger.debug("start login session with ldap");
        Hashtable<String, String> env = initializeWithLdapConnectionSettings(user.getLdapGroup().getLdapServer());

        // Start TLS
        if (ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.LDAP_USE_TLS)) {
            logger.debug("use TLS for auth");
            return isPasswordCorrectForAuthWithTLS(env, user, password);
        } else {
            logger.debug("don't use TLS for auth");
            return isPasswordCorrectForAuthWithoutTLS(env, user, password);
        }
    }

    /**
     * Retrieve home directory of given user.
     *
     * @param user
     *            User object
     * @return path as URI
     */
    public URI getUserHomeDirectory(User user) {
        String userFolderBasePath = ConfigCore.getParameter(ParameterCore.DIR_USERS);

        if (ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.LDAP_USE_LOCAL_DIRECTORY)) {
            return Paths.get(userFolderBasePath, user.getLogin()).toUri();
        }
        Hashtable<String, String> env = initializeWithLdapConnectionSettings(user.getLdapGroup().getLdapServer());
        if (ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.LDAP_USE_TLS)) {
            return getUserHomeDirectoryWithTLS(env, userFolderBasePath, user);
        }

        if (ConfigCore.getBooleanParameter(ParameterCore.LDAP_USE_SIMPLE_AUTH, false)) {
            env.put(Context.SECURITY_AUTHENTICATION, "none");
        }
        DirContext ctx;
        URI userFolderPath = null;
        try {
            ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(buildUserDN(user));
            Attribute ldapAttribute = attrs.get("homeDirectory");
            userFolderPath = URI.create((String) ldapAttribute.get(0));
            ctx.close();
        } catch (NamingException e) {
            logger.error(e.getMessage(), e);
        }

        if (Objects.nonNull(userFolderPath) && !userFolderPath.isAbsolute()) {
            if (userFolderPath.getPath().startsWith("/")) {
                userFolderPath = ServiceManager.getFileService().deleteFirstSlashFromPath(userFolderPath);
            }
            return Paths.get(userFolderBasePath, userFolderPath.getRawPath()).toUri();
        } else {
            return userFolderPath;
        }
    }

    /**
     * Check if User already exists on system.
     *
     * @param user
     *            The User.
     * @return whether the user already exists
     */
    public boolean isUserAlreadyExists(User user) {
        Hashtable<String, String> ldapEnvironment = initializeWithLdapConnectionSettings(
            user.getLdapGroup().getLdapServer());
        DirContext ctx;
        boolean userAlreadyExisting = false;
        try {
            ctx = new InitialDirContext(ldapEnvironment);
            Attributes matchAttrs = new BasicAttributes(true);
            NamingEnumeration<SearchResult> answer = ctx.search(buildUserDN(user), matchAttrs);
            userAlreadyExisting = answer.hasMoreElements();

            while (answer.hasMore()) {
                SearchResult sr = answer.next();
                logger.debug(">>>{}", sr.getName());
                Attributes attrs = sr.getAttributes();
                String givenName = getStringForAttribute(attrs, "givenName");
                String surName = getStringForAttribute(attrs, "sn");
                String mail = getStringForAttribute(attrs, "mail");
                String cn = getStringForAttribute(attrs, "cn");
                String homeDirectory = getStringForAttribute(attrs, "homeDirectory");

                logger.debug(givenName);
                logger.debug(surName);
                logger.debug(mail);
                logger.debug(cn);
                logger.debug(homeDirectory);
            }

            ctx.close();
        } catch (NamingException e) {
            logger.error(e.getMessage(), e);
        }
        return userAlreadyExisting;
    }

    private String getStringForAttribute(Attributes attrs, String identifier) {
        try {
            return attrs.get(identifier).toString();
        } catch (RuntimeException e) {
            return " ";
        }
    }

    /**
     * Get next free uidNumber.
     *
     * @return next free uidNumber
     */
    private String getNextUidNumber(LdapServer ldapServer) {
        Hashtable<String, String> ldapEnvironment = initializeWithLdapConnectionSettings(ldapServer);
        DirContext ctx;
        String rueckgabe = "";
        try {
            ctx = new InitialDirContext(ldapEnvironment);
            Attributes attrs = ctx.getAttributes(ldapServer.getNextFreeUnixIdPattern());
            Attribute la = attrs.get("uidNumber");
            rueckgabe = (String) la.get(0);
            ctx.close();
        } catch (NamingException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return rueckgabe;
    }

    /**
     * Set next free uidNumber.
     */
    private void setNextUidNumber(LdapServer ldapServer) {
        Hashtable<String, String> ldapEnvironment = initializeWithLdapConnectionSettings(ldapServer);
        DirContext ctx;

        try {
            ctx = new InitialDirContext(ldapEnvironment);
            Attributes attrs = ctx.getAttributes(ldapServer.getNextFreeUnixIdPattern());
            Attribute la = attrs.get("uidNumber");
            String oldValue = (String) la.get(0);
            int bla = Integer.parseInt(oldValue) + 1;

            BasicAttribute attrNeu = new BasicAttribute("uidNumber", String.valueOf(bla));
            ModificationItem[] mods = new ModificationItem[1];
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attrNeu);
            ctx.modifyAttributes(ldapServer.getNextFreeUnixIdPattern(), mods);

            ctx.close();
        } catch (NamingException e) {
            logger.error(e.getMessage(), e);
        }

    }

    /**
     * change password of given user, needs old password for authentication.
     *
     * @param user
     *            User object
     * @param inNewPassword
     *            String
     * @return boolean about result of change
     */
    public boolean changeUserPassword(User user, String inNewPassword) throws NoSuchAlgorithmException {
        MD4Digest digester = new MD4Digest();
        PasswordEncryption passwordEncryption = user.getLdapGroup().getLdapServer().getPasswordEncryption();
        Hashtable<String, String> env = initializeWithLdapConnectionSettings(user.getLdapGroup().getLdapServer());
        if (!user.getLdapGroup().getLdapServer().isReadOnly()) {
            try {
                ModificationItem[] mods = new ModificationItem[4];

                // encryption of password and Base64-Encoding
                MessageDigest md = MessageDigest.getInstance(passwordEncryption.getTitle());
                md.update(inNewPassword.getBytes(StandardCharsets.UTF_8));
                String encryptedPassword = new String(Base64.encodeBase64(md.digest()), StandardCharsets.UTF_8);

                // change attribute userPassword
                BasicAttribute userPassword = new BasicAttribute("userPassword",
                        "{" + passwordEncryption + "}" + encryptedPassword);
                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, userPassword);

                // change attribute lanmgrPassword
                BasicAttribute lanmgrPassword = proceedPassword("sambaLMPassword", inNewPassword, null);
                mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, lanmgrPassword);

                // change attribute ntlmPassword
                BasicAttribute ntlmPassword = proceedPassword("sambaNTPassword", inNewPassword, digester);
                mods[2] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, ntlmPassword);

                BasicAttribute sambaPwdLastSet = new BasicAttribute("sambaPwdLastSet",
                        String.valueOf(System.currentTimeMillis() / 1000L));
                mods[3] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, sambaPwdLastSet);

                DirContext ctx = new InitialDirContext(env);
                ctx.modifyAttributes(buildUserDN(user), mods);

                // Close the context when we're done
                ctx.close();
                return true;
            } catch (NamingException e) {
                logger.debug("Benutzeranmeldung nicht korrekt oder Passwortänderung nicht möglich", e);
                return false;
            }
        }
        return false;
    }

    private URI getUserHomeDirectoryWithTLS(Hashtable<String, String> env, String userFolderBasePath, User user) {
        env.put("java.naming.ldap.version", "3");
        LdapContext ctx = null;
        StartTlsResponse tls = null;
        try {
            ctx = new InitialLdapContext(env, null);

            // Authentication must be performed over a secure channel
            tls = (StartTlsResponse) ctx.extendedOperation(new StartTlsRequest());
            tls.negotiate();

            ctx.reconnect(null);

            Attributes attrs = ctx.getAttributes(buildUserDN(user));
            Attribute la = attrs.get("homeDirectory");
            return URI.create((String) la.get(0));
        } catch (IOException e) {
            logger.error("TLS negotiation error:", e);
            return Paths.get(userFolderBasePath, user.getLogin()).toUri();
        } catch (NamingException e) {
            logger.error("JNDI error:", e);
            return Paths.get(userFolderBasePath, user.getLogin()).toUri();
        } finally {
            closeConnections(ctx, tls);
        }
    }

    private boolean isPasswordCorrectForAuthWithTLS(Hashtable<String, String> env, User user, String password) {
        env.put("java.naming.ldap.version", "3");
        LdapContext ctx = null;
        StartTlsResponse tls = null;
        try {
            ctx = new InitialLdapContext(env, null);

            // Authentication must be performed over a secure channel
            tls = (StartTlsResponse) ctx.extendedOperation(new StartTlsRequest());
            tls.negotiate();

            // Authenticate via SASL EXTERNAL mechanism using client X.509
            // certificate contained in JVM keystore
            ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
            ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, buildUserDN(user));
            ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
            ctx.reconnect(null);
            return true;
            // perform search for privileged attributes under authenticated context
        } catch (IOException e) {
            logger.error("TLS negotiation error:", e);
            return false;
        } catch (NamingException e) {
            logger.error("JNDI error:", e);
            return false;
        } finally {
            closeConnections(ctx, tls);
        }
    }

    private boolean isPasswordCorrectForAuthWithoutTLS(Hashtable<String, String> env, User user, String password) {
        if (ConfigCore.getBooleanParameter(ParameterCore.LDAP_USE_SIMPLE_AUTH, false)) {
            env.put(Context.SECURITY_AUTHENTICATION, "none");
            // TODO: test for password
        } else {
            env.put(Context.SECURITY_PRINCIPAL, buildUserDN(user));
            env.put(Context.SECURITY_CREDENTIALS, password);
        }
        logger.debug("ldap environment set");

        try {
            logger.debug("start classic ldap authentication");
            logger.debug("user DN is {}", buildUserDN(user));

            if (Objects.isNull(ConfigCore.getParameter(ParameterCore.LDAP_ATTRIBUTE_TO_TEST))) {
                logger.debug("ldap attribute to test is null");
                DirContext ctx = new InitialDirContext(env);
                ctx.close();
                return true;
            } else {
                logger.debug("ldap attribute to test is not null");
                DirContext ctx = new InitialDirContext(env);

                Attributes attrs = ctx.getAttributes(buildUserDN(user));
                Attribute la = attrs.get(ConfigCore.getParameter(ParameterCore.LDAP_ATTRIBUTE_TO_TEST));
                logger.debug("ldap attributes set");
                String test = (String) la.get(0);
                if (test.equals(ConfigCore.getParameter(ParameterCore.LDAP_VALUE_OF_ATTRIBUTE))) {
                    logger.debug("ldap ok");
                    ctx.close();
                    return true;
                } else {
                    logger.debug("ldap not ok");
                    ctx.close();
                    return false;
                }
            }
        } catch (NamingException e) {
            logger.debug("login not allowed for {}. Exception: {}", user.getLogin(), e);
            return false;
        }
    }

    private void closeConnections(LdapContext ctx, StartTlsResponse tls) {
        if (Objects.nonNull(tls)) {
            try {
                // Tear down TLS connection
                tls.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (Objects.nonNull(ctx)) {
            try {
                // Close LDAP connection
                ctx.close();
            } catch (NamingException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private BasicAttribute proceedPassword(String identifier, String newPassword, MD4Digest digester) {
        try {
            byte[] hash;
            if (Objects.isNull(digester)) {
                hash = LdapUser.lmHash(newPassword);
            } else {
                byte[] unicodePassword = newPassword.getBytes(StandardCharsets.UTF_16LE);
                hash = new byte[digester.getDigestSize()];
                digester.update(unicodePassword, 0, unicodePassword.length);
                digester.doFinal(hash, 0);
            }
            return new BasicAttribute(identifier, LdapUser.toHexString(hash));
            // TODO: Don't catch super class exception, make sure that
            // the password isn't logged here
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
                | BadPaddingException | RuntimeException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    // TODO test if this methods works
    private void loadCertificates(String path, String passwd, LdapServer ldapServer) {
        /* wenn die Zertifikate noch nicht im Keystore sind, jetzt einlesen */
        File myPfad = new File(path);
        if (!myPfad.exists()) {
            try (FileOutputStream ksos = (FileOutputStream) ServiceManager.getFileService().write(myPfad.toURI());
                    // TODO: Rename parameters to something more meaningful,
                    // this is quite specific for the GDZ
                    FileInputStream cacertFile = new FileInputStream(ldapServer.getRootCertificate());
                    FileInputStream certFile2 = new FileInputStream(ldapServer.getPdcCertificate())) {

                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate cacert = (X509Certificate) cf.generateCertificate(cacertFile);
                X509Certificate servercert = (X509Certificate) cf.generateCertificate(certFile2);

                KeyStore ks = KeyStore.getInstance("jks");
                char[] password = passwd.toCharArray();

                // TODO: Let this method really load a keystore if configured
                // initialize the keystore, if file is available, load the
                // keystore
                ks.load(null);

                ks.setCertificateEntry("ROOTCERT", cacert);
                ks.setCertificateEntry("PDC", servercert);
                ks.store(ksos, password);
            } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException
                    | RuntimeException e) {
                logger.error(e.getMessage(), e);
            }

        }
    }
}
