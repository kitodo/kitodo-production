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
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;

public class ClientConverterIT {

    private static final String MESSAGE = "Client was not converted correctly!";

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertClients();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldGetAsObject() {
        ClientConverter clientConverter = new ClientConverter();
        Client client = clientConverter.getAsObject(null, null, "2");
        assertEquals(MESSAGE, 2, client.getId().intValue());
    }

    @Test
    public void shouldGetAsObjectIncorrectString() {
        ClientConverter clientConverter = new ClientConverter();
        Client client = clientConverter.getAsObject(null, null, "in");
        assertEquals(MESSAGE, "0", String.valueOf(client.getId()));
    }

    @Test
    public void shouldGetAsObjectIncorrectId() {
        ClientConverter clientConverter = new ClientConverter();
        String client = String.valueOf(clientConverter.getAsObject(null, null, "10"));
        assertEquals(MESSAGE, "0", client);
    }

    @Test
    public void shouldGetAsObjectNullObject() {
        ClientConverter clientConverter = new ClientConverter();
        Object client = clientConverter.getAsObject(null, null, null);
        assertNull(MESSAGE, client);
    }

    @Test
    public void shouldGetAsString() {
        ClientConverter clientConverter = new ClientConverter();
        Client newClient = new Client();
        newClient.setId(20);
        String client = clientConverter.getAsString(null, null, newClient);
        assertEquals(MESSAGE, "20", client);
    }

    @Test
    public void shouldGetAsStringWithoutId() {
        ClientConverter clientConverter = new ClientConverter();
        Client newClient = new Client();
        String client = clientConverter.getAsString(null, null, newClient);
        assertEquals(MESSAGE, "0", client);
    }

    @Test
    public void shouldGetByIdAsString() throws DAOException {
        ClientConverter clientConverter = new ClientConverter();
        Client client = ServiceManager.getClientService().getById(20);
        String clientId = clientConverter.getAsString(null, null, client);
        assertEquals(MESSAGE, "20", clientId);
    }

    @Test
    public void shouldNotGetAsStringNullObject() {
        ClientConverter clientConverter = new ClientConverter();
        String client = clientConverter.getAsString(null, null, null);
        assertNull(MESSAGE, client);
    }
}
