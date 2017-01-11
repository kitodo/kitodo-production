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
