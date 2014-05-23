/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2014 Goobi. Digialisieren im Verein e.V. &lt;contact@goobi.org&gt;
 * 
 * Visit the websites for more information.
 *     		- http://www.goobi.org/en/
 *     		- https://github.com/goobi
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination. As a special
 * exception, the copyright holders of this library give you permission to link
 * this library with independent modules to produce an executable, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is not
 * derived from or based on this library. If you modify this library, you may
 * extend this exception to your version of the library, but you are not obliged
 * to do so. If you do not wish to do so, delete this exception statement from
 * your version.
 */
package de.sub.goobi.beans;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;

import de.sub.goobi.helper.Helper;

/**
 * The class Batch represents a user-definable, unordered collection of
 * processes that methods can be applied on in batch processing.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class Batch {
	/**
	 * Database record identifier. May be null in case that the Batch has not
	 * yet been saved to hibernate.
	 */
	private Integer id;
	/**
	 * Batch title. Optional, may be null. If null, the id will be shown to the
	 * user instead.
	 */
	private String title;
	/**
	 * Processes that belong to the batch.
	 */
	private Set<Prozess> processes;

	public Batch() {
		processes = new HashSet<Prozess>();
	}

	public Batch(String title) {
		this.title = title;
	}

	public Batch(Collection<Prozess> processes) {
		this.processes = new HashSet<Prozess>(processes);
	}

	public Batch(String title, Collection<Prozess> processes) {
		this.title = title;
		this.processes = new HashSet<Prozess>(processes);
	}

	public boolean addAll(List<Prozess> processes) {
		return getProcesses().addAll(processes);
	}

	public boolean contains(String s) {
		if (s == null)
			return true;
		return title != null && title.contains(s) || getNumericLabel().contains(s);
	}

	/**
	 * Field getter, required by Hibernate.
	 * 
	 * @return
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * The function getLabel() returns a readable label for the batch, which is
	 * either its title, if defined, or, for batches not having a title (in
	 * recent versions of Production, batches didn’t support titles) its ancient
	 * label, consisting of the prefix “Batch ” together with its id number.
	 * 
	 * @return a readable label for the batch
	 */
	public String getLabel() {
		if (title != null)
			return title;
		return getNumericLabel();
	}

	private String getNumericLabel() {
		return Helper.getTranslation("batch", "Batch") + ' ' + id;
	}

	/**
	 * Field getter, required by Hibernate.
	 * 
	 * @return
	 */
	public Set<Prozess> getProcesses() {
		Hibernate.initialize(processes);
		return processes;
	}

	/**
	 * Field getter, required by Hibernate.
	 * 
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	public boolean removeAll(List<Prozess> processes) {
		return getProcesses().removeAll(processes);
	}

	/**
	 * Field setter, required by Hibernate.
	 * 
	 * @param id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Field setter, required by Hibernate.
	 * 
	 * @param processes
	 */
	public void setProcesses(Set<Prozess> processes) {
		this.processes = processes;
	}

	/**
	 * Field setter, required by Hibernate.
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

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
			return result.toString();
		} catch (RuntimeException fallback) {
			return super.toString();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((processes == null) ? 0 : processes.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Batch))
			return false;
		Batch other = (Batch) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (processes == null) {
			if (other.processes != null)
				return false;
		} else if (!processes.equals(other.processes))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
}
