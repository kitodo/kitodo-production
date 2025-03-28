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
import org.kitodo.data.database.beans.Docket;

public class DocketConverterIT {

    private static final String MESSAGE = "Docket was not converted correctly!";

    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertClients();
        MockDatabase.insertDockets();
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldGetAsObject() {
        DocketConverter docketConverter = new DocketConverter();
        Docket docket = (Docket) docketConverter.getAsObject(null, null, "2");
        assertEquals(2, docket.getId().intValue(), MESSAGE);
    }

    @Test
    public void shouldGetAsObjectIncorrectString() {
        DocketConverter docketConverter = new DocketConverter();
        String docket = (String) docketConverter.getAsObject(null, null, "in");
        assertEquals("0", docket, MESSAGE);
    }

    @Test
    public void shouldGetAsObjectIncorrectId() {
        DocketConverter docketConverter = new DocketConverter();
        String docket = (String) docketConverter.getAsObject(null, null, "10");
        assertEquals("0", docket, MESSAGE);
    }

    @Test
    public void shouldGetAsObjectNullObject() {
        DocketConverter docketConverter = new DocketConverter();
        Object docket = docketConverter.getAsObject(null, null, null);
        assertNull(docket, MESSAGE);
    }

    @Test
    public void shouldGetAsString() {
        DocketConverter docketConverter = new DocketConverter();
        Docket newDocket = new Docket();
        newDocket.setId(20);
        String docket = docketConverter.getAsString(null, null, newDocket);
        assertEquals("20", docket, MESSAGE);
    }

    @Test
    public void shouldGetAsStringWithoutId() {
        DocketConverter docketConverter = new DocketConverter();
        Docket newDocket = new Docket();
        String docket = docketConverter.getAsString(null, null, newDocket);
        assertEquals("0", docket, MESSAGE);
    }

    @Test
    public void shouldGetAsStringWithString() {
        DocketConverter docketConverter = new DocketConverter();
        String docket = docketConverter.getAsString(null, null, "20");
        assertEquals("20", docket, MESSAGE);
    }

    @Test
    public void shouldNotGetAsStringNullObject() {
        DocketConverter docketConverter = new DocketConverter();
        String docket = docketConverter.getAsString(null, null, null);
        assertNull(docket, MESSAGE);
    }
}
