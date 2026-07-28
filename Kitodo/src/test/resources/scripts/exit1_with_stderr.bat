::
:: (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
::
:: This file is part of the Kitodo project.
::
:: It is licensed under GNU General Public License version 3 or later.
::
:: For the full copyright and license information, please read the
:: GPL3-License.txt file that was distributed with this source code.
::
@ECHO OFF

ECHO Test StdOut output
1>&2 ECHO Test StdErr output
EXIT /B 1
