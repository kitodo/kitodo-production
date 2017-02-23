/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the GPL3-License.txt file that was
 * distributed with this source code.
 */

package org.kitodo.api.externaldatamanagement;

/** Determines the possible sources of external Data. Needs to be implemented by an Emun. */
public interface Source {

	/** returns the enum values, is overwritten by enums default 'values()' method */
	public Source[] values();

}
