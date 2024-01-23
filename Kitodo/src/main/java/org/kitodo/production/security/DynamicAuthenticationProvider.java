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

package org.kitodo.production.security;

import java.util.Objects;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.LocaleHelper;
import org.kitodo.production.security.password.SecurityPasswordEncoder;
import org.kitodo.production.services.ServiceManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;

/**
 * A custom authentication provider which supports the change of authentication
 * type (database or ldap) and details (e.g. ldap server url) during runtime.
 */
@Named("AuthenticationController")
@RequestScoped
public class DynamicAuthenticationProvider implements AuthenticationProvider {
    private static final Logger logger = LogManager.getLogger(DynamicAuthenticationProvider.class);

    private static volatile DynamicAuthenticationProvider instance = null;
    private AuthenticationProvider daoAuthenticationProvider = null;
    private AuthenticationProvider ldapAuthenticationProvider = null;

    private boolean ldapAuthentication;
    private DefaultSpringSecurityContextSource ldapContextSource = null;
    private BindAuthenticator bindAuthenticator = null;
    private final LdapUserDetailsContextMapper ldapUserDetailsContextMapper = new LdapUserDetailsContextMapper();

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
        DynamicAuthenticationProvider localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (DynamicAuthenticationProvider.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new DynamicAuthenticationProvider();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        try {
            User user = ServiceManager.getUserService().getByLdapLoginOrLogin(authentication.getName());
            if (!user.isActive()) {
                throw new DisabledException(SpringSecurityMessageSource.getAccessor().getMessage(
                    "AbstractUserDetailsAuthenticationProvider.disabled",
                    Helper.getString(LocaleHelper.getCurrentLocale(), "errorUserIsDisabled")));
            }
            LdapGroup ldapGroup = user.getLdapGroup();
            if (ldapAuthentication && Objects.nonNull(ldapGroup)) {
                if (Objects.isNull(ldapGroup.getLdapServer())) {
                    throw new AuthenticationServiceException("No LDAP server specified on user's LDAP group");
                }
                configureAuthenticationProvider(ldapGroup.getLdapServer().getUrl(), ldapGroup.getUserDN());
                if (ldapGroup.getUserDN().contains("{ldaplogin}")) {
                    authentication = new UsernamePasswordAuthenticationToken(user.getLdapLogin(), authentication.getCredentials());
                }
                return ldapAuthenticationProvider.authenticate(authentication);
            } else {
                return daoAuthenticationProvider.authenticate(authentication);
            }
        } catch (RuntimeException problem) {
            // if login fails because of some unchecked exception, log it
            // when in debug mode
            logger.debug(problem.getLocalizedMessage(), problem);
            // rethrow exception
            throw problem;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
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

        this.ldapAuthenticationProvider = ldapAuthenticationProvider;
    }

    private void activateDatabaseAuthentication() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(ServiceManager.getUserService());
        daoAuthenticationProvider.setPasswordEncoder(new SecurityPasswordEncoder());
        this.daoAuthenticationProvider = daoAuthenticationProvider;
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
        setLdapAuthentication(ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.LDAP_USE));
    }

    private String[] convertUserDn(String userDn) {
        return new String[] {userDn.replaceFirst("\\{(?:ldap)?login}", "{0}") };
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
        }
        activateDatabaseAuthentication();
    }
}
