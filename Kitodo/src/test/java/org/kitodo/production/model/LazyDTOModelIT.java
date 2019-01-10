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

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.production.dto.ClientDTO;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ClientService;
import org.primefaces.model.SortOrder;

public class LazyDTOModelIT {

    private static ClientService clientService = ServiceManager.getClientService();
    private static LazyDTOModel lazyDTOModel = null;

    @BeforeClass
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertClients();
        lazyDTOModel = new LazyDTOModel(clientService);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldGetRowData() throws Exception {
        List clients = clientService.getAll();
        ClientDTO firstClient = (ClientDTO) clients.get(0);
        ClientDTO lazyClient = (ClientDTO) lazyDTOModel.getRowData(String.valueOf(firstClient.getId()));
        Assert.assertEquals(firstClient.getName(), lazyClient.getName());
    }

    @Test
    public void shouldLoad() {
        List clients = lazyDTOModel.load(0, 2, "clientName", SortOrder.ASCENDING, null);
        ClientDTO client = (ClientDTO) clients.get(0);
        Assert.assertEquals("Second client", client.getName());
    }
}
