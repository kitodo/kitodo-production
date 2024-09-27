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

/**
 * Abstract class describing a custom control button for the OpenLayers map.
 */
class CustomControl extends ol.control.Control {

    /**
     * Initializes a custom control button with various options.
     * 
     * @param {object} options the custom control options (className, icon, title, other OpenLayer options)
     */
    constructor(options) {
        const className = options.className;
        const icon = options.icon;
        const title = options.title;

        const button = document.createElement('button');
        button.innerHTML = "<i class='fa " + icon + "'/>";
        button.setAttribute("type", "button");
        button.setAttribute("title", title);

        const element = document.createElement('div');
        element.className = className  + ' ol-unselectable ol-control ol-rotate';
        element.appendChild(button);

        super({
            element: element,
            target: options.target
        });

        button.addEventListener('click', this.handleClick.bind(this), false);
    }

    /**
     * Abstract method that handles a click event on the button.
     * 
     * @param {MouseEvent} event the click event
     */
    handleClick(event) {
        // not implemented
    }
};

/**
 * Custom control that rotates the image 90 degrees to the left.
 */
class RotateLeftControl extends CustomControl {

    constructor(options) {
        super(Object.assign(options || {}, {
            className: "rotate-left",
            icon: "fa-undo",
            title: "Rotate left",
        }));
    }

    handleClick() {
        const view = this.getMap().getView();
        view.animate({
            rotation: view.getRotation() - (90 * (Math.PI / 180)),
            duration: 100
        });
    }
};

/**
 * Custom control that rotates the image 90 degrees to the right.
 */
class RotateRightControl extends CustomControl {

    constructor(options) {
        super(Object.assign(options || {}, {
            className: "rotate-right",
            icon: "fa-repeat",
            title: "Rotate right",
        }));
    }

    handleClick() {
        const view = this.getMap().getView();
        view.animate({
            rotation: view.getRotation() + (90 * (Math.PI / 180)),
            duration: 100
        });
    }
};

/**
 * Custom control that rotates the image back to default.
 */
class RotateNorthControl extends CustomControl {

    constructor(options) {
        super(Object.assign(options || {}, {
            className: "rotate-north",
            icon: "fa-compass",
            title: "Reset orientation",
        }));
    }

    handleClick() {
        this.getMap().getView().animate({
            rotation: 0,
            duration: 100
        });
    }
};

/**
 * Custom control that scales the image back to default.
 */
class ResetZoomControl extends CustomControl {

    /** The image dimensions as OpenLayers extent */
    #extent;

    /**
     * Initialize a custom control button that scales the image back to its default position.
     * 
     * @param {object} options containing the extent (Array) describing the image dimensions
     */
    constructor(options) {
        super(Object.assign(options, {
            className: "reset-zoom",
            icon: "fa-expand",
            title: "Reset zoom",
        }));
        this.#extent = options.extent;
    }

    handleClick() {
        this.getMap().getView().fit(this.#extent, {});
    }
};

/**
 * Class managing OpenLayers detail map showing images.
 */
class KitodoDetailMap {

    /**
     * Remember position, rotation and zoom level such that a new OpenLayers map can be 
     * initialized with the same position, rotation and zoom level when new images are selected.
     */
    #last_view = {
        center: null,
        zoom: null,
        rotation: null,
    };

