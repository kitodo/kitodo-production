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

package org.kitodo;

import java.time.LocalDate;
import java.util.Objects;

import org.kitodo.production.model.bibliography.course.Block;
import org.kitodo.production.model.bibliography.course.Course;
import org.kitodo.production.model.bibliography.course.Issue;

public class NewspaperCourse {
    private static Course course;
    private static Course duplicatedCourse;

    /**
     * Returns a course of appearance.
     *
     * @return a course of appearance
     */
    public static Course getCourse() {
        if (Objects.isNull(course)) {
            course = createCourse();
        }
        return course;
    }

    /**
     * Returns a course of appearance with duplicated Entries.
     *
     * @return a course of appearance
     */
    public static Course getDuplicatedCourse() {
        if (Objects.isNull(duplicatedCourse)) {
            duplicatedCourse = createDuplicatedCourse();
        }
        return duplicatedCourse;
    }

    private static Course createCourse() {
        Course course = new Course();
        addFirstBlock(course);
        addSecondBlock(course);
        return course;
    }

    private static Course createDuplicatedCourse() {
        Course course = new Course();
        addFirstBlockDuplicate(course);
        return course;
    }

    private static void addFirstBlock(Course course) {
        Block block = new Block(course);
        block.setPublicationPeriod(LocalDate.of(1703, 8, 8), LocalDate.of(1703, 9, 30));
        Issue issue = new Issue(course);
        issue.setMonday(true);
        issue.addAddition(LocalDate.of(1703, 8, 8));
        issue.addExclusion(LocalDate.of(1703, 8, 9));
        issue.addExclusion(LocalDate.of(1703, 8, 16));
        issue.addAddition(LocalDate.of(1703, 8, 17));
        issue.addExclusion(LocalDate.of(1703, 8, 30));
        issue.addAddition(LocalDate.of(1703, 8, 31));
        issue.addAddition(LocalDate.of(1703, 9, 6));
        block.addIssue(issue);
        course.add(block);
    }

    private static void addFirstBlockDuplicate(Course course) {
        Block block = new Block(course);
        block.setPublicationPeriod(LocalDate.of(1703, 8, 8), LocalDate.of(1703, 9, 30));
        Issue issue = new Issue(course);
        issue.setMonday(true);
        issue.addAddition(LocalDate.of(1703, 8, 8));
        issue.addExclusion(LocalDate.of(1703, 8, 9));
        issue.addExclusion(LocalDate.of(1703, 8, 16));
        issue.addAddition(LocalDate.of(1703, 8, 17));
        issue.addExclusion(LocalDate.of(1703, 8, 30));
        issue.addAddition(LocalDate.of(1703, 8, 31));
        issue.addAddition(LocalDate.of(1703, 9, 6));
        block.addIssue(issue);
        Issue secondIssue = new Issue(course);
        secondIssue.setMonday(true);
        block.addIssue(secondIssue);
        course.add(block);
    }

    private static void addSecondBlock(Course course) {
        Block block = new Block(course);
        block.setPublicationPeriod(LocalDate.of(1704, 12, 31), LocalDate.of(1705, 1, 30));
        Issue issue = new Issue(course);
        issue.setSaturday(true);
        issue.addAddition(LocalDate.of(1705, 1, 27));
        block.addIssue(issue);
        course.add(block);
    }

}
