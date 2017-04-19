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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.kitodo.data.database.helper.enums.PropertyType;

@Entity
@Table(name = "property")
public class Property extends BaseBean implements Comparable<Property> {
    private static final long serialVersionUID = -2356566712752716107L;

    @Column(name = "title")
    private String title;

    @Column(name = "value", columnDefinition = "longtext")
    private String value;

    @Column(name = "obligatory")
    private Boolean obligatory;

    @Column(name = "dataType")
    private Integer dataType;

    @Column(name = "choice")
    private String choice;

    @Column(name = "creationDate")
    private Date creationDate;

    @Column(name = "container")
    private Integer container;

    @ManyToMany(mappedBy = "properties", cascade = CascadeType.ALL)
    private List<Process> processes;

    @ManyToMany(mappedBy = "properties", cascade = CascadeType.ALL)
    private List<Template> templates;

    @ManyToMany(mappedBy = "properties", cascade = CascadeType.ALL)
    private List<User> users;

    @ManyToMany(mappedBy = "properties", cascade = CascadeType.ALL)
    private List<Workpiece> workpieces;

    @Transient
    private List<String> valueList;

    /**
     * Constructor.
     */
    public Property() {
        this.obligatory = false;
        this.dataType = PropertyType.String.getId();
        this.creationDate = new Date();
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getChoice() {
        return this.choice;
    }

    public void setChoice(String choice) {
        this.choice = choice;
    }

    public Boolean isObligatory() {
        if (this.obligatory == null) {
            this.obligatory = false;
        }
        return this.obligatory;
    }

    public void setObligatory(Boolean obligatory) {
        this.obligatory = obligatory;
    }

    public Date getCreationDate() {
        return this.creationDate;
    }

    public void setCreationDate(Date creation) {
        this.creationDate = creation;
    }

    /**
     * Fetter for data type set to private for hibernate, for use in program use
     * getType instead.
     *
     * @return data type as integer
     */
    @SuppressWarnings("unused")
    private Integer getDataType() {
        return this.dataType;
    }

    /**
     * Set data type to defined integer. only for internal use through
     * hibernate, for changing data type use setType instead.
     *
     * @param dataType
     *            as Integer
     */
    @SuppressWarnings("unused")
    private void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    /**
     * Get data type as {@link PropertyType}.
     *
     * @return current data type
     */
    public PropertyType getType() {
        if (this.dataType == null) {
            this.dataType = PropertyType.String.getId();
        }
        return PropertyType.getById(this.dataType);
    }

    /**
     * Set data type to specific value from {@link PropertyType}.
     *
     * @param inputType
     *            as {@link PropertyType}
     */
    public void setType(PropertyType inputType) {
        this.dataType = inputType.getId();
    }

    public List<String> getValueList() {
        if (this.valueList == null) {
            this.valueList = new ArrayList<>();
        }
        return this.valueList;
    }

    public void setValueList(List<String> valueList) {
        this.valueList = valueList;
    }

    public Integer getContainer() {
        if (this.container == null) {
            return 0;
        }
        return this.container;
    }

    public void setContainer(Integer order) {
        if (order == null) {
            order = 0;
        }
        this.container = order;
    }

    public List<Process> getProcesses() {
        if (this.processes == null) {
            this.processes = new ArrayList<>();
        }
        return this.processes;
    }

    public void setProcesses(List<Process> processes) {
        this.processes = processes;
    }

    public List<Template> getTemplates() {
        if (this.templates == null) {
            this.templates = new ArrayList<>();
        }
        return this.templates;
    }

    public void setTemplates(List<Template> templates) {
        this.templates = templates;
    }

    public List<User> getUsers() {
        if (this.users == null) {
            this.users = new ArrayList<>();
        }
        return this.users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Workpiece> getWorkpieces() {
        if (this.workpieces == null) {
            this.workpieces = new ArrayList<>();
        }
        return this.workpieces;
    }

    public void setWorkpieces(List<Workpiece> workpieces) {
        this.workpieces = workpieces;
    }

    public int compareTo(Property property) {
        return this.getTitle().toLowerCase().compareTo(property.getTitle().toLowerCase());
    }
}
