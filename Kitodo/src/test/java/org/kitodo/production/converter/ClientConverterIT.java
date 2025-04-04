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
import org.kitodo.data.database.beans.Client;

public class ClientConverterIT {

    private static final String MESSAGE = "Client was not converted correctly!";

    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertClients();
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldGetAsObject() {
        ClientConverter clientConverter = new ClientConverter();
        Client client = (Client) clientConverter.getAsObject(null, null, "2");
        assertEquals(2, client.getId().intValue(), MESSAGE);
    }

    @Test
    public void shouldGetAsObjectIncorrectString() {
        ClientConverter clientConverter = new ClientConverter();
        String client = (String) clientConverter.getAsObject(null, null, "in");
        assertEquals("0", client, MESSAGE);
    }

    @Test
    public void shouldGetAsObjectIncorrectId() {
        ClientConverter clientConverter = new ClientConverter();
        String client = (String) clientConverter.getAsObject(null, null, "10");
        assertEquals("0", client, MESSAGE);
    }

    @Test
    public void shouldGetAsObjectNullObject() {
        ClientConverter clientConverter = new ClientConverter();
        Object client = clientConverter.getAsObject(null, null, null);
        assertNull(client, MESSAGE);
    }

    @Test
    public void shouldGetAsString() {
        ClientConverter clientConverter = new ClientConverter();
        Client newClient = new Client();
        newClient.setId(20);
        String client = clientConverter.getAsString(null, null, newClient);
        assertEquals("20", client, MESSAGE);
    }

    @Test
    public void shouldGetAsStringWithoutId() {
        ClientConverter clientConverter = new ClientConverter();
        Client newClient = new Client();
        String client = clientConverter.getAsString(null, null, newClient);
        assertEquals("0", client, MESSAGE);
    }

    @Test
    public void shouldGetAsStringWithString() {
        ClientConverter clientConverter = new ClientConverter();
        String client = clientConverter.getAsString(null, null, "20");
        assertEquals("20", client, MESSAGE);
    }

    @Test
    public void shouldNotGetAsStringNullObject() {
        ClientConverter clientConverter = new ClientConverter();
        String client = clientConverter.getAsString(null, null, null);
        assertNull(client, MESSAGE);
    }
}