    /**
     * Image properties of the image that is currently shown in OpenLayers. Object with properties 
     * dimensions (width, height) and path (url).
     */
    #image = {
        dimensions: null,
        path: null,
    };

    /**
     * The OpenLayers maps instance
     */
    #map = null;

    /**
     * Initialize a new Kitodo detail map
     */
    constructor() {
        this.registerResizeEvent();
    }

    /** 
     * Debounces various event handlers to improve performance, e.g. when resizing. 
     * 
     * @param {function} func the function to be debounced
     * @param {number} timeout the timeout in milliseconds
     * 
     * @returns {function} the debounced function
     */
    static makeDebounced(func, timeout = 100) {
        let timer = null;
        return function () {
            clearTimeout(timer);
            timer = setTimeout(func, timeout);
        };
    }

    /**
     * Generate a random string of [a-zA-Z0-9].
     * 
     * @param {number} length the length of the string to be generated
     * @returns the string of random characters
     */
    static randomUUID(length) {
        const possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    
        let text = "";
        for (let i = 0; i < length; i++) {
            text += possible.charAt(Math.floor(Math.random() * possible.length));
        }
    
        return text;
    }

    /**
     * Create an OpenLayers projection given the image extent.
     * 
     * @param {Array} extent the extent describing the image dimensions
     * @returns {ol.proj.Projection} the OpenLayers projection
     */
    createProjection(extent) {
        return new ol.proj.Projection({
            code: 'kitodo-image',
            units: 'pixels',
            extent: extent
        });
    }

    /**
     * Create an OpenLayers source object for the image.
     * 
     * @param {Array} extent the extent describing the image dimensions
     * @param {string} path the path (url) of the image
     * @param {ol.proj.Projection} projection the OpenLayers projection used to map the image to the canvas
     * @returns {ol.source.ImageStatic} the OpenLayers image source object
     */
    createSource(extent, path, projection) {
        return new ol.source.ImageStatic({
            url: path,
            projection: projection,
            imageExtent: extent,
            interpolate: true
        });
    }

    /**
     * Hide the map canvas (while the image is loading)
     */
    hideCanvas() {
        let canvas = document.querySelector("#map canvas");
        let loadingIcon = document.querySelector("#map > .fa-spinner");
        if (canvas) {
            canvas.style.opacity = 0;
            loadingIcon.style.opacity = 1;
        }
    }
    
    /**
     * Show the map canvas (as soon as the image has finished loading)
     */
    showCanvas() {
        let canvas = document.querySelector("#map canvas");
        let loadingIcon = document.querySelector("#map > .fa-spinner");
        if (canvas) {
            canvas.style.opacity = 1;
            loadingIcon.style.opacity = 0;
        }
    }

    /**
     * Handler that is called as soon as the image was completely loaded
     * @param {*} image the jQuery image dom element
     */
    onImageLoad(image) {
        this.#image = {
            dimensions: [image.width(), image.height()],
            path: image[0].src,
        };
        this.initializeOpenLayersMap();
    }

    /**
     * Register the load event for the current image.
     */
    registerImageLoadEvent() {
        this.hideCanvas();
        let image = $("#imagePreviewForm\\:mediaPreviewGraphicImage");
        if (image.length > 0) {
            image.on("load", this.onImageLoad.bind(this, image));
            image[0].src = image[0].src.replace(/&uuid=[a-z0-9]+/i, "") + "&uuid=" + KitodoDetailMap.randomUUID(8);
        }
    }

    /**
     * Return extent array containg image dimensions.
     * 
     * @param {Array} dimensions dimensions in pixel as [width, height]
     * @returns {Array} the extent array
     */
    createImageExtent(dimensions) {
        return [0, 0, dimensions[0], dimensions[1]];
    }
    
    /**
     * Creates the OpenLayers map object as soon as the image as been loaded.
     */
    initializeOpenLayersMap() {
        // Map image coordinates to map coordinates to be able to use image extent in pixels.
        const extent = this.createImageExtent(this.#image.dimensions);
        const projection = this.createProjection(extent);
    
        if (this.#map) {
            // make last OpenLayers map forget canvas target 
            // (triggers OpenLayers cleanup code and allows garbage collection)
            this.#map.setTarget(null);
        }

        // initialize new OpenLayers map
        this.#map = new ol.Map({
            controls: ol.control.defaults({
                attributionOptions: {
                    collapsible: false
                },
                zoomOptions: {
                    delta: 3 // zoom delta when clicking zoom buttons
                },
                rotate: false
            }).extend([
                new RotateLeftControl(),
                new RotateRightControl(),
                new RotateNorthControl(),
                new ResetZoomControl({extent: extent})
            ]),
            interactions: ol.interaction.defaults({
                zoomDelta: 5, // zoom delta when using mouse wheel
                zoomDuration: 100,
            }),
            layers: [
                new ol.layer.Image({
                    source: this.createSource(extent, this.#image.path, projection)
                })
            ],
            target: 'map',
            view: new ol.View({
                projection: projection,
                center: this.unnormalizeCenter(this.#last_view.center, extent), 
                zoom: this.#last_view.zoom,
                rotation: this.#last_view.rotation,
                zoomFactor: 1.1,
                extent: extent,
                constrainOnlyCenter: true,
                smoothExtentConstraint: true,
                showFullExtent: true,
                padding: [20, 20, 20, 20]
            })
        });
        if (this.#last_view.center == null) {
            // fit image to current viewport unless previous zoom and center position is known
            this.#map.getView().fit(extent, {});
        }
        // register various events to make sure that previous view is remembered
        this.#map.on("rendercomplete", KitodoDetailMap.makeDebounced(this.onRenderComplete.bind(this)));
        this.#map.on("change", KitodoDetailMap.makeDebounced(this.saveCurrentView.bind(this)));
        this.#map.on("postrender", KitodoDetailMap.makeDebounced(this.saveCurrentView.bind(this)));
    }

    /**
     * Return unnormalized center coordinates in case previous center is known (not null). Otherwise
     * center is calculated from the image extent containing image dimensions.
     * 
     * @param {Array} center the normalized center coordinates [0..1, 0..1]
     * @returns {Array} unnormalized center
     */
    unnormalizeCenter(center) {
        if (center !== null) {
            return [
                center[0] * this.#image.dimensions[0],
                center[1] * this.#image.dimensions[1],
            ]
        }
        return ol.extent.getCenter(this.createImageExtent(this.#image.dimensions));
    }

    /**
     * Normalizes the center coordinates from [0..width, 0..height] to [0..1, 0..1] such 
     * that images with different dimensions are visualized at the same relative position 
     * in the viewport.
     * 
     * @param {Array} center the current center coordinates as reported by OpenLayers
     * @returns {Array} the normalized center coordinates
     */
    normalizeCenter(center) {
        return [
            center[0] / this.#image.dimensions[0],
            center[1] / this.#image.dimensions[1],
        ];
    }

    /**
     * Remembers current view properties (center, zoom rotation) such that the OpenLayers
     * map can be initialized with the same parameters when selecting another image.
     */
    saveCurrentView() {
        this.#last_view = {
            center: this.normalizeCenter(this.#map.getView().getCenter()),
            zoom: this.#map.getView().getZoom(),
            rotation: this.#map.getView().getRotation(),
        };
    }

    /**
     * Is called by OpenLayers whenever a canvas rendering has finished. Unless debounced, this 
     * event is triggered potentially at 60fps.
     */
    onRenderComplete() {
        this.showCanvas();
        this.saveCurrentView();
    }

    /**
     * Registers the resize event for the meta data editor column, such that the image can be 
     * repositioned appropriately.
     */
    registerResizeEvent() {
        // reload map if container was resized
        $('#thirdColumnWrapper').on('resize', KitodoDetailMap.makeDebounced(this.onResize.bind(this)));
    }

    /**
     * Is called when a resize event has happened. Unless debounced, this event is triggered potentially 
     * at 60fps.
     */
    onResize() {
        if (this.#map) {
            this.#map.updateSize();
        }
    }

    /** 
     * Reloads the image. Is called when the detail view is activated, or a new image was selected.
     */
    update() {
        this.registerImageLoadEvent();
    }
}

// register detail map class with the metadataEditor namespace
var metadataEditor = metadataEditor || {};
metadataEditor.detailMap = new KitodoDetailMap();

