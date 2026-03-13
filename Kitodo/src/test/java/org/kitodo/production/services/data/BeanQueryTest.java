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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.production.enums.ProcessState;
import org.primefaces.model.SortOrder;

public class BeanQueryTest {
    @Test
    public void shouldConstructBeanQuery() {
        BeanQuery processQuery = new BeanQuery(Process.class);
        assertThat("should construct HQL query for process", processQuery.formQueryForAll(), startsWith(
            "FROM Process "));

        BeanQuery taskQuery = new BeanQuery(Task.class);
        assertThat("should construct HQL query for task", taskQuery.formQueryForAll(), startsWith("FROM Task "));

        BeanQuery rulesetQuery = new BeanQuery(Ruleset.class);
        assertThat("should construct HQL query for ruleset", rulesetQuery.formQueryForAll(), startsWith(
            "FROM Ruleset "));
    }

    @Test
    public void shouldAddBooleanRestriction() {
        BeanQuery beanQuery = new BeanQuery(Process.class);
        beanQuery.addBooleanRestriction("project.active", Boolean.TRUE);
        assertThat("should construct HQL query with boolean restriction", beanQuery.formQueryForAll(), containsString(
            "WHERE process.project.active = :projectActive"));
        assertThat("should define projectActive as TRUE", beanQuery.getQueryParameters().get("projectActive"), is(
            Boolean.TRUE));
    }

    @Test
    public void shouldAddInCollectionRestriction() {
        List<TaskStatus> taskStatus = Arrays.asList(TaskStatus.OPEN, TaskStatus.INWORK);

        BeanQuery beanQuery = new BeanQuery(Process.class);
        beanQuery.addInCollectionRestriction("processingStatus", taskStatus);
        assertThat("should construct HQL query with in collection restriction", beanQuery.formQueryForAll(),
            containsString("WHERE process.processingStatus IN (:processingstatus)"));
        assertThat("should define processingstatus as List<TaskStatus>", beanQuery.getQueryParameters().get(
            "processingstatus"), is(equalTo(taskStatus)));
    }

    @Test
    public void shouldAddIntegerRestriction() {
        BeanQuery beanQuery = new BeanQuery(Task.class);
        beanQuery.addIntegerRestriction("processingUser.id", 42);
        assertThat("should construct HQL query with integer restriction", beanQuery.formQueryForAll(), containsString(
            "WHERE task.processingUser.id = :processinguserId"));
        assertThat("should define processinguserId as 42", beanQuery.getQueryParameters().get("processinguserId"), is(
            equalTo(42)));
    }

    @Test
    public void shouldAddNotInCollectionRestriction() {
        Collection<Integer> excludedProcessIds = Arrays.asList(2, 3, 5, 7, 11, 13, 17, 19, 23);

        BeanQuery beanQuery = new BeanQuery(Process.class);
        beanQuery.addNotInCollectionRestriction("id", excludedProcessIds);
        assertThat("should construct HQL query with not in collection restriction", beanQuery.formQueryForAll(),
            containsString("WHERE process.id NOT IN (:id)"));
        assertThat("should define id as Collection<Integer>", beanQuery.getQueryParameters().get("id"), is(equalTo(
            excludedProcessIds)));
    }

    @Test
    public void shouldAddNullRestriction() {
        BeanQuery beanQuery = new BeanQuery(Process.class);
        beanQuery.addNullRestriction("parent.id");
        assertThat("should construct HQL query with null restriction", beanQuery.formQueryForAll(), containsString(
            "WHERE process.parent.id IS NULL"));
        assertThat("should return no parameters", beanQuery.getQueryParameters().size(), is(equalTo(0)));
    }

    @Test
    public void shouldAddStringRestriction() {
        BeanQuery beanQuery = new BeanQuery(Ruleset.class);
        beanQuery.addStringRestriction("title", "Ruleset_Test");
        assertThat("should construct HQL query with string restriction", beanQuery.formQueryForAll(), startsWith(
            "FROM Ruleset AS ruleset WHERE ruleset.title = :title"));
        assertThat("should define title as Ruleset_Test", beanQuery.getQueryParameters().get("title"), is(equalTo(
            "Ruleset_Test")));
    }

    @Test
    public void shouldAddXIdRestriction() {
        BeanQuery beanQuery = new BeanQuery(Project.class);
        beanQuery.addXIdRestriction("users", 42);
        assertThat("should construct HQL query with id restriction on many-to-many relationship", beanQuery
                .formQueryForAll(), startsWith(
                    "SELECT project FROM Project AS project INNER JOIN project.users AS user WITH user.id = :userId"));
        assertThat("should define userId as 42", beanQuery.getQueryParameters().get("userId"), is(equalTo(42)));
    }

