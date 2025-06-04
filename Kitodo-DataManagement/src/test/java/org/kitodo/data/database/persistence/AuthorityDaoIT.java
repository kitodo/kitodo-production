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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockIndex;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.exceptions.DAOException;

public class AuthorityDaoIT {

    @BeforeAll
    public static void setUp() throws Exception {
        MockIndex.startNode();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        MockIndex.stopNode();
    }

    @Test
    public void runPersistenceSuitTest() throws DAOException {
        List<Authority> authorities = getAuthorities();

        AuthorityDAO authorityDAO = new AuthorityDAO();
        authorityDAO.save(authorities.get(0));
        authorityDAO.save(authorities.get(1));
        authorityDAO.save(authorities.get(2));

        assertEquals(3, authorityDAO.getAll().size(), "Objects were not saved or not found!");
        assertEquals(2, authorityDAO.getAll(1,2).size(), "Objects were not saved or not found!");
        assertEquals("first_authority", authorityDAO.getById(1).getTitle(), "Object was not saved or not found!");

        authorityDAO.remove(1);
        authorityDAO.remove(authorities.get(1));
        assertEquals(1, authorityDAO.getAll().size(), "Objects were not removed or not found!");

        Exception exception = assertThrows(DAOException.class,
            () -> authorityDAO.getById(1));
        assertEquals("Object cannot be found in database", exception.getMessage());
    }

    private List<Authority> getAuthorities() {
        Authority firstAuthority = new Authority();
        firstAuthority.setTitle("first_authority");

        Authority secondAuthority = new Authority();
        secondAuthority.setTitle("second_authority");

        Authority thirdAuthority = new Authority();
        thirdAuthority.setTitle("third_authority");

        List<Authority> authorities = new ArrayList<>();
        authorities.add(firstAuthority);
        authorities.add(secondAuthority);
        authorities.add(thirdAuthority);
        return authorities;
    }
}
