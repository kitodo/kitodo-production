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

// Kitodo namespace
window.kitodo = {};
var kitodo = window.kitodo;
var map;

/**
 * @param {Object=} opt_options Custom control options for Kitodo in OpenLayers
 * @extends {ol.control.Rotate}
 * @constructor
 */
kitodo.RotateLeftControl = function(opt_options) {
    var options = opt_options || {};

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
 * @param {Object=} opt_options Custom control options for Kitodo in OpenLayers
 * @extends {ol.control.Rotate}
 * @constructor
 */
kitodo.RotateRightControl = function(opt_options) {
    var options = opt_options || {};

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

ol.inherits(kitodo.RotateLeftControl, ol.control.Rotate);
ol.inherits(kitodo.RotateRightControl, ol.control.Rotate);

// load image to get correct dimensions
var image = new Image();
var imagePath = document.getElementById("imageData").dataset.image;
var imageDimensions;
image.onload = function () {
    imageDimensions = [image.width, image.height];
    initializeMap(imageDimensions);
};
image.src = imagePath;

function initializeMap(imageDimensions) {
    // Map image coordinates to map coordinates to be able to use image extent in pixels.
    var extent = [0, 0, imageDimensions[0], imageDimensions[1]];
    var projection = new ol.proj.Projection({
        code: 'kitodo-image',
        units: 'pixels',
        extent: extent
    });

    map = new ol.Map({
        controls: ol.control.defaults({
            attributionOptions: {
                collapsible: false
            }
        }).extend([
            new kitodo.RotateLeftControl()
        ]).extend([
            new kitodo.RotateRightControl()
        ]),
        layers: [
            new ol.layer.Image({
                source: new ol.source.ImageStatic({
                    url: imagePath,
                    projection: projection,
                    imageExtent: extent
                })
            })
        ],
        target: 'map',
        view: new ol.View({
            projection: projection,
            center: ol.extent.getCenter(extent),
            zoom: 1,
            maxZoom: 8
        })
    });
}
