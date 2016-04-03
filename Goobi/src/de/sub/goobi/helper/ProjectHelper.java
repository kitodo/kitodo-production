package de.sub.goobi.helper;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *     		- http://www.goobi.org
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

import de.sub.goobi.beans.Projekt;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Schritt;
import de.sub.goobi.helper.enums.StepStatus;

public class ProjectHelper {

	/**
	 * static to reduce load
	 *
	 *
	 * @param instance
	 * @return a GoobiCollection of the following structure:
	 *  GoobiCollection 1-n representing the steps each step has the following properties @ stepTitle,stepOrder,stepCount,stepImageCount
	 *                  ,totalProcessCount,totalImageCount which can get extracted by the IGoobiCollection Inteface using the getItem(<name>) method
	 *
	 *                  standard workflow of the project according to the definition that only steps shared by all processes are returned. The
	 *                  workflow order is returned according to the average order returen by a grouping by step titel
	 *
	 *                  consider workflow structure to be a prototype, it would probably make things easier, to either assemble the underlying
	 *                  construction in separate classes or to create a new class with these properties
	 */

	@SuppressWarnings("unchecked")
	synchronized public static List<StepInformation> getProjectWorkFlowOverview(Projekt project) {
		Long totalNumberOfProc = 0l;
		Long totalNumberOfImages = 0l;

		Session session = Helper.getHibernateSession();



		Criteria critTotals = session.createCriteria(Prozess.class, "proc");
		critTotals.add(Restrictions.eq("proc.istTemplate", Boolean.FALSE));
		critTotals.add(Restrictions.eq("proc.projekt", project));

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



		Criteria critSteps = session.createCriteria(Schritt.class);

		critSteps.createCriteria("prozess", "proc");
		critSteps.addOrder(Order.asc("reihenfolge"));

		critSteps.add(Restrictions.eq("proc.istTemplate", Boolean.FALSE));
		critSteps.add(Restrictions.eq("proc.projekt", project));

		proList = Projections.projectionList();

		proList.add(Projections.groupProperty(("titel")));
		proList.add(Projections.count("id"));
		proList.add(Projections.avg("reihenfolge"));


		critSteps.setProjection(proList);

		// now we have to discriminate the hits where the max number of hits doesn't reach numberOfProcs
		// and extract a workflow, which is the workflow common for all processes according to its titel
		// the position will be calculated by the average of 'reihenfolge' of steps

		list = critSteps.list();

		String title;
		Double averageStepOrder;
		Long numberOfSteps;
		Long numberOfImages;

		List<StepInformation> workFlow = new ArrayList<StepInformation>();

		for (Object obj : list) {
			Object[] row = (Object[]) obj;

			title = (String) (row[FieldList.stepName.fieldLocation]);
			numberOfSteps = (Long) (row[FieldList.stepCount.fieldLocation]);
			averageStepOrder = (Double) (row[FieldList.stepOrder.fieldLocation]);

			// in this step we only take the steps which are present in each of the workflows
			if (numberOfSteps.equals(totalNumberOfProc)) {
				StepInformation newStep = new StepInformation(title, averageStepOrder);
				newStep.setNumberOfTotalImages(totalNumberOfImages.intValue());
				newStep.setNumberOfTotalSteps(totalNumberOfProc.intValue());
				workFlow.add(newStep);
			}
		}

		Criteria critStepDone = session.createCriteria(Schritt.class, "step");

		critStepDone.createCriteria("prozess", "proc");

		critStepDone.add(Restrictions.eq("step.bearbeitungsstatus", StepStatus.DONE.getValue()));
		critStepDone.add(Restrictions.eq("proc.istTemplate", Boolean.FALSE));
		critStepDone.add(Restrictions.eq("proc.projekt", project));

		ProjectionList proCount = Projections.projectionList();

		proCount.add(Projections.groupProperty(("step.titel")));
		proCount.add(Projections.count("proc.id"));
		proCount.add(Projections.sum("proc.sortHelperImages"));

		critStepDone.setProjection(proCount);

		list = critStepDone.list();

		for (Object obj : list) {

			Object[] row = (Object[]) obj;

			title = (String) (row[FieldList.stepName.fieldLocation]);
			numberOfSteps = (Long) (row[FieldList.stepCount.fieldLocation]);
			numberOfImages = (Long) (row[FieldList.imageCount.fieldLocation]);

			// getting from the workflow collection the collection which represents step <title>
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
	static private enum FieldList {
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
		 * uses the field "stepOrder"
		 */
		@Override
		public int compare(StepInformation arg0, StepInformation arg1) {
			Double d1 = arg0.getAverageStepOrder();
			Double d2 = arg1.getAverageStepOrder();
			return d1.compareTo(d2);
		}
	}
}
