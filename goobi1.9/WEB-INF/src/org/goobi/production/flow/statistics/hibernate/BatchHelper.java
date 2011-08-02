package org.goobi.production.flow.statistics.hibernate;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2011, intranda GmbH, Göttingen
 * 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.text.StrTokenizer;
import org.apache.log4j.Logger;
import org.goobi.production.flow.IlikeExpression;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import de.sub.goobi.Beans.Batch;
import de.sub.goobi.Beans.Benutzer;
import de.sub.goobi.Beans.Projekt;
import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Forms.LoginForm;
import de.sub.goobi.Persistence.BenutzerDAO;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.PaginatingCriteria;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;

/**
 * class provides methods used by implementations of IEvaluableFilter
 * 
 * @author Robert Sehr
 * 
 */
public class BatchHelper {

	private static final Logger logger = Logger.getLogger(BatchHelper.class);

	/**
	 * limit query to project (formerly part of ProzessverwaltungForm)
	 * 
	 * @param crit
	 */
	protected static void limitToUserAccessRights(Conjunction con) {
		/* restriction to specific projects if not with admin rights */
		LoginForm loginForm = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
		Benutzer aktuellerNutzer = null;
		try {
			aktuellerNutzer = new BenutzerDAO().get(loginForm.getMyBenutzer().getId());
		} catch (DAOException e) {
			logger.warn("DAOException", e);
		}
		if (aktuellerNutzer != null) {
			if (loginForm.getMaximaleBerechtigung() > 1) {
				Disjunction dis = Restrictions.disjunction();
				for (Projekt proj : aktuellerNutzer.getProjekteList()) {
					dis.add(Restrictions.eq("projekt", proj));
				}
				con.add(dis);
			}
		}
	}

	protected static void limitToUserAssignedSteps(Conjunction con) {
		LoginForm loginForm = (LoginForm) Helper.getManagedBeanValue("#{LoginForm}");
		Benutzer aktuellerNutzer = null;
		Session session = Helper.getHibernateSession();
		try {
			aktuellerNutzer = new BenutzerDAO().get(loginForm.getMyBenutzer().getId());
		} catch (DAOException e) {
			logger.warn("DAOException", e);
		}
		if (aktuellerNutzer != null) {
			if (loginForm.getMaximaleBerechtigung() > 1) {
				List<Integer> idList = new ArrayList<Integer>();
				idList.add(Integer.valueOf(0));
				// usergroups only
				Criteria critBatch = session.createCriteria(Batch.class);
				Criteria critProj = critBatch.createCriteria("project", "proj");
				// project
				critProj.createCriteria("benutzer", "projektbenutzer");
				critBatch.add(Restrictions.eq("projektbenutzer.id", aktuellerNutzer.getId()));
				
				// template
				Criteria critProc = critBatch.createCriteria("processes", "proz");
				critBatch.add(Restrictions.eq("proz.istTemplate", Boolean.valueOf(false)));

				// only open steps
				Criteria critStep = critProc.createCriteria("schritte", "step");
				critBatch.add(Restrictions.or(Restrictions.eq("step.bearbeitungsstatus", Integer.valueOf(1)),
						Restrictions.like("step.bearbeitungsstatus", Integer.valueOf(2))));
				critBatch.add(Restrictions.eqProperty("step.titel", "stepTitle"));
				
				// only usergroups
				critStep.createCriteria("benutzergruppen", "gruppen").createCriteria("benutzer", "gruppennutzer");
				critBatch.add(Restrictions.eq("gruppennutzer.id", aktuellerNutzer.getId()));
				critBatch.setProjection(Projections.id());
				for (@SuppressWarnings("unchecked")
				Iterator<Object> it = critBatch.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list().iterator(); it.hasNext();) {
					idList.add((Integer) it.next());
				}
				

				
				
				//	Users only

				Criteria critUser = session.createCriteria(Batch.class);
				Criteria critUserProj = critUser.createCriteria("project", "project");
				// project
				critUserProj.createCriteria("benutzer", "projektbenutzer");
				critUser.add(Restrictions.eq("projektbenutzer.id", aktuellerNutzer.getId()));
				
				// template
				Criteria critUserProc = critUser.createCriteria("processes", "proz");
				critUser.add(Restrictions.eq("proz.istTemplate", Boolean.valueOf(false)));

				// only open steps
				Criteria critUserStep = critUserProc.createCriteria("schritte", "step");
				critUser.add(Restrictions.or(Restrictions.eq("step.bearbeitungsstatus", Integer.valueOf(1)),
						Restrictions.like("step.bearbeitungsstatus", Integer.valueOf(2))));
				critUser.add(Restrictions.eqProperty("step.titel", "stepTitle"));
				critUserStep.createCriteria("benutzer", "nutzer");
				critUserStep.add(Restrictions.eq("nutzer.id", aktuellerNutzer.getId()));

				
				critUser.setProjection(Projections.id());
				for (@SuppressWarnings("unchecked")
				Iterator<Object> it = critUser.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list().iterator(); it.hasNext();) {
					idList.add((Integer) it.next());
				}
				con.add(Restrictions.in("id", idList));
			}
		}
	}

