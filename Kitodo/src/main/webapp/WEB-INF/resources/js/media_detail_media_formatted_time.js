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

let mediaElement = document.querySelector('#imagePreviewForm\\:mediaDetailMediaContainer video, #imagePreviewForm\\:mediaDetailMediaContainer audio');
let timerElement = document.querySelector('#imagePreviewForm\\:mediaCurrentFormattedTime');

let formattedTime = document.createElement('div');
formattedTime.setAttribute("id", "mediaFormattedTime");
mediaElement.after(formattedTime);
mediaElement.addEventListener("timeupdate", function () {
    timerElement.innerHTML = metadataEditor.gallery.mediaPartial.convertSecondsToFormattedTime(mediaElement.currentTime);
});
