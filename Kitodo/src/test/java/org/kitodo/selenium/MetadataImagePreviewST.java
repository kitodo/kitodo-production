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

package org.kitodo.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.FileStructureValidationException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.selenium.testframework.BaseTestSelenium;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.test.utils.ProcessTestUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.xml.sax.SAXException;

/**
 * Tests the image preview panel (OpenLayers map) in the metadata editor.
 */
public class MetadataImagePreviewST extends BaseTestSelenium {

    private static final String TEST_RENAME_MEDIA_FILE = "testRenameMediaMeta.xml";
    private static final String PROCESS_TITLE = "Detail View"; 
    private static final String OPEN_LAYERS_CANVAS_SELECTOR = "#imagePreviewForm .ol-layer canvas";
    private static final String OPEN_LAYERS_ZOOM_IN_SELECTOR = "#imagePreviewForm .ol-zoom-in";
    private static final String OPEN_LAYERS_ZOOM_OUT_SELECTOR = "#imagePreviewForm .ol-zoom-out";
    private static final String OPEN_LAYERS_ZOOM_RESET_SELECTOR = "#imagePreviewForm .reset-zoom button";
    private static final String OPEN_LAYERS_ROTATE_LEFT_SELECTOR = "#imagePreviewForm .rotate-left button";
    private static final String OPEN_LAYERS_ROTATE_RIGHT_SELECTOR = "#imagePreviewForm .rotate-right button";
    private static final String OPEN_LAYERS_ROTATE_NORTH_SELECTOR = "#imagePreviewForm .rotate-north button";
    private static final String GALLERY_HEADING_WRAPPER_SELECTOR = "#galleryHeadingWrapper span";
    private static final String SECOND_THUMBNAIL_SELECTOR = 
        "#imagePreviewForm\\:thumbnailWrapper > div:nth-child(2) .thumbnail-container";
    private static final Double EPSILON = 0.001;

    private static int processId = -1;

    /**
     * Prepare tests by inserting dummy processes into database and index for sub-folders of test metadata resources.
     * @throws DAOException when saving of dummy or test processes fails.
     * @throws IOException when copying test metadata or image files fails.
     */
    @BeforeAll
    public static void prepare() throws DAOException, IOException {
        MockDatabase.insertFoldersForSecondProject();
        processId = MockDatabase.insertTestProcessIntoSecondProject(PROCESS_TITLE);
        ProcessTestUtils.copyTestFiles(processId, TEST_RENAME_MEDIA_FILE);
    }

    /**
     * Tests whether the image preview is shown when a user clicks on the image preview button.
     * @throws Exception when something fails
     */
    @Test
    public void imageVisibleTest() throws Exception {
        login("kowal");
        Pages.getProcessesPage().goTo().editMetadata(PROCESS_TITLE);

        // check detail view is not yet visible
        assertEquals(0, findElementsByCSS(OPEN_LAYERS_CANVAS_SELECTOR).size());

        // open detail view
        Pages.getMetadataEditorPage().openDetailView();
        
        // check it is visible now
        pollAssertTrue(() -> findElementsByCSS(OPEN_LAYERS_CANVAS_SELECTOR).getFirst().isDisplayed());
    }

    /**
     * Tests whether the zoom buttons of the image preview (zoom in, out, reset) work as intended.
     * @throws Exception when something fails
     */
    @Test 
    public void zoomLevelTest() throws Exception {
        login("kowal");

        // open detail view and wait for openlayers canvas
        Pages.getProcessesPage().goTo().editMetadata(PROCESS_TITLE);
        Pages.getMetadataEditorPage().openDetailView();
        pollAssertTrue(() -> findElementsByCSS(OPEN_LAYERS_CANVAS_SELECTOR).getFirst().isDisplayed());

        // remember initial zoom
        Double initialZoom = getOpenLayersZoom();
        assertTrue(initialZoom > 0);

        // zoom in, and check zoom increases
        findElementsByCSS(OPEN_LAYERS_ZOOM_IN_SELECTOR).getFirst().click();
        pollAssertTrue(() -> !isOpenLayersAnimating());
        assertTrue(() -> getOpenLayersZoom() > initialZoom);

        // zoom out, and check zoom returns to initial zoom level
        findElementsByCSS(OPEN_LAYERS_ZOOM_OUT_SELECTOR).getFirst().click();
        pollAssertTrue(() -> !isOpenLayersAnimating());
        assertTrue(() -> Math.abs(getOpenLayersZoom() - initialZoom) < EPSILON);

        // zoom in, and reset zoom, check zoom returns to initial zoom level
        findElementsByCSS(OPEN_LAYERS_ZOOM_IN_SELECTOR).getFirst().click();
        pollAssertTrue(() -> !isOpenLayersAnimating());
        findElementsByCSS(OPEN_LAYERS_ZOOM_RESET_SELECTOR).getFirst().click();
        pollAssertTrue(() -> !isOpenLayersAnimating());
        assertTrue(() -> Math.abs(getOpenLayersZoom() - initialZoom) < EPSILON);
    }

