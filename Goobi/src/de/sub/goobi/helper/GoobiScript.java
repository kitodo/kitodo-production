package de.sub.goobi.helper;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.log4j.Logger;

import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;
import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.beans.Benutzergruppe;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Regelsatz;
import de.sub.goobi.beans.Schritt;
import de.sub.goobi.export.dms.ExportDms;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.helper.tasks.LongRunningTaskManager;
import de.sub.goobi.helper.tasks.ProcessSwapInTask;
import de.sub.goobi.helper.tasks.ProcessSwapOutTask;
import de.sub.goobi.helper.tasks.TiffWriterTask;
import de.sub.goobi.persistence.BenutzerDAO;
import de.sub.goobi.persistence.BenutzergruppenDAO;
import de.sub.goobi.persistence.ProzessDAO;
import de.sub.goobi.persistence.RegelsatzDAO;
import de.sub.goobi.persistence.SchrittDAO;
import de.sub.goobi.persistence.apache.StepManager;
import de.sub.goobi.persistence.apache.StepObject;

//TODO: Delete me, this should be part of the Plugins...
//TODO: Break this up into multiple classes with a common interface
//TODO: add funny observer pattern here for more complexity
//TODO: add some general mechanism for string-output of goobi scripts in jsp

public class GoobiScript {
    HashMap<String, String> myParameters;
    private static final Logger logger = Logger.getLogger(GoobiScript.class);
    public final static String DIRECTORY_SUFFIX = "_tif";

    /**
     * Starten des Scripts ================================================================
     */
    public void execute(List<Prozess> inProzesse, String inScript) {
        this.myParameters = new HashMap<String, String>();
        /*
         * -------------------------------- alle Suchparameter zerlegen und erfassen --------------------------------
         */
        StrTokenizer tokenizer = new StrTokenizer(inScript, ' ', '\"');
        while (tokenizer.hasNext()) {
            String tok = tokenizer.nextToken();
            if (tok.indexOf(":") == -1) {
                Helper.setFehlerMeldung("goobiScriptfield", "missing delimiter / unknown parameter: ", tok);
            } else {
                String myKey = tok.substring(0, tok.indexOf(":"));
                String myValue = tok.substring(tok.indexOf(":") + 1);
                this.myParameters.put(myKey, myValue);
            }
        }

        /*
         * -------------------------------- die passende Methode mit den richtigen Parametern übergeben --------------------------------
         */
        if (this.myParameters.get("action") == null) {
            Helper.setFehlerMeldung(
                    "goobiScriptfield",
                    "missing action",
                    " - possible: 'action:swapsteps, action:adduser, action:addusergroup, action:swapprozessesout, action:swapprozessesin, action:deleteTiffHeaderFile, action:importFromFileSystem'");
            return;
        }

        /*
         * -------------------------------- Aufruf der richtigen Methode über den Parameter --------------------------------
         */
        if (this.myParameters.get("action").equals("swapSteps")) {
            swapSteps(inProzesse);
        } else if (this.myParameters.get("action").equals("swapProzessesOut")) {
            swapOutProzesses(inProzesse);
        } else if (this.myParameters.get("action").equals("swapProzessesIn")) {
            swapInProzesses(inProzesse);
        } else if (this.myParameters.get("action").equals("importFromFileSystem")) {
            importFromFileSystem(inProzesse);
        } else if (this.myParameters.get("action").equals("addUser")) {
            adduser(inProzesse);
        } else if (this.myParameters.get("action").equals("tiffWriter")) {
            writeTiffHeader(inProzesse);
        } else if (this.myParameters.get("action").equals("addUserGroup")) {
            addusergroup(inProzesse);
        } else if (this.myParameters.get("action").equals("setTaskProperty")) {
            setTaskProperty(inProzesse);
        } else if (this.myParameters.get("action").equals("deleteStep")) {
            deleteStep(inProzesse);
        } else if (this.myParameters.get("action").equals("addStep")) {
            addStep(inProzesse);
        } else if (this.myParameters.get("action").equals("setStepNumber")) {
            setStepNumber(inProzesse);
        } else if (this.myParameters.get("action").equals("setStepStatus")) {
            setStepStatus(inProzesse);
        } else if (this.myParameters.get("action").equals("addShellScriptToStep")) {
            addShellScriptToStep(inProzesse);
        } else if (this.myParameters.get("action").equals("addModuleToStep")) {
            addModuleToStep(inProzesse);
        } else if (this.myParameters.get("action").equals("updateImagePath")) {
            updateImagePath(inProzesse);
        } else if (this.myParameters.get("action").equals("updateContentFiles")) {
            updateContentFiles(inProzesse);
        } else if (this.myParameters.get("action").equals("deleteTiffHeaderFile")) {
            deleteTiffHeaderFile(inProzesse);
        } else if (this.myParameters.get("action").equals("setRuleset")) {
            setRuleset(inProzesse);
        } else if (this.myParameters.get("action").equals("exportDms")) {
            exportDms(inProzesse, this.myParameters.get("exportImages"), true);
        } else if (this.myParameters.get("action").equals("export")) {
            exportDms(inProzesse, this.myParameters.get("exportImages"), Boolean.getBoolean(this.myParameters.get("exportOcr")));
        } else if (this.myParameters.get("action").equals("doit")) {
            exportDms(inProzesse, "false", false);
        } else if (this.myParameters.get("action").equals("doit2")) {
            exportDms(inProzesse, "false", true);

        } else if (this.myParameters.get("action").equals("runscript")) {
            String stepname = this.myParameters.get("stepname");
            String scriptname = this.myParameters.get("script");
            if (stepname == null) {
                Helper.setFehlerMeldung("goobiScriptfield", "", "Missing parameter");
            } else {
                runScript(inProzesse, stepname, scriptname);
            }
        } else if (this.myParameters.get("action").equals("deleteProcess")) {
            String value = myParameters.get("contentOnly");
            boolean contentOnly = true;
            if (value != null && value.equalsIgnoreCase("false")) {
                contentOnly = false;
            }
            deleteProcess(inProzesse, contentOnly);
        } else {
            Helper.setFehlerMeldung(
                    "goobiScriptfield",
                    "Unknown action",
                    " - use: 'action:swapsteps, action:adduser, action:addusergroup, action:swapprozessesout, action:swapprozessesin, action:deleteTiffHeaderFile, action:importFromFileSystem'");
            return;
        }

        Helper.setMeldung("goobiScriptfield", "", "GoobiScript finished");
    }

