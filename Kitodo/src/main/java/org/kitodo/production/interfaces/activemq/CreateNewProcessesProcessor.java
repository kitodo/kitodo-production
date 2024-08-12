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

package org.kitodo.production.interfaces.activemq;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;

import javax.jms.JMSException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.FunctionalMetadata;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoRecordFoundException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.exceptions.ProcessorException;
import org.kitodo.exceptions.UnsupportedFormatException;
import org.kitodo.production.forms.createprocess.ProcessFieldedMetadata;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.ProcessHelper;
import org.kitodo.production.helper.TempProcess;
import org.kitodo.production.process.ProcessGenerator;
import org.kitodo.production.process.ProcessValidator;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ImportService;
import org.kitodo.production.services.data.RulesetService;
import org.xml.sax.SAXException;

/**
 * An Active MQ service interface to create new processes.
 */
public class CreateNewProcessesProcessor extends ActiveMQProcessor {
    private static final Logger logger = LogManager.getLogger(CreateNewProcessesProcessor.class);

    private static final String ACQUISITION_STAGE = "create";
    private static final int IMPORT_DEPTH = 1;
    private static final List<LanguageRange> METADATA_LANGUAGE = Locale.LanguageRange.parse("en");

    private final ImportService importService = ServiceManager.getImportService();
    private final RulesetService rulesetService = ServiceManager.getRulesetService();

    private RulesetManagementInterface rulesetManagement;

    /**
     * The default constructor looks up the queue name to use in
     * kitodo_config.properties. If that is not configured and “null” is passed
     * to the super constructor, this will prevent
     * ActiveMQDirector.registerListeners() from starting this service.
     */
    public CreateNewProcessesProcessor() {
        super(ConfigCore.getOptionalString(ParameterCore.ACTIVE_MQ_CREATE_NEW_PROCESSES_QUEUE).orElse(null));
    }

    /*
     * The main routine processing incoming tickets.
     */
    @Override
    protected void process(MapMessageObjectReader ticket) throws ProcessorException, JMSException {
        try {
            CreateNewProcessOrder order = new CreateNewProcessOrder(ticket);
            rulesetManagement = rulesetService.openRuleset(order.getTemplate().getRuleset());
            TempProcess tempProcess;
            if (!order.getImports().isEmpty()) {
                tempProcess = importProcess(order, 0);
                tempProcess.getWorkpiece().getLogicalStructure().getMetadata().addAll(order.getMetadata());
                tempProcess.verifyDocType();
                for (int which = 1; which < order.getImports().size(); which++) {
                    rulesetManagement.updateMetadata(tempProcess.getWorkpiece().getLogicalStructure().getType(),
                        tempProcess.getWorkpiece().getLogicalStructure().getMetadata(), ACQUISITION_STAGE,
                        importProcess(order, which).getWorkpiece().getLogicalStructure().getMetadata());
                }
            } else {
                ProcessGenerator processGenerator = new ProcessGenerator();
                processGenerator.generateProcess(order.getTemplateId(), order.getProjectId());
                tempProcess = new TempProcess(processGenerator.getGeneratedProcess(), new Workpiece());
                tempProcess.getWorkpiece().getLogicalStructure().getMetadata().addAll(order.getMetadata());
                tempProcess.verifyDocType();
            }
            ProcessFieldedMetadata processDetails = ProcessHelper.initializeProcessDetails(tempProcess.getWorkpiece()
                    .getLogicalStructure(), rulesetManagement, ACQUISITION_STAGE, METADATA_LANGUAGE);
            ProcessHelper.generateAtstslFields(tempProcess, processDetails.getRows(), Collections.emptyList(),
                tempProcess.getProcess().getBaseType(), rulesetManagement, ACQUISITION_STAGE, METADATA_LANGUAGE, order
                        .getParent(), true);
            if (order.getTitle().isPresent()) {
                tempProcess.getProcess().setTitle(order.getTitle().get());
            }
            if (!ProcessValidator.isProcessTitleCorrect(tempProcess.getProcess().getTitle())) {
                throw new ProcessorException(Helper.getTranslation("processTitleAlreadyInUse", tempProcess.getProcess()
                        .getTitle()));
            }
        } catch (DataException | DAOException | InvalidMetadataValueException | IOException | NoRecordFoundException
                | NoSuchMetadataFieldException | ParserConfigurationException | ProcessGenerationException
                | SAXException | TransformerException | UnsupportedFormatException | URISyntaxException
                | XPathExpressionException e) {
            throw new ProcessorException(e.getMessage());
        }
    }

    /**
     * Imports a dataset with an import configuration.
     * 
     * @param order
     *            order for creating the process
     * @param which
     *            which dataset should be imported
     * @return the imported dataset
     */
    private TempProcess importProcess(CreateNewProcessOrder order, int which) throws DAOException,
            InvalidMetadataValueException, IOException, NoRecordFoundException, NoSuchMetadataFieldException,
            ParserConfigurationException, ProcessGenerationException, ProcessorException, SAXException,
            TransformerException, UnsupportedFormatException, URISyntaxException, XPathExpressionException {

        List<TempProcess> processHierarchy = importService.importProcessHierarchy(order.getImports().get(which)
                .getValue(), order.getImports().get(which).getKey(), order.getProjectId(), order.getTemplateId(),
            IMPORT_DEPTH, rulesetManagement.getFunctionalKeys(FunctionalMetadata.HIGHERLEVEL_IDENTIFIER));
        if (processHierarchy.size() == 0) {
            throw new ProcessorException("Process was not imported");
        } else if (processHierarchy.size() > 1) {
            throw new ProcessorException(processHierarchy.size() + " processes were imported");
        }
        return processHierarchy.get(0);
    }
}
