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
package de.sub.goobi.metadaten.copier;

import java.util.List;

import org.apache.commons.configuration.ConfigurationException;

public class OverwriteOrCreateRule extends DataCopyrule {

	protected static final String OPERATOR = "=";
	private MetadataSelector destination;
	private DataSelector source;

	@Override
	public void apply(CopierData data) {
		String value = source.findIn(data);
		if (value == null) {
			return;
		}
		destination.createOrOverwrite(data, value);
	}

	@Override
	protected int getMinObjects() {
		return 1;
	}

	@Override
	protected int getMaxObjects() {
		return 1;
	}

	/**
	 * Saves the source object path.
	 * 
	 * @see de.sub.goobi.metadaten.MetadataCopyrule#setObjects(java.util.List)
	 */
	@Override
	protected void setObjects(List<String> objects) throws ConfigurationException {
		destination = MetadataSelector.create(objects.get(0));
	}

	/**
	 * Saves the destination object path.
	 * 
	 * @see de.sub.goobi.metadaten.MetadataCopyrule#setSubject(java.lang.String)
	 */
	@Override
	protected void setSubject(String subject) throws ConfigurationException {
		source = DataSelector.create(subject);
	}

	/**
	 * Returns a string that textually represents this copy rule.
	 * 
	 * @return a string representation of this copy rule
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return destination.toString() + ' ' + OPERATOR + ' ' + source.toString();
	}
}
