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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Client;
import org.kitodo.dto.ClientDTO;
import org.kitodo.services.ServiceManager;

public class ClientServiceIT {

    private static final ClientService clientService = new ServiceManager().getClientService();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Before
    public void multipleInit() throws InterruptedException {
        Thread.sleep(500);
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldGetAllClients() throws Exception {
        List<Client> clients = clientService.getAll();
        assertEquals("Clients were not found database!", 2, clients.size());
    }

    @Test
    public void shouldGetProjetByClient() throws Exception {
        Client client = clientService.getById(1);
        assertEquals("Clients were not found database!", 2, client.getProjects().size());
    }

    @Test
    public void shouldFindAllAuthorizations() throws Exception {
        List<ClientDTO> clients = clientService.findAll();
        assertEquals("Not all clients were found in database!", 2, clients.size());
    }

    @Test
    public void shouldFindById() throws Exception {
        ClientDTO client = clientService.findById(2);
        String actual = client.getName();
        String expected = "Second client";
        assertEquals("User group was not found in index!", expected, actual);
    }
}
