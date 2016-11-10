/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.kitodo.org
 *     - https://github.com/goobi/goobi-production
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
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.sub.goobi.metadaten;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.goobi.api.display.Item;

import ugh.dl.Metadata;

public interface Metadatum {
	
	  public abstract ArrayList<Item> getWert();

	      public abstract void setWert(String inWert);

	      public abstract String getTyp();

	      public abstract void setTyp(String inTyp);

	      public abstract int getIdentifier();

	      public abstract void setIdentifier(int identifier);

	      public abstract Metadata getMd();

	      public abstract void setMd(Metadata md);

	      /******************************************************
	       * 
	       * new functions for use of display configuration 
	       * whithin xml files
	       * 
	       *****************************************************/

	      public abstract String getOutputType();

	      public abstract List<SelectItem> getItems();

	      public abstract void setItems(List<SelectItem> items);

	      public abstract List<String> getSelectedItems();

	      public abstract void setSelectedItems(List<String> selectedItems);

	      public abstract String getSelectedItem();

	      public abstract void setSelectedItem(String selectedItem);

	      public abstract void setValue(String value);

	      public abstract String getValue();
}
