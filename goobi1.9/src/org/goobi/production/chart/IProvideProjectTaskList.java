package org.goobi.production.chart;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *   - http://gdz.sub.uni-goettingen.de
 *   - http://www.intranda.com
 *
 * Copyright 2009, Center for Retrospective Digitization, Göttingen (GDZ),
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 */

import java.util.List;

import de.sub.goobi.Beans.Projekt;



/**
 * This interface is used to provide a data source for the extended 
 * Project Statistics
 * 
 * @author Steffen Hankiewicz
 * @author Wulf Riebensahm
 *
 */

public interface IProvideProjectTaskList {
	
	/**
	 * 
	 * @param inProject
	 * @param countImages
	 * @return
	 */
	
	public List<IProjectTask> calculateProjectTasks(Projekt inProject, Boolean countImages, Integer inMax);
}
