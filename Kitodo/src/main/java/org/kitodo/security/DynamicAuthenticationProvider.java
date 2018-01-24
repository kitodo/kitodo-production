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
import org.kitodo.services.ServiceManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;

public class DynamicAuthenticationProvider implements AuthenticationProvider {

    private static DynamicAuthenticationProvider instance = null;
    private AuthenticationProvider authenticationProvider;
    private boolean LdapAuthentication;
    private boolean DataBaseAuthentication;
    private ServiceManager serviceManager = new ServiceManager();

    private String ldapUrl = "";

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
        return authenticationProvider.authenticate(authentication);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authenticationProvider.supports(authentication);
    }

    public void setLdap(String ldapUrl, String userDnPattern) {

        DefaultSpringSecurityContextSource ldapContextSource = new DefaultSpringSecurityContextSource(ldapUrl);
        ldapContextSource.afterPropertiesSet();

        BindAuthenticator authenticator = new BindAuthenticator(ldapContextSource);
        authenticator.setUserDnPatterns(new String[] {userDnPattern });

        LdapAuthenticationProvider ldapAuthenticationProvider = new LdapAuthenticationProvider(authenticator,
                new CustomLdapAuthoritiesPopulator());

        this.authenticationProvider = ldapAuthenticationProvider;
    }

    public void configureDb() {
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

}
