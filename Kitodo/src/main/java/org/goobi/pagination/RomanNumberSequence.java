/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *    - http://gdz.sub.uni-goettingen.de
 *    - http://www.kitodo.org
 *    - https://github.com/goobi/goobi-production
 *
 * Copyright 2011, Center for Retrospective Digitization, Göttingen (GDZ),
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.goobi.pagination;

import java.util.ArrayList;

import ugh.dl.RomanNumeral;

public class RomanNumberSequence extends ArrayList<String> {
	/**
	 * @param start add description
	 * @param end add description
	 */
	public RomanNumberSequence(int start, int end) {

		generateElements(start, end, 1);

	}

	/**
	 * @param start add description
	 * @param end add description
	 * @param increment add description
	 */
	public RomanNumberSequence(int start, int end, int increment) {

		generateElements(start, end, increment);

	}

	private void generateElements(int start, int end, int increment) {
		RomanNumeral r = new RomanNumeral();
		for (int i = start; i <= end; i = (i + increment)) {
			r.setValue(i);
			this.add(r.toString());
		}
	}

}
