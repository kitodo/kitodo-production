package de.sub.goobi.Beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Vorlage implements Serializable {
   private static final long serialVersionUID = 1736135433162833277L;
   private Integer id;
   private String herkunft;
   private Prozess prozess;
   private Set<Vorlageeigenschaft> eigenschaften;

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
}
