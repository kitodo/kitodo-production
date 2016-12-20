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
 * The package ModsPlugin contains the classes required to access the Kalliope
 * MODS catalogue system. The main class implementing the plugin is ModsPlugin.
 * It provides the public methods
 *
 *  void    configure(Map) [*]
 *  Object  find(String, long)
 *  String  getDescription() [*]
 *  Map     getHit(Object, long, long)
 *  long    getNumberOfHits(Object, long)
 *  String  getTitle() [*]
 *  void    setPreferences(Prefs)
 *  boolean supportsCatalogue(String)
 *  void    useCatalogue(String)
 *
 * as specified by org.goobi.production.plugin.UnspecificPlugin [*] and
 * org.goobi.production.plugin.CataloguePlugin.CataloguePlugin.
 *
 * Most of the code originates form the package <kbd>de.unigoettingen.sub.
 * search.opac</kbd> created by “Ludwig” and maybe others in 2005. The plugin
 * classes can be compiled with the libraries from /Goobi/WEB-INF/lib and the
 * Tomcat server runtime libraries on the classpath.
 *
 * @author Arved Solth, Christopher Timm
 */
package org.kitodo.production.plugin.CataloguePlugin.ModsPlugin;
