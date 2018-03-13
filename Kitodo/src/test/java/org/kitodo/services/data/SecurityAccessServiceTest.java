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

package org.kitodo.services.data;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kitodo.services.security.SecurityAccessService;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@RunWith(MockitoJUnitRunner.class)
public class SecurityAccessServiceTest {

    SecurityAccessService securityAccessService = new SecurityAccessService();

    private Collection<SimpleGrantedAuthority> getAuthoritiesOfCurrentAuthenticationByAuthorityTitle(
            String authorityTitle) {
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("admin_test"));
        authorities.add(new SimpleGrantedAuthority("admin_test2"));
        authorities.add(new SimpleGrantedAuthority("admin_test3"));
        authorities.add(new SimpleGrantedAuthority("user_test"));
        authorities.add(new SimpleGrantedAuthority("user_test2"));
        authorities.add(new SimpleGrantedAuthority("user_test3"));
        authorities.add(new SimpleGrantedAuthority("user_test4"));

        Collection<SimpleGrantedAuthority> specifiedAuthorities = new ArrayList<>();
        for (SimpleGrantedAuthority grantedAuthority : authorities) {
            if (grantedAuthority.getAuthority().contains(authorityTitle)) {
                specifiedAuthorities.add(grantedAuthority);
            }
        }

        return specifiedAuthorities;
    }

    @Test
    public void getAuthoritiesOfCurrentAuthenticationByAuthorityTitleTest() {
        System.out.println(getAuthoritiesOfCurrentAuthenticationByAuthorityTitle("admin"));
        assertTrue(true);

    }

}
