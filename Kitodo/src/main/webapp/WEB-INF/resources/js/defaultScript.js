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

$(document).ready(function() {
    $('#loadingScreen').hide();
});

window.updateLogoutCountdown = function(t) {
    let growlMessage = $('#sticky-notifications_container div.ui-growl-message p');
    let currentTime;
    let minutes = Math.floor(t.current / 60);
    let seconds = t.current % 60;
    if (seconds < 10) {
        currentTime = minutes + ":0" + seconds;
    } else {
        currentTime = minutes + ":" + seconds;
    }
    let currentMessage = growlMessage.text();
    if (currentMessage.match(/\d+:\d+/g)) {
        growlMessage.text(currentMessage.replace(/\d+:\d+/g, currentTime));
    } else {
        growlMessage.text(currentMessage + " " + currentTime);
    }
};
