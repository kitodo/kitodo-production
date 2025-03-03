/**
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */
/* globals metadataEditor */

let initMediaPartial = function () {
    let mediaElement = document.querySelector('#imagePreviewForm\\:mediaDetailMediaContainer video, #imagePreviewForm\\:mediaDetailMediaContainer audio');
    let mediaPartial = document.querySelector('#metadataEditorWrapper .columnHeading.mediaPartialHeading');
    if (mediaElement && mediaPartial) {
        let durationTime = metadataEditor.gallery.mediaPartial.convertFormattedTimeToMilliseconds(mediaPartial.dataset.mediaPartialDuration);
        let startTime = metadataEditor.gallery.mediaPartial.convertFormattedTimeToMilliseconds(mediaPartial.dataset.mediaPartialStart);
        let stopTime = (startTime + durationTime) / 1000;

        mediaElement.addEventListener("timeupdate", function () {
            if (mediaElement.currentTime >= stopTime) {
                mediaElement.currentTime = stopTime;
                mediaElement.pause();
            }
        });

        let onCanPlay = function () {
            mediaElement.currentTime = startTime / 1000;
        };

        mediaElement.addEventListener("play", function () {
            mediaElement.removeEventListener("canplay", onCanPlay);
            mediaElement.currentTime = startTime / 1000;
        });

        mediaElement.addEventListener("canplay", onCanPlay);
    }
};

initMediaPartial();

document.addEventListener("kitodo-metadataditor-mediaview-update", initMediaPartial);
