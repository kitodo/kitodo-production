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

Source="$1"
Target="$2"
User="$3"

/bin/ln -vs "$Source" "$Target"
/bin/chown -vR "$User" "$Source" 