    private void updateContentFiles(List<Prozess> inProzesse) {
        for (Prozess proz : inProzesse) {
            try {
                Fileformat myRdf = proz.readMetadataFile();
                myRdf.getDigitalDocument().addAllContentFiles();
                proz.writeMetadataFile(myRdf);
                Helper.setMeldung("goobiScriptfield", "ContentFiles updated: ", proz.getTitel());
            } catch (ugh.exceptions.DocStructHasNoTypeException e) {
                Helper.setFehlerMeldung("DocStructHasNoTypeException", e.getMessage());

            } catch (Exception e) {
                Helper.setFehlerMeldung("goobiScriptfield", "Error while updating content files", e);
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "updateContentFiles finished");
    }
    
    private void deleteProcess(List<Prozess> inProzesse, boolean contentOnly) {
        ProzessDAO dao = new ProzessDAO();
        for (Prozess p : inProzesse) {
            String title = p.getTitel();
            if (contentOnly) {
                try {
                    File ocr = new File(p.getOcrDirectory());
                    if (ocr.exists()) {
                        Helper.deleteDir(ocr);
                    }
                    File images = new File(p.getImagesDirectory());
                    if (images.exists()) {
                        Helper.deleteDir(images);
                    }
                    Helper.setMeldung("Content deleted for " + title);
                } catch (Exception e) {
                    Helper.setFehlerMeldung("Can not delete content for " + p.getTitel(), e);
                }
            }
            if (!contentOnly) {
                deleteMetadataDirectory(p);
                try {
                    dao.remove(p);
                    Helper.setMeldung("Process " + title + " deleted.");
                } catch (DAOException e) {
                    Helper.setFehlerMeldung("could not delete process " + p.getTitel(), e);
                }
            }
        }
    }

    private void deleteMetadataDirectory(Prozess p) {
        try {
            Helper.deleteDir(new File(p.getProcessDataDirectory()));
            File ocr = new File(p.getOcrDirectory());
            if (ocr.exists()) {
                Helper.deleteDir(ocr);
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung("Can not delete metadata directory", e);
        }
    }

    private void runScript(List<Prozess> inProzesse, String stepname, String scriptname) {
        HelperSchritteWithoutHibernate hs = new HelperSchritteWithoutHibernate();
        for (Prozess p : inProzesse) {
            for (Schritt step : p.getSchritteList()) {
                if (step.getTitel().equalsIgnoreCase(stepname)) {
                    StepObject so = StepManager.getStepById(step.getId());
                    if (scriptname != null) {
                        if (step.getAllScripts().containsKey(scriptname)) {
                            String path = step.getAllScripts().get(scriptname);
                            hs.executeScriptForStepObject(so, path, false);
                        }
                    } else {
                        hs.executeAllScriptsForStep(so, false);
                    }
                }
            }
        }

    }

    /**
     * Prozesse auslagern ================================================================
     */
    private void swapOutProzesses(List<Prozess> inProzesse) {
        for (Prozess p : inProzesse) {

            ProcessSwapOutTask task = new ProcessSwapOutTask();
            task.initialize(p);
            LongRunningTaskManager.getInstance().addTask(task);
            LongRunningTaskManager.getInstance().executeTask(task);

        }
    }

    /**
     * Prozesse wieder einlagern ================================================================
     */
    private void swapInProzesses(List<Prozess> inProzesse) {
        for (Prozess p : inProzesse) {

            ProcessSwapInTask task = new ProcessSwapInTask();
            task.initialize(p);
            LongRunningTaskManager.getInstance().addTask(task);
            LongRunningTaskManager.getInstance().executeTask(task);
        }
    }

    /**
     * voll allen gewählten Prozessen die Daten aus einem Verzeichnis einspielen ================================================================
     */
    private void importFromFileSystem(List<Prozess> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("sourcefolder") == null || this.myParameters.get("sourcefolder").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "missing parameter: ", "sourcefolder");
            return;
        }

        File sourceFolder = new File(this.myParameters.get("sourcefolder"));
        if (!sourceFolder.exists() || !sourceFolder.isDirectory()) {
            Helper.setFehlerMeldung("goobiScriptfield", "Directory " + this.myParameters.get("sourcefolder") + " does not exisist");
            return;
        }
        try {

            for (Prozess p : inProzesse) {
                File imagesFolder = new File(p.getImagesOrigDirectory(false));
                if (imagesFolder.list().length > 0) {
                    Helper.setFehlerMeldung("goobiScriptfield", "", "The process " + p.getTitel() + " [" + p.getId().intValue()
                            + "] has already data in image folder");
                } else {
                    File sourceFolderProzess = new File(sourceFolder, p.getTitel());
                    if (!sourceFolderProzess.exists() || !sourceFolder.isDirectory()) {
                        Helper.setFehlerMeldung("goobiScriptfield", "", "The directory for process " + p.getTitel() + " [" + p.getId().intValue()
                                + "] is not existing");
                    } else {
                        CopyFile.copyDirectory(sourceFolderProzess, imagesFolder);
                        Helper.setMeldung("goobiScriptfield", "", "The directory for process " + p.getTitel() + " [" + p.getId().intValue()
                                + "] is copied");
                    }
                    Helper.setMeldung("goobiScriptfield", "", "The process " + p.getTitel() + " [" + p.getId().intValue() + "] is copied");
                }
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung(e);
            logger.error(e);
        }
    }

    /**
     * Regelsatz setzen ================================================================
     */
    private void setRuleset(List<Prozess> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("ruleset") == null || this.myParameters.get("ruleset").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "ruleset");
            return;
        }

