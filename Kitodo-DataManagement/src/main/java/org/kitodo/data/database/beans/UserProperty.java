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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.kitodo.data.database.beans.property.GoobiPropertyInterface;
import org.kitodo.data.database.helper.enums.PropertyType;

@Entity
@Table(name = "userProperty")
public class UserProperty implements GoobiPropertyInterface {
    private static final long serialVersionUID = -2356566712752716107L;

    @Id
    @Column(name = "id")
    @GeneratedValue
    private Integer id;

    @Column(name = "title")
    private String title;

    @Column(name = "value")
    private String value;

    @Column(name = "obligatory")
    private Boolean obligatory;

    @Column(name = "dataType")
    private Integer dataType;

    @Column(name = "choice")
    private String choice;

    @Column(name = "creationDate")
    private Date creationDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Transient
    private List<String> valueList;

    /**
     * Constructor.
     */
    public UserProperty() {
        this.obligatory = false;
        this.dataType = PropertyType.String.getId();
        this.creationDate = new Date();
    }

    @Override
    public Integer getId() {
        return this.id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getChoice() {
        return this.choice;
    }

    @Override
    public void setChoice(String choice) {
        this.choice = choice;
    }

    @Override
    public Boolean isObligatory() {
        if (this.obligatory == null) {
            this.obligatory = false;
        }
        return this.obligatory;
    }

    @Override
    public void setObligatory(Boolean isObligatory) {
        this.obligatory = isObligatory;
    }

    @Override
    public Date getCreationDate() {
        return this.creationDate;
    }

    @Override
    public void setCreationDate(Date creation) {
        this.creationDate = creation;
    }

    /**
     * Getter for dataType set to private for hibernate, for use in program use getType instead - why?!.
     *
     * @return dataType as integer
     */
    @SuppressWarnings("unused")
    private Integer getDataType() {
        return this.dataType;
    }

    /**
     * Set dataType to defined integer only for internal use through hibernate, for changing dataType
     * use setType instead.
     *
     * @param dataType as Integer
     */
    @SuppressWarnings("unused")
    private void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    /**
     * Get dataType as {@link PropertyType}.
     *
     * @return current dataType
     */
    @Override
    public PropertyType getType() {
        if (this.dataType == null) {
            this.dataType = PropertyType.String.getId();
        }
        return PropertyType.getById(this.dataType);
    }

    /**
     * Set dataType to specific value from {@link PropertyType}.
     *
     * @param inputType as {@link PropertyType}
     */
    @Override
    public void setType(PropertyType inputType) {
        this.dataType = inputType.getId();
    }

    @Override
    public Integer getContainer() {
        return 0;
    }

    @Override
    public void setContainer(Integer container) {

    }

    /**
     * How it is possible that here appears list if object can have only one value?!.
     *
     * @return list of values
     */
    public List<String> getValueList() {
        if (this.valueList == null) {
            this.valueList = new ArrayList<>();
        }
        return this.valueList;
    }

    public void setValueList(List<String> valueList) {
        this.valueList = valueList;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