	/**
	 * This functions extracts the Integer from the parameters passed with the
	 * step filter in first positon.
	 * 
	 * @param String
	 *            parameter
	 * @author Wulf Riebensahm
	 * @return Integer
	 ****************************************************************************/
	protected static Integer getStepStart(String parameter) {
		String[] strArray = parameter.split("-");
		return Integer.parseInt(strArray[0]);
	}

	/**
	 * This functions extracts the Integer from the parameters passed with the
	 * step filter in last positon.
	 * 
	 * @param String
	 *            parameter
	 * @author Wulf Riebensahm
	 * @return Integer
	 ****************************************************************************/
	protected static Integer getStepEnd(String parameter) {
		String[] strArray = parameter.split("-");
		return Integer.parseInt(strArray[1]);
	}



	/**
	 * Filter processes for done steps range
	 * 
	 * @param crit
	 *            {@link Criteria} to extend
	 * @param tok
	 *            part of filter string to use
	 * @param inStatus
	 *            {@link StepStatus} of searched step
	 ****************************************************************************/
	protected static void filterStepRange(Conjunction con, String parameters, StepStatus inStatus) {
		con.add(Restrictions.and(
				Restrictions.and(Restrictions.ge("steps.reihenfolge", BatchHelper.getStepStart(parameters)),
						Restrictions.le("steps.reihenfolge", BatchHelper.getStepEnd(parameters))),
				Restrictions.eq("steps.bearbeitungsstatus", inStatus.getValue().intValue())));
	}

	/**
	 * Filter processes for steps name with given status
	 * 
	 * @param inStatus
	 *            {@link StepStatus} of searched step
	 * @param crit
	 *            {@link Criteria} to extend
	 * @param parameters
	 *            part of filter string to use
	 ****************************************************************************/
	protected static void filterStepName(Conjunction con, String parameters, StepStatus inStatus) {
		if (con == null) {
			con = Restrictions.conjunction();
		}
		con.add(Restrictions.and(Restrictions.like("steps.titel", "%" + parameters + "%"),
				Restrictions.eq("steps.bearbeitungsstatus", inStatus.getValue().intValue())));
	}

	/**
	 * Filter processes for done steps min
	 * 
	 * @param crit
	 *            {@link Criteria} to extend
	 * @param parameters
	 *            part of filter string to use
	 * @param inStatus
	 *            {@link StepStatus} of searched step
	 ****************************************************************************/
	protected static void filterStepMin(Conjunction con, String parameters, StepStatus inStatus) {
		if (con == null) {
			con = Restrictions.conjunction();
		}
		con.add(Restrictions.and(Restrictions.ge("steps.reihenfolge", BatchHelper.getStepStart(parameters)),
				Restrictions.eq("steps.bearbeitungsstatus", inStatus.getValue().intValue())));
	}

