package org.goobi.production.chart;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.kitodo.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
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

	@Override
	public List<IProjectTask> calculateProjectTasks(Projekt inProject, Boolean countImages, Integer inMax) {
		List<IProjectTask> myTaskList = new ArrayList<IProjectTask>();
		calculate(inProject, myTaskList, countImages, inMax);
		return myTaskList;
	}

	private synchronized void calculate(Projekt inProject, List<IProjectTask> myTaskList, Boolean countImages,
			Integer inMax) {
		Session session = Helper.getHibernateSession();
		Criteria crit = session.createCriteria(Schritt.class);
		crit.addOrder(Order.asc("reihenfolge"));
		crit.createCriteria("prozess", "proz");
		crit.add(Restrictions.eq("proz.istTemplate", Boolean.FALSE));
		crit.add(Restrictions.eq("proz.projekt", inProject));

		ScrollableResults list = crit.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);

		while (list.next()) {
			Schritt step = (Schritt) list.get(0);
			String shorttitle = (step.getTitel().length() > 60 ? step.getTitel().substring(0, 60) + "..." : step
					.getTitel());

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
