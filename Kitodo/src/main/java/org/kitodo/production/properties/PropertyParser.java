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

package org.kitodo.production.properties;

import de.sub.kitodo.helper.Helper;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;

public class PropertyParser {
    private static final Logger logger = Logger.getLogger(PropertyParser.class);

    /**
     * Get properties for task.
     *
     * @param mySchritt
     *            Task object
     * @return list of ProcessProperty objects
     */
    public static ArrayList<ProcessProperty> getPropertiesForStep(Task mySchritt) {
        Hibernate.initialize(mySchritt.getProcess());
        Hibernate.initialize(mySchritt.getProcess().getProject());
        String stepTitle = mySchritt.getTitle();
        String projectTitle = mySchritt.getProcess().getProject().getTitle();
        ArrayList<ProcessProperty> properties = new ArrayList<ProcessProperty>();

        if (mySchritt.getProcess().isTemplate()) {
            return properties;
        }

        String path = new Helper().getKitodoConfigDirectory() + "kitodo_processProperties.xml";
        XMLConfiguration config;
        try {
            config = new XMLConfiguration(path);
        } catch (ConfigurationException e) {
            logger.error(e);
            config = new XMLConfiguration();
        }
        config.setListDelimiter('&');
        config.setReloadingStrategy(new FileChangedReloadingStrategy());

        // run though all properties
        int countProperties = config.getMaxIndex("property");
        for (int i = 0; i <= countProperties; i++) {

            // general values for property
            ProcessProperty pp = new ProcessProperty();
            pp.setName(config.getString("property(" + i + ")[@name]"));
            pp.setContainer(config.getInt("property(" + i + ")[@container]"));

            // projects
            int count = config.getMaxIndex("property(" + i + ").project");
            for (int j = 0; j <= count; j++) {
                pp.getProjects().add(config.getString("property(" + i + ").project(" + j + ")"));
            }

            // project is configured
            if (pp.getProjects().contains("*") || pp.getProjects().contains(projectTitle)) {

                // showStep
                boolean containsCurrentStepTitle = false;
                count = config.getMaxIndex("property(" + i + ").showStep");
                for (int j = 0; j <= count; j++) {
                    ShowStepCondition ssc = new ShowStepCondition();
                    ssc.setName(config.getString("property(" + i + ").showStep(" + j + ")[@name]"));
                    String access = config.getString("property(" + i + ").showStep(" + j + ")[@access]");
                    boolean duplicate = config.getBoolean("property(" + i + ").showStep(" + j + ")[@duplicate]", false);
                    ssc.setAccessCondition(AccessCondition.getAccessConditionByName(access));
                    if (ssc.getName().equals(stepTitle)) {
                        containsCurrentStepTitle = true;
                        pp.setDuplicationAllowed(duplicate);
                        pp.setCurrentStepAccessCondition(AccessCondition.getAccessConditionByName(access));
                    }

                    pp.getShowStepConditions().add(ssc);
                }

                // steptitle is configured
                if (containsCurrentStepTitle) {
                    // showProcessGroupAccessCondition
                    String groupAccess = config.getString("property(" + i + ").showProcessGroup[@access]");
                    if (groupAccess != null) {
                        pp.setShowProcessGroupAccessCondition(AccessCondition.getAccessConditionByName(groupAccess));
                    } else {
                        pp.setShowProcessGroupAccessCondition(AccessCondition.WRITE);
                    }

                    // validation expression
                    pp.setValidation(config.getString("property(" + i + ").validation"));
                    // type
                    pp.setType(Type.getTypeByName(config.getString("property(" + i + ").type")));
                    // (default) value
                    pp.setValue(config.getString("property(" + i + ").defaultvalue"));

                    // possible values
                    count = config.getMaxIndex("property(" + i + ").value");
                    for (int j = 0; j <= count; j++) {
                        pp.getPossibleValues().add(config.getString("property(" + i + ").value(" + j + ")"));
                    }
                    properties.add(pp);
                }
            }
        }

        // add existing 'eigenschaften' to properties from config, so we have
        // all properties from config and
        // some of them with already existing 'eigenschaften'
        ArrayList<ProcessProperty> listClone = new ArrayList<ProcessProperty>(properties);
        List<org.kitodo.data.database.beans.ProcessProperty> plist = mySchritt.getProcess().getProperties();
        for (org.kitodo.data.database.beans.ProcessProperty pe : plist) {

            for (ProcessProperty pp : listClone) {
                // TODO added temporarily a fix for NPE. Properties without
                // title shouldn't exist at all
                if (pe.getTitle() != null) {

                    if (pe.getTitle().equals(pp.getName())) {
                        // pp has no pe assigned
                        if (pp.getProzesseigenschaft() == null) {
                            pp.setProzesseigenschaft(pe);
                            pp.setValue(pe.getValue());
                            pp.setContainer(pe.getContainer());
                        } else {
                            // clone pp
                            ProcessProperty pnew = pp.getClone(pe.getContainer());
                            pnew.setProzesseigenschaft(pe);
                            pnew.setValue(pe.getValue());
                            pnew.setContainer(pe.getContainer());
                            properties.add(pnew);
                        }
                    }
                }
            }
        }
        return properties;
    }

