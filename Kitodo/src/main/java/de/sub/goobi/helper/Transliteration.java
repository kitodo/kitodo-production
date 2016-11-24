package de.sub.goobi.helper;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.kitodo.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
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
public class Transliteration {

	public String transliterate_iso(String inString) {
		String s = "";
		char[] arr = inString.toCharArray();
		for (int i = 0; i < arr.length; i++) {
			switch (arr[i]) {
				case 0x410:
					s = s + "A";
					break;
				case 0x430:
					s = s + "a";
					break;
				case 0x411:
					s = s + "B";
					break;
				case 0x431:
					s = s + "b";
					break;
				case 0x412:
					s = s + "V";
					break;
				case 0x432:
					s = s + "v";
					break;
				case 0x413:
					s = s + "G";
					break;
				case 0x433:
					s = s + "g";
					break;
				case 0x414:
					s = s + "D";
					break;
				case 0x434:
					s = s + "d";
					break;
				case 0x415:
					s = s + "E";
					break;
				case 0x435:
					s = s + "e";
					break;
				case 0x0CB:
					s = s + "\u00CB";
					break;
				case 0x451:
					s = s + "\u00EB";
					break;
				case 0x416:
					s = s + "\u017D";
					break;
				case 0x436:
					s = s + "\u017E";
					break;
				case 0x417:
					s = s + "Z";
					break;
				case 0x437:
					s = s + "z";
					break;
				case 0x418:
					s = s + "I";
					break;
				case 0x438:
					s = s + "i";
					break;
				case 0x419:
					s = s + "J";
					break;
				case 0x439:
					s = s + "j";
					break;
				case 0x41A:
					s = s + "K";
					break;
				case 0x43A:
					s = s + "k";
					break;
				case 0x41B:
					s = s + "L";
					break;
				case 0x43B:
					s = s + "l";
					break;
				case 0x41C:
					s = s + "M";
					break;
				case 0x43C:
					s = s + "m";
					break;
				case 0x41D:
					s = s + "N";
					break;
				case 0x43D:
					s = s + "n";
					break;
				case 0x41E:
					s = s + "O";
					break;
				case 0x43E:
					s = s + "o";
					break;
				case 0x41F:
					s = s + "P";
					break;
				case 0x43F:
					s = s + "p";
					break;
				case 0x420:
					s = s + "R";
					break;
				case 0x440:
					s = s + "r";
					break;
				case 0x421:
					s = s + "S";
					break;
				case 0x441:
					s = s + "s";
					break;
				case 0x422:
					s = s + "T";
					break;
				case 0x442:
					s = s + "t";
					break;
				case 0x423:
					s = s + "U";
					break;
				case 0x443:
					s = s + "u";
					break;
				case 0x424:
					s = s + "F";
					break;
				case 0x444:
					s = s + "f";
					break;
				case 0x425:
					s = s + "H";
					break;
				case 0x445:
					s = s + "h";
					break;
				case 0x426:
					s = s + "C";
					break;
				case 0x446:
					s = s + "c";
					break;
				case 0x427:
					s = s + "\u010C";
					break;
				case 0x447:
					s = s + "\u010D";
					break;
				case 0x428:
					s = s + "\u0160";
					break;
				case 0x448:
					s = s + "\u0161";
					break;
				case 0x429:
					s = s + "\u015C";
					break;
				case 0x449:
					s = s + "\u015D";
					break;
				case 0x44A:
					s = s + "\u201D";
					break;
				case 0x44B:
					s = s + "y";
					break;
				case 0x44C:
					s = s + "'";
					break;
				case 0x42D:
					s = s + "\u00C8";
					break;
				case 0x44D:
					s = s + "\u00E8";
					break;
				case 0x42E:
					s = s + "\u01D3";
					break;
				case 0x44E:
					s = s + "\u01D4";
					break;
				case 0x42F:
					s = s + "\u01CD";
					break;
				case 0x44F:
					s = s + "\u01CE";
					break;
				default:
					s = s + arr[i];
			}
		}
		return s;
	}

