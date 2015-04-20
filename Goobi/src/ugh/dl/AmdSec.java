package ugh.dl;
/*******************************************************************************
 * ugh.dl / AmdSec.java
 * 
 * Copyright 2012 intranda GmbH, Göttingen
 * 
 * http://www.intranda.com
 * http://www.digiverso.com
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This Library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

public class AmdSec implements Serializable {

	/**
	 * @author Robert Sehr
	 */
	private static final long serialVersionUID = -2651069769792564435L;
	private String id;
	private ArrayList<Md> techMdList;

	public AmdSec(ArrayList<Md> techMdList) {
		super();
		this.techMdList = techMdList;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ArrayList<Md> getTechMdList() {
		return techMdList;
	}

	public void setTechMdList(ArrayList<Md> techMdList) {
		this.techMdList = techMdList;
	}

	public void addTechMd(Md techMd) {
		if (techMdList == null) {
			techMdList = new ArrayList<Md>();
		}
		this.techMdList.add(techMd);
	}

	public List<Node> getTechMdsAsNodes() {
		List<Node> nodeList = new ArrayList<Node>();
		if (this.techMdList != null) {
			for (Md techMd : this.techMdList) {
				nodeList.add(techMd.getContent());
			}
		}
		return nodeList;
	}

}
