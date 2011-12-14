package de.sub.goobi.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.goobi.production.api.property.xmlbasedprovider.Status;

import de.sub.goobi.beans.property.DisplayPropertyList;
import de.sub.goobi.beans.property.IGoobiEntity;
import de.sub.goobi.beans.property.IGoobiProperty;

public class Vorlage implements Serializable, IGoobiEntity {
   private static final long serialVersionUID = 1736135433162833277L;
   private Integer id;
   private String herkunft;
   private Prozess prozess;
   private Set<Vorlageeigenschaft> eigenschaften;
	private DisplayPropertyList displayProperties;

   private boolean panelAusgeklappt = true;

   public Vorlage() {
      eigenschaften = new HashSet<Vorlageeigenschaft>();
   }

   /*#####################################################
    #####################################################
    ##                                                                                                                          
    ##                                                             Getter und Setter                                   
    ##                                                                                                                 
    #####################################################
    ####################################################*/

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public Prozess getProzess() {
      return prozess;
   }

   public void setProzess(Prozess prozess) {
      this.prozess = prozess;
   }

   public boolean isPanelAusgeklappt() {
      return panelAusgeklappt;
   }

   public void setPanelAusgeklappt(boolean panelAusgeklappt) {
      this.panelAusgeklappt = panelAusgeklappt;
   }

   public Set<Vorlageeigenschaft> getEigenschaften() {
      return eigenschaften;
   }

   public void setEigenschaften(Set<Vorlageeigenschaft> eigenschaften) {
      this.eigenschaften = eigenschaften;
   }
   
   

   /*#####################################################
    #####################################################
    ##																															 
    ##																Helper									
    ##                                                   															    
    #####################################################
    ####################################################*/

   public String getHerkunft() {
      return herkunft;
   }

   public void setHerkunft(String herkunft) {
      this.herkunft = herkunft;
   }

   public int getEigenschaftenSize() {
      if (eigenschaften == null)
         return 0;
      else
         return eigenschaften.size();
   }

   public List<Vorlageeigenschaft> getEigenschaftenList() {
      if (eigenschaften == null)
         return new ArrayList<Vorlageeigenschaft>();
      return new ArrayList<Vorlageeigenschaft>(eigenschaften);
   }
   
	public Status getStatus() {
		return Status.getResourceStatusFromEntity(this);
	}
	
	public List<IGoobiProperty> getProperties() {
		List<IGoobiProperty> returnlist = new ArrayList<IGoobiProperty>();
		returnlist.addAll(getEigenschaftenList());
		return returnlist;
	}	
	public void addProperty(IGoobiProperty toAdd) {
		eigenschaften.add((Vorlageeigenschaft) toAdd);
	}
	
	
	public void removeProperty(IGoobiProperty toRemove) {
		getEigenschaften().remove(toRemove);
		toRemove.setOwningEntity(null);
		
	}
	
	/**
	 * 
	 * @return instance of {@link DisplayPropertyList}
	 */
	public DisplayPropertyList getDisplayProperties() {
		if (displayProperties == null) {
			displayProperties = new DisplayPropertyList(this);
		}
		return displayProperties;
	}
	
	public void refreshProperties() {
		displayProperties = null;
	}
}
