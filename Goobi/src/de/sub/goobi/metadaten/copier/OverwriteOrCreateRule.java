/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2014 Goobi. Digitalisieren im Verein e. V. <contact@goobi.org>
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

/**
 * Data copy rule that either overwrites the metadatum described by the selector
 * on the left hand side or creates it anew, if it isn’t yet present.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class OverwriteOrCreateRule extends DataCopyrule {

	/**
	 * Operator representing the OverwriteOrCreateRule in the data copier syntax
	 */
	protected static final String OPERATOR = "=";

	/**
	 * Selector for the metadatum to be overwritten or created
	 */
	private MetadataSelector destination;

	/**
	 * Selector for the data to be copied
	 */
	private DataSelector source;

	/**
	 * Applies the rule to the given data object
	 * 
	 * @param data
	 *            data to apply the rule on
	 * @see de.sub.goobi.metadaten.copier.DataCopyrule#apply(de.sub.goobi.metadaten.copier.CopierData)
	 */
	@Override
	public void apply(CopierData data) {
		String value = source.findIn(data);
		if (value == null) {
			return;
		}
		destination.createOrOverwrite(data, value);
	}

	/**
	 * Returns the minimal number of objects required by the rule to work as
	 * expected, that is 1.
	 * 
	 * @return always 1
	 * @see de.sub.goobi.metadaten.copier.DataCopyrule#getMinObjects()
	 */
	@Override
	protected int getMinObjects() {
		return 1;
	}

	/**
	 * Returns the maximal number of objects supported by the rule to work as
	 * expected, that is 1.
	 * 
	 * @return always 1
	 * @see de.sub.goobi.metadaten.copier.DataCopyrule#getMaxObjects()
	 */
	@Override
	protected int getMaxObjects() {
		return 1;
	}

	/**
	 * Saves the source object path.
	 * 
	 * @see de.sub.goobi.metadaten.copier.DataCopyrule#setObjects(java.util.List)
	 */
	@Override
	protected void setObjects(List<String> objects) throws ConfigurationException {
		source = DataSelector.create(objects.get(0));
	}

	/**
	 * Saves the destination object path.
	 * 
	 * @see de.sub.goobi.metadaten.copier.DataCopyrule#setSubject(java.lang.String)
	 */
	@Override
	protected void setSubject(String subject) throws ConfigurationException {
		destination = MetadataSelector.create(subject);
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
