/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 *
 * (c) 2014 Goobi. Digitalisieren im Verein e.V. <contact@goobi.org>
 *
 * Visit the websites for more information.
 *     		- http://www.kitodo.org/en/
 *     		- https://github.com/goobi
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * The package PicaPlugin contains the classes required to access a PICA
 * library catalogue system. The main class implementing the plugin is
 * PicaPlugin. It provides the public methods
 *
 *    void    configure(Map) [*]
 *    Object  find(String, long)
 *    String  getDescription() [*]
 *    Map     getHit(Object, long, long)
 *    long    getNumberOfHits(Object, long)
 *    String  getTitle() [*]
 *    void    setPreferences(Prefs)
 *    boolean supportsCatalogue(String)
 *    void    useCatalogue(String)
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