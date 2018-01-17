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

package de.sub.goobi.helper.ldap;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Hashtable;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.JDKMessageDigest.MD4;
import org.kitodo.data.database.beans.User;
import org.kitodo.services.ServiceManager;

public class Ldap {
    private static final Logger logger = LogManager.getLogger(Ldap.class);
    private final ServiceManager serviceManager = new ServiceManager();

    public Ldap() {

    }

    private String getUserDN(User inUser) {
        String userDN = inUser.getLdapGroup().getUserDN();
        userDN = userDN.replaceAll("\\{login\\}", inUser.getLogin());
        if (inUser.getLdapLogin() != null) {
            userDN = userDN.replaceAll("\\{ldaplogin\\}", inUser.getLdapLogin());
        }
        userDN = userDN.replaceAll("\\{firstname\\}", inUser.getName());
        userDN = userDN.replaceAll("\\{lastname\\}", inUser.getSurname());
        return userDN;
    }

    /**
     * create new user in LDAP-directory.
     *
     * @param inBenutzer
     *            User object
     * @param inPasswort
     *            String
     */
    public void createNewUser(User inBenutzer, String inPasswort)
            throws NamingException, NoSuchAlgorithmException, IOException {

        if (!ConfigCore.getBooleanParameter("ldap_readonly", false)) {
            Hashtable<String, String> env = getLdapConnectionSettings();
            env.put(Context.SECURITY_PRINCIPAL, ConfigCore.getParameter("ldap_adminLogin"));
            env.put(Context.SECURITY_CREDENTIALS, ConfigCore.getParameter("ldap_adminPassword"));

            LdapUser dr = new LdapUser();
            dr.configure(inBenutzer, inPasswort, getNextUidNumber());
            DirContext ctx = new InitialDirContext(env);
            ctx.bind(getUserDN(inBenutzer), dr);
            ctx.close();
            setNextUidNumber();
            Helper.setMeldung(null, Helper.getTranslation("ldapWritten") + " "
                    + serviceManager.getUserService().getFullName(inBenutzer), "");
            /*
             * check if HomeDir exists, else create it
             */
            logger.debug("HomeVerzeichnis pruefen");
            URI homePath = URI.create(getUserHomeDirectory(inBenutzer));
            if (!new File(homePath).exists()) {
                logger.debug("HomeVerzeichnis existiert noch nicht");
                serviceManager.getFileService().createDirectoryForUser(homePath, inBenutzer.getLogin());
                logger.debug("HomeVerzeichnis angelegt");
            } else {
                logger.debug("HomeVerzeichnis existiert schon");
            }
        } else {
            Helper.setMeldung(Helper.getTranslation("ldapIsReadOnly"));
        }
    }

