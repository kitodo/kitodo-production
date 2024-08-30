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
 * Provides an interface for some particularly central data objects of the
 * application. Data objects from the database or third-party sources can be
 * used agnostically in the implementation via the interface. These are:
 * <ul>
 * <li>for access management: users ({@link User}) with roles
 * ({@link Role}) who work for clients ({@link Client})
 * <li>for project management: business domain configurations
 * ({@link Ruleset}), workflows ({@link Workflow}), production
 * templates ({@link Template}) and runnotes ({@link Docket})
 * <li>for operations: processes ({@link Process}) with properties
 * ({@link Property}), with their tasks ({@link Task}), in
 * batches ({@link Batch})
 * </ul>
 * The interface objects are based on the common interface
 * {@link BaseBean}. The factory methods are provided by the
 * {@link DataFactoryInterface}.
 */
package org.kitodo.data.interfaces;