	/**
	 * Filter processes for done steps max
	 * 
	 * @param crit
	 *            {@link Criteria} to extend
	 * @param parameters
	 *            part of filter string to use
	 * @param inStatus
	 *            {@link StepStatus} of searched step
	 ****************************************************************************/
	protected static void filterStepMax(Conjunction con, String parameters, StepStatus inStatus) {
		if (con == null) {
			con = Restrictions.conjunction();
		}
		con.add(Restrictions.and(Restrictions.le("steps.reihenfolge", BatchHelper.getStepEnd(parameters)),
				Restrictions.eq("steps.bearbeitungsstatus", inStatus.getValue().intValue())));
	}

	/**
	 * Filter processes for done steps exact
	 * 
	 * @param crit
	 *            {@link Criteria} to extend
	 * @param parameters
	 *            part of filter string to use
	 * @param inStatus
	 *            {@link StepStatus} of searched step
	 ****************************************************************************/
	protected static void filterStepExact(Conjunction con, String parameters, StepStatus inStatus) {
		con.add(Restrictions.and(Restrictions.eq("steps.reihenfolge", BatchHelper.getStepStart(parameters)),
				Restrictions.eq("steps.bearbeitungsstatus", inStatus.getValue().intValue())));
	}

	/**
	 * Filter processes for done steps by user
	 * 
	 * @param crit
	 *            {@link Criteria} to extend
	 * @param tok
	 *            part of filter string to use
	 ****************************************************************************/
	protected static void filterStepDoneUser(Conjunction con, String tok) {
		/*
		 * filtering by a certain done step, which the current user finished
		 */
		String login = tok.substring("stepDoneUser:".length());
		con.add(Restrictions.eq("user.login", login));
	}

	/**
	 * Filter processes by project
	 * 
	 * @param crit
	 *            {@link Criteria} to extend
	 * @param tok
	 *            part of filter string to use
	 ****************************************************************************/
	protected static void filterProject(Conjunction con, String tok) {
		/* filter according to linked project */
		con.add(Restrictions.like("proj.titel", "%" + tok.substring(5) + "%"));
	}

	/**
	 * Filter processes by scan template
	 * 
	 * @param crit
	 *            {@link Criteria} to extend
	 * @param tok
	 *            part of filter string to use
	 ****************************************************************************/
	protected static void filterScanTemplate(Conjunction con, String tok) {
		/* Filtering by signature */
		// crit.add(Restrictions.like("vorleig.titel", "%Signatur%"));
		con.add(Restrictions.like("vorleig.wert", "%" + tok.substring(5) + "%"));
	}

	protected static void filterStepProperty(Conjunction con, String tok) {
		/* Filtering by signature */
		String[] ts = tok.substring(8).split(":");
		// crit.add(Restrictions.like("vorleig.titel", "%Signatur%"));

		if (ts.length > 1) {
			con.add(Restrictions.and(Restrictions.like("schritteig.wert", "%" + ts[1] + "%"),
					Restrictions.like("schritteig.titel", "%" + ts[0] + "%")));
		} else {
			con.add(Restrictions.like("schritteig.wert", "%" + tok.substring(8) + "%"));
		}
	}

	protected static void filterProcessProperty(Conjunction con, String tok) {
		/* Filtering by signature */
		// crit.add(Restrictions.like("vorleig.titel", "%Signatur%"));
		/* Filtering by signature */
		String[] ts = tok.substring(tok.indexOf(":") + 1).split(":");
		// crit.add(Restrictions.like("vorleig.titel", "%Signatur%"));

		if (ts.length > 1) {
			con.add(Restrictions.and(Restrictions.like("prozesseig.wert", "%" + ts[1] + "%"),
					Restrictions.like("prozesseig.titel", "%" + ts[0] + "%")));
		} else {
			con.add(Restrictions.like("prozesseig.wert", "%" + ts[0] + "%"));
		}
	}

