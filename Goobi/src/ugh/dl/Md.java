package ugh.dl;
/*******************************************************************************
 * ugh.dl / MD.java
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

import org.w3c.dom.Node;

public class Md implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6784880447020540980L;
	private Node content;
	private String id;
	private String type;
	
	public Md(Node content) {
		super();
		this.content = content;
	}

	public Node getContent() {
		return content;
	}

	public void setContent(Node content) {
		this.content = content;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
