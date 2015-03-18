/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 *
 * (c) 2015 Goobi. Digialisieren im Verein e.V. &lt;contact@goobi.org&gt;
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

package src.de.sub.goobi.helper.encryption;

import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.Test;

import edu.sysu.virgoftp.ftp.encrypt.MD4;

public class MD4Test {
	static HashMap<String, byte[]> testData;

	static {
		testData = new HashMap<String, byte[]>();
		testData.put("Password", new byte[]{-92, -12, -100, 64, 101, 16, -67, -54, -74, -126, 78, -25, -61, 15, -40, 82});
		testData.put("12345678", new byte[]{37, -105, 69, -53, 18, 58, 82, -86, 46, 105, 58, -86, -52, -94, -37, 82});
		testData.put("GoobiPassword1234*./", new byte[]{82,-81, 20, -80, 72, 78, 95, 109, -12, -42, -105, -55, -29, 12, 109, 79});
		testData.put("AreallyreallyreallylongPassword", new byte[]{45, -50, -121, -8, -30, 74, -60, 71, 56, 76, -127, -90, 98, -48, 80, 126});
		testData.put("$%!--_-_/*-äöüä", new byte[]{-105, 38, -102, 0, -49, 47, -11, 119, 70, -87, 54, 40, 105, -94, 19, 53});
	}

	@Test
	public void encryptTest () {
		for (String clearText: testData.keySet()) {
			byte hmm[] = new byte[0];
			try {
				hmm = MD4.mdfour(clearText.getBytes("UnicodeLittleUnmarked"));
			} catch (UnsupportedEncodingException e) {
				System.out.println("Unsupported encoding exception: " + e.getMessage());
			}
			assertTrue("Encrypted password doesn't match the precomputed one! ", Arrays.equals(hmm, testData.get(clearText)));
		}
	}
}
