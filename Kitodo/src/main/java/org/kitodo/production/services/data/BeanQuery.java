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

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.data.database.beans.BaseBean;
import org.primefaces.model.SortOrder;

public class BeanQuery {

    private String objectClass;
    private String varName;
    private Collection<String> restrictions = new ArrayList<>();
    private Pair<String, String> sorting = Pair.of("id", "ASC");
    private Map<String, Object> parameters = new HashMap<>();

    public BeanQuery(Class<? extends BaseBean> beanClass) {
        objectClass = beanClass.getSimpleName();
        varName = objectClass.toLowerCase();
    }

    /**
     * Requires that the hits in a specific column must have a specific value.
     * 
     * @param fieldName
     *            class field that must have the specified value
     * @param value
     *            value that the field must have
     */
    public void addBooleanRestriction(String fieldName, Boolean value) {
        String joker = varName(fieldName);
        restrictions.add(varName + '.' + fieldName + " = :" + joker);
        parameters.put(joker, value);
    }

    /**
     * Requires that the hits must correspond to any of the specified values in
     * the specified class field.
     * 
     * @param fieldName
     *            class field in which the value must be
     * @param values
     *            value that the class field must accept one of
     */
    public void addInCollectionRestriction(String fieldName, Collection<?> values) {
        String parameterName = varName(fieldName);
        restrictions.add(varName + '.' + fieldName + " IN (:" + parameterName + ')');
        parameters.put(parameterName, values);
    }

    /**
     * Requires that the hits in a specific column must have a specific value.
     * 
     * @param column
     *            column that must have the specified value
     * @param value
     *            value that the column must have
     */
    public void addIntegerRestriction(String column, int value) {
        String parameterName = varName(column);
        restrictions.add(varName + '.' + column + " = :" + parameterName);
        parameters.put(parameterName, value);
    }

    /**
     * Requires that the hits do not correspond to any of the specified values
     * in the specified database column ​​(exclusion).
     * 
     * @param column
     *            column in which the value must not be
     * @param values
     *            value that the column must not accept
     */
    public void addNotInCollectionRestriction(String column, Collection<Integer> values) {
        String parameterName = varName(column);
        restrictions.add(varName + '.' + column + " NOT IN (:" + parameterName + ')');
        parameters.put(parameterName, values);
    }


    public void addNullRestriction(String column) {
        restrictions.add(varName + '.' + column + " IS NULL");
    }

    /**
     * Requires that the search only finds objects where the user input either
     * matches the record number, or is part of the <i>title</i>. Title here
     * means the label. If the input is not a number, the first option is
     * omitted.
     * 
     * @param searchInput
     *            single line input by the user
     */
    public void forIdOrInTitle(String searchInput) {
        try {
            Integer possibleId = Integer.valueOf(searchInput);
            restrictions.add('(' + varName + ".id = :possibleId OR " + varName + ".title LIKE '%:searchInput%')");
            parameters.put("possibleId", possibleId);
            parameters.put("searchInput", searchInput);
        } catch (NumberFormatException e) {
            restrictions.add(varName + ".title LIKE '%:searchInput%'");
            parameters.put("searchInput", searchInput);
        }
    }

    /**
     * Requires that the query only find objects owned by the specified client.
     * 
     * @param sessionClientId
     *            client record number
     */
    public void restrictToClient(int sessionClientId) {
        switch (objectClass) {
            case "Process":
                restrictions.add(varName + ".project.client.id = :sessionClientId");
                break;
            case "Task":
                restrictions.add(varName + ".process.project.client.id = :sessionClientId");
                break;
            default:
                throw new IllegalStateException("complete switch");
        }
        parameters.put("sessionClientId", sessionClientId);
    }

    public void restrictToNotCompletedProcesses() {
        restrictions.add(varName + ".sortHelperStatus != '100000000000'");
    }

    /**
     * Requires that the search only find items that belong to one of the
     * specified projects.
     * 
     * @param projectIDs
     *            record numbers of the projects to which the hits may belong
     */
    public void restrictToProjects(Collection<Integer> projectIDs) {
        switch (objectClass) {
            case "Process":
                restrictions.add(varName + ".project.id IN (:projectIDs)");
                break;
            case "Task":
                restrictions.add(varName + ".process.project.id IN (:projectIDs)");
                break;
            default:
                throw new IllegalStateException("complete switch");
        }
        parameters.put("projectIDs", projectIDs);
    }

    public void restrictWithUserFilterString(String s) {
        // full user filters not yet implemented
        forIdOrInTitle(s);
    }

    public void defineSorting(String sortField, SortOrder sortOrder) {
        sorting = Pair.of(varName + '.' + sortField, SortOrder.DESCENDING.equals(sortOrder) ? "DESC" : "ASC");
    }

    /**
     * Forms and returns a query to count all objects.
     * 
     * @return a query to count all objects
     */
    public String formCountQuery() {
        String query = "SELECT COUNT(*) FROM " + objectClass;
        if (!restrictions.isEmpty()) {
            query += " AS " + varName + " WHERE " + String.join(" AND ", restrictions);
        }
        return query;
    }

    /**
     * Forms and returns a query for all objects.
     * 
     * @return a query for all objects
     */
    public String formQueryForAll() {
        String query = "FROM " + objectClass + " AS " + varName;
        if (!restrictions.isEmpty()) {
            query += " WHERE " + String.join(" AND ", restrictions);
        }
        query += " ORDER BY " + sorting.getKey() + ' ' + sorting.getValue();
        return query;
    }

    public Map<String, Object> getQueryParameters() {
        return parameters;
    }

    private String varName(String input) {
        StringBuilder result = new StringBuilder();
        CharacterIterator inputIterator = new StringCharacterIterator(input);
        boolean upperCase = false;
        while (inputIterator.current() != CharacterIterator.DONE) {
            char currentChar = inputIterator.current();
            if (currentChar < '0' || (currentChar > '9' && currentChar < 'A')
                    || (currentChar > 'Z' && currentChar < 'a') || currentChar > 'z') {
                upperCase = true;
            } else {
                result.append(upperCase ? Character.toUpperCase(currentChar) : Character.toLowerCase(currentChar));
                upperCase = false;
            }
            inputIterator.next();
        }
        return result.toString();
    }
}
