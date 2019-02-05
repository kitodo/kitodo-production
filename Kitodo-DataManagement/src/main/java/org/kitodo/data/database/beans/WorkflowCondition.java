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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "workflowCondition")
public class WorkflowCondition extends BaseBean {

    @Column(name = "type")
    private String type;

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
        this.type = type;
        this.value = value;
    }

    /**
     * Get type.
     *
     * @return value of type
     */
    public String getType() {
        return type;
    }

    /**
     * Set type.
     *
     * @param type
     *            as java.lang.String
     */
    public void setType(String type) {
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
