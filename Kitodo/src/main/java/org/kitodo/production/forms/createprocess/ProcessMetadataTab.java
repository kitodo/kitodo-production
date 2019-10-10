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

package org.kitodo.production.forms.createprocess;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.ComplexMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ProcessMetadataTab {
    private static final Logger logger = LogManager.getLogger(ProcessMetadataTab.class);
    private static final String PERSON = "Person";
    private static final String ROLE = "Role";
    private static final String AUTHOR = "Author";
    private static final String FIRST_NAME = "FirstName";
    private static final String LAST_NAME = "LastName";


    private CreateProcessForm createProcessForm;

    private ProcessFieldedMetadata processDetails = new ProcessFieldedMetadata();

    public ProcessMetadataTab(CreateProcessForm createProcessForm) {
        this.createProcessForm = createProcessForm;
    }

    /**
     * initialize process details table.
     * @param structure
     *          which its Metadaten are wanted to be shown
     */
    public void initializeProcessDetails(IncludedStructuralElement structure) {
        StructuralElementViewInterface divisionView = this.createProcessForm.getRuleset().getStructuralElementView(
                createProcessForm.getProcessDataTab().getDocType(),
                this.createProcessForm.getAcquisitionStage(),
                this.createProcessForm.getPriorityList());
        processDetails = new ProcessFieldedMetadata(this, structure, divisionView);
    }

    /**
     * Set processDetails.
     * @param processDetails
     *          as ProcessFieldedMetadata
     */
    public void setProcessDetails(ProcessFieldedMetadata processDetails) {
        this.processDetails = processDetails;
    }


    /**
     * Get all details in the processDetails as a list.
     *
     * @return the list of details of the processDetails
     */
    public List<ProcessDetail> getProcessDetailsElements() {
        return processDetails.getRows();
    }

    /**
     * preserve all the metadata in the processDetails.
     */
    public void preserve() {
        try {
            processDetails.preserve();
        } catch (NoSuchMetadataFieldException | InvalidMetadataValueException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    /**
     * Add a new process detail to processDetails.
     * @param metadataId
     *              the key of the metadata want to be added
     * @return the added process detail as a ProcessDetail.
     *
     * @throws NoSuchMetadataFieldException
     *          if the metadataId is not allowed as a Metadata for the doctype.
     */
    private ProcessDetail addProcessDetail(String metadataId) throws NoSuchMetadataFieldException {
        Collection<MetadataViewInterface> docTypeAddableDivisions = this.createProcessForm.getRuleset().getStructuralElementView(
               this.createProcessForm.getProcessDataTab().getDocType(),
                this.createProcessForm.getAcquisitionStage(),
                this.createProcessForm.getPriorityList()).getAddableMetadata(Collections.emptyMap(), Collections.emptyList());

        List<MetadataViewInterface> filteredViews = docTypeAddableDivisions
               .stream()
               .filter(metadataView -> metadataView.getId().equals(metadataId))
               .collect(Collectors.toList());

        if (!filteredViews.isEmpty()) {
            if (filteredViews.get(0).isComplex()) {
                return processDetails.createMetadataGroupPanel((ComplexMetadataViewInterface) filteredViews.get(0),
                        Collections.emptyList());
            } else {
                return processDetails.createMetadataEntryEdit((SimpleMetadataViewInterface) filteredViews.get(0),
                        Collections.emptyList());

            }
        }
        throw new NoSuchMetadataFieldException(metadataId, "");
    }

    void fillProcessDetailsElements(List<ProcessDetail> processDetailList, NodeList nodes, boolean isChild) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            Element element = (Element) node;
            String nodeName = element.getAttribute("name");
            boolean filled = false;
            for (ProcessDetail detail : processDetailList) {
                if (Objects.nonNull(detail.getMetadataID()) && detail.getMetadataID().equals(nodeName)) {
                    if (isChild || getProcessDetailValue(detail).isEmpty()) {
                        filled = true;
                        if (node.getLocalName().equals("metadataGroup")
                                && detail instanceof ProcessFieldedMetadata) {
                            fillProcessDetailsElements(((ProcessFieldedMetadata) detail).getRows(),
                                    element.getChildNodes(), true);
                        } else if (node.getLocalName().equals("metadata")) {
                            setProcessDetailValue(detail, element.getTextContent());
                        }
                    }
                    break;
                }
            }
            if (!filled) {

                try {
                    ProcessDetail newDetail = addProcessDetail(nodeName);
                    this.processDetails.getRows().add(newDetail);
                    if (newDetail instanceof ProcessFieldedMetadata) {
                        fillProcessDetailsElements(((ProcessFieldedMetadata) newDetail).getRows(), element.getChildNodes(), true);
                    } else {
                        setProcessDetailValue(newDetail, element.getTextContent());
                    }
                } catch (NoSuchMetadataFieldException e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }

    /**
     * Set the value of a specific process detail in processDetails.
     * @param processDetail the specific process detail that its value want to be modified
     *      as ProcessDetail
     * @param value
     *       as a java.lang.String
     * @return the modified processDetail
     *      as a ProcessDetail
     */
    public static ProcessDetail setProcessDetailValue(ProcessDetail processDetail, String value) {
        if (processDetail instanceof ProcessTextMetadata) {
            // TODO: incorporate "initstart" and "initend" values from kitodo_projects.xml like AddtionalField!
            ((ProcessTextMetadata) processDetail).setValue(value);
        } else if (processDetail instanceof ProcessBooleanMetadata) {
            ((ProcessBooleanMetadata) processDetail).setActive(Boolean.parseBoolean(value));
        } else if (processDetail instanceof ProcessSelectMetadata) {
            ((ProcessSelectMetadata) processDetail).setSelectedItem(value);
        }
        return processDetail;
    }

    /**
     *  Get the value of a specific processDetail in the processDetails.
     * @param processDetail
     *      as ProcessDetail
     * @return the value as a java.lang.String
     */
    public static String getProcessDetailValue(ProcessDetail processDetail) {
        String value = "";
        if (processDetail instanceof ProcessTextMetadata) {
            return ((ProcessTextMetadata) processDetail).getValue();
        } else if (processDetail instanceof ProcessBooleanMetadata) {
            return String.valueOf(((ProcessBooleanMetadata) processDetail).isActive());
        } else if (processDetail instanceof ProcessSelectMetadata) {
            return String.join(", ", ((ProcessSelectMetadata) processDetail).getSelectedItems());
        } else if (processDetail instanceof ProcessFieldedMetadata && processDetail.getMetadataID().equals(PERSON)) {
            value = getCreator(((ProcessFieldedMetadata) processDetail).getRows());
        }
        return value;
    }

    /**
     * get all creators names .
     * @param processDetailsList the list of elements in processDetails
     *      as a list of processDetail
     * @return all creators names as a String
     */
    public static String getListOfCreators(List<ProcessDetail> processDetailsList) {
        String listofAuthors = "";
        for (ProcessDetail detail : processDetailsList) {
            if (detail instanceof ProcessFieldedMetadata
                    && PERSON.equals(detail.getMetadataID())) {
                ProcessFieldedMetadata tableRow = (ProcessFieldedMetadata) detail;
                for (ProcessDetail detailsTableRow : tableRow.getRows()) {
                    if (ROLE.equals(detailsTableRow.getMetadataID())
                            && AUTHOR.equals(ProcessMetadataTab.getProcessDetailValue(detailsTableRow))) {
                        listofAuthors = listofAuthors.concat(getCreator(tableRow.getRows()));
                        break;
                    }
                }
            }
        }
        return listofAuthors;
    }

    private static String getCreator(List<ProcessDetail> processDetailList) {
        String author = "";
        for (ProcessDetail detail : processDetailList) {
            if (FIRST_NAME.equals(detail.getMetadataID())
                    || LAST_NAME.equals(detail.getMetadataID())) {
                author = author.concat(ProcessMetadataTab.getProcessDetailValue(detail));
            }
        }
        return author;
    }
}