	/**
	 * Filter processes by Ids
	 * 
	 * @param crit
	 *            {@link Criteria} to extend
	 * @param tok
	 *            part of filter string to use
	 ****************************************************************************/
	protected static void filterIds(Conjunction con, String tok) {
		/* filtering by ids */
		// Disjunction dis = Restrictions.disjunction();
		List<Integer> listIds = new ArrayList<Integer>();
		if (tok.substring(5).length() > 0) {
			// tok.substring(5).split(" ")
			String[] tempids = tok.substring(5).split(" ");
			for (int i = 0; i < tempids.length; i++) {
				int tempid = Integer.parseInt(tempids[i]);
				listIds.add(tempid);
			}
		}
		con.add(Restrictions.in("id", listIds));
	}

	/**
	 * Filter processes by workpiece
	 * 
	 * @param crit
	 *            {@link Criteria} to extend
	 * @param tok
	 *            part of filter string to use
	 ****************************************************************************/
	protected static void filterWorkpiece(Conjunction con, String tok) {
		/* filter according signature */

		con.add(Restrictions.like("werkeig.wert", "%" + tok.substring(5) + "%"));
	}

	/**
	 * This method builds a criteria depending on a filter string and some other
	 * parameters passed on along the initial criteria. The filter is parsed and
	 * depending on which data structures are used for applying filtering
	 * restrictions conjunctions are formed and collect the restrictions and
	 * then will be applied on the corresponding criteria. A criteria is only
	 * added if needed for the presence of filters applying to it.
	 * 
	 * 
	 * @param inFilter
	 * @param crit
	 * @param isTemplate
	 * @param returnParameters
	 *            Object containing values which need to be set and returned to
	 *            UserDefinedFilter
	 * @param userAssignedStepsOnly
	 * @param stepOpenOnly
	 * @return String used to pass on error messages about errors in the filter
	 *         expression
	 */
	protected static String criteriaBuilder(Session session, String inFilter, PaginatingCriteria crit) {
//		boolean flagSetCritProjects = false;

		// keeping a reference to the passed criteria
		Criteria inCrit = crit;
		@SuppressWarnings("unused")
		Criteria critProject = null;
		Criteria critProcess = null;

		// to collect and return feedback about erroneous use of filter
		// expressions
		String message = new String("");

		StrTokenizer tokenizer = new StrTokenizer(inFilter, ' ', '\"');

		// conjunctions collecting conditions
		Conjunction conjWorkPiece = null;
		Conjunction conjProjects = null;
//		Conjunction conjSteps = null;
		Conjunction conjProcesses = null;
		Conjunction conjTemplates = null;
		Conjunction conjUsers = null;
		Conjunction conjStepProperties = null;
		Conjunction conjProcessProperties = null;
		
		Conjunction conBatchUsers = Restrictions.conjunction();
		
		limitToUserAssignedSteps(conBatchUsers);
		
		conjProjects = Restrictions.conjunction();
		limitToUserAccessRights(conjProjects);
		// in case nothing is set here it needs to be removed again
		// happens if user has admin rights
		if (conjProjects.toString().equals("()")) {
			conjProjects = null;
//			flagSetCritProjects = true;
		}
		if (conBatchUsers.toString().equals("()")) {
			conBatchUsers = null;
//			flagSetCritProjects = true;
		}

//		List<String> aliases = new ArrayList<String>();
		// this is needed for evaluating a filter string
		while (tokenizer.hasNext()) {
			String tok = tokenizer.nextToken().trim();

			if (tok.startsWith("processeig:")) {
				if (conjProcessProperties == null) {
					conjProcessProperties = Restrictions.conjunction();
				}
				BatchHelper.filterProcessProperty(conjProcessProperties, tok);
			} else if (tok.startsWith("stepeig:")) {
				if (conjStepProperties == null) {
					conjStepProperties = Restrictions.conjunction();
				}
				BatchHelper.filterStepProperty(conjStepProperties, tok);
//			}
//
//			// search over steps
//			// original filter, is left here for compatibility reason
//			// doesn't fit into new keyword scheme
//			else if (tok.startsWith("step:")) {
//				if (conjSteps == null) {
//					conjSteps = Restrictions.conjunction();
//				}
//				message = message + createHistoricFilter(conjSteps, tok, false);
//
//			} else if (tok.toLowerCase().startsWith("stepinwork:")) {
//				if (conjSteps == null) {
//					conjSteps = Restrictions.conjunction();
//				}
//				message = message + (createStepFilters(null, conjSteps, tok, 11, StepStatus.INWORK));
//
//				// new keyword stepLocked implemented
//			} else if (tok.toLowerCase().startsWith("steplocked:")) {
//				if (conjSteps == null) {
//					conjSteps = Restrictions.conjunction();
//				}
//				message = message + (createStepFilters(null, conjSteps, tok, 11, StepStatus.LOCKED));
//
//				// new keyword stepOpen implemented
//			} else if (tok.toLowerCase().startsWith("stepopen:")) {
//				if (conjSteps == null) {
//					conjSteps = Restrictions.conjunction();
//				}
//				message = message + (createStepFilters(null, conjSteps, tok, 9, StepStatus.OPEN));
//
//				// new keyword stepDone implemented
//			} else if (tok.toLowerCase().startsWith("stepdone:")) {
//				if (conjSteps == null) {
//					conjSteps = Restrictions.conjunction();
//				}
//				message = message + (createStepFilters(null, conjSteps, tok, 9, StepStatus.DONE));
//
//				// new keyword stepDoneTitle implemented, replacing so far
//				// undocumented
//			} else if (tok.toLowerCase().startsWith("stepdonetitle:")) {
//				if (conjSteps == null) {
//					conjSteps = Restrictions.conjunction();
//				}
//				String stepTitel = tok.substring("stepDoneTitle:".length());
//				BatchHelper.filterStepName(conjSteps, stepTitel, StepStatus.DONE);

			} else if (tok.toLowerCase().startsWith("stepdoneuser:")) {
				if (conjUsers == null) {
					conjUsers = Restrictions.conjunction();
				}
				BatchHelper.filterStepDoneUser(conjUsers, tok);

			} else if (tok.startsWith("proj:")) {
				if (conjProjects == null) {
					conjProjects = Restrictions.conjunction();
				}
				BatchHelper.filterProject(conjProjects, tok);

			} else if (tok.startsWith("vorl:")) {
				if (conjTemplates == null) {
					conjTemplates = Restrictions.conjunction();
				}
				BatchHelper.filterScanTemplate(conjTemplates, tok);

			} else if (tok.startsWith("idin:")) {
				if (conjProcesses == null) {
					conjProcesses = Restrictions.conjunction();
				}
				BatchHelper.filterIds(conjProcesses, tok);

			} else if (tok.startsWith("proc:")) {
				if (conjProcesses == null) {
					conjProcesses = Restrictions.conjunction();
				}
				conjProcesses.add(Restrictions.like("titel", "%" + tok + "%"));

			} else if (tok.startsWith("werk:")) {
				if (conjWorkPiece == null) {
					conjWorkPiece = Restrictions.conjunction();
				}
				BatchHelper.filterWorkpiece(conjWorkPiece, tok);

			} else if (tok.startsWith("-")) {

				if (conjProcesses == null) {
					conjProcesses = Restrictions.conjunction();
				}

				conjProcesses.add(Restrictions.not(Restrictions.like("titel", "%" + tok.substring(1) + "%")));

			} else {

				/* standard-search parameter */
				if (conjProcesses == null) {
					conjProcesses = Restrictions.conjunction();
				}

				conjProcesses.add(IlikeExpression.ilike("titel", "*" + tok + "*", '!'));
			}
		}

		if (conjProcesses != null) {

			critProcess = crit.createCriteria("prozess", "proc");
			// crit.createAlias("proc.ProjekteID", "projID");

			if (conjProcesses != null) {
				// inCrit.add(conjProcesses);
				critProcess.add(conjProcesses);
			}

		}

	
		if (conjTemplates != null) {

			crit.createCriteria("vorlagen", "vorl");
			crit.createAlias("vorl.eigenschaften", "vorleig");
			inCrit.add(conjTemplates);

		}

		if (conjProcessProperties != null) {

			inCrit.createAlias("eigenschaften", "prozesseig");
			inCrit.add(conjProcessProperties);

		}

		if (conjStepProperties != null) {

			Criteria stepCrit = session.createCriteria(Prozess.class);
			stepCrit.createCriteria("schritte", "steps");
			stepCrit.createAlias("steps.eigenschaften", "schritteig");
			stepCrit.add(conjStepProperties);
			stepCrit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
			List<Integer> myIds = new ArrayList<Integer>();

			for (@SuppressWarnings("unchecked")
			Iterator<Prozess> it = stepCrit.setFirstResult(0).setMaxResults(Integer.MAX_VALUE).list().iterator(); it.hasNext();) {
				Prozess p = it.next();
				myIds.add(p.getId());
			}
			crit.add(Restrictions.in("id", myIds));

		}

		if (conjWorkPiece != null) {
			critProcess.createCriteria("werkstuecke", "werk");
			critProcess.createAlias("werk.eigenschaften", "werkeig");
			critProcess.add(conjWorkPiece);

		}
		if (conjUsers != null) {

			critProcess.createCriteria("bearbeitungsbenutzer", "user");
			critProcess.add(conjUsers);

		}
		
		if (conBatchUsers != null) {
			inCrit.add(conBatchUsers);
		}
		
		return message;
	}

//	/**
//	 * 
//	 * @param conjSteps
//	 * @param filterPart
//	 * @return
//	 */
//	private static String createHistoricFilter(Conjunction conjSteps, String filterPart, Boolean stepCriteria) {
//		/* filtering by a certain minimal status */
//		Integer stepReihenfolge;
//		String stepTitle = filterPart.substring(5);
//		// if the criteria is build on steps the table need not be identified
//		String tableIdentifier;
//		if (stepCriteria) {
//			tableIdentifier = "";
//		} else {
//			tableIdentifier = "steps.";
//		}
//		try {
//			stepReihenfolge = Integer.parseInt(stepTitle);
//		} catch (NumberFormatException e) {
//			stepTitle = filterPart.substring(5);
//			if (stepTitle.startsWith("-")) {
//				stepTitle = stepTitle.substring(1);
//				conjSteps.add(Restrictions.and(Restrictions.not(Restrictions.like(tableIdentifier + "titel", "%" + stepTitle + "%")),
//						Restrictions.ge(tableIdentifier + "bearbeitungsstatus", StepStatus.OPEN.getValue())));
//				return "";
//			} else {
//				conjSteps.add(Restrictions.and(Restrictions.like(tableIdentifier + "titel", "%" + stepTitle + "%"),
//						Restrictions.ge(tableIdentifier + "bearbeitungsstatus", StepStatus.OPEN.getValue())));
//				return "";
//			}
//		}
//		conjSteps.add(Restrictions.and(Restrictions.eq(tableIdentifier + "reihenfolge", stepReihenfolge),
//				Restrictions.ge(tableIdentifier + "bearbeitungsstatus", StepStatus.OPEN.getValue())));
//		return "";
//	}


}
