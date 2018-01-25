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

import java.util.Objects;

import de.sub.goobi.config.ConfigCore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.services.ServiceManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;

public class DynamicAuthenticationProvider implements AuthenticationProvider {

    private static DynamicAuthenticationProvider instance = null;
    private AuthenticationProvider authenticationProvider;
    private static final Logger logger = LogManager.getLogger(DynamicAuthenticationProvider.class);
    private boolean ldapAuthentication = false;
    private ServiceManager serviceManager = new ServiceManager();

    /**
     * Constructor for DynamicAuthenticationProvider which also sets instance
     * variable for singleton usage.
     */
    public DynamicAuthenticationProvider() {
        if (Objects.equals(instance, null)) {
            synchronized (DynamicAuthenticationProvider.class) {
                if (Objects.equals(instance, null)) {
                    instance = this;
                }
            }
        }
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (ldapAuthentication) {
            Object principal = authentication.getPrincipal();

            UserDetails user = null;

            if (principal instanceof UserDetails) {
                user = (UserDetails) principal;
            }

            if (user != null) {
                try {
                    User userBean = serviceManager.getUserService().getByLogin(user.getUsername());
                    configureLdap(userBean.getLdapGroup());
                } catch (DAOException e) {
                    //using getUserService().getByLogin, DAOExeption is thrown when username is not found on database
                    //it must be converted in UsernameNotFoundException in order to match interface method signature
                    throw new UsernameNotFoundException("Username " + user.getUsername() + " was not found!");
                }
            }
        }
        return authenticationProvider.authenticate(authentication);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authenticationProvider.supports(authentication);
    }

    public void configureLdap(LdapGroup ldapGroup) {

        DefaultSpringSecurityContextSource ldapContextSource = new DefaultSpringSecurityContextSource(ldapGroup.getLdapServer().getUrl());
        ldapContextSource.afterPropertiesSet();

        BindAuthenticator authenticator = new BindAuthenticator(ldapContextSource);
        authenticator.setUserDnPatterns(new String[] {ldapGroup.getUserDN() });

        LdapAuthenticationProvider ldapAuthenticationProvider = new LdapAuthenticationProvider(authenticator,
                new CustomLdapAuthoritiesPopulator());

        this.authenticationProvider = ldapAuthenticationProvider;
    }

    public void configureDb() {

        ldapAuthentication = false;

        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(serviceManager.getUserService());
        daoAuthenticationProvider.setPasswordEncoder(new SecurityPasswordEncoder());
        this.authenticationProvider = daoAuthenticationProvider;
    }

    /**
     * Return singleton variable of type DynamicAuthenticationProvider.
     *
     * @return unique instance of DynamicAuthenticationProvider
     */
    public static DynamicAuthenticationProvider getInstance() {
        return instance;
    }

    public void readLocalConfig() {
        String ldapUse = ConfigCore.getParameter("ldap_use");
        if (ldapUse.equals("true")) {
            ldapAuthentication = true;
        }
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
     * @param ldapAuthentication The ldapAuthentication.
     */
    public void setLdapAuthentication(boolean ldapAuthentication) {
        this.ldapAuthentication = ldapAuthentication;
    }
}
