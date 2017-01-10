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

package org.goobi.production.chart;

import de.sub.goobi.beans.Projekt;

import java.util.List;

/**
 * This interface is used to provide a data source for the extended Project Statistics
 *
 * @author Steffen Hankiewicz
 * @author Wulf Riebensahm
 */

public interface IProvideProjectTaskList {

	/**
	 * @param inProject add description
	 * @param countImages add description
	 * @return List
	 */

	public List<IProjectTask> calculateProjectTasks(Projekt inProject, Boolean countImages, Integer inMax);
}