    /**
     * Get properties for process.
     *
     * @param process
     *            object
     * @return ProcessProperty object
     */
    public static ArrayList<ProcessProperty> getPropertiesForProcess(Process process) {
        Hibernate.initialize(process.getProject());
        String projectTitle = process.getProject().getTitle();
        ArrayList<ProcessProperty> properties = new ArrayList<ProcessProperty>();
        if (process.isTemplate()) {
            List<org.kitodo.data.database.beans.ProcessProperty> plist = process.getProperties();
            for (org.kitodo.data.database.beans.ProcessProperty pe : plist) {
                ProcessProperty pp = new ProcessProperty();
                pp.setName(pe.getTitle());
                pp.setProzesseigenschaft(pe);
                pp.setType(Type.TEXT);
                pp.setValue(pe.getValue());
                pp.setContainer(pe.getContainer());
                properties.add(pp);
            }
            return properties;
        }
        String path = new Helper().getKitodoConfigDirectory() + "kitodo_processProperties.xml";
        XMLConfiguration config;
        try {
            config = new XMLConfiguration(path);
        } catch (ConfigurationException e) {
            logger.error(e);
            config = new XMLConfiguration();
        }
        config.setListDelimiter('&');
        config.setReloadingStrategy(new FileChangedReloadingStrategy());

        // run though all properties
        int countProperties = config.getMaxIndex("property");
        for (int i = 0; i <= countProperties; i++) {

            // general values for property
            ProcessProperty pp = new ProcessProperty();
            pp.setName(config.getString("property(" + i + ")[@name]"));
            pp.setContainer(config.getInt("property(" + i + ")[@container]"));

            // projects
            int count = config.getMaxIndex("property(" + i + ").project");
            for (int j = 0; j <= count; j++) {
                pp.getProjects().add(config.getString("property(" + i + ").project(" + j + ")"));
            }

            // project is configured
            if (pp.getProjects().contains("*") || pp.getProjects().contains(projectTitle)) {

                // validation expression
                pp.setValidation(config.getString("property(" + i + ").validation"));
                // type
                pp.setType(Type.getTypeByName(config.getString("property(" + i + ").type")));
                // (default) value
                pp.setValue(config.getString("property(" + i + ").defaultvalue"));

                // possible values
                count = config.getMaxIndex("property(" + i + ").value");
                for (int j = 0; j <= count; j++) {
                    pp.getPossibleValues().add(config.getString("property(" + i + ").value(" + j + ")"));
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("add property A " + pp.getName() + " - " + pp.getValue() + " - " + pp.getContainer());
                }
                properties.add(pp);

            }
        } // add existing 'eigenschaften' to properties from config, so we have
          // all properties from config and some
          // of them with already existing 'eigenschaften'
        List<ProcessProperty> listClone = new ArrayList<ProcessProperty>(properties);
        List<org.kitodo.data.database.beans.ProcessProperty> plist = process.getProperties();
        for (org.kitodo.data.database.beans.ProcessProperty pe : plist) {

            // TODO added temporarily a fix for NPE. Properties without title
            // shouldn't exist at all
            if (pe.getTitle() != null) {

                for (ProcessProperty pp : listClone) {
                    if (pe.getTitle().equals(pp.getName())) {
                        // pp has no pe assigned
                        if (pp.getProzesseigenschaft() == null) {
                            pp.setProzesseigenschaft(pe);
                            pp.setValue(pe.getValue());
                            pp.setContainer(pe.getContainer());
                        } else {
                            // clone pp
                            ProcessProperty pnew = pp.getClone(pe.getContainer());
                            pnew.setProzesseigenschaft(pe);
                            pnew.setValue(pe.getValue());
                            pnew.setContainer(pe.getContainer());
                            if (logger.isDebugEnabled()) {
                                logger.debug("add property B " + pp.getName() + " - " + pp.getValue() + " - "
                                        + pp.getContainer());
                            }
                            properties.add(pnew);
                        }
                    }
                }
            }
        }

        // add 'eigenschaft' to all ProcessProperties
        for (ProcessProperty pp : properties) {
            if (pp.getProzesseigenschaft() == null) {
            } else {
                plist.remove(pp.getProzesseigenschaft());
            }
        }
        // create ProcessProperties to remaining 'eigenschaften'
        if (plist.size() > 0) {
            for (org.kitodo.data.database.beans.ProcessProperty pe : plist) {
                ProcessProperty pp = new ProcessProperty();
                pp.setProzesseigenschaft(pe);
                pp.setName(pe.getTitle());
                pp.setValue(pe.getValue());
                pp.setContainer(pe.getContainer());
                pp.setType(Type.TEXT);
                if (logger.isDebugEnabled()) {
                    logger.debug("add property C " + pp.getName() + " - " + pp.getValue() + " - " + pp.getContainer());
                }
                properties.add(pp);

            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("all properties are " + properties.size());
        }

        return properties;
    }
}
