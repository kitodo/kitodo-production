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

package org.kitodo.production.services.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.LdapGroupDAO;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.primefaces.model.SortOrder;

public class LdapGroupService extends SearchDatabaseService<LdapGroup, LdapGroupDAO> {

    public LdapGroupService() {
        super(new LdapGroupDAO());
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM LdapGroup");
    }

    @Override
    public Long countResults(Map filters) throws DAOException {
        return countDatabaseRows();
    }

    @Override
    public List<LdapGroup> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        return new ArrayList<>();
    }

    /**
     * Duplicate the LDAP group with the given ID 'itemId'.
     *
     * @return the duplicated LdapGroup
     */
    public LdapGroup duplicateLdapGroup(LdapGroup baseLdapGroup) {
        LdapGroup duplicatedLdapGroup = new LdapGroup();

        // LDAP group _title_ and _displayName_ should explicitly _not_ be duplicated!
        duplicatedLdapGroup.setHomeDirectory(baseLdapGroup.getHomeDirectory());
        duplicatedLdapGroup.setGidNumber(baseLdapGroup.getGidNumber());
        duplicatedLdapGroup.setUserDN(baseLdapGroup.getUserDN());
        duplicatedLdapGroup.setObjectClasses(baseLdapGroup.getObjectClasses());
        duplicatedLdapGroup.setSn(baseLdapGroup.getSn());
        duplicatedLdapGroup.setUid(baseLdapGroup.getUid());
        duplicatedLdapGroup.setDescription(baseLdapGroup.getDescription());
        duplicatedLdapGroup.setGecos(baseLdapGroup.getGecos());
        duplicatedLdapGroup.setLoginShell(baseLdapGroup.getLoginShell());

        duplicatedLdapGroup.setSambaSID(baseLdapGroup.getSambaSID());
        duplicatedLdapGroup.setSambaAcctFlags(baseLdapGroup.getSambaAcctFlags());
        duplicatedLdapGroup.setSambaKickoffTime(baseLdapGroup.getSambaKickoffTime());
        duplicatedLdapGroup.setSambaLogonHours(baseLdapGroup.getSambaLogonHours());
        duplicatedLdapGroup.setSambaLogonScript(baseLdapGroup.getSambaLogonScript());
        duplicatedLdapGroup.setSambaPasswordHistory(baseLdapGroup.getSambaPasswordHistory());
        duplicatedLdapGroup.setSambaPrimaryGroupSID(baseLdapGroup.getSambaPrimaryGroupSID());
        duplicatedLdapGroup.setSambaPwdMustChange(baseLdapGroup.getSambaPwdMustChange());

        duplicatedLdapGroup.setLdapServer(baseLdapGroup.getLdapServer());

        return duplicatedLdapGroup;
    }
}
