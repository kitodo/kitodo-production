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
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.kitodo.data.database.helper.enums.BatchType;
import org.kitodo.data.database.persistence.BatchDAO;

/**
 * The class Batch represents a user-definable, unordered collection of
 * processes that methods can be applied on in batch processing.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
@Entity
@Table(name = "batch")
public class Batch extends BaseIndexedBean {
    private static final long serialVersionUID = -5187947220333984868L;

    /**
     * The field title holds the batch title. Using titles for batches is
     * optional, the field may be null. If so, the id will be shown to the user
     * instead.
     */
    @Column(name = "title")
    private String title;

    /**
     * The field type holds the batch type.
     */
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private BatchType type;

    /**
     * The field processes holds the processes that belong to the batch.
     */
    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinTable(name = "batch_x_process", joinColumns = {
            @JoinColumn(name = "batch_id", foreignKey = @ForeignKey(name = "FK_batch_x_process_batch_id")) }, inverseJoinColumns = {
            @JoinColumn(name = "process_id", foreignKey = @ForeignKey(name = "FK_batch_x_process_process_id")) })
    private List<Process> processes;

    /**
     * Default constructor. Creates an empty batch object.
     */
    public Batch() {
        this.processes = new ArrayList<>();
    }

    /**
     * Constructor to create an empty batch object with a given type.
     *
     * @param type
     *            of the batch
     */
    public Batch(BatchType type) {
        this.processes = new ArrayList<>();
        this.type = type;
    }

    /**
     * Constructor to create an empty batch object with a given title and a
     * type.
     *
     * @param title
     *            for the batch
     * @param type
     *            of the batch
     */
    public Batch(String title, BatchType type) {
        this.processes = new ArrayList<>();
        this.title = title;
        this.type = type;
    }

    /**
     * Constructor to create a batch that holds the given processes.
     *
     * @param type
     *            of the batch
     * @param processes
     *            that go into the batch
     */
    public Batch(BatchType type, Collection<? extends Process> processes) {
        this.processes = new ArrayList<>(processes);
        this.type = type;
    }

    /**
     * Constructor to create a batch with a given title that holds the given
     * processes.
     *
     * @param title
     *            for the batch
     * @param type
     *            of the batch
     * @param processes
     *            that go into the batch
     */
    public Batch(String title, BatchType type, Collection<? extends Process> processes) {
        this.title = title;
        this.type = type;
        this.processes = new ArrayList<>(processes);
    }

    /**
     * The function getTitle() returns the batch title. Using titles for batches
     * is optional, the field may be null. If so, the function returns null.
     *
     * @return the batch title
     */
    public String getTitle() {
        return title;
    }

    /**
     * The method setTitle() can be used to set a batch title. This function is
     * also required by Hibernate when creating objects from the database.
     *
     * @param title
     *            for the batch
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the batch type.
     *
     * @return the batch type
     */
    public BatchType getType() {
        return type;
    }

    /**
     * The method setType() can be used to set a batch title. This function is
     * also required by Hibernate when creating objects from the database.
     *
     * @param type
     *            for the batch
     */
    public void setType(BatchType type) {
        this.type = type;
    }

    /**
     * The function getProcesses() return the processes that belong to the
     * batch.
     *
     * @return the processes that are in the batch
     */
    public List<Process> getProcesses() {
        initialize(new BatchDAO(), this.processes);
        if (Objects.isNull(this.processes)) {
            this.processes = new ArrayList<>();
        }
        return this.processes;
    }

    /**
     * The method setProcesses() sets the processes that belong to the batch.
     * This method is also required by Hibernate when creating objects from the
     * database.
     *
     * @param processes
     *            that belong to the batch
     */
    public void setProcesses(List<Process> processes) {
        if (this.processes == null) {
            this.processes = processes;
        } else {
            this.processes.clear();
            this.processes.addAll(processes);
        }
    }

    /**
     * The function equals() indicates whether some other object is “equal to”
     * this one.
     *
     * @param object
     *            the reference object with which to compare
     * @return true if this object is the same as the obj argument; false
     *         otherwise
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof Batch) {
            Batch batch = (Batch) object;
            return Objects.equals(this.getId(), batch.getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, type, processes);
    }

    // Here will be methods which should be in BatchService but are used by jsp
    // files

    public String getLabel() {
        return this.getTitle() != null ? this.getTitle() : getNumericLabel();
    }

    private String getNumericLabel() {
        return "batch" + ' ' + this.getId();
    }

    /**
     * The function toString() returns a concise but informative representation
     * that is easy for a person to read and that "textually represents" this
     * batch.
     *
     */
    public String toString() {
        try {
            StringBuilder result = new StringBuilder(this.getTitle() != null ? this.getTitle().length() + 20 : 30);
            if (this.getTitle() != null) {
                result.append(this.getTitle());
            } else if (this.getId() != null) {
                result.append("Batch");
                result.append(' ');
                result.append(this.getId());
            } else {
                result.append('−');
            }
            result.append(" (");
            String extent = "{0} processes";
            String size = Integer.toString(this.getProcesses().size());
            result.append(extent.replaceFirst("\\{0\\}", size));
            result.append(')');
            if (this.getType() != null) {
                result.append(" [");
                result.append(this.getType().toString());
                result.append(']');
            }
            return result.toString();
        } catch (RuntimeException fallback) {
            return super.toString();
        }
    }
}
