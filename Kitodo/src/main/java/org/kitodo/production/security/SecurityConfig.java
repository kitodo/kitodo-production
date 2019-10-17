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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

/**
 * The main security configuration class for the application. The configure
 * method is called once during start of the application.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static volatile SecurityConfig instance = null;
    private SessionRegistry sessionRegistry;
    private static final String CLIENT_ANY = "CLIENT_ANY";
    private static final String GLOBAL = "GLOBAL";
    private static final String EDIT_CLIENT = "editClient_";
    private static final String EDIT_DOCKET = "editDocket_";
    private static final String EDIT_RULESET = "editRuleset_";
    private static final String EDIT_PROCESS = "editProcess_";
    private static final String EDIT_PROJECT = "editProject_";
    private static final String EDIT_ROLE = "editRole_";
    private static final String EDIT_TEMPLATE = "editTemplate_";
    private static final String EDIT_WORKFLOW = "editWorkflow_";
    private static final String VIEW_ALL_DOCKETS = "viewAllDockets_";
    private static final String VIEW_ALL_PROCESSES = "viewAllProcesses_";
    private static final String VIEW_ALL_PROJECTS = "viewAllProjects_";
    private static final String VIEW_ALL_RULESETS = "viewAllRulesets_";
    private static final String VIEW_ALL_TASKS = "viewAllTasks_";
    private static final String VIEW_ALL_TEMPLATES = "viewAllTemplates_";
    private static final String VIEW_ALL_USERS = "viewAllUsers_";
    private static final String VIEW_ALL_ROLES = "viewAllRoles_";
    private static final String VIEW_ALL_WORKFLOWS = "viewAllWorkflows_";
    private static final String VIEW_CLIENT = "viewClient_";
    private static final String VIEW_DOCKET = "viewDocket_";
    private static final String VIEW_PROCESS = "viewProcess_";
    private static final String VIEW_PROJECT = "viewProject_";
    private static final String VIEW_RULESET = "viewRuleset_";
    private static final String VIEW_ROLE = "viewRole_";
    private static final String VIEW_TEMPLATE = "viewTemplate_";
    private static final String VIEW_WORKFLOW = "viewProcess_";
    private static final String LOGIN_PAGE = "/pages/login.jsf";

    /**
     * Constructor for SecurityConfig which also sets instance variable for
     * singleton usage.
     */
    public SecurityConfig() {
        SecurityConfig localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (SecurityConfig.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = this;
                    instance = localReference;
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
        if (Objects.isNull(this.sessionRegistry)) {
            this.sessionRegistry = new SessionRegistryImpl();
        }
        return this.sessionRegistry;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // CSRF protection is disabled. In default enabled state, CSRF Token must be included on every request.
        http.csrf().disable();

        http.sessionManagement().maximumSessions(1).sessionRegistry(getSessionRegistry())
                .expiredUrl(LOGIN_PAGE);

        // viewAll... Authority to view list of entities
        // view...... Authority to view entity at edit page
        // edit...... Authority to change and save entities at edit page
        http.authorizeRequests()
            .antMatchers("/pages/clientEdit.jsf*").hasAnyAuthority(
                EDIT_CLIENT + GLOBAL,
                EDIT_CLIENT + CLIENT_ANY,
                VIEW_CLIENT + GLOBAL,
                VIEW_CLIENT + CLIENT_ANY)

            .antMatchers("/pages/indexingPage.jsf").hasAnyAuthority(
                "editIndex_" + GLOBAL,
                "viewIndex_" + GLOBAL)

            .antMatchers("/pages/processes.jsf").hasAnyAuthority(
                VIEW_ALL_PROCESSES + GLOBAL,
                VIEW_ALL_PROCESSES + CLIENT_ANY)
            .antMatchers("/pages/processEdit.jsf*").hasAnyAuthority(
                EDIT_PROCESS + GLOBAL,
                EDIT_PROCESS + CLIENT_ANY,
                VIEW_PROCESS + GLOBAL,
                VIEW_PROCESS + CLIENT_ANY)

            .antMatchers("/pages/projects.jsf").hasAnyAuthority(
                VIEW_ALL_PROJECTS + GLOBAL,
                VIEW_ALL_PROJECTS + CLIENT_ANY,
                VIEW_ALL_TEMPLATES + GLOBAL,
                VIEW_ALL_TEMPLATES + CLIENT_ANY,
                VIEW_ALL_DOCKETS + GLOBAL,
                VIEW_ALL_DOCKETS + CLIENT_ANY,
                VIEW_ALL_RULESETS + GLOBAL,
                VIEW_ALL_RULESETS + CLIENT_ANY,
                VIEW_ALL_WORKFLOWS + GLOBAL)
            .antMatchers("/pages/projectEdit.jsf*").hasAnyAuthority(
                EDIT_PROJECT + GLOBAL,
                EDIT_PROJECT + CLIENT_ANY,
                VIEW_PROJECT + GLOBAL,
                VIEW_PROJECT + CLIENT_ANY)

                .antMatchers("/pages/templateEdit.jsf*").hasAnyAuthority(
                EDIT_TEMPLATE + GLOBAL,
                EDIT_TEMPLATE + CLIENT_ANY,
                VIEW_TEMPLATE + GLOBAL,
                VIEW_TEMPLATE + CLIENT_ANY)

            .antMatchers("/pages/docketEdit.jsf*").hasAnyAuthority(
                EDIT_DOCKET + GLOBAL,
                EDIT_DOCKET + CLIENT_ANY,
                VIEW_DOCKET + GLOBAL,
                VIEW_DOCKET + CLIENT_ANY)

            .antMatchers("/pages/rulesetEdit.jsf*").hasAnyAuthority(
                EDIT_RULESET + GLOBAL,
                EDIT_RULESET + CLIENT_ANY,
                VIEW_RULESET + GLOBAL,
                VIEW_RULESET + CLIENT_ANY)

            .antMatchers("/pages/workflowEdit.jsf*").hasAnyAuthority(
                EDIT_WORKFLOW + GLOBAL,
                EDIT_WORKFLOW + CLIENT_ANY,
                VIEW_WORKFLOW + GLOBAL,
                VIEW_WORKFLOW + CLIENT_ANY)

            .antMatchers("/pages/tasks.jsf").hasAnyAuthority(
                VIEW_ALL_TASKS + GLOBAL,
                VIEW_ALL_TASKS + CLIENT_ANY)

            .antMatchers("/pages/users.jsf").hasAnyAuthority(
                VIEW_ALL_USERS + GLOBAL,
                VIEW_ALL_USERS + CLIENT_ANY,
                VIEW_ALL_ROLES + GLOBAL,
                VIEW_ALL_ROLES + CLIENT_ANY,
                "viewAllClients_" + GLOBAL,
                "viewAllLdapGroups_" + GLOBAL)

            .antMatchers("/pages/roleEdit.jsf*").hasAnyAuthority(
                EDIT_ROLE + GLOBAL,
                EDIT_ROLE + CLIENT_ANY,
                VIEW_ROLE + GLOBAL,
                VIEW_ROLE + CLIENT_ANY)

            .antMatchers("/pages/ldapgroupEdit.jsf*").hasAnyAuthority(
                "editLdapGroup_" + GLOBAL,
                "viewLdapGroup_" + GLOBAL)

            .antMatchers("/pages/images/**").permitAll()
            .antMatchers("/javax.faces.resource/**", "**/resources/**").permitAll()
            .antMatchers("/js/modeler.js").permitAll()
            .antMatchers("/js/toggle.js").permitAll()
            .anyRequest().authenticated();

        http.addFilterAfter(new SecurityObjectAccessFilter(), FilterSecurityInterceptor.class);

        http.formLogin()
                .loginPage(LOGIN_PAGE)
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/pages/desktop.jsf")
                .successHandler(new CustomLoginSuccessHandler())
                .permitAll()
                .and()
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessHandler(new CustomLogoutSuccessHandler(LOGIN_PAGE));
    }

    /**
     * Sets the DynamicAuthenticationProvider as AuthenticationProvider.
     * (authentication against ldap or database).
     *
     * @param authenticationManagerBuilder
     *            The authentication manager builder
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder authenticationManagerBuilder) {
        authenticationManagerBuilder.authenticationProvider(DynamicAuthenticationProvider.getInstance());
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
