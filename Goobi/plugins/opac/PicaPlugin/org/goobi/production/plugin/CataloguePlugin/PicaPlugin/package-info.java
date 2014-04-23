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
 * search.opac</kbd>. The plugin classes can be compiled with the libraries
 * from /Goobi/WEB-INF/lib and the Tomcat server runtime libraries on the
 * classpath. 
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
package org.goobi.production.plugin.CataloguePlugin.PicaPlugin;