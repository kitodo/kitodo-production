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

/**
 * Implements the functionality of the ruleset.
 *
 * <p>
 * The ruleset is the heart of Production. It describes the elements, that the
 * structure of the digitally represented cultural work can be built from, and
 * in what form metadata for describing these structures can be attached.
 * Technically, this means that it determines which input masks the web
 * interface displays.
 * 
 * <p>
 * This is a fairly complex process and happens in the following way:
 * Essentially, an auxiliary table is set up. This is not a table in the
 * database but a list of objects of the class {@link AuxiliaryTableRow}. The
 * whole thing exists only in memory as long as a view is used. The view is
 * built in the following steps:
 * <ul>
 * <li>First, see if there is a restriction for the current division in the
 * correlation section. If so, the metadata keys named in it are added to the
 * table in the order given. Regardless of whether unspecified keys are
 * unrestricted or forbidden, for the keys explicitly named in the restriction
 * rule, the restriction rule also specifies their display order.
 * <li>Then it depends. If unspecified is unrestricted, or if there is no
 * restriction rule, the remaining (if there is no rule, all) metadata keys are
 * written to a second list.
 * <li>Now the editing settings are considered. There are global and specific
 * ones that only apply to an acquisition stage. That is, it checks to see if
 * there is an acquisition stage, and the specific settings are merged with the
 * general ones, so that the specifics apply before the general ones. Isn’t a
 * general setting either, the standard applies.
 * <li>From here it becomes specific, that is, the rest depends on the metadata
 * objects and fields to be added. Now the transferred metadata objects are
 * assigned to the rows of the two tables. If it turns out that metadata
 * objects exist for which there is no key in the ruleset (a mis-matching
 * ruleset, for example, because the ruleset was changed), “undefined” metadata
 * keys are generated for the nonexistent types. These will be added to the
 * second list.
 * <li>The members of the second list are sorted by label in the preferred
 * language specified by the user. Then the second list is appended to the first
 * one.
 * <li>Now the final field list is generated from the auxiliary table. That
 * depends on several factors now. If there is a metadata object but the key
 * has been defined as excluded, the object will be returned in a line without a
 * key. If there is no metadata object, a row is only generated if it has been
 * added or is always showing. If there are metadata objects, a line is created
 * for each one. And then, if requested, another empty line. Exceptions are
 * multiple selection lists, here only one line is generated, which contains all
 * objects. The whole thing works recursively for complex metadata keys and
 * element lists of selection types.
 * </ul>
 *
 * @author Matthias Ronge
 */
package org.kitodo.dataeditor.ruleset;
