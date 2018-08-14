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

package org.kitodo.security;

import de.sub.goobi.config.ConfigCore;

import java.util.Objects;

import org.kitodo.config.Parameters;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.services.ServiceManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;

public class DynamicAuthenticationProvider implements AuthenticationProvider {

    private ServiceManager serviceManager = new ServiceManager();

    private static DynamicAuthenticationProvider instance = null;
    private AuthenticationProvider authenticationProvider = null;

    private boolean ldapAuthentication;
    private DefaultSpringSecurityContextSource ldapContextSource = null;
    private BindAuthenticator bindAuthenticator = null;
    private LdapUserDetailsContextMapper ldapUserDetailsContextMapper = new LdapUserDetailsContextMapper();

    /**
     * The private Constructor which initially reads the local config.
     */
    private DynamicAuthenticationProvider() {
        readLocalConfig();
    }

    /**
     * Return singleton variable of type DynamicAuthenticationProvider.
     *
     * @return unique instance of DynamicAuthenticationProvider
     */
    public static DynamicAuthenticationProvider getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (DynamicAuthenticationProvider.class) {
                if (Objects.equals(instance, null)) {
                    instance = new DynamicAuthenticationProvider();
                }
            }
        }
        return instance;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        if (ldapAuthentication) {
            try {
                User user = serviceManager.getUserService().getByLdapLoginWithFallback(authentication.getName());
                configureAuthenticationProvider(user.getLdapGroup().getLdapServer().getUrl(),
                    user.getLdapGroup().getUserDN());
            } catch (DAOException e) {
                // getByLogin() throws DAOExeption, it must be converted in
                // UsernameNotFoundException
                // in order to match interface method signature
                throw new UsernameNotFoundException("Error on reading user from database!");
            }
        }
        return authenticationProvider.authenticate(authentication);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authenticationProvider.supports(authentication);
    }

    /**
     * This method activates ldap authentication and configures ldap url and userDn
     * pattern.
     *
     * @param url
     *            The url to ldap server.
     * @param userDn
     *            The user dn pattern.
     */
    private void configureAuthenticationProvider(String url, String userDn) {

        if (Objects.nonNull(url) && Objects.nonNull(userDn)) {

            if (Objects.isNull(this.ldapContextSource)) {
                this.ldapContextSource = new DefaultSpringSecurityContextSource(url);
            } else {
                this.ldapContextSource.setUrl(url);
            }
            this.ldapContextSource.afterPropertiesSet();

            if (Objects.isNull(this.bindAuthenticator)) {
                this.bindAuthenticator = new BindAuthenticator(ldapContextSource);
            }
            bindAuthenticator.setUserDnPatterns(convertUserDn(userDn));

            LdapAuthenticationProvider ldapAuthenticationProvider = new LdapAuthenticationProvider(bindAuthenticator);
            ldapAuthenticationProvider.setUserDetailsContextMapper(this.ldapUserDetailsContextMapper);

            this.authenticationProvider = ldapAuthenticationProvider;
        } else {
            throw new AuthenticationServiceException("No ldap-server specified on users ldap-group");
        }
    }

    private void activateDatabaseAuthentication() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(serviceManager.getUserService());
        daoAuthenticationProvider.setPasswordEncoder(new SecurityPasswordEncoder());
        this.authenticationProvider = daoAuthenticationProvider;
    }

    /**
     * This activates the Ldap authentication with initial url and userDn. These
     * values are later replace by the user information when authentication is
     * performed.
     */
    private void activateLdapAuthentication() {
        configureAuthenticationProvider("ldap://0.0.0.0", "no userDn");
    }

    private void readLocalConfig() {
        setLdapAuthentication(ConfigCore.getBooleanParameter(Parameters.LDAP_USE));
    }

    private String[] convertUserDn(String userDn) {
        return new String[] {userDn.replaceFirst("\\{login}", "{0}") };
    }

    /**
     * Gets ldapAuthentication.
     *
     * @return The ldapAuthentication.
     */
    public boolean isLdapAuthentication() {
        return ldapAuthentication;
    }

    /**
     * Sets ldapAuthentication.
     *
     * @param ldapAuthentication
     *            The ldapAuthentication.
     */
    public void setLdapAuthentication(boolean ldapAuthentication) {
        this.ldapAuthentication = ldapAuthentication;
        if (ldapAuthentication) {
            activateLdapAuthentication();
        } else {
            activateDatabaseAuthentication();
        }
    }
}
