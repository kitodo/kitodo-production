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

import java.util.Collections;
import java.util.Set;

import de.sub.goobi.helper.Helper;

/**
 * The class Batch represents a user-definable, unordered collection of
 * processes that methods can be applied on in batch processing.
 * 
 * This is a pure bean class except for a bit sophisticated toString() method
 * which shall generate a description presentable to the user.
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
		processes = Collections.emptySet();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Set<Prozess> getProcesses() {
		return processes;
	}

	public void setProcesses(Set<Prozess> processes) {
		this.processes = processes;
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
				String size = processes != null ? Integer.toString(processes.size()) : "−";
				result.append(extent.replaceFirst("\\{0\\}", size));
			} catch (RuntimeException unexpected) {
				result.setLength(0);
				result.append(title != null ? title : id);
				result.append(" (");
				result.append(processes != null ? processes.size() : null);
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
