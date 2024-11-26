package org.kitodo.production.services.data;

import java.util.Map;
import java.util.Objects;

/**
 * A part of the filter searching on the database for a name.
 */
public class DatabaseQueryPart implements UserSpecifiedFilter {

    protected FilterField filterField;
    protected boolean operand;
    private String value;

    DatabaseQueryPart(FilterField filterField, String value, boolean operand) {
        this.filterField = filterField;
        this.operand = operand;
        this.value = value;
    }

    protected DatabaseQueryPart(FilterField filterField, boolean operand) {
        this.filterField = filterField;
        this.operand = operand;
    }

    String getDatabaseQuery(String className, String varName, String parameterName) {
        String query = Objects.equals(className, "Task") ? filterField.getTaskTitleQuery()
                : filterField.getProcessTitleQuery();
        query = query.contains("~") ? query.replace("~", varName) : varName + '.' + query;
        query = query.contains("#") ? query.replace("#", parameterName) : query + " = " + parameterName;
        return operand ? query : "NOT (" + query + ')';
    }

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
