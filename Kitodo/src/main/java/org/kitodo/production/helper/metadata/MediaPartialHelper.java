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

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.MediaPartial;
import org.kitodo.api.dataformat.MediaVariant;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.View;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.production.metadata.MetadataEditor;

public class MediaPartialHelper {

    public static final Pattern FORMATTED_TIME_PATTERN = Pattern.compile("([0-1]\\d|2[0-3]):[0-5]\\d:[0-5]\\d\\.\\d{3}");

    private static final MediaPartialLogicalDivisionComparator logicalDivisionComparator = new MediaPartialLogicalDivisionComparator();

    /**
     * Convert formatted time to milliseconds.
     *
     * @param formattedTime
     *         The formatted time in form of {@value #FORMATTED_TIME_PATTERN}
     * @return The milliseconds
     */
    public static Long convertFormattedTimeToMilliseconds(String formattedTime) {
        return Duration.between(LocalTime.MIN, LocalTime.parse(formattedTime)).toMillis();
    }

    /**
     * Convert various time input formats to formatted time.
     *
     * @param time
     *         The time input.
     * @return The formatted time
     */
    public static String convertTimeToFormattedTime(String time) {
        if (FORMATTED_TIME_PATTERN.matcher(time).matches()) {
            return time;
        }

        String[] separatedMilliseconds = time.split("\\.");
        Date date;
        try {
            date = DateUtils.parseDate(separatedMilliseconds[0], "HH:mm:ss", "mm:ss", "ss");
            if (separatedMilliseconds.length == 2) {
                String filledMilliseconds = StringUtils.rightPad(separatedMilliseconds[1], 3, "0");
                date = new Date(date.getTime() + Integer.parseInt(filledMilliseconds));
            }
        } catch (ParseException | NumberFormatException e) {
            return time;
        }

        return new SimpleDateFormat("HH:mm:ss.SSS").format(date);
    }

    /**
     * Convert milliseconds to formatted time.
     *
     * @param milliseconds
     *         The milliseconds
     * @return The formatted time
     */
    public static String convertMillisecondsToFormattedTime(Long milliseconds) {
        Duration duration = Duration.ofMillis(milliseconds);
        return String.format("%02d:%02d:%02d.%03d", duration.toHours(), duration.toMinutesPart(),
                duration.toSecondsPart(), duration.toMillisPart());
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
        logicalDivisions.sort(logicalDivisionComparator);
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
        logicalDivisions.sort(logicalDivisionComparator.reversed());

        ListIterator<LogicalDivision> iterator = logicalDivisions.listIterator();
        LogicalDivision previousLogicalDivision = null;
        while (iterator.hasNext()) {
            LogicalDivision logicalDivision = iterator.next();
            if (logicalDivision.getViews().isEmpty()) {
                continue;
            }
            MediaPartial mediaPartial = logicalDivision.getViews().getFirst().getPhysicalDivision()
                    .getMediaPartial();
            if (Objects.nonNull(previousLogicalDivision)) {
                // calculate the duration of media partial to previous media partial
                PhysicalDivision previousPhysicalDivision = previousLogicalDivision.getViews().getFirst()
                        .getPhysicalDivision();
                if (previousPhysicalDivision.hasMediaPartial()) {
                    long previousBegin = convertFormattedTimeToMilliseconds(
                            previousPhysicalDivision.getMediaPartial().getBegin());
                    long currentBegin = convertFormattedTimeToMilliseconds(mediaPartial.getBegin());
                    String extent = convertMillisecondsToFormattedTime(previousBegin - currentBegin);
                    mediaPartial.setExtent(extent);
                }
            } else {
                // calculate the duration of media partial to the end of media
                mediaPartial.setExtent(convertMillisecondsToFormattedTime(
                        mediaDuration - convertFormattedTimeToMilliseconds(mediaPartial.getBegin())));
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

        MediaPartial mediaPartial = new MediaPartial(begin);
        physicalDivision.setMediaPartial(mediaPartial);
        logicalDivision.getViews().add(View.of(physicalDivision));

        physicalDivision.getLogicalDivisions().add(logicalDivision);

        LinkedList<PhysicalDivision> physicalDivisions = MetadataEditor.getAncestorsOfPhysicalDivision(
                mediaSelection.getKey(), workpiece.getPhysicalStructure());
        if (!physicalDivisions.isEmpty()) {
            MetadataEditor.getAncestorsOfPhysicalDivision(mediaSelection.getKey(), workpiece.getPhysicalStructure())
                    .getLast().getChildren().add(physicalDivision);
        }
        mediaSelection.getValue().getChildren().add(logicalDivision);
    }

    /**
     * Add a media partial division to the media partial divisions map.
     *
     * @param mediaPartialDivisions
     *         The media partial divisions
     * @param logicalDivisions
     *         The logical divisions of current selection
     * @param mediaFiles
     *         The media files of current selection
     */
    public static void addMediaPartialDivisions(Map<LogicalDivision, MediaPartial> mediaPartialDivisions,
            List<LogicalDivision> logicalDivisions, Map<MediaVariant, URI> mediaFiles) {
        for (LogicalDivision logicalDivision : logicalDivisions) {
            for (View view : logicalDivision.getViews()) {
                if (PhysicalDivision.TYPE_TRACK.equals(
                        view.getPhysicalDivision().getType()) && view.getPhysicalDivision()
                        .hasMediaPartial() && view.getPhysicalDivision().getMediaFiles().equals(mediaFiles)) {
                    mediaPartialDivisions.put(logicalDivision, view.getPhysicalDivision().getMediaPartial());
                }
            }
            addMediaPartialDivisions(mediaPartialDivisions, logicalDivision.getChildren(), mediaFiles);
        }
    }

}
