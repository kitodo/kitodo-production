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

package de.sub.goobi.metadaten;

import org.goobi.api.display.Item;
import ugh.dl.Metadata;

import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;

class MockMetadatum implements Metadatum {

    private String value;


    public MockMetadatum() {
    }

    public MockMetadatum(String value) {
	this.value = value;
    }

    public int getIdentifier() {
	return 0;
    }

    public List<SelectItem> getItems() {
	return null;
    }

    public Metadata getMd() {
	return null;
    }

    public String getOutputType() {
	return null;
    }

    public String getSelectedItem() {
	return null;
    }

    public List<String> getSelectedItems() {
	return null;
    }

    public String getTyp() {
	return null;
    }

    public String getValue() {
	return value;
    }

    public ArrayList<Item> getWert() {
	return null;
    }

    public void setIdentifier(int identifier) {
    }

    public void setItems(List<SelectItem> items) {
    }

    public void setMd(Metadata md) {
    }

    public void setSelectedItem(String selectedItem) {
    }

    public void setSelectedItems(List<String> selectedItems) {
    }

    public void setTyp(String inTyp) {
    }

    public void setValue(String value) {
    }

    public void setWert(String inWert) {
	value = inWert;
    }

}
