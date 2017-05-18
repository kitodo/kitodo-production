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

    @ManyToMany(mappedBy = "properties", cascade = CascadeType.PERSIST)
    private List<Process> processes;

    @ManyToMany(mappedBy = "properties", cascade = CascadeType.PERSIST)
    private List<Template> templates;

    @ManyToMany(mappedBy = "properties", cascade = CascadeType.PERSIST)
    private List<User> users;

    @ManyToMany(mappedBy = "properties", cascade = CascadeType.PERSIST)
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

    /**
     * Get title.
     * 
     * @return title as String
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Set title.
     * 
     * @param title
     *            as String
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get value.
     * 
     * @return value as String
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Set value.
     * 
     * @param value
     *            as String
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Get choice.
     * 
     * @return choice as String
     */
    public String getChoice() {
        return this.choice;
    }

    /**
     * Set choice.
     * 
     * @param choice
     *            as String
     */
    public void setChoice(String choice) {
        this.choice = choice;
    }

    /**
     * Check if property is obligatory (mandatory).
     *
     * @return value from database or false
     */
    public Boolean isObligatory() {
        if (this.obligatory == null) {
            this.obligatory = false;
        }
        return this.obligatory;
    }

    /**
     * Set obligatory.
     * 
     * @param obligatory
     *            as Boolean
     */
    public void setObligatory(Boolean obligatory) {
        this.obligatory = obligatory;
    }

    /**
     * Get creation date.
     * 
     * @return creation date as Date
     */
    public Date getCreationDate() {
        return this.creationDate;
    }

    /**
     * Set creation date.
     * 
     * @param creationDate
     *            as Date
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Getter for data type set to private for hibernate, for use in program use
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

    /**
     * Get value list or new empty list.
     *
     * @return value list or new empty list
     */
    public List<String> getValueList() {
        if (this.valueList == null) {
            this.valueList = new ArrayList<>();
        }
        return this.valueList;
    }

    /**
     * Not know usage method.
     * 
     * @param valueList
     *            list of Strings
     */
    public void setValueList(List<String> valueList) {
        this.valueList = valueList;
    }

    /**
     * Get container value.
     *
     * @return value from database or 0
     */
    public Integer getContainer() {
        if (this.container == null) {
            return 0;
        }
        return this.container;
    }

    /**
     * Set container value as given or 0 when given is null.
     *
     * @param container
     *            value from database or 0
     */
    public void setContainer(Integer container) {
        if (container == null) {
            container = 0;
        }
        this.container = container;
    }

    /**
     * Get container value.
     *
     * @return value from database or 0
     */
    public List<Process> getProcesses() {
        if (this.processes == null) {
            this.processes = new ArrayList<>();
        }
        return this.processes;
    }

    /**
     * Set processes.
     * 
     * @param processes
     *            as List
     */
    public void setProcesses(List<Process> processes) {
        this.processes = processes;
    }

    /**
     * Get templates list or new empty list.
     *
     * @return templates list or new empty list
     */
    public List<Template> getTemplates() {
        if (this.templates == null) {
            this.templates = new ArrayList<>();
        }
        return this.templates;
    }

    /**
     * Set templates.
     * 
     * @param templates
     *            as List
     */
    public void setTemplates(List<Template> templates) {
        this.templates = templates;
    }

    /**
     * Get users list or new empty list.
     *
     * @return users list or new empty list
     */
    public List<User> getUsers() {
        if (this.users == null) {
            this.users = new ArrayList<>();
        }
        return this.users;
    }

    /**
     * Set users.
     * 
     * @param users
     *            as List
     */
    public void setUsers(List<User> users) {
        this.users = users;
    }

    /**
     * Get workpieces list or new empty list.
     *
     * @return workpieces list or new empty list
     */
    public List<Workpiece> getWorkpieces() {
        if (this.workpieces == null) {
            this.workpieces = new ArrayList<>();
        }
        return this.workpieces;
    }

    /**
     * Set workpieces.
     * 
     * @param workpieces
     *            as List
     */
    public void setWorkpieces(List<Workpiece> workpieces) {
        this.workpieces = workpieces;
    }

    /**
     * Compare property to other property object.
     * 
     * @param property
     *            object
     * @return int
     */
    public int compareTo(Property property) {
        int title = this.getTitle().toLowerCase().compareTo(property.getTitle().toLowerCase());
        int value = this.getValue().toLowerCase().compareTo(property.getValue().toLowerCase());
        if (title == 0 && value == 0) {
            return 0;
        } else if (value == 0) {
            return title;
        } else {
            return value;
        }
    }
}
