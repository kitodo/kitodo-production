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

package org.kitodo.model;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.dto.UserDTO;
import org.kitodo.services.ServiceManager;
import org.primefaces.model.SortOrder;

public class LazyDTOModelIT {

    private ServiceManager serviceManager = new ServiceManager();
    private LazyDTOModel lazyDTOModel = new LazyDTOModel(serviceManager.getUserService());

    @Before
    public void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertUserGroupsFull();
    }

    @After
    public void tearDown() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldLoad() throws Exception {
        List users = lazyDTOModel.load(0, 2, "login", SortOrder.ASCENDING, null);
        UserDTO user = (UserDTO) users.get(0);
        Assert.assertEquals(user.getLogin(), "dora");
    }

}
