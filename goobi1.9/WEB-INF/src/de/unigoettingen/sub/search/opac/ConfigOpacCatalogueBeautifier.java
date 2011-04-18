package de.unigoettingen.sub.search.opac;
import java.util.ArrayList;

/**
 * die OpacBeautifier dienen zur Manipulation des Ergebnisses, was 
 * als Treffer einer Opacabfrage zurückgegeben wird. Dabei soll die 
 * Eigenschaft eines Wertes gesetzt werden, wenn bestimmte Werte in 
 * dem opac-Ergebnis auftreten. 
 * ================================================================*/
public class ConfigOpacCatalogueBeautifier {
 private  ConfigOpacCatalogueBeautifierElement tagElementToChange;
   private ArrayList<ConfigOpacCatalogueBeautifierElement> tagElementsToProof;
   
   public ConfigOpacCatalogueBeautifier(ConfigOpacCatalogueBeautifierElement inChangeElement, ArrayList<ConfigOpacCatalogueBeautifierElement> inProofElements) {
      tagElementToChange = inChangeElement;
      tagElementsToProof = inProofElements;
   }

   public ConfigOpacCatalogueBeautifierElement getTagElementToChange() {
      return tagElementToChange;
   }

   public ArrayList<ConfigOpacCatalogueBeautifierElement> getTagElementsToProof() {
      return tagElementsToProof;
   }
   

}
