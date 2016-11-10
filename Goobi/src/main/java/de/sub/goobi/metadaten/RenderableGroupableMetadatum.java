/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2014 Goobi. Digitalisieren im Verein e. V. <contact@goobi.org>
 * 
 * Visit the websites for more information.
 *     		- http://www.kitodo.org/en/
 *     		- https://github.com/goobi
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination. As a special
 * exception, the copyright holders of this library give you permission to link
 * this library with independent modules to produce an executable, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is not
 * derived from or based on this library. If you modify this library, you may
 * extend this exception to your version of the library, but you are not obliged
 * to do so. If you do not wish to do so, delete this exception statement from
 * your version.
 */
package de.sub.goobi.metadaten;

import java.util.List;

import ugh.dl.Metadata;

/**
 * A RenderableGroupableMetadatum is a metadatum which can—but doesn’t have to
 * be—a member of a RenderableMetadataGroup. A RenderableGroupableMetadatum can
 * be a RenderablePersonMetadataGroup—which is a special case of a
 * RenderableMetadataGroup—but must not be a RenderableMetadataGroup.
 * 
 * Java interfaces are always public and this interface holds the public methods
 * that are accessed by JSF during rendering. Other methods with a more
 * restricted visibility cannot be defined here. They will be defined in the
 * abstract class {@link RenderableGroupableMetadatum}.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
interface RenderableGroupableMetadatum {

	/**
	 * Shall add the data passed from the metadata element as content to the
	 * element.
	 * 
	 * @param data
	 *            data to add
	 */
	void addContent(Metadata data);

	/**
	 * Shall return the label for the metadatum in the language previously set.
	 * 
	 * @return the label for the metadatum
	 */
	String getLabel();

	/**
	 * Shall return true if the element is contained in a group and is the first
	 * element in its members list, false otherwise.
	 * 
	 * @return if the element is the first in its list
	 */
	boolean isFirst();

	/**
	 * Shall return whether the user shall be depredated the permission to edit
	 * the value(s) on the screen.
	 * 
	 * @return whether the component shall be read-only
	 */
	boolean isReadonly();

	/**
	 * Shall return the metadata elements contained in this display element
	 * backing bean.
	 * 
	 * @return the metadata elements contained in this bean
	 */
	List<? extends Metadata> toMetadata();
}
