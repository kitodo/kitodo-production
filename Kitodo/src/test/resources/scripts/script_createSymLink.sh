#!/usr/bin/env bash
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

export PATH="/usr/bin:/bin:${PATH}"

Source="$1"
Target="$2"
User="$3"

ln -vs "$Source" "$Target"
chown -vR "$User" "$Source"
