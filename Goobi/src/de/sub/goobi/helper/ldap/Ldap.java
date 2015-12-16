package de.sub.goobi.helper.ldap;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

import edu.sysu.virgoftp.ftp.encrypt.MD4;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;

public class Ldap {
	private static final Logger myLogger = Logger.getLogger(Ldap.class);

	public Ldap() {

	}

	private String getUserDN(Benutzer inBenutzer) {
		String userDN = inBenutzer.getLdapGruppe().getUserDN();
		userDN = userDN.replaceAll("\\{login\\}", inBenutzer.getLogin());
		if (inBenutzer.getLdaplogin() != null) {
			userDN = userDN.replaceAll("\\{ldaplogin\\}", inBenutzer.getLdaplogin());
		}
		userDN = userDN.replaceAll("\\{firstname\\}", inBenutzer.getVorname());
		userDN = userDN.replaceAll("\\{lastname\\}", inBenutzer.getNachname());
		return userDN;
	}

	/**
	 * create new user in LDAP-directory
	 * 
	 * @param inBenutzer
	 * @param inPasswort
	 * @throws NamingException
	 * @throws NoSuchAlgorithmException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void createNewUser(Benutzer inBenutzer, String inPasswort) throws NamingException, NoSuchAlgorithmException, IOException,
			InterruptedException {

		if (!ConfigMain.getBooleanParameter("ldap_readonly", false)) {
			Hashtable<String, String> env = LdapConnectionSettings();
			env.put(Context.SECURITY_PRINCIPAL, ConfigMain.getParameter("ldap_adminLogin"));
			env.put(Context.SECURITY_CREDENTIALS, ConfigMain.getParameter("ldap_adminPassword"));

			LdapUser dr = new LdapUser();
			dr.configure(inBenutzer, inPasswort, getNextUidNumber());
			DirContext ctx = new InitialDirContext(env);
			ctx.bind(getUserDN(inBenutzer), dr);
			ctx.close();
			setNextUidNumber();
			Helper.setMeldung(null, Helper.getTranslation("ldapWritten") + " " + inBenutzer.getNachVorname(), "");
			/*
			 * -------------------------------- check if HomeDir exists, else create it --------------------------------
			 */
			myLogger.debug("HomeVerzeichnis pruefen");
			String homePath = getUserHomeDirectory(inBenutzer);
			if (!new File(homePath).exists()) {
				myLogger.debug("HomeVerzeichnis existiert noch nicht");
                FilesystemHelper.createDirectoryForUser(homePath, inBenutzer.getLogin());
                myLogger.debug("HomeVerzeichnis angelegt");
			} else {
				myLogger.debug("HomeVerzeichnis existiert schon");
			}
		} else {
			Helper.setMeldung(Helper.getTranslation("ldapIsReadOnly"));
		}
	}

	/**
	 * Check if connection with login and password possible
	 * 
	 * @param inBenutzer
	 * @param inPasswort
	 * @return Login correct or not
	 */
	public boolean isUserPasswordCorrect(Benutzer inBenutzer, String inPasswort) {
		myLogger.debug("start login session with ldap");
		Hashtable<String, String> env = LdapConnectionSettings();

		// Start TLS
		if (ConfigMain.getBooleanParameter("ldap_useTLS", false)) {
			myLogger.debug("use TLS for auth");
			env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.PROVIDER_URL, ConfigMain.getParameter("ldap_url"));
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
				// Perform search for privileged attributes under authenticated context

			} catch (IOException e) {
				myLogger.error("TLS negotiation error:", e);
				return false;
			} catch (NamingException e) {
				myLogger.error("JNDI error:", e);
				return false;
			} finally {
				if (tls != null) {
					try {
						// Tear down TLS connection
						tls.close();
					} catch (IOException e) {
					}
				}
				if (ctx != null) {
					try {
						// Close LDAP connection
						ctx.close();
					} catch (NamingException e) {
					}
				}
			}
		} else {
			myLogger.debug("don't use TLS for auth");
			if (ConfigMain.getBooleanParameter("useSimpleAuthentification", false)) {
				env.put(Context.SECURITY_AUTHENTICATION, "none");
				// TODO auf passwort testen
			} else {
				env.put(Context.SECURITY_PRINCIPAL, getUserDN(inBenutzer));
				env.put(Context.SECURITY_CREDENTIALS, inPasswort);
			}
			myLogger.debug("ldap environment set");

			try {
				myLogger.debug("start classic ldap authentification");
				myLogger.debug("user DN is " + getUserDN(inBenutzer));

				if (ConfigMain.getParameter("ldap_AttributeToTest") == null) {
					myLogger.debug("ldap attribute to test is null");
					new InitialDirContext(env);
					return true;
				} else {
					myLogger.debug("ldap attribute to test is not null");
					DirContext ctx = new InitialDirContext(env);

					Attributes attrs = ctx.getAttributes(getUserDN(inBenutzer));
					Attribute la = attrs.get(ConfigMain.getParameter("ldap_AttributeToTest"));
					myLogger.debug("ldap attributes set");
					String test = (String) la.get(0);
					if (test.equals(ConfigMain.getParameter("ldap_ValueOfAttribute"))) {
						myLogger.debug("ldap ok");
						ctx.close();
						return true;
					} else {
						myLogger.debug("ldap not ok");
						ctx.close();
						return false;
					}
				}
			} catch (NamingException e) {
				myLogger.debug("login not allowed for " + inBenutzer.getLogin(), e);
				return false;
			}
		}
	}

	/**
	 * retrieve home directory of given user
	 * 
	 * @param inBenutzer
	 * @return path as string
	 */
	public String getUserHomeDirectory(Benutzer inBenutzer) {
		if (ConfigMain.getBooleanParameter("useLocalDirectory", false)) {
			return ConfigMain.getParameter("dir_Users") + inBenutzer.getLogin();
		}
		Hashtable<String, String> env = LdapConnectionSettings();
		if (ConfigMain.getBooleanParameter("ldap_useTLS", false)) {

			env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.PROVIDER_URL, ConfigMain.getParameter("ldap_url"));
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
				ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, ConfigMain.getParameter("ldap_adminLogin"));
				ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, ConfigMain.getParameter("ldap_adminPassword"));

				ctx.reconnect(null);

				Attributes attrs = ctx.getAttributes(getUserDN(inBenutzer));
				Attribute la = attrs.get("homeDirectory");
				return (String) la.get(0);

				// Perform search for privileged attributes under authenticated context

			} catch (IOException e) {
				myLogger.error("TLS negotiation error:", e);

				return ConfigMain.getParameter("dir_Users") + inBenutzer.getLogin();
			} catch (NamingException e) {

				myLogger.error("JNDI error:", e);

				return ConfigMain.getParameter("dir_Users") + inBenutzer.getLogin();
			} finally {
				if (tls != null) {
					try {
						// Tear down TLS connection
						tls.close();
					} catch (IOException e) {
					}
				}
				if (ctx != null) {
					try {
						// Close LDAP connection
						ctx.close();
					} catch (NamingException e) {
					}
				}
			}
		} else if (ConfigMain.getBooleanParameter("useSimpleAuthentification", false)) {
			env.put(Context.SECURITY_AUTHENTICATION, "none");
		} else {
			env.put(Context.SECURITY_PRINCIPAL, ConfigMain.getParameter("ldap_adminLogin"));
			env.put(Context.SECURITY_CREDENTIALS, ConfigMain.getParameter("ldap_adminPassword"));

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
			myLogger.error(e);
		}
		return rueckgabe;
	}

	/**
	 * check if User already exists on system
	 * 
	 * @param inBenutzer
	 * @return path as string
	 */
	public boolean isUserAlreadyExists(String inLogin) {
		Hashtable<String, String> env = LdapConnectionSettings();
		env.put(Context.SECURITY_PRINCIPAL, ConfigMain.getParameter("ldap_adminLogin"));
		env.put(Context.SECURITY_CREDENTIALS, ConfigMain.getParameter("ldap_adminPassword"));
		DirContext ctx;
		boolean rueckgabe = false;
		try {
			ctx = new InitialDirContext(env);
			Attributes matchAttrs = new BasicAttributes(true);
			NamingEnumeration<SearchResult> answer = ctx.search("ou=users,dc=gdz,dc=sub,dc=uni-goettingen,dc=de", matchAttrs);
			rueckgabe = answer.hasMoreElements();

			while (answer.hasMore()) {
				SearchResult sr = answer.next();
				myLogger.debug(">>>" + sr.getName());
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
				myLogger.debug(givenName);
				myLogger.debug(surName);
				myLogger.debug(mail);
				myLogger.debug(cn);
				myLogger.debug(hd);

			}

			ctx.close();
		} catch (NamingException e) {
			myLogger.error(e);
		}
		return rueckgabe;
	}

	/**
	 * Get next free uidNumber
	 * 
	 * @return next free uidNumber
	 * @throws NamingException
	 */
	private String getNextUidNumber() {
		Hashtable<String, String> env = LdapConnectionSettings();
		env.put(Context.SECURITY_PRINCIPAL, ConfigMain.getParameter("ldap_adminLogin"));
		env.put(Context.SECURITY_CREDENTIALS, ConfigMain.getParameter("ldap_adminPassword"));
		DirContext ctx;
		String rueckgabe = "";
		try {
			ctx = new InitialDirContext(env);
			Attributes attrs = ctx.getAttributes(ConfigMain.getParameter("ldap_nextFreeUnixId"));
			Attribute la = attrs.get("uidNumber");
			rueckgabe = (String) la.get(0);
			ctx.close();
		} catch (NamingException e) {
			myLogger.error(e);
			Helper.setFehlerMeldung(e.getMessage());
		}
		return rueckgabe;
	}

	/**
	 * Set next free uidNumber
	 * 
	 * @throws NamingException
	 */
	private void setNextUidNumber() {
		Hashtable<String, String> env = LdapConnectionSettings();
		env.put(Context.SECURITY_PRINCIPAL, ConfigMain.getParameter("ldap_adminLogin"));
		env.put(Context.SECURITY_CREDENTIALS, ConfigMain.getParameter("ldap_adminPassword"));
		DirContext ctx;

		try {
			ctx = new InitialDirContext(env);
			Attributes attrs = ctx.getAttributes(ConfigMain.getParameter("ldap_nextFreeUnixId"));
			Attribute la = attrs.get("uidNumber");
			String oldValue = (String) la.get(0);
			int bla = Integer.parseInt(oldValue) + 1;

			BasicAttribute attrNeu = new BasicAttribute("uidNumber", String.valueOf(bla));
			ModificationItem[] mods = new ModificationItem[1];
			mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attrNeu);
			ctx.modifyAttributes(ConfigMain.getParameter("ldap_nextFreeUnixId"), mods);

			ctx.close();
		} catch (NamingException e) {
			myLogger.error(e);
		}

	}

	/**
	 * change password of given user, needs old password for authentification
	 * 
	 * @param inUser
	 * @param inOldPassword
	 * @param inNewPassword
	 * @return boolean about result of change
	 * @throws NoSuchAlgorithmException
	 */
	public boolean changeUserPassword(Benutzer inBenutzer, String inOldPassword, String inNewPassword) throws NoSuchAlgorithmException {
		Hashtable<String, String> env = LdapConnectionSettings();
		if (!ConfigMain.getBooleanParameter("ldap_readonly", false)) {
			env.put(Context.SECURITY_PRINCIPAL, ConfigMain.getParameter("ldap_adminLogin"));
			env.put(Context.SECURITY_CREDENTIALS, ConfigMain.getParameter("ldap_adminPassword"));

			try {
				DirContext ctx = new InitialDirContext(env);

		
				/*
				 * -------------------------------- Encryption of password and Base64-Encoding --------------------------------
				 */
				MessageDigest md = MessageDigest.getInstance(ConfigMain.getParameter("ldap_encryption", "SHA"));
				md.update(inNewPassword.getBytes());
				String digestBase64 = new String(Base64.encodeBase64(md.digest()));
				ModificationItem[] mods = new ModificationItem[4];

				/*
				 * -------------------------------- UserPasswort-Attribut ändern --------------------------------
				 */
				BasicAttribute userpassword = new BasicAttribute("userPassword", "{" + ConfigMain.getParameter("ldap_encryption", "SHA") + "}"
						+ digestBase64);

				/*
				 * -------------------------------- LanMgr-Passwort-Attribut ändern --------------------------------
				 */
				BasicAttribute lanmgrpassword = null;
				try {
					lanmgrpassword = new BasicAttribute("sambaLMPassword", LdapUser.toHexString(LdapUser.lmHash(inNewPassword)));
					// TODO: Don't catch super class exception, make sure that the password isn't logged here
				} catch (Exception e) {
					myLogger.error(e);
				}

				/*
				 * -------------------------------- NTLM-Passwort-Attribut ändern --------------------------------
				 */
				BasicAttribute ntlmpassword = null;
				try {
					byte hmm[] = MD4.mdfour(inNewPassword.getBytes("UnicodeLittleUnmarked"));
					ntlmpassword = new BasicAttribute("sambaNTPassword", LdapUser.toHexString(hmm));
				} catch (UnsupportedEncodingException e) {
					// TODO: Make sure that the password isn't logged here
					myLogger.error(e);
				}

				BasicAttribute sambaPwdLastSet = new BasicAttribute("sambaPwdLastSet", String.valueOf(System.currentTimeMillis() / 1000l));

				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, userpassword);
				mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, lanmgrpassword);
				mods[2] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, ntlmpassword);
				mods[3] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, sambaPwdLastSet);
				ctx.modifyAttributes(getUserDN(inBenutzer), mods);

				// Close the context when we're done
				ctx.close();
				return true;
			} catch (NamingException e) {
				myLogger.debug("Benutzeranmeldung nicht korrekt oder Passwortänderung nicht möglich", e);
				return false;
			}
		}
		return false;
	}

	private Hashtable<String, String> LdapConnectionSettings() {
		// Set up environment for creating initial context
		Hashtable<String, String> env = new Hashtable<String, String>(11);
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ConfigMain.getParameter("ldap_url"));
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		/* wenn die Verbindung über ssl laufen soll */
		if (ConfigMain.getBooleanParameter("ldap_sslconnection")) {
			String keystorepath = ConfigMain.getParameter("ldap_keystore");
			String keystorepasswd = ConfigMain.getParameter("ldap_keystore_password");

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
			try {
				FileOutputStream ksos = new FileOutputStream(path);
				// TODO: Rename parameters to something more meaningful, this is quite specific for the GDZ
				FileInputStream cacertFile = new FileInputStream(ConfigMain.getParameter("ldap_cert_root"));
				FileInputStream certFile2 = new FileInputStream(ConfigMain.getParameter("ldap_cert_pdc"));

				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				X509Certificate cacert = (X509Certificate) cf.generateCertificate(cacertFile);
				X509Certificate servercert = (X509Certificate) cf.generateCertificate(certFile2);

				KeyStore ks = KeyStore.getInstance("jks");
				char[] password = passwd.toCharArray();

				// TODO: Let this method really load a keystore if configured
				// initalize the keystore, if file is available, load the keystore
				ks.load(null);

				ks.setCertificateEntry("ROOTCERT", cacert);
				ks.setCertificateEntry("PDC", servercert);

				ks.store(ksos, password);
				ksos.close();
			} catch (Exception e) {
				myLogger.error(e);
			}

		}
	}

}
