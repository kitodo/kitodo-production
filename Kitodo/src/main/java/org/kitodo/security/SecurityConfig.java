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

import org.kitodo.services.ServiceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;

import java.util.Objects;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static SecurityConfig instance = null;
    private transient ServiceManager serviceManager = new ServiceManager();
    private SecurityPasswordEncoder passwordEncoder = new SecurityPasswordEncoder();
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

        http.sessionManagement().maximumSessions(1).sessionRegistry(getSessionRegistry());

        http
            .authorizeRequests()
                .antMatchers("/admin/**").hasAuthority("admin")
                .antMatchers("/pages/statischBedienung.jsf").permitAll()
                .antMatchers("/pages/statischTechnischerHintergrund.jsf").permitAll()
                .antMatchers("/pages/images/**").permitAll()
                .antMatchers("/javax.faces.resource/**", "**/resources/**").permitAll()
                .antMatchers("/js/toggle.js").permitAll()
                .antMatchers("/pages/aktiveNutzer.jsf").permitAll()
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/pages/Main.jsf")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/pages/Main.jsf")
                .permitAll()
                .and()
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessHandler(new CustomLogoutSuccessHandler("/pages/Main.jsf"));
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        DynamicAuthenticationProvider authenticationProvider = new DynamicAuthenticationProvider();
        authenticationProvider.readLocalConfig();
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
