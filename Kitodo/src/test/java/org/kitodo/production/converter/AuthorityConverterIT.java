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

package org.kitodo.production.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Authority;

public class AuthorityConverterIT {

    private static final String MESSAGE = "Authority was not converted correctly!";

    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertAuthorities();
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldGetAsObject() {
        AuthorityConverter authorityConverter = new AuthorityConverter();
        Authority authority = (Authority) authorityConverter.getAsObject(null, null, "20");
        assertEquals(20, authority.getId().intValue(), MESSAGE);
    }

    @Test
    public void shouldGetAsObjectIncorrectString() {
        AuthorityConverter authorityConverter = new AuthorityConverter();
        String authority = (String) authorityConverter.getAsObject(null, null, "in");
        assertEquals("0", authority, MESSAGE);
    }

    @Test
    public void shouldGetAsObjectIncorrectId() {
        AuthorityConverter authorityConverter = new AuthorityConverter();
        String authority = (String) authorityConverter.getAsObject(null, null, "1000");
        assertEquals("0", authority, MESSAGE);
    }

    @Test
    public void shouldGetAsObjectNullObject() {
        AuthorityConverter authorityConverter = new AuthorityConverter();
        Object authority = authorityConverter.getAsObject(null, null, null);
        assertNull(authority, MESSAGE);
    }

    @Test
    public void shouldGetAsString() {
        AuthorityConverter authorityConverter = new AuthorityConverter();
        Authority newAuthority = new Authority();
        newAuthority.setId(20);
        String authority = authorityConverter.getAsString(null, null, newAuthority);
        assertEquals("20", authority, MESSAGE);
    }

    @Test
    public void shouldGetAsStringWithoutId() {
        AuthorityConverter authorityConverter = new AuthorityConverter();
        Authority newAuthority = new Authority();
        String authority = authorityConverter.getAsString(null, null, newAuthority);
        assertEquals("0", authority, MESSAGE);
    }

    @Test
    public void shouldGetAsStringWithString() {
        AuthorityConverter authorityConverter = new AuthorityConverter();
        String authority = authorityConverter.getAsString(null, null, "20");
        assertEquals("20", authority, MESSAGE);
    }

    @Test
    public void shouldNotGetAsStringNullObject() {
        AuthorityConverter authorityConverter = new AuthorityConverter();
        String authority = authorityConverter.getAsString(null, null, null);
        assertNull(authority, MESSAGE);
    }
}
