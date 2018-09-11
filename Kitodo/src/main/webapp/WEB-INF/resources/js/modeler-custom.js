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

$(window).on("load", function() {
    $.ready.then(function() {
        if ($('#editForm\\:workflowTabView\\:btnReadXmlDiagram').length > 0) {
            $('#editForm\\:workflowTabView\\:btnReadXmlDiagram')[0].click();
        } else {
            $('#editForm\\:workflowTabView\\:js-create-diagram')[0].click();
        }
    });
});
