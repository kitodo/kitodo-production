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
 * Contains a plug-in required to access a PICA library catalogue system.
 * The main class implementing the plug-in is PicaPlugin. It provides the
 * following public methods which are specified in
 * {@link org.goobi.production.plugin.UnspecificPlugin}:
 *
 * <pre>   void    configure(Map)
 *   String  getDescription()
 *   String  getTitle()</pre>
 *
 * and in {@link org.goobi.production.plugin.CataloguePlugin.CataloguePlugin}:
 *
 * <pre>   Object  find(String, long)
 *   Map     getHit(Object, long, long)
 *   long    getNumberOfHits(Object, long)
 *   void    setPreferences(Prefs)
 *   boolean supportsCatalogue(String)
 *   void    useCatalogue(String)</pre>
 *
 * Most of the code originates form the package
 * <kbd>de.unigoettingen.sub.search.opac</kbd> created by “Ludwig” and maybe
 * others in 2005. To compile, the plug-in requires the libraries from
 * /Goobi/WEB-INF/lib and the Tomcat server runtime libraries on the class path.
 *
 * @author Matthias Ronge
 */
package org.goobi.production.plugin.CataloguePlugin.PicaPlugin;
