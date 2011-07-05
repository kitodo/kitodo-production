package de.sub.goobi.Beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Werkstueck implements Serializable {
   private static final long serialVersionUID = 123266825187246791L;
   private Integer id;
   private Prozess prozess;
   private Set<Werkstueckeigenschaft> eigenschaften;

   private boolean panelAusgeklappt = true;

   public Werkstueck() {
      eigenschaften = new HashSet<Werkstueckeigenschaft>();
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

   public Set<Werkstueckeigenschaft> getEigenschaften() {
      return eigenschaften;
   }

   public void setEigenschaften(Set<Werkstueckeigenschaft> eigenschaften) {
      this.eigenschaften = eigenschaften;
   }

   /*#####################################################
    #####################################################
    ##																															 
    ##																Helper									
    ##                                                   															    
    #####################################################
    ####################################################*/

   public int getEigenschaftenSize() {
      if (eigenschaften == null)
         return 0;
      else
         return eigenschaften.size();
   }

   public List<Werkstueckeigenschaft> getEigenschaftenList() {
      if (eigenschaften == null)
         return new ArrayList<Werkstueckeigenschaft>();
      return new ArrayList<Werkstueckeigenschaft>(eigenschaften);
   }
}
