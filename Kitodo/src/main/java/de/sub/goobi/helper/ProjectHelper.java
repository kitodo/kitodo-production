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

package de.sub.goobi.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.goobi.production.flow.statistics.StepInformation;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.helper.enums.TaskStatus;

public class ProjectHelper {

    /**
     * static to reduce load
     *
     * @param project
     *            object
     * @return a GoobiCollection of the following structure: GoobiCollection 1-n
     *         representing the steps each step has the following properties @
     *         stepTitle, stepOrder,
     *         stepCount,stepImageCount,totalProcessCount,totalImageCount which
     *         can get extracted by the IGoobiCollection Interface using the
     *         getItem(&lt;name&gt;) method standard workflow of the project
     *         according to the definition that only steps shared by all
     *         processes are returned. The workflow order is returned according
     *         to the average order return by a grouping by step title consider
     *         workflow structure to be a prototype, it would probably make
     *         things easier, to either assemble the underlying construction in
     *         separate classes or to create a new class with these properties
     */

    @SuppressWarnings("unchecked")
    public static synchronized List<StepInformation> getProjectWorkFlowOverview(Project project) {
        Long totalNumberOfProc = 0L;
        Long totalNumberOfImages = 0L;

        Session session = Helper.getHibernateSession();

        Criteria critTotals = session.createCriteria(Process.class, "proc");
        critTotals.add(Restrictions.eq("proc.template", Boolean.FALSE));
        critTotals.add(Restrictions.eq("proc.project", project));

        ProjectionList proList = Projections.projectionList();

        proList.add(Projections.count("proc.id"));
        proList.add(Projections.sum("proc.sortHelperImages"));

        critTotals.setProjection(proList);

        List<Object> list = critTotals.list();

        for (Object obj : list) {
            Object[] row = (Object[]) obj;

            totalNumberOfProc = (Long) row[FieldList.totalProcessCount.fieldLocation];
            totalNumberOfImages = (Long) row[FieldList.totalImageCount.fieldLocation];
            ;
        }

        proList = null;
        list = null;

        Criteria critSteps = session.createCriteria(Task.class);

        critSteps.createCriteria("process", "proc");
        critSteps.addOrder(Order.asc("ordering"));

        critSteps.add(Restrictions.eq("proc.template", Boolean.FALSE));
        critSteps.add(Restrictions.eq("proc.project", project));

        proList = Projections.projectionList();

        proList.add(Projections.groupProperty(("title")));
        proList.add(Projections.count("id"));
        proList.add(Projections.avg("ordering"));

        critSteps.setProjection(proList);

        // now we have to discriminate the hits where the max number of hits
        // doesn't reach numberOfProcs
        // and extract a workflow, which is the workflow common for all
        // processes according to its titel
        // the position will be calculated by the average of 'reihenfolge' of
        // steps

        list = critSteps.list();

        String title;
        Double averageStepOrder;
        Long numberOfSteps;
        Long numberOfImages;

        List<StepInformation> workFlow = new ArrayList<>();

        for (Object obj : list) {
            Object[] row = (Object[]) obj;

            title = (String) (row[FieldList.stepName.fieldLocation]);
            numberOfSteps = (Long) (row[FieldList.stepCount.fieldLocation]);
            averageStepOrder = (Double) (row[FieldList.stepOrder.fieldLocation]);

            // in this step we only take the steps which are present in each of
            // the workflows
            if (numberOfSteps.equals(totalNumberOfProc)) {
                StepInformation newStep = new StepInformation(title, averageStepOrder);
                newStep.setNumberOfTotalImages(totalNumberOfImages.intValue());
                newStep.setNumberOfTotalSteps(totalNumberOfProc.intValue());
                workFlow.add(newStep);
            }
        }

        Criteria critStepDone = session.createCriteria(Task.class, "step");

        critStepDone.createCriteria("process", "proc");

        critStepDone.add(Restrictions.eq("step.processingStatus", TaskStatus.DONE.getValue()));
        critStepDone.add(Restrictions.eq("proc.template", Boolean.FALSE));
        critStepDone.add(Restrictions.eq("proc.project", project));

        ProjectionList proCount = Projections.projectionList();

        proCount.add(Projections.groupProperty(("step.title")));
        proCount.add(Projections.count("proc.id"));
        proCount.add(Projections.sum("proc.sortHelperImages"));

        critStepDone.setProjection(proCount);

        list = critStepDone.list();

        for (Object obj : list) {

            Object[] row = (Object[]) obj;

            title = (String) (row[FieldList.stepName.fieldLocation]);
            numberOfSteps = (Long) (row[FieldList.stepCount.fieldLocation]);
            numberOfImages = (Long) (row[FieldList.imageCount.fieldLocation]);

            // getting from the workflow collection the collection which
            // represents step <title>
            // we only created one for each step holding the counts of processes
            for (StepInformation currentStep : workFlow) {
                if (currentStep.getTitle().equals(title)) {
                    currentStep.setNumberOfStepsDone(numberOfSteps.intValue());
                    currentStep.setNumberOfImagesDone(numberOfImages.intValue());
                }
            }
        }
        Comparator<StepInformation> comp = new compareWorkflowSteps();
        Collections.sort(workFlow, comp);
        return workFlow;
    }

    /*
     * enum to help addressing the fields of the projections above
     */
    private enum FieldList {
        stepName(0), stepCount(1), stepOrder(2),

        // different projection
        imageCount(2),

        // different projection
        totalProcessCount(0), totalImageCount(1);

        Integer fieldLocation;

        FieldList(Integer fieldLocation) {
            this.fieldLocation = fieldLocation;
        }
    }

    // TODO: move this class to StepInformation
    private static class compareWorkflowSteps implements Comparator<StepInformation>, Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * uses the field "stepOrder".
         */
        @Override
        public int compare(StepInformation firstStepInformation, StepInformation secondStepInformation) {
            Double firstAverageStepOrder = firstStepInformation.getAverageStepOrder();
            Double secondAverageStepOrder = secondStepInformation.getAverageStepOrder();
            return firstAverageStepOrder.compareTo(secondAverageStepOrder);
        }
    }
}
