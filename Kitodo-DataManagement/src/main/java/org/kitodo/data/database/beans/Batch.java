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
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

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

    /**
     * The field title holds the batch title. Using titles for batches is
     * optional, the field may be null. If so, the id will be shown to the user
     * instead.
     */
    @Column(name = "title")
    private String title;

    /**
     * The field processes holds the processes that belong to the batch.
     */
    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "batch_x_process", joinColumns = {
            @JoinColumn(name = "batch_id", foreignKey = @ForeignKey(name = "FK_batch_x_process_batch_id")) }, inverseJoinColumns = {
            @JoinColumn(name = "process_id", foreignKey = @ForeignKey(name = "FK_batch_x_process_process_id")) })
    private List<Process> processes;

    @Transient
    private String label = "";

    /**
     * Default constructor. Creates an empty batch object.
     */
    public Batch() {
        this.processes = new ArrayList<>();
    }

    /**
     * Constructor to create an empty batch object with a given title and a
     * type.
     *
     * @param title
     *            for the batch
     */
    public Batch(String title) {
        this.processes = new ArrayList<>();
        this.title = title;
    }

    /**
     * Constructor to create a batch that holds the given processes.
     *
     * @param processes
     *            that go into the batch
     */
    public Batch(Collection<? extends Process> processes) {
        this.processes = new ArrayList<>(processes);
    }

    /**
     * Constructor to create a batch with a given title that holds the given
     * processes.
     *
     * @param title
     *            for the batch
     * @param processes
     *            that go into the batch
     */
    public Batch(String title, Collection<? extends Process> processes) {
        this.title = title;
        this.processes = new ArrayList<>(processes);
    }

    /**
     * Returns the batch title. Using titles for batches
     * is optional, the field may be null. If so, the function returns null.
     *
     * @return the batch title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Can be used to set a batch title. This function is
     * also required by Hibernate when creating objects from the database.
     *
     * @param title
     *            for the batch
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Return the processes that belong to the
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
     * Sets the processes that belong to the batch.
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
     * Set label.
     *
     * @param label as java.lang.String
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Indicates whether some other object is “equal to”
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
        return Objects.hash(title, processes);
    }

    @Override
    public String toString() {
        return this.label;
    }
}
