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

package org.goobi.pagination;

import java.util.ArrayList;

public class IntegerSequence extends ArrayList<Integer> {

	public IntegerSequence() {
	}

	/**
	 * @param start add description
	 * @param end add description
	 */
	public IntegerSequence(int start, int end) {

		generateElements(start, end, 1);

	}

	/**
	 * @param start add description
	 * @param end add description
	 * @param increment add description
	 */
	public IntegerSequence(int start, int end, int increment) {

		generateElements(start, end, increment);

	}

	private void generateElements(int start, int end, int increment) {

		if (start > end) {
			throw new IllegalArgumentException("Sequence end value cannot be smaller than start value.");
		}

		this.ensureCapacity(end - start);

		for (int i = start; i <= end; i = (i + increment)) {
			this.add(Integer.valueOf(i));
		}
	}

}
