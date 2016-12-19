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

package org.goobi.production.flow.statistics.hibernate;

/**
 * This class is passed on to criteriaBuilders so that certain parameter can be set which depend on the parsing of
 * the filter string.
 *
 * @author Wulf
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
