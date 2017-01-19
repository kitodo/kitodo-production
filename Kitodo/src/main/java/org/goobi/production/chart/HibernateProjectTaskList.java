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

import java.util.ArrayList;
import java.util.List;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import org.kitodo.data.database.beans.Projekt;
import org.kitodo.data.database.beans.Schritt;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;

public class HibernateProjectTaskList implements IProvideProjectTaskList {

	@Override
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
