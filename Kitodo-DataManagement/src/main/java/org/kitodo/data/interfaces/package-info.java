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
 * <li>for access management: users ({@link UserInterface}) with roles
 * ({@link RoleInterface}) who work for clients ({@link ClientInterface})
 * <li>for project management: business domain configurations
 * ({@link RulesetInterface}), workflows ({@link WorkflowInterface}), production
 * templates ({@link TemplateInterface}) and runnotes ({@link DocketInterface})
 * <li>for operations: processes ({@link ProcessInterface}) with properties
 * ({@link PropertyInterface}), with their tasks ({@link TaskInterface}), in
 * batches ({@link BatchInterface})
 * </ul>
 * The interface objects are based on the common interface
 * {@link BaseBeanInterface}. The factory methods are provided by the
 * {@link DataFactoryInterface}.
 */
package org.kitodo.data.interfaces;
