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

package org.kitodo.production.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ClientService;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

public class LazyBeanModelIT {

    private static final ClientService clientService = ServiceManager.getClientService();
    private static LazyBeanModel lazyBeanModel = null;

    @BeforeAll
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertClients();
        lazyBeanModel = new LazyBeanModel(clientService);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldGetRowData() throws Exception {
        List<?> clients = clientService.getAll();
        Client firstClient = (Client) clients.getFirst();
        Client lazyClient = (Client) lazyBeanModel.getRowData(String.valueOf(firstClient.getId()));
        assertEquals(firstClient.getName(), lazyClient.getName());
    }

    @Test
    public void shouldLoadFromDatabase() {
        Map<String, SortMeta> sortNameAsc = Map.of("name", SortMeta.builder().field("name").order(SortOrder.ASCENDING).build());
        Map<String, SortMeta> sortNameDesc = Map.of("name", SortMeta.builder().field("name").order(SortOrder.DESCENDING).build());

        List<?> clients = lazyBeanModel.load(0, 2, sortNameAsc, null);
        assertEquals(2, clients.size());

        clients = lazyBeanModel.load(0, 10, sortNameAsc, null);
        assertEquals(3, clients.size());

        Client client = (Client) clients.getFirst();
        assertEquals("First client", client.getName());

        clients = lazyBeanModel.load(0, 2, sortNameDesc, null);
        client = (Client) clients.getFirst();
        assertEquals("Second client", client.getName());
    }

    @Test
    public void shouldLoadFromIndex() throws Exception {
        MockDatabase.cleanDatabase();
        MockDatabase.insertForAuthenticationTesting();
        MockDatabase.insertDockets();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);

        LazyBeanModel lazyBeanModelDocket = new LazyBeanModel(ServiceManager.getDocketService());

        Map<String, SortMeta> sortTitleAsc = Map.of("title", SortMeta.builder().field("title").order(SortOrder.ASCENDING).build());
        Map<String, SortMeta> sortTitleDesc = Map.of("title", SortMeta.builder().field("title").order(SortOrder.DESCENDING).build());

        List<?> dockets = lazyBeanModelDocket.load(0, 2, sortTitleAsc, null);
        assertEquals(2, dockets.size());

        Docket docket = (Docket) dockets.getFirst();
        assertEquals("default", docket.getTitle());

        dockets = lazyBeanModelDocket.load(0, 2, sortTitleDesc, null);
        docket = (Docket) dockets.getFirst();
        assertEquals("tester", docket.getTitle());
    }
}
