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

package org.goobi.production.chart;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import de.sub.goobi.beans.Projekt;
import de.sub.goobi.beans.Schritt;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;

public class HibernateProjectTaskList implements IProvideProjectTaskList {

	public List<IProjectTask> calculateProjectTasks(Projekt inProject, Boolean countImages, Integer inMax) {
		List<IProjectTask> myTaskList = new ArrayList<IProjectTask>();
		calculate(inProject, myTaskList, countImages, inMax);
		return myTaskList;
	}

	private synchronized void calculate(Projekt inProject, List<IProjectTask> myTaskList, Boolean countImages, Integer inMax) {
		Session session = Helper.getHibernateSession();
		Criteria crit = session.createCriteria(Schritt.class);
		crit.addOrder(Order.asc("reihenfolge"));
		crit.createCriteria("prozess", "proz");
		crit.add(Restrictions.eq("proz.istTemplate", Boolean.FALSE));
		crit.add(Restrictions.eq("proz.projekt", inProject));

		ScrollableResults list = crit.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);

		while (list.next()) {
			Schritt step = (Schritt) list.get(0);
			String shorttitle = (step.getTitel().length() > 60 ? step.getTitel().substring(0, 60) + "..." : step.getTitel());

			IProjectTask pt = null;
			for (IProjectTask task : myTaskList) {
				if (task.getTitle().equals(shorttitle)) {
					pt = task;
					break;
				}
			}
			if (pt == null) {
				pt = new ProjectTask(shorttitle, 0, 0);
				myTaskList.add(pt);
			}

			if (step.getBearbeitungsstatusEnum() == StepStatus.DONE) {
				if (countImages) {
					pt.setStepsCompleted(pt.getStepsCompleted() + step.getProzess().getSortHelperImages());
				} else {
					pt.setStepsCompleted(pt.getStepsCompleted() + 1);
				}
			}
			
			if (countImages) {
				pt.setStepsMax(pt.getStepsMax() + step.getProzess().getSortHelperImages());
			} else {
				pt.setStepsMax(pt.getStepsMax() + 1);
			}
			
		}
	}

}
