package org.goobi.production.plugin.interfaces;

import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import de.unigoettingen.sub.search.opac.ConfigOpacCatalogue;
import de.unigoettingen.sub.search.opac.ConfigOpacDoctype;

public interface IOpacPlugin extends IPlugin {


    public Fileformat search(String inSuchfeld, String inSuchbegriff, ConfigOpacCatalogue coc, Prefs inPrefs)
            throws Exception;

    public int getHitcount();

    public String getAtstsl();

    public ConfigOpacDoctype getOpacDocType();

}