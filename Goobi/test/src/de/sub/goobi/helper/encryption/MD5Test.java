/**
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
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
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

package de.sub.goobi.helper.encryption;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

import de.sub.goobi.helper.encryption.MD5;

public class MD5Test {
	static HashMap<String, String> testData;

	static {
		testData = new HashMap<String, String>();
		testData.put("Test", "0cbc6611f5540bd0809a388dc95a615b");
		testData.put("Password", "dc647eb65e6711e155375218212b3964");
		testData.put("12345678", "25d55ad283aa400af464c76d713c07ad");
		testData.put("GoobiPassword1234*./", "8480dc5afeaadcfc3114ea22e38e3412");
		testData.put("AreallyreallyreallylongPassword", "84b23a5fc3b6f0a275d32c28dbb28478");
		testData.put("$%!--_-_/*-äöüä", "57fe2c6b74dcedd667234f1955aea362");
	}

	@Test
	public void encryptTest () {
		MD5 md5 = new MD5("");
		for (String clearText: testData.keySet()) {
			String encrypted = md5.getMD5(clearText);
			assertTrue("Encrypted password doesn't match the precomputed one!",
				   encrypted != null && encrypted.equals(testData.get(clearText)));
		}
	}
}
