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

import de.sub.goobi.helper.Helper;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.helper.enums.TaskStatus;

public class HibernateProjectTaskList implements IProvideProjectTaskList {

    @Override
    public List<IProjectTask> calculateProjectTasks(Project inProject, Boolean countImages, Integer inMax) {
        List<IProjectTask> myTaskList = new ArrayList<>();
        calculate(inProject, myTaskList, countImages, inMax);
        return myTaskList;
    }

    private synchronized void calculate(Project inProject, List<IProjectTask> myTaskList, Boolean countImages,
            Integer inMax) {
        Session session = Helper.getHibernateSession();
        Criteria crit = session.createCriteria(Task.class);
        crit.addOrder(Order.asc("ordering"));
        crit.createCriteria("process", "proz");
        crit.add(Restrictions.eq("proz.template", Boolean.FALSE));
        crit.add(Restrictions.eq("proz.project", inProject));

        ScrollableResults list = crit.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);

        while (list.next()) {
            Task step = (Task) list.get(0);
            String shorttitle = (step.getTitle().length() > 60 ? step.getTitle().substring(0, 60) + "..."
                    : step.getTitle());

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

            if (step.getProcessingStatusEnum() == TaskStatus.DONE) {
                if (countImages) {
                    pt.setStepsCompleted(pt.getStepsCompleted() + step.getProcess().getSortHelperImages());
                } else {
                    pt.setStepsCompleted(pt.getStepsCompleted() + 1);
                }
            }

            if (countImages) {
                pt.setStepsMax(pt.getStepsMax() + step.getProcess().getSortHelperImages());
            } else {
                pt.setStepsMax(pt.getStepsMax() + 1);
            }
        }
    }

}