    /**
     * Tests whether the rotation buttons of the image preview (rotation left, right, north) work as intended.
     * @throws Exception when something fails
     */
    @Test 
    public void rotationTest() throws Exception {
        login("kowal");

        // open detail view and wait for openlayers canvas
        Pages.getProcessesPage().goTo().editMetadata(PROCESS_TITLE);
        Pages.getMetadataEditorPage().openDetailView();
        pollAssertTrue(() -> findElementsByCSS(OPEN_LAYERS_CANVAS_SELECTOR).getFirst().isDisplayed());

        // check initial rotation is zero
        assertTrue(Math.abs(getOpenLayersRotation()) < EPSILON);

        // rotate left and check rotation is decreasing
        findElementsByCSS(OPEN_LAYERS_ROTATE_LEFT_SELECTOR).getFirst().click();
        pollAssertTrue(() -> !isOpenLayersAnimating());
        assertTrue(() -> getOpenLayersRotation() < 0.0);

        // rotate back, and check rotation returns to zero
        findElementsByCSS(OPEN_LAYERS_ROTATE_RIGHT_SELECTOR).getFirst().click();
        pollAssertTrue(() -> !isOpenLayersAnimating());
        assertTrue(() -> Math.abs(getOpenLayersRotation()) < EPSILON);

        // rotate left and reset to north, check rotation returns to zero
        findElementsByCSS(OPEN_LAYERS_ROTATE_LEFT_SELECTOR).getFirst().click();
        pollAssertTrue(() -> !isOpenLayersAnimating());
        findElementsByCSS(OPEN_LAYERS_ROTATE_NORTH_SELECTOR).getFirst().click();
        pollAssertTrue(() -> !isOpenLayersAnimating());
        assertTrue(() -> Math.abs(getOpenLayersRotation()) < EPSILON);
    }

    /**
     * Tests that both zoom level and rotation persists when a user clicks on another image 
     * (which causes OpenLayers to be loaded again).
     * @throws Exception when something fails
     */
    @Test
    public void viewPersistsImageChange() throws Exception {
        login("kowal");

        // open detail view and wait for openlayers canvas
        Pages.getProcessesPage().goTo().editMetadata(PROCESS_TITLE);
        Pages.getMetadataEditorPage().openDetailView();
        pollAssertTrue(() -> findElementsByCSS(OPEN_LAYERS_CANVAS_SELECTOR).getFirst().isDisplayed());

        // remember initial zoom, rotation
        Double initialZoom = getOpenLayersZoom();
        Double initialRotation = getOpenLayersRotation();

        // rotate left and zoom in
        findElementsByCSS(OPEN_LAYERS_ROTATE_LEFT_SELECTOR).getFirst().click();
        findElementsByCSS(OPEN_LAYERS_ZOOM_IN_SELECTOR).getFirst().click();
        pollAssertTrue(() -> !isOpenLayersAnimating());

        // remember changed zoom, rotation
        Double changedZoom = getOpenLayersZoom();
        Double changedRotation = getOpenLayersRotation();

        // verify zoom and rotation was applied
        assertTrue(Math.abs(initialZoom - changedZoom) > 0);
        assertTrue(Math.abs(initialRotation - changedRotation) > 0);

        // change to second image
        findElementsByCSS(SECOND_THUMBNAIL_SELECTOR).getFirst().click();
        
        // wait until second image has been loaded
        pollAssertTrue(
            () -> "Bild 1, Seite -".equals(
                findElementsByCSS(GALLERY_HEADING_WRAPPER_SELECTOR).getFirst().getText().strip()
            )
        );

        // wait until OpenLayers canvas is available
        pollAssertTrue(() -> findElementsByCSS(OPEN_LAYERS_CANVAS_SELECTOR).getFirst().isDisplayed());

        // check that rotation and zoom was correctly applied to next image (and is not reset)
        assertTrue(Math.abs(getOpenLayersZoom() - changedZoom) < EPSILON);
        assertTrue(Math.abs(getOpenLayersRotation() - changedRotation) < EPSILON);
    }

