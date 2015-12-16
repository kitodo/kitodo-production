/*
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 *
 * (c) 2015 Goobi. Digitalisieren im Verein e.V. <contact@goobi.org>
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
package de.sub.goobi.metadaten;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.compress.utils.Charsets;

/**
 * Object representing a separator.
 * 
 * @author Matthias Ronge
 */
public class Separator implements Selectable {

	/**
	 * Characters not allowed here.
	 */
	private static final int[] ILLEGAL_CHARACTERS = { 0, 34 };

	/**
	 * Regular expression to remove quotes from the input string.
	 */
	private static final Pattern UNQUOTE = Pattern.compile("[^,\"]+|\"([^\"]*)\"");

	/**
	 * Creates a lot of separator objects from a String array.
	 * 
	 * @param data
	 *            elements to create Separators from
	 * @return a list of separator objects
	 */
	public static List<Separator> factory(String data) {
		List<Separator> result = new LinkedList<Separator>();
		Matcher m = UNQUOTE.matcher(data);
		while (m.find()) {
		    if (m.group(1) != null) {
		        result.add(new Separator(m.group(1)));
		    } else {
		        result.add(new Separator(m.group()));
		    }
		} 
		return result;
	}

	/**
	 * The separator string.
	 */
	private final String separator;

	/**
	 * Creates a new separator
	 * 
	 * @param separator
	 *            separator String
	 */
	public Separator(String separator) {
		for (int i = 0; i < separator.length(); i++) {
			int codePoint = separator.codePointAt(i);
			for (int illegal : ILLEGAL_CHARACTERS) {
				if (codePoint == illegal) {
					throw new IllegalArgumentException(
							String.format("Illegal character %c (U+%04X) at index %d.", illegal, illegal, i));
				}
			}
		}
		this.separator = separator;
	}

	/**
	 * Returns a readable ID for the separator.
	 */
	@Override
	public String getId() {
		return new BigInteger(separator.getBytes(Charsets.UTF_8)).toString(Character.MAX_RADIX);
	}

	/**
	 * Returns a visible label for the separator. White spaces are replaced by
	 * open boxes (␣) to be visible.
	 */
	@Override
	public String getLabel() {
		return separator.replaceAll(" ", "\u2423");
	}

	/**
	 * Return the separator string.
	 * 
	 * @return the separator string
	 */
	public String getSeparatorString() {
		return separator;
	}
}
