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
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.helper.enums.TaskStatus;

/**
 * This class implements the IProvideProjectTaskList and approaches the problem
 * by using a projection on the hibernate criteria, which accelerates data
 * retrieval.
 *
 * @author Wulf Riebensahm
 *
 */
public class HibernateProjectionProjectTaskList implements IProvideProjectTaskList {
    private static final Logger logger = LogManager.getLogger(HibernateProjectionProjectTaskList.class);

    @Override
    public List<IProjectTask> calculateProjectTasks(Project inProject, Boolean countImages, Integer inMax) {
        List<IProjectTask> myTaskList = new ArrayList<>();
        calculate(inProject, myTaskList, countImages, inMax);
        return myTaskList;
    }

    @SuppressWarnings("rawtypes")
    private synchronized void calculate(Project inProject, List<IProjectTask> myTaskList, Boolean countImages,
            Integer inMax) {

        Session session = Helper.getHibernateSession();
        Criteria crit = session.createCriteria(Task.class);

        crit.createCriteria("process", "proc");

        crit.addOrder(Order.asc("ordering"));

        crit.add(Restrictions.eq("proc.template", Boolean.FALSE));
        crit.add(Restrictions.eq("proc.project", inProject));

        ProjectionList proList = Projections.projectionList();

        proList.add(Projections.property("title"));
        proList.add(Projections.property("processingStatus"));
        proList.add(Projections.sum("proc.sortHelperImages"));
        proList.add(Projections.count("id"));
        // proList.add(Projections.groupProperty(("reihenfolge")));

        proList.add(Projections.groupProperty(("title")));
        proList.add(Projections.groupProperty(("processingStatus")));

        crit.setProjection(proList);

        List list = crit.list();

        Iterator it = list.iterator();
        if (!it.hasNext()) {
            logger.debug("No any data!");
        } else {
            Integer rowCount = 0;
            while (it.hasNext()) {
                Object[] row = (Object[]) it.next();
                rowCount++;
                StringBuilder message = new StringBuilder();

                String shorttitle;
                if (((String) row[FieldList.stepName.getFieldLocation()]).length() > 60) {
                    shorttitle = ((String) row[FieldList.stepName.getFieldLocation()]).substring(0, 60) + "...";
                } else {
                    shorttitle = (String) row[FieldList.stepName.getFieldLocation()];
                }

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

                if (TaskStatus.DONE.getValue().equals(row[FieldList.stepStatus.getFieldLocation()])) {
                    if (countImages) {
                        pt.setStepsCompleted((Integer) row[FieldList.pageCount.getFieldLocation()]);
                    } else {
                        pt.setStepsCompleted((Integer) row[FieldList.processCount.getFieldLocation()]);
                    }
                }

                if (countImages) {
                    pt.setStepsMax(pt.getStepsMax() + (Integer) row[FieldList.pageCount.getFieldLocation()]);
                } else {
                    pt.setStepsMax(pt.getStepsMax() + (Integer) row[FieldList.processCount.getFieldLocation()]);
                }

                for (int i = 0; i < row.length; i++) {
                    message.append("|");
                    message.append(row[i]);
                }
                logger.debug(Integer.toString(rowCount) + message);
            }
        }

    }

    private enum FieldList {
        stepName(0), stepStatus(1), pageCount(2), processCount(3);

        Integer fieldLocation;

        FieldList(Integer fieldLocation) {
            this.fieldLocation = fieldLocation;
        }

        Integer getFieldLocation() {
            return this.fieldLocation;
        }
    }

}
