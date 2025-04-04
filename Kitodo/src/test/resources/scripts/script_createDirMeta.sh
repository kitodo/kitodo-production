#!/bin/sh
#
# (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
#
# This file is part of the Kitodo project.
#
# It is licensed under GNU General Public License version 3 or later.
#
# For the full copyright and license information, please read the
# GPL3-License.txt file that was distributed with this source code.
#

#
# Note: Ensure that Tomcat has permission to execute the given commands.
#

Directory="$1"

/bin/mkdir -vm 0775 "$Directory"
