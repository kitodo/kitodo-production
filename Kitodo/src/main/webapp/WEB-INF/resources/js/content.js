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

function setContentHeight() {
    var panelWrapper = $('.ui-tabs-panels');
    panelWrapper.height(window.innerHeight - panelWrapper.offset().top - $('footer').height());
}

window.addEventListener('load', setContentHeight);
window.addEventListener('resize', setContentHeight);
