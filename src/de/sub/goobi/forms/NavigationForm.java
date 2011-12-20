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

package de.sub.goobi.forms;

import de.sub.goobi.config.ConfigMain;


public class NavigationForm {
	private String aktuell = "0";

	public String getAktuell() {
		return aktuell;
	}

	public void setAktuell(String aktuell) {
		this.aktuell = aktuell;
	}	
	
	public String Reload(){
		return "";
	}
	
	public String JeniaPopupCloseAction(){       
//       Helper help = new Helper();
//       BenutzerverwaltungForm bvf = (BenutzerverwaltungForm) Helper.getManagedBeanValue("#{BenutzerverwaltungForm}");
//		bvf.getMyClass().setStandort("die ID lautet: " + help.getRequestParameter("ID"));
//        try {
//         bvf.setMyClass(new BenutzerDAO().get(bvf.getMyClass().getId()));
//      } catch (DAOException e) {
//         e.printStackTrace();
//      }
       return "jeniaClosePopupFrameWithAction";
	}    
    
    public String BenutzerBearbeiten(){
        return "BenutzerBearbeiten";
    }
    
    /**
     * 
     * @return true if show_modulemanager in file GoobiConfig.properties is =true
     */
    public Boolean getShowModuleManager(){
    	return ConfigMain.getBooleanParameter("show_modulemanager");
    }
    
    /**
     * 
     * @return true if show_taskmanager in file GoobiConfig.properties is =true
     */
    public Boolean getShowTaskManager(){
    	return ConfigMain.getBooleanParameter("show_taskmanager");	
    }
    
    
    
}
