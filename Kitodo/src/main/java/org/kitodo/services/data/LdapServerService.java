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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.LdapServer;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.LdapServerDAO;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.SearchDatabaseService;

import java.util.Objects;

public class LdapServerService extends SearchDatabaseService<LdapServer, LdapServerDAO> {

    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(LdapServerService.class);
    private static LdapServerService instance = null;

    /**
     * Return singleton variable of type LdapServerService.
     *
     * @return unique instance of LdapServerService
     */
    public static LdapServerService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (HistoryService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new LdapServerService();
                }
            }
        }
        return instance;
    }

    public LdapServerService() {
        super(new LdapServerDAO());
    }

    public void save(LdapServer ldapServer) throws DAOException {
        dao.save(ldapServer);
    }

    public void remove(LdapServer ldapServer) throws DAOException {
        dao.remove(ldapServer);
    }

    public void remove(Integer id) throws DAOException {
        dao.remove(id);
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("FROM LdapGroup");
    }
}
