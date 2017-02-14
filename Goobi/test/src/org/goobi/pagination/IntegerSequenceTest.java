///*
// * This file is part of the Goobi Application - a Workflow tool for the support of
// * mass digitization.
// *
// * Visit the websites for more information.
// *    - http://gdz.sub.uni-goettingen.de
// *    - http://www.goobi.org
// *    - http://launchpad.net/goobi-production
// *
// * Copyright 2011, Center for Retrospective Digitization, Göttingen (GDZ),
// *
// * This program is free software; you can redistribute it and/or modify it under
// * the terms of the GNU General Public License as published by the Free Software
// * Foundation; either version 2 of the License, or (at your option) any later
// * version.
// *
// * This program is distributed in the hope that it will be useful, but WITHOUT ANY
// * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
// * PARTICULAR PURPOSE. See the GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//
//// 
//package org.goobi.pagination;
//
//
//// TODO diese Klasse wieder reaktivieren, wenn Code übernommen wurde oder löschen wenn nicht
//
//import org.junit.Test;
//
//import static junit.framework.Assert.assertEquals;
//import static junit.framework.Assert.assertTrue;
//
//public class IntegerSequenceTest {
//
//	@Test
//	public void sequenceWithoutRangeIsEmpty() {
//
//		IntegerSequence seq = new IntegerSequence();
//		assertTrue(seq.isEmpty());
//
//	}
//
//	@Test(expected=IllegalArgumentException.class)
//	public void throwIllegalArgumentExceptionWhenStartIsBelowEnd() {
//
//		IntegerSequence seq = new IntegerSequence(5, 1);
//
//	}
//
//	@Test
//	public void sequenceFromOneToFiveHasFiveElements() {
//
//		IntegerSequence seq = new IntegerSequence(1, 5);
//		assertEquals(5, seq.size());
//
//	}
//
//	@Test
//	public void sequenceIsStrictlyIncreasing() {
//
//		IntegerSequence seq = new IntegerSequence(1, 5);
//
//		int last = 0;
//		for (int i : seq) {
//			assertTrue(i > last);
//			last = i;
//		}
//
//	}
//
//	@Test
//	public void differenceBetweenElementsEqualsIncrement() {
//
//		IntegerSequence seq = new IntegerSequence(1, 5, 2);
//
//		int last = -1;
//		for (int i : seq) {
//			assertTrue("Difference between elements should be equal to increment (2)", (last+2) == i);
//			last = i;
//		}
//
//
//	}
//
//}
