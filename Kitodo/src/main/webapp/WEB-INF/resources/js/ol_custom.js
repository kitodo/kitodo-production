/**
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */
/* globals ol */
// jshint unused:false

// Kitodo namespace
var kitodo = {};
kitodo.map = null;

/**
 * @param {Object=} options Custom control options for Kitodo in OpenLayers
 * @extends {ol.control.Rotate}
 * @constructor
 */
kitodo.RotateLeftControl = function(options = {}) {
    var buttonLeft = document.createElement('button');
    buttonLeft.innerHTML = "<i class='fa fa-undo'/>";
    buttonLeft.setAttribute("type", "button");
    buttonLeft.setAttribute("title", "Rotate left");

    var this_ = this;

    var handleRotateLeft = function() {
        var view = this_.getMap().getView();
        view.animate({
            rotation: view.getRotation() - (90 * (Math.PI / 180)),
            duration: 100
        });
    };

    buttonLeft.addEventListener('click', handleRotateLeft, false);

    var elementLeft = document.createElement('div');
    elementLeft.className = 'rotate-left ol-unselectable ol-control ol-rotate';
    elementLeft.appendChild(buttonLeft);

    ol.control.Control.call(this, {
        element: elementLeft,
        target: options.target
    });
};

/**
 * @param {Object=} options Custom control options for Kitodo in OpenLayers
 * @extends {ol.control.Rotate}
 * @constructor
 */
kitodo.RotateRightControl = function(options = {}) {
    var buttonRight = document.createElement('button');
    buttonRight.innerHTML = "<i class='fa fa-repeat'/>";
    buttonRight.setAttribute("type", "button");
    buttonRight.setAttribute("title", "Rotate right");

    var this_ = this;

    var handleRotateRight = function() {
        var view = this_.getMap().getView();
        view.animate({
            rotation:  view.getRotation() + (90 * (Math.PI / 180)),
            duration: 100
        });
    };

    buttonRight.addEventListener('click', handleRotateRight, false);

    var elementRight = document.createElement('div');
    elementRight.className = 'rotate-right ol-unselectable ol-control ol-rotate';
    elementRight.appendChild(buttonRight);

    ol.control.Control.call(this, {
        element: elementRight,
        target: options.target,
        duration: 250
    });
};

function resetNorth() {
    if (kitodo.map) {
        let view = kitodo.map.getView();
        view.animate({
            rotation: 0,
            duration: 0
        });
    }
}

/**
 * @param {Object=} options Custom control options for Kitodo in OpenLayers
 * @extends {ol.control.Rotate}
 * @constructor
 */
kitodo.ResetNorthControl = function(options = {}) {
    let buttonResetNorth = document.createElement("button");
    buttonResetNorth.innerHTML = "<i class='fa fa-compass'/>";
    buttonResetNorth.setAttribute("type", "button");
    buttonResetNorth.setAttribute("title", "Reset orientation");

    buttonResetNorth.addEventListener("click", resetNorth, false);

    let elementResetNorth = document.createElement("div");
    elementResetNorth.className = "ol-rotate ol-unselectable ol-control"; /*ol-rotate-reset*/
    elementResetNorth.appendChild(buttonResetNorth);

    ol.control.Control.call(this, {
        element: elementResetNorth,
        target: options.target,
        duration: 250
    });
};

ol.inherits(kitodo.RotateLeftControl, ol.control.Rotate);
ol.inherits(kitodo.RotateRightControl, ol.control.Rotate);
ol.inherits(kitodo.ResetNorthControl, ol.control.Rotate);

function random(length) {
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    for (var i = 0; i < length; i++) {
        text += possible.charAt(Math.floor(Math.random() * possible.length));
    }

    return text;
}

function createProjection(extent) {
    return new ol.proj.Projection({
        code: 'kitodo-image',
        units: 'pixels',
        extent: extent
    });
}

function createSource(extent, imagePath, projection) {
    return new ol.source.ImageStatic({
        url: imagePath,
        projection: projection,
        imageExtent: extent
    });
}

function hideCanvas() {
    let map = document.querySelector("#map canvas");
    let loadingIcon = document.querySelector("#map > .fa-spinner");
    if (map) {
        map.style.opacity = 0;
        loadingIcon.style.opacity = 1;
    }
}

function showCanvas() {
    let map = document.querySelector("#map canvas");
    let loadingIcon = document.querySelector("#map > .fa-spinner");
    if (map) {
        map.style.opacity = 1;
        loadingIcon.style.opacity = 0;
    }
}

function initializeMap(imageDimensions, imagePath) {
    // Map image coordinates to map coordinates to be able to use image extent in pixels.
    let extent = [0, 0, imageDimensions[0], imageDimensions[1]];
    let projection = createProjection(extent);

    kitodo.map = new ol.Map({
        controls: ol.control.defaults({
            attributionOptions: {
                collapsible: false
            },
            zoomOptions: {
                delta: 3
            },
            rotate: false
        }).extend([
            new kitodo.RotateRightControl(),
            new kitodo.RotateLeftControl(),
            new kitodo.ResetNorthControl()
        ]),
        layers: [
            new ol.layer.Image({
                source: createSource(extent, imagePath, projection)
            })
        ],
        target: 'map',
        view: new ol.View({
            projection: projection,
            center: ol.extent.getCenter(extent),
            zoomFactor: 1.1
        })
    });
    kitodo.map.getView().fit(extent, {});
    kitodo.map.on("rendercomplete", function () {
        showCanvas();
    });
}

function updateMap(imageDimensions, imagePath) {
    // Map image coordinates to map coordinates to be able to use image extent in pixels.
    let extent = [0, 0, imageDimensions[0], imageDimensions[1]];
    let projection = createProjection(extent);

    kitodo.map.getLayers().getArray()[0].setSource(createSource(extent, imagePath, projection));
    kitodo.map.getView().setCenter(ol.extent.getCenter(extent));
    kitodo.map.getView().getProjection().setExtent(extent);
    kitodo.map.getView().fit(extent, {});
}

function addListener(element) {
    element.on("load", function () {
        if (kitodo.map && $("#map .ol-viewport").length) {
            updateMap([element.width(), element.height()], element[0].src);
        } else {
            initializeMap([element.width(), element.height()], element[0].src);
        }
    });
}

function initializeImage() {
    resetNorth();
    hideCanvas();
    let image = $("#imagePreviewForm\\:mediaPreviewGraphicImage");
    if (image.length > 0) {
        addListener(image);
        image[0].src = image[0].src.replace(/&uuid=[a-z0-9]+/i, "") + "&uuid=" + random(8);
    }
}

function changeToMapView() {
    initializeImage();
    showCanvas();
    if (kitodo.map) {
        kitodo.map.handleTargetChanged_();
    }
}

// reload map if container was resized
$('#thirdColumnWrapper').on('resize', function () {
    if (kitodo.map) {
        // FIXME: This causes lags. It should only be executed *once* after resize.
        kitodo.map.updateSize();
    }
});

$(document).ready(function () {
    initializeImage();
});