        try {
            RegelsatzDAO rdao = new RegelsatzDAO();
            ProzessDAO pdao = new ProzessDAO();
            List<Regelsatz> rulesets = rdao.search("from Regelsatz where titel='" + this.myParameters.get("ruleset") + "'");
            if (rulesets == null || rulesets.size() == 0) {
                Helper.setFehlerMeldung("goobiScriptfield", "Could not found ruleset: ", "ruleset");
                return;
            }
            Regelsatz regelsatz = rulesets.get(0);

            for (Prozess p : inProzesse) {
                p.setRegelsatz(regelsatz);
                pdao.save(p);
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung(e);
            logger.error(e);
        }
    }

    /**
     * Tauschen zweier Schritte gegeneinander ================================================================
     */
    private void swapSteps(List<Prozess> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("swap1nr") == null || this.myParameters.get("swap1nr").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "swap1nr");
            return;
        }
        if (this.myParameters.get("swap2nr") == null || this.myParameters.get("swap2nr").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "swap2nr");
            return;
        }
        if (this.myParameters.get("swap1title") == null || this.myParameters.get("swap1title").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "swap1title");
            return;
        }
        if (this.myParameters.get("swap2title") == null || this.myParameters.get("swap2title").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "swap2title");
            return;
        }
        int reihenfolge1;
        int reihenfolge2;
        try {
            reihenfolge1 = Integer.parseInt(this.myParameters.get("swap1nr"));
            reihenfolge2 = Integer.parseInt(this.myParameters.get("swap2nr"));
        } catch (NumberFormatException e1) {
            Helper.setFehlerMeldung("goobiScriptfield", "Invalid order number used: ", this.myParameters.get("swap1nr") + " - "
                    + this.myParameters.get("swap2nr"));
            return;
        }

        /*
         * -------------------------------- Durchführung der Action --------------------------------
         */
        SchrittDAO sdao = new SchrittDAO();
        for (Prozess proz : inProzesse) {
            /*
             * -------------------------------- Swapsteps --------------------------------
             */
            Schritt s1 = null;
            Schritt s2 = null;
            for (Iterator<Schritt> iterator = proz.getSchritteList().iterator(); iterator.hasNext();) {
                Schritt s = iterator.next();
                if (s.getTitel().equals(this.myParameters.get("swap1title")) && s.getReihenfolge().intValue() == reihenfolge1) {
                    s1 = s;
                }
                if (s.getTitel().equals(this.myParameters.get("swap2title")) && s.getReihenfolge().intValue() == reihenfolge2) {
                    s2 = s;
                }
            }
            if (s1 != null && s2 != null) {
                StepStatus statustemp = s1.getBearbeitungsstatusEnum();
                s1.setBearbeitungsstatusEnum(s2.getBearbeitungsstatusEnum());
                s2.setBearbeitungsstatusEnum(statustemp);
                s1.setReihenfolge(Integer.valueOf(reihenfolge2));
                s2.setReihenfolge(Integer.valueOf(reihenfolge1));
                try {
                    sdao.save(s1);
                    sdao.save(s2);
                } catch (DAOException e) {
                    Helper.setFehlerMeldung("goobiScriptfield", "Error on save while swapping steps in process: ", proz.getTitel() + " - "
                            + s1.getTitel() + " : " + s2.getTitel());
                    logger.error("Error on save while swapping process: " + proz.getTitel() + " - " + s1.getTitel() + " : " + s2.getTitel(), e);
                }

                Helper.setMeldung("goobiScriptfield", "Swapped steps in: ", proz.getTitel());
            }

        }
        Helper.setMeldung("goobiScriptfield", "swapsteps finished: ");
    }

    /**
     * Schritte löschen ================================================================
     */
    private void deleteStep(List<Prozess> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }

        /*
         * -------------------------------- Durchführung der Action --------------------------------
         */
        ProzessDAO sdao = new ProzessDAO();
        for (Prozess proz : inProzesse) {
            if (proz.getSchritte() != null) {
                for (Iterator<Schritt> iterator = proz.getSchritte().iterator(); iterator.hasNext();) {
                    Schritt s = iterator.next();
                    if (s.getTitel().equals(this.myParameters.get("steptitle"))) {
                        proz.getSchritte().remove(s);
                        try {
                            sdao.save(proz);
                        } catch (DAOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + proz.getTitel(), e);
                            logger.error("goobiScriptfield" + "Error while saving process: " + proz.getTitel(), e);
                        }
                        Helper.setMeldung("goobiScriptfield", "Removed step from process: ", proz.getTitel());
                        break;
                    }
                }
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "deleteStep finished: ");
    }

    /**
     * Schritte hinzufuegen ================================================================
     */
    private void addStep(List<Prozess> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }
        if (this.myParameters.get("number") == null || this.myParameters.get("number").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "number");
            return;
        }

        if (!StringUtils.isNumeric(this.myParameters.get("number"))) {
            Helper.setFehlerMeldung("goobiScriptfield", "Wrong number parameter", "(only numbers allowed)");
            return;
        }

        /*
         * -------------------------------- Durchführung der Action --------------------------------
         */
        ProzessDAO sdao = new ProzessDAO();
        for (Prozess proz : inProzesse) {
            Schritt s = new Schritt();
            s.setTitel(this.myParameters.get("steptitle"));
            s.setReihenfolge(Integer.parseInt(this.myParameters.get("number")));
            s.setProzess(proz);
            if (proz.getSchritte() == null) {
                proz.setSchritte(new HashSet<Schritt>());
            }
            proz.getSchritte().add(s);
            try {
                sdao.save(proz);
            } catch (DAOException e) {
                Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + proz.getTitel(), e);
                logger.error("goobiScriptfield" + "Error while saving process: " + proz.getTitel(), e);
            }
            Helper.setMeldung("goobiScriptfield", "Added step to process: ", proz.getTitel());
        }
        Helper.setMeldung("goobiScriptfield", "", "addStep finished: ");
    }

    /**
     * ShellScript an Schritt hängen ================================================================
     */
    private void addShellScriptToStep(List<Prozess> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Fehlender Parameter: ", "steptitle");
            return;
        }

        if (this.myParameters.get("label") == null || this.myParameters.get("label").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Fehlender Parameter: ", "label");
            return;
        }

        if (this.myParameters.get("script") == null || this.myParameters.get("script").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Fehlender Parameter: ", "script");
            return;
        }

        /*
         * -------------------------------- Durchführung der Action --------------------------------
         */
        ProzessDAO sdao = new ProzessDAO();
        for (Prozess proz : inProzesse) {
            if (proz.getSchritte() != null) {
                for (Iterator<Schritt> iterator = proz.getSchritte().iterator(); iterator.hasNext();) {
                    Schritt s = iterator.next();
                    if (s.getTitel().equals(this.myParameters.get("steptitle"))) {
                        s.setTypAutomatischScriptpfad(this.myParameters.get("script"));
                        s.setScriptname1(this.myParameters.get("label"));
                        s.setTypScriptStep(true);
                        try {
                            sdao.save(proz);
                        } catch (DAOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + proz.getTitel(), e);
                            logger.error("goobiScriptfield" + "Error while saving process: " + proz.getTitel(), e);
                        }
                        Helper.setMeldung("goobiScriptfield", "Added script to step: ", proz.getTitel());
                        break;
                    }
                }
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "addShellScriptToStep finished: ");
    }

    /**
     * ShellScript an Schritt hängen ================================================================
     */
    private void addModuleToStep(List<Prozess> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }

        if (this.myParameters.get("module") == null || this.myParameters.get("module").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "module");
            return;
        }

        /*
         * -------------------------------- Durchführung der Action --------------------------------
         */
        ProzessDAO sdao = new ProzessDAO();
        for (Prozess proz : inProzesse) {
            if (proz.getSchritte() != null) {
                for (Iterator<Schritt> iterator = proz.getSchritte().iterator(); iterator.hasNext();) {
                    Schritt s = iterator.next();
                    if (s.getTitel().equals(this.myParameters.get("steptitle"))) {
                        s.setTypModulName(this.myParameters.get("module"));
                        try {
                            sdao.save(proz);
                        } catch (DAOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + proz.getTitel(), e);
                            logger.error("goobiScriptfield" + "Error while saving process: " + proz.getTitel(), e);
                        }
                        Helper.setMeldung("goobiScriptfield", "Added module to step: ", proz.getTitel());
                        break;
                    }
                }
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "addModuleToStep finished: ");
    }

    /**
     * Flag von Schritten setzen ================================================================
     */
    private void setTaskProperty(List<Prozess> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }

        if (this.myParameters.get("property") == null || this.myParameters.get("property").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "property");
            return;
        }

        if (this.myParameters.get("value") == null || this.myParameters.get("value").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "value");
            return;
        }

        String property = this.myParameters.get("property");
        String value = this.myParameters.get("value");

        if (!property.equals("metadata") && !property.equals("readimages") && !property.equals("writeimages") && !property.equals("validate")
                && !property.equals("exportdms") && !property.equals("batch") && !property.equals("automatic")) {
            Helper.setFehlerMeldung("goobiScriptfield", "",
                    "wrong parameter 'property'; possible values: metadata, readimages, writeimages, validate, exportdms");
            return;
        }

        if (!value.equals("true") && !value.equals("false")) {
            Helper.setFehlerMeldung("goobiScriptfield", "wrong parameter 'value'; possible values: true, false");
            return;
        }

        /*
         * -------------------------------- Durchführung der Action --------------------------------
         */
        ProzessDAO sdao = new ProzessDAO();
        for (Prozess proz : inProzesse) {
            if (proz.getSchritte() != null) {
                for (Iterator<Schritt> iterator = proz.getSchritte().iterator(); iterator.hasNext();) {
                    Schritt s = iterator.next();
                    if (s.getTitel().equals(this.myParameters.get("steptitle"))) {

                        if (property.equals("metadata")) {
                            s.setTypMetadaten(Boolean.parseBoolean(value));
                        }
                        if (property.equals("automatic")) {
                            s.setTypAutomatisch(Boolean.parseBoolean(value));
                        }
                        if (property.equals("batch")) {
                            s.setBatchStep(Boolean.parseBoolean(value));
                        }
                        if (property.equals("readimages")) {
                            s.setTypImagesLesen(Boolean.parseBoolean(value));
                        }
                        if (property.equals("writeimages")) {
                            s.setTypImagesSchreiben(Boolean.parseBoolean(value));
                        }
                        if (property.equals("validate")) {
                            s.setTypBeimAbschliessenVerifizieren(Boolean.parseBoolean(value));
                        }
                        if (property.equals("exportdms")) {
                            s.setTypExportDMS(Boolean.parseBoolean(value));
                        }

                        try {
                            sdao.save(proz);
                        } catch (DAOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + proz.getTitel(), e);
                            logger.error("goobiScriptfield" + "Error while saving process: " + proz.getTitel(), e);
                        }
                        Helper.setMeldung("goobiScriptfield", "Error while saving process: ", proz.getTitel());
                        break;
                    }
                }
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "setTaskProperty abgeschlossen: ");
    }

    /**
     * Schritte auf bestimmten Status setzen ================================================================
     */
    private void setStepStatus(List<Prozess> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }

        if (this.myParameters.get("status") == null || this.myParameters.get("status").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "status");
            return;
        }

        if (!this.myParameters.get("status").equals("0") && !this.myParameters.get("status").equals("1")
                && !this.myParameters.get("status").equals("2") && !this.myParameters.get("status").equals("3")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Wrong status parameter: status ", "(possible: 0=closed, 1=open, 2=in work, 3=finished");
            return;
        }

        /*
         * -------------------------------- Durchführung der Action --------------------------------
         */
        SchrittDAO sdao = new SchrittDAO();
        for (Prozess proz : inProzesse) {
            for (Iterator<Schritt> iterator = proz.getSchritteList().iterator(); iterator.hasNext();) {
                Schritt s = iterator.next();
                if (s.getTitel().equals(this.myParameters.get("steptitle"))) {
                    s.setBearbeitungsstatusAsString(this.myParameters.get("status"));
                    try {
                        sdao.save(s);
                    } catch (DAOException e) {
                        Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + proz.getTitel(), e);
                        logger.error("goobiScriptfield" + "Error while saving process: " + proz.getTitel(), e);
                    }
                    Helper.setMeldung("goobiScriptfield", "stepstatus set in process: ", proz.getTitel());
                    break;
                }
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "setStepStatus finished: ");
    }

    /**
     * Schritte auf bestimmten Reihenfolge setzen ================================================================
     */
    private void setStepNumber(List<Prozess> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }

        if (this.myParameters.get("number") == null || this.myParameters.get("number").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "number");
            return;
        }

        if (!StringUtils.isNumeric(this.myParameters.get("number"))) {
            Helper.setFehlerMeldung("goobiScriptfield", "Wrong number parameter", "(only numbers allowed)");
            return;
        }

        /*
         * -------------------------------- Durchführung der Action --------------------------------
         */
        SchrittDAO sdao = new SchrittDAO();
        for (Prozess proz : inProzesse) {
            for (Iterator<Schritt> iterator = proz.getSchritteList().iterator(); iterator.hasNext();) {
                Schritt s = iterator.next();
                if (s.getTitel().equals(this.myParameters.get("steptitle"))) {
                    s.setReihenfolge(Integer.parseInt(this.myParameters.get("number")));
                    try {
                        sdao.save(s);
                    } catch (DAOException e) {
                        Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + proz.getTitel(), e);
                        logger.error("goobiScriptfield" + "Error while saving process: " + proz.getTitel(), e);
                    }
                    Helper.setMeldung("goobiScriptfield", "step order changed in process: ", proz.getTitel());
                    break;
                }
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "setStepNumber finished ");
    }

    /**
     * Benutzer zu Schritt hinzufügen ================================================================
     */
    private void adduser(List<Prozess> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }
        if (this.myParameters.get("username") == null || this.myParameters.get("username").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "username");
            return;
        }
        /* prüfen, ob ein solcher Benutzer existiert */
        Benutzer myUser = null;
        try {
            List<Benutzer> treffer = new BenutzerDAO().search("from Benutzer where login='" + this.myParameters.get("username") + "'");
            if (treffer != null && treffer.size() > 0) {
                myUser = treffer.get(0);
            } else {
                Helper.setFehlerMeldung("goobiScriptfield", "Unknown user: ", this.myParameters.get("username"));
                return;
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("goobiScriptfield", "Error in GoobiScript.adduser", e);
            logger.error("goobiScriptfield" + "Error in GoobiScript.adduser: ", e);
            return;
        }

        /*
         * -------------------------------- Durchführung der Action --------------------------------
         */
        SchrittDAO sdao = new SchrittDAO();
        for (Prozess proz : inProzesse) {
            for (Iterator<Schritt> iterator = proz.getSchritteList().iterator(); iterator.hasNext();) {
                Schritt s = iterator.next();
                if (s.getTitel().equals(this.myParameters.get("steptitle"))) {
                    Set<Benutzer> myBenutzer = s.getBenutzer();
                    if (myBenutzer == null) {
                        myBenutzer = new HashSet<Benutzer>();
                        s.setBenutzer(myBenutzer);
                    }
                    if (!myBenutzer.contains(myUser)) {
                        myBenutzer.add(myUser);
                        try {
                            sdao.save(s);
                        } catch (DAOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while saving - " + proz.getTitel(), e);
                            logger.error("goobiScriptfield" + "Error while saving - " + proz.getTitel(), e);
                            return;
                        }
                    }
                }
            }
            Helper.setMeldung("goobiScriptfield", "Added user to step: ", proz.getTitel());
        }
        Helper.setMeldung("goobiScriptfield", "", "adduser finished.");
    }

    /**
     * Benutzergruppe zu Schritt hinzufügen ================================================================
     */
    private void addusergroup(List<Prozess> inProzesse) {
        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (this.myParameters.get("steptitle") == null || this.myParameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return;
        }
        if (this.myParameters.get("group") == null || this.myParameters.get("group").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "group");
            return;
        }
        /* prüfen, ob ein solcher Benutzer existiert */
        Benutzergruppe myGroup = null;
        try {
            List<Benutzergruppe> treffer =
                    new BenutzergruppenDAO().search("from Benutzergruppe where titel='" + this.myParameters.get("group") + "'");
            if (treffer != null && treffer.size() > 0) {
                myGroup = treffer.get(0);
            } else {
                Helper.setFehlerMeldung("goobiScriptfield", "Unknown group: ", this.myParameters.get("group"));
                return;
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("goobiScriptfield", "Error in GoobiScript.addusergroup", e);
            return;
        }

        /*
         * -------------------------------- Durchführung der Action --------------------------------
         */
        SchrittDAO sdao = new SchrittDAO();
        for (Prozess proz : inProzesse) {
            for (Iterator<Schritt> iterator = proz.getSchritteList().iterator(); iterator.hasNext();) {
                Schritt s = iterator.next();
                if (s.getTitel().equals(this.myParameters.get("steptitle"))) {
                    Set<Benutzergruppe> myBenutzergruppe = s.getBenutzergruppen();
                    if (myBenutzergruppe == null) {
                        myBenutzergruppe = new HashSet<Benutzergruppe>();
                        s.setBenutzergruppen(myBenutzergruppe);
                    }
                    if (!myBenutzergruppe.contains(myGroup)) {
                        myBenutzergruppe.add(myGroup);
                        try {
                            sdao.save(s);
                        } catch (DAOException e) {
                            Helper.setFehlerMeldung("goobiScriptfield", "Error while saving - " + proz.getTitel(), e);
                            return;
                        }
                    }
                }
            }
            Helper.setMeldung("goobiScriptfield", "added usergroup to step: ", proz.getTitel());
        }
        Helper.setMeldung("goobiScriptfield", "", "addusergroup finished");
    }

    /**
     * TiffHeader von den Prozessen löschen ================================================================
     */
    public void deleteTiffHeaderFile(List<Prozess> inProzesse) {
        for (Prozess proz : inProzesse) {
            try {
                File tiffheaderfile = new File(proz.getImagesDirectory() + "tiffwriter.conf");
                if (tiffheaderfile.exists()) {
                    tiffheaderfile.delete();
                }
                Helper.setMeldung("goobiScriptfield", "TiffHeaderFile deleted: ", proz.getTitel());
            } catch (Exception e) {
                Helper.setFehlerMeldung("goobiScriptfield", "Error while deleting TiffHeader", e);
            }
        }
        Helper.setMeldung("goobiScriptfield", "", "deleteTiffHeaderFile finished");
    }

    /**
     * TiffHeader von den Prozessen neu schreiben ================================================================
     */
    private void writeTiffHeader(List<Prozess> inProzesse) {
        for (Iterator<Prozess> iter = inProzesse.iterator(); iter.hasNext();) {
            Prozess proz = iter.next();
            TiffWriterTask task = new TiffWriterTask();
            task.initialize(proz);
            LongRunningTaskManager.getInstance().addTask(task);
        }
    }

    /**
     * Imagepfad in den Metadaten neu setzen (evtl. vorhandene zunächst löschen) ================================================================
     */
    public void updateImagePath(List<Prozess> inProzesse) {
        for (Prozess proz : inProzesse) {
            try {

                Fileformat myRdf = proz.readMetadataFile();
                UghHelper ughhelp = new UghHelper();
                MetadataType mdt = ughhelp.getMetadataType(proz, "pathimagefiles");
                List<? extends ugh.dl.Metadata> alleImagepfade = myRdf.getDigitalDocument().getPhysicalDocStruct().getAllMetadataByType(mdt);
                if (alleImagepfade.size() > 0) {
                    for (Metadata md : alleImagepfade) {
                        myRdf.getDigitalDocument().getPhysicalDocStruct().getAllMetadata().remove(md);
                    }
                }
                Metadata newmd = new Metadata(mdt);
                if (SystemUtils.IS_OS_WINDOWS) {
                    newmd.setValue("file:/" + proz.getImagesDirectory() + proz.getTitel() + DIRECTORY_SUFFIX);
                } else {
                    newmd.setValue("file://" + proz.getImagesDirectory() + proz.getTitel() + DIRECTORY_SUFFIX);
                }
                myRdf.getDigitalDocument().getPhysicalDocStruct().addMetadata(newmd);
                proz.writeMetadataFile(myRdf);
                Helper.setMeldung("goobiScriptfield", "ImagePath updated: ", proz.getTitel());

            } catch (ugh.exceptions.DocStructHasNoTypeException e) {
                Helper.setFehlerMeldung("DocStructHasNoTypeException", e.getMessage());
            } catch (UghHelperException e) {
                Helper.setFehlerMeldung("UghHelperException", e.getMessage());
            } catch (MetadataTypeNotAllowedException e) {
                Helper.setFehlerMeldung("MetadataTypeNotAllowedException", e.getMessage());

            } catch (Exception e) {
                Helper.setFehlerMeldung("goobiScriptfield", "Error while updating imagepath", e);
            }

        }
        Helper.setMeldung("goobiScriptfield", "", "updateImagePath finished");

    }

    private void exportDms(List<Prozess> processes, String exportImages, boolean exportFulltext) {
        ExportDms dms;
        if (exportImages != null && exportImages.equals("false")) {
            dms = new ExportDms(false);
            dms.setExportFulltext(exportFulltext);
        } else {
            dms = new ExportDms(true);
        }
        for (Prozess prozess : processes) {
            try {
                dms.startExport(prozess);
            } catch (DocStructHasNoTypeException e) {
                logger.error("DocStructHasNoTypeException", e);
            } catch (PreferencesException e) {
                logger.error("PreferencesException", e);
            } catch (WriteException e) {
                logger.error("WriteException", e);
            } catch (MetadataTypeNotAllowedException e) {
                logger.error("MetadataTypeNotAllowedException", e);
            } catch (ReadException e) {
                logger.error("ReadException", e);
            } catch (TypeNotAllowedForParentException e) {
                logger.error("TypeNotAllowedForParentException", e);
            } catch (IOException e) {
                logger.error("IOException", e);
            } catch (InterruptedException e) {
                logger.error("InterruptedException", e);
            } catch (ExportFileException e) {
                logger.error("ExportFileException", e);
            } catch (UghHelperException e) {
                logger.error("UghHelperException", e);
            } catch (SwapException e) {
                logger.error("SwapException", e);
            } catch (DAOException e) {
                logger.error("DAOException", e);
            }
        }
    }
}
