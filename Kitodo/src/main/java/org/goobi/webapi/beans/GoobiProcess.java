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

package org.goobi.webapi.beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GoobiProcess {

	private String identifier;

	private String title;

	public GoobiProcess() {
	}

	public GoobiProcess(String identifier, String title) {
		this.identifier = identifier;
		this.title = title;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getTitle() {
		return title;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