    /**
     * Check if connection with login and password possible.
     *
     * @param inBenutzer
     *            User object
     * @param inPasswort
     *            String
     * @return Login correct or not
     */
    public boolean isUserPasswordCorrect(User inBenutzer, String inPasswort) {
        logger.debug("start login session with ldap");
        Hashtable<String, String> env = getLdapConnectionSettings();

        // Start TLS
        if (ConfigCore.getBooleanParameter("ldap_useTLS", false)) {
            logger.debug("use TLS for auth");
            env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, ConfigCore.getParameter("ldap_url"));
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
                ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, getUserDN(inBenutzer));
                ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, inPasswort);
                ctx.reconnect(null);
                return true;
                // Perform search for privileged attributes under authenticated
                // context

            } catch (IOException e) {
                logger.error("TLS negotiation error:", e);
                return false;
            } catch (NamingException e) {
                logger.error("JNDI error:", e);
                return false;
            } finally {
                if (tls != null) {
                    try {
                        // Tear down TLS connection
                        tls.close();
                    } catch (IOException e) {
                        logger.error(e);
                    }
                }
                if (ctx != null) {
                    try {
                        // Close LDAP connection
                        ctx.close();
                    } catch (NamingException e) {
                        logger.error(e);
                    }
                }
            }
        } else {
            logger.debug("don't use TLS for auth");
            if (ConfigCore.getBooleanParameter("useSimpleAuthentification", false)) {
                env.put(Context.SECURITY_AUTHENTICATION, "none");
                // TODO auf passwort testen
            } else {
                env.put(Context.SECURITY_PRINCIPAL, getUserDN(inBenutzer));
                env.put(Context.SECURITY_CREDENTIALS, inPasswort);
            }
            logger.debug("ldap environment set");

            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("start classic ldap authentification");
                    logger.debug("user DN is " + getUserDN(inBenutzer));
                }

                if (ConfigCore.getParameter("ldap_AttributeToTest") == null) {
                    logger.debug("ldap attribute to test is null");
                    DirContext ctx = new InitialDirContext(env);
                    ctx.close();
                    return true;
                } else {
                    logger.debug("ldap attribute to test is not null");
                    DirContext ctx = new InitialDirContext(env);

                    Attributes attrs = ctx.getAttributes(getUserDN(inBenutzer));
                    Attribute la = attrs.get(ConfigCore.getParameter("ldap_AttributeToTest"));
                    logger.debug("ldap attributes set");
                    String test = (String) la.get(0);
                    if (test.equals(ConfigCore.getParameter("ldap_ValueOfAttribute"))) {
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
                if (logger.isDebugEnabled()) {
                    logger.debug("login not allowed for " + inBenutzer.getLogin(), e);
                }
                return false;
            }
        }
    }

    /**
     * retrieve home directory of given user.
     *
     * @param inBenutzer
     *            User object
     * @return path as string
     */
    public String getUserHomeDirectory(User inBenutzer) {
        if (ConfigCore.getBooleanParameter("useLocalDirectory", false)) {
            return ConfigCore.getParameter("dir_Users") + inBenutzer.getLogin();
        }
        Hashtable<String, String> env = getLdapConnectionSettings();
        if (ConfigCore.getBooleanParameter("ldap_useTLS", false)) {

            env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, ConfigCore.getParameter("ldap_url"));
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
                ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, ConfigCore.getParameter("ldap_adminLogin"));
                ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, ConfigCore.getParameter("ldap_adminPassword"));

                ctx.reconnect(null);

                Attributes attrs = ctx.getAttributes(getUserDN(inBenutzer));
                Attribute la = attrs.get("homeDirectory");
                return (String) la.get(0);

                // Perform search for privileged attributes under authenticated
                // context

            } catch (IOException e) {
                logger.error("TLS negotiation error:", e);

                return ConfigCore.getParameter("dir_Users") + inBenutzer.getLogin();
            } catch (NamingException e) {

                logger.error("JNDI error:", e);

                return ConfigCore.getParameter("dir_Users") + inBenutzer.getLogin();
            } finally {
                if (tls != null) {
                    try {
                        // Tear down TLS connection
                        tls.close();
                    } catch (IOException e) {
                        logger.error(e);
                    }
                }
                if (ctx != null) {
                    try {
                        // Close LDAP connection
                        ctx.close();
                    } catch (NamingException e) {
                        logger.error(e);
                    }
                }
            }
        } else if (ConfigCore.getBooleanParameter("useSimpleAuthentification", false)) {
            env.put(Context.SECURITY_AUTHENTICATION, "none");
        } else {
            env.put(Context.SECURITY_PRINCIPAL, ConfigCore.getParameter("ldap_adminLogin"));
            env.put(Context.SECURITY_CREDENTIALS, ConfigCore.getParameter("ldap_adminPassword"));

        }
        DirContext ctx;
        String rueckgabe = "";
        try {
            ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(getUserDN(inBenutzer));
            Attribute la = attrs.get("homeDirectory");
            rueckgabe = (String) la.get(0);
            ctx.close();
        } catch (NamingException e) {
            logger.error(e);
        }
        return rueckgabe;
    }

    /**
     * check if User already exists on system.
     *
     * @param inLogin
     *            String
     * @return path as string
     */
    public boolean isUserAlreadyExists(String inLogin) {
        Hashtable<String, String> env = getLdapConnectionSettings();
        env.put(Context.SECURITY_PRINCIPAL, ConfigCore.getParameter("ldap_adminLogin"));
        env.put(Context.SECURITY_CREDENTIALS, ConfigCore.getParameter("ldap_adminPassword"));
        DirContext ctx;
        boolean rueckgabe = false;
        try {
            ctx = new InitialDirContext(env);
            Attributes matchAttrs = new BasicAttributes(true);
            NamingEnumeration<SearchResult> answer = ctx.search("ou=users,dc=gdz,dc=sub,dc=uni-goettingen,dc=de",
                    matchAttrs);
            rueckgabe = answer.hasMoreElements();

            while (answer.hasMore()) {
                SearchResult sr = answer.next();
                if (logger.isDebugEnabled()) {
                    logger.debug(">>>" + sr.getName());
                }
                Attributes attrs = sr.getAttributes();
                String givenName = " ";
                String surName = " ";
                String mail = " ";
                String cn = " ";
                String hd = " ";
                try {
                    givenName = attrs.get("givenName").toString();
                } catch (Exception err) {
                    givenName = " ";
                }
                try {
                    surName = attrs.get("sn").toString();
                } catch (Exception e2) {
                    surName = " ";
                }
                try {
                    mail = attrs.get("mail").toString();
                } catch (Exception e3) {
                    mail = " ";
                }
                try {
                    cn = attrs.get("cn").toString();
                } catch (Exception e4) {
                    cn = " ";
                }
                try {
                    hd = attrs.get("homeDirectory").toString();
                } catch (Exception e4) {
                    hd = " ";
                }
                logger.debug(givenName);
                logger.debug(surName);
                logger.debug(mail);
                logger.debug(cn);
                logger.debug(hd);

            }

            ctx.close();
        } catch (NamingException e) {
            logger.error(e);
        }
        return rueckgabe;
    }

    /**
     * Get next free uidNumber.
     *
     * @return next free uidNumber
     */
    private String getNextUidNumber() {
        Hashtable<String, String> env = getLdapConnectionSettings();
        env.put(Context.SECURITY_PRINCIPAL, ConfigCore.getParameter("ldap_adminLogin"));
        env.put(Context.SECURITY_CREDENTIALS, ConfigCore.getParameter("ldap_adminPassword"));
        DirContext ctx;
        String rueckgabe = "";
        try {
            ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(ConfigCore.getParameter("ldap_nextFreeUnixId"));
            Attribute la = attrs.get("uidNumber");
            rueckgabe = (String) la.get(0);
            ctx.close();
        } catch (NamingException e) {
            logger.error(e);
            Helper.setFehlerMeldung(e.getMessage());
        }
        return rueckgabe;
    }

    /**
     * Set next free uidNumber.
     */
    private void setNextUidNumber() {
        Hashtable<String, String> env = getLdapConnectionSettings();
        env.put(Context.SECURITY_PRINCIPAL, ConfigCore.getParameter("ldap_adminLogin"));
        env.put(Context.SECURITY_CREDENTIALS, ConfigCore.getParameter("ldap_adminPassword"));
        DirContext ctx;

        try {
            ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(ConfigCore.getParameter("ldap_nextFreeUnixId"));
            Attribute la = attrs.get("uidNumber");
            String oldValue = (String) la.get(0);
            int bla = Integer.parseInt(oldValue) + 1;

            BasicAttribute attrNeu = new BasicAttribute("uidNumber", String.valueOf(bla));
            ModificationItem[] mods = new ModificationItem[1];
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attrNeu);
            ctx.modifyAttributes(ConfigCore.getParameter("ldap_nextFreeUnixId"), mods);

            ctx.close();
        } catch (NamingException e) {
            logger.error(e);
        }

    }

    /**
     * change password of given user, needs old password for authentication.
     *
     * @param inUser
     *            User object
     * @param inOldPassword
     *            String
     * @param inNewPassword
     *            String
     * @return boolean about result of change
     */
    public boolean changeUserPassword(User inUser, String inOldPassword, String inNewPassword)
            throws NoSuchAlgorithmException {
        MD4 digester = new MD4();
        Hashtable<String, String> env = getLdapConnectionSettings();
        if (!ConfigCore.getBooleanParameter("ldap_readonly", false)) {
            env.put(Context.SECURITY_PRINCIPAL, ConfigCore.getParameter("ldap_adminLogin"));
            env.put(Context.SECURITY_CREDENTIALS, ConfigCore.getParameter("ldap_adminPassword"));

            try {
                DirContext ctx = new InitialDirContext(env);

                /*
                 * Encryption of password and Base64-Encoding
                 */
                MessageDigest md = MessageDigest.getInstance(ConfigCore.getParameter("ldap_encryption", "SHA"));
                md.update(inNewPassword.getBytes(StandardCharsets.UTF_8));
                String digestBase64 = new String(Base64.encodeBase64(md.digest()), StandardCharsets.UTF_8);
                ModificationItem[] mods = new ModificationItem[4];

                /*
                 * UserPasswort-Attribut ändern
                 */
                BasicAttribute userpassword = new BasicAttribute("userPassword",
                        "{" + ConfigCore.getParameter("ldap_encryption", "SHA") + "}" + digestBase64);

                /*
                 * LanMgr-Passwort-Attribut ändern
                 */
                BasicAttribute lanmgrpassword = null;
                try {
                    lanmgrpassword = new BasicAttribute("sambaLMPassword",
                            LdapUser.toHexString(LdapUser.lmHash(inNewPassword)));
                    // TODO: Don't catch super class exception, make sure that
                    // the password isn't logged here
                } catch (Exception e) {
                    logger.error(e);
                }

                /*
                 * NTLM-Passwort-Attribut ändern
                 */
                BasicAttribute ntlmpassword = null;
                try {
                    byte[] hmm = digester.digest(inNewPassword.getBytes("UnicodeLittleUnmarked"));
                    ntlmpassword = new BasicAttribute("sambaNTPassword", LdapUser.toHexString(hmm));
                } catch (UnsupportedEncodingException e) {
                    // TODO: Make sure that the password isn't logged here
                    logger.error(e);
                }

                BasicAttribute sambaPwdLastSet = new BasicAttribute("sambaPwdLastSet",
                        String.valueOf(System.currentTimeMillis() / 1000L));

                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, userpassword);
                mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, lanmgrpassword);
                mods[2] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, ntlmpassword);
                mods[3] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, sambaPwdLastSet);
                ctx.modifyAttributes(getUserDN(inUser), mods);

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

    private Hashtable<String, String> getLdapConnectionSettings() {
        // Set up environment for creating initial context
        Hashtable<String, String> env = new Hashtable<>(11);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ConfigCore.getParameter("ldap_url"));
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        /* wenn die Verbindung über ssl laufen soll */
        if (ConfigCore.getBooleanParameter("ldap_sslconnection")) {
            String keystorepath = ConfigCore.getParameter("ldap_keystore");
            String keystorepasswd = ConfigCore.getParameter("ldap_keystore_password");

            // add all necessary certificates first
            loadCertificates(keystorepath, keystorepasswd);

            // set properties, so that the current keystore is used for SSL
            System.setProperty("javax.net.ssl.keyStore", keystorepath);
            System.setProperty("javax.net.ssl.trustStore", keystorepath);
            System.setProperty("javax.net.ssl.keyStorePassword", keystorepasswd);
            env.put(Context.SECURITY_PROTOCOL, "ssl");
        }
        return env;
    }

    private void loadCertificates(String path, String passwd) {
        /* wenn die Zertifikate noch nicht im Keystore sind, jetzt einlesen */
        File myPfad = new File(path);
        if (!myPfad.exists()) {
            try (FileOutputStream ksos = (FileOutputStream) serviceManager.getFileService().write(myPfad.toURI());
                    // TODO: Rename parameters to something more meaningful,
                    // this is quite specific for the GDZ
                    FileInputStream cacertFile = new FileInputStream(ConfigCore.getParameter("ldap_cert_root"));
                    FileInputStream certFile2 = new FileInputStream(ConfigCore.getParameter("ldap_cert_pdc"))) {

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
            } catch (Exception e) {
                logger.error(e);
            }

        }
    }

}
