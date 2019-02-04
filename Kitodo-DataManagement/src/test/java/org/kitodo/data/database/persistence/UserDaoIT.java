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

package org.kitodo.data.database.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;

public class UserDaoIT {

    @Test
    public void runPersistenceSuitTest() throws DAOException {
        List<User> users = getAuthorities();

        UserDAO userDAO = new UserDAO();
        userDAO.save(users.get(0));
        userDAO.save(users.get(1));
        userDAO.save(users.get(2));

        assertEquals("Objects were not saved or not found!", 3, userDAO.getAll().size());
        assertEquals("Objects were not saved or not found!", 2, userDAO.getAll(1,2).size());
        assertEquals("Object was not saved or not found!", "first_user", userDAO.getById(1).getLogin());

        userDAO.remove(1);
        userDAO.remove(users.get(1));
        assertEquals("Objects were not removed or not found!", 1, userDAO.getAll().size());

        User deletedUser = userDAO.getById(1);
        assertTrue("Object was not saved or not removed!", deletedUser.isDeleted());
    }

    private List<User> getAuthorities() {
        User firstUser = new User();
        firstUser.setLogin("first_user");

        User secondUser = new User();
        secondUser.setLogin("second_user");

        User thirdUser = new User();
        thirdUser.setLogin("third_user");

        List<User> users = new ArrayList<>();
        users.add(firstUser);
        users.add(secondUser);
        users.add(thirdUser);
        return users;
    }
}
