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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;

/**
 * The abstract class DataCopyrule defines method signatures to implement a rule
 * which may later be used to modify metadata depending on various conditions,
 * and provides a factory method to create the matching metadata copy rule
 * implementation from a given command string.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public abstract class DataCopyrule {

	/**
	 * The final Map AVAILABLE_RULES maps the operators of the available
	 * metadata copyrules to their respective classes. If more metadata
	 * copyrules are to be added to this implementation, they will have to be
	 * listed named here.
	 */
	private static final Map<String, Class<? extends DataCopyrule>> AVAILABLE_RULES = new HashMap<String, Class<? extends DataCopyrule>>(
			3) {
		private static final long serialVersionUID = 1L;
		{
			put(CopyIfMetadataIsAbsentRule.OPERATOR, CopyIfMetadataIsAbsentRule.class);
			put(OverwriteOrCreateRule.OPERATOR, OverwriteOrCreateRule.class);
		}
	};

	/**
	 * Factory method to create a class implementing the metadata copy rule
	 * referenced by a given command string
	 * 
	 * @param command
	 *            A space-separated string constisting of subject (aka.
	 *            patiens), operator (aka. agens) and (optional) objects
	 *            (depending on what objects the operator requires).
	 * @return a class implementing the metadata copy rule referenced
	 * @throws ConfigurationException
	 *             if the operator cannot be resolved or the number of arguments
	 *             doesn’t match
	 */
	public static DataCopyrule createFor(String command) throws ConfigurationException {
		List<String> arguments = Arrays.asList(command.split("\\s+"));
		String operator;
		try {
			operator = arguments.get(1);
		} catch (IndexOutOfBoundsException e) {
			throw new ConfigurationException("Missing operator (second argument) in line: " + command);
		}
		Class<? extends DataCopyrule> ruleClass = AVAILABLE_RULES.get(operator);
		if (ruleClass == null) {
			throw new ConfigurationException("Unknown operator: " + operator);
		}
		DataCopyrule ruleImplementation;
		try {
			ruleImplementation = ruleClass.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		ruleImplementation.setSubject(arguments.get(0));
		if (ruleImplementation.getMaxObjects() > 0) {
			List<String> objects = arguments.subList(2, arguments.size());
			if (objects.size() < ruleImplementation.getMinObjects()) {
				throw new ConfigurationException("Too few arguments in line: " + command);
			}
			if (objects.size() > ruleImplementation.getMaxObjects()) {
				throw new ConfigurationException("Too many arguments in line: " + command);
			}
			ruleImplementation.setObjects(objects);
		}
		return ruleImplementation;
	}

	/**
	 * When called, the rule must be applied to the given fileformat.
	 * 
	 * @param data
	 *            data to apply yourself on
	 */
	protected abstract void apply(CopierData data);

	/**
	 * The function getMinObject must return the minimal number of objects
	 * required by the rule to work as expected.
	 * 
	 * @return the minimal number of objects required by the rule
	 */
	protected abstract int getMinObjects();

	/**
	 * The function getMinObject must return the maximal number of objects
	 * required by the rule to work as expected. If it returns 0, the
	 * setObjects() method will not be called.
	 * 
	 * @return the maximal number of objects required by the rule
	 */
	protected abstract int getMaxObjects();

	/**
	 * The method setObjects() is called to pass the rule its objects. The list
	 * passed is reliable to the restrictions defined by getMinObjects() and
	 * getMaxObjects().
	 * 
	 * @param objects
	 *            a list of objects to be used by the rule
	 * @throws ConfigurationException
	 *             may be thrown if one of the objects cannot be processed
	 */
	protected abstract void setObjects(List<String> objects) throws ConfigurationException;

	/**
	 * The method setSubject() is called to pass the rule its subject.
	 * 
	 * @param subject
	 *            a subject to be used by the rule
	 * @throws ConfigurationException
	 *             may be thrown if the subject cannot be processed
	 */
	protected abstract void setSubject(String subject) throws ConfigurationException;
}
