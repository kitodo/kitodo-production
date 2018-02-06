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

package org.kitodo.data.database.beans;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class UserGroupTest {

    @Test
    public void shouldGetAuthoritiesByClient() {

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

        userGroup.setUserGroupClientAuthorityRelations(userGroupClientAuthorityRelations);

        List<Authority> authorities = userGroup.getAuthoritiesByClient(client);
        assertEquals("Number of returned authorities are not matching", 4, authorities.size());
    }

    @Test
    public void shouldGetAuthoritiesByProject() {

        UserGroup userGroup = new UserGroup();
        userGroup.setTitle("testGroup1");
        Project project = new Project();
        project.setTitle("TestProject1");
        Authority authority = new Authority();
        authority.setTitle("TestAuthority1");

        List<UserGroupProjectAuthorityRelation> userGroupProjectAuthorityRelations = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            userGroupProjectAuthorityRelations
                    .add(new UserGroupProjectAuthorityRelation(userGroup, project, authority));
        }

        Project secondProject = new Project();
        secondProject.setTitle("TestProject2");

        userGroupProjectAuthorityRelations.get(3).setProject(secondProject);
        userGroupProjectAuthorityRelations.get(4).setProject(secondProject);

        userGroup.setUserGroupProjectAuthorityRelations(userGroupProjectAuthorityRelations);

        List<Authority> authorities = userGroup.getAuthoritiesByProject(project);
        assertEquals("Number of returned authorities are not matching", 5, authorities.size());
    }
}
