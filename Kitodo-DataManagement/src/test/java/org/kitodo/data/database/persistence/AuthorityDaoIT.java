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

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.exceptions.DAOException;

public class AuthorityDaoIT {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void runPersistenceSuitTest() throws DAOException {
        List<Authority> authorities = getAuthorities();

        AuthorityDAO authorityDAO = new AuthorityDAO();
        authorityDAO.save(authorities.get(0));
        authorityDAO.save(authorities.get(1));
        authorityDAO.save(authorities.get(2));

        assertEquals("Objects were not saved or not found!", 3, authorityDAO.getAll().size());
        assertEquals("Objects were not saved or not found!", 2, authorityDAO.getAll(1,2).size());
        assertEquals("Object was not saved or not found!", "first_authority", authorityDAO.getById(1).getTitle());

        authorityDAO.remove(1);
        authorityDAO.remove(authorities.get(1));
        assertEquals("Objects were not removed or not found!", 1, authorityDAO.getAll().size());

        exception.expect(DAOException.class);
        exception.expectMessage("Object cannot be found in database");
        authorityDAO.getById(1);
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
