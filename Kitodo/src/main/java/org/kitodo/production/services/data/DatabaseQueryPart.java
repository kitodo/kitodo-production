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

import java.util.Map;
import java.util.Objects;

/**
 * A part of the filter searching on the database for a name.
 */
public class DatabaseQueryPart implements UserSpecifiedFilter {

    static final String SQL_FALSE = "1 != 1";
    protected FilterField filterField;
    protected boolean operand;
    private String value;

    /**
     * Creates a new part of the search engine to be resolved on the database.
     *
     * @param filterField
     *            search field chosen by the user
     * @param value
     *            what the user wants to find
     * @param operand
     *            whether it should be found
     */
    DatabaseQueryPart(FilterField filterField, String value, boolean operand) {
        this.filterField = filterField;
        this.operand = operand;
        this.value = likeValue(filterField.getLikeSearch(), value);
    }

    /**
     * Returns a value like the search value, depending on the like search mode
     * specified for the search field.
     * 
     * @param likeSearch
     *            like search setting
     * @param value
     *            user entered search value
     * @return search value for like search
     */
    private static final String likeValue(LikeSearch likeSearch, String value) {
        boolean asteriskAllowed = Objects.equals(likeSearch, LikeSearch.ALLOWED);
        if (!asteriskAllowed) {
            value = value.replace("*", "");
        }
        if (Objects.equals(likeSearch, LikeSearch.ALWAYS_RIGHT)) {
            return value.concat("%");
        }
        if (asteriskAllowed) {
            return value.replace("*", "%");
        } else
            return value;
    }

    /**
     * Creates a new part of the search engine to be resolved on the database.
     *
     * @param filterField
     *            search field chosen by the user
     * @param operand
     *            whether it should be found
     */
    protected DatabaseQueryPart(FilterField filterField, boolean operand) {
        this.filterField = filterField;
        this.operand = operand;
    }

    /**
     * Returns the database search query.
     * 
     * @param className
     *            which objects to search
     * @param varName
     *            HQL query variable representing the object to search
     * @param parameterName
     *            string of the parameter for the search value
     * @return the database search query
     */
    String getDatabaseQuery(String className, String varName, String parameterName) {
        String query = Objects.equals(className, "Task") ? filterField.getTaskTitleQuery()
                : filterField.getProcessTitleQuery();
        if (Objects.isNull(query)) {
            return SQL_FALSE;
        }
        query = query.contains("~") ? query.replace("~", varName) : varName + '.' + query;
        query = query.contains("#") ? query.replace("#", parameterName)
                : query + (value.contains("%") ? " LIKE :" : " = :") + parameterName;
        return operand ? query : "NOT (" + query + ')';
    }

    /**
     * Adds the parameters for the search query.
     * 
     * @param parameterName
     *            string of the parameter for the search value
     * @param parameters
     *            map to which the parameters should be added
     */
    void addParameters(String parameterName, Map<String, Object> parameters) {
        if (Objects.nonNull(filterField.getQueryObject())) {
            parameters.put("queryObject", filterField.getQueryObject());
        }
        parameters.put(parameterName, value);
    }

    @Override
    public FilterField getFilterField() {
        return filterField;
    }
}
