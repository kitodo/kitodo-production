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
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.exceptions.DAOException;

public class ClientDaoIT {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void runPersistenceSuitTest() throws DAOException {
        List<Client> clients = getAuthorities();

        ClientDAO clientDAO = new ClientDAO();
        clientDAO.save(clients.get(0));
        clientDAO.save(clients.get(1));
        clientDAO.save(clients.get(2));

        assertEquals("Objects were not saved or not found!", 3, clientDAO.getAll().size());
        assertEquals("Objects were not saved or not found!", 2, clientDAO.getAll(1,2).size());
        assertEquals("Object was not saved or not found!", "first_client", clientDAO.getById(1).getName());

        clientDAO.remove(1);
        clientDAO.remove(clients.get(1));
        assertEquals("Objects were not removed or not found!", 1, clientDAO.getAll().size());

        exception.expect(DAOException.class);
        exception.expectMessage("Object cannot be found in database");
        clientDAO.getById(1);
    }

    private List<Client> getAuthorities() {
        Client firstClient = new Client();
        firstClient.setName("first_client");

        Client secondClient = new Client();
        secondClient.setName("second_client");

        Client thirdClient = new Client();
        thirdClient.setName("third_client");

        List<Client> clients = new ArrayList<>();
        clients.add(firstClient);
        clients.add(secondClient);
        clients.add(thirdClient);
        return clients;
    }
}
