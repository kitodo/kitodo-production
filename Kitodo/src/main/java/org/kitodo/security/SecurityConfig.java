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

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static SecurityConfig instance = null;
    private SessionRegistry sessionRegistry;

    /**
     * Constructor for SecurityConfig which also sets instance variable for singleton usage.
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

        http.authorizeRequests()
            .antMatchers("/pages/clients.jsf").hasAnyAuthority(
                "admin_GLOBAL",
                "viewAllClients_GLOBAL")
            .antMatchers("/pages/clientEdit.jsf*").hasAnyAuthority(
                "admin_GLOBAL",
                "editClient_GLOBAL")

            .antMatchers("/pages/indexingPage.jsf").hasAnyAuthority(
                "admin_GLOBAL",
                "viewIndex_GLOBAL",
                "editIndex_GLOBAL")

            .antMatchers("/pages/processes.jsf").hasAnyAuthority(
                "admin_GLOBAL",
                "viewAllProcesses_GLOBAL",
                "viewAllProcesses_CLIENT_ANY",
                "viewAllProcesses_PROJECT_ANY")
            .antMatchers("/pages/processEdit.jsf*").hasAnyAuthority(
                "admin_GLOBAL",
                "editProcess_GLOBAL",
                "editProcess_CLIENT_ANY",
                "editProcess_PROJECT_ANY")

            .antMatchers("/pages/projects.jsf").hasAnyAuthority(
                "admin_GLOBAL",
                "viewAllProjects_GLOBAL",
                "viewAllProjects_CLIENT_ANY",
                "viewAllTemplates_GLOBAL",
                "viewAllTemplates_CLIENT_ANY",
                "viewAllTemplates_PROJECT_ANY",
                "viewAllDockets_GLOBAL",
                "viewAllRulesets_GLOBAL")
            .antMatchers("/pages/projectEdit.jsf*").hasAnyAuthority(
                "admin_GLOBAL",
                "editProject_GLOBAL",
                "editProject_CLIENT_ANY",
                "editProject_PROJECT_ANY")
            .antMatchers("/pages/editDocket.jsf*").hasAnyAuthority(
                "admin_GLOBAL",
                "editDocket_GLOBAL")
            .antMatchers("/pages/rulesetEdit.jsf*").hasAnyAuthority(
                "admin_GLOBAL",
                "editRuleset_GLOBAL")

            .antMatchers("/pages/tasks.jsf").hasAnyAuthority(
                "admin_GLOBAL",
                "viewAllTasks_GLOBAL",
                "viewAllTasks_CLIENT_ANY",
                "viewAllTasks_PROJECT_ANY")

            .antMatchers("/pages/users.jsf").hasAnyAuthority(
                "admin_GLOBAL",
                "viewAllUsers_GLOBAL",
                "viewAllUsers_CLIENT_ANY",
                "viewAllUserGroups_GLOBAL",
                "viewAllUserGroups_CLIENT_ANY",
                "viewAllLdapGroups_GLOBAL")
            .antMatchers("/pages/userEdit.jsf*").hasAnyAuthority(
                "admin_GLOBAL",
                "editUser_GLOBAL",
                "editUser_CLIENT_ANY")
            .antMatchers("/pages/usergroupEdit.jsf*").hasAnyAuthority(
                "admin_GLOBAL",
                "editUserGroup_GLOBAL",
                "editUserGroup_CLIENT_ANY")
            .antMatchers("/pages/ldapgroupEdit.jsf*").hasAnyAuthority(
                "admin_GLOBAL",
                "editLdapGroup_GLOBAL")

            .antMatchers("/pages/images/**").permitAll()
            .antMatchers("/javax.faces.resource/**", "**/resources/**").permitAll()
            .antMatchers("/js/toggle.js").permitAll()
            .anyRequest().authenticated();

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

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {
        DynamicAuthenticationProvider authenticationProvider = new DynamicAuthenticationProvider();
        authenticationProvider.readLocalConfig();
        authenticationProvider.initializeAuthenticationProvider();
        auth.authenticationProvider(authenticationProvider);
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
