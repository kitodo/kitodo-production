package org.goobi.production.importer;

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
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
/**
 * 
 * @author Igor Toker
 *
 */
@XStreamAlias("property")
public class FireburnProperty {
	@XStreamAsAttribute
	@XStreamAlias("cdName")
	public String cdName;
	@XStreamAsAttribute
	@XStreamAlias("titel")
	public String titel;
	@XStreamAsAttribute
	@XStreamAlias("date")
	public String date;

	//Anzahl der Cd's
	@XStreamAlias("cdnumber")
	@XStreamAsAttribute
	public int cdnumber = 1;
	@XStreamAlias("size")
	@XStreamAsAttribute
	public long size;
	@XStreamAlias("type")
	@XStreamAsAttribute
	public String type;

	public FireburnProperty(String cdName, String titel, String date, int cdnumber, String type, long size) {
		super();
		this.cdName = cdName;
		this.titel = titel;
		this.date = date;

		this.cdnumber = cdnumber;
		this.type = type;
		this.size = size;
	}


}
