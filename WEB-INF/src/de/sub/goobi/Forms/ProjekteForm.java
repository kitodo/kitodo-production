package de.sub.goobi.Forms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

import de.sub.goobi.Beans.ProjectFileGroup;
import de.sub.goobi.Beans.Projekt;
import de.sub.goobi.Persistence.ProjektDAO;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Page;
import de.sub.goobi.helper.exceptions.DAOException;

public class ProjekteForm extends BasisForm {
	private static final long serialVersionUID = 6735912903249358786L;
	private Projekt myProjekt = new Projekt();
	private ProjectFileGroup myFilegroup;
	private ProjektDAO dao = new ProjektDAO();

	//lists accepting the preliminary actions of adding and delting filegroups
	//it needs the execution of commit fileGroups to make these changes permanent
	private List<Integer> newFileGroups = new ArrayList<Integer>();
	private List<Integer> deletedFileGroups = new ArrayList<Integer>();

	public ProjekteForm() {
		super();
	}

	//making sure its cleaned up
	public void finalize() {
		this.Cancel();
	}

	/**
	 * this method deletes filegroups by their id's in the list
	 * 
	 * @param List
	 *            <Integer> fileGroups
	 */
	private void deleteFileGroups(List<Integer> fileGroups) {
		for (Integer id : fileGroups) {
			for (ProjectFileGroup f : this.myProjekt.getFilegroupsList()) {
				if (f.getId() == id) {
					this.myProjekt.getFilegroups().remove(f);
					break;
				}
			}
		}
	}

	/**
	 * this method flushes the newFileGroups List, thus makes them permanent and
	 * deletes those marked for deleting, making the removal permanent
	 */
	private void commitFileGroups() {
		//resetting the List of new fileGroups
		newFileGroups = new ArrayList<Integer>();
		//deleting the fileGroups marked for deletion
		deleteFileGroups(deletedFileGroups);
		//resetting the List of fileGroups marked for deletion
		deletedFileGroups = new ArrayList<Integer>();
	}

	/**
	 * this needs to be executed in order to rollback adding of filegroups
	 * 
	 * @return
	 */
	public String Cancel() {
		//flushing new fileGroups
		deleteFileGroups(newFileGroups);
		//resetting the List of new fileGroups
		newFileGroups = new ArrayList<Integer>();
		//resetting the List of fileGroups marked for deletion
		deletedFileGroups = new ArrayList<Integer>();
		return "ProjekteAlle";
	}

	public String Neu() {
		myProjekt = new Projekt();
		return "ProjekteBearbeiten";
	}

	public String Speichern() {
		//call this to make saving and deleting permanent
		this.commitFileGroups();
		try {
			dao.save(myProjekt);
			return "ProjekteAlle";
		} catch (DAOException e) {
			new Helper().setFehlerMeldung("fehlerNichtSpeicherbar", e.getMessage());
			e.printStackTrace();
			return "";
		}
	}

	public String Loeschen() {
		try {
			dao.remove(myProjekt);
		} catch (DAOException e) {
			new Helper().setFehlerMeldung("fehlerNichtLoeschbar", e.getMessage());
			return "";
		}
		return "ProjekteAlle";
	}

	public String FilterKein() {
		try {
			Session session = Helper.getHibernateSession();
//			session.flush();
			session.clear();
//			session = Helper.getHibernateSession();
			Criteria crit = session.createCriteria(Projekt.class);
			crit.addOrder(Order.asc("titel"));
			page = new Page(crit, 0);
		} catch (HibernateException he) {
			new Helper().setFehlerMeldung("fehlerBeimEinlesen", he.getMessage());
			return "";
		}
		return "ProjekteAlle";
	}

	public String FilterKeinMitZurueck() {
		FilterKein();
		return zurueck;
	}

	public String filegroupAdd() {
		myFilegroup = new ProjectFileGroup();
		myFilegroup.setProject(myProjekt);
		newFileGroups.add(myFilegroup.getId());
		return zurueck;
	}

	public String filegroupSave() {
		if (myProjekt.getFilegroups() == null)
			myProjekt.setFilegroups(new HashSet<ProjectFileGroup>());
		if (!myProjekt.getFilegroups().contains(myFilegroup))
			myProjekt.getFilegroups().add(myFilegroup);

		return "jeniaClosePopupFrameWithAction";
	}

	public String filegroupEdit() {
		return zurueck;
	}

	public String filegroupDelete() {
		//to be deleted fileGroups ids are listed 
		// and deleted after a commit		
		deletedFileGroups.add(myFilegroup.getId());
		// original line
		//myProjekt.getFilegroups().remove(myFilegroup);
		return "ProjekteBearbeiten";
	}

	/*#####################################################
	 #####################################################
	 ##                                                                                              
	 ##                                                Getter und Setter                         
	 ##                                                                                                    
	 #####################################################
	 ####################################################*/

	public Projekt getMyProjekt() {
		return myProjekt;
	}

	public void setMyProjekt(Projekt inProjekt) {
		// has to be called if a page back move was done
		this.Cancel();
		this.myProjekt = inProjekt;
	}

	/**
	 * The need to commit deleted fileGroups only after the save action requires
	 * a filter, so that those filegroups marked for delete are not shown
	 * anymore
	 * 
	 * @return modified ArrayList
	 */
	public ArrayList<ProjectFileGroup> getFileGroupList() {
		ArrayList<ProjectFileGroup> filteredFileGroupList = new ArrayList<ProjectFileGroup>(myProjekt.getFilegroupsList());

		for (Integer id : deletedFileGroups) {
			for (ProjectFileGroup f : this.myProjekt.getFilegroupsList()) {
				if (f.getId() == id) {
					filteredFileGroupList.remove(f);
					break;
				}
			}
		}
		return filteredFileGroupList;
	}

	public ProjectFileGroup getMyFilegroup() {
		return myFilegroup;
	}

	public void setMyFilegroup(ProjectFileGroup myFilegroup) {
		this.myFilegroup = myFilegroup;
	}

}
