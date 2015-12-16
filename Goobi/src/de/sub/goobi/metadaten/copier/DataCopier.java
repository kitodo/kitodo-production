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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;

/**
 * A data copier is a class that can be parameterised to copy data in goobi
 * processes depending on rules.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class DataCopier {

	/**
	 * Holds the rules this data copier can apply to a set of working data.
	 */
	private final List<DataCopyrule> rules;

	/**
	 * Creates a new DataCopier.
	 * 
	 * @param program
	 *            a semicolon-separated list of expressions defining rules to
	 *            apply to the metadata
	 * @throws ConfigurationException
	 *             may be thrown if the program is syntactically wrong
	 */
	public DataCopier(String program) throws ConfigurationException {
		List<String> commands = Arrays.asList(program.split(";"));
		rules = new ArrayList<DataCopyrule>(commands.size());
		for (String command : commands) {
			rules.add(DataCopyrule.createFor(command));
		}
	}

	/**
	 * Applies the rules defined by the “program” passed to the constructor onto
	 * a given dataset.
	 * 
	 * @param data
	 *            a data object to work on
	 */
	public void process(CopierData data) {
		for (DataCopyrule rule : rules) {
			rule.apply(data);
		}
	}

	/**
	 * Returns a string that textually represents this data copier.
	 * 
	 * @return a string representation of this data copier.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return rules.toString();
	}
}
