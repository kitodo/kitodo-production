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
	 * @returns a GoobiCollection of the following structure:
	 * @GoobiCollection 1-n representing the steps each step has the following properties @ stepTitle,stepOrder,stepCount,stepImageCount
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
		Integer totalNumberOfProc = 0;
		Integer totalNumberOfImages = 0;

		Session session = Helper.getHibernateSession();

		/*
		 * The following block is representing this native query by way of critera and projection
		 * 
		 * SELECT count(p.prozesseid) as processCount, sum(p.sortHelperImages) as imageCount FROM prozesse p where p.isttemplate=0 and p.projekteid=2
		 * ;
		 */

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

			totalNumberOfProc = (Integer) row[FieldList.totalProcessCount.fieldLocation];
			totalNumberOfImages = (Integer) row[FieldList.totalImageCount.fieldLocation];
			;
		}

		proList = null;
		list = null;

		/*
		 * 
		 * SELECT count(s.reihenfolge), s.titel, (avg(s.reihenfolge)), sum(p.sorthelperimages) FROM schritte s inner join prozesse p on
		 * s.prozesseid=p.prozesseid inner join projekte pr on p.projekteid=pr.projekteid where pr.projekteid=18 group by s.titel order by
		 * avg(s.reihenfolge);
		 * 
		 * The following block is representing this native query by way of critera and projection
		 */

		Criteria critSteps = session.createCriteria(Schritt.class);

		critSteps.createCriteria("prozess", "proc");
		critSteps.addOrder(Order.asc("reihenfolge"));

		critSteps.add(Restrictions.eq("proc.istTemplate", Boolean.FALSE));
		critSteps.add(Restrictions.eq("proc.projekt", project));

		proList = Projections.projectionList();

		proList.add(Projections.groupProperty(("titel")));
		proList.add(Projections.count("id"));
		proList.add(Projections.avg("reihenfolge"));

		// proList.add(Projections.sum("proc.sortHelperImages")); // is not really needed here but will be in the next step

		// proList.add(Projections.groupProperty(("bearbeitungsstatus")));

		critSteps.setProjection(proList);

		// now we have to discriminate the hits where the max number of hits doesn't reach numberOfProcs
		// and extract a workflow, which is the workflow common for all processes according to its titel
		// the position will be calculated by the average of 'reihenfolge' of steps

		list = critSteps.list();

		String title;
		Double averageStepOrder;
		Integer numberOfSteps;
		Integer numberOfImages;

		List<StepInformation> workFlow = new ArrayList<StepInformation>();

		for (Object obj : list) {
			Object[] row = (Object[]) obj;

			title = (String) (row[FieldList.stepName.fieldLocation]);
			numberOfSteps = (Integer) (row[FieldList.stepCount.fieldLocation]);
			averageStepOrder = (Double) (row[FieldList.stepOrder.fieldLocation]);

			// in this step we only take the steps which are present in each of the workflows
			if (numberOfSteps.equals(totalNumberOfProc)) {
				StepInformation newStep = new StepInformation(title, averageStepOrder);
				newStep.setNumberOfTotalImages(totalNumberOfImages);
				newStep.setNumberOfTotalSteps(totalNumberOfProc);
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
			numberOfSteps = (Integer) (row[FieldList.stepCount.fieldLocation]);
			numberOfImages = (Integer) (row[FieldList.imageCount.fieldLocation]);

			// getting from the workflow collection the collection which represents step <title>
			// we only created one for each step holding the counts of processes
			for (StepInformation currentStep : workFlow) {
				if (currentStep.getTitle().equals(title)) {
					currentStep.setNumberOfStepsDone(numberOfSteps);
					currentStep.setNumberOfImagesDone(numberOfImages);
				}
			}
		}
		Comparator<StepInformation> comp = new compareWorkflowSteps();
		Collections.sort(workFlow, comp);
		return workFlow;
	}

	/*
	 * enum to help adressing the fields of the projections above
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
		public int compare(StepInformation arg0, StepInformation arg1) {
			Double d1 = arg0.getAverageStepOrder();
			Double d2 = arg1.getAverageStepOrder();
			return d1.compareTo(d2);
		}
	}

	// synchronized public static IGoobiCollection<IGoobiCollection<IProperty>> getWorkFlow (IGoobiObject instance, Boolean notOnlyCommonFlow) {
	@SuppressWarnings("unchecked")
	public static List<StepInformation> getWorkFlow(Projekt inProj, Boolean notOnlyCommonFlow) {
		Integer totalNumberOfProc = 0;
		// false as default
		if (notOnlyCommonFlow == null) {
			notOnlyCommonFlow = false;
		}
		List<StepInformation> workFlow = new ArrayList<StepInformation>();
		Session session = Helper.getHibernateSession();
		/*
		 * The following block is representing this native query by way of critera and projection
		 * 
		 * SELECT count(p.prozesseid) as processCount, sum(p.sortHelperImages) as imageCount FROM prozesse p where p.isttemplate=0 and p.projekteid=2
		 * ;
		 */

		Criteria critTotals = session.createCriteria(Prozess.class, "proc");
		critTotals.add(Restrictions.eq("proc.istTemplate", Boolean.FALSE));
		critTotals.add(Restrictions.eq("proc.projekt", inProj));

		ProjectionList proList = Projections.projectionList();

		proList.add(Projections.count("proc.id"));

		critTotals.setProjection(proList);

		List<Object> list = critTotals.list();

		for (Object obj : list) {
			Object[] row = (Object[]) obj;

			totalNumberOfProc = (Integer) row[FieldList.totalProcessCount.fieldLocation];
		}

		proList = null;
		list = null;

		/*
		 * 
		 * SELECT count(s.reihenfolge), s.titel, (avg(s.reihenfolge)), sum(p.sorthelperimages) FROM schritte s inner join prozesse p on
		 * s.prozesseid=p.prozesseid inner join projekte pr on p.projekteid=pr.projekteid where pr.projekteid=18 group by s.titel order by
		 * avg(s.reihenfolge);
		 * 
		 * The following block is representing this native query by way of critera and projection
		 */

		Criteria critSteps = session.createCriteria(Schritt.class);

		critSteps.createCriteria("prozess", "proc");
		critSteps.addOrder(Order.asc("reihenfolge"));

		critSteps.add(Restrictions.eq("proc.istTemplate", Boolean.FALSE));
		critSteps.add(Restrictions.eq("proc.projekt", inProj));

		proList = Projections.projectionList();

		proList.add(Projections.groupProperty(("titel")));
		proList.add(Projections.count("id"));
		proList.add(Projections.avg("reihenfolge"));

		// proList.add(Projections.sum("proc.sortHelperImages")); // is not really needed here but will be in the next step

		// proList.add(Projections.groupProperty(("bearbeitungsstatus")));

		critSteps.setProjection(proList);

		// now we have to discriminate the hits where the max number of hits doesn't reach numberOfProcs
		// and extract a workflow, which is the workflow common for all processes according to its titel
		// the position will be calculated by the average of 'reihenfolge' of steps

		list = critSteps.list();

		String title;
		Double averageStepOrder;
		Integer numberOfSteps;

		for (Object obj : list) {
			Object[] row = (Object[]) obj;

			title = (String) (row[FieldList.stepName.fieldLocation]);
			numberOfSteps = (Integer) (row[FieldList.stepCount.fieldLocation]);
			averageStepOrder = (Double) (row[FieldList.stepOrder.fieldLocation]);

			// in this step we only take the steps which are present in each of the workflows unless notOnlyCommonFlow is set to true
			if (numberOfSteps.equals(totalNumberOfProc) || notOnlyCommonFlow) {
				// for each step we create a new collection which is child of the collection workFlow created above
				StepInformation newStep = new StepInformation(title, averageStepOrder);
				workFlow.add(newStep);
				// should probably use a different implementation of IGoobiProperty
				// maybe StandardGoobiProperty
				// for each field we create a property, which is part of the newStep collection

			}
		}
		Comparator<StepInformation> comp = new compareWorkflowSteps();
		Collections.sort(workFlow, comp);
		return workFlow;

	}
}
