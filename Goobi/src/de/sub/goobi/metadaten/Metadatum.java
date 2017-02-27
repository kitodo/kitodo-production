/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
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
