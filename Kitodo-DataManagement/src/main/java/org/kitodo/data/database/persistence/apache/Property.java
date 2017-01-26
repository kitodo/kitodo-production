/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.data.database.persistence.apache;

import java.util.Date;

public class Property {
	
	private int id; 
	private String title;
	private String value;
	private boolean isObligatory;
	private int dataTypeId;
	private String choice;
	private Date creationDate;
	private int container;

	public Property(int id, String title, String value, boolean isObligatory, int dataTypeId, String choice,
			Date creationDate, int container) {
		this.id = id;
		this.title = title;
		this.value = value;
		this.isObligatory = isObligatory;
		this.dataTypeId = dataTypeId;
		this.choice = choice;
		this.creationDate = creationDate;
		this.container = container;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isObligatory() {
		return this.isObligatory;
	}

	public void setIsObligatory(boolean isObligatory) {
		this.isObligatory = isObligatory;
	}

	public int getDataTypeId() {
		return this.dataTypeId;
	}

	public void setDataTypeId(int dataTypeId) {
		this.dataTypeId = dataTypeId;
	}

	public String getChoice() {
		return this.choice;
	}

	public void setChoice(String choice) {
		this.choice = choice;
	}

	public Date getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public int getContainer() {
		return this.container;
	}

	public void setContainer(int container) {
		this.container = container;
	}

}
