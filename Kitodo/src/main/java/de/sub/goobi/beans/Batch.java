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

package de.sub.goobi.beans;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.BatchDAO;

/**
 * The class Batch represents a user-definable, unordered collection of
 * processes that methods can be applied on in batch processing.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
@Entity
@Table(name = "batch")
public class Batch {
	/**
	 * Type of batch:
	 *
	 * <dl>
	 * <dt>LOGISTIC</dt>
	 * <dd>facilitates the logistics of excavation and processing in the
	 * digitisation centre</dd>
	 * <dt>NEWSPAPER</dt>
	 * <dd>forms the complete edition of a newspaper</dd>
	 * <dt>SERIAL</dt>
	 * <dd>forms the complete edition of a serial publication</dd>
	 * </dl>
	 *
	 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
	 */
	public enum Type {
		LOGISTIC, NEWSPAPER, SERIAL
	}

	/**
	 * The field id holds the database record identifier. It is null in case
	 * that the Batch has not yet been saved by Hibernate.
	 */
	@Id
	@Column(name = "id")
	@GeneratedValue
	private Integer id;

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
	private Type type;

	/**
	 * The field processes holds the processes that belong to the batch.
	 */
	@ManyToMany
	@JoinTable(name = "batch_x_process",
			joinColumns = {
					@JoinColumn(
							name = "batch_id",
							foreignKey = @ForeignKey(name = "FK_batch_x_process_batch_id")
					) },
			inverseJoinColumns = {
					@JoinColumn(
							name = "process_id",
							foreignKey = @ForeignKey(name = "FK_batch_x_process_process_id")
					) })
	private Set<Prozess> processes;

	/**
	 * Default constructor. Creates an empty batch object.
	 */
	public Batch() {
		this.processes = new HashSet<Prozess>(0);
	}

	/**
	 * Constructor to create an empty batch object with a given type.
	 *
	 * @param type
	 *            type of the batch
	 */
	public Batch(Type type) {
		this.processes = new HashSet<Prozess>(0);
		this.type = type;
	}

	/**
	 * Constructor to create an empty batch object with a given title and a
	 * type.
	 *
	 * @param title
	 *            title for the batch
	 * @param type
	 *            type of the batch
	 */
	public Batch(String title, Type type) {
		this.processes = new HashSet<Prozess>(0);
		this.title = title;
		this.type = type;
	}

	/**
	 * Constructor to create a batch that holds the given processes.
	 *
	 * @param type
	 *            type of the batch
	 * @param processes
	 *            processes that go into the batch
	 */
	public Batch(Type type, Collection<? extends Prozess> processes) {
		this.processes = new HashSet<Prozess>(processes);
		this.type = type;
	}

	/**
	 * Constructor to create a batch with a given title that holds the given
	 * processes.
	 *
	 * @param title
	 *            title for the batch
	 * @param type
	 *            type of the batch
	 * @param processes
	 *            processes that go into the batch
	 */
	public Batch(String title, Type type, Collection<? extends Prozess> processes) {
		this.title = title;
		this.type = type;
		this.processes = new HashSet<Prozess>(processes);
	}

	/**
	 * The function add() adds the given process to this batch if it is not
	 * already present.
	 *
	 * @param process
	 *            process to add
	 * @return true if this batch did not already contain the specified process
	 */
	public boolean add(Prozess process) {
		return getProcesses().add(process);
	}

	/**
	 * The function addAll() adds all of the elements in the given collection to
	 * this batch if they're not already present.
	 *
	 * @param processes
	 *            collection containing elements to be added to this set
	 * @return true if this set changed as a result of the call
	 */
	public boolean addAll(Collection<? extends Prozess> processes) {
		return getProcesses().addAll(processes);
	}

	/**
	 * The function contains() returns true if the title (if set) or the
	 * id-based label contain the specified sequence of char values.
	 *
	 * @param s
	 *            the sequence to search for
	 * @return true if the title or label contain s, false otherwise
	 */
	public boolean contains(CharSequence s) {
		if (s == null) {
			return true;
		}
		return title != null && title.contains(s) || getNumericLabel().contains(s);
	}

	/**
	 * The function getId() returns the database record identifier for the
	 * batch. In case that the Batch has not yet been saved by Hibernate it
	 * returns null.
	 *
	 * This method is required by Hibernate.
	 *
	 * @return the database record identifier for the batch
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * The function getIdString() returns the identifier for the batch as
	 * read-only property "idString".
	 *
	 * This method is required by Faces which silently fails if you try to use
	 * the id Integer.
	 *
	 * @return the identifier for the batch as String
	 */
	public String getIdString() {
		return id.toString();
	}

	/**
	 * The function getLabel() returns a readable label for the batch, which is
	 * either its title, if defined, or, for batches not having a title (in
	 * recent versions of Production, batches didn’t support titles) its ancient
	 * label, consisting of the prefix “Batch ” (in the desired translation)
	 * together with its id number.
	 *
	 * @return a readable label for the batch
	 */
	public String getLabel() {
		return title != null ? title : getNumericLabel();
	}

	/**
	 * The function getNumericLabel() returns a readable label for the batch,
	 * consisting of the prefix “Batch ” (in the desired translation) together
	 * with its id number.
	 *
	 * @return a readable label for the batch
	 */
	private String getNumericLabel() {
		return Helper.getTranslation("batch", "Batch") + ' ' + id;
	}

	/**
	 * The function getProcesses() return the processes that belong to the
	 * batch.
	 *
	 * The internal logic is to make sure the collection has been populated from
	 * Hibernate.
	 *
	 * @return the processes that are in the batch
	 */
	public Set<Prozess> getProcesses() {
		if (id != null) {
			try {
				Hibernate.initialize(processes);
			} catch (HibernateException e) {
				BatchDAO.reattach(this);
				Hibernate.initialize(processes);
			}
		}
		return processes;
	}

	/**
	 * The function getTitle() returns the batch title. Using titles for batches
	 * is optional, the field may be null. If so, the function returns null. Use
	 * {@link #getLabel()} to get either the title or an alternative name.
	 *
	 * @return the batch title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Returns the batch type.
	 *
	 * @return the batch type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Returns the translated batch type label.
	 *
	 * @return the display label for the batch type
	 */
	public String getTypeTranslated() {
		if (type != null) {
			return Helper.getTranslation("batch_type_".concat(type.toString().toLowerCase()));
		} else {
			return "";
		}
	}

	/**
	 * The function removeAll() removes all elements that are contained in the
	 * given collection from this batch.
	 *
	 * @param processes
	 *            collection containing elements to be removed from this set
	 * @return true if the set of processes was changed as a result of the call
	 */
	public boolean removeAll(Collection<?> processes) {
		return getProcesses().removeAll(processes);
	}

	/**
	 * The method setId() sets the database record identifier of this batch.
	 * This method is solely intended to be called by Hibernate when creating
	 * objects from the database. Do not use it in the code.
	 *
	 * @param id
	 *            database record identifier of this batch
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * The method setProcesses() sets the processes that belong to the batch.
	 *
	 * This method is also required by Hibernate when creating objects from the
	 * database.
	 *
	 * @param processes
	 *            processes that belong to the batch
	 */
	public void setProcesses(Set<Prozess> processes) {
		this.processes = processes;
	}

	/**
	 * The method setTitle() can be used to set a batch title.
	 *
	 * This function is also required by Hibernate when creating objects from
	 * the database.
	 *
	 * @param title
	 *            a title for the batch
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * The method setType() can be used to set a batch title.
	 *
	 * This function is also required by Hibernate when creating objects from
	 * the database.
	 *
	 * @param type
	 *            type for the batch
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * Returns the number of elements in this batch. If this batch contains more
	 * than Integer.MAX_VALUE elements, returns Integer.MAX_VALUE.
	 *
	 * @return the number of elements in this batch
	 * @see java.util.Collection#size()
	 */
	public int size() {
		return processes.size();
	}

	/**
	 * The function toString() returns a concise but informative representation
	 * that is easy for a person to read and that "textually represents" this
	 * batch.
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		try {
			StringBuilder result = new StringBuilder(title != null ? title.length() + 20 : 30);
			try {
				if (title != null) {
					result.append(title);
				} else if (id != null) {
					result.append(Helper.getTranslation("batch", "Batch"));
					result.append(' ');
					result.append(id);
				} else {
					result.append('−');
				}
				result.append(" (");
				String extent = Helper.getTranslation("numProzesse", "{0} processes");
				String size = getProcesses() != null ? Integer.toString(processes.size()) : "−";
				result.append(extent.replaceFirst("\\{0\\}", size));
			} catch (RuntimeException unexpected) {
				result.setLength(0);
				result.append(title != null ? title : id);
				result.append(" (");
				result.append(getProcesses() != null ? processes.size() : null);
			}
			result.append(')');
			if (type != null) {
				result.append(" [");
				result.append(getTypeTranslated());
				result.append(']');
			}
			return result.toString();
		} catch (RuntimeException fallback) {
			return super.toString();
		}
	}

	/**
	 * The function equals() indicates whether some other object is “equal to”
	 * this one.
	 *
	 * @param obj
	 *            the reference object with which to compare
	 * @return true if this object is the same as the obj argument; false
	 *         otherwise
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Batch)) {
			return false;
		}
		Batch other = (Batch) obj;
		if (id != null && id.equals(other.id)) {
			return true;
		}
		if (title == null) {
			if (other.title != null) {
				return false;
			}
		} else if (!title.equals(other.title)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		if (processes == null) {
			if (other.processes != null) {
				return false;
			}
		} else if (!processes.equals(other.processes)) {
			return false;
		}
		return true;
	}

	/**
	 * Goobi does not keep objects around from Hibernate session to Hibernate
	 * session, so this is the working approach here.
	 * 
	 * @see "https://developer.jboss.org/wiki/EqualsandHashCode"
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