    @Test
    public void shouldSearchForIdOrInTitle() {
        BeanQuery numberQuery = new BeanQuery(Process.class);
        numberQuery.forIdOrInTitle("42");
        assertThat("should construct HQL query with 42 as ID or in title", numberQuery.formQueryForAll(),
            containsString("WHERE (process.id = :possibleId OR process.title LIKE :searchInput)"));
        Map<String, Object> queryParameters = numberQuery.getQueryParameters();
        assertThat("should define possibleId as 42", queryParameters.get("possibleId"), is(equalTo(42)));
        assertThat("should define searchInput as '%42%'", queryParameters.get("searchInput"), is(equalTo("%42%")));

        BeanQuery textQuery = new BeanQuery(Process.class);
        textQuery.forIdOrInTitle("LoremIpsum");
        assertThat("should construct HQL query with LoremIpsum in title", textQuery.formQueryForAll(), containsString(
            "WHERE process.title LIKE :searchInput"));
        assertThat("should define searchInput as '%LoremIpsum%'", textQuery.getQueryParameters().get("searchInput"),
            is(equalTo("%LoremIpsum%")));
    }

    @Test
    public void shouldRestrictToClient() {
        BeanQuery processQuery = new BeanQuery(Process.class);
        processQuery.restrictToClient(1);
        assertThat("should construct HQL query for client", processQuery.formQueryForAll(), containsString(
            "WHERE process.project.client.id = :sessionClientId"));
        assertThat("should define sessionClientId as 1", processQuery.getQueryParameters().get("sessionClientId"), is(
            equalTo(1)));

        BeanQuery taskQuery = new BeanQuery(Task.class);
        taskQuery.restrictToClient(1);
        assertThat("should construct HQL query for client", taskQuery.formQueryForAll(), containsString(
            "WHERE task.process.project.client.id = :sessionClientId"));
    }

    @Test
    public void shouldRestrictToNotCompletedProcesses() {
        BeanQuery beanQuery = new BeanQuery(Process.class);
        beanQuery.restrictToNotCompletedProcesses();
        assertThat("should construct HQL query for not completed processes", beanQuery.formQueryForAll(),
            containsString("WHERE (process.sortHelperStatus IS NULL OR process.sortHelperStatus != :completedState)"));
        assertThat("should define completedState", beanQuery.getQueryParameters().get("completedState"), is(equalTo(
            ProcessState.COMPLETED.getValue())));
    }

    @Test
    public void shouldRestrictToProjects() {
        Collection<Integer> projectIDs = Arrays.asList(1, 2, 3);
        BeanQuery processQuery = new BeanQuery(Process.class);
        processQuery.restrictToProjects(projectIDs);
        assertThat("should construct HQL query for projects", processQuery.formQueryForAll(), containsString(
            "WHERE process.project.id IN (:projectIDs)"));
        assertThat("should define projectIDs as Collection<Integer>", processQuery.getQueryParameters().get(
            "projectIDs"), is(equalTo(projectIDs)));

        BeanQuery taskQuery = new BeanQuery(Task.class);
        taskQuery.restrictToProjects(projectIDs);
        assertThat("should construct HQL query for projects", taskQuery.formQueryForAll(), containsString(
            "WHERE task.process.project.id IN (:projectIDs)"));
    }

    @Test
    public void shouldRestrictToRoles() {
        List<Role> roles = Arrays.asList(new Role());

        BeanQuery beanQuery = new BeanQuery(Task.class);
        beanQuery.restrictToRoles(roles);
        assertThat("should construct HQL query for roles", beanQuery.formQueryForAll(), startsWith(
            "FROM Task AS task WHERE EXISTS (SELECT 1 FROM task.roles r WHERE r IN (:userRoles))"));
        assertThat("should define userRoles as Collection<Role>", beanQuery.getQueryParameters().get("userRoles"),
            is(equalTo(roles)));
    }

    @Test
    public void shouldDefineSorting() {
        BeanQuery beanQuery = new BeanQuery(Process.class);
        assertThat("should construct HQL query sorting by ID", beanQuery.formQueryForAll(), containsString(
            "ORDER BY process.id ASC"));
        beanQuery.defineSorting("title", SortOrder.DESCENDING);
        assertThat("should construct HQL query sorting by title descending", beanQuery.formQueryForAll(),
            containsString("ORDER BY process.title DESC"));
    }

    @Test
    public void shouldSetUnordered() {
        BeanQuery beanQuery = new BeanQuery(Process.class);
        assertThat("should construct HQL query sorting by ID", beanQuery.formQueryForAll(), containsString(
            "ORDER BY process.id ASC"));
        beanQuery.setUnordered();
        assertThat("should construct HQL query without sorting", beanQuery.formQueryForAll(), not(containsString(
            "ORDER BY")));
    }

    @Test
    public void shouldFormCountQuery() {
        BeanQuery beanQuery = new BeanQuery(Process.class);
        String countQuery = beanQuery.formCountQuery();
        assertThat("should construct HQL count query", countQuery, startsWith("SELECT COUNT(*) FROM Process"));
        assertThat("count query should not define sorting", countQuery, not(containsString("ORDER BY")));
    }

    @Test
    public void shouldFormQueryForDistinct() {
        BeanQuery beanQuery = new BeanQuery(Process.class);
        assertThat("should construct HQL distinct query", beanQuery.formQueryForDistinct("title", false), is(equalTo(
            "SELECT DISTINCT process.title FROM Process AS process")));
    }
}
