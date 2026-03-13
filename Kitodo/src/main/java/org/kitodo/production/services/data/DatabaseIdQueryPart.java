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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A part of the filter searching on the database for an ID or ID range.
 */
class DatabaseIdQueryPart extends DatabaseQueryPart {

    private static final Pattern NON_DIGITS = Pattern.compile("\\D+");
    private static final String SECOND_PARAMETER_EXTENSION = "upTo";

    private final Integer firstId;
    private final Integer upToId;
    private final List<Integer> idList;

    /**
     * Constructor, creates a new DatabaseQueryPart.
     * 
     * @param filterField
     *            field to search on
     * @param firstId
     *            first or only ID
     * @param upToId
     *            {@code null} or last ID
     * @param operand
     *            include in search
     */
    DatabaseIdQueryPart(FilterField filterField, String firstId, String upToId, boolean operand) {
        super(filterField, operand);
        this.firstId = Integer.valueOf(firstId);
        this.upToId = Objects.nonNull(upToId) ? Integer.valueOf(upToId) : null;
        this.idList = null;
    }

    /**
     * Constructor, creates a new DatabaseQueryPart.
     * 
     * @param filterField
     *            field to search on
     * @param ids
     *            one or more IDs, separated by non-digit characters (such as
     *            space)
     * @param operand
     *            include in search
     */
    DatabaseIdQueryPart(FilterField filterField, String ids, boolean operand) {
        super(filterField, operand);
        List<Integer> idList = NON_DIGITS.splitAsStream(ids).filter(Predicate.not(String::isEmpty)).map(
            Integer::valueOf).collect(Collectors.toList());
        boolean multipleIDs = idList.size() > 1;
        this.firstId = multipleIDs ? null : idList.getFirst();
        this.idList = multipleIDs ? idList : null;
        this.upToId = null;
    }

    /**
     * Returns the part HQL query. If the query contains the keyword "IN", it
     * must be queried using JOIN, otherwise using WHERE.
     * 
     * @param className
     *            "Task" for a query on the tasks table, else for a query on the
     *            process table.
     * @param varName
     *            variable name to use in the query
     * @param parameterName
     *            parameter name to use in the query
     * @return the part HQL query
     */
    @Override
    String getDatabaseQuery(String className, String varName, String parameterName) {
        StringBuilder query = new StringBuilder();
        query.append(varName);
        query.append('.');
        if (Objects.equals(className, "Task")) {
            query.append(filterField.getTaskIdQuery());
        } else {
            query.append(filterField.getProcessIdQuery());
        }
        if (Objects.nonNull(idList)) {
            if (!operand) {
                query.append(" NOT");
            }
            query.append(" IN (:");
            query.append(parameterName);
            query.append(')');
        } else {
            if (upToId == null) {
                query.append(operand ? " = :" : " != :");
                query.append(parameterName);
            } else {
                if (!operand) {
                    query.append(" NOT");
                }
                query.append(" BETWEEN :");
                query.append(parameterName);
                query.append(" AND :");
                query.append(parameterName);
                query.append(SECOND_PARAMETER_EXTENSION);
            }
        }
        return query.toString();
    }

    /**
     * Puts the parameters necessary for the query.
     * 
     * @param parameterName
     *            parameter name used in the query
     * @param parameters
     *            map to put the parameters into
     */
    @Override
    void addParameters(String parameterName, Map<String, Object> parameters) {
        if (Objects.nonNull(filterField.getQueryObject())) {
            parameters.put("queryObject", filterField.getQueryObject());
        }
        parameters.put(parameterName, firstId != null ? firstId : idList);
        if (Objects.nonNull(upToId)) {
            parameters.put(parameterName.concat(SECOND_PARAMETER_EXTENSION), upToId);
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(filterField);
        if (Objects.isNull(firstId)) {
            if (!operand) {
                stringBuilder.append(" NOT");
            }
            stringBuilder.append(" IN ");
            stringBuilder.append(idList);
        } else {
            if (Objects.isNull(upToId)) {
                if (operand) {
                    stringBuilder.append(" = ");
                } else {
                    stringBuilder.append(" != ");
                }
                stringBuilder.append(firstId);
            } else {
                if (!operand) {
                    stringBuilder.append(" NOT");
                }
                stringBuilder.append(" BETWEEN ");
                stringBuilder.append(firstId);
                stringBuilder.append(" AND ");
                stringBuilder.append(upToId);
            }
        }
        return stringBuilder.toString();
    }
}
