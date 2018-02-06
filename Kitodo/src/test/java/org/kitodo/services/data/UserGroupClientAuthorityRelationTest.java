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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.beans.UserGroupClientAuthorityRelation;
import org.kitodo.services.ServiceManager;

public class UserGroupClientAuthorityRelationTest {

    private ServiceManager serviceManager = new ServiceManager();

    @Test
    public void shouldGetAuthoritiesOfRelation() {

        UserGroup userGroup = new UserGroup();
        userGroup.setTitle("testGroup1");
        Client client = new Client();
        client.setName("TestClient1");
        Authority authority = new Authority();
        authority.setTitle("TestAuthority1");

        List<UserGroupClientAuthorityRelation> userGroupClientAuthorityRelations = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            userGroupClientAuthorityRelations.add(new UserGroupClientAuthorityRelation(userGroup, client, authority));
        }

        Client secondClient = new Client();
        secondClient.setName("TestClient2");

        userGroupClientAuthorityRelations.get(3).setClient(secondClient);
        userGroupClientAuthorityRelations.get(4).setClient(secondClient);

        List<Authority> authorities = serviceManager.getUserGroupClientAuthorityRelationService()
                .getAuthoritiesFromListByClientAndUserGroup(userGroup, client, userGroupClientAuthorityRelations);

        assertEquals("Number of returned authorities are not matching", 4, authorities.size());

    }
}
