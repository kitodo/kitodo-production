package de.sub.goobi.Forms;

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
