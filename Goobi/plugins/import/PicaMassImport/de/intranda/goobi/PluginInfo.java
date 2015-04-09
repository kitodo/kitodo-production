package de.intranda.goobi;

/**
 * Copyright by intranda GmbH 2013. All rights reserved.
 * 
 * Visit the websites for more information. 
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PluginInfo {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
		System.out.println("This is a Goobi plugin from intranda");
		System.out.println("Copyright 2013 by intranda GmbH");
		System.out.println("All rights reserved");
		System.out.println("info@intranda.com");
		System.out.println("\n-----------------------------------------------------\n");
		System.out.println("This plugin file contains the following plugins:");

		String bla = convertStreamToString(PluginInfo.class.getResourceAsStream("plugins.txt"));
		bla = bla.replaceAll(".class", "");
		bla = bla.replaceAll(" ", "\n");
		System.out.println("\n" + bla);

		System.out.println("\n+++++++++++++++++++++++++++++++++++++++++++++++++++++");
	}

	public static String convertStreamToString(InputStream is) throws IOException {
		StringBuilder sb = new StringBuilder(Math.max(16, is.available()));
		char[] tmp = new char[4096];

		try {
			InputStreamReader reader = new InputStreamReader(is);
			for (int cnt; (cnt = reader.read(tmp)) > 0;)
				sb.append(tmp, 0, cnt);
		} finally {
			is.close();
		}
		return sb.toString();
	}

}
