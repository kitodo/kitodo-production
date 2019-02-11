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

import java.security.InvalidParameterException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Table(name = "workflowCondition")
public class WorkflowCondition extends BaseBean {

    private static final long serialVersionUID = -5187947220333984868L;

    /**
     * Enum for workflow condition type. Types:
     *
     * <dl>
     * <dt>SCRIPT</dt>
     * <dd>path to executable script will be used for value</dd>
     * <dt>XPATH</dt>
     * <dd>expression to search in the metadata file will be used for value</dd>
     * </dl>
     */
    public enum Type {
        SCRIPT,
        XPATH
    }

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(name = "value")
    private String value;

    protected WorkflowCondition() {
    }

    /**
     * Public constructor.
     * 
     * @param type
     *            of workflow condition
     * @param value
     *            of workflow condition
     */
    public WorkflowCondition(String type, String value) {
        if (type.equalsIgnoreCase("script")) {
            this.type = Type.SCRIPT;
        } else if (type.equalsIgnoreCase("xpath")) {
            this.type = Type.XPATH;
        } else {
            throw new InvalidParameterException("Type should be script or XPath, but was " +  type);
        }
        this.value = value;
    }

    /**
     * Get type.
     *
     * @return value of type
     */
    public Type getType() {
        return type;
    }

    /**
     * Set type.
     *
     * @param type
     *            as java.lang.String
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Get value.
     *
     * @return value of value
     */
    public String getValue() {
        return value;
    }

    /**
     * Set value.
     *
     * @param value
     *            as java.lang.String
     */
    public void setValue(String value) {
        this.value = value;
    }
}
