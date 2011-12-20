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
package org.goobi.production.flow.statistics.hibernate;
/**
 * This class is passed on to criteriaBuilders so that certain parameter 
 * can be set which depend on the parsing of the filter string 
 * 
 * 
 * @author Wulf
 *
 */

public class ParametersData {
	private Boolean flagCriticalQuery = false;
	private Integer exactStepDone;

	public ParametersData() {
	}

	public ParametersData(Boolean flagCriticalQuery, Integer exactStepDone) {
		this.flagCriticalQuery = flagCriticalQuery;
		this.exactStepDone = exactStepDone;
	}

	public Boolean getFlagCriticalQuery() {
		return flagCriticalQuery;
	}

	public void setCriticalQuery() {
		this.flagCriticalQuery = true;
	}

	public Integer getExactStepDone() {
		return exactStepDone;
	}

	public void setStepDone(Integer exactStepDone) {
		this.exactStepDone = exactStepDone;
	}
}