    /**
     * Test that navigation buttons select previous or following image, are disabled in case there is 
     * no previous or next image, and are hidden if the mouse is not inside the image preview panel.
     */
    @Test
    public void navigationButtonTest() throws Exception {
        login("kowal");

        // open metadata editor and detail view
        Pages.getProcessesPage().goTo().editMetadata(PROCESS_TITLE);
        Pages.getMetadataEditorPage().openDetailView();
        pollAssertTrue(() -> findElementsByCSS(OPEN_LAYERS_CANVAS_SELECTOR).getFirst().isDisplayed());
        
        // image is thumbnail 2, which is the very first image, such that left buttons are disabled
        assertEquals("Bild 2, Seite -", findElementsByCSS(GALLERY_HEADING_WRAPPER_SELECTOR).getFirst().getText().strip());
        WebElement leftMany = findElementsByCSS("#imagePreviewForm\\:navigateToPreviousElementMany").getFirst();
        WebElement rightMany = findElementsByCSS("#imagePreviewForm\\:navigateToNextElementMany").getFirst();

        // check left buttons are disabled and right buttons are enabled
        assertFalse(leftMany.isEnabled());
        assertTrue(rightMany.isEnabled());
        
        // click on right-many button, which selects last image
        rightMany.click();

        // wait for image 3 to be shown
        pollAssertTrue(
            () -> "Bild 3, Seite -".equals(
                findElementsByCSS(GALLERY_HEADING_WRAPPER_SELECTOR).getFirst().getText().strip()
            )
        );

        // find buttons again because image preview is re-rendered
        WebElement leftOne = findElementsByCSS("#imagePreviewForm\\:navigateToPreviousElementOne").getFirst();
        WebElement rightOne = findElementsByCSS("#imagePreviewForm\\:navigateToNextElementOne").getFirst();

        // check left buttons are enabled and right buttons are disabled
        assertTrue(leftOne.isEnabled());
        assertFalse(rightOne.isEnabled());

        // click on left-one button, selecting image 1 (middle image of all 3 images)
        leftOne.click();

        // wait for image 1 to be shown
        pollAssertTrue(
            () -> "Bild 1, Seite -".equals(
                findElementsByCSS(GALLERY_HEADING_WRAPPER_SELECTOR).getFirst().getText().strip()
            )
        );

        // find buttons again because image preview is re-rendered
        leftOne = findElementsByCSS("#imagePreviewForm\\:navigateToPreviousElementOne").getFirst();
        rightOne = findElementsByCSS("#imagePreviewForm\\:navigateToNextElementOne").getFirst();

        // both left and right buttons are enabled
        assertTrue(leftOne.isEnabled());
        assertTrue(rightOne.isEnabled());

        // check that buttons are displayed (since mouse in hovering buttons due to prior click)
        assertTrue(leftOne.isDisplayed());
        assertTrue(rightOne.isDisplayed());

        // move mouse to main header menu
        new Actions(Browser.getDriver()).moveToElement(findElementsByCSS("#menu").getFirst()).perform();

        // check buttons are hidden now
        pollAssertTrue(
            () -> !findElementsByCSS("#imagePreviewForm\\:navigateToPreviousElementOne").getFirst().isDisplayed()
        );
    }

    /**
     * Close metadata editor and logout after every test.
     * @throws Exception when page navigation fails
     */
    @AfterEach
    public void closeEditorAndLogout() throws Exception {
        Pages.getMetadataEditorPage().closeEditor();
        Pages.getTopNavigation().logout();
    }

    /**
     * Cleanup test environment by removing temporal dummy processes from database and index.
     * @throws DAOException when dummy process cannot be removed from database
     * @throws IOException when deleting test files fails.
     * @throws SAXException when deleting test files fails.
     */
    @AfterAll
    public static void cleanup() throws DAOException, IOException, SAXException, FileStructureValidationException {
        ProcessService.deleteProcess(processId);
    }

    private void login(String username) throws ReflectiveOperationException, InterruptedException {
        User metadataUser = ServiceManager.getUserService().getByLogin(username);
        Pages.getLoginPage().goTo().performLogin(metadataUser);
    }

    private List<WebElement> findElementsByCSS(String css) {
        return Browser.getDriver().findElements(By.cssSelector(css));
    }

    private Boolean isOpenLayersAnimating() {
        return (Boolean)Browser.getDriver().executeScript("return metadataEditor.detailMap.getAnimating()");
    }

    private Double getOpenLayersZoom() {
        Object result = Browser.getDriver().executeScript("return metadataEditor.detailMap.getZoom()");
        return ((Number)result).doubleValue();
    }

    private Double getOpenLayersRotation() {
        Object result = Browser.getDriver().executeScript("return metadataEditor.detailMap.getRotation()");
        return ((Number)result).doubleValue();
    }

}
