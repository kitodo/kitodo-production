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
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.dto.UserDTO;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.UserService;
import org.primefaces.model.SortOrder;

public class LazyDTOModelIT {

    private static final ServiceManager serviceManager = new ServiceManager();
    private static UserService userService = serviceManager.getUserService();
    private static LazyDTOModel lazyDTOModel = null;

    /**
     * Performs computationally expensive setup shared several tests. This
     * compromises the independence of the tests, bit is a necessary
     * optimization here.
     * 
     * @throws Exception
     *             if something goes wrong
     */
    @BeforeClass
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertUserGroupsFull();
        lazyDTOModel = new LazyDTOModel(userService);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldGetRowData() throws Exception {
        List users = userService.findAll();
        UserDTO firstUser = (UserDTO) users.get(0);
        UserDTO lazyUser = (UserDTO) lazyDTOModel.getRowData(String.valueOf(firstUser.getId()));
        Assert.assertEquals(firstUser.getLogin(), lazyUser.getLogin());
    }

    @Test
    public void shouldLoad() throws Exception {
        List users = lazyDTOModel.load(0, 2, "login", SortOrder.ASCENDING, null);
        UserDTO user = (UserDTO) users.get(0);
        Assert.assertEquals("dora", user.getLogin());
    }
}