	public String transliterate_din(String inString) {
		String s = "";
		char[] arr = inString.toCharArray();
		for (int i = 0; i < arr.length; i++) {
			switch (arr[i]) {
				case 0x410:
					s = s + "A";
					break;
				case 0x430:
					s = s + "a";
					break;
				case 0x411:
					s = s + "B";
					break;
				case 0x431:
					s = s + "b";
					break;
				case 0x412:
					s = s + "V";
					break;
				case 0x432:
					s = s + "v";
					break;
				case 0x413:
					s = s + "G";
					break;
				case 0x433:
					s = s + "g";
					break;
				case 0x414:
					s = s + "D";
					break;
				case 0x434:
					s = s + "d";
					break;
				case 0x415:
					s = s + "E";
					break;
				case 0x435:
					s = s + "e";
					break;
				case 0x0CB:
					s = s + "\u00CB";
					break;
				case 0x451:
					s = s + "\u00EB";
					break;
				case 0x416:
					s = s + "\u017D";
					break;
				case 0x436:
					s = s + "\u017E";
					break;
				case 0x417:
					s = s + "Z";
					break;
				case 0x437:
					s = s + "z";
					break;
				case 0x418:
					s = s + "I";
					break;
				case 0x438:
					s = s + "i";
					break;
				case 0x419:
					s = s + "J";
					break;
				case 0x439:
					s = s + "j";
					break;
				case 0x41A:
					s = s + "K";
					break;
				case 0x43A:
					s = s + "k";
					break;
				case 0x41B:
					s = s + "L";
					break;
				case 0x43B:
					s = s + "l";
					break;
				case 0x41C:
					s = s + "M";
					break;
				case 0x43C:
					s = s + "m";
					break;
				case 0x41D:
					s = s + "N";
					break;
				case 0x43D:
					s = s + "n";
					break;
				case 0x41E:
					s = s + "O";
					break;
				case 0x43E:
					s = s + "o";
					break;
				case 0x41F:
					s = s + "P";
					break;
				case 0x43F:
					s = s + "p";
					break;
				case 0x420:
					s = s + "R";
					break;
				case 0x440:
					s = s + "r";
					break;
				case 0x421:
					s = s + "S";
					break;
				case 0x441:
					s = s + "s";
					break;
				case 0x422:
					s = s + "T";
					break;
				case 0x442:
					s = s + "t";
					break;
				case 0x423:
					s = s + "U";
					break;
				case 0x443:
					s = s + "u";
					break;
				case 0x424:
					s = s + "F";
					break;
				case 0x444:
					s = s + "f";
					break;
				case 0x425:
					s = s + "Ch";
					break;
				case 0x445:
					s = s + "ch";
					break;
				case 0x426:
					s = s + "C";
					break;
				case 0x446:
					s = s + "c";
					break;
				case 0x427:
					s = s + "\u010C";
					break;
				case 0x447:
					s = s + "\u010D";
					break;
				case 0x428:
					s = s + "\u0160";
					break;
				case 0x448:
					s = s + "\u0161";
					break;
				case 0x429:
					s = s + "\u0160" + "\u010D";
					break;
				case 0x449:
					s = s + "\u0161" + "\u010D";
					break;
				case 0x44A:
					s = s + "\u201D";
					break;
				case 0x44B:
					s = s + "y";
					break;
				case 0x44C:
					s = s + "'";
					break;
				case 0x42D:
					s = s + "\u0116";
					break;
				case 0x44D:
					s = s + "\u0117";
					break;
				case 0x42E:
					s = s + "Ju";
					break;
				case 0x44E:
					s = s + "ju";
					break;
				case 0x42F:
					s = s + "Ja";
					break;
				case 0x44F:
					s = s + "ja";
					break;
				default:
					s = s + arr[i];
			}
		}
		return s;
	}
}
