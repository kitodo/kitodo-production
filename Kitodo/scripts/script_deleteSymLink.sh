#!/bin/sh
#
# (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
#
# This file is part of the Kitodo project.
#
# It is licensed under GNU General Public License version 3 or later.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
# * exception statement from your version.

#
# Note: Ensure that Tomcat has permission to execute the given commands.
#

Link="$1"

rm -v "$Link"
