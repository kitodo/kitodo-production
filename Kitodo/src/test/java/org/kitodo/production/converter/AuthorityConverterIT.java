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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Authority;

public class AuthorityConverterIT {

    private static final String MESSAGE = "Authority was not converted correctly!";

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertAuthorities();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldGetAsObject() {
        AuthorityConverter authorityConverter = new AuthorityConverter();
        Authority authority = (Authority) authorityConverter.getAsObject(null, null, "20");
        assertEquals(MESSAGE, 20, authority.getId().intValue());
    }

    @Test
    public void shouldGetAsObjectIncorrectString() {
        AuthorityConverter authorityConverter = new AuthorityConverter();
        String authority = (String) authorityConverter.getAsObject(null, null, "in");
        assertEquals(MESSAGE, "0", authority);
    }

    @Test
    public void shouldGetAsObjectIncorrectId() {
        AuthorityConverter authorityConverter = new AuthorityConverter();
        String authority = (String) authorityConverter.getAsObject(null, null, "1000");
        assertEquals(MESSAGE, "0", authority);
    }

    @Test
    public void shouldGetAsObjectNullObject() {
        AuthorityConverter authorityConverter = new AuthorityConverter();
        Object authority = authorityConverter.getAsObject(null, null, null);
        assertNull(MESSAGE, authority);
    }

    @Test
    public void shouldGetAsString() {
        AuthorityConverter authorityConverter = new AuthorityConverter();
        Authority newAuthority = new Authority();
        newAuthority.setId(20);
        String authority = authorityConverter.getAsString(null, null, newAuthority);
        assertEquals(MESSAGE, "20", authority);
    }

    @Test
    public void shouldGetAsStringWithoutId() {
        AuthorityConverter authorityConverter = new AuthorityConverter();
        Authority newAuthority = new Authority();
        String authority = authorityConverter.getAsString(null, null, newAuthority);
        assertEquals(MESSAGE, "0", authority);
    }

    @Test
    public void shouldGetAsStringWithString() {
        AuthorityConverter authorityConverter = new AuthorityConverter();
        String authority = authorityConverter.getAsString(null, null, "20");
        assertEquals(MESSAGE, "20", authority);
    }

    @Test
    public void shouldNotGetAsStringNullObject() {
        AuthorityConverter authorityConverter = new AuthorityConverter();
        String authority = authorityConverter.getAsString(null, null, null);
        assertNull(MESSAGE, authority);
    }
}
