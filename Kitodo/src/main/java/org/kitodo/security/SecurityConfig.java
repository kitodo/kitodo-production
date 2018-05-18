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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static SecurityConfig instance = null;
    private SessionRegistry sessionRegistry;
    private static final String CLIENT_ANY = "CLIENT_ANY";
    private static final String GLOBAL = "GLOBAL";
    private static final String PROJECT_ANY = "PROJECT_ANY";
    private static final String ADMIN_GLOBAL = "admin_" + GLOBAL;

    /**
     * Constructor for SecurityConfig which also sets instance variable for
     * singleton usage.
     */
    public SecurityConfig() {
        if (Objects.equals(instance, null)) {
            synchronized (SecurityConfig.class) {
                if (Objects.equals(instance, null)) {
                    instance = this;
                }
            }
        }
    }

    /**
     * Gets sessionRegistry.
     *
     * @return The sessionRegistry.
     */
    public SessionRegistry getSessionRegistry() {
        if (this.sessionRegistry == null) {
            this.sessionRegistry = new SessionRegistryImpl();
        }
        return this.sessionRegistry;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // CSRF protection is disabled. In default enabled state, CSRF Token must be included on every request.
        http.csrf().disable();

        http.sessionManagement().maximumSessions(1).sessionRegistry(getSessionRegistry())
                .expiredUrl("/pages/login.jsf");

        // viewAll... Authority to view list of entities
        // view...... Authority to view entity at edit page
        // edit...... Authority to change and save entities at edit page
        http.authorizeRequests()
            .antMatchers("/pages/clients.jsf").hasAnyAuthority(
                ADMIN_GLOBAL,
                "viewAllClients_" + GLOBAL)
            .antMatchers("/pages/clientEdit.jsf*").hasAnyAuthority(
                ADMIN_GLOBAL,
                "editClient_" + GLOBAL,
                "editClient_" + CLIENT_ANY)

            .antMatchers("/pages/indexingPage.jsf").hasAnyAuthority(
                ADMIN_GLOBAL,
                "editIndex_" + GLOBAL,
                "viewIndex_" + GLOBAL)

            .antMatchers("/pages/processes.jsf").hasAnyAuthority(
                ADMIN_GLOBAL,
                "viewAllProcesses_" + GLOBAL,
                "viewAllProcesses_" + CLIENT_ANY,
                "viewAllProcesses_" + PROJECT_ANY)
            .antMatchers("/pages/processEdit.jsf*").hasAnyAuthority(
                ADMIN_GLOBAL,
                "editProcess_" + GLOBAL,
                "editProcess_" + CLIENT_ANY,
                "editProcess_" + PROJECT_ANY)

            .antMatchers("/pages/projects.jsf").hasAnyAuthority(
                ADMIN_GLOBAL,
                "viewAllProjects_" + GLOBAL,
                "viewAllProjects_" + CLIENT_ANY,
                "viewAllTemplates_" + GLOBAL,
                "viewAllTemplates_" + CLIENT_ANY,
                "viewAllTemplates_" + PROJECT_ANY,
                "viewAllDockets_" + GLOBAL,
                "viewAllRulesets_" + GLOBAL)
            .antMatchers("/pages/projectEdit.jsf*").hasAnyAuthority(
                ADMIN_GLOBAL,
                "editProject_" + GLOBAL,
                "editProject_" + CLIENT_ANY,
                "editProject_" + PROJECT_ANY,
                "viewProject_" + GLOBAL,
                "viewProject_" + CLIENT_ANY,
                "viewProject_" + PROJECT_ANY)

            .antMatchers("/pages/docketEdit.jsf*").hasAnyAuthority(
                ADMIN_GLOBAL,
                "editDocket_" + GLOBAL)

            .antMatchers("/pages/rulesetEdit.jsf*").hasAnyAuthority(
                ADMIN_GLOBAL,
                "editRuleset_" + GLOBAL)

            .antMatchers("/pages/tasks.jsf").hasAnyAuthority(
                ADMIN_GLOBAL,
                "viewAllTasks_" + GLOBAL,
                "viewAllTasks_" + CLIENT_ANY,
                "viewAllTasks_" + PROJECT_ANY)

            .antMatchers("/pages/users.jsf").hasAnyAuthority(
                ADMIN_GLOBAL,
                "viewAllUsers_" + GLOBAL,
                "viewAllUsers_" + CLIENT_ANY,
                "viewAllUserGroups_" + GLOBAL,
                "viewAllUserGroups_" + CLIENT_ANY,
                "viewAllLdapGroups_" + GLOBAL)
            .antMatchers("/pages/userEdit.jsf*").hasAnyAuthority(
                ADMIN_GLOBAL,
                "editUser_" + GLOBAL,
                "editUser_" + CLIENT_ANY,
                "viewUser_" + GLOBAL,
                "viewUser_" + CLIENT_ANY)

            .antMatchers("/pages/usergroupEdit.jsf*").hasAnyAuthority(
                ADMIN_GLOBAL,
                "editUserGroup_" + GLOBAL,
                "editUserGroup_" + CLIENT_ANY,
                "viewUserGroup_" + GLOBAL,
                "viewUserGroup_" + CLIENT_ANY)

            .antMatchers("/pages/ldapgroupEdit.jsf*").hasAnyAuthority(
                ADMIN_GLOBAL,
                "editLdapGroup_" + GLOBAL,
                "viewLdapGroup_" + GLOBAL)

            .antMatchers("/pages/images/**").permitAll()
            .antMatchers("/javax.faces.resource/**", "**/resources/**").permitAll()
            .antMatchers("/js/toggle.js").permitAll()
            .anyRequest().authenticated();

        http.addFilterAfter(new SecurityObjectAccessFilter(), FilterSecurityInterceptor.class);

        http.formLogin()
                .loginPage("/pages/login.jsf")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/pages/start.jsf")
                .permitAll()
                .and()
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessHandler(new CustomLogoutSuccessHandler("/pages/login.jsf"));
    }

    /**
     * Reads local config and configures the dynamic authentication provider
     * (authentication against ldap or database).
     * 
     * @param authenticationManagerBuilder
     *            The authentication manager builder
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder authenticationManagerBuilder) {
        DynamicAuthenticationProvider authenticationProvider = new DynamicAuthenticationProvider();
        authenticationProvider.readLocalConfig();
        authenticationProvider.initializeAuthenticationProvider();
        authenticationManagerBuilder.authenticationProvider(authenticationProvider);
    }

    /**
     * Return singleton variable of type SecurityConfig.
     *
     * @return unique instance of SecurityConfig
     */
    public static SecurityConfig getInstance() {
        return instance;
    }
}
