/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.goobi.production.importer;
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
