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

public class StepObject {

	private int id;
	private String title;
	private int ordering;
	private int processingStatus;
	private Date processingTime;
	private Date processingBegin;
	private Date processingEnd;
	private int processingUser;
	private Integer editType;
	private boolean typeAutomatic = false;
	private boolean typeExport = false;
	private int processId;
	private boolean typeReadAccess = false;
	private boolean typeWriteAccess = false;
	private boolean typeMetadataAccess = false;
	private boolean typeFinishImmediately = false;
	private String stepPlugin;
	private String validationPlugin;

	public StepObject(int id, String title, int ordering, int processingStatus, Date processingTime, Date processingBegin,
			Date processingEnd, int processingUser, Integer editType, boolean typeExport, boolean typeAutomatic, int processId,
			boolean readAccess, boolean writeAccess, boolean metadataAccess, boolean typeFinishImmediately, String stepPlugin, String validationPlugin) {
		super();
		this.id = id;
		this.title = title;
		this.ordering = ordering;
		this.processingStatus = processingStatus;
		this.processingTime = processingTime;
		this.processingBegin = processingBegin;
		this.processingEnd = processingEnd;
		this.processingUser = processingUser;
		this.editType = editType;
		this.typeExport = typeExport;
		this.typeAutomatic = typeAutomatic;
		this.processId = processId;
		this.setTypeReadAccess(readAccess);
		this.typeWriteAccess = writeAccess;
		this.typeMetadataAccess = metadataAccess;
		this.setTypeFinishImmediately(typeFinishImmediately);
		this.stepPlugin = stepPlugin;
		this.validationPlugin = validationPlugin;

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

	public int getOrdering() {
		return this.ordering;
	}

	public void setOrdering(int ordering) {
		this.ordering = ordering;
	}

	public int getProcessingStatus() {
		return this.processingStatus;
	}

	public void setProcessingStatus(int processingStatus) {
		this.processingStatus = processingStatus;
	}

	public Date getProcessingTime() {
		return this.processingTime;
	}

	public void setProcessingTime(Date processingTime) {
		this.processingTime = processingTime;
	}

	public Date getProcessingBegin() {
		return this.processingBegin;
	}

	public void setProcessingBegin(Date processingBegin) {
		this.processingBegin = processingBegin;
	}

	public Date getProcessingEnd() {
		return this.processingEnd;
	}

	public void setProcessingEnd(Date processingEnd) {
		this.processingEnd = processingEnd;
	}

	public Integer getEditType() {
		return this.editType;
	}

	public void setEditType(Integer editType) {
		this.editType = editType;
	}

	public int getProcessingUser() {
		return this.processingUser;
	}

	public void setProcessingUser(int processingUser) {
		this.processingUser = processingUser;
	}

	public boolean isTypeAutomatic() {
		return this.typeAutomatic;
	}

	public void setTypeAutomatic(boolean typeAutomatic) {
		this.typeAutomatic = typeAutomatic;
	}

	public int getProcessId() {
		return this.processId;
	}

	public void setProcessId(int processId) {
		this.processId = processId;
	}

	public boolean isTypeExport() {
		return this.typeExport;
	}

	public void setTypeExport(boolean typeExport) {
		this.typeExport = typeExport;
	}

	public boolean isTypeWriteAccess() {
		return this.typeWriteAccess;
	}

	public void setTypeWriteAccess(boolean typeWriteAccess) {
		this.typeWriteAccess = typeWriteAccess;
	}

	public boolean isTypeMetadataAccess() {
		return this.typeMetadataAccess;
	}

	public void setTypeMetadataAccess(boolean typeMetadataAccess) {
		this.typeMetadataAccess = typeMetadataAccess;
	}

	public boolean isTypeReadAccess() {
		return typeReadAccess;
	}

	public void setTypeReadAccess(boolean typeReadAccess) {
		this.typeReadAccess = typeReadAccess;
	}

	public boolean isTypeFinishImmediately() {
		return typeFinishImmediately;
	}

	public void setTypeFinishImmediately(boolean typeFinishImmediately) {
		this.typeFinishImmediately = typeFinishImmediately;
	}

	public String getStepPlugin() {
		return stepPlugin;
	}

	public void setStepPlugin(String stepPlugin) {
		this.stepPlugin = stepPlugin;
	}

	public String getValidationPlugin() {
		return validationPlugin;
	}

	public void setValidationPlugin(String validationPlugin) {
		this.validationPlugin = validationPlugin;
	}

}
