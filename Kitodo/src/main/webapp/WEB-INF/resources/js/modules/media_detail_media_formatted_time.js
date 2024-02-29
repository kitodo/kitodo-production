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

let initMediaFormattedTime = function () {
    let mediaElement = document.querySelector('#imagePreviewForm\\:mediaDetailMediaContainer video, #imagePreviewForm\\:mediaDetailMediaContainer audio');

    if( mediaElement ) {
        if(mediaElement.tagName === 'VIDEO') {
            mediaElement.style.maxWidth = '700px';
        }

        let formattedTime = document.createElement('div');
        formattedTime.setAttribute("id", "mediaFormattedTime");
        mediaElement.after(formattedTime);

        formattedTime.innerHTML = metadataEditor.gallery.mediaPartial.convertSecondsToFormattedTime(mediaElement.currentTime);
        mediaElement.addEventListener("timeupdate", function () {
            formattedTime.innerHTML = metadataEditor.gallery.mediaPartial.convertSecondsToFormattedTime(mediaElement.currentTime);
        });

        const jumpButtons = document.getElementsByClassName("media-formatted-time-jump-button");
        Array.from(jumpButtons).forEach(function (jumpButton) {
            jumpButton.addEventListener('click', function () {
                mediaElement.pause();
                let jumpMilliseconds = parseInt(this.getAttribute("data-media-formatted-time-jump-milliseconds"));
                mediaElement.currentTime = ((mediaElement.currentTime * 1000) + jumpMilliseconds) / 1000;
                formattedTime.innerHTML = metadataEditor.gallery.mediaPartial.convertSecondsToFormattedTime(mediaElement.currentTime);
            });
        });
    }
};

initMediaFormattedTime();

document.addEventListener("kitodo-metadataditor-mediaview-update", initMediaFormattedTime);
