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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.UserProperty;
import org.kitodo.data.database.exceptions.DAOException;

/**
 * Tests for UserPropertyService class.
 */
public class UserPropertyServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException, IOException {
        MockDatabase.insertProcessesFull();
        MockDatabase.insertUserProperties();
    }

    @AfterClass
    public static void cleanDatabase() {
        //MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldFindProcessUser() throws Exception {
        UserPropertyService userPropertyService = new UserPropertyService();

        UserProperty userProperty = userPropertyService.find(1);
        boolean condition = userProperty.getTitle().equals("First Property") && userProperty.getValue().equals("first value");
        assertTrue("User property was not found in database!", condition);
    }

    @Test
    public void shouldFindAllUsers() throws Exception {
        UserPropertyService userPropertyService = new UserPropertyService();

        List<UserProperty> userProperties = userPropertyService.findAll();
        assertEquals("Not all user properties were found in database!", 2, userProperties.size());
    }

    @Test
    public void shouldGetNormalizedTitle() throws Exception {
        UserPropertyService userPropertyService = new UserPropertyService();

        UserProperty userProperty = userPropertyService.find(1);
        String expected = "First_Property";
        String actual = userPropertyService.getNormalizedTitle(userProperty);
        assertEquals("Normalized title doesn't match to given plain text!", expected, actual);
    }

    @Test
    public void shouldGetNormalizedValue() throws Exception {
        UserPropertyService userPropertyService = new UserPropertyService();

        UserProperty userProperty = userPropertyService.find(1);
        String expected = "first_value";
        String actual = userPropertyService.getNormalizedValue(userProperty);
        assertEquals("Normalized value doesn't match to given plain text!", expected, actual);
    }
}
