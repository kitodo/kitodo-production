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

package org.kitodo.production.helper.metadata;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.MediaPartialView;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.View;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.production.metadata.MetadataEditor;

public class MediaPartialHelper {

    public static final String FORMATTED_TIME_REGEX = "([0-1]\\d|2[0-3]):[0-5]\\d:[0-5]\\d\\.\\d{3}";

    /**
     * Convert formatted time to milliseconds.
     *
     * @param formattedTime
     *         The formatted time in form of {@value #FORMATTED_TIME_REGEX}
     * @return The milliseconds
     */
    public static Long convertFormattedTimeToMilliseconds(String formattedTime) {
        int milliseconds = 0;
        String[] splittedFormattedTime = formattedTime.split("\\.");
        formattedTime = splittedFormattedTime[0];
        milliseconds = Integer.parseInt(splittedFormattedTime[1]);
        String[] time = formattedTime.split(":");
        return (Integer.parseInt(time[0]) * 3600L + Integer.parseInt(time[1]) * 60L + Integer.parseInt(time[2])) * 1000 + milliseconds;
    }

    /**
     * Convert milliseconds to formatted time.
     *
     * @param milliseconds
     *         The milliseconds
     * @return The formatted time
     */
    public static String convertMillisecondsToFormattedTime(Long milliseconds) {
        String formattedMilliseconds = StringUtils.leftPad(
                String.valueOf(TimeUnit.MILLISECONDS.toMillis(milliseconds) % 1000), 3, "0");
        return String.format("%02d:%02d:%02d.%s", TimeUnit.MILLISECONDS.toHours(milliseconds),
                TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60, TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60,
                formattedMilliseconds);
    }

    /**
     * Calculates the extent field of every media partial and sort media partials by begin.
     *
     * @param logicalDivisions
     *         The logical divisions of media partials.
     * @param mediaDuration
     *         The media duration.
     */
    public static void calculateExtentAndSortMediaPartials(List<LogicalDivision> logicalDivisions, Long mediaDuration) {
        calculateExtentForMediaPartials(logicalDivisions, mediaDuration);
        logicalDivisions.sort(getLogicalDivisionComparator());
    }

    /**
     * Calculate the extent of a media partial.
     *
     * <p>
     * Calculates the duration or extent of a media partial until the next one or until the end of the media.
     *
     * @param logicalDivisions
     *         The logical divisions of media partials.
     * @param mediaDuration
     *         The media duration.
     */
    private static void calculateExtentForMediaPartials(List<LogicalDivision> logicalDivisions, Long mediaDuration) {
        // sorting reverse by begin
        logicalDivisions.sort(getLogicalDivisionComparator().reversed());

        ListIterator<LogicalDivision> iterator = logicalDivisions.listIterator();
        LogicalDivision previousLogicalDivision = null;
        while (iterator.hasNext()) {
            LogicalDivision logicalDivision = iterator.next();
            MediaPartialView mediaPartialView = logicalDivision.getViews().getFirst().getPhysicalDivision()
                    .getMediaPartialView();
            if (Objects.nonNull(previousLogicalDivision)) {
                // calculate the duration of media partial to previous media partial
                PhysicalDivision previousPhysicalDivision = previousLogicalDivision.getViews().getFirst()
                        .getPhysicalDivision();
                if (previousPhysicalDivision.hasMediaPartialView()) {
                    mediaPartialView.setExtent(convertMillisecondsToFormattedTime(convertFormattedTimeToMilliseconds(
                            previousPhysicalDivision.getMediaPartialView()
                                    .getBegin()) - convertFormattedTimeToMilliseconds(mediaPartialView.getBegin())));
                }
            } else {
                // calculate the duration of media partial to the end of media
                mediaPartialView.setExtent(convertMillisecondsToFormattedTime(
                        mediaDuration - convertFormattedTimeToMilliseconds(mediaPartialView.getBegin())));
            }
            previousLogicalDivision = logicalDivision;
        }
    }

    /**
     * Add a media partial to the media selection.
     *
     * @param type
     *         Type of the logical division
     * @param title
     *         The title of the media partial
     * @param begin
     *         The begin of the media partial
     * @param mediaSelection
     *         The media selection
     * @param workpiece
     *         The workpiece
     */
    public static void addMediaPartialToMediaSelection(String type, String title, String begin,
            Pair<PhysicalDivision, LogicalDivision> mediaSelection, Workpiece workpiece) {
        LogicalDivision logicalDivision = new LogicalDivision();
        logicalDivision.setType(type);
        logicalDivision.setLabel(title);
        PhysicalDivision physicalDivision = new PhysicalDivision();
        physicalDivision.getMediaFiles().putAll(mediaSelection.getKey().getMediaFiles());
        physicalDivision.setType(PhysicalDivision.TYPE_TRACK);

        MediaPartialView mediaPartialView = new MediaPartialView(begin);
        physicalDivision.setMediaPartialView(mediaPartialView);
        mediaPartialView.setPhysicalDivision(physicalDivision);
        logicalDivision.getViews().add(mediaPartialView);

        physicalDivision.getLogicalDivisions().add(logicalDivision);

        MetadataEditor.getAncestorsOfPhysicalDivision(mediaSelection.getKey(), workpiece.getPhysicalStructure()).getLast()
                .getChildren().add(physicalDivision);

        mediaSelection.getValue().getChildren().add(logicalDivision);
    }

    private static Comparator<LogicalDivision> getLogicalDivisionComparator() {
        return (logicalDivisionA, logicalDivisionB) -> {
            View viewA = logicalDivisionA.getViews().getFirst();
            View viewB = logicalDivisionB.getViews().getFirst();
            if (Objects.nonNull(viewA) && Objects.nonNull(viewB)) {
                PhysicalDivision physicalDivisionA = viewA.getPhysicalDivision();
                PhysicalDivision physicalDivisionB = viewB.getPhysicalDivision();
                if (physicalDivisionA.hasMediaPartialView() && physicalDivisionB.hasMediaPartialView()) {
                    return physicalDivisionA.getMediaPartialView().getBegin()
                            .compareTo(physicalDivisionB.getMediaPartialView().getBegin());
                }
            }
            return Integer.compare(logicalDivisionA.getOrder(), logicalDivisionB.getOrder());
        };
    }

}
