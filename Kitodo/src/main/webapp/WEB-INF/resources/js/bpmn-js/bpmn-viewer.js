/*!
 * bpmn-js - bpmn-viewer v0.25.1

 * Copyright 2014 - 2017 camunda Services GmbH and other contributors
 *
 * Released under the bpmn.io license
 * http://bpmn.io/license
 *
 * Source Code: https://github.com/bpmn-io/bpmn-js
 *
 * Date: 2017-11-23
 */
(function(f){if(typeof exports==="object"&&typeof module!=="undefined"){module.exports=f()}else if(typeof define==="function"&&define.amd){define([],f)}else{var g;if(typeof window!=="undefined"){g=window}else if(typeof global!=="undefined"){g=global}else if(typeof self!=="undefined"){g=self}else{g=this}g.BpmnJS = f()}})(function(){var define,module,exports;return (function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(_dereq_,module,exports){
/**
 * The code in the <project-logo></project-logo> area
 * must not be changed.
 *
 * @see http://bpmn.io/license for more information.
 */
'use strict';

var assign = _dereq_(190),
    omit = _dereq_(194),
    isNumber = _dereq_(184);

var domify = _dereq_(204),
    domQuery = _dereq_(206),
    domRemove = _dereq_(207);

var innerSVG = _dereq_(233);

var Diagram = _dereq_(30),
    BpmnModdle = _dereq_(15);


var inherits = _dereq_(72);

var Importer = _dereq_(8);


function checkValidationError(err) {

  // check if we can help the user by indicating wrong BPMN 2.0 xml
  // (in case he or the exporting tool did not get that right)

  var pattern = /unparsable content <([^>]+)> detected([\s\S]*)$/;
  var match = pattern.exec(err.message);

  if (match) {
    err.message =
      'unparsable content <' + match[1] + '> detected; ' +
      'this may indicate an invalid BPMN 2.0 diagram file' + match[2];
  }

  return err;
}

var DEFAULT_OPTIONS = {
  width: '100%',
  height: '100%',
  position: 'relative'
};


/**
 * Ensure the passed argument is a proper unit (defaulting to px)
 */
function ensureUnit(val) {
  return val + (isNumber(val) ? 'px' : '');
}

/**
 * A viewer for BPMN 2.0 diagrams.
 *
 * Have a look at {@link NavigatedViewer} or {@link Modeler} for bundles that include
 * additional features.
 *
 *
 * ## Extending the Viewer
 *
 * In order to extend the viewer pass extension modules to bootstrap via the
 * `additionalModules` option. An extension module is an object that exposes
 * named services.
 *
 * The following example depicts the integration of a simple
 * logging component that integrates with interaction events:
 *
 *
 * ```javascript
 *
 * // logging component
 * function InteractionLogger(eventBus) {
 *   eventBus.on('element.hover', function(event) {
 *     console.log()
 *   })
 * }
 *
 * InteractionLogger.$inject = [ 'eventBus' ]; // minification save
 *
 * // extension module
 * var extensionModule = {
 *   __init__: [ 'interactionLogger' ],
 *   interactionLogger: [ 'type', InteractionLogger ]
 * };
 *
 * // extend the viewer
 * var bpmnViewer = new Viewer({ additionalModules: [ extensionModule ] });
 * bpmnViewer.importXML(...);
 * ```
 *
 * @param {Object} [options] configuration options to pass to the viewer
 * @param {DOMElement} [options.container] the container to render the viewer in, defaults to body.
 * @param {String|Number} [options.width] the width of the viewer
 * @param {String|Number} [options.height] the height of the viewer
 * @param {Object} [options.moddleExtensions] extension packages to provide
 * @param {Array<didi.Module>} [options.modules] a list of modules to override the default modules
 * @param {Array<didi.Module>} [options.additionalModules] a list of modules to use with the default modules
 */
function Viewer(options) {

  options = assign({}, DEFAULT_OPTIONS, options);

  this._moddle = this._createModdle(options);

  this._container = this._createContainer(options);

  /* <project-logo> */

  addProjectLogo(this._container);

  /* </project-logo> */

  this._init(this._container, this._moddle, options);
}

inherits(Viewer, Diagram);

module.exports = Viewer;


/**
 * Parse and render a BPMN 2.0 diagram.
 *
 * Once finished the viewer reports back the result to the
 * provided callback function with (err, warnings).
 *
 * ## Life-Cycle Events
 *
 * During import the viewer will fire life-cycle events:
 *
 *   * import.parse.start (about to read model from xml)
 *   * import.parse.complete (model read; may have worked or not)
 *   * import.render.start (graphical import start)
 *   * import.render.complete (graphical import finished)
 *   * import.done (everything done)
 *
 * You can use these events to hook into the life-cycle.
 *
 * @param {String} xml the BPMN 2.0 xml
 * @param {Function} [done] invoked with (err, warnings=[])
 */
Viewer.prototype.importXML = function(xml, done) {

  // done is optional
  done = done || function() {};

  var self = this;

  // hook in pre-parse listeners +
  // allow xml manipulation
  xml = this._emit('import.parse.start', { xml: xml }) || xml;

  this._moddle.fromXML(xml, 'bpmn:Definitions', function(err, definitions, context) {

    // hook in post parse listeners +
    // allow definitions manipulation
    definitions = self._emit('import.parse.complete', {
      error: err,
      definitions: definitions,
      context: context
    }) || definitions;

    var parseWarnings = context.warnings;

    if (err) {
      err = checkValidationError(err);

      self._emit('import.done', { error: err, warnings: parseWarnings });

      return done(err, parseWarnings);
    }

    self.importDefinitions(definitions, function(err, importWarnings) {
      var allWarnings = [].concat(parseWarnings, importWarnings || []);

      self._emit('import.done', { error: err, warnings: allWarnings });

      done(err, allWarnings);
    });
  });
};

/**
 * Export the currently displayed BPMN 2.0 diagram as
 * a BPMN 2.0 XML document.
 *
 * @param {Object} [options] export options
 * @param {Boolean} [options.format=false] output formated XML
 * @param {Boolean} [options.preamble=true] output preamble
 *
 * @param {Function} done invoked with (err, xml)
 */
Viewer.prototype.saveXML = function(options, done) {

  if (!done) {
    done = options;
    options = {};
  }

  var definitions = this._definitions;

  if (!definitions) {
    return done(new Error('no definitions loaded'));
  }

  this._moddle.toXML(definitions, options, done);
};

/**
 * Export the currently displayed BPMN 2.0 diagram as
 * an SVG image.
 *
 * @param {Object} [options]
 * @param {Function} done invoked with (err, svgStr)
 */
Viewer.prototype.saveSVG = function(options, done) {

  if (!done) {
    done = options;
    options = {};
  }

  var canvas = this.get('canvas');

  var contentNode = canvas.getDefaultLayer(),
      defsNode = domQuery('defs', canvas._svg);

  var contents = innerSVG(contentNode),
      defs = defsNode ? '<defs>' + innerSVG(defsNode) + '</defs>' : '';

  var bbox = contentNode.getBBox();

  var svg =
    '<?xml version="1.0" encoding="utf-8"?>\n' +
    '<!-- created with bpmn-js / http://bpmn.io -->\n' +
    '<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">\n' +
    '<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" ' +
         'width="' + bbox.width + '" height="' + bbox.height + '" ' +
         'viewBox="' + bbox.x + ' ' + bbox.y + ' ' + bbox.width + ' ' + bbox.height + '" version="1.1">' +
      defs + contents +
    '</svg>';

  done(null, svg);
};

/**
 * Get a named diagram service.
 *
 * @example
 *
 * var elementRegistry = viewer.get('elementRegistry');
 * var startEventShape = elementRegistry.get('StartEvent_1');
 *
 * @param {String} name
 *
 * @return {Object} diagram service instance
 *
 * @method Viewer#get
 */

/**
 * Invoke a function in the context of this viewer.
 *
 * @example
 *
 * viewer.invoke(function(elementRegistry) {
 *   var startEventShape = elementRegistry.get('StartEvent_1');
 * });
 *
 * @param {Function} fn to be invoked
 *
 * @return {Object} the functions return value
 *
 * @method Viewer#invoke
 */

/**
 * Remove all drawn elements from the viewer.
 *
 * After calling this method the viewer can still
 * be reused for opening another diagram.
 *
 * @method Viewer#clear
 */

Viewer.prototype.importDefinitions = function(definitions, done) {

  // use try/catch to not swallow synchronous exceptions
  // that may be raised during model parsing
  try {

    if (this._definitions) {
      // clear existing rendered diagram
      this.clear();
    }

    // update definitions
    this._definitions = definitions;

    // perform graphical import
    Importer.importBpmnDiagram(this, definitions, done);
  } catch (e) {

    // handle synchronous errors
    done(e);
  }
};

Viewer.prototype.getModules = function() {
  return this._modules;
};

/**
 * Destroy the viewer instance and remove all its
 * remainders from the document tree.
 */
Viewer.prototype.destroy = function() {

  // diagram destroy
  Diagram.prototype.destroy.call(this);

  // dom detach
  domRemove(this._container);
};

/**
 * Register an event listener
 *
 * Remove a previously added listener via {@link #off(event, callback)}.
 *
 * @param {String} event
 * @param {Number} [priority]
 * @param {Function} callback
 * @param {Object} [that]
 */
Viewer.prototype.on = function(event, priority, callback, target) {
  return this.get('eventBus').on(event, priority, callback, target);
};

/**
 * De-register an event listener
 *
 * @param {String} event
 * @param {Function} callback
 */
Viewer.prototype.off = function(event, callback) {
  this.get('eventBus').off(event, callback);
};

Viewer.prototype.attachTo = function(parentNode) {

  if (!parentNode) {
    throw new Error('parentNode required');
  }

  // ensure we detach from the
  // previous, old parent
  this.detach();

  // unwrap jQuery if provided
  if (parentNode.get && parentNode.constructor.prototype.jquery) {
    parentNode = parentNode.get(0);
  }

  if (typeof parentNode === 'string') {
    parentNode = domQuery(parentNode);
  }

  parentNode.appendChild(this._container);

  this._emit('attach', {});
};

Viewer.prototype.getDefinitions = function() {
  return this._definitions;
};

Viewer.prototype.detach = function() {

  var container = this._container,
      parentNode = container.parentNode;

  if (!parentNode) {
    return;
  }

  this._emit('detach', {});

  parentNode.removeChild(container);
};

Viewer.prototype._init = function(container, moddle, options) {

  var baseModules = options.modules || this.getModules(),
      additionalModules = options.additionalModules || [],
      staticModules = [
        {
          bpmnjs: [ 'value', this ],
          moddle: [ 'value', moddle ]
        }
      ];

  var diagramModules = [].concat(staticModules, baseModules, additionalModules);

  var diagramOptions = assign(omit(options, 'additionalModules'), {
    canvas: assign({}, options.canvas, { container: container }),
    modules: diagramModules
  });

  // invoke diagram constructor
  Diagram.call(this, diagramOptions);

  if (options && options.container) {
    this.attachTo(options.container);
  }
};

/**
 * Emit an event on the underlying {@link EventBus}
 *
 * @param  {String} type
 * @param  {Object} event
 *
 * @return {Object} event processing result (if any)
 */
Viewer.prototype._emit = function(type, event) {
  return this.get('eventBus').fire(type, event);
};

Viewer.prototype._createContainer = function(options) {

  var container = domify('<div class="bjs-container"></div>');

  assign(container.style, {
    width: ensureUnit(options.width),
    height: ensureUnit(options.height),
    position: options.position
  });

  return container;
};

Viewer.prototype._createModdle = function(options) {
  var moddleOptions = assign({}, this._moddleExtensions, options.moddleExtensions);

  return new BpmnModdle(moddleOptions);
};


// modules the viewer is composed of
Viewer.prototype._modules = [
  _dereq_(2),
  _dereq_(52),
  _dereq_(51),
  _dereq_(47)
];

// default moddle extensions the viewer is composed of
Viewer.prototype._moddleExtensions = {};

/* <project-logo> */

var PoweredBy = _dereq_(14),
    domEvent = _dereq_(205);

/**
 * Adds the project logo to the diagram container as
 * required by the bpmn.io license.
 *
 * @see http://bpmn.io/license
 *
 * @param {Element} container
 */
function addProjectLogo(container) {
  var logoData = PoweredBy.BPMNIO_LOGO;

  var linkMarkup =
    '<a href="http://bpmn.io" ' +
       'target="_blank" ' +
       'class="bjs-powered-by" ' +
       'title="Powered by bpmn.io" ' +
       'style="position: absolute; bottom: 15px; right: 15px; z-index: 100">' +
        '<img src="data:image/png;base64,' + logoData + '">' +
    '</a>';

  var linkElement = domify(linkMarkup);

  container.appendChild(linkElement);

  domEvent.bind(linkElement, 'click', function(event) {
    PoweredBy.open();

    event.preventDefault();
  });
}

/* </project-logo> */

},{"14":14,"15":15,"184":184,"190":190,"194":194,"2":2,"204":204,"205":205,"206":206,"207":207,"233":233,"30":30,"47":47,"51":51,"52":52,"72":72,"8":8}],2:[function(_dereq_,module,exports){
module.exports = {
  __depends__: [
    _dereq_(5),
    _dereq_(10)
  ]
};
},{"10":10,"5":5}],3:[function(_dereq_,module,exports){
'use strict';

var inherits = _dereq_(72),
    isObject = _dereq_(185),
    assign = _dereq_(190),
    forEach = _dereq_(78),
    every = _dereq_(75),
    some = _dereq_(82);

var BaseRenderer = _dereq_(38),
    TextUtil = _dereq_(64),
    DiUtil = _dereq_(11);

var getBusinessObject = _dereq_(13).getBusinessObject,
    is = _dereq_(13).is;

var RenderUtil = _dereq_(62);

var componentsToPath = RenderUtil.componentsToPath,
    createLine = RenderUtil.createLine;

var domQuery = _dereq_(206);

var svgAppend = _dereq_(226),
    svgAttr = _dereq_(228),
    svgCreate = _dereq_(231),
    svgClasses = _dereq_(229);

var rotate = _dereq_(63).rotate,
    transform = _dereq_(63).transform,
    translate = _dereq_(63).translate;

var Ids = _dereq_(71),
    RENDERER_IDS = new Ids();

var TASK_BORDER_RADIUS = 10;
var INNER_OUTER_DIST = 3;

var LABEL_STYLE = {
  fontFamily: 'Arial, sans-serif',
  fontSize: 12
};


function BpmnRenderer(eventBus, styles, pathMap, canvas, priority) {

  BaseRenderer.call(this, eventBus, priority);

  var rendererId = RENDERER_IDS.next();

  var textUtil = new TextUtil({
    style: LABEL_STYLE,
    size: { width: 100 }
  });

  var markers = {};

  var computeStyle = styles.computeStyle;

  function addMarker(id, options) {
    var attrs = assign({
      fill: 'black',
      strokeWidth: 1,
      strokeLinecap: 'round',
      strokeDasharray: 'none'
    }, options.attrs);

    var ref = options.ref || { x: 0, y: 0 };

    var scale = options.scale || 1;

    // fix for safari / chrome / firefox bug not correctly
    // resetting stroke dash array
    if (attrs.strokeDasharray === 'none') {
      attrs.strokeDasharray = [10000, 1];
    }

    var marker = svgCreate('marker');

    svgAttr(options.element, attrs);

    svgAppend(marker, options.element);

    svgAttr(marker, {
      id: id,
      viewBox: '0 0 20 20',
      refX: ref.x,
      refY: ref.y,
      markerWidth: 20 * scale,
      markerHeight: 20 * scale,
      orient: 'auto'
    });

    var defs = domQuery('defs', canvas._svg);

    if (!defs) {
      defs = svgCreate('defs');

      svgAppend(canvas._svg, defs);
    }

    svgAppend(defs, marker);

    markers[id] = marker;
  }

  function marker(type, fill, stroke) {
    var id = type + '-' + fill + '-' + stroke + '-' + rendererId;

    if (!markers[id]) {
      createMarker(type, fill, stroke);
    }

    return 'url(#' + id +  ')';
  }

  function createMarker(type, fill, stroke) {
    var id = type + '-' + fill + '-' + stroke + '-' + rendererId;

    if (type === 'sequenceflow-end') {
      var sequenceflowEnd = svgCreate('path');
      svgAttr(sequenceflowEnd, { d: 'M 1 5 L 11 10 L 1 15 Z' });

      addMarker(id, {
        element: sequenceflowEnd,
        ref: { x: 11, y: 10 },
        scale: 0.5,
        attrs: {
          fill: stroke,
          stroke: stroke
        }
      });
    }

    if (type === 'messageflow-start') {
      var messageflowStart = svgCreate('circle');
      svgAttr(messageflowStart, { cx: 6, cy: 6, r: 3.5 });

      addMarker(id, {
        element: messageflowStart,
        attrs: {
          fill: fill,
          stroke: stroke
        },
        ref: { x: 6, y: 6 }
      });
    }

    if (type === 'messageflow-end') {
      var messageflowEnd = svgCreate('path');
      svgAttr(messageflowEnd, { d: 'm 1 5 l 0 -3 l 7 3 l -7 3 z' });

      addMarker(id, {
        element: messageflowEnd,
        attrs: {
          fill: fill,
          stroke: stroke,
          strokeLinecap: 'butt'
        },
        ref: { x: 8.5, y: 5 }
      });
    }

    if (type === 'association-start') {
      var associationStart = svgCreate('path');
      svgAttr(associationStart, { d: 'M 11 5 L 1 10 L 11 15' });

      addMarker(id, {
        element: associationStart,
        attrs: {
          fill: 'none',
          stroke: stroke,
          strokeWidth: 1.5
        },
        ref: { x: 1, y: 10 },
        scale: 0.5
      });
    }

    if (type === 'association-end') {
      var associationEnd = svgCreate('path');
      svgAttr(associationEnd, { d: 'M 1 5 L 11 10 L 1 15' });

      addMarker(id, {
        element: associationEnd,
        attrs: {
          fill: 'none',
          stroke: stroke,
          strokeWidth: 1.5
        },
        ref: { x: 12, y: 10 },
        scale: 0.5
      });
    }

    if (type === 'conditional-flow-marker') {
      var conditionalflowMarker = svgCreate('path');
      svgAttr(conditionalflowMarker, { d: 'M 0 10 L 8 6 L 16 10 L 8 14 Z' });

      addMarker(id, {
        element: conditionalflowMarker,
        attrs: {
          fill: fill,
          stroke: stroke
        },
        ref: { x: -1, y: 10 },
        scale: 0.5
      });
    }

    if (type === 'conditional-default-flow-marker') {
      var conditionaldefaultflowMarker = svgCreate('path');
      svgAttr(conditionaldefaultflowMarker, { d: 'M 6 4 L 10 16' });

      addMarker(id, {
        element: conditionaldefaultflowMarker,
        attrs: {
          stroke: stroke
        },
        ref: { x: 0, y: 10 },
        scale: 0.5
      });
    }
  }

  function drawCircle(parentGfx, width, height, offset, attrs) {

    if (isObject(offset)) {
      attrs = offset;
      offset = 0;
    }

    offset = offset || 0;

    attrs = computeStyle(attrs, {
      stroke: 'black',
      strokeWidth: 2,
      fill: 'white'
    });

    var cx = width / 2,
        cy = height / 2;

    var circle = svgCreate('circle');
    svgAttr(circle, {
      cx: cx,
      cy: cy,
      r: Math.round((width + height) / 4 - offset)
    });
    svgAttr(circle, attrs);

    svgAppend(parentGfx, circle);

    return circle;
  }

  function drawRect(parentGfx, width, height, r, offset, attrs) {

    if (isObject(offset)) {
      attrs = offset;
      offset = 0;
    }

    offset = offset || 0;

    attrs = computeStyle(attrs, {
      stroke: 'black',
      strokeWidth: 2,
      fill: 'white'
    });

    var rect = svgCreate('rect');
    svgAttr(rect, {
      x: offset,
      y: offset,
      width: width - offset * 2,
      height: height - offset * 2,
      rx: r,
      ry: r
    });
    svgAttr(rect, attrs);

    svgAppend(parentGfx, rect);

    return rect;
  }

  function drawDiamond(parentGfx, width, height, attrs) {

    var x_2 = width / 2;
    var y_2 = height / 2;

    var points = [{ x: x_2, y: 0 }, { x: width, y: y_2 }, { x: x_2, y: height }, { x: 0, y: y_2 }];

    var pointsString = points.map(function(point) {
      return point.x + ',' + point.y;
    }).join(' ');

    attrs = computeStyle(attrs, {
      stroke: 'black',
      strokeWidth: 2,
      fill: 'white'
    });

    var polygon = svgCreate('polygon');
    svgAttr(polygon, {
      points: pointsString
    });
    svgAttr(polygon, attrs);

    svgAppend(parentGfx, polygon);

    return polygon;
  }

  function drawLine(parentGfx, waypoints, attrs) {
    attrs = computeStyle(attrs, [ 'no-fill' ], {
      stroke: 'black',
      strokeWidth: 2,
      fill: 'none'
    });

    var line = createLine(waypoints, attrs);

    svgAppend(parentGfx, line);

    return line;
  }

  function drawPath(parentGfx, d, attrs) {

    attrs = computeStyle(attrs, [ 'no-fill' ], {
      strokeWidth: 2,
      stroke: 'black'
    });

    var path = svgCreate('path');
    svgAttr(path, { d: d });
    svgAttr(path, attrs);

    svgAppend(parentGfx, path);

    return path;
  }

  function drawMarker(type, parentGfx, path, attrs) {
    return drawPath(parentGfx, path, assign({ 'data-marker': type }, attrs));
  }

  function as(type) {
    return function(parentGfx, element) {
      return handlers[type](parentGfx, element);
    };
  }

  function renderer(type) {
    return handlers[type];
  }

  function renderEventContent(element, parentGfx) {

    var event = getSemantic(element);
    var isThrowing = isThrowEvent(event);

    if (isTypedEvent(event, 'bpmn:MessageEventDefinition')) {
      return renderer('bpmn:MessageEventDefinition')(parentGfx, element, isThrowing);
    }

    if (isTypedEvent(event, 'bpmn:TimerEventDefinition')) {
      return renderer('bpmn:TimerEventDefinition')(parentGfx, element, isThrowing);
    }

    if (isTypedEvent(event, 'bpmn:ConditionalEventDefinition')) {
      return renderer('bpmn:ConditionalEventDefinition')(parentGfx, element);
    }

    if (isTypedEvent(event, 'bpmn:SignalEventDefinition')) {
      return renderer('bpmn:SignalEventDefinition')(parentGfx, element, isThrowing);
    }

    if (isTypedEvent(event, 'bpmn:CancelEventDefinition') &&
      isTypedEvent(event, 'bpmn:TerminateEventDefinition', { parallelMultiple: false })) {
      return renderer('bpmn:MultipleEventDefinition')(parentGfx, element, isThrowing);
    }

    if (isTypedEvent(event, 'bpmn:CancelEventDefinition') &&
      isTypedEvent(event, 'bpmn:TerminateEventDefinition', { parallelMultiple: true })) {
      return renderer('bpmn:ParallelMultipleEventDefinition')(parentGfx, element, isThrowing);
    }

    if (isTypedEvent(event, 'bpmn:EscalationEventDefinition')) {
      return renderer('bpmn:EscalationEventDefinition')(parentGfx, element, isThrowing);
    }

    if (isTypedEvent(event, 'bpmn:LinkEventDefinition')) {
      return renderer('bpmn:LinkEventDefinition')(parentGfx, element, isThrowing);
    }

    if (isTypedEvent(event, 'bpmn:ErrorEventDefinition')) {
      return renderer('bpmn:ErrorEventDefinition')(parentGfx, element, isThrowing);
    }

    if (isTypedEvent(event, 'bpmn:CancelEventDefinition')) {
      return renderer('bpmn:CancelEventDefinition')(parentGfx, element, isThrowing);
    }

    if (isTypedEvent(event, 'bpmn:CompensateEventDefinition')) {
      return renderer('bpmn:CompensateEventDefinition')(parentGfx, element, isThrowing);
    }

    if (isTypedEvent(event, 'bpmn:TerminateEventDefinition')) {
      return renderer('bpmn:TerminateEventDefinition')(parentGfx, element, isThrowing);
    }

    return null;
  }

  function renderLabel(parentGfx, label, options) {
    var text = textUtil.createText(label || '', options);
    svgClasses(text).add('djs-label');
    svgAppend(parentGfx, text);

    return text;
  }

  function renderEmbeddedLabel(parentGfx, element, align) {
    var semantic = getSemantic(element);

    return renderLabel(parentGfx, semantic.name, {
      box: element,
      align: align,
      padding: 5,
      style: {
        fill: getStrokeColor(element)
      }
    });
  }

  function renderExternalLabel(parentGfx, element) {
    var semantic = getSemantic(element);
    var box = {
      width: 90,
      height: 30,
      x: element.width / 2 + element.x,
      y: element.height / 2 + element.y
    };

    return renderLabel(parentGfx, semantic.name, {
      box: box,
      fitBox: true,
      style: { fontSize: '11px' }
    });
  }

  function renderLaneLabel(parentGfx, text, element) {
    var textBox = renderLabel(parentGfx, text, {
      box: { height: 30, width: element.height },
      align: 'center-middle',
      style: {
        fill: getStrokeColor(element)
      }
    });

    var top = -1 * element.height;

    transform(textBox, 0, -top, 270);
  }

  function createPathFromConnection(connection) {
    var waypoints = connection.waypoints;

    var pathData = 'm  ' + waypoints[0].x + ',' + waypoints[0].y;
    for (var i = 1; i < waypoints.length; i++) {
      pathData += 'L' + waypoints[i].x + ',' + waypoints[i].y + ' ';
    }
    return pathData;
  }

  var handlers = this.handlers = {
    'bpmn:Event': function(parentGfx, element, attrs) {
      return drawCircle(parentGfx, element.width, element.height, attrs);
    },
    'bpmn:StartEvent': function(parentGfx, element) {
      var attrs = {
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      };

      var semantic = getSemantic(element);

      if (!semantic.isInterrupting) {
        attrs = {
          strokeDasharray: '6',
          strokeLinecap: 'round'
        };
      }

      var circle = renderer('bpmn:Event')(parentGfx, element, attrs);

      renderEventContent(element, parentGfx);

      return circle;
    },
    'bpmn:MessageEventDefinition': function(parentGfx, element, isThrowing) {
      var pathData = pathMap.getScaledPath('EVENT_MESSAGE', {
        xScaleFactor: 0.9,
        yScaleFactor: 0.9,
        containerWidth: element.width,
        containerHeight: element.height,
        position: {
          mx: 0.235,
          my: 0.315
        }
      });

      var fill = isThrowing ? getStrokeColor(element) : getFillColor(element);
      var stroke = isThrowing ? getFillColor(element) : getStrokeColor(element);

      var messagePath = drawPath(parentGfx, pathData, {
        strokeWidth: 1,
        fill: fill,
        stroke: stroke
      });

      return messagePath;
    },
    'bpmn:TimerEventDefinition': function(parentGfx, element) {
      var circle = drawCircle(parentGfx, element.width, element.height, 0.2 * element.height, {
        strokeWidth: 2,
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      });

      var pathData = pathMap.getScaledPath('EVENT_TIMER_WH', {
        xScaleFactor: 0.75,
        yScaleFactor: 0.75,
        containerWidth: element.width,
        containerHeight: element.height,
        position: {
          mx: 0.5,
          my: 0.5
        }
      });

      drawPath(parentGfx, pathData, {
        strokeWidth: 2,
        strokeLinecap: 'square',
        stroke: getStrokeColor(element)
      });

      for (var i = 0;i < 12;i++) {

        var linePathData = pathMap.getScaledPath('EVENT_TIMER_LINE', {
          xScaleFactor: 0.75,
          yScaleFactor: 0.75,
          containerWidth: element.width,
          containerHeight: element.height,
          position: {
            mx: 0.5,
            my: 0.5
          }
        });

        var width = element.width / 2;
        var height = element.height / 2;

        drawPath(parentGfx, linePathData, {
          strokeWidth: 1,
          strokeLinecap: 'square',
          transform: 'rotate(' + (i * 30) + ',' + height + ',' + width + ')',
          stroke: getStrokeColor(element)
        });
      }

      return circle;
    },
    'bpmn:EscalationEventDefinition': function(parentGfx, event, isThrowing) {
      var pathData = pathMap.getScaledPath('EVENT_ESCALATION', {
        xScaleFactor: 1,
        yScaleFactor: 1,
        containerWidth: event.width,
        containerHeight: event.height,
        position: {
          mx: 0.5,
          my: 0.2
        }
      });

      var fill = isThrowing ? getStrokeColor(event) : 'none';

      return drawPath(parentGfx, pathData, {
        strokeWidth: 1,
        fill: fill,
        stroke: getStrokeColor(event)
      });
    },
    'bpmn:ConditionalEventDefinition': function(parentGfx, event) {
      var pathData = pathMap.getScaledPath('EVENT_CONDITIONAL', {
        xScaleFactor: 1,
        yScaleFactor: 1,
        containerWidth: event.width,
        containerHeight: event.height,
        position: {
          mx: 0.5,
          my: 0.222
        }
      });

      return drawPath(parentGfx, pathData, {
        strokeWidth: 1,
        stroke: getStrokeColor(event)
      });
    },
    'bpmn:LinkEventDefinition': function(parentGfx, event, isThrowing) {
      var pathData = pathMap.getScaledPath('EVENT_LINK', {
        xScaleFactor: 1,
        yScaleFactor: 1,
        containerWidth: event.width,
        containerHeight: event.height,
        position: {
          mx: 0.57,
          my: 0.263
        }
      });

      var fill = isThrowing ? getStrokeColor(event) : 'none';

      return drawPath(parentGfx, pathData, {
        strokeWidth: 1,
        fill: fill,
        stroke: getStrokeColor(event)
      });
    },
    'bpmn:ErrorEventDefinition': function(parentGfx, event, isThrowing) {
      var pathData = pathMap.getScaledPath('EVENT_ERROR', {
        xScaleFactor: 1.1,
        yScaleFactor: 1.1,
        containerWidth: event.width,
        containerHeight: event.height,
        position: {
          mx: 0.2,
          my: 0.722
        }
      });

      var fill = isThrowing ? getStrokeColor(event) : 'none';

      return drawPath(parentGfx, pathData, {
        strokeWidth: 1,
        fill: fill,
        stroke: getStrokeColor(event)
      });
    },
    'bpmn:CancelEventDefinition': function(parentGfx, event, isThrowing) {
      var pathData = pathMap.getScaledPath('EVENT_CANCEL_45', {
        xScaleFactor: 1.0,
        yScaleFactor: 1.0,
        containerWidth: event.width,
        containerHeight: event.height,
        position: {
          mx: 0.638,
          my: -0.055
        }
      });

      var fill = isThrowing ? 'black' : 'none';

      var path = drawPath(parentGfx, pathData, {
        strokeWidth: 1,
        fill: fill
      });

      rotate(path, 45);

      return path;
    },
    'bpmn:CompensateEventDefinition': function(parentGfx, event, isThrowing) {
      var pathData = pathMap.getScaledPath('EVENT_COMPENSATION', {
        xScaleFactor: 1,
        yScaleFactor: 1,
        containerWidth: event.width,
        containerHeight: event.height,
        position: {
          mx: 0.22,
          my: 0.5
        }
      });

      var fill = isThrowing ? getStrokeColor(event) : 'none';

      return drawPath(parentGfx, pathData, {
        strokeWidth: 1,
        fill: fill,
        stroke: getStrokeColor(event)
      });
    },
    'bpmn:SignalEventDefinition': function(parentGfx, event, isThrowing) {
      var pathData = pathMap.getScaledPath('EVENT_SIGNAL', {
        xScaleFactor: 0.9,
        yScaleFactor: 0.9,
        containerWidth: event.width,
        containerHeight: event.height,
        position: {
          mx: 0.5,
          my: 0.2
        }
      });

      var fill = isThrowing ? getStrokeColor(event) : 'none';

      return drawPath(parentGfx, pathData, {
        strokeWidth: 1,
        fill: fill,
        stroke: getStrokeColor(event)
      });
    },
    'bpmn:MultipleEventDefinition': function(parentGfx, event, isThrowing) {
      var pathData = pathMap.getScaledPath('EVENT_MULTIPLE', {
        xScaleFactor: 1.1,
        yScaleFactor: 1.1,
        containerWidth: event.width,
        containerHeight: event.height,
        position: {
          mx: 0.222,
          my: 0.36
        }
      });

      var fill = isThrowing ? 'black' : 'none';

      return drawPath(parentGfx, pathData, {
        strokeWidth: 1,
        fill: fill
      });
    },
    'bpmn:ParallelMultipleEventDefinition': function(parentGfx, event) {
      var pathData = pathMap.getScaledPath('EVENT_PARALLEL_MULTIPLE', {
        xScaleFactor: 1.2,
        yScaleFactor: 1.2,
        containerWidth: event.width,
        containerHeight: event.height,
        position: {
          mx: 0.458,
          my: 0.194
        }
      });

      return drawPath(parentGfx, pathData, {
        strokeWidth: 1,
        fill: getStrokeColor(event),
        stroke: getStrokeColor(event)
      });
    },
    'bpmn:EndEvent': function(parentGfx, element) {
      var circle = renderer('bpmn:Event')(parentGfx, element, {
        strokeWidth: 4,
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      });

      renderEventContent(element, parentGfx, true);

      return circle;
    },
    'bpmn:TerminateEventDefinition': function(parentGfx, element) {
      var circle = drawCircle(parentGfx, element.width, element.height, 8, {
        strokeWidth: 4,
        fill: getStrokeColor(element),
        stroke: getStrokeColor(element)
      });

      return circle;
    },
    'bpmn:IntermediateEvent': function(parentGfx, element) {
      var outer = renderer('bpmn:Event')(parentGfx, element, {
        strokeWidth: 1,
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      });

      /* inner */ drawCircle(parentGfx, element.width, element.height, INNER_OUTER_DIST, {
        strokeWidth: 1,
        fill: getFillColor(element, 'none'),
        stroke: getStrokeColor(element)
      });

      renderEventContent(element, parentGfx);

      return outer;
    },
    'bpmn:IntermediateCatchEvent': as('bpmn:IntermediateEvent'),
    'bpmn:IntermediateThrowEvent': as('bpmn:IntermediateEvent'),

    'bpmn:Activity': function(parentGfx, element, attrs) {
      return drawRect(parentGfx, element.width, element.height, TASK_BORDER_RADIUS, attrs);
    },

    'bpmn:Task': function(parentGfx, element) {
      var attrs = {
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      };

      var rect = renderer('bpmn:Activity')(parentGfx, element, attrs);

      renderEmbeddedLabel(parentGfx, element, 'center-middle');
      attachTaskMarkers(parentGfx, element);

      return rect;
    },
    'bpmn:ServiceTask': function(parentGfx, element) {
      var task = renderer('bpmn:Task')(parentGfx, element);

      var pathDataBG = pathMap.getScaledPath('TASK_TYPE_SERVICE', {
        abspos: {
          x: 12,
          y: 18
        }
      });

      /* service bg */ drawPath(parentGfx, pathDataBG, {
        strokeWidth: 1,
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      });

      var fillPathData = pathMap.getScaledPath('TASK_TYPE_SERVICE_FILL', {
        abspos: {
          x: 17.2,
          y: 18
        }
      });

      /* service fill */ drawPath(parentGfx, fillPathData, {
        strokeWidth: 0,
        fill: getFillColor(element)
      });

      var pathData = pathMap.getScaledPath('TASK_TYPE_SERVICE', {
        abspos: {
          x: 17,
          y: 22
        }
      });

      /* service */ drawPath(parentGfx, pathData, {
        strokeWidth: 1,
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      });

      return task;
    },
    'bpmn:UserTask': function(parentGfx, element) {
      var task = renderer('bpmn:Task')(parentGfx, element);

      var x = 15;
      var y = 12;

      var pathData = pathMap.getScaledPath('TASK_TYPE_USER_1', {
        abspos: {
          x: x,
          y: y
        }
      });

      /* user path */ drawPath(parentGfx, pathData, {
        strokeWidth: 0.5,
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      });

      var pathData2 = pathMap.getScaledPath('TASK_TYPE_USER_2', {
        abspos: {
          x: x,
          y: y
        }
      });

      /* user2 path */ drawPath(parentGfx, pathData2, {
        strokeWidth: 0.5,
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      });

      var pathData3 = pathMap.getScaledPath('TASK_TYPE_USER_3', {
        abspos: {
          x: x,
          y: y
        }
      });

      /* user3 path */ drawPath(parentGfx, pathData3, {
        strokeWidth: 0.5,
        fill: getStrokeColor(element),
        stroke: getStrokeColor(element)
      });

      return task;
    },
    'bpmn:ManualTask': function(parentGfx, element) {
      var task = renderer('bpmn:Task')(parentGfx, element);

      var pathData = pathMap.getScaledPath('TASK_TYPE_MANUAL', {
        abspos: {
          x: 17,
          y: 15
        }
      });

      /* manual path */ drawPath(parentGfx, pathData, {
        strokeWidth: 0.5, // 0.25,
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      });

      return task;
    },
    'bpmn:SendTask': function(parentGfx, element) {
      var task = renderer('bpmn:Task')(parentGfx, element);

      var pathData = pathMap.getScaledPath('TASK_TYPE_SEND', {
        xScaleFactor: 1,
        yScaleFactor: 1,
        containerWidth: 21,
        containerHeight: 14,
        position: {
          mx: 0.285,
          my: 0.357
        }
      });

      /* send path */ drawPath(parentGfx, pathData, {
        strokeWidth: 1,
        fill: getStrokeColor(element),
        stroke: getFillColor(element)
      });

      return task;
    },
    'bpmn:ReceiveTask' : function(parentGfx, element) {
      var semantic = getSemantic(element);

      var task = renderer('bpmn:Task')(parentGfx, element);
      var pathData;

      if (semantic.instantiate) {
        drawCircle(parentGfx, 28, 28, 20 * 0.22, { strokeWidth: 1 });

        pathData = pathMap.getScaledPath('TASK_TYPE_INSTANTIATING_SEND', {
          abspos: {
            x: 7.77,
            y: 9.52
          }
        });
      } else {

        pathData = pathMap.getScaledPath('TASK_TYPE_SEND', {
          xScaleFactor: 0.9,
          yScaleFactor: 0.9,
          containerWidth: 21,
          containerHeight: 14,
          position: {
            mx: 0.3,
            my: 0.4
          }
        });
      }

      /* receive path */ drawPath(parentGfx, pathData, {
        strokeWidth: 1,
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      });

      return task;
    },
    'bpmn:ScriptTask': function(parentGfx, element) {
      var task = renderer('bpmn:Task')(parentGfx, element);

      var pathData = pathMap.getScaledPath('TASK_TYPE_SCRIPT', {
        abspos: {
          x: 15,
          y: 20
        }
      });

      /* script path */ drawPath(parentGfx, pathData, {
        strokeWidth: 1,
        stroke: getStrokeColor(element)
      });

      return task;
    },
    'bpmn:BusinessRuleTask': function(parentGfx, element) {
      var task = renderer('bpmn:Task')(parentGfx, element);

      var headerPathData = pathMap.getScaledPath('TASK_TYPE_BUSINESS_RULE_HEADER', {
        abspos: {
          x: 8,
          y: 8
        }
      });

      var businessHeaderPath = drawPath(parentGfx, headerPathData);
      svgAttr(businessHeaderPath, {
        strokeWidth: 1,
        fill: getFillColor(element, '#aaaaaa'),
        stroke: getStrokeColor(element)
      });

      var headerData = pathMap.getScaledPath('TASK_TYPE_BUSINESS_RULE_MAIN', {
        abspos: {
          x: 8,
          y: 8
        }
      });

      var businessPath = drawPath(parentGfx, headerData);
      svgAttr(businessPath, {
        strokeWidth: 1,
        stroke: getStrokeColor(element)
      });

      return task;
    },
    'bpmn:SubProcess': function(parentGfx, element, attrs) {
      attrs = assign({
        fillOpacity: 0.95,
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      }, attrs);

      var rect = renderer('bpmn:Activity')(parentGfx, element, attrs);

      var expanded = DiUtil.isExpanded(element);

      var isEventSubProcess = DiUtil.isEventSubProcess(element);

      if (isEventSubProcess) {
        svgAttr(rect, {
          strokeDasharray: '1,2'
        });
      }

      renderEmbeddedLabel(parentGfx, element, expanded ? 'center-top' : 'center-middle');

      if (expanded) {
        attachTaskMarkers(parentGfx, element);
      } else {
        attachTaskMarkers(parentGfx, element, ['SubProcessMarker']);
      }

      return rect;
    },
    'bpmn:AdHocSubProcess': function(parentGfx, element) {
      return renderer('bpmn:SubProcess')(parentGfx, element);
    },
    'bpmn:Transaction': function(parentGfx, element) {
      var outer = renderer('bpmn:SubProcess')(parentGfx, element);

      var innerAttrs = styles.style([ 'no-fill', 'no-events' ], {
        stroke: getStrokeColor(element)
      });

      /* inner path */ drawRect(parentGfx, element.width, element.height, TASK_BORDER_RADIUS - 2, INNER_OUTER_DIST, innerAttrs);

      return outer;
    },
    'bpmn:CallActivity': function(parentGfx, element) {
      return renderer('bpmn:SubProcess')(parentGfx, element, {
        strokeWidth: 5
      });
    },
    'bpmn:Participant': function(parentGfx, element) {

      var attrs = {
        fillOpacity: 0.95,
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      };

      var lane = renderer('bpmn:Lane')(parentGfx, element, attrs);

      var expandedPool = DiUtil.isExpanded(element);

      if (expandedPool) {
        drawLine(parentGfx, [
          { x: 30, y: 0 },
          { x: 30, y: element.height }
        ], {
          stroke: getStrokeColor(element)
        });
        var text = getSemantic(element).name;
        renderLaneLabel(parentGfx, text, element);
      } else {
        // Collapsed pool draw text inline
        var text2 = getSemantic(element).name;
        renderLabel(parentGfx, text2, {
          box: element, align: 'center-middle',
          style: {
            fill: getStrokeColor(element)
          }
        });
      }

      var participantMultiplicity = !!(getSemantic(element).participantMultiplicity);

      if (participantMultiplicity) {
        renderer('ParticipantMultiplicityMarker')(parentGfx, element);
      }

      return lane;
    },
    'bpmn:Lane': function(parentGfx, element, attrs) {
      var rect = drawRect(parentGfx, element.width, element.height, 0, assign({
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      }, attrs));

      var semantic = getSemantic(element);

      if (semantic.$type === 'bpmn:Lane') {
        var text = semantic.name;
        renderLaneLabel(parentGfx, text, element);
      }

      return rect;
    },
    'bpmn:InclusiveGateway': function(parentGfx, element) {
      var attrs = {
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      };

      var diamond = drawDiamond(parentGfx, element.width, element.height, attrs);

      /* circle path */
      drawCircle(parentGfx, element.width, element.height, element.height * 0.24, {
        strokeWidth: 2.5,
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      });

      return diamond;
    },
    'bpmn:ExclusiveGateway': function(parentGfx, element) {
      var attrs = {
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      };

      var diamond = drawDiamond(parentGfx, element.width, element.height, attrs);

      var pathData = pathMap.getScaledPath('GATEWAY_EXCLUSIVE', {
        xScaleFactor: 0.4,
        yScaleFactor: 0.4,
        containerWidth: element.width,
        containerHeight: element.height,
        position: {
          mx: 0.32,
          my: 0.3
        }
      });

      if ((getDi(element).isMarkerVisible)) {
        drawPath(parentGfx, pathData, {
          strokeWidth: 1,
          fill: getStrokeColor(element),
          stroke: getStrokeColor(element)
        });
      }

      return diamond;
    },
    'bpmn:ComplexGateway': function(parentGfx, element) {
      var attrs = {
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      };

      var diamond = drawDiamond(parentGfx, element.width, element.height, attrs);

      var pathData = pathMap.getScaledPath('GATEWAY_COMPLEX', {
        xScaleFactor: 0.5,
        yScaleFactor:0.5,
        containerWidth: element.width,
        containerHeight: element.height,
        position: {
          mx: 0.46,
          my: 0.26
        }
      });

      /* complex path */ drawPath(parentGfx, pathData, {
        strokeWidth: 1,
        fill: getStrokeColor(element),
        stroke: getStrokeColor(element)
      });

      return diamond;
    },
    'bpmn:ParallelGateway': function(parentGfx, element) {
      var attrs = {
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      };

      var diamond = drawDiamond(parentGfx, element.width, element.height, attrs);

      var pathData = pathMap.getScaledPath('GATEWAY_PARALLEL', {
        xScaleFactor: 0.6,
        yScaleFactor:0.6,
        containerWidth: element.width,
        containerHeight: element.height,
        position: {
          mx: 0.46,
          my: 0.2
        }
      });

      /* parallel path */ drawPath(parentGfx, pathData, {
        strokeWidth: 1,
        fill: getStrokeColor(element),
        stroke: getStrokeColor(element)
      });

      return diamond;
    },
    'bpmn:EventBasedGateway': function(parentGfx, element) {

      var semantic = getSemantic(element);

      var attrs = {
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      };

      var diamond = drawDiamond(parentGfx, element.width, element.height, attrs);

      /* outer circle path */ drawCircle(parentGfx, element.width, element.height, element.height * 0.20, {
        strokeWidth: 1,
        fill: 'none',
        stroke: getStrokeColor(element)
      });

      var type = semantic.eventGatewayType;
      var instantiate = !!semantic.instantiate;

      function drawEvent() {

        var pathData = pathMap.getScaledPath('GATEWAY_EVENT_BASED', {
          xScaleFactor: 0.18,
          yScaleFactor: 0.18,
          containerWidth: element.width,
          containerHeight: element.height,
          position: {
            mx: 0.36,
            my: 0.44
          }
        });

        var attrs = {
          strokeWidth: 2,
          fill: getFillColor(element, 'none'),
          stroke: getStrokeColor(element)
        };

        /* event path */ drawPath(parentGfx, pathData, attrs);
      }

      if (type === 'Parallel') {

        var pathData = pathMap.getScaledPath('GATEWAY_PARALLEL', {
          xScaleFactor: 0.4,
          yScaleFactor:0.4,
          containerWidth: element.width,
          containerHeight: element.height,
          position: {
            mx: 0.474,
            my: 0.296
          }
        });

        var parallelPath = drawPath(parentGfx, pathData);
        svgAttr(parallelPath, {
          strokeWidth: 1,
          fill: 'none'
        });
      } else if (type === 'Exclusive') {

        if (!instantiate) {
          var innerCircle = drawCircle(parentGfx, element.width, element.height, element.height * 0.26);
          svgAttr(innerCircle, {
            strokeWidth: 1,
            fill: 'none',
            stroke: getStrokeColor(element)
          });
        }

        drawEvent();
      }


      return diamond;
    },
    'bpmn:Gateway': function(parentGfx, element) {
      return drawDiamond(parentGfx, element.width, element.height);
    },
    'bpmn:SequenceFlow': function(parentGfx, element) {
      var pathData = createPathFromConnection(element);

      var fill = getFillColor(element),
          stroke = getStrokeColor(element);

      var attrs = {
        strokeLinejoin: 'round',
        markerEnd: marker('sequenceflow-end', fill, stroke),
        stroke: getStrokeColor(element)
      };

      var path = drawPath(parentGfx, pathData, attrs);

      var sequenceFlow = getSemantic(element);
      var source = element.source.businessObject;

      // conditional flow marker
      if (sequenceFlow.conditionExpression && source.$instanceOf('bpmn:Activity')) {
        svgAttr(path, {
          markerStart: marker('conditional-flow-marker', fill, stroke)
        });
      }

      // default marker
      if (source.default && (source.$instanceOf('bpmn:Gateway') || source.$instanceOf('bpmn:Activity')) &&
          source.default === sequenceFlow) {
        svgAttr(path, {
          markerStart: marker('conditional-default-flow-marker', fill, stroke)
        });
      }

      return path;
    },
    'bpmn:Association': function(parentGfx, element, attrs) {

      var semantic = getSemantic(element);

      var fill = getFillColor(element),
          stroke = getStrokeColor(element);

      attrs = assign({
        strokeDasharray: '0.5, 5',
        strokeLinecap: 'round',
        strokeLinejoin: 'round',
        stroke: getStrokeColor(element)
      }, attrs || {});

      if (semantic.associationDirection === 'One' ||
          semantic.associationDirection === 'Both') {
        attrs.markerEnd = marker('association-end', fill, stroke);
      }

      if (semantic.associationDirection === 'Both') {
        attrs.markerStart = marker('association-start', fill, stroke);
      }

      return drawLine(parentGfx, element.waypoints, attrs);
    },
    'bpmn:DataInputAssociation': function(parentGfx, element) {
      var fill = getFillColor(element),
          stroke = getStrokeColor(element);

      return renderer('bpmn:Association')(parentGfx, element, {
        markerEnd: marker('association-end', fill, stroke)
      });
    },
    'bpmn:DataOutputAssociation': function(parentGfx, element) {
      var fill = getFillColor(element),
          stroke = getStrokeColor(element);

      return renderer('bpmn:Association')(parentGfx, element, {
        markerEnd: marker('association-end', fill, stroke)
      });
    },
    'bpmn:MessageFlow': function(parentGfx, element) {

      var semantic = getSemantic(element),
          di = getDi(element);

      var fill = getFillColor(element),
          stroke = getStrokeColor(element);

      var pathData = createPathFromConnection(element);

      var attrs = {
        markerEnd: marker('messageflow-end', fill, stroke),
        markerStart: marker('messageflow-start', fill, stroke),
        strokeDasharray: '10, 12',
        strokeLinecap: 'round',
        strokeLinejoin: 'round',
        strokeWidth: '1.5px',
        stroke: getStrokeColor(element)
      };

      var path = drawPath(parentGfx, pathData, attrs);

      if (semantic.messageRef) {
        var midPoint = path.getPointAtLength(path.getTotalLength() / 2);

        var markerPathData = pathMap.getScaledPath('MESSAGE_FLOW_MARKER', {
          abspos: {
            x: midPoint.x,
            y: midPoint.y
          }
        });

        var messageAttrs = { strokeWidth: 1 };

        if (di.messageVisibleKind === 'initiating') {
          messageAttrs.fill = 'white';
          messageAttrs.stroke = 'black';
        } else {
          messageAttrs.fill = '#888';
          messageAttrs.stroke = 'white';
        }

        drawPath(parentGfx, markerPathData, messageAttrs);
      }

      return path;
    },
    'bpmn:DataObject': function(parentGfx, element) {
      var pathData = pathMap.getScaledPath('DATA_OBJECT_PATH', {
        xScaleFactor: 1,
        yScaleFactor: 1,
        containerWidth: element.width,
        containerHeight: element.height,
        position: {
          mx: 0.474,
          my: 0.296
        }
      });

      var elementObject = drawPath(parentGfx, pathData, {
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      });

      var semantic = getSemantic(element);

      if (isCollection(semantic)) {
        renderDataItemCollection(parentGfx, element);
      }

      return elementObject;
    },
    'bpmn:DataObjectReference': as('bpmn:DataObject'),
    'bpmn:DataInput': function(parentGfx, element) {

      var arrowPathData = pathMap.getRawPath('DATA_ARROW');

      // page
      var elementObject = renderer('bpmn:DataObject')(parentGfx, element);

      /* input arrow path */ drawPath(parentGfx, arrowPathData, { strokeWidth: 1 });

      return elementObject;
    },
    'bpmn:DataOutput': function(parentGfx, element) {
      var arrowPathData = pathMap.getRawPath('DATA_ARROW');

      // page
      var elementObject = renderer('bpmn:DataObject')(parentGfx, element);

      /* output arrow path */ drawPath(parentGfx, arrowPathData, {
        strokeWidth: 1,
        fill: 'black'
      });

      return elementObject;
    },
    'bpmn:DataStoreReference': function(parentGfx, element) {
      var DATA_STORE_PATH = pathMap.getScaledPath('DATA_STORE', {
        xScaleFactor: 1,
        yScaleFactor: 1,
        containerWidth: element.width,
        containerHeight: element.height,
        position: {
          mx: 0,
          my: 0.133
        }
      });

      var elementStore = drawPath(parentGfx, DATA_STORE_PATH, {
        strokeWidth: 2,
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      });

      return elementStore;
    },
    'bpmn:BoundaryEvent': function(parentGfx, element) {

      var semantic = getSemantic(element),
          cancel = semantic.cancelActivity;

      var attrs = {
        strokeWidth: 1,
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      };

      if (!cancel) {
        attrs.strokeDasharray = '6';
        attrs.strokeLinecap = 'round';
      }

      var outer = renderer('bpmn:Event')(parentGfx, element, attrs);
      /* inner path */ drawCircle(parentGfx, element.width, element.height, INNER_OUTER_DIST, assign(attrs, { fill: 'none' }));

      renderEventContent(element, parentGfx);

      return outer;
    },
    'bpmn:Group': function(parentGfx, element) {
      return drawRect(parentGfx, element.width, element.height, TASK_BORDER_RADIUS, {
        strokeWidth: 1,
        strokeDasharray: '8,3,1,3',
        fill: 'none',
        pointerEvents: 'none'
      });
    },
    'label': function(parentGfx, element) {
      return renderExternalLabel(parentGfx, element);
    },
    'bpmn:TextAnnotation': function(parentGfx, element) {
      var style = {
        'fill': 'none',
        'stroke': 'none'
      };

      var textElement = drawRect(parentGfx, element.width, element.height, 0, 0, style);

      var textPathData = pathMap.getScaledPath('TEXT_ANNOTATION', {
        xScaleFactor: 1,
        yScaleFactor: 1,
        containerWidth: element.width,
        containerHeight: element.height,
        position: {
          mx: 0.0,
          my: 0.0
        }
      });
      drawPath(parentGfx, textPathData, {
        stroke: getStrokeColor(element)
      });

      var text = getSemantic(element).text || '';
      renderLabel(parentGfx, text, { box: element, align: 'left-top', padding: 5 });

      return textElement;
    },
    'ParticipantMultiplicityMarker': function(parentGfx, element) {
      var markerPath = pathMap.getScaledPath('MARKER_PARALLEL', {
        xScaleFactor: 1,
        yScaleFactor: 1,
        containerWidth: element.width,
        containerHeight: element.height,
        position: {
          mx: ((element.width / 2) / element.width),
          my: (element.height - 15) / element.height
        }
      });

      drawMarker('participant-multiplicity', parentGfx, markerPath);
    },
    'SubProcessMarker': function(parentGfx, element) {
      var markerRect = drawRect(parentGfx, 14, 14, 0, {
        strokeWidth: 1,
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      });

      // Process marker is placed in the middle of the box
      // therefore fixed values can be used here
      translate(markerRect, element.width / 2 - 7.5, element.height - 20);

      var markerPath = pathMap.getScaledPath('MARKER_SUB_PROCESS', {
        xScaleFactor: 1.5,
        yScaleFactor: 1.5,
        containerWidth: element.width,
        containerHeight: element.height,
        position: {
          mx: (element.width / 2 - 7.5) / element.width,
          my: (element.height - 20) / element.height
        }
      });

      drawMarker('sub-process', parentGfx, markerPath, {
        fill: getFillColor(element),
        stroke: getStrokeColor(element)
      });
    },
    'ParallelMarker': function(parentGfx, element, position) {
      var markerPath = pathMap.getScaledPath('MARKER_PARALLEL', {
        xScaleFactor: 1,
        yScaleFactor: 1,
        containerWidth: element.width,
        containerHeight: element.height,
        position: {
          mx: ((element.width / 2 + position.parallel) / element.width),
          my: (element.height - 20) / element.height
        }
      });

      drawMarker('parallel', parentGfx, markerPath);
    },
    'SequentialMarker': function(parentGfx, element, position) {
      var markerPath = pathMap.getScaledPath('MARKER_SEQUENTIAL', {
        xScaleFactor: 1,
        yScaleFactor: 1,
        containerWidth: element.width,
        containerHeight: element.height,
        position: {
          mx: ((element.width / 2 + position.seq) / element.width),
          my: (element.height - 19) / element.height
        }
      });

      drawMarker('sequential', parentGfx, markerPath);
    },
    'CompensationMarker': function(parentGfx, element, position) {
      var markerMath = pathMap.getScaledPath('MARKER_COMPENSATION', {
        xScaleFactor: 1,
        yScaleFactor: 1,
        containerWidth: element.width,
        containerHeight: element.height,
        position: {
          mx: ((element.width / 2 + position.compensation) / element.width),
          my: (element.height - 13) / element.height
        }
      });

      drawMarker('compensation', parentGfx, markerMath, { strokeWidth: 1 });
    },
    'LoopMarker': function(parentGfx, element, position) {
      var markerPath = pathMap.getScaledPath('MARKER_LOOP', {
        xScaleFactor: 1,
        yScaleFactor: 1,
        containerWidth: element.width,
        containerHeight: element.height,
        position: {
          mx: ((element.width / 2 + position.loop) / element.width),
          my: (element.height - 7) / element.height
        }
      });

      drawMarker('loop', parentGfx, markerPath, {
        strokeWidth: 1,
        fill: 'none',
        strokeLinecap: 'round',
        strokeMiterlimit: 0.5
      });
    },
    'AdhocMarker': function(parentGfx, element, position) {
      var markerPath = pathMap.getScaledPath('MARKER_ADHOC', {
        xScaleFactor: 1,
        yScaleFactor: 1,
        containerWidth: element.width,
        containerHeight: element.height,
        position: {
          mx: ((element.width / 2 + position.adhoc) / element.width),
          my: (element.height - 15) / element.height
        }
      });

      drawMarker('adhoc', parentGfx, markerPath, {
        strokeWidth: 1,
        fill: 'black'
      });
    }
  };

  function attachTaskMarkers(parentGfx, element, taskMarkers) {
    var obj = getSemantic(element);

    var subprocess = taskMarkers && taskMarkers.indexOf('SubProcessMarker') !== -1;
    var position;

    if (subprocess) {
      position = {
        seq: -21,
        parallel: -22,
        compensation: -42,
        loop: -18,
        adhoc: 10
      };
    } else {
      position = {
        seq: -3,
        parallel: -6,
        compensation: -27,
        loop: 0,
        adhoc: 10
      };
    }

    forEach(taskMarkers, function(marker) {
      renderer(marker)(parentGfx, element, position);
    });

    if (obj.isForCompensation) {
      renderer('CompensationMarker')(parentGfx, element, position);
    }

    if (obj.$type === 'bpmn:AdHocSubProcess') {
      renderer('AdhocMarker')(parentGfx, element, position);
    }

    var loopCharacteristics = obj.loopCharacteristics,
        isSequential = loopCharacteristics && loopCharacteristics.isSequential;

    if (loopCharacteristics) {

      if (isSequential === undefined) {
        renderer('LoopMarker')(parentGfx, element, position);
      }

      if (isSequential === false) {
        renderer('ParallelMarker')(parentGfx, element, position);
      }

      if (isSequential === true) {
        renderer('SequentialMarker')(parentGfx, element, position);
      }
    }
  }

  function renderDataItemCollection(parentGfx, element) {

    var yPosition = (element.height - 16) / element.height;

    var pathData = pathMap.getScaledPath('DATA_OBJECT_COLLECTION_PATH', {
      xScaleFactor: 1,
      yScaleFactor: 1,
      containerWidth: element.width,
      containerHeight: element.height,
      position: {
        mx: 0.451,
        my: yPosition
      }
    });

    /* collection path */ drawPath(parentGfx, pathData, {
      strokeWidth: 2
    });
  }


  // extension API, use at your own risk
  this._drawPath = drawPath;

}


inherits(BpmnRenderer, BaseRenderer);

BpmnRenderer.$inject = [ 'eventBus', 'styles', 'pathMap', 'canvas' ];

module.exports = BpmnRenderer;


BpmnRenderer.prototype.canRender = function(element) {
  return is(element, 'bpmn:BaseElement');
};

BpmnRenderer.prototype.drawShape = function(parentGfx, element) {
  var type = element.type;
  var h = this.handlers[type];

  /* jshint -W040 */
  return h(parentGfx, element);
};

BpmnRenderer.prototype.drawConnection = function(parentGfx, element) {
  var type = element.type;
  var h = this.handlers[type];

  /* jshint -W040 */
  return h(parentGfx, element);
};

BpmnRenderer.prototype.getShapePath = function(element) {

  if (is(element, 'bpmn:Event')) {
    return getCirclePath(element);
  }

  if (is(element, 'bpmn:Activity')) {
    return getRoundRectPath(element, TASK_BORDER_RADIUS);
  }

  if (is(element, 'bpmn:Gateway')) {
    return getDiamondPath(element);
  }

  return getRectPath(element);
};


///////// helper functions /////////////////////////////

/**
 * Checks if eventDefinition of the given element matches with semantic type.
 *
 * @return {boolean} true if element is of the given semantic type
 */
function isTypedEvent(event, eventDefinitionType, filter) {

  function matches(definition, filter) {
    return every(filter, function(val, key) {

      // we want a == conversion here, to be able to catch
      // undefined == false and friends
      /* jshint -W116 */
      return definition[key] == val;
    });
  }

  return some(event.eventDefinitions, function(definition) {
    return definition.$type === eventDefinitionType && matches(event, filter);
  });
}

function isThrowEvent(event) {
  return (event.$type === 'bpmn:IntermediateThrowEvent') || (event.$type === 'bpmn:EndEvent');
}

function isCollection(element) {
  var dataObject = element.dataObjectRef;

  return element.isCollection || (dataObject && dataObject.isCollection);
}

function getDi(element) {
  return element.businessObject.di;
}

function getSemantic(element) {
  return element.businessObject;
}



/////// cropping path customizations /////////////////////////

function getCirclePath(shape) {

  var cx = shape.x + shape.width / 2,
      cy = shape.y + shape.height / 2,
      radius = shape.width / 2;

  var circlePath = [
    ['M', cx, cy],
    ['m', 0, -radius],
    ['a', radius, radius, 0, 1, 1, 0, 2 * radius],
    ['a', radius, radius, 0, 1, 1, 0, -2 * radius],
    ['z']
  ];

  return componentsToPath(circlePath);
}

function getRoundRectPath(shape, borderRadius) {

  var x = shape.x,
      y = shape.y,
      width = shape.width,
      height = shape.height;

  var roundRectPath = [
    ['M', x + borderRadius, y],
    ['l', width - borderRadius * 2, 0],
    ['a', borderRadius, borderRadius, 0, 0, 1, borderRadius, borderRadius],
    ['l', 0, height - borderRadius * 2],
    ['a', borderRadius, borderRadius, 0, 0, 1, -borderRadius, borderRadius],
    ['l', borderRadius * 2 - width, 0],
    ['a', borderRadius, borderRadius, 0, 0, 1, -borderRadius, -borderRadius],
    ['l', 0, borderRadius * 2 - height],
    ['a', borderRadius, borderRadius, 0, 0, 1, borderRadius, -borderRadius],
    ['z']
  ];

  return componentsToPath(roundRectPath);
}

function getDiamondPath(shape) {

  var width = shape.width,
      height = shape.height,
      x = shape.x,
      y = shape.y,
      halfWidth = width / 2,
      halfHeight = height / 2;

  var diamondPath = [
    ['M', x + halfWidth, y],
    ['l', halfWidth, halfHeight],
    ['l', -halfWidth, halfHeight],
    ['l', -halfWidth, -halfHeight],
    ['z']
  ];

  return componentsToPath(diamondPath);
}

function getRectPath(shape) {
  var x = shape.x,
      y = shape.y,
      width = shape.width,
      height = shape.height;

  var rectPath = [
    ['M', x, y],
    ['l', width, 0],
    ['l', 0, height],
    ['l', -width, 0],
    ['z']
  ];

  return componentsToPath(rectPath);
}

function getFillColor(element, defaultColor) {
  var bo = getBusinessObject(element);

  return bo.di.get('fill') || defaultColor || 'white';
}

function getStrokeColor(element, defaultColor) {
  var bo = getBusinessObject(element);

  return bo.di.get('stroke') || defaultColor || 'black';
}

},{"11":11,"13":13,"185":185,"190":190,"206":206,"226":226,"228":228,"229":229,"231":231,"38":38,"62":62,"63":63,"64":64,"71":71,"72":72,"75":75,"78":78,"82":82}],4:[function(_dereq_,module,exports){
'use strict';

/**
 * Map containing SVG paths needed by BpmnRenderer.
 */

function PathMap() {

  /**
   * Contains a map of path elements
   *
   * <h1>Path definition</h1>
   * A parameterized path is defined like this:
   * <pre>
   * 'GATEWAY_PARALLEL': {
   *   d: 'm {mx},{my} {e.x0},0 0,{e.x1} {e.x1},0 0,{e.y0} -{e.x1},0 0,{e.y1} ' +
          '-{e.x0},0 0,-{e.y1} -{e.x1},0 0,-{e.y0} {e.x1},0 z',
   *   height: 17.5,
   *   width:  17.5,
   *   heightElements: [2.5, 7.5],
   *   widthElements: [2.5, 7.5]
   * }
   * </pre>
   * <p>It's important to specify a correct <b>height and width</b> for the path as the scaling
   * is based on the ratio between the specified height and width in this object and the
   * height and width that is set as scale target (Note x,y coordinates will be scaled with
   * individual ratios).</p>
   * <p>The '<b>heightElements</b>' and '<b>widthElements</b>' array must contain the values that will be scaled.
   * The scaling is based on the computed ratios.
   * Coordinates on the y axis should be in the <b>heightElement</b>'s array, they will be scaled using
   * the computed ratio coefficient.
   * In the parameterized path the scaled values can be accessed through the 'e' object in {} brackets.
   *   <ul>
   *    <li>The values for the y axis can be accessed in the path string using {e.y0}, {e.y1}, ....</li>
   *    <li>The values for the x axis can be accessed in the path string using {e.x0}, {e.x1}, ....</li>
   *   </ul>
   *   The numbers x0, x1 respectively y0, y1, ... map to the corresponding array index.
   * </p>
   */
  this.pathMap = {
    'EVENT_MESSAGE': {
      d: 'm {mx},{my} l 0,{e.y1} l {e.x1},0 l 0,-{e.y1} z l {e.x0},{e.y0} l {e.x0},-{e.y0}',
      height: 36,
      width:  36,
      heightElements: [6, 14],
      widthElements: [10.5, 21]
    },
    'EVENT_SIGNAL': {
      d: 'M {mx},{my} l {e.x0},{e.y0} l -{e.x1},0 Z',
      height: 36,
      width: 36,
      heightElements: [18],
      widthElements: [10, 20]
    },
    'EVENT_ESCALATION': {
      d: 'M {mx},{my} l {e.x0},{e.y0} l -{e.x0},-{e.y1} l -{e.x0},{e.y1} Z',
      height: 36,
      width: 36,
      heightElements: [20, 7],
      widthElements: [8]
    },
    'EVENT_CONDITIONAL': {
      d: 'M {e.x0},{e.y0} l {e.x1},0 l 0,{e.y2} l -{e.x1},0 Z ' +
         'M {e.x2},{e.y3} l {e.x0},0 ' +
         'M {e.x2},{e.y4} l {e.x0},0 ' +
         'M {e.x2},{e.y5} l {e.x0},0 ' +
         'M {e.x2},{e.y6} l {e.x0},0 ' +
         'M {e.x2},{e.y7} l {e.x0},0 ' +
         'M {e.x2},{e.y8} l {e.x0},0 ',
      height: 36,
      width:  36,
      heightElements: [8.5, 14.5, 18, 11.5, 14.5, 17.5, 20.5, 23.5, 26.5],
      widthElements:  [10.5, 14.5, 12.5]
    },
    'EVENT_LINK': {
      d: 'm {mx},{my} 0,{e.y0} -{e.x1},0 0,{e.y1} {e.x1},0 0,{e.y0} {e.x0},-{e.y2} -{e.x0},-{e.y2} z',
      height: 36,
      width: 36,
      heightElements: [4.4375, 6.75, 7.8125],
      widthElements: [9.84375, 13.5]
    },
    'EVENT_ERROR': {
      d: 'm {mx},{my} {e.x0},-{e.y0} {e.x1},-{e.y1} {e.x2},{e.y2} {e.x3},-{e.y3} -{e.x4},{e.y4} -{e.x5},-{e.y5} z',
      height: 36,
      width: 36,
      heightElements: [0.023, 8.737, 8.151, 16.564, 10.591, 8.714],
      widthElements: [0.085, 6.672, 6.97, 4.273, 5.337, 6.636]
    },
    'EVENT_CANCEL_45': {
      d: 'm {mx},{my} -{e.x1},0 0,{e.x0} {e.x1},0 0,{e.y1} {e.x0},0 ' +
        '0,-{e.y1} {e.x1},0 0,-{e.y0} -{e.x1},0 0,-{e.y1} -{e.x0},0 z',
      height: 36,
      width: 36,
      heightElements: [4.75, 8.5],
      widthElements: [4.75, 8.5]
    },
    'EVENT_COMPENSATION': {
      d: 'm {mx},{my} {e.x0},-{e.y0} 0,{e.y1} z m {e.x1},-{e.y2} {e.x2},-{e.y3} 0,{e.y1} -{e.x2},-{e.y3} z',
      height: 36,
      width: 36,
      heightElements: [6.5, 13, 0.4, 6.1],
      widthElements: [9, 9.3, 8.7]
    },
    'EVENT_TIMER_WH': {
      d: 'M {mx},{my} l {e.x0},-{e.y0} m -{e.x0},{e.y0} l {e.x1},{e.y1} ',
      height: 36,
      width:  36,
      heightElements: [10, 2],
      widthElements: [3, 7]
    },
    'EVENT_TIMER_LINE': {
      d:  'M {mx},{my} ' +
          'm {e.x0},{e.y0} l -{e.x1},{e.y1} ',
      height: 36,
      width:  36,
      heightElements: [10, 3],
      widthElements: [0, 0]
    },
    'EVENT_MULTIPLE': {
      d:'m {mx},{my} {e.x1},-{e.y0} {e.x1},{e.y0} -{e.x0},{e.y1} -{e.x2},0 z',
      height: 36,
      width:  36,
      heightElements: [6.28099, 12.56199],
      widthElements: [3.1405, 9.42149, 12.56198]
    },
    'EVENT_PARALLEL_MULTIPLE': {
      d:'m {mx},{my} {e.x0},0 0,{e.y1} {e.x1},0 0,{e.y0} -{e.x1},0 0,{e.y1} ' +
        '-{e.x0},0 0,-{e.y1} -{e.x1},0 0,-{e.y0} {e.x1},0 z',
      height: 36,
      width:  36,
      heightElements: [2.56228, 7.68683],
      widthElements: [2.56228, 7.68683]
    },
    'GATEWAY_EXCLUSIVE': {
      d:'m {mx},{my} {e.x0},{e.y0} {e.x1},{e.y0} {e.x2},0 {e.x4},{e.y2} ' +
                    '{e.x4},{e.y1} {e.x2},0 {e.x1},{e.y3} {e.x0},{e.y3} ' +
                    '{e.x3},0 {e.x5},{e.y1} {e.x5},{e.y2} {e.x3},0 z',
      height: 17.5,
      width:  17.5,
      heightElements: [8.5, 6.5312, -6.5312, -8.5],
      widthElements:  [6.5, -6.5, 3, -3, 5, -5]
    },
    'GATEWAY_PARALLEL': {
      d:'m {mx},{my} 0,{e.y1} -{e.x1},0 0,{e.y0} {e.x1},0 0,{e.y1} {e.x0},0 ' +
        '0,-{e.y1} {e.x1},0 0,-{e.y0} -{e.x1},0 0,-{e.y1} -{e.x0},0 z',
      height: 30,
      width:  30,
      heightElements: [5, 12.5],
      widthElements: [5, 12.5]
    },
    'GATEWAY_EVENT_BASED': {
      d:'m {mx},{my} {e.x0},{e.y0} {e.x0},{e.y1} {e.x1},{e.y2} {e.x2},0 z',
      height: 11,
      width:  11,
      heightElements: [-6, 6, 12, -12],
      widthElements: [9, -3, -12]
    },
    'GATEWAY_COMPLEX': {
      d:'m {mx},{my} 0,{e.y0} -{e.x0},-{e.y1} -{e.x1},{e.y2} {e.x0},{e.y1} -{e.x2},0 0,{e.y3} ' +
        '{e.x2},0  -{e.x0},{e.y1} l {e.x1},{e.y2} {e.x0},-{e.y1} 0,{e.y0} {e.x3},0 0,-{e.y0} {e.x0},{e.y1} ' +
        '{e.x1},-{e.y2} -{e.x0},-{e.y1} {e.x2},0 0,-{e.y3} -{e.x2},0 {e.x0},-{e.y1} -{e.x1},-{e.y2} ' +
        '-{e.x0},{e.y1} 0,-{e.y0} -{e.x3},0 z',
      height: 17.125,
      width:  17.125,
      heightElements: [4.875, 3.4375, 2.125, 3],
      widthElements: [3.4375, 2.125, 4.875, 3]
    },
    'DATA_OBJECT_PATH': {
      d:'m 0,0 {e.x1},0 {e.x0},{e.y0} 0,{e.y1} -{e.x2},0 0,-{e.y2} {e.x1},0 0,{e.y0} {e.x0},0',
      height: 61,
      width:  51,
      heightElements: [10, 50, 60],
      widthElements: [10, 40, 50, 60]
    },
    'DATA_OBJECT_COLLECTION_PATH': {
      d:'m {mx}, {my} ' +
        'm  0 15  l 0 -15 ' +
        'm  4 15  l 0 -15 ' +
        'm  4 15  l 0 -15 ',
      height: 61,
      width:  51,
      heightElements: [12],
      widthElements: [1, 6, 12, 15]
    },
    'DATA_ARROW': {
      d:'m 5,9 9,0 0,-3 5,5 -5,5 0,-3 -9,0 z',
      height: 61,
      width:  51,
      heightElements: [],
      widthElements: []
    },
    'DATA_STORE': {
      d:'m  {mx},{my} ' +
        'l  0,{e.y2} ' +
        'c  {e.x0},{e.y1} {e.x1},{e.y1}  {e.x2},0 ' +
        'l  0,-{e.y2} ' +
        'c -{e.x0},-{e.y1} -{e.x1},-{e.y1} -{e.x2},0' +
        'c  {e.x0},{e.y1} {e.x1},{e.y1}  {e.x2},0 ' +
        'm  -{e.x2},{e.y0}' +
        'c  {e.x0},{e.y1} {e.x1},{e.y1} {e.x2},0' +
        'm  -{e.x2},{e.y0}' +
        'c  {e.x0},{e.y1} {e.x1},{e.y1}  {e.x2},0',
      height: 61,
      width:  61,
      heightElements: [7, 10, 45],
      widthElements:  [2, 58, 60]
    },
    'TEXT_ANNOTATION': {
      d: 'm {mx}, {my} m 10,0 l -10,0 l 0,{e.y0} l 10,0',
      height: 30,
      width: 10,
      heightElements: [30],
      widthElements: [10]
    },
    'MARKER_SUB_PROCESS': {
      d: 'm{mx},{my} m 7,2 l 0,10 m -5,-5 l 10,0',
      height: 10,
      width: 10,
      heightElements: [],
      widthElements: []
    },
    'MARKER_PARALLEL': {
      d: 'm{mx},{my} m 3,2 l 0,10 m 3,-10 l 0,10 m 3,-10 l 0,10',
      height: 10,
      width: 10,
      heightElements: [],
      widthElements: []
    },
    'MARKER_SEQUENTIAL': {
      d: 'm{mx},{my} m 0,3 l 10,0 m -10,3 l 10,0 m -10,3 l 10,0',
      height: 10,
      width: 10,
      heightElements: [],
      widthElements: []
    },
    'MARKER_COMPENSATION': {
      d: 'm {mx},{my} 7,-5 0,10 z m 7.1,-0.3 6.9,-4.7 0,10 -6.9,-4.7 z',
      height: 10,
      width: 21,
      heightElements: [],
      widthElements: []
    },
    'MARKER_LOOP': {
      d: 'm {mx},{my} c 3.526979,0 6.386161,-2.829858 6.386161,-6.320661 0,-3.490806 -2.859182,-6.320661 ' +
        '-6.386161,-6.320661 -3.526978,0 -6.38616,2.829855 -6.38616,6.320661 0,1.745402 ' +
        '0.714797,3.325567 1.870463,4.469381 0.577834,0.571908 1.265885,1.034728 2.029916,1.35457 ' +
        'l -0.718163,-3.909793 m 0.718163,3.909793 -3.885211,0.802902',
      height: 13.9,
      width: 13.7,
      heightElements: [],
      widthElements: []
    },
    'MARKER_ADHOC': {
      d: 'm {mx},{my} m 0.84461,2.64411 c 1.05533,-1.23780996 2.64337,-2.07882 4.29653,-1.97997996 2.05163,0.0805 ' +
        '3.85579,1.15803 5.76082,1.79107 1.06385,0.34139996 2.24454,0.1438 3.18759,-0.43767 0.61743,-0.33642 ' +
        '1.2775,-0.64078 1.7542,-1.17511 0,0.56023 0,1.12046 0,1.6807 -0.98706,0.96237996 -2.29792,1.62393996 ' +
        '-3.6918,1.66181996 -1.24459,0.0927 -2.46671,-0.2491 -3.59505,-0.74812 -1.35789,-0.55965 ' +
        '-2.75133,-1.33436996 -4.27027,-1.18121996 -1.37741,0.14601 -2.41842,1.13685996 -3.44288,1.96782996 z',
      height: 4,
      width: 15,
      heightElements: [],
      widthElements: []
    },
    'TASK_TYPE_SEND': {
      d: 'm {mx},{my} l 0,{e.y1} l {e.x1},0 l 0,-{e.y1} z l {e.x0},{e.y0} l {e.x0},-{e.y0}',
      height: 14,
      width:  21,
      heightElements: [6, 14],
      widthElements: [10.5, 21]
    },
    'TASK_TYPE_SCRIPT': {
      d: 'm {mx},{my} c 9.966553,-6.27276 -8.000926,-7.91932 2.968968,-14.938 l -8.802728,0 ' +
        'c -10.969894,7.01868 6.997585,8.66524 -2.968967,14.938 z ' +
        'm -7,-12 l 5,0 ' +
        'm -4.5,3 l 4.5,0 ' +
        'm -3,3 l 5,0' +
        'm -4,3 l 5,0',
      height: 15,
      width:  12.6,
      heightElements: [6, 14],
      widthElements: [10.5, 21]
    },
    'TASK_TYPE_USER_1': {
      d: 'm {mx},{my} c 0.909,-0.845 1.594,-2.049 1.594,-3.385 0,-2.554 -1.805,-4.62199999 ' +
        '-4.357,-4.62199999 -2.55199998,0 -4.28799998,2.06799999 -4.28799998,4.62199999 0,1.348 ' +
        '0.974,2.562 1.89599998,3.405 -0.52899998,0.187 -5.669,2.097 -5.794,4.7560005 v 6.718 ' +
        'h 17 v -6.718 c 0,-2.2980005 -5.5279996,-4.5950005 -6.0509996,-4.7760005 z' +
        'm -8,6 l 0,5.5 m 11,0 l 0,-5'
    },
    'TASK_TYPE_USER_2': {
      d: 'm {mx},{my} m 2.162,1.009 c 0,2.4470005 -2.158,4.4310005 -4.821,4.4310005 ' +
        '-2.66499998,0 -4.822,-1.981 -4.822,-4.4310005 '
    },
    'TASK_TYPE_USER_3': {
      d: 'm {mx},{my} m -6.9,-3.80 c 0,0 2.25099998,-2.358 4.27399998,-1.177 2.024,1.181 4.221,1.537 ' +
        '4.124,0.965 -0.098,-0.57 -0.117,-3.79099999 -4.191,-4.13599999 -3.57499998,0.001 ' +
        '-4.20799998,3.36699999 -4.20699998,4.34799999 z'
    },
    'TASK_TYPE_MANUAL': {
      d: 'm {mx},{my} c 0.234,-0.01 5.604,0.008 8.029,0.004 0.808,0 1.271,-0.172 1.417,-0.752 0.227,-0.898 ' +
        '-0.334,-1.314 -1.338,-1.316 -2.467,-0.01 -7.886,-0.004 -8.108,-0.004 -0.014,-0.079 0.016,-0.533 0,-0.61 ' +
        '0.195,-0.042 8.507,0.006 9.616,0.002 0.877,-0.007 1.35,-0.438 1.353,-1.208 0.003,-0.768 -0.479,-1.09 ' +
        '-1.35,-1.091 -2.968,-0.002 -9.619,-0.013 -9.619,-0.013 v -0.591 c 0,0 5.052,-0.016 7.225,-0.016 ' +
        '0.888,-0.002 1.354,-0.416 1.351,-1.193 -0.006,-0.761 -0.492,-1.196 -1.361,-1.196 -3.473,-0.005 ' +
        '-10.86,-0.003 -11.0829995,-0.003 -0.022,-0.047 -0.045,-0.094 -0.069,-0.139 0.3939995,-0.319 ' +
        '2.0409995,-1.626 2.4149995,-2.017 0.469,-0.4870005 0.519,-1.1650005 0.162,-1.6040005 -0.414,-0.511 ' +
        '-0.973,-0.5 -1.48,-0.236 -1.4609995,0.764 -6.5999995,3.6430005 -7.7329995,4.2710005 -0.9,0.499 ' +
        '-1.516,1.253 -1.882,2.19 -0.37000002,0.95 -0.17,2.01 -0.166,2.979 0.004,0.718 -0.27300002,1.345 ' +
        '-0.055,2.063 0.629,2.087 2.425,3.312 4.859,3.318 4.6179995,0.014 9.2379995,-0.139 13.8569995,-0.158 ' +
        '0.755,-0.004 1.171,-0.301 1.182,-1.033 0.012,-0.754 -0.423,-0.969 -1.183,-0.973 -1.778,-0.01 ' +
        '-5.824,-0.004 -6.04,-0.004 10e-4,-0.084 0.003,-0.586 10e-4,-0.67 z'
    },
    'TASK_TYPE_INSTANTIATING_SEND': {
      d: 'm {mx},{my} l 0,8.4 l 12.6,0 l 0,-8.4 z l 6.3,3.6 l 6.3,-3.6'
    },
    'TASK_TYPE_SERVICE': {
      d: 'm {mx},{my} v -1.71335 c 0.352326,-0.0705 0.703932,-0.17838 1.047628,-0.32133 ' +
        '0.344416,-0.14465 0.665822,-0.32133 0.966377,-0.52145 l 1.19431,1.18005 1.567487,-1.57688 ' +
        '-1.195028,-1.18014 c 0.403376,-0.61394 0.683079,-1.29908 0.825447,-2.01824 l 1.622133,-0.01 ' +
        'v -2.2196 l -1.636514,0.01 c -0.07333,-0.35153 -0.178319,-0.70024 -0.323564,-1.04372 ' +
        '-0.145244,-0.34406 -0.321407,-0.6644 -0.522735,-0.96217 l 1.131035,-1.13631 -1.583305,-1.56293 ' +
        '-1.129598,1.13589 c -0.614052,-0.40108 -1.302883,-0.68093 -2.022633,-0.82247 l 0.0093,-1.61852 ' +
        'h -2.241173 l 0.0042,1.63124 c -0.353763,0.0736 -0.705369,0.17977 -1.049785,0.32371 -0.344415,0.14437 ' +
        '-0.665102,0.32092 -0.9635006,0.52046 l -1.1698628,-1.15823 -1.5667691,1.5792 1.1684265,1.15669 ' +
        'c -0.4026573,0.61283 -0.68308,1.29797 -0.8247287,2.01713 l -1.6588041,0.003 v 2.22174 ' +
        'l 1.6724648,-0.006 c 0.073327,0.35077 0.1797598,0.70243 0.3242851,1.04472 0.1452428,0.34448 ' +
        '0.3214064,0.6644 0.5227339,0.96066 l -1.1993431,1.19723 1.5840256,1.56011 1.1964668,-1.19348 ' +
        'c 0.6140517,0.40346 1.3028827,0.68232 2.0233517,0.82331 l 7.19e-4,1.69892 h 2.226848 z ' +
        'm 0.221462,-3.9957 c -1.788948,0.7502 -3.8576,-0.0928 -4.6097055,-1.87438 -0.7521065,-1.78321 ' +
        '0.090598,-3.84627 1.8802645,-4.59604 1.78823,-0.74936 3.856881,0.0929 4.608987,1.87437 ' +
        '0.752106,1.78165 -0.0906,3.84612 -1.879546,4.59605 z'
    },
    'TASK_TYPE_SERVICE_FILL': {
      d: 'm {mx},{my} c -1.788948,0.7502 -3.8576,-0.0928 -4.6097055,-1.87438 -0.7521065,-1.78321 ' +
        '0.090598,-3.84627 1.8802645,-4.59604 1.78823,-0.74936 3.856881,0.0929 4.608987,1.87437 ' +
        '0.752106,1.78165 -0.0906,3.84612 -1.879546,4.59605 z'
    },
    'TASK_TYPE_BUSINESS_RULE_HEADER': {
      d: 'm {mx},{my} 0,4 20,0 0,-4 z'
    },
    'TASK_TYPE_BUSINESS_RULE_MAIN': {
      d: 'm {mx},{my} 0,12 20,0 0,-12 z' +
        'm 0,8 l 20,0 ' +
        'm -13,-4 l 0,8'
    },
    'MESSAGE_FLOW_MARKER': {
      d: 'm {mx},{my} m -10.5 ,-7 l 0,14 l 21,0 l 0,-14 z l 10.5,6 l 10.5,-6'
    }
  };

  this.getRawPath = function getRawPath(pathId) {
    return this.pathMap[pathId].d;
  };

  /**
   * Scales the path to the given height and width.
   * <h1>Use case</h1>
   * <p>Use case is to scale the content of elements (event, gateways) based
   * on the element bounding box's size.
   * </p>
   * <h1>Why not transform</h1>
   * <p>Scaling a path with transform() will also scale the stroke and IE does not support
   * the option 'non-scaling-stroke' to prevent this.
   * Also there are use cases where only some parts of a path should be
   * scaled.</p>
   *
   * @param {String} pathId The ID of the path.
   * @param {Object} param <p>
   *   Example param object scales the path to 60% size of the container (data.width, data.height).
   *   <pre>
   *   {
   *     xScaleFactor: 0.6,
   *     yScaleFactor:0.6,
   *     containerWidth: data.width,
   *     containerHeight: data.height,
   *     position: {
   *       mx: 0.46,
   *       my: 0.2,
   *     }
   *   }
   *   </pre>
   *   <ul>
   *    <li>targetpathwidth = xScaleFactor * containerWidth</li>
   *    <li>targetpathheight = yScaleFactor * containerHeight</li>
   *    <li>Position is used to set the starting coordinate of the path. M is computed:
    *    <ul>
    *      <li>position.x * containerWidth</li>
    *      <li>position.y * containerHeight</li>
    *    </ul>
    *    Center of the container <pre> position: {
   *       mx: 0.5,
   *       my: 0.5,
   *     }</pre>
   *     Upper left corner of the container
   *     <pre> position: {
   *       mx: 0.0,
   *       my: 0.0,
   *     }</pre>
   *    </li>
   *   </ul>
   * </p>
   *
   */
  this.getScaledPath = function getScaledPath(pathId, param) {
    var rawPath = this.pathMap[pathId];

    // positioning
    // compute the start point of the path
    var mx, my;

    if (param.abspos) {
      mx = param.abspos.x;
      my = param.abspos.y;
    } else {
      mx = param.containerWidth * param.position.mx;
      my = param.containerHeight * param.position.my;
    }

    var coordinates = {}; //map for the scaled coordinates
    if (param.position) {

      // path
      var heightRatio = (param.containerHeight / rawPath.height) * param.yScaleFactor;
      var widthRatio = (param.containerWidth / rawPath.width) * param.xScaleFactor;


      //Apply height ratio
      for (var heightIndex = 0; heightIndex < rawPath.heightElements.length; heightIndex++) {
        coordinates['y' + heightIndex] = rawPath.heightElements[heightIndex] * heightRatio;
      }

      //Apply width ratio
      for (var widthIndex = 0; widthIndex < rawPath.widthElements.length; widthIndex++) {
        coordinates['x' + widthIndex] = rawPath.widthElements[widthIndex] * widthRatio;
      }
    }

    //Apply value to raw path
    var path = format(
      rawPath.d, {
        mx: mx,
        my: my,
        e: coordinates
      }
    );
    return path;
  };
}

module.exports = PathMap;

////////// helpers //////////

// copied from https://github.com/adobe-webplatform/Snap.svg/blob/master/src/svg.js
var tokenRegex = /\{([^\}]+)\}/g,
    objNotationRegex = /(?:(?:^|\.)(.+?)(?=\[|\.|$|\()|\[('|")(.+?)\2\])(\(\))?/g; // matches .xxxxx or ["xxxxx"] to run over object properties

function replacer(all, key, obj) {
  var res = obj;
  key.replace(objNotationRegex, function(all, name, quote, quotedName, isFunc) {
    name = name || quotedName;
    if (res) {
      if (name in res) {
        res = res[name];
      }
      typeof res == 'function' && isFunc && (res = res());
    }
  });
  res = (res == null || res == obj ? all : res) + '';

  return res;
}

function format(str, obj) {
  return String(str).replace(tokenRegex, function(all, key) {
    return replacer(all, key, obj);
  });
}

},{}],5:[function(_dereq_,module,exports){
module.exports = {
  __init__: [ 'bpmnRenderer' ],
  bpmnRenderer: [ 'type', _dereq_(3) ],
  pathMap: [ 'type', _dereq_(4) ]
};

},{"3":3,"4":4}],6:[function(_dereq_,module,exports){
'use strict';

var assign = _dereq_(190),
    map = _dereq_(80);

var LabelUtil = _dereq_(12);

var TextUtil = _dereq_(64);

var is = _dereq_(13).is;

var hasExternalLabel = LabelUtil.hasExternalLabel,
    getExternalLabelBounds = LabelUtil.getExternalLabelBounds,
    isExpanded = _dereq_(11).isExpanded,
    elementToString = _dereq_(9).elementToString;


function elementData(semantic, attrs) {
  return assign({
    id: semantic.id,
    type: semantic.$type,
    businessObject: semantic
  }, attrs);
}

function collectWaypoints(waypoints) {
  return map(waypoints, function(p) {
    return { x: p.x, y: p.y };
  });
}

function notYetDrawn(translate, semantic, refSemantic, property) {
  return new Error(translate('element {element} referenced by {referenced}#{property} not yet drawn', {
    element: elementToString(refSemantic),
    referenced: elementToString(semantic),
    property: property
  }));
}


/**
 * An importer that adds bpmn elements to the canvas
 *
 * @param {EventBus} eventBus
 * @param {Canvas} canvas
 * @param {ElementFactory} elementFactory
 * @param {ElementRegistry} elementRegistry
 */
function BpmnImporter(eventBus, canvas, elementFactory, elementRegistry, translate) {
  this._eventBus = eventBus;
  this._canvas = canvas;

  this._elementFactory = elementFactory;
  this._elementRegistry = elementRegistry;
  this._translate = translate;

  this._textUtil = new TextUtil();
}

BpmnImporter.$inject = [ 'eventBus', 'canvas', 'elementFactory', 'elementRegistry', 'translate' ];

module.exports = BpmnImporter;


/**
 * Add bpmn element (semantic) to the canvas onto the
 * specified parent shape.
 */
BpmnImporter.prototype.add = function(semantic, parentElement) {

  var di = semantic.di,
      element,
      translate = this._translate,
      hidden;

  // ROOT ELEMENT
  // handle the special case that we deal with a
  // invisible root element (process or collaboration)
  if (is(di, 'bpmndi:BPMNPlane')) {

    // add a virtual element (not being drawn)
    element = this._elementFactory.createRoot(elementData(semantic));

    this._canvas.setRootElement(element);
  }

  // SHAPE
  else if (is(di, 'bpmndi:BPMNShape')) {

    var collapsed = !isExpanded(semantic);
    hidden = parentElement && (parentElement.hidden || parentElement.collapsed);

    var bounds = semantic.di.bounds;

    element = this._elementFactory.createShape(elementData(semantic, {
      collapsed: collapsed,
      hidden: hidden,
      x: Math.round(bounds.x),
      y: Math.round(bounds.y),
      width: Math.round(bounds.width),
      height: Math.round(bounds.height)
    }));

    if (is(semantic, 'bpmn:BoundaryEvent')) {
      this._attachBoundary(semantic, element);
    }

    this._canvas.addShape(element, parentElement);
  }

  // CONNECTION
  else if (is(di, 'bpmndi:BPMNEdge')) {

    var source = this._getSource(semantic),
        target = this._getTarget(semantic);

    hidden = parentElement && (parentElement.hidden || parentElement.collapsed);

    element = this._elementFactory.createConnection(elementData(semantic, {
      hidden: hidden,
      source: source,
      target: target,
      waypoints: collectWaypoints(semantic.di.waypoint)
    }));

    if (is(semantic, 'bpmn:DataAssociation')) {

      // render always on top; this ensures DataAssociations
      // are rendered correctly across different "hacks" people
      // love to model such as cross participant / sub process
      // associations
      parentElement = null;
    }

    this._canvas.addConnection(element, parentElement);
  } else {
    throw new Error(translate('unknown di {di} for element {semantic}', {
      di: elementToString(di),
      semantic: elementToString(semantic)
    }));
  }
  // (optional) LABEL
  if (hasExternalLabel(semantic)) {
    this.addLabel(semantic, element);
  }


  this._eventBus.fire('bpmnElement.added', { element: element });

  return element;
};


/**
 * Attach the boundary element to the given host
 *
 * @param {ModdleElement} boundarySemantic
 * @param {djs.model.Base} boundaryElement
 */
BpmnImporter.prototype._attachBoundary = function(boundarySemantic, boundaryElement) {
  var translate = this._translate;
  var hostSemantic = boundarySemantic.attachedToRef;

  if (!hostSemantic) {
    throw new Error(translate('missing {semantic}#attachedToRef', {
      semantic: elementToString(boundarySemantic)
    }));
  }

  var host = this._elementRegistry.get(hostSemantic.id),
      attachers = host && host.attachers;

  if (!host) {
    throw notYetDrawn(translate, boundarySemantic, hostSemantic, 'attachedToRef');
  }

  // wire element.host <> host.attachers
  boundaryElement.host = host;

  if (!attachers) {
    host.attachers = attachers = [];
  }

  if (attachers.indexOf(boundaryElement) === -1) {
    attachers.push(boundaryElement);
  }
};


/**
 * add label for an element
 */
BpmnImporter.prototype.addLabel = function(semantic, element) {
  var bounds,
      text,
      label;

  bounds = getExternalLabelBounds(semantic, element);

  text = semantic.name;

  if (text) {
    // get corrected bounds from actual layouted text
    bounds = getLayoutedBounds(bounds, text, this._textUtil);
  }

  label = this._elementFactory.createLabel(elementData(semantic, {
    id: semantic.id + '_label',
    labelTarget: element,
    type: 'label',
    hidden: element.hidden || !semantic.name,
    x: Math.round(bounds.x),
    y: Math.round(bounds.y),
    width: Math.round(bounds.width),
    height: Math.round(bounds.height)
  }));

  return this._canvas.addShape(label, element.parent);
};

/**
 * Return the drawn connection end based on the given side.
 *
 * @throws {Error} if the end is not yet drawn
 */
BpmnImporter.prototype._getEnd = function(semantic, side) {

  var element,
      refSemantic,
      type = semantic.$type,
      translate = this._translate;

  refSemantic = semantic[side + 'Ref'];

  // handle mysterious isMany DataAssociation#sourceRef
  if (side === 'source' && type === 'bpmn:DataInputAssociation') {
    refSemantic = refSemantic && refSemantic[0];
  }

  // fix source / target for DataInputAssociation / DataOutputAssociation
  if (side === 'source' && type === 'bpmn:DataOutputAssociation' ||
      side === 'target' && type === 'bpmn:DataInputAssociation') {

    refSemantic = semantic.$parent;
  }

  element = refSemantic && this._getElement(refSemantic);

  if (element) {
    return element;
  }

  if (refSemantic) {
    throw notYetDrawn(translate, semantic, refSemantic, side + 'Ref');
  } else {
    throw new Error(translate('{semantic}#{side} Ref not specified', {
      semantic: elementToString(semantic),
      side: side
    }));
  }
};

BpmnImporter.prototype._getSource = function(semantic) {
  return this._getEnd(semantic, 'source');
};

BpmnImporter.prototype._getTarget = function(semantic) {
  return this._getEnd(semantic, 'target');
};


BpmnImporter.prototype._getElement = function(semantic) {
  return this._elementRegistry.get(semantic.id);
};


// TODO(nikku): repeating code (search for <getLayoutedBounds>)

var EXTERNAL_LABEL_STYLE = {
  fontFamily: 'Arial, sans-serif',
  fontSize: '11px'
};

function getLayoutedBounds(bounds, text, textUtil) {

  var layoutedLabelDimensions = textUtil.getDimensions(text, {
    box: {
      width: 90,
      height: 30,
      x: bounds.width / 2 + bounds.x,
      y: bounds.height / 2 + bounds.y
    },
    style: EXTERNAL_LABEL_STYLE
  });

  // resize label shape to fit label text
  return {
    x: Math.round(bounds.x + bounds.width / 2 - layoutedLabelDimensions.width / 2),
    y: Math.round(bounds.y),
    width: Math.ceil(layoutedLabelDimensions.width),
    height: Math.ceil(layoutedLabelDimensions.height)
  };
}
},{"11":11,"12":12,"13":13,"190":190,"64":64,"80":80,"9":9}],7:[function(_dereq_,module,exports){
'use strict';

var filter = _dereq_(76),
    find = _dereq_(77),
    forEach = _dereq_(78);

var Refs = _dereq_(220);

var elementToString = _dereq_(9).elementToString;

var diRefs = new Refs({ name: 'bpmnElement', enumerable: true }, { name: 'di' });

/**
 * Returns true if an element has the given meta-model type
 *
 * @param  {ModdleElement}  element
 * @param  {String}         type
 *
 * @return {Boolean}
 */
function is(element, type) {
  return element.$instanceOf(type);
}


/**
 * Find a suitable display candidate for definitions where the DI does not
 * correctly specify one.
 */
function findDisplayCandidate(definitions) {
  return find(definitions.rootElements, function(e) {
    return is(e, 'bpmn:Process') || is(e, 'bpmn:Collaboration');
  });
}


function BpmnTreeWalker(handler, translate) {

  // list of containers already walked
  var handledElements = {};

  // list of elements to handle deferred to ensure
  // prerequisites are drawn
  var deferred = [];

  ///// Helpers /////////////////////////////////

  function contextual(fn, ctx) {
    return function(e) {
      fn(e, ctx);
    };
  }

  function handled(element) {
    handledElements[element.id] = element;
  }

  function isHandled(element) {
    return handledElements[element.id];
  }

  function visit(element, ctx) {

    var gfx = element.gfx;

    // avoid multiple rendering of elements
    if (gfx) {
      throw new Error(
        translate('already rendered {element}', { element: elementToString(element) })
      );
    }

    // call handler
    return handler.element(element, ctx);
  }

  function visitRoot(element, diagram) {
    return handler.root(element, diagram);
  }

  function visitIfDi(element, ctx) {

    try {
      var gfx = element.di && visit(element, ctx);

      handled(element);

      return gfx;
    } catch (e) {
      logError(e.message, { element: element, error: e });

      console.error(translate('failed to import {element}', { element: elementToString(element) }));
      console.error(e);
    }
  }

  function logError(message, context) {
    handler.error(message, context);
  }

  ////// DI handling ////////////////////////////

  function registerDi(di) {
    var bpmnElement = di.bpmnElement;

    if (bpmnElement) {
      if (bpmnElement.di) {
        logError(
          translate('multiple DI elements defined for {element}', {
            element: elementToString(bpmnElement)
          }),
          { element: bpmnElement  }
        );
      } else {
        diRefs.bind(bpmnElement, 'di');
        bpmnElement.di = di;
      }
    } else {
      logError(
        translate('no bpmnElement referenced in {element}', {
          element: elementToString(di)
        }),
        { element: di }
      );
    }
  }

  function handleDiagram(diagram) {
    handlePlane(diagram.plane);
  }

  function handlePlane(plane) {
    registerDi(plane);

    forEach(plane.planeElement, handlePlaneElement);
  }

  function handlePlaneElement(planeElement) {
    registerDi(planeElement);
  }


  ////// Semantic handling //////////////////////

  /**
   * Handle definitions and return the rendered diagram (if any)
   *
   * @param {ModdleElement} definitions to walk and import
   * @param {ModdleElement} [diagram] specific diagram to import and display
   *
   * @throws {Error} if no diagram to display could be found
   */
  function handleDefinitions(definitions, diagram) {
    // make sure we walk the correct bpmnElement

    var diagrams = definitions.diagrams;

    if (diagram && diagrams.indexOf(diagram) === -1) {
      throw new Error(translate('diagram not part of bpmn:Definitions'));
    }

    if (!diagram && diagrams && diagrams.length) {
      diagram = diagrams[0];
    }

    // no diagram -> nothing to import
    if (!diagram) {
      throw new Error(translate('no diagram to display'));
    }

    // load DI from selected diagram only
    handleDiagram(diagram);


    var plane = diagram.plane;

    if (!plane) {
      throw new Error(translate(
        'no plane for {element}',
        { element: elementToString(diagram) }
      ));
    }

    var rootElement = plane.bpmnElement;

    // ensure we default to a suitable display candidate (process or collaboration),
    // even if non is specified in DI
    if (!rootElement) {
      rootElement = findDisplayCandidate(definitions);

      if (!rootElement) {
        throw new Error(translate('no process or collaboration to display'));
      } else {

        logError(
          translate('correcting missing bpmnElement on {plane} to {rootElement}', {
            plane: elementToString(plane),
            rootElement: elementToString(rootElement)
          })
        );

        // correct DI on the fly
        plane.bpmnElement = rootElement;
        registerDi(plane);
      }
    }


    var ctx = visitRoot(rootElement, plane);

    if (is(rootElement, 'bpmn:Process')) {
      handleProcess(rootElement, ctx);
    } else if (is(rootElement, 'bpmn:Collaboration')) {
      handleCollaboration(rootElement, ctx);

      // force drawing of everything not yet drawn that is part of the target DI
      handleUnhandledProcesses(definitions.rootElements, ctx);
    } else {
      throw new Error(
        translate('unsupported bpmnElement for {plane}: {rootElement}', {
          plane: elementToString(plane),
          rootElement: elementToString(rootElement)
        })
      );
    }

    // handle all deferred elements
    handleDeferred(deferred);
  }

  function handleDeferred(deferred) {
    forEach(deferred, function(d) { d(); });
  }

  function handleProcess(process, context) {
    handleFlowElementsContainer(process, context);
    handleIoSpecification(process.ioSpecification, context);

    handleArtifacts(process.artifacts, context);

    // log process handled
    handled(process);
  }

  function handleUnhandledProcesses(rootElements) {

    // walk through all processes that have not yet been drawn and draw them
    // if they contain lanes with DI information.
    // we do this to pass the free-floating lane test cases in the MIWG test suite
    var processes = filter(rootElements, function(e) {
      return !isHandled(e) && is(e, 'bpmn:Process') && e.laneSets;
    });

    processes.forEach(contextual(handleProcess));
  }

  function handleMessageFlow(messageFlow, context) {
    visitIfDi(messageFlow, context);
  }

  function handleMessageFlows(messageFlows, context) {
    forEach(messageFlows, contextual(handleMessageFlow, context));
  }

  function handleDataAssociation(association, context) {
    visitIfDi(association, context);
  }

  function handleDataInput(dataInput, context) {
    visitIfDi(dataInput, context);
  }

  function handleDataOutput(dataOutput, context) {
    visitIfDi(dataOutput, context);
  }

  function handleArtifact(artifact, context) {

    // bpmn:TextAnnotation
    // bpmn:Group
    // bpmn:Association

    visitIfDi(artifact, context);
  }

  function handleArtifacts(artifacts, context) {

    forEach(artifacts, function(e) {
      if (is(e, 'bpmn:Association')) {
        deferred.push(function() {
          handleArtifact(e, context);
        });
      } else {
        handleArtifact(e, context);
      }
    });
  }

  function handleIoSpecification(ioSpecification, context) {

    if (!ioSpecification) {
      return;
    }

    forEach(ioSpecification.dataInputs, contextual(handleDataInput, context));
    forEach(ioSpecification.dataOutputs, contextual(handleDataOutput, context));
  }

  function handleSubProcess(subProcess, context) {
    handleFlowElementsContainer(subProcess, context);
    handleArtifacts(subProcess.artifacts, context);
  }

  function handleFlowNode(flowNode, context) {
    var childCtx = visitIfDi(flowNode, context);

    if (is(flowNode, 'bpmn:SubProcess')) {
      handleSubProcess(flowNode, childCtx || context);
    }

    if (is(flowNode, 'bpmn:Activity')) {
      handleIoSpecification(flowNode.ioSpecification, context);
    }

    // defer handling of associations
    // affected types:
    //
    //   * bpmn:Activity
    //   * bpmn:ThrowEvent
    //   * bpmn:CatchEvent
    //
    deferred.push(function() {
      forEach(flowNode.dataInputAssociations, contextual(handleDataAssociation, context));
      forEach(flowNode.dataOutputAssociations, contextual(handleDataAssociation, context));
    });
  }

  function handleSequenceFlow(sequenceFlow, context) {
    visitIfDi(sequenceFlow, context);
  }

  function handleDataElement(dataObject, context) {
    visitIfDi(dataObject, context);
  }

  function handleBoundaryEvent(dataObject, context) {
    visitIfDi(dataObject, context);
  }

  function handleLane(lane, context) {
    var newContext = visitIfDi(lane, context);

    if (lane.childLaneSet) {
      handleLaneSet(lane.childLaneSet, newContext || context);
    }

    wireFlowNodeRefs(lane);
  }

  function handleLaneSet(laneSet, context) {
    forEach(laneSet.lanes, contextual(handleLane, context));
  }

  function handleLaneSets(laneSets, context) {
    forEach(laneSets, contextual(handleLaneSet, context));
  }

  function handleFlowElementsContainer(container, context) {
    if (container.laneSets) {
      handleLaneSets(container.laneSets, context);
    }

    handleFlowElements(container.flowElements, context);
  }

  function handleFlowElements(flowElements, context) {
    forEach(flowElements, function(e) {
      if (is(e, 'bpmn:SequenceFlow')) {
        deferred.push(function() {
          handleSequenceFlow(e, context);
        });
      } else if (is(e, 'bpmn:BoundaryEvent')) {
        deferred.unshift(function() {
          handleBoundaryEvent(e, context);
        });
      } else if (is(e, 'bpmn:FlowNode')) {
        handleFlowNode(e, context);
      } else if (is(e, 'bpmn:DataObject')) {
        // SKIP (assume correct referencing via DataObjectReference)
      } else if (is(e, 'bpmn:DataStoreReference')) {
        handleDataElement(e, context);
      } else if (is(e, 'bpmn:DataObjectReference')) {
        handleDataElement(e, context);
      } else {
        logError(
          translate('unrecognized flowElement {element} in context {context}', {
            element: elementToString(e),
            context: (context ? elementToString(context.businessObject) : 'null')
          }),
          { element: e, context: context }
        );
      }
    });
  }

  function handleParticipant(participant, context) {
    var newCtx = visitIfDi(participant, context);

    var process = participant.processRef;
    if (process) {
      handleProcess(process, newCtx || context);
    }
  }

  function handleCollaboration(collaboration) {

    forEach(collaboration.participants, contextual(handleParticipant));

    handleArtifacts(collaboration.artifacts);

    // handle message flows latest in the process
    deferred.push(function() {
      handleMessageFlows(collaboration.messageFlows);
    });
  }


  function wireFlowNodeRefs(lane) {
    // wire the virtual flowNodeRefs <-> relationship
    forEach(lane.flowNodeRef, function(flowNode) {
      var lanes = flowNode.get('lanes');

      if (lanes) {
        lanes.push(lane);
      }
    });
  }

  ///// API ////////////////////////////////

  return {
    handleDefinitions: handleDefinitions
  };
}

module.exports = BpmnTreeWalker;
},{"220":220,"76":76,"77":77,"78":78,"9":9}],8:[function(_dereq_,module,exports){
'use strict';

var BpmnTreeWalker = _dereq_(7);


/**
 * Import the definitions into a diagram.
 *
 * Errors and warnings are reported through the specified callback.
 *
 * @param  {Diagram} diagram
 * @param  {ModdleElement} definitions
 * @param  {Function} done the callback, invoked with (err, [ warning ]) once the import is done
 */
function importBpmnDiagram(diagram, definitions, done) {

  var importer = diagram.get('bpmnImporter'),
      eventBus = diagram.get('eventBus'),
      translate = diagram.get('translate');

  var error,
      warnings = [];

  /**
   * Walk the diagram semantically, importing (=drawing)
   * all elements you encounter.
   *
   * @param {ModdleElement} definitions
   */
  function render(definitions) {

    var visitor = {

      root: function(element) {
        return importer.add(element);
      },

      element: function(element, parentShape) {
        return importer.add(element, parentShape);
      },

      error: function(message, context) {
        warnings.push({ message: message, context: context });
      }
    };

    var walker = new BpmnTreeWalker(visitor, translate);

    // traverse BPMN 2.0 document model,
    // starting at definitions
    walker.handleDefinitions(definitions);
  }

  eventBus.fire('import.render.start', { definitions: definitions });

  try {
    render(definitions);
  } catch (e) {
    error = e;
  }

  eventBus.fire('import.render.complete', {
    error: error,
    warnings: warnings
  });

  done(error, warnings);
}

module.exports.importBpmnDiagram = importBpmnDiagram;
},{"7":7}],9:[function(_dereq_,module,exports){
'use strict';

module.exports.elementToString = function(e) {
  if (!e) {
    return '<null>';
  }

  return '<' + e.$type + (e.id ? ' id="' + e.id : '') + '" />';
};
},{}],10:[function(_dereq_,module,exports){
module.exports = {
  __depends__: [
    _dereq_(52)
  ],
  bpmnImporter: [ 'type', _dereq_(6) ]
};
},{"52":52,"6":6}],11:[function(_dereq_,module,exports){
'use strict';

var is = _dereq_(13).is,
    getBusinessObject = _dereq_(13).getBusinessObject;

var forEach = _dereq_(78);

module.exports.isExpanded = function(element) {

  if (is(element, 'bpmn:CallActivity')) {
    return false;
  }

  if (is(element, 'bpmn:SubProcess')) {
    return !!getBusinessObject(element).di.isExpanded;
  }

  if (is(element, 'bpmn:Participant')) {
    return !!getBusinessObject(element).processRef;
  }

  return true;
};

module.exports.isInterrupting = function(element) {
  return element && getBusinessObject(element).isInterrupting !== false;
};

module.exports.isEventSubProcess = function(element) {
  return element && !!getBusinessObject(element).triggeredByEvent;
};

function hasEventDefinition(element, eventType) {
  var bo = getBusinessObject(element),
      hasEventDefinition = false;

  if (bo.eventDefinitions) {
    forEach(bo.eventDefinitions, function(event) {
      if (is(event, eventType)) {
        hasEventDefinition = true;
      }
    });
  }

  return hasEventDefinition;
}

module.exports.hasEventDefinition = hasEventDefinition;

module.exports.hasErrorEventDefinition = function(element) {
  return hasEventDefinition(element, 'bpmn:ErrorEventDefinition');
};

module.exports.hasEscalationEventDefinition = function(element) {
  return hasEventDefinition(element, 'bpmn:EscalationEventDefinition');
};

module.exports.hasCompensateEventDefinition = function(element) {
  return hasEventDefinition(element, 'bpmn:CompensateEventDefinition');
};

},{"13":13,"78":78}],12:[function(_dereq_,module,exports){
'use strict';

var assign = _dereq_(190);

var is = _dereq_(13).is;

var DEFAULT_LABEL_SIZE = module.exports.DEFAULT_LABEL_SIZE = {
  width: 90,
  height: 20
};

var FLOW_LABEL_INDENT = module.exports.FLOW_LABEL_INDENT = 15;


/**
 * Returns true if the given semantic has an external label
 *
 * @param {BpmnElement} semantic
 * @return {Boolean} true if has label
 */
module.exports.hasExternalLabel = function(semantic) {
  return is(semantic, 'bpmn:Event') ||
         is(semantic, 'bpmn:Gateway') ||
         is(semantic, 'bpmn:DataStoreReference') ||
         is(semantic, 'bpmn:DataObjectReference') ||
         is(semantic, 'bpmn:SequenceFlow') ||
         is(semantic, 'bpmn:MessageFlow');
};

/**
 * Get the position for sequence flow labels
 *
 * @param  {Array<Point>} waypoints
 * @return {Point} the label position
 */
function getFlowLabelPosition(waypoints) {

  // get the waypoints mid
  var mid = waypoints.length / 2 - 1;

  var first = waypoints[Math.floor(mid)];
  var second = waypoints[Math.ceil(mid + 0.01)];

  // get position
  var position = getWaypointsMid(waypoints);

  // calculate angle
  var angle = Math.atan( (second.y - first.y) / (second.x - first.x) );

  var x = position.x,
      y = position.y;

  if ( Math.abs(angle) < Math.PI / 2 ) {
    y -= FLOW_LABEL_INDENT;
  } else {
    x += FLOW_LABEL_INDENT;
  }

  return { x: x, y: y };
}

module.exports.getFlowLabelPosition = getFlowLabelPosition;

/**
 * Get the middle of a number of waypoints
 *
 * @param  {Array<Point>} waypoints
 * @return {Point} the mid point
 */
function getWaypointsMid(waypoints) {

  var mid = waypoints.length / 2 - 1;

  var first = waypoints[Math.floor(mid)];
  var second = waypoints[Math.ceil(mid + 0.01)];

  return {
    x: first.x + (second.x - first.x) / 2,
    y: first.y + (second.y - first.y) / 2
  };
}

module.exports.getWaypointsMid = getWaypointsMid;


function getExternalLabelMid(element) {

  if (element.waypoints) {
    return getFlowLabelPosition(element.waypoints);
  } else {
    return {
      x: element.x + element.width / 2,
      y: element.y + element.height + DEFAULT_LABEL_SIZE.height / 2
    };
  }
}

module.exports.getExternalLabelMid = getExternalLabelMid;


/**
 * Returns the bounds of an elements label, parsed from the elements DI or
 * generated from its bounds.
 *
 * @param {BpmnElement} semantic
 * @param {djs.model.Base} element
 */
module.exports.getExternalLabelBounds = function(semantic, element) {

  var mid,
      size,
      bounds,
      di = semantic.di,
      label = di.label;

  if (label && label.bounds) {
    bounds = label.bounds;

    size = {
      width: Math.max(DEFAULT_LABEL_SIZE.width, bounds.width),
      height: bounds.height
    };

    mid = {
      x: bounds.x + bounds.width / 2,
      y: bounds.y + bounds.height / 2
    };
  } else {

    mid = getExternalLabelMid(element);

    size = DEFAULT_LABEL_SIZE;
  }

  return assign({
    x: mid.x - size.width / 2,
    y: mid.y - size.height / 2
  }, size);
};

},{"13":13,"190":190}],13:[function(_dereq_,module,exports){
'use strict';

/**
 * Is an element of the given BPMN type?
 *
 * @param  {djs.model.Base|ModdleElement} element
 * @param  {String} type
 *
 * @return {Boolean}
 */
function is(element, type) {
  var bo = getBusinessObject(element);

  return bo && (typeof bo.$instanceOf === 'function') && bo.$instanceOf(type);
}

module.exports.is = is;


/**
 * Return the business object for a given element.
 *
 * @param  {djs.model.Base|ModdleElement} element
 *
 * @return {ModdleElement}
 */
function getBusinessObject(element) {
  return (element && element.businessObject) || element;
}

module.exports.getBusinessObject = getBusinessObject;

},{}],14:[function(_dereq_,module,exports){
/**
 * This file must not be changed or exchanged.
 *
 * @see http://bpmn.io/license for more information.
 */

'use strict';

var domify = _dereq_(204);

var domDelegate = _dereq_(203);

/* jshint -W101 */

// inlined ../resources/bpmnjs.png
var logoData = module.exports.BPMNIO_LOGO = 'iVBORw0KGgoAAAANSUhEUgAAADQAAAA0CAMAAADypuvZAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAADBQTFRFiMte9PrwldFwfcZPqtqN0+zEyOe1XLgjvuKncsJAZ70y6fXh3vDT////UrQV////G2zN+AAAABB0Uk5T////////////////////AOAjXRkAAAHDSURBVHjavJZJkoUgDEBJmAX8979tM8u3E6x20VlYJfFFMoL4vBDxATxZcakIOJTWSmxvKWVIkJ8jHvlRv1F2LFrVISCZI+tCtQx+XfewgVTfyY3plPiQEAzI3zWy+kR6NBhFBYeBuscJLOUuA2WVLpCjVIaFzrNQZArxAZKUQm6gsj37L9Cb7dnIBUKxENaaMJQqMpDXvSL+ktxdGRm2IsKgJGGPg7atwUG5CcFUEuSv+CwQqizTrvDTNXdMU2bMiDWZd8d7QIySWVRsb2vBBioxOFt4OinPBapL+neAb5KL5IJ8szOza2/DYoipUCx+CjO0Bpsv0V6mktNZ+k8rlABlWG0FrOpKYVo8DT3dBeLEjUBAj7moDogVii7nSS9QzZnFcOVBp1g2PyBQ3Vr5aIapN91VJy33HTJLC1iX2FY6F8gRdaAeIEfVONgtFCzZTmoLEdOjBDfsIOA6128gw3eu1shAajdZNAORxuQDJN5A5PbEG6gNIu24QJD5iNyRMZIr6bsHbCtCU/OaOaSvgkUyDMdDa1BXGf5HJ1To+/Ym6mCKT02Y+/Sa126ZKyd3jxhzpc1r8zVL6YM1Qy/kR4ABAFJ6iQUnivhAAAAAAElFTkSuQmCC';

/* jshint +W101 */


function css(attrs) {
  return attrs.join(';');
}

var LIGHTBOX_STYLES = css([
  'z-index: 1001',
  'position: fixed',
  'top: 0',
  'left: 0',
  'right: 0',
  'bottom: 0'
]);

var BACKDROP_STYLES = css([
  'width: 100%',
  'height: 100%',
  'background: rgba(0,0,0,0.2)'
]);

var NOTICE_STYLES = css([
  'position: absolute',
  'left: 50%',
  'top: 40%',
  'margin: 0 -130px',
  'width: 260px',
  'padding: 10px',
  'background: white',
  'border: solid 1px #AAA',
  'border-radius: 3px',
  'font-family: Helvetica, Arial, sans-serif',
  'font-size: 14px',
  'line-height: 1.2em'
]);

var LIGHTBOX_MARKUP =
  '<div class="bjs-powered-by-lightbox" style="' + LIGHTBOX_STYLES + '">' +
    '<div class="backdrop" style="' + BACKDROP_STYLES + '"></div>' +
    '<div class="notice" style="' + NOTICE_STYLES + '">' +
      '<a href="http://bpmn.io" target="_blank" style="float: left; margin-right: 10px">' +
        '<img src="data:image/png;base64,'+ logoData +'">' +
      '</a>' +
      'Web-based tooling for BPMN, DMN and CMMN diagrams ' +
      'powered by <a href="http://bpmn.io" target="_blank">bpmn.io</a>.' +
    '</div>' +
  '</div>';


var lightbox;

function open() {

  if (!lightbox) {
    lightbox = domify(LIGHTBOX_MARKUP);

    domDelegate.bind(lightbox, '.backdrop', 'click', function(event) {
      document.body.removeChild(lightbox);
    });
  }

  document.body.appendChild(lightbox);
}

module.exports.open = open;
},{"203":203,"204":204}],15:[function(_dereq_,module,exports){
module.exports = _dereq_(17);
},{"17":17}],16:[function(_dereq_,module,exports){
'use strict';

var isString = _dereq_(187),
    isFunction = _dereq_(182),
    assign = _dereq_(190);

var Moddle = _dereq_(211),
    XmlReader = _dereq_(209),
    XmlWriter = _dereq_(210);

/**
 * A sub class of {@link Moddle} with support for import and export of BPMN 2.0 xml files.
 *
 * @class BpmnModdle
 * @extends Moddle
 *
 * @param {Object|Array} packages to use for instantiating the model
 * @param {Object} [options] additional options to pass over
 */
function BpmnModdle(packages, options) {
  Moddle.call(this, packages, options);
}

BpmnModdle.prototype = Object.create(Moddle.prototype);

module.exports = BpmnModdle;


/**
 * Instantiates a BPMN model tree from a given xml string.
 *
 * @param {String}   xmlStr
 * @param {String}   [typeName='bpmn:Definitions'] name of the root element
 * @param {Object}   [options]  options to pass to the underlying reader
 * @param {Function} done       callback that is invoked with (err, result, parseContext)
 *                              once the import completes
 */
BpmnModdle.prototype.fromXML = function(xmlStr, typeName, options, done) {

  if (!isString(typeName)) {
    done = options;
    options = typeName;
    typeName = 'bpmn:Definitions';
  }

  if (isFunction(options)) {
    done = options;
    options = {};
  }

  var reader = new XmlReader(assign({ model: this, lax: true }, options));
  var rootHandler = reader.handler(typeName);

  reader.fromXML(xmlStr, rootHandler, done);
};


/**
 * Serializes a BPMN 2.0 object tree to XML.
 *
 * @param {String}   element    the root element, typically an instance of `bpmn:Definitions`
 * @param {Object}   [options]  to pass to the underlying writer
 * @param {Function} done       callback invoked with (err, xmlStr) once the import completes
 */
BpmnModdle.prototype.toXML = function(element, options, done) {

  if (isFunction(options)) {
    done = options;
    options = {};
  }

  var writer = new XmlWriter(options);
  try {
    var result = writer.toXML(element);
    done(null, result);
  } catch (e) {
    done(e);
  }
};

},{"182":182,"187":187,"190":190,"209":209,"210":210,"211":211}],17:[function(_dereq_,module,exports){
'use strict';

var assign = _dereq_(190);

var BpmnModdle = _dereq_(16);

var packages = {
  bpmn: _dereq_(19),
  bpmndi: _dereq_(20),
  dc: _dereq_(21),
  di: _dereq_(22),
  bioc: _dereq_(18)
};

module.exports = function(additionalPackages, options) {
  return new BpmnModdle(assign({}, packages, additionalPackages), options);
};

},{"16":16,"18":18,"19":19,"190":190,"20":20,"21":21,"22":22}],18:[function(_dereq_,module,exports){
module.exports={
  "name": "bpmn.io colors for BPMN",
  "uri": "http://bpmn.io/schema/bpmn/biocolor/1.0",
  "prefix": "bioc",
  "types": [
    {
      "name": "ColoredShape",
      "extends": [ "bpmndi:BPMNShape" ],
      "properties": [
        {
          "name": "stroke",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "fill",
          "isAttr": true,
          "type": "String"
        }
      ]
    },
    {
      "name": "ColoredEdge",
      "extends": [ "bpmndi:BPMNEdge" ],
      "properties": [
        {
          "name": "stroke",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "fill",
          "isAttr": true,
          "type": "String"
        }
      ]
    }
  ],
  "enumerations": [],
  "associations": []
}

},{}],19:[function(_dereq_,module,exports){
module.exports={
  "name": "BPMN20",
  "uri": "http://www.omg.org/spec/BPMN/20100524/MODEL",
  "associations": [],
  "types": [
    {
      "name": "Interface",
      "superClass": [
        "RootElement"
      ],
      "properties": [
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "operations",
          "type": "Operation",
          "isMany": true
        },
        {
          "name": "implementationRef",
          "type": "String",
          "isAttr": true
        }
      ]
    },
    {
      "name": "Operation",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "inMessageRef",
          "type": "Message",
          "isReference": true
        },
        {
          "name": "outMessageRef",
          "type": "Message",
          "isReference": true
        },
        {
          "name": "errorRef",
          "type": "Error",
          "isMany": true,
          "isReference": true
        },
        {
          "name": "implementationRef",
          "type": "String",
          "isAttr": true
        }
      ]
    },
    {
      "name": "EndPoint",
      "superClass": [
        "RootElement"
      ]
    },
    {
      "name": "Auditing",
      "superClass": [
        "BaseElement"
      ]
    },
    {
      "name": "GlobalTask",
      "superClass": [
        "CallableElement"
      ],
      "properties": [
        {
          "name": "resources",
          "type": "ResourceRole",
          "isMany": true
        }
      ]
    },
    {
      "name": "Monitoring",
      "superClass": [
        "BaseElement"
      ]
    },
    {
      "name": "Performer",
      "superClass": [
        "ResourceRole"
      ]
    },
    {
      "name": "Process",
      "superClass": [
        "FlowElementsContainer",
        "CallableElement"
      ],
      "properties": [
        {
          "name": "processType",
          "type": "ProcessType",
          "isAttr": true
        },
        {
          "name": "isClosed",
          "isAttr": true,
          "type": "Boolean"
        },
        {
          "name": "auditing",
          "type": "Auditing"
        },
        {
          "name": "monitoring",
          "type": "Monitoring"
        },
        {
          "name": "properties",
          "type": "Property",
          "isMany": true
        },
        {
          "name": "laneSets",
          "type": "LaneSet",
          "isMany": true,
          "replaces": "FlowElementsContainer#laneSets"
        },
        {
          "name": "flowElements",
          "type": "FlowElement",
          "isMany": true,
          "replaces": "FlowElementsContainer#flowElements"
        },
        {
          "name": "artifacts",
          "type": "Artifact",
          "isMany": true
        },
        {
          "name": "resources",
          "type": "ResourceRole",
          "isMany": true
        },
        {
          "name": "correlationSubscriptions",
          "type": "CorrelationSubscription",
          "isMany": true
        },
        {
          "name": "supports",
          "type": "Process",
          "isMany": true,
          "isReference": true
        },
        {
          "name": "definitionalCollaborationRef",
          "type": "Collaboration",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "isExecutable",
          "isAttr": true,
          "type": "Boolean"
        }
      ]
    },
    {
      "name": "LaneSet",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "lanes",
          "type": "Lane",
          "isMany": true
        },
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        }
      ]
    },
    {
      "name": "Lane",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "partitionElementRef",
          "type": "BaseElement",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "partitionElement",
          "type": "BaseElement"
        },
        {
          "name": "flowNodeRef",
          "type": "FlowNode",
          "isMany": true,
          "isReference": true
        },
        {
          "name": "childLaneSet",
          "type": "LaneSet",
          "xml": {
            "serialize": "xsi:type"
          }
        }
      ]
    },
    {
      "name": "GlobalManualTask",
      "superClass": [
        "GlobalTask"
      ]
    },
    {
      "name": "ManualTask",
      "superClass": [
        "Task"
      ]
    },
    {
      "name": "UserTask",
      "superClass": [
        "Task"
      ],
      "properties": [
        {
          "name": "renderings",
          "type": "Rendering",
          "isMany": true
        },
        {
          "name": "implementation",
          "isAttr": true,
          "type": "String"
        }
      ]
    },
    {
      "name": "Rendering",
      "superClass": [
        "BaseElement"
      ]
    },
    {
      "name": "HumanPerformer",
      "superClass": [
        "Performer"
      ]
    },
    {
      "name": "PotentialOwner",
      "superClass": [
        "HumanPerformer"
      ]
    },
    {
      "name": "GlobalUserTask",
      "superClass": [
        "GlobalTask"
      ],
      "properties": [
        {
          "name": "implementation",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "renderings",
          "type": "Rendering",
          "isMany": true
        }
      ]
    },
    {
      "name": "Gateway",
      "isAbstract": true,
      "superClass": [
        "FlowNode"
      ],
      "properties": [
        {
          "name": "gatewayDirection",
          "type": "GatewayDirection",
          "default": "Unspecified",
          "isAttr": true
        }
      ]
    },
    {
      "name": "EventBasedGateway",
      "superClass": [
        "Gateway"
      ],
      "properties": [
        {
          "name": "instantiate",
          "default": false,
          "isAttr": true,
          "type": "Boolean"
        },
        {
          "name": "eventGatewayType",
          "type": "EventBasedGatewayType",
          "isAttr": true,
          "default": "Exclusive"
        }
      ]
    },
    {
      "name": "ComplexGateway",
      "superClass": [
        "Gateway"
      ],
      "properties": [
        {
          "name": "activationCondition",
          "type": "Expression",
          "xml": {
            "serialize": "xsi:type"
          }
        },
        {
          "name": "default",
          "type": "SequenceFlow",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "ExclusiveGateway",
      "superClass": [
        "Gateway"
      ],
      "properties": [
        {
          "name": "default",
          "type": "SequenceFlow",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "InclusiveGateway",
      "superClass": [
        "Gateway"
      ],
      "properties": [
        {
          "name": "default",
          "type": "SequenceFlow",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "ParallelGateway",
      "superClass": [
        "Gateway"
      ]
    },
    {
      "name": "RootElement",
      "isAbstract": true,
      "superClass": [
        "BaseElement"
      ]
    },
    {
      "name": "Relationship",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "type",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "direction",
          "type": "RelationshipDirection",
          "isAttr": true
        },
        {
          "name": "source",
          "isMany": true,
          "isReference": true,
          "type": "Element"
        },
        {
          "name": "target",
          "isMany": true,
          "isReference": true,
          "type": "Element"
        }
      ]
    },
    {
      "name": "BaseElement",
      "isAbstract": true,
      "properties": [
        {
          "name": "id",
          "isAttr": true,
          "type": "String",
          "isId": true
        },
        {
          "name": "documentation",
          "type": "Documentation",
          "isMany": true
        },
        {
          "name": "extensionDefinitions",
          "type": "ExtensionDefinition",
          "isMany": true,
          "isReference": true
        },
        {
          "name": "extensionElements",
          "type": "ExtensionElements"
        }
      ]
    },
    {
      "name": "Extension",
      "properties": [
        {
          "name": "mustUnderstand",
          "default": false,
          "isAttr": true,
          "type": "Boolean"
        },
        {
          "name": "definition",
          "type": "ExtensionDefinition",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "ExtensionDefinition",
      "properties": [
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "extensionAttributeDefinitions",
          "type": "ExtensionAttributeDefinition",
          "isMany": true
        }
      ]
    },
    {
      "name": "ExtensionAttributeDefinition",
      "properties": [
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "type",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "isReference",
          "default": false,
          "isAttr": true,
          "type": "Boolean"
        },
        {
          "name": "extensionDefinition",
          "type": "ExtensionDefinition",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "ExtensionElements",
      "properties": [
        {
          "name": "valueRef",
          "isAttr": true,
          "isReference": true,
          "type": "Element"
        },
        {
          "name": "values",
          "type": "Element",
          "isMany": true
        },
        {
          "name": "extensionAttributeDefinition",
          "type": "ExtensionAttributeDefinition",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "Documentation",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "text",
          "type": "String",
          "isBody": true
        },
        {
          "name": "textFormat",
          "default": "text/plain",
          "isAttr": true,
          "type": "String"
        }
      ]
    },
    {
      "name": "Event",
      "isAbstract": true,
      "superClass": [
        "FlowNode",
        "InteractionNode"
      ],
      "properties": [
        {
          "name": "properties",
          "type": "Property",
          "isMany": true
        }
      ]
    },
    {
      "name": "IntermediateCatchEvent",
      "superClass": [
        "CatchEvent"
      ]
    },
    {
      "name": "IntermediateThrowEvent",
      "superClass": [
        "ThrowEvent"
      ]
    },
    {
      "name": "EndEvent",
      "superClass": [
        "ThrowEvent"
      ]
    },
    {
      "name": "StartEvent",
      "superClass": [
        "CatchEvent"
      ],
      "properties": [
        {
          "name": "isInterrupting",
          "default": true,
          "isAttr": true,
          "type": "Boolean"
        }
      ]
    },
    {
      "name": "ThrowEvent",
      "isAbstract": true,
      "superClass": [
        "Event"
      ],
      "properties": [
        {
          "name": "dataInputs",
          "type": "DataInput",
          "isMany": true
        },
        {
          "name": "dataInputAssociations",
          "type": "DataInputAssociation",
          "isMany": true
        },
        {
          "name": "inputSet",
          "type": "InputSet"
        },
        {
          "name": "eventDefinitions",
          "type": "EventDefinition",
          "isMany": true
        },
        {
          "name": "eventDefinitionRef",
          "type": "EventDefinition",
          "isMany": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "CatchEvent",
      "isAbstract": true,
      "superClass": [
        "Event"
      ],
      "properties": [
        {
          "name": "parallelMultiple",
          "isAttr": true,
          "type": "Boolean",
          "default": false
        },
        {
          "name": "dataOutputs",
          "type": "DataOutput",
          "isMany": true
        },
        {
          "name": "dataOutputAssociations",
          "type": "DataOutputAssociation",
          "isMany": true
        },
        {
          "name": "outputSet",
          "type": "OutputSet"
        },
        {
          "name": "eventDefinitions",
          "type": "EventDefinition",
          "isMany": true
        },
        {
          "name": "eventDefinitionRef",
          "type": "EventDefinition",
          "isMany": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "BoundaryEvent",
      "superClass": [
        "CatchEvent"
      ],
      "properties": [
        {
          "name": "cancelActivity",
          "default": true,
          "isAttr": true,
          "type": "Boolean"
        },
        {
          "name": "attachedToRef",
          "type": "Activity",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "EventDefinition",
      "isAbstract": true,
      "superClass": [
        "RootElement"
      ]
    },
    {
      "name": "CancelEventDefinition",
      "superClass": [
        "EventDefinition"
      ]
    },
    {
      "name": "ErrorEventDefinition",
      "superClass": [
        "EventDefinition"
      ],
      "properties": [
        {
          "name": "errorRef",
          "type": "Error",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "TerminateEventDefinition",
      "superClass": [
        "EventDefinition"
      ]
    },
    {
      "name": "EscalationEventDefinition",
      "superClass": [
        "EventDefinition"
      ],
      "properties": [
        {
          "name": "escalationRef",
          "type": "Escalation",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "Escalation",
      "properties": [
        {
          "name": "structureRef",
          "type": "ItemDefinition",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "escalationCode",
          "isAttr": true,
          "type": "String"
        }
      ],
      "superClass": [
        "RootElement"
      ]
    },
    {
      "name": "CompensateEventDefinition",
      "superClass": [
        "EventDefinition"
      ],
      "properties": [
        {
          "name": "waitForCompletion",
          "isAttr": true,
          "type": "Boolean"
        },
        {
          "name": "activityRef",
          "type": "Activity",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "TimerEventDefinition",
      "superClass": [
        "EventDefinition"
      ],
      "properties": [
        {
          "name": "timeDate",
          "type": "Expression",
          "xml": {
            "serialize": "xsi:type"
          }
        },
        {
          "name": "timeCycle",
          "type": "Expression",
          "xml": {
            "serialize": "xsi:type"
          }
        },
        {
          "name": "timeDuration",
          "type": "Expression",
          "xml": {
            "serialize": "xsi:type"
          }
        }
      ]
    },
    {
      "name": "LinkEventDefinition",
      "superClass": [
        "EventDefinition"
      ],
      "properties": [
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "target",
          "type": "LinkEventDefinition",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "source",
          "type": "LinkEventDefinition",
          "isMany": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "MessageEventDefinition",
      "superClass": [
        "EventDefinition"
      ],
      "properties": [
        {
          "name": "messageRef",
          "type": "Message",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "operationRef",
          "type": "Operation",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "ConditionalEventDefinition",
      "superClass": [
        "EventDefinition"
      ],
      "properties": [
        {
          "name": "condition",
          "type": "Expression",
          "xml": {
            "serialize": "xsi:type"
          }
        }
      ]
    },
    {
      "name": "SignalEventDefinition",
      "superClass": [
        "EventDefinition"
      ],
      "properties": [
        {
          "name": "signalRef",
          "type": "Signal",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "Signal",
      "superClass": [
        "RootElement"
      ],
      "properties": [
        {
          "name": "structureRef",
          "type": "ItemDefinition",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        }
      ]
    },
    {
      "name": "ImplicitThrowEvent",
      "superClass": [
        "ThrowEvent"
      ]
    },
    {
      "name": "DataState",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        }
      ]
    },
    {
      "name": "ItemAwareElement",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "itemSubjectRef",
          "type": "ItemDefinition",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "dataState",
          "type": "DataState"
        }
      ]
    },
    {
      "name": "DataAssociation",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "assignment",
          "type": "Assignment",
          "isMany": true
        },
        {
          "name": "sourceRef",
          "type": "ItemAwareElement",
          "isMany": true,
          "isReference": true
        },
        {
          "name": "targetRef",
          "type": "ItemAwareElement",
          "isReference": true
        },
        {
          "name": "transformation",
          "type": "FormalExpression",
          "xml": {
            "serialize": "property"
          }
        }
      ]
    },
    {
      "name": "DataInput",
      "superClass": [
        "ItemAwareElement"
      ],
      "properties": [
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "isCollection",
          "default": false,
          "isAttr": true,
          "type": "Boolean"
        },
        {
          "name": "inputSetRef",
          "type": "InputSet",
          "isVirtual": true,
          "isMany": true,
          "isReference": true
        },
        {
          "name": "inputSetWithOptional",
          "type": "InputSet",
          "isVirtual": true,
          "isMany": true,
          "isReference": true
        },
        {
          "name": "inputSetWithWhileExecuting",
          "type": "InputSet",
          "isVirtual": true,
          "isMany": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "DataOutput",
      "superClass": [
        "ItemAwareElement"
      ],
      "properties": [
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "isCollection",
          "default": false,
          "isAttr": true,
          "type": "Boolean"
        },
        {
          "name": "outputSetRef",
          "type": "OutputSet",
          "isVirtual": true,
          "isMany": true,
          "isReference": true
        },
        {
          "name": "outputSetWithOptional",
          "type": "OutputSet",
          "isVirtual": true,
          "isMany": true,
          "isReference": true
        },
        {
          "name": "outputSetWithWhileExecuting",
          "type": "OutputSet",
          "isVirtual": true,
          "isMany": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "InputSet",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "dataInputRefs",
          "type": "DataInput",
          "isMany": true,
          "isReference": true
        },
        {
          "name": "optionalInputRefs",
          "type": "DataInput",
          "isMany": true,
          "isReference": true
        },
        {
          "name": "whileExecutingInputRefs",
          "type": "DataInput",
          "isMany": true,
          "isReference": true
        },
        {
          "name": "outputSetRefs",
          "type": "OutputSet",
          "isMany": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "OutputSet",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "dataOutputRefs",
          "type": "DataOutput",
          "isMany": true,
          "isReference": true
        },
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "inputSetRefs",
          "type": "InputSet",
          "isMany": true,
          "isReference": true
        },
        {
          "name": "optionalOutputRefs",
          "type": "DataOutput",
          "isMany": true,
          "isReference": true
        },
        {
          "name": "whileExecutingOutputRefs",
          "type": "DataOutput",
          "isMany": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "Property",
      "superClass": [
        "ItemAwareElement"
      ],
      "properties": [
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        }
      ]
    },
    {
      "name": "DataInputAssociation",
      "superClass": [
        "DataAssociation"
      ]
    },
    {
      "name": "DataOutputAssociation",
      "superClass": [
        "DataAssociation"
      ]
    },
    {
      "name": "InputOutputSpecification",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "dataInputs",
          "type": "DataInput",
          "isMany": true
        },
        {
          "name": "dataOutputs",
          "type": "DataOutput",
          "isMany": true
        },
        {
          "name": "inputSets",
          "type": "InputSet",
          "isMany": true
        },
        {
          "name": "outputSets",
          "type": "OutputSet",
          "isMany": true
        }
      ]
    },
    {
      "name": "DataObject",
      "superClass": [
        "FlowElement",
        "ItemAwareElement"
      ],
      "properties": [
        {
          "name": "isCollection",
          "default": false,
          "isAttr": true,
          "type": "Boolean"
        }
      ]
    },
    {
      "name": "InputOutputBinding",
      "properties": [
        {
          "name": "inputDataRef",
          "type": "InputSet",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "outputDataRef",
          "type": "OutputSet",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "operationRef",
          "type": "Operation",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "Assignment",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "from",
          "type": "Expression",
          "xml": {
            "serialize": "xsi:type"
          }
        },
        {
          "name": "to",
          "type": "Expression",
          "xml": {
            "serialize": "xsi:type"
          }
        }
      ]
    },
    {
      "name": "DataStore",
      "superClass": [
        "RootElement",
        "ItemAwareElement"
      ],
      "properties": [
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "capacity",
          "isAttr": true,
          "type": "Integer"
        },
        {
          "name": "isUnlimited",
          "default": true,
          "isAttr": true,
          "type": "Boolean"
        }
      ]
    },
    {
      "name": "DataStoreReference",
      "superClass": [
        "ItemAwareElement",
        "FlowElement"
      ],
      "properties": [
        {
          "name": "dataStoreRef",
          "type": "DataStore",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "DataObjectReference",
      "superClass": [
        "ItemAwareElement",
        "FlowElement"
      ],
      "properties": [
        {
          "name": "dataObjectRef",
          "type": "DataObject",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "ConversationLink",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "sourceRef",
          "type": "InteractionNode",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "targetRef",
          "type": "InteractionNode",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        }
      ]
    },
    {
      "name": "ConversationAssociation",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "innerConversationNodeRef",
          "type": "ConversationNode",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "outerConversationNodeRef",
          "type": "ConversationNode",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "CallConversation",
      "superClass": [
        "ConversationNode"
      ],
      "properties": [
        {
          "name": "calledCollaborationRef",
          "type": "Collaboration",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "participantAssociations",
          "type": "ParticipantAssociation",
          "isMany": true
        }
      ]
    },
    {
      "name": "Conversation",
      "superClass": [
        "ConversationNode"
      ]
    },
    {
      "name": "SubConversation",
      "superClass": [
        "ConversationNode"
      ],
      "properties": [
        {
          "name": "conversationNodes",
          "type": "ConversationNode",
          "isMany": true
        }
      ]
    },
    {
      "name": "ConversationNode",
      "isAbstract": true,
      "superClass": [
        "InteractionNode",
        "BaseElement"
      ],
      "properties": [
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "participantRefs",
          "type": "Participant",
          "isMany": true,
          "isReference": true
        },
        {
          "name": "messageFlowRefs",
          "type": "MessageFlow",
          "isMany": true,
          "isReference": true
        },
        {
          "name": "correlationKeys",
          "type": "CorrelationKey",
          "isMany": true
        }
      ]
    },
    {
      "name": "GlobalConversation",
      "superClass": [
        "Collaboration"
      ]
    },
    {
      "name": "PartnerEntity",
      "superClass": [
        "RootElement"
      ],
      "properties": [
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "participantRef",
          "type": "Participant",
          "isMany": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "PartnerRole",
      "superClass": [
        "RootElement"
      ],
      "properties": [
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "participantRef",
          "type": "Participant",
          "isMany": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "CorrelationProperty",
      "superClass": [
        "RootElement"
      ],
      "properties": [
        {
          "name": "correlationPropertyRetrievalExpression",
          "type": "CorrelationPropertyRetrievalExpression",
          "isMany": true
        },
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "type",
          "type": "ItemDefinition",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "Error",
      "superClass": [
        "RootElement"
      ],
      "properties": [
        {
          "name": "structureRef",
          "type": "ItemDefinition",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "errorCode",
          "isAttr": true,
          "type": "String"
        }
      ]
    },
    {
      "name": "CorrelationKey",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "correlationPropertyRef",
          "type": "CorrelationProperty",
          "isMany": true,
          "isReference": true
        },
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        }
      ]
    },
    {
      "name": "Expression",
      "superClass": [
        "BaseElement"
      ],
      "isAbstract": false,
      "properties": [
        {
          "name": "body",
          "type": "String",
          "isBody": true
        }
      ]
    },
    {
      "name": "FormalExpression",
      "superClass": [
        "Expression"
      ],
      "properties": [
        {
          "name": "language",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "evaluatesToTypeRef",
          "type": "ItemDefinition",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "Message",
      "superClass": [
        "RootElement"
      ],
      "properties": [
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "itemRef",
          "type": "ItemDefinition",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "ItemDefinition",
      "superClass": [
        "RootElement"
      ],
      "properties": [
        {
          "name": "itemKind",
          "type": "ItemKind",
          "isAttr": true
        },
        {
          "name": "structureRef",
          "type": "String",
          "isAttr": true
        },
        {
          "name": "isCollection",
          "default": false,
          "isAttr": true,
          "type": "Boolean"
        },
        {
          "name": "import",
          "type": "Import",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "FlowElement",
      "isAbstract": true,
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "auditing",
          "type": "Auditing"
        },
        {
          "name": "monitoring",
          "type": "Monitoring"
        },
        {
          "name": "categoryValueRef",
          "type": "CategoryValue",
          "isMany": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "SequenceFlow",
      "superClass": [
        "FlowElement"
      ],
      "properties": [
        {
          "name": "isImmediate",
          "isAttr": true,
          "type": "Boolean"
        },
        {
          "name": "conditionExpression",
          "type": "Expression",
          "xml": {
            "serialize": "xsi:type"
          }
        },
        {
          "name": "sourceRef",
          "type": "FlowNode",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "targetRef",
          "type": "FlowNode",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "FlowElementsContainer",
      "isAbstract": true,
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "laneSets",
          "type": "LaneSet",
          "isMany": true
        },
        {
          "name": "flowElements",
          "type": "FlowElement",
          "isMany": true
        }
      ]
    },
    {
      "name": "CallableElement",
      "isAbstract": true,
      "superClass": [
        "RootElement"
      ],
      "properties": [
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "ioSpecification",
          "type": "InputOutputSpecification",
          "xml": {
            "serialize": "property"
          }
        },
        {
          "name": "supportedInterfaceRef",
          "type": "Interface",
          "isMany": true,
          "isReference": true
        },
        {
          "name": "ioBinding",
          "type": "InputOutputBinding",
          "isMany": true,
          "xml": {
            "serialize": "property"
          }
        }
      ]
    },
    {
      "name": "FlowNode",
      "isAbstract": true,
      "superClass": [
        "FlowElement"
      ],
      "properties": [
        {
          "name": "incoming",
          "type": "SequenceFlow",
          "isMany": true,
          "isReference": true
        },
        {
          "name": "outgoing",
          "type": "SequenceFlow",
          "isMany": true,
          "isReference": true
        },
        {
          "name": "lanes",
          "type": "Lane",
          "isVirtual": true,
          "isMany": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "CorrelationPropertyRetrievalExpression",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "messagePath",
          "type": "FormalExpression"
        },
        {
          "name": "messageRef",
          "type": "Message",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "CorrelationPropertyBinding",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "dataPath",
          "type": "FormalExpression"
        },
        {
          "name": "correlationPropertyRef",
          "type": "CorrelationProperty",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "Resource",
      "superClass": [
        "RootElement"
      ],
      "properties": [
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "resourceParameters",
          "type": "ResourceParameter",
          "isMany": true
        }
      ]
    },
    {
      "name": "ResourceParameter",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "isRequired",
          "isAttr": true,
          "type": "Boolean"
        },
        {
          "name": "type",
          "type": "ItemDefinition",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "CorrelationSubscription",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "correlationKeyRef",
          "type": "CorrelationKey",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "correlationPropertyBinding",
          "type": "CorrelationPropertyBinding",
          "isMany": true
        }
      ]
    },
    {
      "name": "MessageFlow",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "sourceRef",
          "type": "InteractionNode",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "targetRef",
          "type": "InteractionNode",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "messageRef",
          "type": "Message",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "MessageFlowAssociation",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "innerMessageFlowRef",
          "type": "MessageFlow",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "outerMessageFlowRef",
          "type": "MessageFlow",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "InteractionNode",
      "isAbstract": true,
      "properties": [
        {
          "name": "incomingConversationLinks",
          "type": "ConversationLink",
          "isVirtual": true,
          "isMany": true,
          "isReference": true
        },
        {
          "name": "outgoingConversationLinks",
          "type": "ConversationLink",
          "isVirtual": true,
          "isMany": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "Participant",
      "superClass": [
        "InteractionNode",
        "BaseElement"
      ],
      "properties": [
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "interfaceRef",
          "type": "Interface",
          "isMany": true,
          "isReference": true
        },
        {
          "name": "participantMultiplicity",
          "type": "ParticipantMultiplicity"
        },
        {
          "name": "endPointRefs",
          "type": "EndPoint",
          "isMany": true,
          "isReference": true
        },
        {
          "name": "processRef",
          "type": "Process",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "ParticipantAssociation",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "innerParticipantRef",
          "type": "Participant",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "outerParticipantRef",
          "type": "Participant",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "ParticipantMultiplicity",
      "properties": [
        {
          "name": "minimum",
          "default": 0,
          "isAttr": true,
          "type": "Integer"
        },
        {
          "name": "maximum",
          "default": 1,
          "isAttr": true,
          "type": "Integer"
        }
      ]
    },
    {
      "name": "Collaboration",
      "superClass": [
        "RootElement"
      ],
      "properties": [
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "isClosed",
          "isAttr": true,
          "type": "Boolean"
        },
        {
          "name": "participants",
          "type": "Participant",
          "isMany": true
        },
        {
          "name": "messageFlows",
          "type": "MessageFlow",
          "isMany": true
        },
        {
          "name": "artifacts",
          "type": "Artifact",
          "isMany": true
        },
        {
          "name": "conversations",
          "type": "ConversationNode",
          "isMany": true
        },
        {
          "name": "conversationAssociations",
          "type": "ConversationAssociation"
        },
        {
          "name": "participantAssociations",
          "type": "ParticipantAssociation",
          "isMany": true
        },
        {
          "name": "messageFlowAssociations",
          "type": "MessageFlowAssociation",
          "isMany": true
        },
        {
          "name": "correlationKeys",
          "type": "CorrelationKey",
          "isMany": true
        },
        {
          "name": "choreographyRef",
          "type": "Choreography",
          "isMany": true,
          "isReference": true
        },
        {
          "name": "conversationLinks",
          "type": "ConversationLink",
          "isMany": true
        }
      ]
    },
    {
      "name": "ChoreographyActivity",
      "isAbstract": true,
      "superClass": [
        "FlowNode"
      ],
      "properties": [
        {
          "name": "participantRefs",
          "type": "Participant",
          "isMany": true,
          "isReference": true
        },
        {
          "name": "initiatingParticipantRef",
          "type": "Participant",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "correlationKeys",
          "type": "CorrelationKey",
          "isMany": true
        },
        {
          "name": "loopType",
          "type": "ChoreographyLoopType",
          "default": "None",
          "isAttr": true
        }
      ]
    },
    {
      "name": "CallChoreography",
      "superClass": [
        "ChoreographyActivity"
      ],
      "properties": [
        {
          "name": "calledChoreographyRef",
          "type": "Choreography",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "participantAssociations",
          "type": "ParticipantAssociation",
          "isMany": true
        }
      ]
    },
    {
      "name": "SubChoreography",
      "superClass": [
        "ChoreographyActivity",
        "FlowElementsContainer"
      ],
      "properties": [
        {
          "name": "artifacts",
          "type": "Artifact",
          "isMany": true
        }
      ]
    },
    {
      "name": "ChoreographyTask",
      "superClass": [
        "ChoreographyActivity"
      ],
      "properties": [
        {
          "name": "messageFlowRef",
          "type": "MessageFlow",
          "isMany": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "Choreography",
      "superClass": [
        "FlowElementsContainer",
        "Collaboration"
      ]
    },
    {
      "name": "GlobalChoreographyTask",
      "superClass": [
        "Choreography"
      ],
      "properties": [
        {
          "name": "initiatingParticipantRef",
          "type": "Participant",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "TextAnnotation",
      "superClass": [
        "Artifact"
      ],
      "properties": [
        {
          "name": "text",
          "type": "String"
        },
        {
          "name": "textFormat",
          "default": "text/plain",
          "isAttr": true,
          "type": "String"
        }
      ]
    },
    {
      "name": "Group",
      "superClass": [
        "Artifact"
      ],
      "properties": [
        {
          "name": "categoryValueRef",
          "type": "CategoryValue",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "Association",
      "superClass": [
        "Artifact"
      ],
      "properties": [
        {
          "name": "associationDirection",
          "type": "AssociationDirection",
          "isAttr": true
        },
        {
          "name": "sourceRef",
          "type": "BaseElement",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "targetRef",
          "type": "BaseElement",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "Category",
      "superClass": [
        "RootElement"
      ],
      "properties": [
        {
          "name": "categoryValue",
          "type": "CategoryValue",
          "isMany": true
        },
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        }
      ]
    },
    {
      "name": "Artifact",
      "isAbstract": true,
      "superClass": [
        "BaseElement"
      ]
    },
    {
      "name": "CategoryValue",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "categorizedFlowElements",
          "type": "FlowElement",
          "isVirtual": true,
          "isMany": true,
          "isReference": true
        },
        {
          "name": "value",
          "isAttr": true,
          "type": "String"
        }
      ]
    },
    {
      "name": "Activity",
      "isAbstract": true,
      "superClass": [
        "FlowNode"
      ],
      "properties": [
        {
          "name": "isForCompensation",
          "default": false,
          "isAttr": true,
          "type": "Boolean"
        },
        {
          "name": "default",
          "type": "SequenceFlow",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "ioSpecification",
          "type": "InputOutputSpecification",
          "xml": {
            "serialize": "property"
          }
        },
        {
          "name": "boundaryEventRefs",
          "type": "BoundaryEvent",
          "isMany": true,
          "isReference": true
        },
        {
          "name": "properties",
          "type": "Property",
          "isMany": true
        },
        {
          "name": "dataInputAssociations",
          "type": "DataInputAssociation",
          "isMany": true
        },
        {
          "name": "dataOutputAssociations",
          "type": "DataOutputAssociation",
          "isMany": true
        },
        {
          "name": "startQuantity",
          "default": 1,
          "isAttr": true,
          "type": "Integer"
        },
        {
          "name": "resources",
          "type": "ResourceRole",
          "isMany": true
        },
        {
          "name": "completionQuantity",
          "default": 1,
          "isAttr": true,
          "type": "Integer"
        },
        {
          "name": "loopCharacteristics",
          "type": "LoopCharacteristics"
        }
      ]
    },
    {
      "name": "ServiceTask",
      "superClass": [
        "Task"
      ],
      "properties": [
        {
          "name": "implementation",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "operationRef",
          "type": "Operation",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "SubProcess",
      "superClass": [
        "Activity",
        "FlowElementsContainer",
        "InteractionNode"
      ],
      "properties": [
        {
          "name": "triggeredByEvent",
          "default": false,
          "isAttr": true,
          "type": "Boolean"
        },
        {
          "name": "artifacts",
          "type": "Artifact",
          "isMany": true
        }
      ]
    },
    {
      "name": "LoopCharacteristics",
      "isAbstract": true,
      "superClass": [
        "BaseElement"
      ]
    },
    {
      "name": "MultiInstanceLoopCharacteristics",
      "superClass": [
        "LoopCharacteristics"
      ],
      "properties": [
        {
          "name": "isSequential",
          "default": false,
          "isAttr": true,
          "type": "Boolean"
        },
        {
          "name": "behavior",
          "type": "MultiInstanceBehavior",
          "default": "All",
          "isAttr": true
        },
        {
          "name": "loopCardinality",
          "type": "Expression",
          "xml": {
            "serialize": "xsi:type"
          }
        },
        {
          "name": "loopDataInputRef",
          "type": "ItemAwareElement",
          "isReference": true
        },
        {
          "name": "loopDataOutputRef",
          "type": "ItemAwareElement",
          "isReference": true
        },
        {
          "name": "inputDataItem",
          "type": "DataInput",
          "xml": {
            "serialize": "property"
          }
        },
        {
          "name": "outputDataItem",
          "type": "DataOutput",
          "xml": {
            "serialize": "property"
          }
        },
        {
          "name": "complexBehaviorDefinition",
          "type": "ComplexBehaviorDefinition",
          "isMany": true
        },
        {
          "name": "completionCondition",
          "type": "Expression",
          "xml": {
            "serialize": "xsi:type"
          }
        },
        {
          "name": "oneBehaviorEventRef",
          "type": "EventDefinition",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "noneBehaviorEventRef",
          "type": "EventDefinition",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "StandardLoopCharacteristics",
      "superClass": [
        "LoopCharacteristics"
      ],
      "properties": [
        {
          "name": "testBefore",
          "default": false,
          "isAttr": true,
          "type": "Boolean"
        },
        {
          "name": "loopCondition",
          "type": "Expression",
          "xml": {
            "serialize": "xsi:type"
          }
        },
        {
          "name": "loopMaximum",
          "type": "Expression",
          "xml": {
            "serialize": "xsi:type"
          }
        }
      ]
    },
    {
      "name": "CallActivity",
      "superClass": [
        "Activity"
      ],
      "properties": [
        {
          "name": "calledElement",
          "type": "String",
          "isAttr": true
        }
      ]
    },
    {
      "name": "Task",
      "superClass": [
        "Activity",
        "InteractionNode"
      ]
    },
    {
      "name": "SendTask",
      "superClass": [
        "Task"
      ],
      "properties": [
        {
          "name": "implementation",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "operationRef",
          "type": "Operation",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "messageRef",
          "type": "Message",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "ReceiveTask",
      "superClass": [
        "Task"
      ],
      "properties": [
        {
          "name": "implementation",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "instantiate",
          "default": false,
          "isAttr": true,
          "type": "Boolean"
        },
        {
          "name": "operationRef",
          "type": "Operation",
          "isAttr": true,
          "isReference": true
        },
        {
          "name": "messageRef",
          "type": "Message",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "ScriptTask",
      "superClass": [
        "Task"
      ],
      "properties": [
        {
          "name": "scriptFormat",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "script",
          "type": "String"
        }
      ]
    },
    {
      "name": "BusinessRuleTask",
      "superClass": [
        "Task"
      ],
      "properties": [
        {
          "name": "implementation",
          "isAttr": true,
          "type": "String"
        }
      ]
    },
    {
      "name": "AdHocSubProcess",
      "superClass": [
        "SubProcess"
      ],
      "properties": [
        {
          "name": "completionCondition",
          "type": "Expression",
          "xml": {
            "serialize": "xsi:type"
          }
        },
        {
          "name": "ordering",
          "type": "AdHocOrdering",
          "isAttr": true
        },
        {
          "name": "cancelRemainingInstances",
          "default": true,
          "isAttr": true,
          "type": "Boolean"
        }
      ]
    },
    {
      "name": "Transaction",
      "superClass": [
        "SubProcess"
      ],
      "properties": [
        {
          "name": "protocol",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "method",
          "isAttr": true,
          "type": "String"
        }
      ]
    },
    {
      "name": "GlobalScriptTask",
      "superClass": [
        "GlobalTask"
      ],
      "properties": [
        {
          "name": "scriptLanguage",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "script",
          "isAttr": true,
          "type": "String"
        }
      ]
    },
    {
      "name": "GlobalBusinessRuleTask",
      "superClass": [
        "GlobalTask"
      ],
      "properties": [
        {
          "name": "implementation",
          "isAttr": true,
          "type": "String"
        }
      ]
    },
    {
      "name": "ComplexBehaviorDefinition",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "condition",
          "type": "FormalExpression"
        },
        {
          "name": "event",
          "type": "ImplicitThrowEvent"
        }
      ]
    },
    {
      "name": "ResourceRole",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "resourceRef",
          "type": "Resource",
          "isReference": true
        },
        {
          "name": "resourceParameterBindings",
          "type": "ResourceParameterBinding",
          "isMany": true
        },
        {
          "name": "resourceAssignmentExpression",
          "type": "ResourceAssignmentExpression"
        },
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        }
      ]
    },
    {
      "name": "ResourceParameterBinding",
      "properties": [
        {
          "name": "expression",
          "type": "Expression",
          "xml": {
            "serialize": "xsi:type"
          }
        },
        {
          "name": "parameterRef",
          "type": "ResourceParameter",
          "isAttr": true,
          "isReference": true
        }
      ]
    },
    {
      "name": "ResourceAssignmentExpression",
      "properties": [
        {
          "name": "expression",
          "type": "Expression",
          "xml": {
            "serialize": "xsi:type"
          }
        }
      ],
      "superClass": [
        "BaseElement"
      ]
    },
    {
      "name": "Import",
      "properties": [
        {
          "name": "importType",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "location",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "namespace",
          "isAttr": true,
          "type": "String"
        }
      ]
    },
    {
      "name": "Definitions",
      "superClass": [
        "BaseElement"
      ],
      "properties": [
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "targetNamespace",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "expressionLanguage",
          "default": "http://www.w3.org/1999/XPath",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "typeLanguage",
          "default": "http://www.w3.org/2001/XMLSchema",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "imports",
          "type": "Import",
          "isMany": true
        },
        {
          "name": "extensions",
          "type": "Extension",
          "isMany": true
        },
        {
          "name": "rootElements",
          "type": "RootElement",
          "isMany": true
        },
        {
          "name": "diagrams",
          "isMany": true,
          "type": "bpmndi:BPMNDiagram"
        },
        {
          "name": "exporter",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "relationships",
          "type": "Relationship",
          "isMany": true
        },
        {
          "name": "exporterVersion",
          "isAttr": true,
          "type": "String"
        }
      ]
    }
  ],
  "enumerations": [
    {
      "name": "ProcessType",
      "literalValues": [
        {
          "name": "None"
        },
        {
          "name": "Public"
        },
        {
          "name": "Private"
        }
      ]
    },
    {
      "name": "GatewayDirection",
      "literalValues": [
        {
          "name": "Unspecified"
        },
        {
          "name": "Converging"
        },
        {
          "name": "Diverging"
        },
        {
          "name": "Mixed"
        }
      ]
    },
    {
      "name": "EventBasedGatewayType",
      "literalValues": [
        {
          "name": "Parallel"
        },
        {
          "name": "Exclusive"
        }
      ]
    },
    {
      "name": "RelationshipDirection",
      "literalValues": [
        {
          "name": "None"
        },
        {
          "name": "Forward"
        },
        {
          "name": "Backward"
        },
        {
          "name": "Both"
        }
      ]
    },
    {
      "name": "ItemKind",
      "literalValues": [
        {
          "name": "Physical"
        },
        {
          "name": "Information"
        }
      ]
    },
    {
      "name": "ChoreographyLoopType",
      "literalValues": [
        {
          "name": "None"
        },
        {
          "name": "Standard"
        },
        {
          "name": "MultiInstanceSequential"
        },
        {
          "name": "MultiInstanceParallel"
        }
      ]
    },
    {
      "name": "AssociationDirection",
      "literalValues": [
        {
          "name": "None"
        },
        {
          "name": "One"
        },
        {
          "name": "Both"
        }
      ]
    },
    {
      "name": "MultiInstanceBehavior",
      "literalValues": [
        {
          "name": "None"
        },
        {
          "name": "One"
        },
        {
          "name": "All"
        },
        {
          "name": "Complex"
        }
      ]
    },
    {
      "name": "AdHocOrdering",
      "literalValues": [
        {
          "name": "Parallel"
        },
        {
          "name": "Sequential"
        }
      ]
    }
  ],
  "prefix": "bpmn",
  "xml": {
    "tagAlias": "lowerCase",
    "typePrefix": "t"
  }
}
},{}],20:[function(_dereq_,module,exports){
module.exports={
  "name": "BPMNDI",
  "uri": "http://www.omg.org/spec/BPMN/20100524/DI",
  "types": [
    {
      "name": "BPMNDiagram",
      "properties": [
        {
          "name": "plane",
          "type": "BPMNPlane",
          "redefines": "di:Diagram#rootElement"
        },
        {
          "name": "labelStyle",
          "type": "BPMNLabelStyle",
          "isMany": true
        }
      ],
      "superClass": [
        "di:Diagram"
      ]
    },
    {
      "name": "BPMNPlane",
      "properties": [
        {
          "name": "bpmnElement",
          "isAttr": true,
          "isReference": true,
          "type": "bpmn:BaseElement",
          "redefines": "di:DiagramElement#modelElement"
        }
      ],
      "superClass": [
        "di:Plane"
      ]
    },
    {
      "name": "BPMNShape",
      "properties": [
        {
          "name": "bpmnElement",
          "isAttr": true,
          "isReference": true,
          "type": "bpmn:BaseElement",
          "redefines": "di:DiagramElement#modelElement"
        },
        {
          "name": "isHorizontal",
          "isAttr": true,
          "type": "Boolean"
        },
        {
          "name": "isExpanded",
          "isAttr": true,
          "type": "Boolean"
        },
        {
          "name": "isMarkerVisible",
          "isAttr": true,
          "type": "Boolean"
        },
        {
          "name": "label",
          "type": "BPMNLabel"
        },
        {
          "name": "isMessageVisible",
          "isAttr": true,
          "type": "Boolean"
        },
        {
          "name": "participantBandKind",
          "type": "ParticipantBandKind",
          "isAttr": true
        },
        {
          "name": "choreographyActivityShape",
          "type": "BPMNShape",
          "isAttr": true,
          "isReference": true
        }
      ],
      "superClass": [
        "di:LabeledShape"
      ]
    },
    {
      "name": "BPMNEdge",
      "properties": [
        {
          "name": "label",
          "type": "BPMNLabel"
        },
        {
          "name": "bpmnElement",
          "isAttr": true,
          "isReference": true,
          "type": "bpmn:BaseElement",
          "redefines": "di:DiagramElement#modelElement"
        },
        {
          "name": "sourceElement",
          "isAttr": true,
          "isReference": true,
          "type": "di:DiagramElement",
          "redefines": "di:Edge#source"
        },
        {
          "name": "targetElement",
          "isAttr": true,
          "isReference": true,
          "type": "di:DiagramElement",
          "redefines": "di:Edge#target"
        },
        {
          "name": "messageVisibleKind",
          "type": "MessageVisibleKind",
          "isAttr": true,
          "default": "initiating"
        }
      ],
      "superClass": [
        "di:LabeledEdge"
      ]
    },
    {
      "name": "BPMNLabel",
      "properties": [
        {
          "name": "labelStyle",
          "type": "BPMNLabelStyle",
          "isAttr": true,
          "isReference": true,
          "redefines": "di:DiagramElement#style"
        }
      ],
      "superClass": [
        "di:Label"
      ]
    },
    {
      "name": "BPMNLabelStyle",
      "properties": [
        {
          "name": "font",
          "type": "dc:Font"
        }
      ],
      "superClass": [
        "di:Style"
      ]
    }
  ],
  "enumerations": [
    {
      "name": "ParticipantBandKind",
      "literalValues": [
        {
          "name": "top_initiating"
        },
        {
          "name": "middle_initiating"
        },
        {
          "name": "bottom_initiating"
        },
        {
          "name": "top_non_initiating"
        },
        {
          "name": "middle_non_initiating"
        },
        {
          "name": "bottom_non_initiating"
        }
      ]
    },
    {
      "name": "MessageVisibleKind",
      "literalValues": [
        {
          "name": "initiating"
        },
        {
          "name": "non_initiating"
        }
      ]
    }
  ],
  "associations": [],
  "prefix": "bpmndi"
}
},{}],21:[function(_dereq_,module,exports){
module.exports={
  "name": "DC",
  "uri": "http://www.omg.org/spec/DD/20100524/DC",
  "types": [
    {
      "name": "Boolean"
    },
    {
      "name": "Integer"
    },
    {
      "name": "Real"
    },
    {
      "name": "String"
    },
    {
      "name": "Font",
      "properties": [
        {
          "name": "name",
          "type": "String",
          "isAttr": true
        },
        {
          "name": "size",
          "type": "Real",
          "isAttr": true
        },
        {
          "name": "isBold",
          "type": "Boolean",
          "isAttr": true
        },
        {
          "name": "isItalic",
          "type": "Boolean",
          "isAttr": true
        },
        {
          "name": "isUnderline",
          "type": "Boolean",
          "isAttr": true
        },
        {
          "name": "isStrikeThrough",
          "type": "Boolean",
          "isAttr": true
        }
      ]
    },
    {
      "name": "Point",
      "properties": [
        {
          "name": "x",
          "type": "Real",
          "default": "0",
          "isAttr": true
        },
        {
          "name": "y",
          "type": "Real",
          "default": "0",
          "isAttr": true
        }
      ]
    },
    {
      "name": "Bounds",
      "properties": [
        {
          "name": "x",
          "type": "Real",
          "default": "0",
          "isAttr": true
        },
        {
          "name": "y",
          "type": "Real",
          "default": "0",
          "isAttr": true
        },
        {
          "name": "width",
          "type": "Real",
          "isAttr": true
        },
        {
          "name": "height",
          "type": "Real",
          "isAttr": true
        }
      ]
    }
  ],
  "prefix": "dc",
  "associations": []
}
},{}],22:[function(_dereq_,module,exports){
module.exports={
  "name": "DI",
  "uri": "http://www.omg.org/spec/DD/20100524/DI",
  "types": [
    {
      "name": "DiagramElement",
      "isAbstract": true,
      "properties": [
        {
          "name": "id",
          "type": "String",
          "isAttr": true,
          "isId": true
        },
        {
          "name": "extension",
          "type": "Extension"
        },
        {
          "name": "owningDiagram",
          "type": "Diagram",
          "isReadOnly": true,
          "isVirtual": true,
          "isReference": true
        },
        {
          "name": "owningElement",
          "type": "DiagramElement",
          "isReadOnly": true,
          "isVirtual": true,
          "isReference": true
        },
        {
          "name": "modelElement",
          "isReadOnly": true,
          "isVirtual": true,
          "isReference": true,
          "type": "Element"
        },
        {
          "name": "style",
          "type": "Style",
          "isReadOnly": true,
          "isVirtual": true,
          "isReference": true
        },
        {
          "name": "ownedElement",
          "type": "DiagramElement",
          "isReadOnly": true,
          "isVirtual": true,
          "isMany": true
        }
      ]
    },
    {
      "name": "Node",
      "isAbstract": true,
      "superClass": [
        "DiagramElement"
      ]
    },
    {
      "name": "Edge",
      "isAbstract": true,
      "superClass": [
        "DiagramElement"
      ],
      "properties": [
        {
          "name": "source",
          "type": "DiagramElement",
          "isReadOnly": true,
          "isVirtual": true,
          "isReference": true
        },
        {
          "name": "target",
          "type": "DiagramElement",
          "isReadOnly": true,
          "isVirtual": true,
          "isReference": true
        },
        {
          "name": "waypoint",
          "isUnique": false,
          "isMany": true,
          "type": "dc:Point",
          "xml": {
            "serialize": "xsi:type"
          }
        }
      ]
    },
    {
      "name": "Diagram",
      "isAbstract": true,
      "properties": [
        {
          "name": "id",
          "type": "String",
          "isAttr": true,
          "isId": true
        },
        {
          "name": "rootElement",
          "type": "DiagramElement",
          "isReadOnly": true,
          "isVirtual": true
        },
        {
          "name": "name",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "documentation",
          "isAttr": true,
          "type": "String"
        },
        {
          "name": "resolution",
          "isAttr": true,
          "type": "Real"
        },
        {
          "name": "ownedStyle",
          "type": "Style",
          "isReadOnly": true,
          "isVirtual": true,
          "isMany": true
        }
      ]
    },
    {
      "name": "Shape",
      "isAbstract": true,
      "superClass": [
        "Node"
      ],
      "properties": [
        {
          "name": "bounds",
          "type": "dc:Bounds"
        }
      ]
    },
    {
      "name": "Plane",
      "isAbstract": true,
      "superClass": [
        "Node"
      ],
      "properties": [
        {
          "name": "planeElement",
          "type": "DiagramElement",
          "subsettedProperty": "DiagramElement-ownedElement",
          "isMany": true
        }
      ]
    },
    {
      "name": "LabeledEdge",
      "isAbstract": true,
      "superClass": [
        "Edge"
      ],
      "properties": [
        {
          "name": "ownedLabel",
          "type": "Label",
          "isReadOnly": true,
          "subsettedProperty": "DiagramElement-ownedElement",
          "isVirtual": true,
          "isMany": true
        }
      ]
    },
    {
      "name": "LabeledShape",
      "isAbstract": true,
      "superClass": [
        "Shape"
      ],
      "properties": [
        {
          "name": "ownedLabel",
          "type": "Label",
          "isReadOnly": true,
          "subsettedProperty": "DiagramElement-ownedElement",
          "isVirtual": true,
          "isMany": true
        }
      ]
    },
    {
      "name": "Label",
      "isAbstract": true,
      "superClass": [
        "Node"
      ],
      "properties": [
        {
          "name": "bounds",
          "type": "dc:Bounds"
        }
      ]
    },
    {
      "name": "Style",
      "isAbstract": true,
      "properties": [
        {
          "name": "id",
          "type": "String",
          "isAttr": true,
          "isId": true
        }
      ]
    },
    {
      "name": "Extension",
      "properties": [
        {
          "name": "values",
          "type": "Element",
          "isMany": true
        }
      ]
    }
  ],
  "associations": [],
  "prefix": "di",
  "xml": {
    "tagAlias": "lowerCase"
  }
}
},{}],23:[function(_dereq_,module,exports){
/**
 * Module dependencies.
 */

try {
  var index = _dereq_(27);
} catch (err) {
  var index = _dereq_(27);
}

/**
 * Whitespace regexp.
 */

var re = /\s+/;

/**
 * toString reference.
 */

var toString = Object.prototype.toString;

/**
 * Wrap `el` in a `ClassList`.
 *
 * @param {Element} el
 * @return {ClassList}
 * @api public
 */

module.exports = function(el){
  return new ClassList(el);
};

/**
 * Initialize a new ClassList for `el`.
 *
 * @param {Element} el
 * @api private
 */

function ClassList(el) {
  if (!el || !el.nodeType) {
    throw new Error('A DOM element reference is required');
  }
  this.el = el;
  this.list = el.classList;
}

/**
 * Add class `name` if not already present.
 *
 * @param {String} name
 * @return {ClassList}
 * @api public
 */

ClassList.prototype.add = function(name){
  // classList
  if (this.list) {
    this.list.add(name);
    return this;
  }

  // fallback
  var arr = this.array();
  var i = index(arr, name);
  if (!~i) arr.push(name);
  this.el.className = arr.join(' ');
  return this;
};

/**
 * Remove class `name` when present, or
 * pass a regular expression to remove
 * any which match.
 *
 * @param {String|RegExp} name
 * @return {ClassList}
 * @api public
 */

ClassList.prototype.remove = function(name){
  if ('[object RegExp]' == toString.call(name)) {
    return this.removeMatching(name);
  }

  // classList
  if (this.list) {
    this.list.remove(name);
    return this;
  }

  // fallback
  var arr = this.array();
  var i = index(arr, name);
  if (~i) arr.splice(i, 1);
  this.el.className = arr.join(' ');
  return this;
};

/**
 * Remove all classes matching `re`.
 *
 * @param {RegExp} re
 * @return {ClassList}
 * @api private
 */

ClassList.prototype.removeMatching = function(re){
  var arr = this.array();
  for (var i = 0; i < arr.length; i++) {
    if (re.test(arr[i])) {
      this.remove(arr[i]);
    }
  }
  return this;
};

/**
 * Toggle class `name`, can force state via `force`.
 *
 * For browsers that support classList, but do not support `force` yet,
 * the mistake will be detected and corrected.
 *
 * @param {String} name
 * @param {Boolean} force
 * @return {ClassList}
 * @api public
 */

ClassList.prototype.toggle = function(name, force){
  // classList
  if (this.list) {
    if ("undefined" !== typeof force) {
      if (force !== this.list.toggle(name, force)) {
        this.list.toggle(name); // toggle again to correct
      }
    } else {
      this.list.toggle(name);
    }
    return this;
  }

  // fallback
  if ("undefined" !== typeof force) {
    if (!force) {
      this.remove(name);
    } else {
      this.add(name);
    }
  } else {
    if (this.has(name)) {
      this.remove(name);
    } else {
      this.add(name);
    }
  }

  return this;
};

/**
 * Return an array of classes.
 *
 * @return {Array}
 * @api public
 */

ClassList.prototype.array = function(){
  var className = this.el.getAttribute('class') || '';
  var str = className.replace(/^\s+|\s+$/g, '');
  var arr = str.split(re);
  if ('' === arr[0]) arr.shift();
  return arr;
};

/**
 * Check if class `name` is present.
 *
 * @param {String} name
 * @return {ClassList}
 * @api public
 */

ClassList.prototype.has =
ClassList.prototype.contains = function(name){
  return this.list
    ? this.list.contains(name)
    : !! ~index(this.array(), name);
};

},{"27":27}],24:[function(_dereq_,module,exports){
var matches = _dereq_(28)

module.exports = function (element, selector, checkYoSelf, root) {
  element = checkYoSelf ? {parentNode: element} : element

  root = root || document

  // Make sure `element !== document` and `element != null`
  // otherwise we get an illegal invocation
  while ((element = element.parentNode) && element !== document) {
    if (matches(element, selector))
      return element
    // After `matches` on the edge case that
    // the selector matches the root
    // (when the root is not the document)
    if (element === root)
      return
  }
}

},{"28":28}],25:[function(_dereq_,module,exports){
/**
 * Module dependencies.
 */

try {
  var closest = _dereq_(24);
} catch(err) {
  var closest = _dereq_(24);
}

try {
  var event = _dereq_(26);
} catch(err) {
  var event = _dereq_(26);
}

/**
 * Delegate event `type` to `selector`
 * and invoke `fn(e)`. A callback function
 * is returned which may be passed to `.unbind()`.
 *
 * @param {Element} el
 * @param {String} selector
 * @param {String} type
 * @param {Function} fn
 * @param {Boolean} capture
 * @return {Function}
 * @api public
 */

exports.bind = function(el, selector, type, fn, capture){
  return event.bind(el, type, function(e){
    var target = e.target || e.srcElement;
    e.delegateTarget = closest(target, selector, true, el);
    if (e.delegateTarget) fn.call(el, e);
  }, capture);
};

/**
 * Unbind event `type`'s callback `fn`.
 *
 * @param {Element} el
 * @param {String} type
 * @param {Function} fn
 * @param {Boolean} capture
 * @api public
 */

exports.unbind = function(el, type, fn, capture){
  event.unbind(el, type, fn, capture);
};

},{"24":24,"26":26}],26:[function(_dereq_,module,exports){
var bind = window.addEventListener ? 'addEventListener' : 'attachEvent',
    unbind = window.removeEventListener ? 'removeEventListener' : 'detachEvent',
    prefix = bind !== 'addEventListener' ? 'on' : '';

/**
 * Bind `el` event `type` to `fn`.
 *
 * @param {Element} el
 * @param {String} type
 * @param {Function} fn
 * @param {Boolean} capture
 * @return {Function}
 * @api public
 */

exports.bind = function(el, type, fn, capture){
  el[bind](prefix + type, fn, capture || false);
  return fn;
};

/**
 * Unbind `el` event `type`'s callback `fn`.
 *
 * @param {Element} el
 * @param {String} type
 * @param {Function} fn
 * @param {Boolean} capture
 * @return {Function}
 * @api public
 */

exports.unbind = function(el, type, fn, capture){
  el[unbind](prefix + type, fn, capture || false);
  return fn;
};
},{}],27:[function(_dereq_,module,exports){
module.exports = function(arr, obj){
  if (arr.indexOf) return arr.indexOf(obj);
  for (var i = 0; i < arr.length; ++i) {
    if (arr[i] === obj) return i;
  }
  return -1;
};
},{}],28:[function(_dereq_,module,exports){
/**
 * Module dependencies.
 */

try {
  var query = _dereq_(29);
} catch (err) {
  var query = _dereq_(29);
}

/**
 * Element prototype.
 */

var proto = Element.prototype;

/**
 * Vendor function.
 */

var vendor = proto.matches
  || proto.webkitMatchesSelector
  || proto.mozMatchesSelector
  || proto.msMatchesSelector
  || proto.oMatchesSelector;

/**
 * Expose `match()`.
 */

module.exports = match;

/**
 * Match `el` to `selector`.
 *
 * @param {Element} el
 * @param {String} selector
 * @return {Boolean}
 * @api public
 */

function match(el, selector) {
  if (!el || el.nodeType !== 1) return false;
  if (vendor) return vendor.call(el, selector);
  var nodes = query.all(selector, el.parentNode);
  for (var i = 0; i < nodes.length; ++i) {
    if (nodes[i] == el) return true;
  }
  return false;
}

},{"29":29}],29:[function(_dereq_,module,exports){
function one(selector, el) {
  return el.querySelector(selector);
}

exports = module.exports = function(selector, el){
  el = el || document;
  return one(selector, el);
};

exports.all = function(selector, el){
  el = el || document;
  return el.querySelectorAll(selector);
};

exports.engine = function(obj){
  if (!obj.one) throw new Error('.one callback required');
  if (!obj.all) throw new Error('.all callback required');
  one = obj.one;
  exports.all = obj.all;
  return exports;
};

},{}],30:[function(_dereq_,module,exports){
module.exports = _dereq_(31);
},{"31":31}],31:[function(_dereq_,module,exports){
'use strict';

var di = _dereq_(66);


/**
 * Bootstrap an injector from a list of modules, instantiating a number of default components
 *
 * @ignore
 * @param {Array<didi.Module>} bootstrapModules
 *
 * @return {didi.Injector} a injector to use to access the components
 */
function bootstrap(bootstrapModules) {

  var modules = [],
      components = [];

  function hasModule(m) {
    return modules.indexOf(m) >= 0;
  }

  function addModule(m) {
    modules.push(m);
  }

  function visit(m) {
    if (hasModule(m)) {
      return;
    }

    (m.__depends__ || []).forEach(visit);

    if (hasModule(m)) {
      return;
    }

    addModule(m);

    (m.__init__ || []).forEach(function(c) {
      components.push(c);
    });
  }

  bootstrapModules.forEach(visit);

  var injector = new di.Injector(modules);

  components.forEach(function(c) {

    try {
      // eagerly resolve component (fn or string)
      injector[typeof c === 'string' ? 'get' : 'invoke'](c);
    } catch (e) {
      console.error('Failed to instantiate component');
      console.error(e.stack);

      throw e;
    }
  });

  return injector;
}

/**
 * Creates an injector from passed options.
 *
 * @ignore
 * @param  {Object} options
 * @return {didi.Injector}
 */
function createInjector(options) {

  options = options || {};

  var configModule = {
    'config': ['value', options]
  };

  var coreModule = _dereq_(37);

  var modules = [ configModule, coreModule ].concat(options.modules || []);

  return bootstrap(modules);
}


/**
 * The main diagram-js entry point that bootstraps the diagram with the given
 * configuration.
 *
 * To register extensions with the diagram, pass them as Array<didi.Module> to the constructor.
 *
 * @class djs.Diagram
 * @memberOf djs
 * @constructor
 *
 * @example
 *
 * <caption>Creating a plug-in that logs whenever a shape is added to the canvas.</caption>
 *
 * // plug-in implemenentation
 * function MyLoggingPlugin(eventBus) {
 *   eventBus.on('shape.added', function(event) {
 *     console.log('shape ', event.shape, ' was added to the diagram');
 *   });
 * }
 *
 * // export as module
 * module.exports = {
 *   __init__: [ 'myLoggingPlugin' ],
 *     myLoggingPlugin: [ 'type', MyLoggingPlugin ]
 * };
 *
 *
 * // instantiate the diagram with the new plug-in
 *
 * var diagram = new Diagram({ modules: [ require('path-to-my-logging-plugin') ] });
 *
 * diagram.invoke([ 'canvas', function(canvas) {
 *   // add shape to drawing canvas
 *   canvas.addShape({ x: 10, y: 10 });
 * });
 *
 * // 'shape ... was added to the diagram' logged to console
 *
 * @param {Object} options
 * @param {Array<didi.Module>} [options.modules] external modules to instantiate with the diagram
 * @param {didi.Injector} [injector] an (optional) injector to bootstrap the diagram with
 */
function Diagram(options, injector) {

  // create injector unless explicitly specified
  this.injector = injector = injector || createInjector(options);

  // API

  /**
   * Resolves a diagram service
   *
   * @method Diagram#get
   *
   * @param {String} name the name of the diagram service to be retrieved
   * @param {Boolean} [strict=true] if false, resolve missing services to null
   */
  this.get = injector.get;

  /**
   * Executes a function into which diagram services are injected
   *
   * @method Diagram#invoke
   *
   * @param {Function|Object[]} fn the function to resolve
   * @param {Object} locals a number of locals to use to resolve certain dependencies
   */
  this.invoke = injector.invoke;

  // init

  // indicate via event


  /**
   * An event indicating that all plug-ins are loaded.
   *
   * Use this event to fire other events to interested plug-ins
   *
   * @memberOf Diagram
   *
   * @event diagram.init
   *
   * @example
   *
   * eventBus.on('diagram.init', function() {
   *   eventBus.fire('my-custom-event', { foo: 'BAR' });
   * });
   *
   * @type {Object}
   */
  this.get('eventBus').fire('diagram.init');
}

module.exports = Diagram;


/**
 * Destroys the diagram
 *
 * @method  Diagram#destroy
 */
Diagram.prototype.destroy = function() {
  this.get('eventBus').fire('diagram.destroy');
};

/**
 * Clear the diagram, removing all contents.
 */
Diagram.prototype.clear = function() {
  this.get('eventBus').fire('diagram.clear');
};

},{"37":37,"66":66}],32:[function(_dereq_,module,exports){
'use strict';

var isNumber = _dereq_(184),
    assign = _dereq_(190),
    forEach = _dereq_(78),
    every = _dereq_(75),
    debounce = _dereq_(85),
    reduce = _dereq_(81);

var Collections = _dereq_(55),
    Elements = _dereq_(56);

var svgAppend = _dereq_(226),
    svgAttr = _dereq_(228),
    svgClasses = _dereq_(229),
    svgCreate = _dereq_(231),
    svgTransform = _dereq_(235);

var createMatrix = _dereq_(232).createMatrix;


function round(number, resolution) {
  return Math.round(number * resolution) / resolution;
}

function ensurePx(number) {
  return isNumber(number) ? number + 'px' : number;
}

/**
 * Creates a HTML container element for a SVG element with
 * the given configuration
 *
 * @param  {Object} options
 * @return {HTMLElement} the container element
 */
function createContainer(options) {

  options = assign({}, { width: '100%', height: '100%' }, options);

  var container = options.container || document.body;

  // create a <div> around the svg element with the respective size
  // this way we can always get the correct container size
  // (this is impossible for <svg> elements at the moment)
  var parent = document.createElement('div');
  parent.setAttribute('class', 'djs-container');

  assign(parent.style, {
    position: 'relative',
    overflow: 'hidden',
    width: ensurePx(options.width),
    height: ensurePx(options.height)
  });

  container.appendChild(parent);

  return parent;
}

function createGroup(parent, cls, childIndex) {
  var group = svgCreate('g');
  svgClasses(group).add(cls);

  var index = childIndex !== undefined ? childIndex : parent.childNodes.length - 1;

  parent.insertBefore(group, parent.childNodes[index]);

  return group;
}

var BASE_LAYER = 'base';


var REQUIRED_MODEL_ATTRS = {
  shape: [ 'x', 'y', 'width', 'height' ],
  connection: [ 'waypoints' ]
};

/**
 * The main drawing canvas.
 *
 * @class
 * @constructor
 *
 * @emits Canvas#canvas.init
 *
 * @param {Object} config
 * @param {EventBus} eventBus
 * @param {GraphicsFactory} graphicsFactory
 * @param {ElementRegistry} elementRegistry
 */
function Canvas(config, eventBus, graphicsFactory, elementRegistry) {

  this._eventBus = eventBus;
  this._elementRegistry = elementRegistry;
  this._graphicsFactory = graphicsFactory;

  this._init(config || {});
}

Canvas.$inject = [ 'config.canvas', 'eventBus', 'graphicsFactory', 'elementRegistry' ];

module.exports = Canvas;


Canvas.prototype._init = function(config) {

  var eventBus = this._eventBus;

  // Creates a <svg> element that is wrapped into a <div>.
  // This way we are always able to correctly figure out the size of the svg element
  // by querying the parent node.
  //
  // (It is not possible to get the size of a svg element cross browser @ 2014-04-01)
  //
  // <div class="djs-container" style="width: {desired-width}, height: {desired-height}">
  //   <svg width="100%" height="100%">
  //    ...
  //   </svg>
  // </div>

  // html container
  var container = this._container = createContainer(config);

  var svg = this._svg = svgCreate('svg');
  svgAttr(svg, { width: '100%', height: '100%' });

  svgAppend(container, svg);

  var viewport = this._viewport = createGroup(svg, 'viewport');

  this._layers = {};

  // debounce canvas.viewbox.changed events
  // for smoother diagram interaction
  if (config.deferUpdate !== false) {
    this._viewboxChanged = debounce(this._viewboxChanged, 300);
  }

  eventBus.on('diagram.init', function() {

    /**
     * An event indicating that the canvas is ready to be drawn on.
     *
     * @memberOf Canvas
     *
     * @event canvas.init
     *
     * @type {Object}
     * @property {SVGElement} svg the created svg element
     * @property {SVGElement} viewport the direct parent of diagram elements and shapes
     */
    eventBus.fire('canvas.init', {
      svg: svg,
      viewport: viewport
    });

    // fire this in order for certain components to check
    // if they need to be adjusted due the canvas size
    this.resized();

  }, this);

  eventBus.on('diagram.destroy', 500, this._destroy, this);
  eventBus.on('diagram.clear', 500, this._clear, this);
};

Canvas.prototype._destroy = function(emit) {
  this._eventBus.fire('canvas.destroy', {
    svg: this._svg,
    viewport: this._viewport
  });

  var parent = this._container.parentNode;

  if (parent) {
    parent.removeChild(this._container);
  }

  delete this._svg;
  delete this._container;
  delete this._layers;
  delete this._rootElement;
  delete this._viewport;
};

Canvas.prototype._clear = function() {

  var self = this;

  var allElements = this._elementRegistry.getAll();

  // remove all elements
  allElements.forEach(function(element) {
    var type = Elements.getType(element);

    if (type === 'root') {
      self.setRootElement(null, true);
    } else {
      self._removeElement(element, type);
    }
  });

  // force recomputation of view box
  delete this._cachedViewbox;
};

/**
 * Returns the default layer on which
 * all elements are drawn.
 *
 * @returns {SVGElement}
 */
Canvas.prototype.getDefaultLayer = function() {
  return this.getLayer(BASE_LAYER, 0);
};

/**
 * Returns a layer that is used to draw elements
 * or annotations on it.
 *
 * Non-existing layers retrieved through this method
 * will be created. During creation, the optional index
 * may be used to create layers below or above existing layers.
 * A layer with a certain index is always created above all
 * existing layers with the same index.
 *
 * @param {String} name
 * @param {Number} index
 *
 * @returns {SVGElement}
 */
Canvas.prototype.getLayer = function(name, index) {

  if (!name) {
    throw new Error('must specify a name');
  }

  var layer = this._layers[name];

  if (!layer) {
    layer = this._layers[name] = this._createLayer(name, index);
  }

  // throw an error if layer creation / retrival is
  // requested on different index
  if (typeof index !== 'undefined' && layer.index !== index) {
    throw new Error('layer <' + name + '> already created at index <' + index + '>');
  }

  return layer.group;
};

/**
 * Creates a given layer and returns it.
 *
 * @param {String} name
 * @param {Number} [index=0]
 *
 * @return {Object} layer descriptor with { index, group: SVGGroup }
 */
Canvas.prototype._createLayer = function(name, index) {

  if (!index) {
    index = 0;
  }

  var childIndex = reduce(this._layers, function(childIndex, layer) {
    if (index >= layer.index) {
      childIndex++;
    }

    return childIndex;
  }, 0);

  return {
    group: createGroup(this._viewport, 'layer-' + name, childIndex),
    index: index
  };

};

/**
 * Returns the html element that encloses the
 * drawing canvas.
 *
 * @return {DOMNode}
 */
Canvas.prototype.getContainer = function() {
  return this._container;
};


/////////////// markers ///////////////////////////////////

Canvas.prototype._updateMarker = function(element, marker, add) {
  var container;

  if (!element.id) {
    element = this._elementRegistry.get(element);
  }

  // we need to access all
  container = this._elementRegistry._elements[element.id];

  if (!container) {
    return;
  }

  forEach([ container.gfx, container.secondaryGfx ], function(gfx) {
    if (gfx) {
      // invoke either addClass or removeClass based on mode
      if (add) {
        svgClasses(gfx).add(marker);
      } else {
        svgClasses(gfx).remove(marker);
      }
    }
  });

  /**
   * An event indicating that a marker has been updated for an element
   *
   * @event element.marker.update
   * @type {Object}
   * @property {djs.model.Element} element the shape
   * @property {Object} gfx the graphical representation of the shape
   * @property {String} marker
   * @property {Boolean} add true if the marker was added, false if it got removed
   */
  this._eventBus.fire('element.marker.update', { element: element, gfx: container.gfx, marker: marker, add: !!add });
};


/**
 * Adds a marker to an element (basically a css class).
 *
 * Fires the element.marker.update event, making it possible to
 * integrate extension into the marker life-cycle, too.
 *
 * @example
 * canvas.addMarker('foo', 'some-marker');
 *
 * var fooGfx = canvas.getGraphics('foo');
 *
 * fooGfx; // <g class="... some-marker"> ... </g>
 *
 * @param {String|djs.model.Base} element
 * @param {String} marker
 */
Canvas.prototype.addMarker = function(element, marker) {
  this._updateMarker(element, marker, true);
};


/**
 * Remove a marker from an element.
 *
 * Fires the element.marker.update event, making it possible to
 * integrate extension into the marker life-cycle, too.
 *
 * @param  {String|djs.model.Base} element
 * @param  {String} marker
 */
Canvas.prototype.removeMarker = function(element, marker) {
  this._updateMarker(element, marker, false);
};

/**
 * Check the existence of a marker on element.
 *
 * @param  {String|djs.model.Base} element
 * @param  {String} marker
 */
Canvas.prototype.hasMarker = function(element, marker) {
  if (!element.id) {
    element = this._elementRegistry.get(element);
  }

  var gfx = this.getGraphics(element);

  return svgClasses(gfx).has(marker);
};

/**
 * Toggles a marker on an element.
 *
 * Fires the element.marker.update event, making it possible to
 * integrate extension into the marker life-cycle, too.
 *
 * @param  {String|djs.model.Base} element
 * @param  {String} marker
 */
Canvas.prototype.toggleMarker = function(element, marker) {
  if (this.hasMarker(element, marker)) {
    this.removeMarker(element, marker);
  } else {
    this.addMarker(element, marker);
  }
};

Canvas.prototype.getRootElement = function() {
  if (!this._rootElement) {
    this.setRootElement({ id: '__implicitroot', children: [] });
  }

  return this._rootElement;
};



//////////////// root element handling ///////////////////////////

/**
 * Sets a given element as the new root element for the canvas
 * and returns the new root element.
 *
 * @param {Object|djs.model.Root} element
 * @param {Boolean} [override] whether to override the current root element, if any
 *
 * @return {Object|djs.model.Root} new root element
 */
Canvas.prototype.setRootElement = function(element, override) {

  if (element) {
    this._ensureValid('root', element);
  }

  var currentRoot = this._rootElement,
      elementRegistry = this._elementRegistry,
      eventBus = this._eventBus;

  if (currentRoot) {
    if (!override) {
      throw new Error('rootElement already set, need to specify override');
    }

    // simulate element remove event sequence
    eventBus.fire('root.remove', { element: currentRoot });
    eventBus.fire('root.removed', { element: currentRoot });

    elementRegistry.remove(currentRoot);
  }

  if (element) {
    var gfx = this.getDefaultLayer();

    // resemble element add event sequence
    eventBus.fire('root.add', { element: element });

    elementRegistry.add(element, gfx, this._svg);

    eventBus.fire('root.added', { element: element, gfx: gfx });
  }

  this._rootElement = element;

  return element;
};



///////////// add functionality ///////////////////////////////

Canvas.prototype._ensureValid = function(type, element) {
  if (!element.id) {
    throw new Error('element must have an id');
  }

  if (this._elementRegistry.get(element.id)) {
    throw new Error('element with id ' + element.id + ' already exists');
  }

  var requiredAttrs = REQUIRED_MODEL_ATTRS[type];

  var valid = every(requiredAttrs, function(attr) {
    return typeof element[attr] !== 'undefined';
  });

  if (!valid) {
    throw new Error(
      'must supply { ' + requiredAttrs.join(', ') + ' } with ' + type);
  }
};

Canvas.prototype._setParent = function(element, parent, parentIndex) {
  Collections.add(parent.children, element, parentIndex);
  element.parent = parent;
};

/**
 * Adds an element to the canvas.
 *
 * This wires the parent <-> child relationship between the element and
 * a explicitly specified parent or an implicit root element.
 *
 * During add it emits the events
 *
 *  * <{type}.add> (element, parent)
 *  * <{type}.added> (element, gfx)
 *
 * Extensions may hook into these events to perform their magic.
 *
 * @param {String} type
 * @param {Object|djs.model.Base} element
 * @param {Object|djs.model.Base} [parent]
 * @param {Number} [parentIndex]
 *
 * @return {Object|djs.model.Base} the added element
 */
Canvas.prototype._addElement = function(type, element, parent, parentIndex) {

  parent = parent || this.getRootElement();

  var eventBus = this._eventBus,
      graphicsFactory = this._graphicsFactory;

  this._ensureValid(type, element);

  eventBus.fire(type + '.add', { element: element, parent: parent });

  this._setParent(element, parent, parentIndex);

  // create graphics
  var gfx = graphicsFactory.create(type, element);

  this._elementRegistry.add(element, gfx);

  // update its visual
  graphicsFactory.update(type, element, gfx);

  eventBus.fire(type + '.added', { element: element, gfx: gfx });

  return element;
};

/**
 * Adds a shape to the canvas
 *
 * @param {Object|djs.model.Shape} shape to add to the diagram
 * @param {djs.model.Base} [parent]
 * @param {Number} [parentIndex]
 *
 * @return {djs.model.Shape} the added shape
 */
Canvas.prototype.addShape = function(shape, parent, parentIndex) {
  return this._addElement('shape', shape, parent, parentIndex);
};

/**
 * Adds a connection to the canvas
 *
 * @param {Object|djs.model.Connection} connection to add to the diagram
 * @param {djs.model.Base} [parent]
 * @param {Number} [parentIndex]
 *
 * @return {djs.model.Connection} the added connection
 */
Canvas.prototype.addConnection = function(connection, parent, parentIndex) {
  return this._addElement('connection', connection, parent, parentIndex);
};


/**
 * Internal remove element
 */
Canvas.prototype._removeElement = function(element, type) {

  var elementRegistry = this._elementRegistry,
      graphicsFactory = this._graphicsFactory,
      eventBus = this._eventBus;

  element = elementRegistry.get(element.id || element);

  if (!element) {
    // element was removed already
    return;
  }

  eventBus.fire(type + '.remove', { element: element });

  graphicsFactory.remove(element);

  // unset parent <-> child relationship
  Collections.remove(element.parent && element.parent.children, element);
  element.parent = null;

  eventBus.fire(type + '.removed', { element: element });

  elementRegistry.remove(element);

  return element;
};


/**
 * Removes a shape from the canvas
 *
 * @param {String|djs.model.Shape} shape or shape id to be removed
 *
 * @return {djs.model.Shape} the removed shape
 */
Canvas.prototype.removeShape = function(shape) {

  /**
   * An event indicating that a shape is about to be removed from the canvas.
   *
   * @memberOf Canvas
   *
   * @event shape.remove
   * @type {Object}
   * @property {djs.model.Shape} element the shape descriptor
   * @property {Object} gfx the graphical representation of the shape
   */

  /**
   * An event indicating that a shape has been removed from the canvas.
   *
   * @memberOf Canvas
   *
   * @event shape.removed
   * @type {Object}
   * @property {djs.model.Shape} element the shape descriptor
   * @property {Object} gfx the graphical representation of the shape
   */
  return this._removeElement(shape, 'shape');
};


/**
 * Removes a connection from the canvas
 *
 * @param {String|djs.model.Connection} connection or connection id to be removed
 *
 * @return {djs.model.Connection} the removed connection
 */
Canvas.prototype.removeConnection = function(connection) {

  /**
   * An event indicating that a connection is about to be removed from the canvas.
   *
   * @memberOf Canvas
   *
   * @event connection.remove
   * @type {Object}
   * @property {djs.model.Connection} element the connection descriptor
   * @property {Object} gfx the graphical representation of the connection
   */

  /**
   * An event indicating that a connection has been removed from the canvas.
   *
   * @memberOf Canvas
   *
   * @event connection.removed
   * @type {Object}
   * @property {djs.model.Connection} element the connection descriptor
   * @property {Object} gfx the graphical representation of the connection
   */
  return this._removeElement(connection, 'connection');
};


/**
 * Return the graphical object underlaying a certain diagram element
 *
 * @param {String|djs.model.Base} element descriptor of the element
 * @param {Boolean} [secondary=false] whether to return the secondary connected element
 *
 * @return {SVGElement}
 */
Canvas.prototype.getGraphics = function(element, secondary) {
  return this._elementRegistry.getGraphics(element, secondary);
};


/**
 * Perform a viewbox update via a given change function.
 *
 * @param {Function} changeFn
 */
Canvas.prototype._changeViewbox = function(changeFn) {

  // notify others of the upcoming viewbox change
  this._eventBus.fire('canvas.viewbox.changing');

  // perform actual change
  changeFn.apply(this);

  // reset the cached viewbox so that
  // a new get operation on viewbox or zoom
  // triggers a viewbox re-computation
  this._cachedViewbox = null;

  // notify others of the change; this step
  // may or may not be debounced
  this._viewboxChanged();
};

Canvas.prototype._viewboxChanged = function() {
  this._eventBus.fire('canvas.viewbox.changed', { viewbox: this.viewbox() });
};


/**
 * Gets or sets the view box of the canvas, i.e. the
 * area that is currently displayed.
 *
 * The getter may return a cached viewbox (if it is currently
 * changing). To force a recomputation, pass `false` as the first argument.
 *
 * @example
 *
 * canvas.viewbox({ x: 100, y: 100, width: 500, height: 500 })
 *
 * // sets the visible area of the diagram to (100|100) -> (600|100)
 * // and and scales it according to the diagram width
 *
 * var viewbox = canvas.viewbox(); // pass `false` to force recomputing the box.
 *
 * console.log(viewbox);
 * // {
 * //   inner: Dimensions,
 * //   outer: Dimensions,
 * //   scale,
 * //   x, y,
 * //   width, height
 * // }
 *
 * // if the current diagram is zoomed and scrolled, you may reset it to the
 * // default zoom via this method, too:
 *
 * var zoomedAndScrolledViewbox = canvas.viewbox();
 *
 * canvas.viewbox({
 *   x: 0,
 *   y: 0,
 *   width: zoomedAndScrolledViewbox.outer.width,
 *   height: zoomedAndScrolledViewbox.outer.height
 * });
 *
 * @param  {Object} [box] the new view box to set
 * @param  {Number} box.x the top left X coordinate of the canvas visible in view box
 * @param  {Number} box.y the top left Y coordinate of the canvas visible in view box
 * @param  {Number} box.width the visible width
 * @param  {Number} box.height
 *
 * @return {Object} the current view box
 */
Canvas.prototype.viewbox = function(box) {

  if (box === undefined && this._cachedViewbox) {
    return this._cachedViewbox;
  }

  var viewport = this._viewport,
      innerBox,
      outerBox = this.getSize(),
      matrix,
      scale,
      x, y;

  if (!box) {
    // compute the inner box based on the
    // diagrams default layer. This allows us to exclude
    // external components, such as overlays
    innerBox = this.getDefaultLayer().getBBox();

    var transform = svgTransform(viewport);
    matrix = transform ? transform.matrix : createMatrix();
    scale = round(matrix.a, 1000);

    x = round(-matrix.e || 0, 1000);
    y = round(-matrix.f || 0, 1000);

    box = this._cachedViewbox = {
      x: x ? x / scale : 0,
      y: y ? y / scale : 0,
      width: outerBox.width / scale,
      height: outerBox.height / scale,
      scale: scale,
      inner: {
        width: innerBox.width,
        height: innerBox.height,
        x: innerBox.x,
        y: innerBox.y
      },
      outer: outerBox
    };

    return box;
  } else {

    this._changeViewbox(function() {
      scale = Math.min(outerBox.width / box.width, outerBox.height / box.height);

      var matrix = this._svg.createSVGMatrix()
        .scale(scale)
        .translate(-box.x, -box.y);

      svgTransform(viewport, matrix);
    });
  }

  return box;
};


/**
 * Gets or sets the scroll of the canvas.
 *
 * @param {Object} [delta] the new scroll to apply.
 *
 * @param {Number} [delta.dx]
 * @param {Number} [delta.dy]
 */
Canvas.prototype.scroll = function(delta) {

  var node = this._viewport;
  var matrix = node.getCTM();

  if (delta) {
    this._changeViewbox(function() {
      delta = assign({ dx: 0, dy: 0 }, delta || {});

      matrix = this._svg.createSVGMatrix().translate(delta.dx, delta.dy).multiply(matrix);

      setCTM(node, matrix);
    });
  }

  return { x: matrix.e, y: matrix.f };
};


/**
 * Gets or sets the current zoom of the canvas, optionally zooming
 * to the specified position.
 *
 * The getter may return a cached zoom level. Call it with `false` as
 * the first argument to force recomputation of the current level.
 *
 * @param {String|Number} [newScale] the new zoom level, either a number, i.e. 0.9,
 *                                   or `fit-viewport` to adjust the size to fit the current viewport
 * @param {String|Point} [center] the reference point { x: .., y: ..} to zoom to, 'auto' to zoom into mid or null
 *
 * @return {Number} the current scale
 */
Canvas.prototype.zoom = function(newScale, center) {

  if (!newScale) {
    return this.viewbox(newScale).scale;
  }

  if (newScale === 'fit-viewport') {
    return this._fitViewport(center);
  }

  var outer,
      matrix;

  this._changeViewbox(function() {

    if (typeof center !== 'object') {
      outer = this.viewbox().outer;

      center = {
        x: outer.width / 2,
        y: outer.height / 2
      };
    }

    matrix = this._setZoom(newScale, center);
  });

  return round(matrix.a, 1000);
};

function setCTM(node, m) {
  var mstr = 'matrix(' + m.a + ',' + m.b + ',' + m.c + ',' + m.d + ',' + m.e + ',' + m.f + ')';
  node.setAttribute('transform', mstr);
}

Canvas.prototype._fitViewport = function(center) {

  var vbox = this.viewbox(),
      outer = vbox.outer,
      inner = vbox.inner,
      newScale,
      newViewbox;

  // display the complete diagram without zooming in.
  // instead of relying on internal zoom, we perform a
  // hard reset on the canvas viewbox to realize this
  //
  // if diagram does not need to be zoomed in, we focus it around
  // the diagram origin instead

  if (inner.x >= 0 &&
      inner.y >= 0 &&
      inner.x + inner.width <= outer.width &&
      inner.y + inner.height <= outer.height &&
      !center) {

    newViewbox = {
      x: 0,
      y: 0,
      width: Math.max(inner.width + inner.x, outer.width),
      height: Math.max(inner.height + inner.y, outer.height)
    };
  } else {

    newScale = Math.min(1, outer.width / inner.width, outer.height / inner.height);
    newViewbox = {
      x: inner.x + (center ? inner.width / 2 - outer.width / newScale / 2 : 0),
      y: inner.y + (center ? inner.height / 2 - outer.height / newScale / 2 : 0),
      width: outer.width / newScale,
      height: outer.height / newScale
    };
  }

  this.viewbox(newViewbox);

  return this.viewbox(false).scale;
};


Canvas.prototype._setZoom = function(scale, center) {

  var svg = this._svg,
      viewport = this._viewport;

  var matrix = svg.createSVGMatrix();
  var point = svg.createSVGPoint();

  var centerPoint,
      originalPoint,
      currentMatrix,
      scaleMatrix,
      newMatrix;

  currentMatrix = viewport.getCTM();

  var currentScale = currentMatrix.a;

  if (center) {
    centerPoint = assign(point, center);

    // revert applied viewport transformations
    originalPoint = centerPoint.matrixTransform(currentMatrix.inverse());

    // create scale matrix
    scaleMatrix = matrix
                    .translate(originalPoint.x, originalPoint.y)
                    .scale(1 / currentScale * scale)
                    .translate(-originalPoint.x, -originalPoint.y);

    newMatrix = currentMatrix.multiply(scaleMatrix);
  } else {
    newMatrix = matrix.scale(scale);
  }

  setCTM(this._viewport, newMatrix);

  return newMatrix;
};


/**
 * Returns the size of the canvas
 *
 * @return {Dimensions}
 */
Canvas.prototype.getSize = function() {
  return {
    width: this._container.clientWidth,
    height: this._container.clientHeight
  };
};


/**
 * Return the absolute bounding box for the given element
 *
 * The absolute bounding box may be used to display overlays in the
 * callers (browser) coordinate system rather than the zoomed in/out
 * canvas coordinates.
 *
 * @param  {ElementDescriptor} element
 * @return {Bounds} the absolute bounding box
 */
Canvas.prototype.getAbsoluteBBox = function(element) {
  var vbox = this.viewbox();
  var bbox;

  // connection
  // use svg bbox
  if (element.waypoints) {
    var gfx = this.getGraphics(element);

    bbox = gfx.getBBox();
  }
  // shapes
  // use data
  else {
    bbox = element;
  }

  var x = bbox.x * vbox.scale - vbox.x * vbox.scale;
  var y = bbox.y * vbox.scale - vbox.y * vbox.scale;

  var width = bbox.width * vbox.scale;
  var height = bbox.height * vbox.scale;

  return {
    x: x,
    y: y,
    width: width,
    height: height
  };
};

/**
 * Fires an event in order other modules can react to the
 * canvas resizing
 */
Canvas.prototype.resized = function() {

  // force recomputation of view box
  delete this._cachedViewbox;

  this._eventBus.fire('canvas.resized');
};

},{"184":184,"190":190,"226":226,"228":228,"229":229,"231":231,"232":232,"235":235,"55":55,"56":56,"75":75,"78":78,"81":81,"85":85}],33:[function(_dereq_,module,exports){
'use strict';

var Model = _dereq_(54);

var assign = _dereq_(190);

/**
 * A factory for diagram-js shapes
 */
function ElementFactory() {
  this._uid = 12;
}

module.exports = ElementFactory;


ElementFactory.prototype.createRoot = function(attrs) {
  return this.create('root', attrs);
};

ElementFactory.prototype.createLabel = function(attrs) {
  return this.create('label', attrs);
};

ElementFactory.prototype.createShape = function(attrs) {
  return this.create('shape', attrs);
};

ElementFactory.prototype.createConnection = function(attrs) {
  return this.create('connection', attrs);
};

/**
 * Create a model element with the given type and
 * a number of pre-set attributes.
 *
 * @param  {String} type
 * @param  {Object} attrs
 * @return {djs.model.Base} the newly created model instance
 */
ElementFactory.prototype.create = function(type, attrs) {

  attrs = assign({}, attrs || {});

  if (!attrs.id) {
    attrs.id = type + '_' + (this._uid++);
  }

  return Model.create(type, attrs);
};
},{"190":190,"54":54}],34:[function(_dereq_,module,exports){
'use strict';

var ELEMENT_ID = 'data-element-id';

var svgAttr = _dereq_(228);


/**
 * @class
 *
 * A registry that keeps track of all shapes in the diagram.
 */
function ElementRegistry(eventBus) {
  this._elements = {};

  this._eventBus = eventBus;
}

ElementRegistry.$inject = [ 'eventBus' ];

module.exports = ElementRegistry;

/**
 * Register a pair of (element, gfx, (secondaryGfx)).
 *
 * @param {djs.model.Base} element
 * @param {SVGElement} gfx
 * @param {SVGElement} [secondaryGfx] optional other element to register, too
 */
ElementRegistry.prototype.add = function(element, gfx, secondaryGfx) {

  var id = element.id;

  this._validateId(id);

  // associate dom node with element
  svgAttr(gfx, ELEMENT_ID, id);

  if (secondaryGfx) {
    svgAttr(secondaryGfx, ELEMENT_ID, id);
  }

  this._elements[id] = { element: element, gfx: gfx, secondaryGfx: secondaryGfx };
};

/**
 * Removes an element from the registry.
 *
 * @param {djs.model.Base} element
 */
ElementRegistry.prototype.remove = function(element) {
  var elements = this._elements,
      id = element.id || element,
      container = id && elements[id];

  if (container) {

    // unset element id on gfx
    svgAttr(container.gfx, ELEMENT_ID, '');

    if (container.secondaryGfx) {
      svgAttr(container.secondaryGfx, ELEMENT_ID, '');
    }

    delete elements[id];
  }
};

/**
 * Update the id of an element
 *
 * @param {djs.model.Base} element
 * @param {String} newId
 */
ElementRegistry.prototype.updateId = function(element, newId) {

  this._validateId(newId);

  if (typeof element === 'string') {
    element = this.get(element);
  }

  this._eventBus.fire('element.updateId', {
    element: element,
    newId: newId
  });

  var gfx = this.getGraphics(element),
      secondaryGfx = this.getGraphics(element, true);

  this.remove(element);

  element.id = newId;

  this.add(element, gfx, secondaryGfx);
};

/**
 * Return the model element for a given id or graphics.
 *
 * @example
 *
 * elementRegistry.get('SomeElementId_1');
 * elementRegistry.get(gfx);
 *
 *
 * @param {String|SVGElement} filter for selecting the element
 *
 * @return {djs.model.Base}
 */
ElementRegistry.prototype.get = function(filter) {
  var id;

  if (typeof filter === 'string') {
    id = filter;
  } else {
    id = filter && svgAttr(filter, ELEMENT_ID);
  }

  var container = this._elements[id];
  return container && container.element;
};

/**
 * Return all elements that match a given filter function.
 *
 * @param {Function} fn
 *
 * @return {Array<djs.model.Base>}
 */
ElementRegistry.prototype.filter = function(fn) {

  var filtered = [];

  this.forEach(function(element, gfx) {
    if (fn(element, gfx)) {
      filtered.push(element);
    }
  });

  return filtered;
};

/**
 * Return all rendered model elements.
 *
 * @return {Array<djs.model.Base>}
 */
ElementRegistry.prototype.getAll = function() {
  return this.filter(function(e) { return e; });
};

/**
 * Iterate over all diagram elements.
 *
 * @param {Function} fn
 */
ElementRegistry.prototype.forEach = function(fn) {

  var map = this._elements;

  Object.keys(map).forEach(function(id) {
    var container = map[id],
        element = container.element,
        gfx = container.gfx;

    return fn(element, gfx);
  });
};

/**
 * Return the graphical representation of an element or its id.
 *
 * @example
 * elementRegistry.getGraphics('SomeElementId_1');
 * elementRegistry.getGraphics(rootElement); // <g ...>
 *
 * elementRegistry.getGraphics(rootElement, true); // <svg ...>
 *
 *
 * @param {String|djs.model.Base} filter
 * @param {Boolean} [secondary=false] whether to return the secondary connected element
 *
 * @return {SVGElement}
 */
ElementRegistry.prototype.getGraphics = function(filter, secondary) {
  var id = filter.id || filter;

  var container = this._elements[id];
  return container && (secondary ? container.secondaryGfx : container.gfx);
};

/**
 * Validate the suitability of the given id and signals a problem
 * with an exception.
 *
 * @param {String} id
 *
 * @throws {Error} if id is empty or already assigned
 */
ElementRegistry.prototype._validateId = function(id) {
  if (!id) {
    throw new Error('element must have an id');
  }

  if (this._elements[id]) {
    throw new Error('element with id ' + id + ' already added');
  }
};

},{"228":228}],35:[function(_dereq_,module,exports){
'use strict';

var isFunction = _dereq_(182),
    isArray = _dereq_(181),
    isNumber = _dereq_(184),
    bind = _dereq_(84),
    assign = _dereq_(190);

var FN_REF = '__fn';

var DEFAULT_PRIORITY = 1000;

var slice = Array.prototype.slice;

/**
 * A general purpose event bus.
 *
 * This component is used to communicate across a diagram instance.
 * Other parts of a diagram can use it to listen to and broadcast events.
 *
 *
 * ## Registering for Events
 *
 * The event bus provides the {@link EventBus#on} and {@link EventBus#once}
 * methods to register for events. {@link EventBus#off} can be used to
 * remove event registrations. Listeners receive an instance of {@link Event}
 * as the first argument. It allows them to hook into the event execution.
 *
 * ```javascript
 *
 * // listen for event
 * eventBus.on('foo', function(event) {
 *
 *   // access event type
 *   event.type; // 'foo'
 *
 *   // stop propagation to other listeners
 *   event.stopPropagation();
 *
 *   // prevent event default
 *   event.preventDefault();
 * });
 *
 * // listen for event with custom payload
 * eventBus.on('bar', function(event, payload) {
 *   console.log(payload);
 * });
 *
 * // listen for event returning value
 * eventBus.on('foobar', function(event) {
 *
 *   // stop event propagation + prevent default
 *   return false;
 *
 *   // stop event propagation + return custom result
 *   return {
 *     complex: 'listening result'
 *   };
 * });
 *
 *
 * // listen with custom priority (default=1000, higher is better)
 * eventBus.on('priorityfoo', 1500, function(event) {
 *   console.log('invoked first!');
 * });
 *
 *
 * // listen for event and pass the context (`this`)
 * eventBus.on('foobar', function(event) {
 *   this.foo();
 * }, this);
 * ```
 *
 *
 * ## Emitting Events
 *
 * Events can be emitted via the event bus using {@link EventBus#fire}.
 *
 * ```javascript
 *
 * // false indicates that the default action
 * // was prevented by listeners
 * if (eventBus.fire('foo') === false) {
 *   console.log('default has been prevented!');
 * };
 *
 *
 * // custom args + return value listener
 * eventBus.on('sum', function(event, a, b) {
 *   return a + b;
 * });
 *
 * // you can pass custom arguments + retrieve result values.
 * var sum = eventBus.fire('sum', 1, 2);
 * console.log(sum); // 3
 * ```
 */
function EventBus() {
  this._listeners = {};

  // cleanup on destroy on lowest priority to allow
  // message passing until the bitter end
  this.on('diagram.destroy', 1, this._destroy, this);
}

module.exports = EventBus;


/**
 * Register an event listener for events with the given name.
 *
 * The callback will be invoked with `event, ...additionalArguments`
 * that have been passed to {@link EventBus#fire}.
 *
 * Returning false from a listener will prevent the events default action
 * (if any is specified). To stop an event from being processed further in
 * other listeners execute {@link Event#stopPropagation}.
 *
 * Returning anything but `undefined` from a listener will stop the listener propagation.
 *
 * @param {String|Array<String>} events
 * @param {Number} [priority=1000] the priority in which this listener is called, larger is higher
 * @param {Function} callback
 * @param {Object} [that] Pass context (`this`) to the callback
 */
EventBus.prototype.on = function(events, priority, callback, that) {

  events = isArray(events) ? events : [ events ];

  if (isFunction(priority)) {
    that = callback;
    callback = priority;
    priority = DEFAULT_PRIORITY;
  }

  if (!isNumber(priority)) {
    throw new Error('priority must be a number');
  }

  var actualCallback = callback;

  if (that) {
    actualCallback = bind(callback, that);

    // make sure we remember and are able to remove
    // bound callbacks via {@link #off} using the original
    // callback
    actualCallback[FN_REF] = callback[FN_REF] || callback;
  }

  var self = this,
      listener = { priority: priority, callback: actualCallback };

  events.forEach(function(e) {
    self._addListener(e, listener);
  });
};


/**
 * Register an event listener that is executed only once.
 *
 * @param {String} event the event name to register for
 * @param {Function} callback the callback to execute
 * @param {Object} [that] Pass context (`this`) to the callback
 */
EventBus.prototype.once = function(event, priority, callback, that) {
  var self = this;

  if (isFunction(priority)) {
    that = callback;
    callback = priority;
    priority = DEFAULT_PRIORITY;
  }

  if (!isNumber(priority)) {
    throw new Error('priority must be a number');
  }

  function wrappedCallback() {
    self.off(event, wrappedCallback);
    return callback.apply(that, arguments);
  }

  // make sure we remember and are able to remove
  // bound callbacks via {@link #off} using the original
  // callback
  wrappedCallback[FN_REF] = callback;

  this.on(event, priority, wrappedCallback);
};


/**
 * Removes event listeners by event and callback.
 *
 * If no callback is given, all listeners for a given event name are being removed.
 *
 * @param {String} event
 * @param {Function} [callback]
 */
EventBus.prototype.off = function(event, callback) {
  var listeners = this._getListeners(event),
      listener,
      listenerCallback,
      idx;

  if (callback) {

    // move through listeners from back to front
    // and remove matching listeners
    for (idx = listeners.length - 1; (listener = listeners[idx]); idx--) {
      listenerCallback = listener.callback;

      if (listenerCallback === callback || listenerCallback[FN_REF] === callback) {
        listeners.splice(idx, 1);
      }
    }
  } else {
    // clear listeners
    listeners.length = 0;
  }
};


/**
 * Fires a named event.
 *
 * @example
 *
 * // fire event by name
 * events.fire('foo');
 *
 * // fire event object with nested type
 * var event = { type: 'foo' };
 * events.fire(event);
 *
 * // fire event with explicit type
 * var event = { x: 10, y: 20 };
 * events.fire('element.moved', event);
 *
 * // pass additional arguments to the event
 * events.on('foo', function(event, bar) {
 *   alert(bar);
 * });
 *
 * events.fire({ type: 'foo' }, 'I am bar!');
 *
 * @param {String} [name] the optional event name
 * @param {Object} [event] the event object
 * @param {...Object} additional arguments to be passed to the callback functions
 *
 * @return {Boolean} the events return value, if specified or false if the
 *                   default action was prevented by listeners
 */
EventBus.prototype.fire = function(type, data) {

  var event,
      listeners,
      returnValue,
      args;

  args = slice.call(arguments);

  if (typeof type === 'object') {
    event = type;
    type = event.type;
  }

  if (!type) {
    throw new Error('no event type specified');
  }

  listeners = this._listeners[type];

  if (!listeners) {
    return;
  }

  // we make sure we fire instances of our home made
  // events here. We wrap them only once, though
  if (data instanceof Event) {
    // we are fine, we alread have an event
    event = data;
  } else {
    event = new Event();
    event.init(data);
  }

  // ensure we pass the event as the first parameter
  args[0] = event;

  // original event type (in case we delegate)
  var originalType = event.type;

  // update event type before delegation
  if (type !== originalType) {
    event.type = type;
  }

  try {
    returnValue = this._invokeListeners(event, args, listeners);
  } finally {
    // reset event type after delegation
    if (type !== originalType) {
      event.type = originalType;
    }
  }

  // set the return value to false if the event default
  // got prevented and no other return value exists
  if (returnValue === undefined && event.defaultPrevented) {
    returnValue = false;
  }

  return returnValue;
};


EventBus.prototype.handleError = function(error) {
  return this.fire('error', { error: error }) === false;
};


EventBus.prototype._destroy = function() {
  this._listeners = {};
};

EventBus.prototype._invokeListeners = function(event, args, listeners) {

  var idx,
      listener,
      returnValue;

  for (idx = 0; (listener = listeners[idx]); idx++) {

    // handle stopped propagation
    if (event.cancelBubble) {
      break;
    }

    returnValue = this._invokeListener(event, args, listener);
  }

  return returnValue;
};

EventBus.prototype._invokeListener = function(event, args, listener) {

  var returnValue;

  try {
    // returning false prevents the default action
    returnValue = invokeFunction(listener.callback, args);

    // stop propagation on return value
    if (returnValue !== undefined) {
      event.returnValue = returnValue;
      event.stopPropagation();
    }

    // prevent default on return false
    if (returnValue === false) {
      event.preventDefault();
    }
  } catch (e) {
    if (!this.handleError(e)) {
      console.error('unhandled error in event listener');
      console.error(e.stack);

      throw e;
    }
  }

  return returnValue;
};

/*
 * Add new listener with a certain priority to the list
 * of listeners (for the given event).
 *
 * The semantics of listener registration / listener execution are
 * first register, first serve: New listeners will always be inserted
 * after existing listeners with the same priority.
 *
 * Example: Inserting two listeners with priority 1000 and 1300
 *
 *    * before: [ 1500, 1500, 1000, 1000 ]
 *    * after: [ 1500, 1500, (new=1300), 1000, 1000, (new=1000) ]
 *
 * @param {String} event
 * @param {Object} listener { priority, callback }
 */
EventBus.prototype._addListener = function(event, newListener) {

  var listeners = this._getListeners(event),
      existingListener,
      idx;

  // ensure we order listeners by priority from
  // 0 (high) to n > 0 (low)
  for (idx = 0; (existingListener = listeners[idx]); idx++) {
    if (existingListener.priority < newListener.priority) {

      // prepend newListener at before existingListener
      listeners.splice(idx, 0, newListener);
      return;
    }
  }

  listeners.push(newListener);
};


EventBus.prototype._getListeners = function(name) {
  var listeners = this._listeners[name];

  if (!listeners) {
    this._listeners[name] = listeners = [];
  }

  return listeners;
};


/**
 * A event that is emitted via the event bus.
 */
function Event() { }

module.exports.Event = Event;

Event.prototype.stopPropagation = function() {
  this.cancelBubble = true;
};

Event.prototype.preventDefault = function() {
  this.defaultPrevented = true;
};

Event.prototype.init = function(data) {
  assign(this, data || {});
};


/**
 * Invoke function. Be fast...
 *
 * @param {Function} fn
 * @param {Array<Object>} args
 *
 * @return {Any}
 */
function invokeFunction(fn, args) {
  return fn.apply(null, args);
}

},{"181":181,"182":182,"184":184,"190":190,"84":84}],36:[function(_dereq_,module,exports){
'use strict';

var forEach = _dereq_(78),
    reduce = _dereq_(81);

var GraphicsUtil = _dereq_(58);

var translate = _dereq_(63).translate;

var domClear = _dereq_(202);

var svgAppend = _dereq_(226),
    svgAttr = _dereq_(228),
    svgClasses = _dereq_(229),
    svgCreate = _dereq_(231),
    svgRemove = _dereq_(234);


/**
 * A factory that creates graphical elements
 *
 * @param {EventBus} eventBus
 * @param {ElementRegistry} elementRegistry
 */
function GraphicsFactory(eventBus, elementRegistry) {
  this._eventBus = eventBus;
  this._elementRegistry = elementRegistry;
}

GraphicsFactory.$inject = [ 'eventBus' , 'elementRegistry' ];

module.exports = GraphicsFactory;


GraphicsFactory.prototype._getChildren = function(element) {

  var gfx = this._elementRegistry.getGraphics(element);

  var childrenGfx;

  // root element
  if (!element.parent) {
    childrenGfx = gfx;
  } else {
    childrenGfx = GraphicsUtil.getChildren(gfx);
    if (!childrenGfx) {
      childrenGfx = svgCreate('g');
      svgClasses(childrenGfx).add('djs-children');

      svgAppend(gfx.parentNode, childrenGfx);
    }
  }

  return childrenGfx;
};

/**
 * Clears the graphical representation of the element and returns the
 * cleared visual (the <g class="djs-visual" /> element).
 */
GraphicsFactory.prototype._clear = function(gfx) {
  var visual = GraphicsUtil.getVisual(gfx);

  domClear(visual);

  return visual;
};

/**
 * Creates a gfx container for shapes and connections
 *
 * The layout is as follows:
 *
 * <g class="djs-group">
 *
 *   <!-- the gfx -->
 *   <g class="djs-element djs-(shape|connection)">
 *     <g class="djs-visual">
 *       <!-- the renderer draws in here -->
 *     </g>
 *
 *     <!-- extensions (overlays, click box, ...) goes here
 *   </g>
 *
 *   <!-- the gfx child nodes -->
 *   <g class="djs-children"></g>
 * </g>
 *
 * @param {Object} parent
 * @param {String} type the type of the element, i.e. shape | connection
 */
GraphicsFactory.prototype._createContainer = function(type, parentGfx) {
  var outerGfx = svgCreate('g');
  svgClasses(outerGfx).add('djs-group');

  svgAppend(parentGfx, outerGfx);

  var gfx = svgCreate('g');
  svgClasses(gfx).add('djs-element');
  svgClasses(gfx).add('djs-' + type);

  svgAppend(outerGfx, gfx);

  // create visual
  var visual = svgCreate('g');
  svgClasses(visual).add('djs-visual');

  svgAppend(gfx, visual);

  return gfx;
};

GraphicsFactory.prototype.create = function(type, element) {
  var childrenGfx = this._getChildren(element.parent);
  return this._createContainer(type, childrenGfx);
};

GraphicsFactory.prototype.updateContainments = function(elements) {

  var self = this,
      elementRegistry = this._elementRegistry,
      parents;

  parents = reduce(elements, function(map, e) {

    if (e.parent) {
      map[e.parent.id] = e.parent;
    }

    return map;
  }, {});

  // update all parents of changed and reorganized their children
  // in the correct order (as indicated in our model)
  forEach(parents, function(parent) {

    var childGfx = self._getChildren(parent),
        children = parent.children;

    if (!children) {
      return;
    }

    forEach(children.slice().reverse(), function(c) {
      var gfx = elementRegistry.getGraphics(c);

      prependTo(gfx.parentNode, childGfx);
    });
  });
};

GraphicsFactory.prototype.drawShape = function(visual, element) {
  var eventBus = this._eventBus;

  return eventBus.fire('render.shape', { gfx: visual, element: element });
};

GraphicsFactory.prototype.getShapePath = function(element) {
  var eventBus = this._eventBus;

  return eventBus.fire('render.getShapePath', element);
};

GraphicsFactory.prototype.drawConnection = function(visual, element) {
  var eventBus = this._eventBus;

  return eventBus.fire('render.connection', { gfx: visual, element: element });
};

GraphicsFactory.prototype.getConnectionPath = function(waypoints) {
  var eventBus = this._eventBus;

  return eventBus.fire('render.getConnectionPath', waypoints);
};

GraphicsFactory.prototype.update = function(type, element, gfx) {
  // Do not update root element
  if (!element.parent) {
    return;
  }

  var visual = this._clear(gfx);

  // redraw
  if (type === 'shape') {
    this.drawShape(visual, element);

    // update positioning
    translate(gfx, element.x, element.y);
  } else
  if (type === 'connection') {
    this.drawConnection(visual, element);
  } else {
    throw new Error('unknown type: ' + type);
  }

  if (element.hidden) {
    svgAttr(gfx, 'display', 'none');
  } else {
    svgAttr(gfx, 'display', 'block');
  }
};

GraphicsFactory.prototype.remove = function(element) {
  var gfx = this._elementRegistry.getGraphics(element);

  // remove
  svgRemove(gfx.parentNode);
};

////////// helpers ///////////

function prependTo(newNode, parentNode) {
  parentNode.insertBefore(newNode, parentNode.firstChild);
}

},{"202":202,"226":226,"228":228,"229":229,"231":231,"234":234,"58":58,"63":63,"78":78,"81":81}],37:[function(_dereq_,module,exports){
module.exports = {
  __depends__: [ _dereq_(41) ],
  __init__: [ 'canvas' ],
  canvas: [ 'type', _dereq_(32) ],
  elementRegistry: [ 'type', _dereq_(34) ],
  elementFactory: [ 'type', _dereq_(33) ],
  eventBus: [ 'type', _dereq_(35) ],
  graphicsFactory: [ 'type', _dereq_(36) ]
};
},{"32":32,"33":33,"34":34,"35":35,"36":36,"41":41}],38:[function(_dereq_,module,exports){
'use strict';

var DEFAULT_RENDER_PRIORITY = 1000;

/**
 * The base implementation of shape and connection renderers.
 *
 * @param {EventBus} eventBus
 * @param {Number} [renderPriority=1000]
 */
function BaseRenderer(eventBus, renderPriority) {
  var self = this;

  renderPriority = renderPriority || DEFAULT_RENDER_PRIORITY;

  eventBus.on([ 'render.shape', 'render.connection' ], renderPriority, function(evt, context) {
    var type = evt.type,
        element = context.element,
        visuals = context.gfx;

    if (self.canRender(element)) {
      if (type === 'render.shape') {
        return self.drawShape(visuals, element);
      } else {
        return self.drawConnection(visuals, element);
      }
    }
  });

  eventBus.on([ 'render.getShapePath', 'render.getConnectionPath'], renderPriority, function(evt, element) {
    if (self.canRender(element)) {
      if (evt.type === 'render.getShapePath') {
        return self.getShapePath(element);
      } else {
        return self.getConnectionPath(element);
      }
    }
  });
}

/**
 * Should check whether *this* renderer can render
 * the element/connection.
 *
 * @param {element} element
 *
 * @returns {Boolean}
 */
BaseRenderer.prototype.canRender = function() {};

/**
 * Provides the shape's snap svg element to be drawn on the `canvas`.
 *
 * @param {djs.Graphics} visuals
 * @param {Shape} shape
 *
 * @returns {Snap.svg} [returns a Snap.svg paper element ]
 */
BaseRenderer.prototype.drawShape = function() {};

/**
 * Provides the shape's snap svg element to be drawn on the `canvas`.
 *
 * @param {djs.Graphics} visuals
 * @param {Connection} connection
 *
 * @returns {Snap.svg} [returns a Snap.svg paper element ]
 */
BaseRenderer.prototype.drawConnection = function() {};

/**
 * Gets the SVG path of a shape that represents it's visual bounds.
 *
 * @param {Shape} shape
 *
 * @return {string} svg path
 */
BaseRenderer.prototype.getShapePath = function() {};

/**
 * Gets the SVG path of a connection that represents it's visual bounds.
 *
 * @param {Connection} connection
 *
 * @return {string} svg path
 */
BaseRenderer.prototype.getConnectionPath = function() {};

module.exports = BaseRenderer;

},{}],39:[function(_dereq_,module,exports){
'use strict';

var inherits = _dereq_(72);

var BaseRenderer = _dereq_(38);

var renderUtil = _dereq_(62);

var componentsToPath = renderUtil.componentsToPath,
    createLine = renderUtil.createLine;

var svgAppend = _dereq_(226),
    svgAttr = _dereq_(228),
    svgCreate = _dereq_(231);

// apply default renderer with lowest possible priority
// so that it only kicks in if noone else could render
var DEFAULT_RENDER_PRIORITY = 1;

/**
 * The default renderer used for shapes and connections.
 *
 * @param {EventBus} eventBus
 * @param {Styles} styles
 */
function DefaultRenderer(eventBus, styles) {
  //
  BaseRenderer.call(this, eventBus, DEFAULT_RENDER_PRIORITY);

  this.CONNECTION_STYLE = styles.style([ 'no-fill' ], { strokeWidth: 5, stroke: 'fuchsia' });
  this.SHAPE_STYLE = styles.style({ fill: 'white', stroke: 'fuchsia', strokeWidth: 2 });
}

inherits(DefaultRenderer, BaseRenderer);


DefaultRenderer.prototype.canRender = function() {
  return true;
};

DefaultRenderer.prototype.drawShape = function drawShape(visuals, element) {

  var rect = svgCreate('rect');
  svgAttr(rect, {
    x: 0,
    y: 0,
    width: element.width || 0,
    height: element.height || 0
  });
  svgAttr(rect, this.SHAPE_STYLE);

  svgAppend(visuals, rect);

  return rect;
};

DefaultRenderer.prototype.drawConnection = function drawConnection(visuals, connection) {

  var line = createLine(connection.waypoints, this.CONNECTION_STYLE);
  svgAppend(visuals, line);

  return line;
};

DefaultRenderer.prototype.getShapePath = function getShapePath(shape) {

  var x = shape.x,
      y = shape.y,
      width = shape.width,
      height = shape.height;

  var shapePath = [
    ['M', x, y],
    ['l', width, 0],
    ['l', 0, height],
    ['l', -width, 0],
    ['z']
  ];

  return componentsToPath(shapePath);
};

DefaultRenderer.prototype.getConnectionPath = function getConnectionPath(connection) {
  var waypoints = connection.waypoints;

  var idx, point, connectionPath = [];

  for (idx = 0; (point = waypoints[idx]); idx++) {

    // take invisible docking into account
    // when creating the path
    point = point.original || point;

    connectionPath.push([ idx === 0 ? 'M' : 'L', point.x, point.y ]);
  }

  return componentsToPath(connectionPath);
};


DefaultRenderer.$inject = [ 'eventBus', 'styles' ];

module.exports = DefaultRenderer;

},{"226":226,"228":228,"231":231,"38":38,"62":62,"72":72}],40:[function(_dereq_,module,exports){
'use strict';

var isArray = _dereq_(181),
    assign = _dereq_(190),
    reduce = _dereq_(81);


/**
 * A component that manages shape styles
 */
function Styles() {

  var defaultTraits = {

    'no-fill': {
      fill: 'none'
    },
    'no-border': {
      strokeOpacity: 0.0
    },
    'no-events': {
      pointerEvents: 'none'
    }
  };

  var self = this;

  /**
   * Builds a style definition from a className, a list of traits and an object of additional attributes.
   *
   * @param  {String} className
   * @param  {Array<String>} traits
   * @param  {Object} additionalAttrs
   *
   * @return {Object} the style defintion
   */
  this.cls = function(className, traits, additionalAttrs) {
    var attrs = this.style(traits, additionalAttrs);

    return assign(attrs, { 'class': className });
  };

  /**
   * Builds a style definition from a list of traits and an object of additional attributes.
   *
   * @param  {Array<String>} traits
   * @param  {Object} additionalAttrs
   *
   * @return {Object} the style defintion
   */
  this.style = function(traits, additionalAttrs) {

    if (!isArray(traits) && !additionalAttrs) {
      additionalAttrs = traits;
      traits = [];
    }

    var attrs = reduce(traits, function(attrs, t) {
      return assign(attrs, defaultTraits[t] || {});
    }, {});

    return additionalAttrs ? assign(attrs, additionalAttrs) : attrs;
  };

  this.computeStyle = function(custom, traits, defaultStyles) {
    if (!isArray(traits)) {
      defaultStyles = traits;
      traits = [];
    }

    return self.style(traits || [], assign({}, defaultStyles, custom || {}));
  };
}

module.exports = Styles;

},{"181":181,"190":190,"81":81}],41:[function(_dereq_,module,exports){
module.exports = {
  __init__: [ 'defaultRenderer' ],
  defaultRenderer: [ 'type', _dereq_(39) ],
  styles: [ 'type', _dereq_(40) ]
};

},{"39":39,"40":40}],42:[function(_dereq_,module,exports){
'use strict';

var forEach = _dereq_(78),
    domDelegate = _dereq_(203);

var isPrimaryButton = _dereq_(60).isPrimaryButton;

var svgAppend = _dereq_(226),
    svgAttr = _dereq_(228),
    svgCreate = _dereq_(231);

var domQuery = _dereq_(206);

var renderUtil = _dereq_(62);

var createLine = renderUtil.createLine,
    updateLine = renderUtil.updateLine;

var LOW_PRIORITY = 500;

/**
 * A plugin that provides interaction events for diagram elements.
 *
 * It emits the following events:
 *
 *   * element.hover
 *   * element.out
 *   * element.click
 *   * element.dblclick
 *   * element.mousedown
 *
 * Each event is a tuple { element, gfx, originalEvent }.
 *
 * Canceling the event via Event#preventDefault() prevents the original DOM operation.
 *
 * @param {EventBus} eventBus
 */
function InteractionEvents(eventBus, elementRegistry, styles) {

  var HIT_STYLE = styles.cls('djs-hit', [ 'no-fill', 'no-border' ], {
    stroke: 'white',
    strokeWidth: 15
  });

  /**
   * Fire an interaction event.
   *
   * @param {String} type local event name, e.g. element.click.
   * @param {DOMEvent} event native event
   * @param {djs.model.Base} [element] the diagram element to emit the event on;
   *                                   defaults to the event target
   */
  function fire(type, event, element) {

    // only react on left mouse button interactions
    // for interaction events
    if (!isPrimaryButton(event)) {
      return;
    }

    var target, gfx, returnValue;

    if (!element) {
      target = event.delegateTarget || event.target;

      if (target) {
        gfx = target;
        element = elementRegistry.get(gfx);
      }
    } else {
      gfx = elementRegistry.getGraphics(element);
    }

    if (!gfx || !element) {
      return;
    }

    returnValue = eventBus.fire(type, { element: element, gfx: gfx, originalEvent: event });

    if (returnValue === false) {
      event.stopPropagation();
      event.preventDefault();
    }
  }

  // TODO(nikku): document this
  var handlers = {};

  function mouseHandler(type) {

    var fn = handlers[type];

    if (!fn) {
      fn = handlers[type] = function(event) {
        fire(type, event);
      };
    }

    return fn;
  }

  var bindings = {
    mouseover: 'element.hover',
    mouseout: 'element.out',
    click: 'element.click',
    dblclick: 'element.dblclick',
    mousedown: 'element.mousedown',
    mouseup: 'element.mouseup'
  };


  ///// manual event trigger

  /**
   * Trigger an interaction event (based on a native dom event)
   * on the target shape or connection.
   *
   * @param {String} eventName the name of the triggered DOM event
   * @param {MouseEvent} event
   * @param {djs.model.Base} targetElement
   */
  function triggerMouseEvent(eventName, event, targetElement) {

    // i.e. element.mousedown...
    var localEventName = bindings[eventName];

    if (!localEventName) {
      throw new Error('unmapped DOM event name <' + eventName + '>');
    }

    return fire(localEventName, event, targetElement);
  }


  var elementSelector = 'svg, .djs-element';

  ///// event registration

  function registerEvent(node, event, localEvent) {
    var handler = mouseHandler(localEvent);
    handler.$delegate = domDelegate.bind(node, elementSelector, event, handler);
  }

  function unregisterEvent(node, event, localEvent) {
    domDelegate.unbind(node, event, mouseHandler(localEvent).$delegate);
  }

  function registerEvents(svg) {
    forEach(bindings, function(val, key) {
      registerEvent(svg, key, val);
    });
  }

  function unregisterEvents(svg) {
    forEach(bindings, function(val, key) {
      unregisterEvent(svg, key, val);
    });
  }

  eventBus.on('canvas.destroy', function(event) {
    unregisterEvents(event.svg);
  });

  eventBus.on('canvas.init', function(event) {
    registerEvents(event.svg);
  });


  eventBus.on([ 'shape.added', 'connection.added' ], function(event) {
    var element = event.element,
        gfx = event.gfx,
        hit;

    if (element.waypoints) {
      hit = createLine(element.waypoints);
    } else {
      hit = svgCreate('rect');
      svgAttr(hit, {
        x: 0,
        y: 0,
        width: element.width,
        height: element.height
      });
    }

    svgAttr(hit, HIT_STYLE);

    svgAppend(gfx, hit);
  });

  // Update djs-hit on change.
  // A low priortity is necessary, because djs-hit of labels has to be updated
  // after the label bounds have been updated in the renderer.
  eventBus.on('shape.changed', LOW_PRIORITY, function(event) {

    var element = event.element,
        gfx = event.gfx,
        hit = domQuery('.djs-hit', gfx);

    svgAttr(hit, {
      width: element.width,
      height: element.height
    });
  });

  eventBus.on('connection.changed', function(event) {

    var element = event.element,
        gfx = event.gfx,
        hit = domQuery('.djs-hit', gfx);

    updateLine(hit, element.waypoints);
  });


  // API

  this.fire = fire;

  this.triggerMouseEvent = triggerMouseEvent;

  this.mouseHandler = mouseHandler;

  this.registerEvent = registerEvent;
  this.unregisterEvent = unregisterEvent;
}


InteractionEvents.$inject = [ 'eventBus', 'elementRegistry', 'styles' ];

module.exports = InteractionEvents;


/**
 * An event indicating that the mouse hovered over an element
 *
 * @event element.hover
 *
 * @type {Object}
 * @property {djs.model.Base} element
 * @property {SVGElement} gfx
 * @property {Event} originalEvent
 */

/**
 * An event indicating that the mouse has left an element
 *
 * @event element.out
 *
 * @type {Object}
 * @property {djs.model.Base} element
 * @property {SVGElement} gfx
 * @property {Event} originalEvent
 */

/**
 * An event indicating that the mouse has clicked an element
 *
 * @event element.click
 *
 * @type {Object}
 * @property {djs.model.Base} element
 * @property {SVGElement} gfx
 * @property {Event} originalEvent
 */

/**
 * An event indicating that the mouse has double clicked an element
 *
 * @event element.dblclick
 *
 * @type {Object}
 * @property {djs.model.Base} element
 * @property {SVGElement} gfx
 * @property {Event} originalEvent
 */

/**
 * An event indicating that the mouse has gone down on an element.
 *
 * @event element.mousedown
 *
 * @type {Object}
 * @property {djs.model.Base} element
 * @property {SVGElement} gfx
 * @property {Event} originalEvent
 */

/**
 * An event indicating that the mouse has gone up on an element.
 *
 * @event element.mouseup
 *
 * @type {Object}
 * @property {djs.model.Base} element
 * @property {SVGElement} gfx
 * @property {Event} originalEvent
 */

},{"203":203,"206":206,"226":226,"228":228,"231":231,"60":60,"62":62,"78":78}],43:[function(_dereq_,module,exports){
module.exports = {
  __init__: [ 'interactionEvents' ],
  interactionEvents: [ 'type', _dereq_(42) ]
};
},{"42":42}],44:[function(_dereq_,module,exports){
'use strict';

var getBBox = _dereq_(56).getBBox;

var LOW_PRIORITY = 500;

var svgAppend = _dereq_(226),
    svgAttr = _dereq_(228),
    svgCreate = _dereq_(231);

var domQuery = _dereq_(206);

var assign = _dereq_(190);


/**
 * @class
 *
 * A plugin that adds an outline to shapes and connections that may be activated and styled
 * via CSS classes.
 *
 * @param {EventBus} eventBus
 * @param {Styles} styles
 * @param {ElementRegistry} elementRegistry
 */
function Outline(eventBus, styles, elementRegistry) {

  this.offset = 6;

  var OUTLINE_STYLE = styles.cls('djs-outline', [ 'no-fill' ]);

  var self = this;

  function createOutline(gfx, bounds) {
    var outline = svgCreate('rect');

    svgAttr(outline, assign({
      x: 10,
      y: 10,
      width: 100,
      height: 100
    }, OUTLINE_STYLE));

    svgAppend(gfx, outline);

    return outline;
  }

  // A low priortity is necessary, because outlines of labels have to be updated
  // after the label bounds have been updated in the renderer.
  eventBus.on([ 'shape.added', 'shape.changed' ], LOW_PRIORITY, function(event) {
    var element = event.element,
        gfx     = event.gfx;

    var outline = domQuery('.djs-outline', gfx);

    if (!outline) {
      outline = createOutline(gfx, element);
    }

    self.updateShapeOutline(outline, element);
  });

  eventBus.on([ 'connection.added', 'connection.changed' ], function(event) {
    var element = event.element,
        gfx     = event.gfx;

    var outline = domQuery('.djs-outline', gfx);

    if (!outline) {
      outline = createOutline(gfx, element);
    }

    self.updateConnectionOutline(outline, element);
  });
}


/**
 * Updates the outline of a shape respecting the dimension of the
 * element and an outline offset.
 *
 * @param  {SVGElement} outline
 * @param  {djs.model.Base} element
 */
Outline.prototype.updateShapeOutline = function(outline, element) {

  svgAttr(outline, {
    x: -this.offset,
    y: -this.offset,
    width: element.width + this.offset * 2,
    height: element.height + this.offset * 2
  });

};


/**
 * Updates the outline of a connection respecting the bounding box of
 * the connection and an outline offset.
 *
 * @param  {SVGElement} outline
 * @param  {djs.model.Base} element
 */
Outline.prototype.updateConnectionOutline = function(outline, connection) {

  var bbox = getBBox(connection);

  svgAttr(outline, {
    x: bbox.x - this.offset,
    y: bbox.y - this.offset,
    width: bbox.width + this.offset * 2,
    height: bbox.height + this.offset * 2
  });

};


Outline.$inject = ['eventBus', 'styles', 'elementRegistry'];

module.exports = Outline;

},{"190":190,"206":206,"226":226,"228":228,"231":231,"56":56}],45:[function(_dereq_,module,exports){
'use strict';

module.exports = {
  __init__: [ 'outline' ],
  outline: [ 'type', _dereq_(44) ]
};
},{"44":44}],46:[function(_dereq_,module,exports){
'use strict';

var isArray = _dereq_(181),
    isString = _dereq_(187),
    isObject = _dereq_(185),
    assign = _dereq_(190),
    forEach = _dereq_(78),
    find = _dereq_(77),
    filter = _dereq_(76);

var domify = _dereq_(204),
    domClasses = _dereq_(201),
    domAttr = _dereq_(200),
    domRemove = _dereq_(207),
    domClear = _dereq_(202);

var getBBox = _dereq_(56).getBBox;

// document wide unique overlay ids
var ids = new (_dereq_(59))('ov');

var LOW_PRIORITY = 500;


function createRoot(parent) {
  var root = domify('<div class="djs-overlay-container" style="position: absolute; width: 0; height: 0;" />');
  parent.insertBefore(root, parent.firstChild);

  return root;
}


function setPosition(el, x, y) {
  assign(el.style, { left: x + 'px', top: y + 'px' });
}

function setVisible(el, visible) {
  el.style.display = visible === false ? 'none' : '';
}

function setTransform(el, transform) {

  el.style['transform-origin'] = 'top left';

  [ '', '-ms-', '-webkit-' ].forEach(function(prefix) {
    el.style[prefix + 'transform'] = transform;
  });
}

function isDef(o) {
  return typeof o !== 'undefined';
}

/**
 * A service that allows users to attach overlays to diagram elements.
 *
 * The overlay service will take care of overlay positioning during updates.
 *
 * @example
 *
 * // add a pink badge on the top left of the shape
 * overlays.add(someShape, {
 *   position: {
 *     top: -5,
 *     left: -5
 *   },
 *   html: '<div style="width: 10px; background: fuchsia; color: white;">0</div>'
 * });
 *
 * // or add via shape id
 *
 * overlays.add('some-element-id', {
 *   position: {
 *     top: -5,
 *     left: -5
 *   }
 *   html: '<div style="width: 10px; background: fuchsia; color: white;">0</div>'
 * });
 *
 * // or add with optional type
 *
 * overlays.add(someShape, 'badge', {
 *   position: {
 *     top: -5,
 *     left: -5
 *   }
 *   html: '<div style="width: 10px; background: fuchsia; color: white;">0</div>'
 * });
 *
 *
 * // remove an overlay
 *
 * var id = overlays.add(...);
 * overlays.remove(id);
 *
 *
 * You may configure overlay defaults during tool by providing a `config` module
 * with `overlays.defaults` as an entry:
 *
 * {
 *   overlays: {
 *     defaults: {
 *       show: {
 *         minZoom: 0.7,
 *         maxZoom: 5.0
 *       },
 *       scale: {
 *         min: 1
 *       }
 *     }
 * }
 *
 * @param {Object} config
 * @param {EventBus} eventBus
 * @param {Canvas} canvas
 * @param {ElementRegistry} elementRegistry
 */
function Overlays(config, eventBus, canvas, elementRegistry) {

  this._eventBus = eventBus;
  this._canvas = canvas;
  this._elementRegistry = elementRegistry;

  this._ids = ids;

  this._overlayDefaults = assign({
    // no show constraints
    show: null,

    // always scale
    scale: true
  }, config && config.defaults);

  /**
   * Mapping overlayId -> overlay
   */
  this._overlays = {};

  /**
   * Mapping elementId -> overlay container
   */
  this._overlayContainers = [];

  // root html element for all overlays
  this._overlayRoot = createRoot(canvas.getContainer());

  this._init();
}


Overlays.$inject = [
  'config.overlays',
  'eventBus',
  'canvas',
  'elementRegistry'
];

module.exports = Overlays;


/**
 * Returns the overlay with the specified id or a list of overlays
 * for an element with a given type.
 *
 * @example
 *
 * // return the single overlay with the given id
 * overlays.get('some-id');
 *
 * // return all overlays for the shape
 * overlays.get({ element: someShape });
 *
 * // return all overlays on shape with type 'badge'
 * overlays.get({ element: someShape, type: 'badge' });
 *
 * // shape can also be specified as id
 * overlays.get({ element: 'element-id', type: 'badge' });
 *
 *
 * @param {Object} search
 * @param {String} [search.id]
 * @param {String|djs.model.Base} [search.element]
 * @param {String} [search.type]
 *
 * @return {Object|Array<Object>} the overlay(s)
 */
Overlays.prototype.get = function(search) {

  if (isString(search)) {
    search = { id: search };
  }

  if (isString(search.element)) {
    search.element = this._elementRegistry.get(search.element);
  }

  if (search.element) {
    var container = this._getOverlayContainer(search.element, true);

    // return a list of overlays when searching by element (+type)
    if (container) {
      return search.type ? filter(container.overlays, { type: search.type }) : container.overlays.slice();
    } else {
      return [];
    }
  } else
  if (search.type) {
    return filter(this._overlays, { type: search.type });
  } else {
    // return single element when searching by id
    return search.id ? this._overlays[search.id] : null;
  }
};

/**
 * Adds a HTML overlay to an element.
 *
 * @param {String|djs.model.Base}   element   attach overlay to this shape
 * @param {String}                  [type]    optional type to assign to the overlay
 * @param {Object}                  overlay   the overlay configuration
 *
 * @param {String|DOMElement}       overlay.html                 html element to use as an overlay
 * @param {Object}                  [overlay.show]               show configuration
 * @param {Number}                  [overlay.show.minZoom]       minimal zoom level to show the overlay
 * @param {Number}                  [overlay.show.maxZoom]       maximum zoom level to show the overlay
 * @param {Object}                  overlay.position             where to attach the overlay
 * @param {Number}                  [overlay.position.left]      relative to element bbox left attachment
 * @param {Number}                  [overlay.position.top]       relative to element bbox top attachment
 * @param {Number}                  [overlay.position.bottom]    relative to element bbox bottom attachment
 * @param {Number}                  [overlay.position.right]     relative to element bbox right attachment
 * @param {Boolean|Object}          [overlay.scale=true]         false to preserve the same size regardless of
 *                                                               diagram zoom
 * @param {Number}                  [overlay.scale.min]
 * @param {Number}                  [overlay.scale.max]
 *
 * @return {String}                 id that may be used to reference the overlay for update or removal
 */
Overlays.prototype.add = function(element, type, overlay) {

  if (isObject(type)) {
    overlay = type;
    type = null;
  }

  if (!element.id) {
    element = this._elementRegistry.get(element);
  }

  if (!overlay.position) {
    throw new Error('must specifiy overlay position');
  }

  if (!overlay.html) {
    throw new Error('must specifiy overlay html');
  }

  if (!element) {
    throw new Error('invalid element specified');
  }

  var id = this._ids.next();

  overlay = assign({}, this._overlayDefaults, overlay, {
    id: id,
    type: type,
    element: element,
    html: overlay.html
  });

  this._addOverlay(overlay);

  return id;
};


/**
 * Remove an overlay with the given id or all overlays matching the given filter.
 *
 * @see Overlays#get for filter options.
 *
 * @param {String} [id]
 * @param {Object} [filter]
 */
Overlays.prototype.remove = function(filter) {

  var overlays = this.get(filter) || [];

  if (!isArray(overlays)) {
    overlays = [ overlays ];
  }

  var self = this;

  forEach(overlays, function(overlay) {

    var container = self._getOverlayContainer(overlay.element, true);

    if (overlay) {
      domRemove(overlay.html);
      domRemove(overlay.htmlContainer);

      delete overlay.htmlContainer;
      delete overlay.element;

      delete self._overlays[overlay.id];
    }

    if (container) {
      var idx = container.overlays.indexOf(overlay);
      if (idx !== -1) {
        container.overlays.splice(idx, 1);
      }
    }
  });

};


Overlays.prototype.show = function() {
  setVisible(this._overlayRoot);
};


Overlays.prototype.hide = function() {
  setVisible(this._overlayRoot, false);
};

Overlays.prototype.clear = function() {
  this._overlays = {};

  this._overlayContainers = [];

  domClear(this._overlayRoot);
};

Overlays.prototype._updateOverlayContainer = function(container) {
  var element = container.element,
      html = container.html;

  // update container left,top according to the elements x,y coordinates
  // this ensures we can attach child elements relative to this container

  var x = element.x,
      y = element.y;

  if (element.waypoints) {
    var bbox = getBBox(element);
    x = bbox.x;
    y = bbox.y;
  }

  setPosition(html, x, y);

  domAttr(container.html, 'data-container-id', element.id);
};


Overlays.prototype._updateOverlay = function(overlay) {

  var position = overlay.position,
      htmlContainer = overlay.htmlContainer,
      element = overlay.element;

  // update overlay html relative to shape because
  // it is already positioned on the element

  // update relative
  var left = position.left,
      top = position.top;

  if (position.right !== undefined) {

    var width;

    if (element.waypoints) {
      width = getBBox(element).width;
    } else {
      width = element.width;
    }

    left = position.right * -1 + width;
  }

  if (position.bottom !== undefined) {

    var height;

    if (element.waypoints) {
      height = getBBox(element).height;
    } else {
      height = element.height;
    }

    top = position.bottom * -1 + height;
  }

  setPosition(htmlContainer, left || 0, top || 0);
};


Overlays.prototype._createOverlayContainer = function(element) {
  var html = domify('<div class="djs-overlays" style="position: absolute" />');

  this._overlayRoot.appendChild(html);

  var container = {
    html: html,
    element: element,
    overlays: []
  };

  this._updateOverlayContainer(container);

  this._overlayContainers.push(container);

  return container;
};


Overlays.prototype._updateRoot = function(viewbox) {
  var scale = viewbox.scale || 1;

  var matrix = 'matrix(' +
  [
    scale,
    0,
    0,
    scale,
    -1 * viewbox.x * scale,
    -1 * viewbox.y * scale
  ].join(',') +
  ')';

  setTransform(this._overlayRoot, matrix);
};


Overlays.prototype._getOverlayContainer = function(element, raw) {
  var container = find(this._overlayContainers, function(c) {
    return c.element === element;
  });


  if (!container && !raw) {
    return this._createOverlayContainer(element);
  }

  return container;
};


Overlays.prototype._addOverlay = function(overlay) {

  var id = overlay.id,
      element = overlay.element,
      html = overlay.html,
      htmlContainer,
      overlayContainer;

  // unwrap jquery (for those who need it)
  if (html.get && html.constructor.prototype.jquery) {
    html = html.get(0);
  }

  // create proper html elements from
  // overlay HTML strings
  if (isString(html)) {
    html = domify(html);
  }

  overlayContainer = this._getOverlayContainer(element);

  htmlContainer = domify('<div class="djs-overlay" data-overlay-id="' + id + '" style="position: absolute">');

  htmlContainer.appendChild(html);

  if (overlay.type) {
    domClasses(htmlContainer).add('djs-overlay-' + overlay.type);
  }

  overlay.htmlContainer = htmlContainer;

  overlayContainer.overlays.push(overlay);
  overlayContainer.html.appendChild(htmlContainer);

  this._overlays[id] = overlay;

  this._updateOverlay(overlay);
  this._updateOverlayVisibilty(overlay, this._canvas.viewbox());
};


Overlays.prototype._updateOverlayVisibilty = function(overlay, viewbox) {
  var show = overlay.show,
      minZoom = show && show.minZoom,
      maxZoom = show && show.maxZoom,
      htmlContainer = overlay.htmlContainer,
      visible = true;

  if (show) {
    if (
      (isDef(minZoom) && minZoom > viewbox.scale) ||
      (isDef(maxZoom) && maxZoom < viewbox.scale)
    ) {
      visible = false;
    }

    setVisible(htmlContainer, visible);
  }

  this._updateOverlayScale(overlay, viewbox);
};


Overlays.prototype._updateOverlayScale = function(overlay, viewbox) {
  var shouldScale = overlay.scale,
      minScale,
      maxScale,
      htmlContainer = overlay.htmlContainer;

  var scale, transform = '';

  if (shouldScale !== true) {

    if (shouldScale === false) {
      minScale = 1;
      maxScale = 1;
    } else {
      minScale = shouldScale.min;
      maxScale = shouldScale.max;
    }

    if (isDef(minScale) && viewbox.scale < minScale) {
      scale = (1 / viewbox.scale || 1) * minScale;
    }

    if (isDef(maxScale) && viewbox.scale > maxScale) {
      scale = (1 / viewbox.scale || 1) * maxScale;
    }
  }

  if (isDef(scale)) {
    transform = 'scale(' + scale + ',' + scale + ')';
  }

  setTransform(htmlContainer, transform);
};


Overlays.prototype._updateOverlaysVisibilty = function(viewbox) {

  var self = this;

  forEach(this._overlays, function(overlay) {
    self._updateOverlayVisibilty(overlay, viewbox);
  });
};


Overlays.prototype._init = function() {

  var eventBus = this._eventBus;

  var self = this;


  // scroll/zoom integration

  function updateViewbox(viewbox) {
    self._updateRoot(viewbox);
    self._updateOverlaysVisibilty(viewbox);

    self.show();
  }

  eventBus.on('canvas.viewbox.changing', function(event) {
    self.hide();
  });

  eventBus.on('canvas.viewbox.changed', function(event) {
    updateViewbox(event.viewbox);
  });


  // remove integration

  eventBus.on([ 'shape.remove', 'connection.remove' ], function(e) {
    var element = e.element;
    var overlays = self.get({ element: element });

    forEach(overlays, function(o) {
      self.remove(o.id);
    });

    var container = self._getOverlayContainer(element);

    if (container) {
      domRemove(container.html);
      var i = self._overlayContainers.indexOf(container);
      if (i !== -1) {
        self._overlayContainers.splice(i, 1);
      }
    }
  });


  // move integration

  eventBus.on('element.changed', LOW_PRIORITY, function(e) {
    var element = e.element;

    var container = self._getOverlayContainer(element, true);

    if (container) {
      forEach(container.overlays, function(overlay) {
        self._updateOverlay(overlay);
      });

      self._updateOverlayContainer(container);
    }
  });


  // marker integration, simply add them on the overlays as classes, too.

  eventBus.on('element.marker.update', function(e) {
    var container = self._getOverlayContainer(e.element, true);
    if (container) {
      domClasses(container.html)[e.add ? 'add' : 'remove'](e.marker);
    }
  });


  // clear overlays with diagram

  eventBus.on('diagram.clear', this.clear, this);
};

},{"181":181,"185":185,"187":187,"190":190,"200":200,"201":201,"202":202,"204":204,"207":207,"56":56,"59":59,"76":76,"77":77,"78":78}],47:[function(_dereq_,module,exports){
module.exports = {
  __init__: [ 'overlays' ],
  overlays: [ 'type', _dereq_(46) ]
};
},{"46":46}],48:[function(_dereq_,module,exports){
'use strict';

var isArray = _dereq_(181),
    forEach = _dereq_(78);


/**
 * A service that offers the current selection in a diagram.
 * Offers the api to control the selection, too.
 *
 * @class
 *
 * @param {EventBus} eventBus the event bus
 */
function Selection(eventBus) {

  this._eventBus = eventBus;

  this._selectedElements = [];

  var self = this;

  eventBus.on([ 'shape.remove', 'connection.remove' ], function(e) {
    var element = e.element;
    self.deselect(element);
  });

  eventBus.on([ 'diagram.clear' ], function(e) {
    self.select(null);
  });
}

Selection.$inject = [ 'eventBus' ];

module.exports = Selection;


Selection.prototype.deselect = function(element) {
  var selectedElements = this._selectedElements;

  var idx = selectedElements.indexOf(element);

  if (idx !== -1) {
    var oldSelection = selectedElements.slice();

    selectedElements.splice(idx, 1);

    this._eventBus.fire('selection.changed', { oldSelection: oldSelection, newSelection: selectedElements });
  }
};


Selection.prototype.get = function() {
  return this._selectedElements;
};

Selection.prototype.isSelected = function(element) {
  return this._selectedElements.indexOf(element) !== -1;
};


/**
 * This method selects one or more elements on the diagram.
 *
 * By passing an additional add parameter you can decide whether or not the element(s)
 * should be added to the already existing selection or not.
 *
 * @method Selection#select
 *
 * @param  {Object|Object[]} elements element or array of elements to be selected
 * @param  {boolean} [add] whether the element(s) should be appended to the current selection, defaults to false
 */
Selection.prototype.select = function(elements, add) {
  var selectedElements = this._selectedElements,
      oldSelection = selectedElements.slice();

  if (!isArray(elements)) {
    elements = elements ? [ elements ] : [];
  }

  // selection may be cleared by passing an empty array or null
  // to the method
  if (add) {
    forEach(elements, function(element) {
      if (selectedElements.indexOf(element) !== -1) {
        // already selected
        return;
      } else {
        selectedElements.push(element);
      }
    });
  } else {
    this._selectedElements = selectedElements = elements.slice();
  }

  this._eventBus.fire('selection.changed', { oldSelection: oldSelection, newSelection: selectedElements });
};

},{"181":181,"78":78}],49:[function(_dereq_,module,exports){
'use strict';

var hasPrimaryModifier = _dereq_(60).hasPrimaryModifier;

var find = _dereq_(77);


function SelectionBehavior(eventBus, selection, canvas, elementRegistry) {

  eventBus.on('create.end', 500, function(e) {

    // select the created shape after a
    // successful create operation
    if (e.context.canExecute) {
      selection.select(e.context.shape);
    }
  });

  eventBus.on('connect.end', 500, function(e) {

    // select the connect end target
    // after a connect operation
    if (e.context.canExecute && e.context.target) {
      selection.select(e.context.target);
    }
  });

  eventBus.on('shape.move.end', 500, function(e) {
    var previousSelection = e.previousSelection || [];

    var shape = elementRegistry.get(e.context.shape.id);

    // make sure at least the main moved element is being
    // selected after a move operation
    var inSelection = find(previousSelection, function(selectedShape) {
      return shape.id === selectedShape.id;
    });

    if (!inSelection) {
      selection.select(shape);
    }
  });

  // Shift + click selection
  eventBus.on('element.click', function(event) {

    var element = event.element;

    // do not select the root element
    // or connections
    if (element === canvas.getRootElement()) {
      element = null;
    }

    var isSelected = selection.isSelected(element),
        isMultiSelect = selection.get().length > 1;

    // mouse-event: SELECTION_KEY
    var add = hasPrimaryModifier(event);

    // select OR deselect element in multi selection
    if (isSelected && isMultiSelect) {
      if (add) {
        return selection.deselect(element);
      } else {
        return selection.select(element);
      }
    } else
    if (!isSelected) {
      selection.select(element, add);
    } else {
      selection.deselect(element);
    }
  });
}

SelectionBehavior.$inject = [ 'eventBus', 'selection', 'canvas', 'elementRegistry' ];
module.exports = SelectionBehavior;

},{"60":60,"77":77}],50:[function(_dereq_,module,exports){
'use strict';

var forEach = _dereq_(78);

var MARKER_HOVER = 'hover',
    MARKER_SELECTED = 'selected';


/**
 * A plugin that adds a visible selection UI to shapes and connections
 * by appending the <code>hover</code> and <code>selected</code> classes to them.
 *
 * @class
 *
 * Makes elements selectable, too.
 *
 * @param {EventBus} events
 * @param {SelectionService} selection
 * @param {Canvas} canvas
 */
function SelectionVisuals(events, canvas, selection, styles) {

  this._multiSelectionBox = null;

  function addMarker(e, cls) {
    canvas.addMarker(e, cls);
  }

  function removeMarker(e, cls) {
    canvas.removeMarker(e, cls);
  }

  events.on('element.hover', function(event) {
    addMarker(event.element, MARKER_HOVER);
  });

  events.on('element.out', function(event) {
    removeMarker(event.element, MARKER_HOVER);
  });

  events.on('selection.changed', function(event) {

    function deselect(s) {
      removeMarker(s, MARKER_SELECTED);
    }

    function select(s) {
      addMarker(s, MARKER_SELECTED);
    }

    var oldSelection = event.oldSelection,
        newSelection = event.newSelection;

    forEach(oldSelection, function(e) {
      if (newSelection.indexOf(e) === -1) {
        deselect(e);
      }
    });

    forEach(newSelection, function(e) {
      if (oldSelection.indexOf(e) === -1) {
        select(e);
      }
    });
  });
}

SelectionVisuals.$inject = [
  'eventBus',
  'canvas',
  'selection',
  'styles'
];

module.exports = SelectionVisuals;

},{"78":78}],51:[function(_dereq_,module,exports){
module.exports = {
  __init__: [ 'selectionVisuals', 'selectionBehavior' ],
  __depends__: [
    _dereq_(43),
    _dereq_(45)
  ],
  selection: [ 'type', _dereq_(48) ],
  selectionVisuals: [ 'type', _dereq_(50) ],
  selectionBehavior: [ 'type', _dereq_(49) ]
};

},{"43":43,"45":45,"48":48,"49":49,"50":50}],52:[function(_dereq_,module,exports){
module.exports = {
  translate: [ 'value', _dereq_(53) ]
};
},{"53":53}],53:[function(_dereq_,module,exports){
'use strict';

/**
 * A simple translation stub to be used for multi-language support
 * in diagrams. Can be easily replaced with a more sophisticated
 * solution.
 *
 * @example
 *
 * // use it inside any diagram component by injecting `translate`.
 *
 * function MyService(translate) {
 *   alert(translate('HELLO {you}', { you: 'You!' }));
 * }
 *
 * @param {String} template to interpolate
 * @param {Object} [replacements] a map with substitutes
 *
 * @return {String} the translated string
 */
module.exports = function translate(template, replacements) {

  replacements = replacements || {};

  return template.replace(/{([^}]+)}/g, function(_, key) {
    return replacements[key] || '{' + key + '}';
  });
};
},{}],54:[function(_dereq_,module,exports){
'use strict';

var assign = _dereq_(190),
    inherits = _dereq_(72);

var Refs = _dereq_(220);

var parentRefs = new Refs({ name: 'children', enumerable: true, collection: true }, { name: 'parent' }),
    labelRefs = new Refs({ name: 'label', enumerable: true }, { name: 'labelTarget' }),
    attacherRefs = new Refs({ name: 'attachers', collection: true }, { name: 'host' }),
    outgoingRefs = new Refs({ name: 'outgoing', collection: true }, { name: 'source' }),
    incomingRefs = new Refs({ name: 'incoming', collection: true }, { name: 'target' });

/**
 * @namespace djs.model
 */

/**
 * @memberOf djs.model
 */

/**
 * The basic graphical representation
 *
 * @class
 *
 * @abstract
 */
function Base() {

  /**
   * The object that backs up the shape
   *
   * @name Base#businessObject
   * @type Object
   */
  Object.defineProperty(this, 'businessObject', {
    writable: true
  });

  /**
   * The parent shape
   *
   * @name Base#parent
   * @type Shape
   */
  parentRefs.bind(this, 'parent');

  /**
   * @name Base#label
   * @type Label
   */
  labelRefs.bind(this, 'label');

  /**
   * The list of outgoing connections
   *
   * @name Base#outgoing
   * @type Array<Connection>
   */
  outgoingRefs.bind(this, 'outgoing');

  /**
   * The list of incoming connections
   *
   * @name Base#incoming
   * @type Array<Connection>
   */
  incomingRefs.bind(this, 'incoming');
}


/**
 * A graphical object
 *
 * @class
 * @constructor
 *
 * @extends Base
 */
function Shape() {
  Base.call(this);

  /**
   * The list of children
   *
   * @name Shape#children
   * @type Array<Base>
   */
  parentRefs.bind(this, 'children');

  /**
   * @name Shape#host
   * @type Shape
   */
  attacherRefs.bind(this, 'host');

  /**
   * @name Shape#attachers
   * @type Shape
   */
  attacherRefs.bind(this, 'attachers');
}

inherits(Shape, Base);


/**
 * A root graphical object
 *
 * @class
 * @constructor
 *
 * @extends Shape
 */
function Root() {
  Shape.call(this);
}

inherits(Root, Shape);


/**
 * A label for an element
 *
 * @class
 * @constructor
 *
 * @extends Shape
 */
function Label() {
  Shape.call(this);

  /**
   * The labeled element
   *
   * @name Label#labelTarget
   * @type Base
   */
  labelRefs.bind(this, 'labelTarget');
}

inherits(Label, Shape);


/**
 * A connection between two elements
 *
 * @class
 * @constructor
 *
 * @extends Base
 */
function Connection() {
  Base.call(this);

  /**
   * The element this connection originates from
   *
   * @name Connection#source
   * @type Base
   */
  outgoingRefs.bind(this, 'source');

  /**
   * The element this connection points to
   *
   * @name Connection#target
   * @type Base
   */
  incomingRefs.bind(this, 'target');
}

inherits(Connection, Base);


var types = {
  connection: Connection,
  shape: Shape,
  label: Label,
  root: Root
};

/**
 * Creates a new model element of the specified type
 *
 * @method create
 *
 * @example
 *
 * var shape1 = Model.create('shape', { x: 10, y: 10, width: 100, height: 100 });
 * var shape2 = Model.create('shape', { x: 210, y: 210, width: 100, height: 100 });
 *
 * var connection = Model.create('connection', { waypoints: [ { x: 110, y: 55 }, {x: 210, y: 55 } ] });
 *
 * @param  {String} type lower-cased model name
 * @param  {Object} attrs attributes to initialize the new model instance with
 *
 * @return {Base} the new model instance
 */
module.exports.create = function(type, attrs) {
  var Type = types[type];
  if (!Type) {
    throw new Error('unknown type: <' + type + '>');
  }
  return assign(new Type(), attrs);
};


module.exports.Base = Base;
module.exports.Root = Root;
module.exports.Shape = Shape;
module.exports.Connection = Connection;
module.exports.Label = Label;

},{"190":190,"220":220,"72":72}],55:[function(_dereq_,module,exports){
'use strict';

/**
 * Failsafe remove an element from a collection
 *
 * @param  {Array<Object>} [collection]
 * @param  {Object} [element]
 *
 * @return {Number} the previous index of the element
 */
module.exports.remove = function(collection, element) {

  if (!collection || !element) {
    return -1;
  }

  var idx = collection.indexOf(element);

  if (idx !== -1) {
    collection.splice(idx, 1);
  }

  return idx;
};

/**
 * Fail save add an element to the given connection, ensuring
 * it does not yet exist.
 *
 * @param {Array<Object>} collection
 * @param {Object} element
 * @param {Number} idx
 */
module.exports.add = function(collection, element, idx) {

  if (!collection || !element) {
    return;
  }

  if (typeof idx !== 'number') {
    idx = -1;
  }

  var currentIdx = collection.indexOf(element);

  if (currentIdx !== -1) {

    if (currentIdx === idx) {
      // nothing to do, position has not changed
      return;
    } else {

      if (idx !== -1) {
        // remove from current position
        collection.splice(currentIdx, 1);
      } else {
        // already exists in collection
        return;
      }
    }
  }

  if (idx !== -1) {
    // insert at specified position
    collection.splice(idx, 0, element);
  } else {
    // push to end
    collection.push(element);
  }
};


/**
 * Fail save get the index of an element in a collection.
 *
 * @param {Array<Object>} collection
 * @param {Object} element
 *
 * @return {Number} the index or -1 if collection or element do
 *                  not exist or the element is not contained.
 */
module.exports.indexOf = function(collection, element) {

  if (!collection || !element) {
    return -1;
  }

  return collection.indexOf(element);
};

},{}],56:[function(_dereq_,module,exports){
'use strict';

var isArray = _dereq_(181),
    isNumber = _dereq_(184),
    groupBy = _dereq_(79),
    forEach = _dereq_(78);

/**
 * Adds an element to a collection and returns true if the
 * element was added.
 *
 * @param {Array<Object>} elements
 * @param {Object} e
 * @param {Boolean} unique
 */
function add(elements, e, unique) {
  var canAdd = !unique || elements.indexOf(e) === -1;

  if (canAdd) {
    elements.push(e);
  }

  return canAdd;
}

/**
 * Iterate over each element in a collection, calling the iterator function `fn`
 * with (element, index, recursionDepth).
 *
 * Recurse into all elements that are returned by `fn`.
 *
 * @param  {Object|Array<Object>} elements
 * @param  {Function} fn iterator function called with (element, index, recursionDepth)
 * @param  {Number} [depth] maximum recursion depth
 */
function eachElement(elements, fn, depth) {

  depth = depth || 0;

  if (!isArray(elements)) {
    elements = [ elements ];
  }

  forEach(elements, function(s, i) {
    var filter = fn(s, i, depth);

    if (isArray(filter) && filter.length) {
      eachElement(filter, fn, depth + 1);
    }
  });
}

/**
 * Collects self + child elements up to a given depth from a list of elements.
 *
 * @param  {djs.model.Base|Array<djs.model.Base>} elements the elements to select the children from
 * @param  {Boolean} unique whether to return a unique result set (no duplicates)
 * @param  {Number} maxDepth the depth to search through or -1 for infinite
 *
 * @return {Array<djs.model.Base>} found elements
 */
function selfAndChildren(elements, unique, maxDepth) {
  var result = [],
      processedChildren = [];

  eachElement(elements, function(element, i, depth) {
    add(result, element, unique);

    var children = element.children;

    // max traversal depth not reached yet
    if (maxDepth === -1 || depth < maxDepth) {

      // children exist && children not yet processed
      if (children && add(processedChildren, children, unique)) {
        return children;
      }
    }
  });

  return result;
}

/**
 * Return self + direct children for a number of elements
 *
 * @param  {Array<djs.model.Base>} elements to query
 * @param  {Boolean} allowDuplicates to allow duplicates in the result set
 *
 * @return {Array<djs.model.Base>} the collected elements
 */
function selfAndDirectChildren(elements, allowDuplicates) {
  return selfAndChildren(elements, !allowDuplicates, 1);
}

/**
 * Return self + ALL children for a number of elements
 *
 * @param  {Array<djs.model.Base>} elements to query
 * @param  {Boolean} allowDuplicates to allow duplicates in the result set
 *
 * @return {Array<djs.model.Base>} the collected elements
 */
function selfAndAllChildren(elements, allowDuplicates) {
  return selfAndChildren(elements, !allowDuplicates, -1);
}

/**
 * Gets the the closure for all selected elements,
 * their connections and their attachment's connections
 *
 * @param {Array<djs.model.Base>} elements
 * @return {Object} enclosure
 */
function getClosure(elements) {

  // original elements passed to this function
  var topLevel = groupBy(elements, function(e) { return e.id; });

  var allShapes = {},
      allConnections = {},
      enclosedElements = {},
      enclosedConnections = {};

  function handleConnection(c) {
    if (topLevel[c.source.id] && topLevel[c.target.id]) {
      topLevel[c.id] = [ c ];
    }

    // not enclosed as a child, but maybe logically
    // (connecting two moved elements?)
    if (allShapes[c.source.id] && allShapes[c.target.id]) {
      enclosedConnections[c.id] = enclosedElements[c.id] = c;
    }

    allConnections[c.id] = c;
  }

  function handleElement(element) {

    enclosedElements[element.id] = element;

    if (element.waypoints) {
      // remember connection
      enclosedConnections[element.id] = allConnections[element.id] = element;
    } else {
      // remember shape
      allShapes[element.id] = element;

      // remember all connections
      forEach(element.incoming, handleConnection);

      forEach(element.outgoing, handleConnection);

      // recurse into children
      return element.children;
    }
  }

  eachElement(elements, handleElement);

  return {
    allShapes: allShapes,
    allConnections: allConnections,
    topLevel: topLevel,
    enclosedConnections: enclosedConnections,
    enclosedElements: enclosedElements
  };
}

/**
 * Returns the surrounding bbox for all elements in
 * the array or the element primitive.
 *
 * @param {Array<djs.model.Shape>|djs.model.Shape} elements
 * @param {Boolean} stopRecursion
 */
function getBBox(elements, stopRecursion) {

  stopRecursion = !!stopRecursion;
  if (!isArray(elements)) {
    elements = [elements];
  }

  var minX,
      minY,
      maxX,
      maxY;

  forEach(elements, function(element) {

    // If element is a connection the bbox must be computed first
    var bbox = element;
    if (element.waypoints && !stopRecursion) {
      bbox = getBBox(element.waypoints, true);
    }

    var x = bbox.x,
        y = bbox.y,
        height = bbox.height || 0,
        width  = bbox.width  || 0;

    if (x < minX || minX === undefined) {
      minX = x;
    }
    if (y < minY || minY === undefined) {
      minY = y;
    }

    if ((x + width) > maxX || maxX === undefined) {
      maxX = x + width;
    }
    if ((y + height) > maxY || maxY === undefined) {
      maxY = y + height;
    }
  });

  return {
    x: minX,
    y: minY,
    height: maxY - minY,
    width: maxX - minX
  };
}


/**
 * Returns all elements that are enclosed from the bounding box.
 *
 *   * If bbox.(width|height) is not specified the method returns
 *     all elements with element.x/y > bbox.x/y
 *   * If only bbox.x or bbox.y is specified, method return all elements with
 *     e.x > bbox.x or e.y > bbox.y
 *
 * @param {Array<djs.model.Shape>} elements List of Elements to search through
 * @param {djs.model.Shape} bbox the enclosing bbox.
 *
 * @return {Array<djs.model.Shape>} enclosed elements
 */
function getEnclosedElements(elements, bbox) {

  var filteredElements = {};

  forEach(elements, function(element) {

    var e = element;

    if (e.waypoints) {
      e = getBBox(e);
    }

    if (!isNumber(bbox.y) && (e.x > bbox.x)) {
      filteredElements[element.id] = element;
    }
    if (!isNumber(bbox.x) && (e.y > bbox.y)) {
      filteredElements[element.id] = element;
    }
    if (e.x > bbox.x && e.y > bbox.y) {
      if (isNumber(bbox.width) && isNumber(bbox.height) &&
          e.width  + e.x < bbox.width  + bbox.x &&
          e.height + e.y < bbox.height + bbox.y) {

        filteredElements[element.id] = element;
      } else if (!isNumber(bbox.width) || !isNumber(bbox.height)) {
        filteredElements[element.id] = element;
      }
    }
  });

  return filteredElements;
}


module.exports.add = add;
module.exports.eachElement = eachElement;
module.exports.selfAndDirectChildren = selfAndDirectChildren;
module.exports.selfAndAllChildren = selfAndAllChildren;
module.exports.getBBox = getBBox;
module.exports.getEnclosedElements = getEnclosedElements;

module.exports.getClosure = getClosure;


function getElementType(element) {

  if ('waypoints' in element) {
    return 'connection';
  }

  if ('x' in element) {
    return 'shape';
  }

  return 'root';
}

module.exports.getType = getElementType;
},{"181":181,"184":184,"78":78,"79":79}],57:[function(_dereq_,module,exports){
'use strict';

function __preventDefault(event) {
  return event && event.preventDefault();
}

function __stopPropagation(event, immediate) {
  if (!event) {
    return;
  }

  if (event.stopPropagation) {
    event.stopPropagation();
  }

  if (immediate && event.stopImmediatePropagation) {
    event.stopImmediatePropagation();
  }
}


function getOriginal(event) {
  return event.originalEvent || event.srcEvent;
}

module.exports.getOriginal = getOriginal;


function stopEvent(event, immediate) {
  stopPropagation(event, immediate);
  preventDefault(event);
}

module.exports.stopEvent = stopEvent;


function preventDefault(event) {
  __preventDefault(event);
  __preventDefault(getOriginal(event));
}

module.exports.preventDefault = preventDefault;


function stopPropagation(event, immediate) {
  __stopPropagation(event, immediate);
  __stopPropagation(getOriginal(event), immediate);
}

module.exports.stopPropagation = stopPropagation;


function toPoint(event) {

  if (event.pointers && event.pointers.length) {
    event = event.pointers[0];
  }

  if (event.touches && event.touches.length) {
    event = event.touches[0];
  }

  return event ? {
    x: event.clientX,
    y: event.clientY
  } : null;
}

module.exports.toPoint = toPoint;

},{}],58:[function(_dereq_,module,exports){
'use strict';

var domQuery = _dereq_(206);

/**
 * SVGs for elements are generated by the {@link GraphicsFactory}.
 *
 * This utility gives quick access to the important semantic
 * parts of an element.
 */

/**
 * Returns the visual part of a diagram element
 *
 * @param {Snap<SVGElement>} gfx
 *
 * @return {Snap<SVGElement>}
 */
function getVisual(gfx) {
  return domQuery('.djs-visual', gfx);
}

/**
 * Returns the children for a given diagram element.
 *
 * @param {Snap<SVGElement>} gfx
 * @return {Snap<SVGElement>}
 */
function getChildren(gfx) {
  return gfx.parentNode.childNodes[1];
}

module.exports.getVisual = getVisual;
module.exports.getChildren = getChildren;

},{"206":206}],59:[function(_dereq_,module,exports){
'use strict';

/**
 * Util that provides unique IDs.
 *
 * @class djs.util.IdGenerator
 * @constructor
 * @memberOf djs.util
 *
 * The ids can be customized via a given prefix and contain a random value to avoid collisions.
 *
 * @param {String} prefix a prefix to prepend to generated ids (for better readability)
 */
function IdGenerator(prefix) {

  this._counter = 0;
  this._prefix = (prefix ? prefix + '-' : '') + Math.floor(Math.random() * 1000000000) + '-';
}

module.exports = IdGenerator;

/**
 * Returns a next unique ID.
 *
 * @method djs.util.IdGenerator#next
 *
 * @returns {String} the id
 */
IdGenerator.prototype.next = function() {
  return this._prefix + (++this._counter);
};

},{}],60:[function(_dereq_,module,exports){
'use strict';

var getOriginalEvent = _dereq_(57).getOriginal;

var isMac = _dereq_(61).isMac;


function isPrimaryButton(event) {
  // button === 0 -> left áka primary mouse button
  return !(getOriginalEvent(event) || event).button;
}

module.exports.isPrimaryButton = isPrimaryButton;

module.exports.isMac = isMac;

module.exports.hasPrimaryModifier = function(event) {
  var originalEvent = getOriginalEvent(event) || event;

  if (!isPrimaryButton(event)) {
    return false;
  }

  // Use alt as primary modifier key for mac OS
  if (isMac()) {
    return originalEvent.metaKey;
  } else {
    return originalEvent.ctrlKey;
  }
};


module.exports.hasSecondaryModifier = function(event) {
  var originalEvent = getOriginalEvent(event) || event;

  return isPrimaryButton(event) && originalEvent.shiftKey;
};

},{"57":57,"61":61}],61:[function(_dereq_,module,exports){
'use strict';

module.exports.isMac = function isMac() {
  return (/mac/i).test(navigator.platform);
};
},{}],62:[function(_dereq_,module,exports){
'use strict';

var svgAttr = _dereq_(228),
    svgCreate = _dereq_(231);


module.exports.componentsToPath = function(elements) {
  return elements.join(',').replace(/,?([A-z]),?/g, '$1');
};

function toSVGPoints(points) {
  var result = '';

  for (var i = 0, p; (p = points[i]); i++) {
    result += p.x + ',' + p.y + ' ';
  }

  return result;
}

module.exports.toSVGPoints = toSVGPoints;

module.exports.createLine = function(points, attrs) {

  var line = svgCreate('polyline');
  svgAttr(line, { points: toSVGPoints(points) });

  if (attrs) {
    svgAttr(line, attrs);
  }

  return line;
};

module.exports.updateLine = function(gfx, points) {
  svgAttr(gfx, { points: toSVGPoints(points) });

  return gfx;
};

},{"228":228,"231":231}],63:[function(_dereq_,module,exports){
'use strict';

var svgTransform = _dereq_(235);

var createTransform = _dereq_(232).createTransform;


/**
 * @param {<SVGElement>} element
 * @param {Number} x
 * @param {Number} y
 * @param {Number} angle
 * @param {Number} amount
 */
module.exports.transform = function(gfx, x, y, angle, amount) {
  var translate = createTransform();
  translate.setTranslate(x, y);

  var rotate = createTransform();
  rotate.setRotate(angle, 0, 0);

  var scale = createTransform();
  scale.setScale(amount || 1, amount || 1);

  svgTransform(gfx, [ translate, rotate, scale ]);
};


/**
 * @param {SVGElement} element
 * @param {Number} x
 * @param {Number} y
 */
module.exports.translate = function(gfx, x, y) {
  var translate = createTransform();
  translate.setTranslate(x, y);

  svgTransform(gfx, translate);
};


/**
 * @param {SVGElement} element
 * @param {Number} angle
 */
module.exports.rotate = function(gfx, angle) {
  var rotate = createTransform();
  rotate.setRotate(angle, 0, 0);

  svgTransform(gfx, rotate);
};


/**
 * @param {SVGElement} element
 * @param {Number} amount
 */
module.exports.scale = function(gfx, amount) {
  var scale = createTransform();
  scale.setScale(amount, amount);

  svgTransform(gfx, scale);
};

},{"232":232,"235":235}],64:[function(_dereq_,module,exports){
'use strict';

var isObject = _dereq_(185),
    assign = _dereq_(190),
    pick = _dereq_(196),
    forEach = _dereq_(78),
    reduce = _dereq_(81),
    merge = _dereq_(193);

var svgAppend = _dereq_(226),
    svgAttr = _dereq_(228),
    svgCreate = _dereq_(231),
    svgRemove = _dereq_(234);

var DEFAULT_BOX_PADDING = 0;

var DEFAULT_LABEL_SIZE = {
  width: 150,
  height: 50
};


function parseAlign(align) {

  var parts = align.split('-');

  return {
    horizontal: parts[0] || 'center',
    vertical: parts[1] || 'top'
  };
}

function parsePadding(padding) {

  if (isObject(padding)) {
    return assign({ top: 0, left: 0, right: 0, bottom: 0 }, padding);
  } else {
    return {
      top: padding,
      left: padding,
      right: padding,
      bottom: padding
    };
  }
}

function getTextBBox(text, fakeText) {

  fakeText.textContent = text;

  try {
    var bbox,
        emptyLine = text === '';

    // add dummy text, when line is empty to determine correct height
    fakeText.textContent = emptyLine ? 'dummy' : text;

    bbox = pick(fakeText.getBBox(), [ 'width', 'height' ]);

    if (emptyLine) {
      // correct width
      bbox.width = 0;
    }

    return bbox;
  } catch (e) {
    return { width: 0, height: 0 };
  }
}


/**
 * Layout the next line and return the layouted element.
 *
 * Alters the lines passed.
 *
 * @param  {Array<String>} lines
 * @return {Object} the line descriptor, an object { width, height, text }
 */
function layoutNext(lines, maxWidth, fakeText) {

  var originalLine = lines.shift(),
      fitLine = originalLine;

  var textBBox;

  for (;;) {
    textBBox = getTextBBox(fitLine, fakeText);

    textBBox.width = fitLine ? textBBox.width : 0;

    // try to fit
    if (fitLine === ' ' || fitLine === '' || textBBox.width < Math.round(maxWidth) || fitLine.length < 2) {
      return fit(lines, fitLine, originalLine, textBBox);
    }

    fitLine = shortenLine(fitLine, textBBox.width, maxWidth);
  }
}

function fit(lines, fitLine, originalLine, textBBox) {
  if (fitLine.length < originalLine.length) {
    var remainder = originalLine.slice(fitLine.length).trim();

    lines.unshift(remainder);
  }
  return { width: textBBox.width, height: textBBox.height, text: fitLine };
}


/**
 * Shortens a line based on spacing and hyphens.
 * Returns the shortened result on success.
 *
 * @param  {String} line
 * @param  {Number} maxLength the maximum characters of the string
 * @return {String} the shortened string
 */
function semanticShorten(line, maxLength) {
  var parts = line.split(/(\s|-)/g),
      part,
      shortenedParts = [],
      length = 0;

  // try to shorten via spaces + hyphens
  if (parts.length > 1) {
    while ((part = parts.shift())) {
      if (part.length + length < maxLength) {
        shortenedParts.push(part);
        length += part.length;
      } else {
        // remove previous part, too if hyphen does not fit anymore
        if (part === '-') {
          shortenedParts.pop();
        }

        break;
      }
    }
  }

  return shortenedParts.join('');
}


function shortenLine(line, width, maxWidth) {
  var length = Math.max(line.length * (maxWidth / width), 1);

  // try to shorten semantically (i.e. based on spaces and hyphens)
  var shortenedLine = semanticShorten(line, length);

  if (!shortenedLine) {

    // force shorten by cutting the long word
    shortenedLine = line.slice(0, Math.max(Math.round(length - 1), 1));
  }

  return shortenedLine;
}


function getHelperSvg() {
  var helperSvg = document.getElementById('helper-svg');

  if (!helperSvg) {
    helperSvg = svgCreate('svg');

    svgAttr(helperSvg, {
      id: 'helper-svg',
      width: 0,
      height: 0,
      style: 'visibility: hidden; position: fixed'
    });

    document.body.appendChild(helperSvg);
  }

  return helperSvg;
}


/**
 * Creates a new label utility
 *
 * @param {Object} config
 * @param {Dimensions} config.size
 * @param {Number} config.padding
 * @param {Object} config.style
 * @param {String} config.align
 */
function Text(config) {

  this._config = assign({}, {
    size: DEFAULT_LABEL_SIZE,
    padding: DEFAULT_BOX_PADDING,
    style: {},
    align: 'center-top'
  }, config || {});
}

/**
 * Returns the layouted text as an SVG element.
 *
 * @param {String} text
 * @param {Object} options
 *
 * @return {SVGElement}
 */
Text.prototype.createText = function(text, options) {
  return this.layoutText(text, options).element;
};

/**
 * Returns a labels layouted dimensions.
 *
 * @param {String} text to layout
 * @param {Object} options
 *
 * @return {Dimensions}
 */
Text.prototype.getDimensions = function(text, options) {
  return this.layoutText(text, options).dimensions;
};

/**
 * Creates and returns a label and its bounding box.
 *
 * @method Text#createText
 *
 * @param {String} text the text to render on the label
 * @param {Object} options
 * @param {String} options.align how to align in the bounding box.
 *                               Any of { 'center-middle', 'center-top' },
 *                               defaults to 'center-top'.
 * @param {String} options.style style to be applied to the text
 * @param {boolean} options.fitBox indicates if box will be recalculated to
 *                                 fit text
 *
 * @return {Object} { element, dimensions }
 */
Text.prototype.layoutText = function(text, options) {
  var box = merge({}, this._config.size, options.box || {}),
      style = merge({}, this._config.style, options.style || {}),
      align = parseAlign(options.align || this._config.align),
      padding = parsePadding(options.padding !== undefined ? options.padding : this._config.padding),
      fitBox = options.fitBox || false;

  var lines = text.split(/\r?\n/g),
      layouted = [];

  var maxWidth = box.width - padding.left - padding.right;

  // ensure correct rendering by attaching helper text node to invisible SVG
  var helperText = svgCreate('text');
  svgAttr(helperText, { x: 0, y: 0 });
  svgAttr(helperText, style);

  var helperSvg = getHelperSvg();

  svgAppend(helperSvg, helperText);

  while (lines.length) {
    layouted.push(layoutNext(lines, maxWidth, helperText));
  }

  var totalHeight = reduce(layouted, function(sum, line, idx) {
    return sum + line.height;
  }, 0);

  var maxLineWidth = reduce(layouted, function(sum, line, idx) {
    return line.width > sum ? line.width : sum;
  }, 0);

  // the y position of the next line
  var y, x;

  switch (align.vertical) {
  case 'middle':
    y = (box.height - totalHeight) / 2 - layouted[0].height / 4;
    break;

  default:
    y = padding.top;
  }

  var textElement = svgCreate('text');

  svgAttr(textElement, style);

  // layout each line taking into account that parent
  // shape might resize to fit text size
  forEach(layouted, function(line) {
    y += line.height;

    switch (align.horizontal) {
    case 'left':
      x = padding.left;
      break;

    case 'right':
      x = ((fitBox ? maxLineWidth : maxWidth)
        - padding.right - line.width);
      break;

    default:
      // aka center
      x = Math.max((((fitBox ? maxLineWidth : maxWidth)
        - line.width) / 2 + padding.left), 0);
    }

    var tspan = svgCreate('tspan');
    svgAttr(tspan, { x: x, y: y });

    tspan.textContent = line.text;

    svgAppend(textElement, tspan);
  });

  svgRemove(helperText);

  var dimensions = {
    width: maxLineWidth,
    height: totalHeight
  };

  return {
    dimensions: dimensions,
    element: textElement
  };
};

module.exports = Text;

},{"185":185,"190":190,"193":193,"196":196,"226":226,"228":228,"231":231,"234":234,"78":78,"81":81}],65:[function(_dereq_,module,exports){

var isArray = function(obj) {
  return Object.prototype.toString.call(obj) === '[object Array]';
};

var annotate = function() {
  var args = Array.prototype.slice.call(arguments);
  
  if (args.length === 1 && isArray(args[0])) {
    args = args[0];
  }

  var fn = args.pop();

  fn.$inject = args;

  return fn;
};


// Current limitations:
// - can't put into "function arg" comments
// function /* (no parenthesis like this) */ (){}
// function abc( /* xx (no parenthesis like this) */ a, b) {}
//
// Just put the comment before function or inside:
// /* (((this is fine))) */ function(a, b) {}
// function abc(a) { /* (((this is fine))) */}

var FN_ARGS = /^function\s*[^\(]*\(\s*([^\)]*)\)/m;
var FN_ARG = /\/\*([^\*]*)\*\//m;

var parse = function(fn) {
  if (typeof fn !== 'function') {
    throw new Error('Cannot annotate "' + fn + '". Expected a function!');
  }

  var match = fn.toString().match(FN_ARGS);
  return match[1] && match[1].split(',').map(function(arg) {
    match = arg.match(FN_ARG);
    return match ? match[1].trim() : arg.trim();
  }) || [];
};


exports.annotate = annotate;
exports.parse = parse;
exports.isArray = isArray;

},{}],66:[function(_dereq_,module,exports){
module.exports = {
  annotate: _dereq_(65).annotate,
  Module: _dereq_(68),
  Injector: _dereq_(67)
};

},{"65":65,"67":67,"68":68}],67:[function(_dereq_,module,exports){
var Module = _dereq_(68);
var autoAnnotate = _dereq_(65).parse;
var annotate = _dereq_(65).annotate;
var isArray = _dereq_(65).isArray;


var Injector = function(modules, parent) {
  parent = parent || {
    get: function(name, strict) {
      currentlyResolving.push(name);

      if (strict === false) {
        return null;
      } else {
        throw error('No provider for "' + name + '"!');
      }
    }
  };

  var currentlyResolving = [];
  var providers = this._providers = Object.create(parent._providers || null);
  var instances = this._instances = Object.create(null);

  var self = instances.injector = this;

  var error = function(msg) {
    var stack = currentlyResolving.join(' -> ');
    currentlyResolving.length = 0;
    return new Error(stack ? msg + ' (Resolving: ' + stack + ')' : msg);
  };

  /**
   * Return a named service.
   *
   * @param {String} name
   * @param {Boolean} [strict=true] if false, resolve missing services to null
   *
   * @return {Object}
   */
  var get = function(name, strict) {
    if (!providers[name] && name.indexOf('.') !== -1) {
      var parts = name.split('.');
      var pivot = get(parts.shift());

      while(parts.length) {
        pivot = pivot[parts.shift()];
      }

      return pivot;
    }

    if (Object.hasOwnProperty.call(instances, name)) {
      return instances[name];
    }

    if (Object.hasOwnProperty.call(providers, name)) {
      if (currentlyResolving.indexOf(name) !== -1) {
        currentlyResolving.push(name);
        throw error('Cannot resolve circular dependency!');
      }

      currentlyResolving.push(name);
      instances[name] = providers[name][0](providers[name][1]);
      currentlyResolving.pop();

      return instances[name];
    }

    return parent.get(name, strict);
  };

  var instantiate = function(Type) {
    var instance = Object.create(Type.prototype);
    var returned = invoke(Type, instance);

    return typeof returned === 'object' ? returned : instance;
  };

  var invoke = function(fn, context) {
    if (typeof fn !== 'function') {
      if (isArray(fn)) {
        fn = annotate(fn.slice());
      } else {
        throw new Error('Cannot invoke "' + fn + '". Expected a function!');
      }
    }

    var inject = fn.$inject && fn.$inject || autoAnnotate(fn);
    var dependencies = inject.map(function(dep) {
      return get(dep);
    });

    // TODO(vojta): optimize without apply
    return fn.apply(context, dependencies);
  };


  var createPrivateInjectorFactory = function(privateChildInjector) {
    return annotate(function(key) {
      return privateChildInjector.get(key);
    });
  };

  var createChild = function(modules, forceNewInstances) {
    if (forceNewInstances && forceNewInstances.length) {
      var fromParentModule = Object.create(null);
      var matchedScopes = Object.create(null);

      var privateInjectorsCache = [];
      var privateChildInjectors = [];
      var privateChildFactories = [];

      var provider;
      var cacheIdx;
      var privateChildInjector;
      var privateChildInjectorFactory;
      for (var name in providers) {
        provider = providers[name];

        if (forceNewInstances.indexOf(name) !== -1) {
          if (provider[2] === 'private') {
            cacheIdx = privateInjectorsCache.indexOf(provider[3]);
            if (cacheIdx === -1) {
              privateChildInjector = provider[3].createChild([], forceNewInstances);
              privateChildInjectorFactory = createPrivateInjectorFactory(privateChildInjector);
              privateInjectorsCache.push(provider[3]);
              privateChildInjectors.push(privateChildInjector);
              privateChildFactories.push(privateChildInjectorFactory);
              fromParentModule[name] = [privateChildInjectorFactory, name, 'private', privateChildInjector];
            } else {
              fromParentModule[name] = [privateChildFactories[cacheIdx], name, 'private', privateChildInjectors[cacheIdx]];
            }
          } else {
            fromParentModule[name] = [provider[2], provider[1]];
          }
          matchedScopes[name] = true;
        }

        if ((provider[2] === 'factory' || provider[2] === 'type') && provider[1].$scope) {
          /*jshint -W083 */
          forceNewInstances.forEach(function(scope) {
            if (provider[1].$scope.indexOf(scope) !== -1) {
              fromParentModule[name] = [provider[2], provider[1]];
              matchedScopes[scope] = true;
            }
          });
        }
      }

      forceNewInstances.forEach(function(scope) {
        if (!matchedScopes[scope]) {
          throw new Error('No provider for "' + scope + '". Cannot use provider from the parent!');
        }
      });

      modules.unshift(fromParentModule);
    }

    return new Injector(modules, self);
  };

  var factoryMap = {
    factory: invoke,
    type: instantiate,
    value: function(value) {
      return value;
    }
  };

  modules.forEach(function(module) {

    function arrayUnwrap(type, value) {
      if (type !== 'value' && isArray(value)) {
        value = annotate(value.slice());
      }

      return value;
    }

    // TODO(vojta): handle wrong inputs (modules)
    if (module instanceof Module) {
      module.forEach(function(provider) {
        var name = provider[0];
        var type = provider[1];
        var value = provider[2];

        providers[name] = [factoryMap[type], arrayUnwrap(type, value), type];
      });
    } else if (typeof module === 'object') {
      if (module.__exports__) {
        var clonedModule = Object.keys(module).reduce(function(m, key) {
          if (key.substring(0, 2) !== '__') {
            m[key] = module[key];
          }
          return m;
        }, Object.create(null));

        var privateInjector = new Injector((module.__modules__ || []).concat([clonedModule]), self);
        var getFromPrivateInjector = annotate(function(key) {
          return privateInjector.get(key);
        });
        module.__exports__.forEach(function(key) {
          providers[key] = [getFromPrivateInjector, key, 'private', privateInjector];
        });
      } else {
        Object.keys(module).forEach(function(name) {
          if (module[name][2] === 'private') {
            providers[name] = module[name];
            return;
          }

          var type = module[name][0];
          var value = module[name][1];

          providers[name] = [factoryMap[type], arrayUnwrap(type, value), type];
        });
      }
    }
  });

  // public API
  this.get = get;
  this.invoke = invoke;
  this.instantiate = instantiate;
  this.createChild = createChild;
};

module.exports = Injector;

},{"65":65,"68":68}],68:[function(_dereq_,module,exports){
var Module = function() {
  var providers = [];

  this.factory = function(name, factory) {
    providers.push([name, 'factory', factory]);
    return this;
  };

  this.value = function(name, value) {
    providers.push([name, 'value', value]);
    return this;
  };

  this.type = function(name, type) {
    providers.push([name, 'type', type]);
    return this;
  };

  this.forEach = function(iterator) {
    providers.forEach(iterator);
  };
};

module.exports = Module;

},{}],69:[function(_dereq_,module,exports){

/**
 * Expose `parse`.
 */

module.exports = parse;

/**
 * Tests for browser support.
 */

var innerHTMLBug = false;
var bugTestDiv;
if (typeof document !== 'undefined') {
  bugTestDiv = document.createElement('div');
  // Setup
  bugTestDiv.innerHTML = '  <link/><table></table><a href="/a">a</a><input type="checkbox"/>';
  // Make sure that link elements get serialized correctly by innerHTML
  // This requires a wrapper element in IE
  innerHTMLBug = !bugTestDiv.getElementsByTagName('link').length;
  bugTestDiv = undefined;
}

/**
 * Wrap map from jquery.
 */

var map = {
  legend: [1, '<fieldset>', '</fieldset>'],
  tr: [2, '<table><tbody>', '</tbody></table>'],
  col: [2, '<table><tbody></tbody><colgroup>', '</colgroup></table>'],
  // for script/link/style tags to work in IE6-8, you have to wrap
  // in a div with a non-whitespace character in front, ha!
  _default: innerHTMLBug ? [1, 'X<div>', '</div>'] : [0, '', '']
};

map.td =
map.th = [3, '<table><tbody><tr>', '</tr></tbody></table>'];

map.option =
map.optgroup = [1, '<select multiple="multiple">', '</select>'];

map.thead =
map.tbody =
map.colgroup =
map.caption =
map.tfoot = [1, '<table>', '</table>'];

map.polyline =
map.ellipse =
map.polygon =
map.circle =
map.text =
map.line =
map.path =
map.rect =
map.g = [1, '<svg xmlns="http://www.w3.org/2000/svg" version="1.1">','</svg>'];

/**
 * Parse `html` and return a DOM Node instance, which could be a TextNode,
 * HTML DOM Node of some kind (<div> for example), or a DocumentFragment
 * instance, depending on the contents of the `html` string.
 *
 * @param {String} html - HTML string to "domify"
 * @param {Document} doc - The `document` instance to create the Node for
 * @return {DOMNode} the TextNode, DOM Node, or DocumentFragment instance
 * @api private
 */

function parse(html, doc) {
  if ('string' != typeof html) throw new TypeError('String expected');

  // default to the global `document` object
  if (!doc) doc = document;

  // tag name
  var m = /<([\w:]+)/.exec(html);
  if (!m) return doc.createTextNode(html);

  html = html.replace(/^\s+|\s+$/g, ''); // Remove leading/trailing whitespace

  var tag = m[1];

  // body support
  if (tag == 'body') {
    var el = doc.createElement('html');
    el.innerHTML = html;
    return el.removeChild(el.lastChild);
  }

  // wrap map
  var wrap = map[tag] || map._default;
  var depth = wrap[0];
  var prefix = wrap[1];
  var suffix = wrap[2];
  var el = doc.createElement('div');
  el.innerHTML = prefix + html + suffix;
  while (depth--) el = el.lastChild;

  // one element
  if (el.firstChild == el.lastChild) {
    return el.removeChild(el.firstChild);
  }

  // several elements
  var fragment = doc.createDocumentFragment();
  while (el.firstChild) {
    fragment.appendChild(el.removeChild(el.firstChild));
  }

  return fragment;
}

},{}],70:[function(_dereq_,module,exports){
var hat = module.exports = function (bits, base) {
    if (!base) base = 16;
    if (bits === undefined) bits = 128;
    if (bits <= 0) return '0';
    
    var digits = Math.log(Math.pow(2, bits)) / Math.log(base);
    for (var i = 2; digits === Infinity; i *= 2) {
        digits = Math.log(Math.pow(2, bits / i)) / Math.log(base) * i;
    }
    
    var rem = digits - Math.floor(digits);
    
    var res = '';
    
    for (var i = 0; i < Math.floor(digits); i++) {
        var x = Math.floor(Math.random() * base).toString(base);
        res = x + res;
    }
    
    if (rem) {
        var b = Math.pow(base, rem);
        var x = Math.floor(Math.random() * b).toString(base);
        res = x + res;
    }
    
    var parsed = parseInt(res, base);
    if (parsed !== Infinity && parsed >= Math.pow(2, bits)) {
        return hat(bits, base)
    }
    else return res;
};

hat.rack = function (bits, base, expandBy) {
    var fn = function (data) {
        var iters = 0;
        do {
            if (iters ++ > 10) {
                if (expandBy) bits += expandBy;
                else throw new Error('too many ID collisions, use more bits')
            }
            
            var id = hat(bits, base);
        } while (Object.hasOwnProperty.call(hats, id));
        
        hats[id] = data;
        return id;
    };
    var hats = fn.hats = {};
    
    fn.get = function (id) {
        return fn.hats[id];
    };
    
    fn.set = function (id, value) {
        fn.hats[id] = value;
        return fn;
    };
    
    fn.bits = bits || 128;
    fn.base = base || 16;
    return fn;
};

},{}],71:[function(_dereq_,module,exports){
'use strict';

var hat = _dereq_(70);


/**
 * Create a new id generator / cache instance.
 *
 * You may optionally provide a seed that is used internally.
 *
 * @param {Seed} seed
 */
function Ids(seed) {

  if (!(this instanceof Ids)) {
    return new Ids(seed);
  }

  seed = seed || [ 128, 36, 1 ];
  this._seed = seed.length ? hat.rack(seed[0], seed[1], seed[2]) : seed;
}

module.exports = Ids;

/**
 * Generate a next id.
 *
 * @param {Object} [element] element to bind the id to
 *
 * @return {String} id
 */
Ids.prototype.next = function(element) {
  return this._seed(element || true);
};

/**
 * Generate a next id with a given prefix.
 *
 * @param {Object} [element] element to bind the id to
 *
 * @return {String} id
 */
Ids.prototype.nextPrefixed = function(prefix, element) {
  var id;

  do {
    id = prefix + this.next(true);
  } while (this.assigned(id));

  // claim {prefix}{random}
  this.claim(id, element);

  // return
  return id;
};

/**
 * Manually claim an existing id.
 *
 * @param {String} id
 * @param {String} [element] element the id is claimed by
 */
Ids.prototype.claim = function(id, element) {
  this._seed.set(id, element || true);
};

/**
 * Returns true if the given id has already been assigned.
 *
 * @param  {String} id
 * @return {Boolean}
 */
Ids.prototype.assigned = function(id) {
  return this._seed.get(id) || false;
};

/**
 * Unclaim an id.
 *
 * @param  {String} id the id to unclaim
 */
Ids.prototype.unclaim = function(id) {
  delete this._seed.hats[id];
};


/**
 * Clear all claimed ids.
 */
Ids.prototype.clear = function() {

  var hats = this._seed.hats,
      id;

  for (id in hats) {
    this.unclaim(id);
  }
};
},{"70":70}],72:[function(_dereq_,module,exports){
if (typeof Object.create === 'function') {
  // implementation from standard node.js 'util' module
  module.exports = function inherits(ctor, superCtor) {
    ctor.super_ = superCtor
    ctor.prototype = Object.create(superCtor.prototype, {
      constructor: {
        value: ctor,
        enumerable: false,
        writable: true,
        configurable: true
      }
    });
  };
} else {
  // old school shim for old browsers
  module.exports = function inherits(ctor, superCtor) {
    ctor.super_ = superCtor
    var TempCtor = function () {}
    TempCtor.prototype = superCtor.prototype
    ctor.prototype = new TempCtor()
    ctor.prototype.constructor = ctor
  }
}

},{}],73:[function(_dereq_,module,exports){
/**
 * Gets the last element of `array`.
 *
 * @static
 * @memberOf _
 * @category Array
 * @param {Array} array The array to query.
 * @returns {*} Returns the last element of `array`.
 * @example
 *
 * _.last([1, 2, 3]);
 * // => 3
 */
function last(array) {
  var length = array ? array.length : 0;
  return length ? array[length - 1] : undefined;
}

module.exports = last;

},{}],74:[function(_dereq_,module,exports){
var LazyWrapper = _dereq_(88),
    LodashWrapper = _dereq_(89),
    baseLodash = _dereq_(120),
    isArray = _dereq_(181),
    isObjectLike = _dereq_(166),
    wrapperClone = _dereq_(179);

/** Used for native method references. */
var objectProto = Object.prototype;

/** Used to check objects for own properties. */
var hasOwnProperty = objectProto.hasOwnProperty;

/**
 * Creates a `lodash` object which wraps `value` to enable implicit chaining.
 * Methods that operate on and return arrays, collections, and functions can
 * be chained together. Methods that retrieve a single value or may return a
 * primitive value will automatically end the chain returning the unwrapped
 * value. Explicit chaining may be enabled using `_.chain`. The execution of
 * chained methods is lazy, that is, execution is deferred until `_#value`
 * is implicitly or explicitly called.
 *
 * Lazy evaluation allows several methods to support shortcut fusion. Shortcut
 * fusion is an optimization strategy which merge iteratee calls; this can help
 * to avoid the creation of intermediate data structures and greatly reduce the
 * number of iteratee executions.
 *
 * Chaining is supported in custom builds as long as the `_#value` method is
 * directly or indirectly included in the build.
 *
 * In addition to lodash methods, wrappers have `Array` and `String` methods.
 *
 * The wrapper `Array` methods are:
 * `concat`, `join`, `pop`, `push`, `reverse`, `shift`, `slice`, `sort`,
 * `splice`, and `unshift`
 *
 * The wrapper `String` methods are:
 * `replace` and `split`
 *
 * The wrapper methods that support shortcut fusion are:
 * `compact`, `drop`, `dropRight`, `dropRightWhile`, `dropWhile`, `filter`,
 * `first`, `initial`, `last`, `map`, `pluck`, `reject`, `rest`, `reverse`,
 * `slice`, `take`, `takeRight`, `takeRightWhile`, `takeWhile`, `toArray`,
 * and `where`
 *
 * The chainable wrapper methods are:
 * `after`, `ary`, `assign`, `at`, `before`, `bind`, `bindAll`, `bindKey`,
 * `callback`, `chain`, `chunk`, `commit`, `compact`, `concat`, `constant`,
 * `countBy`, `create`, `curry`, `debounce`, `defaults`, `defaultsDeep`,
 * `defer`, `delay`, `difference`, `drop`, `dropRight`, `dropRightWhile`,
 * `dropWhile`, `fill`, `filter`, `flatten`, `flattenDeep`, `flow`, `flowRight`,
 * `forEach`, `forEachRight`, `forIn`, `forInRight`, `forOwn`, `forOwnRight`,
 * `functions`, `groupBy`, `indexBy`, `initial`, `intersection`, `invert`,
 * `invoke`, `keys`, `keysIn`, `map`, `mapKeys`, `mapValues`, `matches`,
 * `matchesProperty`, `memoize`, `merge`, `method`, `methodOf`, `mixin`,
 * `modArgs`, `negate`, `omit`, `once`, `pairs`, `partial`, `partialRight`,
 * `partition`, `pick`, `plant`, `pluck`, `property`, `propertyOf`, `pull`,
 * `pullAt`, `push`, `range`, `rearg`, `reject`, `remove`, `rest`, `restParam`,
 * `reverse`, `set`, `shuffle`, `slice`, `sort`, `sortBy`, `sortByAll`,
 * `sortByOrder`, `splice`, `spread`, `take`, `takeRight`, `takeRightWhile`,
 * `takeWhile`, `tap`, `throttle`, `thru`, `times`, `toArray`, `toPlainObject`,
 * `transform`, `union`, `uniq`, `unshift`, `unzip`, `unzipWith`, `values`,
 * `valuesIn`, `where`, `without`, `wrap`, `xor`, `zip`, `zipObject`, `zipWith`
 *
 * The wrapper methods that are **not** chainable by default are:
 * `add`, `attempt`, `camelCase`, `capitalize`, `ceil`, `clone`, `cloneDeep`,
 * `deburr`, `endsWith`, `escape`, `escapeRegExp`, `every`, `find`, `findIndex`,
 * `findKey`, `findLast`, `findLastIndex`, `findLastKey`, `findWhere`, `first`,
 * `floor`, `get`, `gt`, `gte`, `has`, `identity`, `includes`, `indexOf`,
 * `inRange`, `isArguments`, `isArray`, `isBoolean`, `isDate`, `isElement`,
 * `isEmpty`, `isEqual`, `isError`, `isFinite` `isFunction`, `isMatch`,
 * `isNative`, `isNaN`, `isNull`, `isNumber`, `isObject`, `isPlainObject`,
 * `isRegExp`, `isString`, `isUndefined`, `isTypedArray`, `join`, `kebabCase`,
 * `last`, `lastIndexOf`, `lt`, `lte`, `max`, `min`, `noConflict`, `noop`,
 * `now`, `pad`, `padLeft`, `padRight`, `parseInt`, `pop`, `random`, `reduce`,
 * `reduceRight`, `repeat`, `result`, `round`, `runInContext`, `shift`, `size`,
 * `snakeCase`, `some`, `sortedIndex`, `sortedLastIndex`, `startCase`,
 * `startsWith`, `sum`, `template`, `trim`, `trimLeft`, `trimRight`, `trunc`,
 * `unescape`, `uniqueId`, `value`, and `words`
 *
 * The wrapper method `sample` will return a wrapped value when `n` is provided,
 * otherwise an unwrapped value is returned.
 *
 * @name _
 * @constructor
 * @category Chain
 * @param {*} value The value to wrap in a `lodash` instance.
 * @returns {Object} Returns the new `lodash` wrapper instance.
 * @example
 *
 * var wrapped = _([1, 2, 3]);
 *
 * // returns an unwrapped value
 * wrapped.reduce(function(total, n) {
 *   return total + n;
 * });
 * // => 6
 *
 * // returns a wrapped value
 * var squares = wrapped.map(function(n) {
 *   return n * n;
 * });
 *
 * _.isArray(squares);
 * // => false
 *
 * _.isArray(squares.value());
 * // => true
 */
function lodash(value) {
  if (isObjectLike(value) && !isArray(value) && !(value instanceof LazyWrapper)) {
    if (value instanceof LodashWrapper) {
      return value;
    }
    if (hasOwnProperty.call(value, '__chain__') && hasOwnProperty.call(value, '__wrapped__')) {
      return wrapperClone(value);
    }
  }
  return new LodashWrapper(value);
}

// Ensure wrappers are instances of `baseLodash`.
lodash.prototype = baseLodash.prototype;

module.exports = lodash;

},{"120":120,"166":166,"179":179,"181":181,"88":88,"89":89}],75:[function(_dereq_,module,exports){
var arrayEvery = _dereq_(93),
    baseCallback = _dereq_(101),
    baseEvery = _dereq_(107),
    isArray = _dereq_(181),
    isIterateeCall = _dereq_(162);

/**
 * Checks if `predicate` returns truthy for **all** elements of `collection`.
 * The predicate is bound to `thisArg` and invoked with three arguments:
 * (value, index|key, collection).
 *
 * If a property name is provided for `predicate` the created `_.property`
 * style callback returns the property value of the given element.
 *
 * If a value is also provided for `thisArg` the created `_.matchesProperty`
 * style callback returns `true` for elements that have a matching property
 * value, else `false`.
 *
 * If an object is provided for `predicate` the created `_.matches` style
 * callback returns `true` for elements that have the properties of the given
 * object, else `false`.
 *
 * @static
 * @memberOf _
 * @alias all
 * @category Collection
 * @param {Array|Object|string} collection The collection to iterate over.
 * @param {Function|Object|string} [predicate=_.identity] The function invoked
 *  per iteration.
 * @param {*} [thisArg] The `this` binding of `predicate`.
 * @returns {boolean} Returns `true` if all elements pass the predicate check,
 *  else `false`.
 * @example
 *
 * _.every([true, 1, null, 'yes'], Boolean);
 * // => false
 *
 * var users = [
 *   { 'user': 'barney', 'active': false },
 *   { 'user': 'fred',   'active': false }
 * ];
 *
 * // using the `_.matches` callback shorthand
 * _.every(users, { 'user': 'barney', 'active': false });
 * // => false
 *
 * // using the `_.matchesProperty` callback shorthand
 * _.every(users, 'active', false);
 * // => true
 *
 * // using the `_.property` callback shorthand
 * _.every(users, 'active');
 * // => false
 */
function every(collection, predicate, thisArg) {
  var func = isArray(collection) ? arrayEvery : baseEvery;
  if (thisArg && isIterateeCall(collection, predicate, thisArg)) {
    predicate = undefined;
  }
  if (typeof predicate != 'function' || thisArg !== undefined) {
    predicate = baseCallback(predicate, thisArg, 3);
  }
  return func(collection, predicate);
}

module.exports = every;

},{"101":101,"107":107,"162":162,"181":181,"93":93}],76:[function(_dereq_,module,exports){
var arrayFilter = _dereq_(94),
    baseCallback = _dereq_(101),
    baseFilter = _dereq_(108),
    isArray = _dereq_(181);

/**
 * Iterates over elements of `collection`, returning an array of all elements
 * `predicate` returns truthy for. The predicate is bound to `thisArg` and
 * invoked with three arguments: (value, index|key, collection).
 *
 * If a property name is provided for `predicate` the created `_.property`
 * style callback returns the property value of the given element.
 *
 * If a value is also provided for `thisArg` the created `_.matchesProperty`
 * style callback returns `true` for elements that have a matching property
 * value, else `false`.
 *
 * If an object is provided for `predicate` the created `_.matches` style
 * callback returns `true` for elements that have the properties of the given
 * object, else `false`.
 *
 * @static
 * @memberOf _
 * @alias select
 * @category Collection
 * @param {Array|Object|string} collection The collection to iterate over.
 * @param {Function|Object|string} [predicate=_.identity] The function invoked
 *  per iteration.
 * @param {*} [thisArg] The `this` binding of `predicate`.
 * @returns {Array} Returns the new filtered array.
 * @example
 *
 * _.filter([4, 5, 6], function(n) {
 *   return n % 2 == 0;
 * });
 * // => [4, 6]
 *
 * var users = [
 *   { 'user': 'barney', 'age': 36, 'active': true },
 *   { 'user': 'fred',   'age': 40, 'active': false }
 * ];
 *
 * // using the `_.matches` callback shorthand
 * _.pluck(_.filter(users, { 'age': 36, 'active': true }), 'user');
 * // => ['barney']
 *
 * // using the `_.matchesProperty` callback shorthand
 * _.pluck(_.filter(users, 'active', false), 'user');
 * // => ['fred']
 *
 * // using the `_.property` callback shorthand
 * _.pluck(_.filter(users, 'active'), 'user');
 * // => ['barney']
 */
function filter(collection, predicate, thisArg) {
  var func = isArray(collection) ? arrayFilter : baseFilter;
  predicate = baseCallback(predicate, thisArg, 3);
  return func(collection, predicate);
}

module.exports = filter;

},{"101":101,"108":108,"181":181,"94":94}],77:[function(_dereq_,module,exports){
var baseEach = _dereq_(106),
    createFind = _dereq_(145);

/**
 * Iterates over elements of `collection`, returning the first element
 * `predicate` returns truthy for. The predicate is bound to `thisArg` and
 * invoked with three arguments: (value, index|key, collection).
 *
 * If a property name is provided for `predicate` the created `_.property`
 * style callback returns the property value of the given element.
 *
 * If a value is also provided for `thisArg` the created `_.matchesProperty`
 * style callback returns `true` for elements that have a matching property
 * value, else `false`.
 *
 * If an object is provided for `predicate` the created `_.matches` style
 * callback returns `true` for elements that have the properties of the given
 * object, else `false`.
 *
 * @static
 * @memberOf _
 * @alias detect
 * @category Collection
 * @param {Array|Object|string} collection The collection to search.
 * @param {Function|Object|string} [predicate=_.identity] The function invoked
 *  per iteration.
 * @param {*} [thisArg] The `this` binding of `predicate`.
 * @returns {*} Returns the matched element, else `undefined`.
 * @example
 *
 * var users = [
 *   { 'user': 'barney',  'age': 36, 'active': true },
 *   { 'user': 'fred',    'age': 40, 'active': false },
 *   { 'user': 'pebbles', 'age': 1,  'active': true }
 * ];
 *
 * _.result(_.find(users, function(chr) {
 *   return chr.age < 40;
 * }), 'user');
 * // => 'barney'
 *
 * // using the `_.matches` callback shorthand
 * _.result(_.find(users, { 'age': 1, 'active': true }), 'user');
 * // => 'pebbles'
 *
 * // using the `_.matchesProperty` callback shorthand
 * _.result(_.find(users, 'active', false), 'user');
 * // => 'fred'
 *
 * // using the `_.property` callback shorthand
 * _.result(_.find(users, 'active'), 'user');
 * // => 'barney'
 */
var find = createFind(baseEach);

module.exports = find;

},{"106":106,"145":145}],78:[function(_dereq_,module,exports){
var arrayEach = _dereq_(92),
    baseEach = _dereq_(106),
    createForEach = _dereq_(146);

/**
 * Iterates over elements of `collection` invoking `iteratee` for each element.
 * The `iteratee` is bound to `thisArg` and invoked with three arguments:
 * (value, index|key, collection). Iteratee functions may exit iteration early
 * by explicitly returning `false`.
 *
 * **Note:** As with other "Collections" methods, objects with a "length" property
 * are iterated like arrays. To avoid this behavior `_.forIn` or `_.forOwn`
 * may be used for object iteration.
 *
 * @static
 * @memberOf _
 * @alias each
 * @category Collection
 * @param {Array|Object|string} collection The collection to iterate over.
 * @param {Function} [iteratee=_.identity] The function invoked per iteration.
 * @param {*} [thisArg] The `this` binding of `iteratee`.
 * @returns {Array|Object|string} Returns `collection`.
 * @example
 *
 * _([1, 2]).forEach(function(n) {
 *   console.log(n);
 * }).value();
 * // => logs each value from left to right and returns the array
 *
 * _.forEach({ 'a': 1, 'b': 2 }, function(n, key) {
 *   console.log(n, key);
 * });
 * // => logs each value-key pair and returns the object (iteration order is not guaranteed)
 */
var forEach = createForEach(arrayEach, baseEach);

module.exports = forEach;

},{"106":106,"146":146,"92":92}],79:[function(_dereq_,module,exports){
var createAggregator = _dereq_(138);

/** Used for native method references. */
var objectProto = Object.prototype;

/** Used to check objects for own properties. */
var hasOwnProperty = objectProto.hasOwnProperty;

/**
 * Creates an object composed of keys generated from the results of running
 * each element of `collection` through `iteratee`. The corresponding value
 * of each key is an array of the elements responsible for generating the key.
 * The `iteratee` is bound to `thisArg` and invoked with three arguments:
 * (value, index|key, collection).
 *
 * If a property name is provided for `iteratee` the created `_.property`
 * style callback returns the property value of the given element.
 *
 * If a value is also provided for `thisArg` the created `_.matchesProperty`
 * style callback returns `true` for elements that have a matching property
 * value, else `false`.
 *
 * If an object is provided for `iteratee` the created `_.matches` style
 * callback returns `true` for elements that have the properties of the given
 * object, else `false`.
 *
 * @static
 * @memberOf _
 * @category Collection
 * @param {Array|Object|string} collection The collection to iterate over.
 * @param {Function|Object|string} [iteratee=_.identity] The function invoked
 *  per iteration.
 * @param {*} [thisArg] The `this` binding of `iteratee`.
 * @returns {Object} Returns the composed aggregate object.
 * @example
 *
 * _.groupBy([4.2, 6.1, 6.4], function(n) {
 *   return Math.floor(n);
 * });
 * // => { '4': [4.2], '6': [6.1, 6.4] }
 *
 * _.groupBy([4.2, 6.1, 6.4], function(n) {
 *   return this.floor(n);
 * }, Math);
 * // => { '4': [4.2], '6': [6.1, 6.4] }
 *
 * // using the `_.property` callback shorthand
 * _.groupBy(['one', 'two', 'three'], 'length');
 * // => { '3': ['one', 'two'], '5': ['three'] }
 */
var groupBy = createAggregator(function(result, value, key) {
  if (hasOwnProperty.call(result, key)) {
    result[key].push(value);
  } else {
    result[key] = [value];
  }
});

module.exports = groupBy;

},{"138":138}],80:[function(_dereq_,module,exports){
var arrayMap = _dereq_(95),
    baseCallback = _dereq_(101),
    baseMap = _dereq_(121),
    isArray = _dereq_(181);

/**
 * Creates an array of values by running each element in `collection` through
 * `iteratee`. The `iteratee` is bound to `thisArg` and invoked with three
 * arguments: (value, index|key, collection).
 *
 * If a property name is provided for `iteratee` the created `_.property`
 * style callback returns the property value of the given element.
 *
 * If a value is also provided for `thisArg` the created `_.matchesProperty`
 * style callback returns `true` for elements that have a matching property
 * value, else `false`.
 *
 * If an object is provided for `iteratee` the created `_.matches` style
 * callback returns `true` for elements that have the properties of the given
 * object, else `false`.
 *
 * Many lodash methods are guarded to work as iteratees for methods like
 * `_.every`, `_.filter`, `_.map`, `_.mapValues`, `_.reject`, and `_.some`.
 *
 * The guarded methods are:
 * `ary`, `callback`, `chunk`, `clone`, `create`, `curry`, `curryRight`,
 * `drop`, `dropRight`, `every`, `fill`, `flatten`, `invert`, `max`, `min`,
 * `parseInt`, `slice`, `sortBy`, `take`, `takeRight`, `template`, `trim`,
 * `trimLeft`, `trimRight`, `trunc`, `random`, `range`, `sample`, `some`,
 * `sum`, `uniq`, and `words`
 *
 * @static
 * @memberOf _
 * @alias collect
 * @category Collection
 * @param {Array|Object|string} collection The collection to iterate over.
 * @param {Function|Object|string} [iteratee=_.identity] The function invoked
 *  per iteration.
 * @param {*} [thisArg] The `this` binding of `iteratee`.
 * @returns {Array} Returns the new mapped array.
 * @example
 *
 * function timesThree(n) {
 *   return n * 3;
 * }
 *
 * _.map([1, 2], timesThree);
 * // => [3, 6]
 *
 * _.map({ 'a': 1, 'b': 2 }, timesThree);
 * // => [3, 6] (iteration order is not guaranteed)
 *
 * var users = [
 *   { 'user': 'barney' },
 *   { 'user': 'fred' }
 * ];
 *
 * // using the `_.property` callback shorthand
 * _.map(users, 'user');
 * // => ['barney', 'fred']
 */
function map(collection, iteratee, thisArg) {
  var func = isArray(collection) ? arrayMap : baseMap;
  iteratee = baseCallback(iteratee, thisArg, 3);
  return func(collection, iteratee);
}

module.exports = map;

},{"101":101,"121":121,"181":181,"95":95}],81:[function(_dereq_,module,exports){
var arrayReduce = _dereq_(97),
    baseEach = _dereq_(106),
    createReduce = _dereq_(149);

/**
 * Reduces `collection` to a value which is the accumulated result of running
 * each element in `collection` through `iteratee`, where each successive
 * invocation is supplied the return value of the previous. If `accumulator`
 * is not provided the first element of `collection` is used as the initial
 * value. The `iteratee` is bound to `thisArg` and invoked with four arguments:
 * (accumulator, value, index|key, collection).
 *
 * Many lodash methods are guarded to work as iteratees for methods like
 * `_.reduce`, `_.reduceRight`, and `_.transform`.
 *
 * The guarded methods are:
 * `assign`, `defaults`, `defaultsDeep`, `includes`, `merge`, `sortByAll`,
 * and `sortByOrder`
 *
 * @static
 * @memberOf _
 * @alias foldl, inject
 * @category Collection
 * @param {Array|Object|string} collection The collection to iterate over.
 * @param {Function} [iteratee=_.identity] The function invoked per iteration.
 * @param {*} [accumulator] The initial value.
 * @param {*} [thisArg] The `this` binding of `iteratee`.
 * @returns {*} Returns the accumulated value.
 * @example
 *
 * _.reduce([1, 2], function(total, n) {
 *   return total + n;
 * });
 * // => 3
 *
 * _.reduce({ 'a': 1, 'b': 2 }, function(result, n, key) {
 *   result[key] = n * 3;
 *   return result;
 * }, {});
 * // => { 'a': 3, 'b': 6 } (iteration order is not guaranteed)
 */
var reduce = createReduce(arrayReduce, baseEach);

module.exports = reduce;

},{"106":106,"149":149,"97":97}],82:[function(_dereq_,module,exports){
var arraySome = _dereq_(98),
    baseCallback = _dereq_(101),
    baseSome = _dereq_(131),
    isArray = _dereq_(181),
    isIterateeCall = _dereq_(162);

/**
 * Checks if `predicate` returns truthy for **any** element of `collection`.
 * The function returns as soon as it finds a passing value and does not iterate
 * over the entire collection. The predicate is bound to `thisArg` and invoked
 * with three arguments: (value, index|key, collection).
 *
 * If a property name is provided for `predicate` the created `_.property`
 * style callback returns the property value of the given element.
 *
 * If a value is also provided for `thisArg` the created `_.matchesProperty`
 * style callback returns `true` for elements that have a matching property
 * value, else `false`.
 *
 * If an object is provided for `predicate` the created `_.matches` style
 * callback returns `true` for elements that have the properties of the given
 * object, else `false`.
 *
 * @static
 * @memberOf _
 * @alias any
 * @category Collection
 * @param {Array|Object|string} collection The collection to iterate over.
 * @param {Function|Object|string} [predicate=_.identity] The function invoked
 *  per iteration.
 * @param {*} [thisArg] The `this` binding of `predicate`.
 * @returns {boolean} Returns `true` if any element passes the predicate check,
 *  else `false`.
 * @example
 *
 * _.some([null, 0, 'yes', false], Boolean);
 * // => true
 *
 * var users = [
 *   { 'user': 'barney', 'active': true },
 *   { 'user': 'fred',   'active': false }
 * ];
 *
 * // using the `_.matches` callback shorthand
 * _.some(users, { 'user': 'barney', 'active': false });
 * // => false
 *
 * // using the `_.matchesProperty` callback shorthand
 * _.some(users, 'active', false);
 * // => true
 *
 * // using the `_.property` callback shorthand
 * _.some(users, 'active');
 * // => true
 */
function some(collection, predicate, thisArg) {
  var func = isArray(collection) ? arraySome : baseSome;
  if (thisArg && isIterateeCall(collection, predicate, thisArg)) {
    predicate = undefined;
  }
  if (typeof predicate != 'function' || thisArg !== undefined) {
    predicate = baseCallback(predicate, thisArg, 3);
  }
  return func(collection, predicate);
}

module.exports = some;

},{"101":101,"131":131,"162":162,"181":181,"98":98}],83:[function(_dereq_,module,exports){
var getNative = _dereq_(158);

/* Native method references for those with the same name as other `lodash` methods. */
var nativeNow = getNative(Date, 'now');

/**
 * Gets the number of milliseconds that have elapsed since the Unix epoch
 * (1 January 1970 00:00:00 UTC).
 *
 * @static
 * @memberOf _
 * @category Date
 * @example
 *
 * _.defer(function(stamp) {
 *   console.log(_.now() - stamp);
 * }, _.now());
 * // => logs the number of milliseconds it took for the deferred function to be invoked
 */
var now = nativeNow || function() {
  return new Date().getTime();
};

module.exports = now;

},{"158":158}],84:[function(_dereq_,module,exports){
var createWrapper = _dereq_(150),
    replaceHolders = _dereq_(174),
    restParam = _dereq_(87);

/** Used to compose bitmasks for wrapper metadata. */
var BIND_FLAG = 1,
    PARTIAL_FLAG = 32;

/**
 * Creates a function that invokes `func` with the `this` binding of `thisArg`
 * and prepends any additional `_.bind` arguments to those provided to the
 * bound function.
 *
 * The `_.bind.placeholder` value, which defaults to `_` in monolithic builds,
 * may be used as a placeholder for partially applied arguments.
 *
 * **Note:** Unlike native `Function#bind` this method does not set the "length"
 * property of bound functions.
 *
 * @static
 * @memberOf _
 * @category Function
 * @param {Function} func The function to bind.
 * @param {*} thisArg The `this` binding of `func`.
 * @param {...*} [partials] The arguments to be partially applied.
 * @returns {Function} Returns the new bound function.
 * @example
 *
 * var greet = function(greeting, punctuation) {
 *   return greeting + ' ' + this.user + punctuation;
 * };
 *
 * var object = { 'user': 'fred' };
 *
 * var bound = _.bind(greet, object, 'hi');
 * bound('!');
 * // => 'hi fred!'
 *
 * // using placeholders
 * var bound = _.bind(greet, object, _, '!');
 * bound('hi');
 * // => 'hi fred!'
 */
var bind = restParam(function(func, thisArg, partials) {
  var bitmask = BIND_FLAG;
  if (partials.length) {
    var holders = replaceHolders(partials, bind.placeholder);
    bitmask |= PARTIAL_FLAG;
  }
  return createWrapper(func, bitmask, thisArg, partials, holders);
});

// Assign default placeholders.
bind.placeholder = {};

module.exports = bind;

},{"150":150,"174":174,"87":87}],85:[function(_dereq_,module,exports){
var isObject = _dereq_(185),
    now = _dereq_(83);

/** Used as the `TypeError` message for "Functions" methods. */
var FUNC_ERROR_TEXT = 'Expected a function';

/* Native method references for those with the same name as other `lodash` methods. */
var nativeMax = Math.max;

/**
 * Creates a debounced function that delays invoking `func` until after `wait`
 * milliseconds have elapsed since the last time the debounced function was
 * invoked. The debounced function comes with a `cancel` method to cancel
 * delayed invocations. Provide an options object to indicate that `func`
 * should be invoked on the leading and/or trailing edge of the `wait` timeout.
 * Subsequent calls to the debounced function return the result of the last
 * `func` invocation.
 *
 * **Note:** If `leading` and `trailing` options are `true`, `func` is invoked
 * on the trailing edge of the timeout only if the the debounced function is
 * invoked more than once during the `wait` timeout.
 *
 * See [David Corbacho's article](http://drupalmotion.com/article/debounce-and-throttle-visual-explanation)
 * for details over the differences between `_.debounce` and `_.throttle`.
 *
 * @static
 * @memberOf _
 * @category Function
 * @param {Function} func The function to debounce.
 * @param {number} [wait=0] The number of milliseconds to delay.
 * @param {Object} [options] The options object.
 * @param {boolean} [options.leading=false] Specify invoking on the leading
 *  edge of the timeout.
 * @param {number} [options.maxWait] The maximum time `func` is allowed to be
 *  delayed before it's invoked.
 * @param {boolean} [options.trailing=true] Specify invoking on the trailing
 *  edge of the timeout.
 * @returns {Function} Returns the new debounced function.
 * @example
 *
 * // avoid costly calculations while the window size is in flux
 * jQuery(window).on('resize', _.debounce(calculateLayout, 150));
 *
 * // invoke `sendMail` when the click event is fired, debouncing subsequent calls
 * jQuery('#postbox').on('click', _.debounce(sendMail, 300, {
 *   'leading': true,
 *   'trailing': false
 * }));
 *
 * // ensure `batchLog` is invoked once after 1 second of debounced calls
 * var source = new EventSource('/stream');
 * jQuery(source).on('message', _.debounce(batchLog, 250, {
 *   'maxWait': 1000
 * }));
 *
 * // cancel a debounced call
 * var todoChanges = _.debounce(batchLog, 1000);
 * Object.observe(models.todo, todoChanges);
 *
 * Object.observe(models, function(changes) {
 *   if (_.find(changes, { 'user': 'todo', 'type': 'delete'})) {
 *     todoChanges.cancel();
 *   }
 * }, ['delete']);
 *
 * // ...at some point `models.todo` is changed
 * models.todo.completed = true;
 *
 * // ...before 1 second has passed `models.todo` is deleted
 * // which cancels the debounced `todoChanges` call
 * delete models.todo;
 */
function debounce(func, wait, options) {
  var args,
      maxTimeoutId,
      result,
      stamp,
      thisArg,
      timeoutId,
      trailingCall,
      lastCalled = 0,
      maxWait = false,
      trailing = true;

  if (typeof func != 'function') {
    throw new TypeError(FUNC_ERROR_TEXT);
  }
  wait = wait < 0 ? 0 : (+wait || 0);
  if (options === true) {
    var leading = true;
    trailing = false;
  } else if (isObject(options)) {
    leading = !!options.leading;
    maxWait = 'maxWait' in options && nativeMax(+options.maxWait || 0, wait);
    trailing = 'trailing' in options ? !!options.trailing : trailing;
  }

  function cancel() {
    if (timeoutId) {
      clearTimeout(timeoutId);
    }
    if (maxTimeoutId) {
      clearTimeout(maxTimeoutId);
    }
    lastCalled = 0;
    maxTimeoutId = timeoutId = trailingCall = undefined;
  }

  function complete(isCalled, id) {
    if (id) {
      clearTimeout(id);
    }
    maxTimeoutId = timeoutId = trailingCall = undefined;
    if (isCalled) {
      lastCalled = now();
      result = func.apply(thisArg, args);
      if (!timeoutId && !maxTimeoutId) {
        args = thisArg = undefined;
      }
    }
  }

  function delayed() {
    var remaining = wait - (now() - stamp);
    if (remaining <= 0 || remaining > wait) {
      complete(trailingCall, maxTimeoutId);
    } else {
      timeoutId = setTimeout(delayed, remaining);
    }
  }

  function maxDelayed() {
    complete(trailing, timeoutId);
  }

  function debounced() {
    args = arguments;
    stamp = now();
    thisArg = this;
    trailingCall = trailing && (timeoutId || !leading);

    if (maxWait === false) {
      var leadingCall = leading && !timeoutId;
    } else {
      if (!maxTimeoutId && !leading) {
        lastCalled = stamp;
      }
      var remaining = maxWait - (stamp - lastCalled),
          isCalled = remaining <= 0 || remaining > maxWait;

      if (isCalled) {
        if (maxTimeoutId) {
          maxTimeoutId = clearTimeout(maxTimeoutId);
        }
        lastCalled = stamp;
        result = func.apply(thisArg, args);
      }
      else if (!maxTimeoutId) {
        maxTimeoutId = setTimeout(maxDelayed, remaining);
      }
    }
    if (isCalled && timeoutId) {
      timeoutId = clearTimeout(timeoutId);
    }
    else if (!timeoutId && wait !== maxWait) {
      timeoutId = setTimeout(delayed, wait);
    }
    if (leadingCall) {
      isCalled = true;
      result = func.apply(thisArg, args);
    }
    if (isCalled && !timeoutId && !maxTimeoutId) {
      args = thisArg = undefined;
    }
    return result;
  }
  debounced.cancel = cancel;
  return debounced;
}

module.exports = debounce;

},{"185":185,"83":83}],86:[function(_dereq_,module,exports){
var baseDelay = _dereq_(104),
    restParam = _dereq_(87);

/**
 * Defers invoking the `func` until the current call stack has cleared. Any
 * additional arguments are provided to `func` when it's invoked.
 *
 * @static
 * @memberOf _
 * @category Function
 * @param {Function} func The function to defer.
 * @param {...*} [args] The arguments to invoke the function with.
 * @returns {number} Returns the timer id.
 * @example
 *
 * _.defer(function(text) {
 *   console.log(text);
 * }, 'deferred');
 * // logs 'deferred' after one or more milliseconds
 */
var defer = restParam(function(func, args) {
  return baseDelay(func, 1, args);
});

module.exports = defer;

},{"104":104,"87":87}],87:[function(_dereq_,module,exports){
/** Used as the `TypeError` message for "Functions" methods. */
var FUNC_ERROR_TEXT = 'Expected a function';

/* Native method references for those with the same name as other `lodash` methods. */
var nativeMax = Math.max;

/**
 * Creates a function that invokes `func` with the `this` binding of the
 * created function and arguments from `start` and beyond provided as an array.
 *
 * **Note:** This method is based on the [rest parameter](https://developer.mozilla.org/Web/JavaScript/Reference/Functions/rest_parameters).
 *
 * @static
 * @memberOf _
 * @category Function
 * @param {Function} func The function to apply a rest parameter to.
 * @param {number} [start=func.length-1] The start position of the rest parameter.
 * @returns {Function} Returns the new function.
 * @example
 *
 * var say = _.restParam(function(what, names) {
 *   return what + ' ' + _.initial(names).join(', ') +
 *     (_.size(names) > 1 ? ', & ' : '') + _.last(names);
 * });
 *
 * say('hello', 'fred', 'barney', 'pebbles');
 * // => 'hello fred, barney, & pebbles'
 */
function restParam(func, start) {
  if (typeof func != 'function') {
    throw new TypeError(FUNC_ERROR_TEXT);
  }
  start = nativeMax(start === undefined ? (func.length - 1) : (+start || 0), 0);
  return function() {
    var args = arguments,
        index = -1,
        length = nativeMax(args.length - start, 0),
        rest = Array(length);

    while (++index < length) {
      rest[index] = args[start + index];
    }
    switch (start) {
      case 0: return func.call(this, rest);
      case 1: return func.call(this, args[0], rest);
      case 2: return func.call(this, args[0], args[1], rest);
    }
    var otherArgs = Array(start + 1);
    index = -1;
    while (++index < start) {
      otherArgs[index] = args[index];
    }
    otherArgs[start] = rest;
    return func.apply(this, otherArgs);
  };
}

module.exports = restParam;

},{}],88:[function(_dereq_,module,exports){
var baseCreate = _dereq_(103),
    baseLodash = _dereq_(120);

/** Used as references for `-Infinity` and `Infinity`. */
var POSITIVE_INFINITY = Number.POSITIVE_INFINITY;

/**
 * Creates a lazy wrapper object which wraps `value` to enable lazy evaluation.
 *
 * @private
 * @param {*} value The value to wrap.
 */
function LazyWrapper(value) {
  this.__wrapped__ = value;
  this.__actions__ = [];
  this.__dir__ = 1;
  this.__filtered__ = false;
  this.__iteratees__ = [];
  this.__takeCount__ = POSITIVE_INFINITY;
  this.__views__ = [];
}

LazyWrapper.prototype = baseCreate(baseLodash.prototype);
LazyWrapper.prototype.constructor = LazyWrapper;

module.exports = LazyWrapper;

},{"103":103,"120":120}],89:[function(_dereq_,module,exports){
var baseCreate = _dereq_(103),
    baseLodash = _dereq_(120);

/**
 * The base constructor for creating `lodash` wrapper objects.
 *
 * @private
 * @param {*} value The value to wrap.
 * @param {boolean} [chainAll] Enable chaining for all wrapper methods.
 * @param {Array} [actions=[]] Actions to peform to resolve the unwrapped value.
 */
function LodashWrapper(value, chainAll, actions) {
  this.__wrapped__ = value;
  this.__actions__ = actions || [];
  this.__chain__ = !!chainAll;
}

LodashWrapper.prototype = baseCreate(baseLodash.prototype);
LodashWrapper.prototype.constructor = LodashWrapper;

module.exports = LodashWrapper;

},{"103":103,"120":120}],90:[function(_dereq_,module,exports){
(function (global){
var cachePush = _dereq_(135),
    getNative = _dereq_(158);

/** Native method references. */
var Set = getNative(global, 'Set');

/* Native method references for those with the same name as other `lodash` methods. */
var nativeCreate = getNative(Object, 'create');

/**
 *
 * Creates a cache object to store unique values.
 *
 * @private
 * @param {Array} [values] The values to cache.
 */
function SetCache(values) {
  var length = values ? values.length : 0;

  this.data = { 'hash': nativeCreate(null), 'set': new Set };
  while (length--) {
    this.push(values[length]);
  }
}

// Add functions to the `Set` cache.
SetCache.prototype.push = cachePush;

module.exports = SetCache;

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})

},{"135":135,"158":158}],91:[function(_dereq_,module,exports){
/**
 * Copies the values of `source` to `array`.
 *
 * @private
 * @param {Array} source The array to copy values from.
 * @param {Array} [array=[]] The array to copy values to.
 * @returns {Array} Returns `array`.
 */
function arrayCopy(source, array) {
  var index = -1,
      length = source.length;

  array || (array = Array(length));
  while (++index < length) {
    array[index] = source[index];
  }
  return array;
}

module.exports = arrayCopy;

},{}],92:[function(_dereq_,module,exports){
/**
 * A specialized version of `_.forEach` for arrays without support for callback
 * shorthands and `this` binding.
 *
 * @private
 * @param {Array} array The array to iterate over.
 * @param {Function} iteratee The function invoked per iteration.
 * @returns {Array} Returns `array`.
 */
function arrayEach(array, iteratee) {
  var index = -1,
      length = array.length;

  while (++index < length) {
    if (iteratee(array[index], index, array) === false) {
      break;
    }
  }
  return array;
}

module.exports = arrayEach;

},{}],93:[function(_dereq_,module,exports){
/**
 * A specialized version of `_.every` for arrays without support for callback
 * shorthands and `this` binding.
 *
 * @private
 * @param {Array} array The array to iterate over.
 * @param {Function} predicate The function invoked per iteration.
 * @returns {boolean} Returns `true` if all elements pass the predicate check,
 *  else `false`.
 */
function arrayEvery(array, predicate) {
  var index = -1,
      length = array.length;

  while (++index < length) {
    if (!predicate(array[index], index, array)) {
      return false;
    }
  }
  return true;
}

module.exports = arrayEvery;

},{}],94:[function(_dereq_,module,exports){
/**
 * A specialized version of `_.filter` for arrays without support for callback
 * shorthands and `this` binding.
 *
 * @private
 * @param {Array} array The array to iterate over.
 * @param {Function} predicate The function invoked per iteration.
 * @returns {Array} Returns the new filtered array.
 */
function arrayFilter(array, predicate) {
  var index = -1,
      length = array.length,
      resIndex = -1,
      result = [];

  while (++index < length) {
    var value = array[index];
    if (predicate(value, index, array)) {
      result[++resIndex] = value;
    }
  }
  return result;
}

module.exports = arrayFilter;

},{}],95:[function(_dereq_,module,exports){
/**
 * A specialized version of `_.map` for arrays without support for callback
 * shorthands and `this` binding.
 *
 * @private
 * @param {Array} array The array to iterate over.
 * @param {Function} iteratee The function invoked per iteration.
 * @returns {Array} Returns the new mapped array.
 */
function arrayMap(array, iteratee) {
  var index = -1,
      length = array.length,
      result = Array(length);

  while (++index < length) {
    result[index] = iteratee(array[index], index, array);
  }
  return result;
}

module.exports = arrayMap;

},{}],96:[function(_dereq_,module,exports){
/**
 * Appends the elements of `values` to `array`.
 *
 * @private
 * @param {Array} array The array to modify.
 * @param {Array} values The values to append.
 * @returns {Array} Returns `array`.
 */
function arrayPush(array, values) {
  var index = -1,
      length = values.length,
      offset = array.length;

  while (++index < length) {
    array[offset + index] = values[index];
  }
  return array;
}

module.exports = arrayPush;

},{}],97:[function(_dereq_,module,exports){
/**
 * A specialized version of `_.reduce` for arrays without support for callback
 * shorthands and `this` binding.
 *
 * @private
 * @param {Array} array The array to iterate over.
 * @param {Function} iteratee The function invoked per iteration.
 * @param {*} [accumulator] The initial value.
 * @param {boolean} [initFromArray] Specify using the first element of `array`
 *  as the initial value.
 * @returns {*} Returns the accumulated value.
 */
function arrayReduce(array, iteratee, accumulator, initFromArray) {
  var index = -1,
      length = array.length;

  if (initFromArray && length) {
    accumulator = array[++index];
  }
  while (++index < length) {
    accumulator = iteratee(accumulator, array[index], index, array);
  }
  return accumulator;
}

module.exports = arrayReduce;

},{}],98:[function(_dereq_,module,exports){
/**
 * A specialized version of `_.some` for arrays without support for callback
 * shorthands and `this` binding.
 *
 * @private
 * @param {Array} array The array to iterate over.
 * @param {Function} predicate The function invoked per iteration.
 * @returns {boolean} Returns `true` if any element passes the predicate check,
 *  else `false`.
 */
function arraySome(array, predicate) {
  var index = -1,
      length = array.length;

  while (++index < length) {
    if (predicate(array[index], index, array)) {
      return true;
    }
  }
  return false;
}

module.exports = arraySome;

},{}],99:[function(_dereq_,module,exports){
var keys = _dereq_(191);

/**
 * A specialized version of `_.assign` for customizing assigned values without
 * support for argument juggling, multiple sources, and `this` binding `customizer`
 * functions.
 *
 * @private
 * @param {Object} object The destination object.
 * @param {Object} source The source object.
 * @param {Function} customizer The function to customize assigned values.
 * @returns {Object} Returns `object`.
 */
function assignWith(object, source, customizer) {
  var index = -1,
      props = keys(source),
      length = props.length;

  while (++index < length) {
    var key = props[index],
        value = object[key],
        result = customizer(value, source[key], key, object, source);

    if ((result === result ? (result !== value) : (value === value)) ||
        (value === undefined && !(key in object))) {
      object[key] = result;
    }
  }
  return object;
}

module.exports = assignWith;

},{"191":191}],100:[function(_dereq_,module,exports){
var baseCopy = _dereq_(102),
    keys = _dereq_(191);

/**
 * The base implementation of `_.assign` without support for argument juggling,
 * multiple sources, and `customizer` functions.
 *
 * @private
 * @param {Object} object The destination object.
 * @param {Object} source The source object.
 * @returns {Object} Returns `object`.
 */
function baseAssign(object, source) {
  return source == null
    ? object
    : baseCopy(source, keys(source), object);
}

module.exports = baseAssign;

},{"102":102,"191":191}],101:[function(_dereq_,module,exports){
var baseMatches = _dereq_(122),
    baseMatchesProperty = _dereq_(123),
    bindCallback = _dereq_(133),
    identity = _dereq_(197),
    property = _dereq_(199);

/**
 * The base implementation of `_.callback` which supports specifying the
 * number of arguments to provide to `func`.
 *
 * @private
 * @param {*} [func=_.identity] The value to convert to a callback.
 * @param {*} [thisArg] The `this` binding of `func`.
 * @param {number} [argCount] The number of arguments to provide to `func`.
 * @returns {Function} Returns the callback.
 */
function baseCallback(func, thisArg, argCount) {
  var type = typeof func;
  if (type == 'function') {
    return thisArg === undefined
      ? func
      : bindCallback(func, thisArg, argCount);
  }
  if (func == null) {
    return identity;
  }
  if (type == 'object') {
    return baseMatches(func);
  }
  return thisArg === undefined
    ? property(func)
    : baseMatchesProperty(func, thisArg);
}

module.exports = baseCallback;

},{"122":122,"123":123,"133":133,"197":197,"199":199}],102:[function(_dereq_,module,exports){
/**
 * Copies properties of `source` to `object`.
 *
 * @private
 * @param {Object} source The object to copy properties from.
 * @param {Array} props The property names to copy.
 * @param {Object} [object={}] The object to copy properties to.
 * @returns {Object} Returns `object`.
 */
function baseCopy(source, props, object) {
  object || (object = {});

  var index = -1,
      length = props.length;

  while (++index < length) {
    var key = props[index];
    object[key] = source[key];
  }
  return object;
}

module.exports = baseCopy;

},{}],103:[function(_dereq_,module,exports){
var isObject = _dereq_(185);

/**
 * The base implementation of `_.create` without support for assigning
 * properties to the created object.
 *
 * @private
 * @param {Object} prototype The object to inherit from.
 * @returns {Object} Returns the new object.
 */
var baseCreate = (function() {
  function object() {}
  return function(prototype) {
    if (isObject(prototype)) {
      object.prototype = prototype;
      var result = new object;
      object.prototype = undefined;
    }
    return result || {};
  };
}());

module.exports = baseCreate;

},{"185":185}],104:[function(_dereq_,module,exports){
/** Used as the `TypeError` message for "Functions" methods. */
var FUNC_ERROR_TEXT = 'Expected a function';

/**
 * The base implementation of `_.delay` and `_.defer` which accepts an index
 * of where to slice the arguments to provide to `func`.
 *
 * @private
 * @param {Function} func The function to delay.
 * @param {number} wait The number of milliseconds to delay invocation.
 * @param {Object} args The arguments provide to `func`.
 * @returns {number} Returns the timer id.
 */
function baseDelay(func, wait, args) {
  if (typeof func != 'function') {
    throw new TypeError(FUNC_ERROR_TEXT);
  }
  return setTimeout(function() { func.apply(undefined, args); }, wait);
}

module.exports = baseDelay;

},{}],105:[function(_dereq_,module,exports){
var baseIndexOf = _dereq_(116),
    cacheIndexOf = _dereq_(134),
    createCache = _dereq_(143);

/** Used as the size to enable large array optimizations. */
var LARGE_ARRAY_SIZE = 200;

/**
 * The base implementation of `_.difference` which accepts a single array
 * of values to exclude.
 *
 * @private
 * @param {Array} array The array to inspect.
 * @param {Array} values The values to exclude.
 * @returns {Array} Returns the new array of filtered values.
 */
function baseDifference(array, values) {
  var length = array ? array.length : 0,
      result = [];

  if (!length) {
    return result;
  }
  var index = -1,
      indexOf = baseIndexOf,
      isCommon = true,
      cache = (isCommon && values.length >= LARGE_ARRAY_SIZE) ? createCache(values) : null,
      valuesLength = values.length;

  if (cache) {
    indexOf = cacheIndexOf;
    isCommon = false;
    values = cache;
  }
  outer:
  while (++index < length) {
    var value = array[index];

    if (isCommon && value === value) {
      var valuesIndex = valuesLength;
      while (valuesIndex--) {
        if (values[valuesIndex] === value) {
          continue outer;
        }
      }
      result.push(value);
    }
    else if (indexOf(values, value, 0) < 0) {
      result.push(value);
    }
  }
  return result;
}

module.exports = baseDifference;

},{"116":116,"134":134,"143":143}],106:[function(_dereq_,module,exports){
var baseForOwn = _dereq_(114),
    createBaseEach = _dereq_(140);

/**
 * The base implementation of `_.forEach` without support for callback
 * shorthands and `this` binding.
 *
 * @private
 * @param {Array|Object|string} collection The collection to iterate over.
 * @param {Function} iteratee The function invoked per iteration.
 * @returns {Array|Object|string} Returns `collection`.
 */
var baseEach = createBaseEach(baseForOwn);

module.exports = baseEach;

},{"114":114,"140":140}],107:[function(_dereq_,module,exports){
var baseEach = _dereq_(106);

/**
 * The base implementation of `_.every` without support for callback
 * shorthands and `this` binding.
 *
 * @private
 * @param {Array|Object|string} collection The collection to iterate over.
 * @param {Function} predicate The function invoked per iteration.
 * @returns {boolean} Returns `true` if all elements pass the predicate check,
 *  else `false`
 */
function baseEvery(collection, predicate) {
  var result = true;
  baseEach(collection, function(value, index, collection) {
    result = !!predicate(value, index, collection);
    return result;
  });
  return result;
}

module.exports = baseEvery;

},{"106":106}],108:[function(_dereq_,module,exports){
var baseEach = _dereq_(106);

/**
 * The base implementation of `_.filter` without support for callback
 * shorthands and `this` binding.
 *
 * @private
 * @param {Array|Object|string} collection The collection to iterate over.
 * @param {Function} predicate The function invoked per iteration.
 * @returns {Array} Returns the new filtered array.
 */
function baseFilter(collection, predicate) {
  var result = [];
  baseEach(collection, function(value, index, collection) {
    if (predicate(value, index, collection)) {
      result.push(value);
    }
  });
  return result;
}

module.exports = baseFilter;

},{"106":106}],109:[function(_dereq_,module,exports){
/**
 * The base implementation of `_.find`, `_.findLast`, `_.findKey`, and `_.findLastKey`,
 * without support for callback shorthands and `this` binding, which iterates
 * over `collection` using the provided `eachFunc`.
 *
 * @private
 * @param {Array|Object|string} collection The collection to search.
 * @param {Function} predicate The function invoked per iteration.
 * @param {Function} eachFunc The function to iterate over `collection`.
 * @param {boolean} [retKey] Specify returning the key of the found element
 *  instead of the element itself.
 * @returns {*} Returns the found element or its key, else `undefined`.
 */
function baseFind(collection, predicate, eachFunc, retKey) {
  var result;
  eachFunc(collection, function(value, key, collection) {
    if (predicate(value, key, collection)) {
      result = retKey ? key : value;
      return false;
    }
  });
  return result;
}

module.exports = baseFind;

},{}],110:[function(_dereq_,module,exports){
/**
 * The base implementation of `_.findIndex` and `_.findLastIndex` without
 * support for callback shorthands and `this` binding.
 *
 * @private
 * @param {Array} array The array to search.
 * @param {Function} predicate The function invoked per iteration.
 * @param {boolean} [fromRight] Specify iterating from right to left.
 * @returns {number} Returns the index of the matched value, else `-1`.
 */
function baseFindIndex(array, predicate, fromRight) {
  var length = array.length,
      index = fromRight ? length : -1;

  while ((fromRight ? index-- : ++index < length)) {
    if (predicate(array[index], index, array)) {
      return index;
    }
  }
  return -1;
}

module.exports = baseFindIndex;

},{}],111:[function(_dereq_,module,exports){
var arrayPush = _dereq_(96),
    isArguments = _dereq_(180),
    isArray = _dereq_(181),
    isArrayLike = _dereq_(160),
    isObjectLike = _dereq_(166);

/**
 * The base implementation of `_.flatten` with added support for restricting
 * flattening and specifying the start index.
 *
 * @private
 * @param {Array} array The array to flatten.
 * @param {boolean} [isDeep] Specify a deep flatten.
 * @param {boolean} [isStrict] Restrict flattening to arrays-like objects.
 * @param {Array} [result=[]] The initial result value.
 * @returns {Array} Returns the new flattened array.
 */
function baseFlatten(array, isDeep, isStrict, result) {
  result || (result = []);

  var index = -1,
      length = array.length;

  while (++index < length) {
    var value = array[index];
    if (isObjectLike(value) && isArrayLike(value) &&
        (isStrict || isArray(value) || isArguments(value))) {
      if (isDeep) {
        // Recursively flatten arrays (susceptible to call stack limits).
        baseFlatten(value, isDeep, isStrict, result);
      } else {
        arrayPush(result, value);
      }
    } else if (!isStrict) {
      result[result.length] = value;
    }
  }
  return result;
}

module.exports = baseFlatten;

},{"160":160,"166":166,"180":180,"181":181,"96":96}],112:[function(_dereq_,module,exports){
var createBaseFor = _dereq_(141);

/**
 * The base implementation of `baseForIn` and `baseForOwn` which iterates
 * over `object` properties returned by `keysFunc` invoking `iteratee` for
 * each property. Iteratee functions may exit iteration early by explicitly
 * returning `false`.
 *
 * @private
 * @param {Object} object The object to iterate over.
 * @param {Function} iteratee The function invoked per iteration.
 * @param {Function} keysFunc The function to get the keys of `object`.
 * @returns {Object} Returns `object`.
 */
var baseFor = createBaseFor();

module.exports = baseFor;

},{"141":141}],113:[function(_dereq_,module,exports){
var baseFor = _dereq_(112),
    keysIn = _dereq_(192);

/**
 * The base implementation of `_.forIn` without support for callback
 * shorthands and `this` binding.
 *
 * @private
 * @param {Object} object The object to iterate over.
 * @param {Function} iteratee The function invoked per iteration.
 * @returns {Object} Returns `object`.
 */
function baseForIn(object, iteratee) {
  return baseFor(object, iteratee, keysIn);
}

module.exports = baseForIn;

},{"112":112,"192":192}],114:[function(_dereq_,module,exports){
var baseFor = _dereq_(112),
    keys = _dereq_(191);

/**
 * The base implementation of `_.forOwn` without support for callback
 * shorthands and `this` binding.
 *
 * @private
 * @param {Object} object The object to iterate over.
 * @param {Function} iteratee The function invoked per iteration.
 * @returns {Object} Returns `object`.
 */
function baseForOwn(object, iteratee) {
  return baseFor(object, iteratee, keys);
}

module.exports = baseForOwn;

},{"112":112,"191":191}],115:[function(_dereq_,module,exports){
var toObject = _dereq_(177);

/**
 * The base implementation of `get` without support for string paths
 * and default values.
 *
 * @private
 * @param {Object} object The object to query.
 * @param {Array} path The path of the property to get.
 * @param {string} [pathKey] The key representation of path.
 * @returns {*} Returns the resolved value.
 */
function baseGet(object, path, pathKey) {
  if (object == null) {
    return;
  }
  if (pathKey !== undefined && pathKey in toObject(object)) {
    path = [pathKey];
  }
  var index = 0,
      length = path.length;

  while (object != null && index < length) {
    object = object[path[index++]];
  }
  return (index && index == length) ? object : undefined;
}

module.exports = baseGet;

},{"177":177}],116:[function(_dereq_,module,exports){
var indexOfNaN = _dereq_(159);

/**
 * The base implementation of `_.indexOf` without support for binary searches.
 *
 * @private
 * @param {Array} array The array to search.
 * @param {*} value The value to search for.
 * @param {number} fromIndex The index to search from.
 * @returns {number} Returns the index of the matched value, else `-1`.
 */
function baseIndexOf(array, value, fromIndex) {
  if (value !== value) {
    return indexOfNaN(array, fromIndex);
  }
  var index = fromIndex - 1,
      length = array.length;

  while (++index < length) {
    if (array[index] === value) {
      return index;
    }
  }
  return -1;
}

module.exports = baseIndexOf;

},{"159":159}],117:[function(_dereq_,module,exports){
var baseIsEqualDeep = _dereq_(118),
    isObject = _dereq_(185),
    isObjectLike = _dereq_(166);

/**
 * The base implementation of `_.isEqual` without support for `this` binding
 * `customizer` functions.
 *
 * @private
 * @param {*} value The value to compare.
 * @param {*} other The other value to compare.
 * @param {Function} [customizer] The function to customize comparing values.
 * @param {boolean} [isLoose] Specify performing partial comparisons.
 * @param {Array} [stackA] Tracks traversed `value` objects.
 * @param {Array} [stackB] Tracks traversed `other` objects.
 * @returns {boolean} Returns `true` if the values are equivalent, else `false`.
 */
function baseIsEqual(value, other, customizer, isLoose, stackA, stackB) {
  if (value === other) {
    return true;
  }
  if (value == null || other == null || (!isObject(value) && !isObjectLike(other))) {
    return value !== value && other !== other;
  }
  return baseIsEqualDeep(value, other, baseIsEqual, customizer, isLoose, stackA, stackB);
}

module.exports = baseIsEqual;

},{"118":118,"166":166,"185":185}],118:[function(_dereq_,module,exports){
var equalArrays = _dereq_(151),
    equalByTag = _dereq_(152),
    equalObjects = _dereq_(153),
    isArray = _dereq_(181),
    isTypedArray = _dereq_(188);

/** `Object#toString` result references. */
var argsTag = '[object Arguments]',
    arrayTag = '[object Array]',
    objectTag = '[object Object]';

/** Used for native method references. */
var objectProto = Object.prototype;

/** Used to check objects for own properties. */
var hasOwnProperty = objectProto.hasOwnProperty;

/**
 * Used to resolve the [`toStringTag`](http://ecma-international.org/ecma-262/6.0/#sec-object.prototype.tostring)
 * of values.
 */
var objToString = objectProto.toString;

/**
 * A specialized version of `baseIsEqual` for arrays and objects which performs
 * deep comparisons and tracks traversed objects enabling objects with circular
 * references to be compared.
 *
 * @private
 * @param {Object} object The object to compare.
 * @param {Object} other The other object to compare.
 * @param {Function} equalFunc The function to determine equivalents of values.
 * @param {Function} [customizer] The function to customize comparing objects.
 * @param {boolean} [isLoose] Specify performing partial comparisons.
 * @param {Array} [stackA=[]] Tracks traversed `value` objects.
 * @param {Array} [stackB=[]] Tracks traversed `other` objects.
 * @returns {boolean} Returns `true` if the objects are equivalent, else `false`.
 */
function baseIsEqualDeep(object, other, equalFunc, customizer, isLoose, stackA, stackB) {
  var objIsArr = isArray(object),
      othIsArr = isArray(other),
      objTag = arrayTag,
      othTag = arrayTag;

  if (!objIsArr) {
    objTag = objToString.call(object);
    if (objTag == argsTag) {
      objTag = objectTag;
    } else if (objTag != objectTag) {
      objIsArr = isTypedArray(object);
    }
  }
  if (!othIsArr) {
    othTag = objToString.call(other);
    if (othTag == argsTag) {
      othTag = objectTag;
    } else if (othTag != objectTag) {
      othIsArr = isTypedArray(other);
    }
  }
  var objIsObj = objTag == objectTag,
      othIsObj = othTag == objectTag,
      isSameTag = objTag == othTag;

  if (isSameTag && !(objIsArr || objIsObj)) {
    return equalByTag(object, other, objTag);
  }
  if (!isLoose) {
    var objIsWrapped = objIsObj && hasOwnProperty.call(object, '__wrapped__'),
        othIsWrapped = othIsObj && hasOwnProperty.call(other, '__wrapped__');

    if (objIsWrapped || othIsWrapped) {
      return equalFunc(objIsWrapped ? object.value() : object, othIsWrapped ? other.value() : other, customizer, isLoose, stackA, stackB);
    }
  }
  if (!isSameTag) {
    return false;
  }
  // Assume cyclic values are equal.
  // For more information on detecting circular references see https://es5.github.io/#JO.
  stackA || (stackA = []);
  stackB || (stackB = []);

  var length = stackA.length;
  while (length--) {
    if (stackA[length] == object) {
      return stackB[length] == other;
    }
  }
  // Add `object` and `other` to the stack of traversed objects.
  stackA.push(object);
  stackB.push(other);

  var result = (objIsArr ? equalArrays : equalObjects)(object, other, equalFunc, customizer, isLoose, stackA, stackB);

  stackA.pop();
  stackB.pop();

  return result;
}

module.exports = baseIsEqualDeep;

},{"151":151,"152":152,"153":153,"181":181,"188":188}],119:[function(_dereq_,module,exports){
var baseIsEqual = _dereq_(117),
    toObject = _dereq_(177);

/**
 * The base implementation of `_.isMatch` without support for callback
 * shorthands and `this` binding.
 *
 * @private
 * @param {Object} object The object to inspect.
 * @param {Array} matchData The propery names, values, and compare flags to match.
 * @param {Function} [customizer] The function to customize comparing objects.
 * @returns {boolean} Returns `true` if `object` is a match, else `false`.
 */
function baseIsMatch(object, matchData, customizer) {
  var index = matchData.length,
      length = index,
      noCustomizer = !customizer;

  if (object == null) {
    return !length;
  }
  object = toObject(object);
  while (index--) {
    var data = matchData[index];
    if ((noCustomizer && data[2])
          ? data[1] !== object[data[0]]
          : !(data[0] in object)
        ) {
      return false;
    }
  }
  while (++index < length) {
    data = matchData[index];
    var key = data[0],
        objValue = object[key],
        srcValue = data[1];

    if (noCustomizer && data[2]) {
      if (objValue === undefined && !(key in object)) {
        return false;
      }
    } else {
      var result = customizer ? customizer(objValue, srcValue, key) : undefined;
      if (!(result === undefined ? baseIsEqual(srcValue, objValue, customizer, true) : result)) {
        return false;
      }
    }
  }
  return true;
}

module.exports = baseIsMatch;

},{"117":117,"177":177}],120:[function(_dereq_,module,exports){
/**
 * The function whose prototype all chaining wrappers inherit from.
 *
 * @private
 */
function baseLodash() {
  // No operation performed.
}

module.exports = baseLodash;

},{}],121:[function(_dereq_,module,exports){
var baseEach = _dereq_(106),
    isArrayLike = _dereq_(160);

/**
 * The base implementation of `_.map` without support for callback shorthands
 * and `this` binding.
 *
 * @private
 * @param {Array|Object|string} collection The collection to iterate over.
 * @param {Function} iteratee The function invoked per iteration.
 * @returns {Array} Returns the new mapped array.
 */
function baseMap(collection, iteratee) {
  var index = -1,
      result = isArrayLike(collection) ? Array(collection.length) : [];

  baseEach(collection, function(value, key, collection) {
    result[++index] = iteratee(value, key, collection);
  });
  return result;
}

module.exports = baseMap;

},{"106":106,"160":160}],122:[function(_dereq_,module,exports){
var baseIsMatch = _dereq_(119),
    getMatchData = _dereq_(157),
    toObject = _dereq_(177);

/**
 * The base implementation of `_.matches` which does not clone `source`.
 *
 * @private
 * @param {Object} source The object of property values to match.
 * @returns {Function} Returns the new function.
 */
function baseMatches(source) {
  var matchData = getMatchData(source);
  if (matchData.length == 1 && matchData[0][2]) {
    var key = matchData[0][0],
        value = matchData[0][1];

    return function(object) {
      if (object == null) {
        return false;
      }
      return object[key] === value && (value !== undefined || (key in toObject(object)));
    };
  }
  return function(object) {
    return baseIsMatch(object, matchData);
  };
}

module.exports = baseMatches;

},{"119":119,"157":157,"177":177}],123:[function(_dereq_,module,exports){
var baseGet = _dereq_(115),
    baseIsEqual = _dereq_(117),
    baseSlice = _dereq_(130),
    isArray = _dereq_(181),
    isKey = _dereq_(163),
    isStrictComparable = _dereq_(167),
    last = _dereq_(73),
    toObject = _dereq_(177),
    toPath = _dereq_(178);

/**
 * The base implementation of `_.matchesProperty` which does not clone `srcValue`.
 *
 * @private
 * @param {string} path The path of the property to get.
 * @param {*} srcValue The value to compare.
 * @returns {Function} Returns the new function.
 */
function baseMatchesProperty(path, srcValue) {
  var isArr = isArray(path),
      isCommon = isKey(path) && isStrictComparable(srcValue),
      pathKey = (path + '');

  path = toPath(path);
  return function(object) {
    if (object == null) {
      return false;
    }
    var key = pathKey;
    object = toObject(object);
    if ((isArr || !isCommon) && !(key in object)) {
      object = path.length == 1 ? object : baseGet(object, baseSlice(path, 0, -1));
      if (object == null) {
        return false;
      }
      key = last(path);
      object = toObject(object);
    }
    return object[key] === srcValue
      ? (srcValue !== undefined || (key in object))
      : baseIsEqual(srcValue, object[key], undefined, true);
  };
}

module.exports = baseMatchesProperty;

},{"115":115,"117":117,"130":130,"163":163,"167":167,"177":177,"178":178,"181":181,"73":73}],124:[function(_dereq_,module,exports){
var arrayEach = _dereq_(92),
    baseMergeDeep = _dereq_(125),
    isArray = _dereq_(181),
    isArrayLike = _dereq_(160),
    isObject = _dereq_(185),
    isObjectLike = _dereq_(166),
    isTypedArray = _dereq_(188),
    keys = _dereq_(191);

/**
 * The base implementation of `_.merge` without support for argument juggling,
 * multiple sources, and `this` binding `customizer` functions.
 *
 * @private
 * @param {Object} object The destination object.
 * @param {Object} source The source object.
 * @param {Function} [customizer] The function to customize merged values.
 * @param {Array} [stackA=[]] Tracks traversed source objects.
 * @param {Array} [stackB=[]] Associates values with source counterparts.
 * @returns {Object} Returns `object`.
 */
function baseMerge(object, source, customizer, stackA, stackB) {
  if (!isObject(object)) {
    return object;
  }
  var isSrcArr = isArrayLike(source) && (isArray(source) || isTypedArray(source)),
      props = isSrcArr ? undefined : keys(source);

  arrayEach(props || source, function(srcValue, key) {
    if (props) {
      key = srcValue;
      srcValue = source[key];
    }
    if (isObjectLike(srcValue)) {
      stackA || (stackA = []);
      stackB || (stackB = []);
      baseMergeDeep(object, source, key, baseMerge, customizer, stackA, stackB);
    }
    else {
      var value = object[key],
          result = customizer ? customizer(value, srcValue, key, object, source) : undefined,
          isCommon = result === undefined;

      if (isCommon) {
        result = srcValue;
      }
      if ((result !== undefined || (isSrcArr && !(key in object))) &&
          (isCommon || (result === result ? (result !== value) : (value === value)))) {
        object[key] = result;
      }
    }
  });
  return object;
}

module.exports = baseMerge;

},{"125":125,"160":160,"166":166,"181":181,"185":185,"188":188,"191":191,"92":92}],125:[function(_dereq_,module,exports){
var arrayCopy = _dereq_(91),
    isArguments = _dereq_(180),
    isArray = _dereq_(181),
    isArrayLike = _dereq_(160),
    isPlainObject = _dereq_(186),
    isTypedArray = _dereq_(188),
    toPlainObject = _dereq_(189);

/**
 * A specialized version of `baseMerge` for arrays and objects which performs
 * deep merges and tracks traversed objects enabling objects with circular
 * references to be merged.
 *
 * @private
 * @param {Object} object The destination object.
 * @param {Object} source The source object.
 * @param {string} key The key of the value to merge.
 * @param {Function} mergeFunc The function to merge values.
 * @param {Function} [customizer] The function to customize merged values.
 * @param {Array} [stackA=[]] Tracks traversed source objects.
 * @param {Array} [stackB=[]] Associates values with source counterparts.
 * @returns {boolean} Returns `true` if the objects are equivalent, else `false`.
 */
function baseMergeDeep(object, source, key, mergeFunc, customizer, stackA, stackB) {
  var length = stackA.length,
      srcValue = source[key];

  while (length--) {
    if (stackA[length] == srcValue) {
      object[key] = stackB[length];
      return;
    }
  }
  var value = object[key],
      result = customizer ? customizer(value, srcValue, key, object, source) : undefined,
      isCommon = result === undefined;

  if (isCommon) {
    result = srcValue;
    if (isArrayLike(srcValue) && (isArray(srcValue) || isTypedArray(srcValue))) {
      result = isArray(value)
        ? value
        : (isArrayLike(value) ? arrayCopy(value) : []);
    }
    else if (isPlainObject(srcValue) || isArguments(srcValue)) {
      result = isArguments(value)
        ? toPlainObject(value)
        : (isPlainObject(value) ? value : {});
    }
    else {
      isCommon = false;
    }
  }
  // Add the source value to the stack of traversed objects and associate
  // it with its merged value.
  stackA.push(srcValue);
  stackB.push(result);

  if (isCommon) {
    // Recursively merge objects and arrays (susceptible to call stack limits).
    object[key] = mergeFunc(result, srcValue, customizer, stackA, stackB);
  } else if (result === result ? (result !== value) : (value === value)) {
    object[key] = result;
  }
}

module.exports = baseMergeDeep;

},{"160":160,"180":180,"181":181,"186":186,"188":188,"189":189,"91":91}],126:[function(_dereq_,module,exports){
/**
 * The base implementation of `_.property` without support for deep paths.
 *
 * @private
 * @param {string} key The key of the property to get.
 * @returns {Function} Returns the new function.
 */
function baseProperty(key) {
  return function(object) {
    return object == null ? undefined : object[key];
  };
}

module.exports = baseProperty;

},{}],127:[function(_dereq_,module,exports){
var baseGet = _dereq_(115),
    toPath = _dereq_(178);

/**
 * A specialized version of `baseProperty` which supports deep paths.
 *
 * @private
 * @param {Array|string} path The path of the property to get.
 * @returns {Function} Returns the new function.
 */
function basePropertyDeep(path) {
  var pathKey = (path + '');
  path = toPath(path);
  return function(object) {
    return baseGet(object, path, pathKey);
  };
}

module.exports = basePropertyDeep;

},{"115":115,"178":178}],128:[function(_dereq_,module,exports){
/**
 * The base implementation of `_.reduce` and `_.reduceRight` without support
 * for callback shorthands and `this` binding, which iterates over `collection`
 * using the provided `eachFunc`.
 *
 * @private
 * @param {Array|Object|string} collection The collection to iterate over.
 * @param {Function} iteratee The function invoked per iteration.
 * @param {*} accumulator The initial value.
 * @param {boolean} initFromCollection Specify using the first or last element
 *  of `collection` as the initial value.
 * @param {Function} eachFunc The function to iterate over `collection`.
 * @returns {*} Returns the accumulated value.
 */
function baseReduce(collection, iteratee, accumulator, initFromCollection, eachFunc) {
  eachFunc(collection, function(value, index, collection) {
    accumulator = initFromCollection
      ? (initFromCollection = false, value)
      : iteratee(accumulator, value, index, collection);
  });
  return accumulator;
}

module.exports = baseReduce;

},{}],129:[function(_dereq_,module,exports){
var identity = _dereq_(197),
    metaMap = _dereq_(169);

/**
 * The base implementation of `setData` without support for hot loop detection.
 *
 * @private
 * @param {Function} func The function to associate metadata with.
 * @param {*} data The metadata.
 * @returns {Function} Returns `func`.
 */
var baseSetData = !metaMap ? identity : function(func, data) {
  metaMap.set(func, data);
  return func;
};

module.exports = baseSetData;

},{"169":169,"197":197}],130:[function(_dereq_,module,exports){
/**
 * The base implementation of `_.slice` without an iteratee call guard.
 *
 * @private
 * @param {Array} array The array to slice.
 * @param {number} [start=0] The start position.
 * @param {number} [end=array.length] The end position.
 * @returns {Array} Returns the slice of `array`.
 */
function baseSlice(array, start, end) {
  var index = -1,
      length = array.length;

  start = start == null ? 0 : (+start || 0);
  if (start < 0) {
    start = -start > length ? 0 : (length + start);
  }
  end = (end === undefined || end > length) ? length : (+end || 0);
  if (end < 0) {
    end += length;
  }
  length = start > end ? 0 : ((end - start) >>> 0);
  start >>>= 0;

  var result = Array(length);
  while (++index < length) {
    result[index] = array[index + start];
  }
  return result;
}

module.exports = baseSlice;

},{}],131:[function(_dereq_,module,exports){
var baseEach = _dereq_(106);

/**
 * The base implementation of `_.some` without support for callback shorthands
 * and `this` binding.
 *
 * @private
 * @param {Array|Object|string} collection The collection to iterate over.
 * @param {Function} predicate The function invoked per iteration.
 * @returns {boolean} Returns `true` if any element passes the predicate check,
 *  else `false`.
 */
function baseSome(collection, predicate) {
  var result;

  baseEach(collection, function(value, index, collection) {
    result = predicate(value, index, collection);
    return !result;
  });
  return !!result;
}

module.exports = baseSome;

},{"106":106}],132:[function(_dereq_,module,exports){
/**
 * Converts `value` to a string if it's not one. An empty string is returned
 * for `null` or `undefined` values.
 *
 * @private
 * @param {*} value The value to process.
 * @returns {string} Returns the string.
 */
function baseToString(value) {
  return value == null ? '' : (value + '');
}

module.exports = baseToString;

},{}],133:[function(_dereq_,module,exports){
var identity = _dereq_(197);

/**
 * A specialized version of `baseCallback` which only supports `this` binding
 * and specifying the number of arguments to provide to `func`.
 *
 * @private
 * @param {Function} func The function to bind.
 * @param {*} thisArg The `this` binding of `func`.
 * @param {number} [argCount] The number of arguments to provide to `func`.
 * @returns {Function} Returns the callback.
 */
function bindCallback(func, thisArg, argCount) {
  if (typeof func != 'function') {
    return identity;
  }
  if (thisArg === undefined) {
    return func;
  }
  switch (argCount) {
    case 1: return function(value) {
      return func.call(thisArg, value);
    };
    case 3: return function(value, index, collection) {
      return func.call(thisArg, value, index, collection);
    };
    case 4: return function(accumulator, value, index, collection) {
      return func.call(thisArg, accumulator, value, index, collection);
    };
    case 5: return function(value, other, key, object, source) {
      return func.call(thisArg, value, other, key, object, source);
    };
  }
  return function() {
    return func.apply(thisArg, arguments);
  };
}

module.exports = bindCallback;

},{"197":197}],134:[function(_dereq_,module,exports){
var isObject = _dereq_(185);

/**
 * Checks if `value` is in `cache` mimicking the return signature of
 * `_.indexOf` by returning `0` if the value is found, else `-1`.
 *
 * @private
 * @param {Object} cache The cache to search.
 * @param {*} value The value to search for.
 * @returns {number} Returns `0` if `value` is found, else `-1`.
 */
function cacheIndexOf(cache, value) {
  var data = cache.data,
      result = (typeof value == 'string' || isObject(value)) ? data.set.has(value) : data.hash[value];

  return result ? 0 : -1;
}

module.exports = cacheIndexOf;

},{"185":185}],135:[function(_dereq_,module,exports){
var isObject = _dereq_(185);

/**
 * Adds `value` to the cache.
 *
 * @private
 * @name push
 * @memberOf SetCache
 * @param {*} value The value to cache.
 */
function cachePush(value) {
  var data = this.data;
  if (typeof value == 'string' || isObject(value)) {
    data.set.add(value);
  } else {
    data.hash[value] = true;
  }
}

module.exports = cachePush;

},{"185":185}],136:[function(_dereq_,module,exports){
/* Native method references for those with the same name as other `lodash` methods. */
var nativeMax = Math.max;

/**
 * Creates an array that is the composition of partially applied arguments,
 * placeholders, and provided arguments into a single array of arguments.
 *
 * @private
 * @param {Array|Object} args The provided arguments.
 * @param {Array} partials The arguments to prepend to those provided.
 * @param {Array} holders The `partials` placeholder indexes.
 * @returns {Array} Returns the new array of composed arguments.
 */
function composeArgs(args, partials, holders) {
  var holdersLength = holders.length,
      argsIndex = -1,
      argsLength = nativeMax(args.length - holdersLength, 0),
      leftIndex = -1,
      leftLength = partials.length,
      result = Array(leftLength + argsLength);

  while (++leftIndex < leftLength) {
    result[leftIndex] = partials[leftIndex];
  }
  while (++argsIndex < holdersLength) {
    result[holders[argsIndex]] = args[argsIndex];
  }
  while (argsLength--) {
    result[leftIndex++] = args[argsIndex++];
  }
  return result;
}

module.exports = composeArgs;

},{}],137:[function(_dereq_,module,exports){
/* Native method references for those with the same name as other `lodash` methods. */
var nativeMax = Math.max;

/**
 * This function is like `composeArgs` except that the arguments composition
 * is tailored for `_.partialRight`.
 *
 * @private
 * @param {Array|Object} args The provided arguments.
 * @param {Array} partials The arguments to append to those provided.
 * @param {Array} holders The `partials` placeholder indexes.
 * @returns {Array} Returns the new array of composed arguments.
 */
function composeArgsRight(args, partials, holders) {
  var holdersIndex = -1,
      holdersLength = holders.length,
      argsIndex = -1,
      argsLength = nativeMax(args.length - holdersLength, 0),
      rightIndex = -1,
      rightLength = partials.length,
      result = Array(argsLength + rightLength);

  while (++argsIndex < argsLength) {
    result[argsIndex] = args[argsIndex];
  }
  var offset = argsIndex;
  while (++rightIndex < rightLength) {
    result[offset + rightIndex] = partials[rightIndex];
  }
  while (++holdersIndex < holdersLength) {
    result[offset + holders[holdersIndex]] = args[argsIndex++];
  }
  return result;
}

module.exports = composeArgsRight;

},{}],138:[function(_dereq_,module,exports){
var baseCallback = _dereq_(101),
    baseEach = _dereq_(106),
    isArray = _dereq_(181);

/**
 * Creates a `_.countBy`, `_.groupBy`, `_.indexBy`, or `_.partition` function.
 *
 * @private
 * @param {Function} setter The function to set keys and values of the accumulator object.
 * @param {Function} [initializer] The function to initialize the accumulator object.
 * @returns {Function} Returns the new aggregator function.
 */
function createAggregator(setter, initializer) {
  return function(collection, iteratee, thisArg) {
    var result = initializer ? initializer() : {};
    iteratee = baseCallback(iteratee, thisArg, 3);

    if (isArray(collection)) {
      var index = -1,
          length = collection.length;

      while (++index < length) {
        var value = collection[index];
        setter(result, value, iteratee(value, index, collection), collection);
      }
    } else {
      baseEach(collection, function(value, key, collection) {
        setter(result, value, iteratee(value, key, collection), collection);
      });
    }
    return result;
  };
}

module.exports = createAggregator;

},{"101":101,"106":106,"181":181}],139:[function(_dereq_,module,exports){
var bindCallback = _dereq_(133),
    isIterateeCall = _dereq_(162),
    restParam = _dereq_(87);

/**
 * Creates a `_.assign`, `_.defaults`, or `_.merge` function.
 *
 * @private
 * @param {Function} assigner The function to assign values.
 * @returns {Function} Returns the new assigner function.
 */
function createAssigner(assigner) {
  return restParam(function(object, sources) {
    var index = -1,
        length = object == null ? 0 : sources.length,
        customizer = length > 2 ? sources[length - 2] : undefined,
        guard = length > 2 ? sources[2] : undefined,
        thisArg = length > 1 ? sources[length - 1] : undefined;

    if (typeof customizer == 'function') {
      customizer = bindCallback(customizer, thisArg, 5);
      length -= 2;
    } else {
      customizer = typeof thisArg == 'function' ? thisArg : undefined;
      length -= (customizer ? 1 : 0);
    }
    if (guard && isIterateeCall(sources[0], sources[1], guard)) {
      customizer = length < 3 ? undefined : customizer;
      length = 1;
    }
    while (++index < length) {
      var source = sources[index];
      if (source) {
        assigner(object, source, customizer);
      }
    }
    return object;
  });
}

module.exports = createAssigner;

},{"133":133,"162":162,"87":87}],140:[function(_dereq_,module,exports){
var getLength = _dereq_(156),
    isLength = _dereq_(165),
    toObject = _dereq_(177);

/**
 * Creates a `baseEach` or `baseEachRight` function.
 *
 * @private
 * @param {Function} eachFunc The function to iterate over a collection.
 * @param {boolean} [fromRight] Specify iterating from right to left.
 * @returns {Function} Returns the new base function.
 */
function createBaseEach(eachFunc, fromRight) {
  return function(collection, iteratee) {
    var length = collection ? getLength(collection) : 0;
    if (!isLength(length)) {
      return eachFunc(collection, iteratee);
    }
    var index = fromRight ? length : -1,
        iterable = toObject(collection);

    while ((fromRight ? index-- : ++index < length)) {
      if (iteratee(iterable[index], index, iterable) === false) {
        break;
      }
    }
    return collection;
  };
}

module.exports = createBaseEach;

},{"156":156,"165":165,"177":177}],141:[function(_dereq_,module,exports){
var toObject = _dereq_(177);

/**
 * Creates a base function for `_.forIn` or `_.forInRight`.
 *
 * @private
 * @param {boolean} [fromRight] Specify iterating from right to left.
 * @returns {Function} Returns the new base function.
 */
function createBaseFor(fromRight) {
  return function(object, iteratee, keysFunc) {
    var iterable = toObject(object),
        props = keysFunc(object),
        length = props.length,
        index = fromRight ? length : -1;

    while ((fromRight ? index-- : ++index < length)) {
      var key = props[index];
      if (iteratee(iterable[key], key, iterable) === false) {
        break;
      }
    }
    return object;
  };
}

module.exports = createBaseFor;

},{"177":177}],142:[function(_dereq_,module,exports){
(function (global){
var createCtorWrapper = _dereq_(144);

/**
 * Creates a function that wraps `func` and invokes it with the `this`
 * binding of `thisArg`.
 *
 * @private
 * @param {Function} func The function to bind.
 * @param {*} [thisArg] The `this` binding of `func`.
 * @returns {Function} Returns the new bound function.
 */
function createBindWrapper(func, thisArg) {
  var Ctor = createCtorWrapper(func);

  function wrapper() {
    var fn = (this && this !== global && this instanceof wrapper) ? Ctor : func;
    return fn.apply(thisArg, arguments);
  }
  return wrapper;
}

module.exports = createBindWrapper;

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})

},{"144":144}],143:[function(_dereq_,module,exports){
(function (global){
var SetCache = _dereq_(90),
    getNative = _dereq_(158);

/** Native method references. */
var Set = getNative(global, 'Set');

/* Native method references for those with the same name as other `lodash` methods. */
var nativeCreate = getNative(Object, 'create');

/**
 * Creates a `Set` cache object to optimize linear searches of large arrays.
 *
 * @private
 * @param {Array} [values] The values to cache.
 * @returns {null|Object} Returns the new cache object if `Set` is supported, else `null`.
 */
function createCache(values) {
  return (nativeCreate && Set) ? new SetCache(values) : null;
}

module.exports = createCache;

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})

},{"158":158,"90":90}],144:[function(_dereq_,module,exports){
var baseCreate = _dereq_(103),
    isObject = _dereq_(185);

/**
 * Creates a function that produces an instance of `Ctor` regardless of
 * whether it was invoked as part of a `new` expression or by `call` or `apply`.
 *
 * @private
 * @param {Function} Ctor The constructor to wrap.
 * @returns {Function} Returns the new wrapped function.
 */
function createCtorWrapper(Ctor) {
  return function() {
    // Use a `switch` statement to work with class constructors.
    // See http://ecma-international.org/ecma-262/6.0/#sec-ecmascript-function-objects-call-thisargument-argumentslist
    // for more details.
    var args = arguments;
    switch (args.length) {
      case 0: return new Ctor;
      case 1: return new Ctor(args[0]);
      case 2: return new Ctor(args[0], args[1]);
      case 3: return new Ctor(args[0], args[1], args[2]);
      case 4: return new Ctor(args[0], args[1], args[2], args[3]);
      case 5: return new Ctor(args[0], args[1], args[2], args[3], args[4]);
      case 6: return new Ctor(args[0], args[1], args[2], args[3], args[4], args[5]);
      case 7: return new Ctor(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
    }
    var thisBinding = baseCreate(Ctor.prototype),
        result = Ctor.apply(thisBinding, args);

    // Mimic the constructor's `return` behavior.
    // See https://es5.github.io/#x13.2.2 for more details.
    return isObject(result) ? result : thisBinding;
  };
}

module.exports = createCtorWrapper;

},{"103":103,"185":185}],145:[function(_dereq_,module,exports){
var baseCallback = _dereq_(101),
    baseFind = _dereq_(109),
    baseFindIndex = _dereq_(110),
    isArray = _dereq_(181);

/**
 * Creates a `_.find` or `_.findLast` function.
 *
 * @private
 * @param {Function} eachFunc The function to iterate over a collection.
 * @param {boolean} [fromRight] Specify iterating from right to left.
 * @returns {Function} Returns the new find function.
 */
function createFind(eachFunc, fromRight) {
  return function(collection, predicate, thisArg) {
    predicate = baseCallback(predicate, thisArg, 3);
    if (isArray(collection)) {
      var index = baseFindIndex(collection, predicate, fromRight);
      return index > -1 ? collection[index] : undefined;
    }
    return baseFind(collection, predicate, eachFunc);
  };
}

module.exports = createFind;

},{"101":101,"109":109,"110":110,"181":181}],146:[function(_dereq_,module,exports){
var bindCallback = _dereq_(133),
    isArray = _dereq_(181);

/**
 * Creates a function for `_.forEach` or `_.forEachRight`.
 *
 * @private
 * @param {Function} arrayFunc The function to iterate over an array.
 * @param {Function} eachFunc The function to iterate over a collection.
 * @returns {Function} Returns the new each function.
 */
function createForEach(arrayFunc, eachFunc) {
  return function(collection, iteratee, thisArg) {
    return (typeof iteratee == 'function' && thisArg === undefined && isArray(collection))
      ? arrayFunc(collection, iteratee)
      : eachFunc(collection, bindCallback(iteratee, thisArg, 3));
  };
}

module.exports = createForEach;

},{"133":133,"181":181}],147:[function(_dereq_,module,exports){
(function (global){
var arrayCopy = _dereq_(91),
    composeArgs = _dereq_(136),
    composeArgsRight = _dereq_(137),
    createCtorWrapper = _dereq_(144),
    isLaziable = _dereq_(164),
    reorder = _dereq_(173),
    replaceHolders = _dereq_(174),
    setData = _dereq_(175);

/** Used to compose bitmasks for wrapper metadata. */
var BIND_FLAG = 1,
    BIND_KEY_FLAG = 2,
    CURRY_BOUND_FLAG = 4,
    CURRY_FLAG = 8,
    CURRY_RIGHT_FLAG = 16,
    PARTIAL_FLAG = 32,
    PARTIAL_RIGHT_FLAG = 64,
    ARY_FLAG = 128;

/* Native method references for those with the same name as other `lodash` methods. */
var nativeMax = Math.max;

/**
 * Creates a function that wraps `func` and invokes it with optional `this`
 * binding of, partial application, and currying.
 *
 * @private
 * @param {Function|string} func The function or method name to reference.
 * @param {number} bitmask The bitmask of flags. See `createWrapper` for more details.
 * @param {*} [thisArg] The `this` binding of `func`.
 * @param {Array} [partials] The arguments to prepend to those provided to the new function.
 * @param {Array} [holders] The `partials` placeholder indexes.
 * @param {Array} [partialsRight] The arguments to append to those provided to the new function.
 * @param {Array} [holdersRight] The `partialsRight` placeholder indexes.
 * @param {Array} [argPos] The argument positions of the new function.
 * @param {number} [ary] The arity cap of `func`.
 * @param {number} [arity] The arity of `func`.
 * @returns {Function} Returns the new wrapped function.
 */
function createHybridWrapper(func, bitmask, thisArg, partials, holders, partialsRight, holdersRight, argPos, ary, arity) {
  var isAry = bitmask & ARY_FLAG,
      isBind = bitmask & BIND_FLAG,
      isBindKey = bitmask & BIND_KEY_FLAG,
      isCurry = bitmask & CURRY_FLAG,
      isCurryBound = bitmask & CURRY_BOUND_FLAG,
      isCurryRight = bitmask & CURRY_RIGHT_FLAG,
      Ctor = isBindKey ? undefined : createCtorWrapper(func);

  function wrapper() {
    // Avoid `arguments` object use disqualifying optimizations by
    // converting it to an array before providing it to other functions.
    var length = arguments.length,
        index = length,
        args = Array(length);

    while (index--) {
      args[index] = arguments[index];
    }
    if (partials) {
      args = composeArgs(args, partials, holders);
    }
    if (partialsRight) {
      args = composeArgsRight(args, partialsRight, holdersRight);
    }
    if (isCurry || isCurryRight) {
      var placeholder = wrapper.placeholder,
          argsHolders = replaceHolders(args, placeholder);

      length -= argsHolders.length;
      if (length < arity) {
        var newArgPos = argPos ? arrayCopy(argPos) : undefined,
            newArity = nativeMax(arity - length, 0),
            newsHolders = isCurry ? argsHolders : undefined,
            newHoldersRight = isCurry ? undefined : argsHolders,
            newPartials = isCurry ? args : undefined,
            newPartialsRight = isCurry ? undefined : args;

        bitmask |= (isCurry ? PARTIAL_FLAG : PARTIAL_RIGHT_FLAG);
        bitmask &= ~(isCurry ? PARTIAL_RIGHT_FLAG : PARTIAL_FLAG);

        if (!isCurryBound) {
          bitmask &= ~(BIND_FLAG | BIND_KEY_FLAG);
        }
        var newData = [func, bitmask, thisArg, newPartials, newsHolders, newPartialsRight, newHoldersRight, newArgPos, ary, newArity],
            result = createHybridWrapper.apply(undefined, newData);

        if (isLaziable(func)) {
          setData(result, newData);
        }
        result.placeholder = placeholder;
        return result;
      }
    }
    var thisBinding = isBind ? thisArg : this,
        fn = isBindKey ? thisBinding[func] : func;

    if (argPos) {
      args = reorder(args, argPos);
    }
    if (isAry && ary < args.length) {
      args.length = ary;
    }
    if (this && this !== global && this instanceof wrapper) {
      fn = Ctor || createCtorWrapper(func);
    }
    return fn.apply(thisBinding, args);
  }
  return wrapper;
}

module.exports = createHybridWrapper;

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})

},{"136":136,"137":137,"144":144,"164":164,"173":173,"174":174,"175":175,"91":91}],148:[function(_dereq_,module,exports){
(function (global){
var createCtorWrapper = _dereq_(144);

/** Used to compose bitmasks for wrapper metadata. */
var BIND_FLAG = 1;

/**
 * Creates a function that wraps `func` and invokes it with the optional `this`
 * binding of `thisArg` and the `partials` prepended to those provided to
 * the wrapper.
 *
 * @private
 * @param {Function} func The function to partially apply arguments to.
 * @param {number} bitmask The bitmask of flags. See `createWrapper` for more details.
 * @param {*} thisArg The `this` binding of `func`.
 * @param {Array} partials The arguments to prepend to those provided to the new function.
 * @returns {Function} Returns the new bound function.
 */
function createPartialWrapper(func, bitmask, thisArg, partials) {
  var isBind = bitmask & BIND_FLAG,
      Ctor = createCtorWrapper(func);

  function wrapper() {
    // Avoid `arguments` object use disqualifying optimizations by
    // converting it to an array before providing it `func`.
    var argsIndex = -1,
        argsLength = arguments.length,
        leftIndex = -1,
        leftLength = partials.length,
        args = Array(leftLength + argsLength);

    while (++leftIndex < leftLength) {
      args[leftIndex] = partials[leftIndex];
    }
    while (argsLength--) {
      args[leftIndex++] = arguments[++argsIndex];
    }
    var fn = (this && this !== global && this instanceof wrapper) ? Ctor : func;
    return fn.apply(isBind ? thisArg : this, args);
  }
  return wrapper;
}

module.exports = createPartialWrapper;

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})

},{"144":144}],149:[function(_dereq_,module,exports){
var baseCallback = _dereq_(101),
    baseReduce = _dereq_(128),
    isArray = _dereq_(181);

/**
 * Creates a function for `_.reduce` or `_.reduceRight`.
 *
 * @private
 * @param {Function} arrayFunc The function to iterate over an array.
 * @param {Function} eachFunc The function to iterate over a collection.
 * @returns {Function} Returns the new each function.
 */
function createReduce(arrayFunc, eachFunc) {
  return function(collection, iteratee, accumulator, thisArg) {
    var initFromArray = arguments.length < 3;
    return (typeof iteratee == 'function' && thisArg === undefined && isArray(collection))
      ? arrayFunc(collection, iteratee, accumulator, initFromArray)
      : baseReduce(collection, baseCallback(iteratee, thisArg, 4), accumulator, initFromArray, eachFunc);
  };
}

module.exports = createReduce;

},{"101":101,"128":128,"181":181}],150:[function(_dereq_,module,exports){
var baseSetData = _dereq_(129),
    createBindWrapper = _dereq_(142),
    createHybridWrapper = _dereq_(147),
    createPartialWrapper = _dereq_(148),
    getData = _dereq_(154),
    mergeData = _dereq_(168),
    setData = _dereq_(175);

/** Used to compose bitmasks for wrapper metadata. */
var BIND_FLAG = 1,
    BIND_KEY_FLAG = 2,
    PARTIAL_FLAG = 32,
    PARTIAL_RIGHT_FLAG = 64;

/** Used as the `TypeError` message for "Functions" methods. */
var FUNC_ERROR_TEXT = 'Expected a function';

/* Native method references for those with the same name as other `lodash` methods. */
var nativeMax = Math.max;

/**
 * Creates a function that either curries or invokes `func` with optional
 * `this` binding and partially applied arguments.
 *
 * @private
 * @param {Function|string} func The function or method name to reference.
 * @param {number} bitmask The bitmask of flags.
 *  The bitmask may be composed of the following flags:
 *     1 - `_.bind`
 *     2 - `_.bindKey`
 *     4 - `_.curry` or `_.curryRight` of a bound function
 *     8 - `_.curry`
 *    16 - `_.curryRight`
 *    32 - `_.partial`
 *    64 - `_.partialRight`
 *   128 - `_.rearg`
 *   256 - `_.ary`
 * @param {*} [thisArg] The `this` binding of `func`.
 * @param {Array} [partials] The arguments to be partially applied.
 * @param {Array} [holders] The `partials` placeholder indexes.
 * @param {Array} [argPos] The argument positions of the new function.
 * @param {number} [ary] The arity cap of `func`.
 * @param {number} [arity] The arity of `func`.
 * @returns {Function} Returns the new wrapped function.
 */
function createWrapper(func, bitmask, thisArg, partials, holders, argPos, ary, arity) {
  var isBindKey = bitmask & BIND_KEY_FLAG;
  if (!isBindKey && typeof func != 'function') {
    throw new TypeError(FUNC_ERROR_TEXT);
  }
  var length = partials ? partials.length : 0;
  if (!length) {
    bitmask &= ~(PARTIAL_FLAG | PARTIAL_RIGHT_FLAG);
    partials = holders = undefined;
  }
  length -= (holders ? holders.length : 0);
  if (bitmask & PARTIAL_RIGHT_FLAG) {
    var partialsRight = partials,
        holdersRight = holders;

    partials = holders = undefined;
  }
  var data = isBindKey ? undefined : getData(func),
      newData = [func, bitmask, thisArg, partials, holders, partialsRight, holdersRight, argPos, ary, arity];

  if (data) {
    mergeData(newData, data);
    bitmask = newData[1];
    arity = newData[9];
  }
  newData[9] = arity == null
    ? (isBindKey ? 0 : func.length)
    : (nativeMax(arity - length, 0) || 0);

  if (bitmask == BIND_FLAG) {
    var result = createBindWrapper(newData[0], newData[2]);
  } else if ((bitmask == PARTIAL_FLAG || bitmask == (BIND_FLAG | PARTIAL_FLAG)) && !newData[4].length) {
    result = createPartialWrapper.apply(undefined, newData);
  } else {
    result = createHybridWrapper.apply(undefined, newData);
  }
  var setter = data ? baseSetData : setData;
  return setter(result, newData);
}

module.exports = createWrapper;

},{"129":129,"142":142,"147":147,"148":148,"154":154,"168":168,"175":175}],151:[function(_dereq_,module,exports){
var arraySome = _dereq_(98);

/**
 * A specialized version of `baseIsEqualDeep` for arrays with support for
 * partial deep comparisons.
 *
 * @private
 * @param {Array} array The array to compare.
 * @param {Array} other The other array to compare.
 * @param {Function} equalFunc The function to determine equivalents of values.
 * @param {Function} [customizer] The function to customize comparing arrays.
 * @param {boolean} [isLoose] Specify performing partial comparisons.
 * @param {Array} [stackA] Tracks traversed `value` objects.
 * @param {Array} [stackB] Tracks traversed `other` objects.
 * @returns {boolean} Returns `true` if the arrays are equivalent, else `false`.
 */
function equalArrays(array, other, equalFunc, customizer, isLoose, stackA, stackB) {
  var index = -1,
      arrLength = array.length,
      othLength = other.length;

  if (arrLength != othLength && !(isLoose && othLength > arrLength)) {
    return false;
  }
  // Ignore non-index properties.
  while (++index < arrLength) {
    var arrValue = array[index],
        othValue = other[index],
        result = customizer ? customizer(isLoose ? othValue : arrValue, isLoose ? arrValue : othValue, index) : undefined;

    if (result !== undefined) {
      if (result) {
        continue;
      }
      return false;
    }
    // Recursively compare arrays (susceptible to call stack limits).
    if (isLoose) {
      if (!arraySome(other, function(othValue) {
            return arrValue === othValue || equalFunc(arrValue, othValue, customizer, isLoose, stackA, stackB);
          })) {
        return false;
      }
    } else if (!(arrValue === othValue || equalFunc(arrValue, othValue, customizer, isLoose, stackA, stackB))) {
      return false;
    }
  }
  return true;
}

module.exports = equalArrays;

},{"98":98}],152:[function(_dereq_,module,exports){
/** `Object#toString` result references. */
var boolTag = '[object Boolean]',
    dateTag = '[object Date]',
    errorTag = '[object Error]',
    numberTag = '[object Number]',
    regexpTag = '[object RegExp]',
    stringTag = '[object String]';

/**
 * A specialized version of `baseIsEqualDeep` for comparing objects of
 * the same `toStringTag`.
 *
 * **Note:** This function only supports comparing values with tags of
 * `Boolean`, `Date`, `Error`, `Number`, `RegExp`, or `String`.
 *
 * @private
 * @param {Object} object The object to compare.
 * @param {Object} other The other object to compare.
 * @param {string} tag The `toStringTag` of the objects to compare.
 * @returns {boolean} Returns `true` if the objects are equivalent, else `false`.
 */
function equalByTag(object, other, tag) {
  switch (tag) {
    case boolTag:
    case dateTag:
      // Coerce dates and booleans to numbers, dates to milliseconds and booleans
      // to `1` or `0` treating invalid dates coerced to `NaN` as not equal.
      return +object == +other;

    case errorTag:
      return object.name == other.name && object.message == other.message;

    case numberTag:
      // Treat `NaN` vs. `NaN` as equal.
      return (object != +object)
        ? other != +other
        : object == +other;

    case regexpTag:
    case stringTag:
      // Coerce regexes to strings and treat strings primitives and string
      // objects as equal. See https://es5.github.io/#x15.10.6.4 for more details.
      return object == (other + '');
  }
  return false;
}

module.exports = equalByTag;

},{}],153:[function(_dereq_,module,exports){
var keys = _dereq_(191);

/** Used for native method references. */
var objectProto = Object.prototype;

/** Used to check objects for own properties. */
var hasOwnProperty = objectProto.hasOwnProperty;

/**
 * A specialized version of `baseIsEqualDeep` for objects with support for
 * partial deep comparisons.
 *
 * @private
 * @param {Object} object The object to compare.
 * @param {Object} other The other object to compare.
 * @param {Function} equalFunc The function to determine equivalents of values.
 * @param {Function} [customizer] The function to customize comparing values.
 * @param {boolean} [isLoose] Specify performing partial comparisons.
 * @param {Array} [stackA] Tracks traversed `value` objects.
 * @param {Array} [stackB] Tracks traversed `other` objects.
 * @returns {boolean} Returns `true` if the objects are equivalent, else `false`.
 */
function equalObjects(object, other, equalFunc, customizer, isLoose, stackA, stackB) {
  var objProps = keys(object),
      objLength = objProps.length,
      othProps = keys(other),
      othLength = othProps.length;

  if (objLength != othLength && !isLoose) {
    return false;
  }
  var index = objLength;
  while (index--) {
    var key = objProps[index];
    if (!(isLoose ? key in other : hasOwnProperty.call(other, key))) {
      return false;
    }
  }
  var skipCtor = isLoose;
  while (++index < objLength) {
    key = objProps[index];
    var objValue = object[key],
        othValue = other[key],
        result = customizer ? customizer(isLoose ? othValue : objValue, isLoose? objValue : othValue, key) : undefined;

    // Recursively compare objects (susceptible to call stack limits).
    if (!(result === undefined ? equalFunc(objValue, othValue, customizer, isLoose, stackA, stackB) : result)) {
      return false;
    }
    skipCtor || (skipCtor = key == 'constructor');
  }
  if (!skipCtor) {
    var objCtor = object.constructor,
        othCtor = other.constructor;

    // Non `Object` object instances with different constructors are not equal.
    if (objCtor != othCtor &&
        ('constructor' in object && 'constructor' in other) &&
        !(typeof objCtor == 'function' && objCtor instanceof objCtor &&
          typeof othCtor == 'function' && othCtor instanceof othCtor)) {
      return false;
    }
  }
  return true;
}

module.exports = equalObjects;

},{"191":191}],154:[function(_dereq_,module,exports){
var metaMap = _dereq_(169),
    noop = _dereq_(198);

/**
 * Gets metadata for `func`.
 *
 * @private
 * @param {Function} func The function to query.
 * @returns {*} Returns the metadata for `func`.
 */
var getData = !metaMap ? noop : function(func) {
  return metaMap.get(func);
};

module.exports = getData;

},{"169":169,"198":198}],155:[function(_dereq_,module,exports){
var realNames = _dereq_(172);

/**
 * Gets the name of `func`.
 *
 * @private
 * @param {Function} func The function to query.
 * @returns {string} Returns the function name.
 */
function getFuncName(func) {
  var result = (func.name + ''),
      array = realNames[result],
      length = array ? array.length : 0;

  while (length--) {
    var data = array[length],
        otherFunc = data.func;
    if (otherFunc == null || otherFunc == func) {
      return data.name;
    }
  }
  return result;
}

module.exports = getFuncName;

},{"172":172}],156:[function(_dereq_,module,exports){
var baseProperty = _dereq_(126);

/**
 * Gets the "length" property value of `object`.
 *
 * **Note:** This function is used to avoid a [JIT bug](https://bugs.webkit.org/show_bug.cgi?id=142792)
 * that affects Safari on at least iOS 8.1-8.3 ARM64.
 *
 * @private
 * @param {Object} object The object to query.
 * @returns {*} Returns the "length" value.
 */
var getLength = baseProperty('length');

module.exports = getLength;

},{"126":126}],157:[function(_dereq_,module,exports){
var isStrictComparable = _dereq_(167),
    pairs = _dereq_(195);

/**
 * Gets the propery names, values, and compare flags of `object`.
 *
 * @private
 * @param {Object} object The object to query.
 * @returns {Array} Returns the match data of `object`.
 */
function getMatchData(object) {
  var result = pairs(object),
      length = result.length;

  while (length--) {
    result[length][2] = isStrictComparable(result[length][1]);
  }
  return result;
}

module.exports = getMatchData;

},{"167":167,"195":195}],158:[function(_dereq_,module,exports){
var isNative = _dereq_(183);

/**
 * Gets the native function at `key` of `object`.
 *
 * @private
 * @param {Object} object The object to query.
 * @param {string} key The key of the method to get.
 * @returns {*} Returns the function if it's native, else `undefined`.
 */
function getNative(object, key) {
  var value = object == null ? undefined : object[key];
  return isNative(value) ? value : undefined;
}

module.exports = getNative;

},{"183":183}],159:[function(_dereq_,module,exports){
/**
 * Gets the index at which the first occurrence of `NaN` is found in `array`.
 *
 * @private
 * @param {Array} array The array to search.
 * @param {number} fromIndex The index to search from.
 * @param {boolean} [fromRight] Specify iterating from right to left.
 * @returns {number} Returns the index of the matched `NaN`, else `-1`.
 */
function indexOfNaN(array, fromIndex, fromRight) {
  var length = array.length,
      index = fromIndex + (fromRight ? 0 : -1);

  while ((fromRight ? index-- : ++index < length)) {
    var other = array[index];
    if (other !== other) {
      return index;
    }
  }
  return -1;
}

module.exports = indexOfNaN;

},{}],160:[function(_dereq_,module,exports){
var getLength = _dereq_(156),
    isLength = _dereq_(165);

/**
 * Checks if `value` is array-like.
 *
 * @private
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is array-like, else `false`.
 */
function isArrayLike(value) {
  return value != null && isLength(getLength(value));
}

module.exports = isArrayLike;

},{"156":156,"165":165}],161:[function(_dereq_,module,exports){
/** Used to detect unsigned integer values. */
var reIsUint = /^\d+$/;

/**
 * Used as the [maximum length](http://ecma-international.org/ecma-262/6.0/#sec-number.max_safe_integer)
 * of an array-like value.
 */
var MAX_SAFE_INTEGER = 9007199254740991;

/**
 * Checks if `value` is a valid array-like index.
 *
 * @private
 * @param {*} value The value to check.
 * @param {number} [length=MAX_SAFE_INTEGER] The upper bounds of a valid index.
 * @returns {boolean} Returns `true` if `value` is a valid index, else `false`.
 */
function isIndex(value, length) {
  value = (typeof value == 'number' || reIsUint.test(value)) ? +value : -1;
  length = length == null ? MAX_SAFE_INTEGER : length;
  return value > -1 && value % 1 == 0 && value < length;
}

module.exports = isIndex;

},{}],162:[function(_dereq_,module,exports){
var isArrayLike = _dereq_(160),
    isIndex = _dereq_(161),
    isObject = _dereq_(185);

/**
 * Checks if the provided arguments are from an iteratee call.
 *
 * @private
 * @param {*} value The potential iteratee value argument.
 * @param {*} index The potential iteratee index or key argument.
 * @param {*} object The potential iteratee object argument.
 * @returns {boolean} Returns `true` if the arguments are from an iteratee call, else `false`.
 */
function isIterateeCall(value, index, object) {
  if (!isObject(object)) {
    return false;
  }
  var type = typeof index;
  if (type == 'number'
      ? (isArrayLike(object) && isIndex(index, object.length))
      : (type == 'string' && index in object)) {
    var other = object[index];
    return value === value ? (value === other) : (other !== other);
  }
  return false;
}

module.exports = isIterateeCall;

},{"160":160,"161":161,"185":185}],163:[function(_dereq_,module,exports){
var isArray = _dereq_(181),
    toObject = _dereq_(177);

/** Used to match property names within property paths. */
var reIsDeepProp = /\.|\[(?:[^[\]]*|(["'])(?:(?!\1)[^\n\\]|\\.)*?\1)\]/,
    reIsPlainProp = /^\w*$/;

/**
 * Checks if `value` is a property name and not a property path.
 *
 * @private
 * @param {*} value The value to check.
 * @param {Object} [object] The object to query keys on.
 * @returns {boolean} Returns `true` if `value` is a property name, else `false`.
 */
function isKey(value, object) {
  var type = typeof value;
  if ((type == 'string' && reIsPlainProp.test(value)) || type == 'number') {
    return true;
  }
  if (isArray(value)) {
    return false;
  }
  var result = !reIsDeepProp.test(value);
  return result || (object != null && value in toObject(object));
}

module.exports = isKey;

},{"177":177,"181":181}],164:[function(_dereq_,module,exports){
var LazyWrapper = _dereq_(88),
    getData = _dereq_(154),
    getFuncName = _dereq_(155),
    lodash = _dereq_(74);

/**
 * Checks if `func` has a lazy counterpart.
 *
 * @private
 * @param {Function} func The function to check.
 * @returns {boolean} Returns `true` if `func` has a lazy counterpart, else `false`.
 */
function isLaziable(func) {
  var funcName = getFuncName(func),
      other = lodash[funcName];

  if (typeof other != 'function' || !(funcName in LazyWrapper.prototype)) {
    return false;
  }
  if (func === other) {
    return true;
  }
  var data = getData(other);
  return !!data && func === data[0];
}

module.exports = isLaziable;

},{"154":154,"155":155,"74":74,"88":88}],165:[function(_dereq_,module,exports){
/**
 * Used as the [maximum length](http://ecma-international.org/ecma-262/6.0/#sec-number.max_safe_integer)
 * of an array-like value.
 */
var MAX_SAFE_INTEGER = 9007199254740991;

/**
 * Checks if `value` is a valid array-like length.
 *
 * **Note:** This function is based on [`ToLength`](http://ecma-international.org/ecma-262/6.0/#sec-tolength).
 *
 * @private
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is a valid length, else `false`.
 */
function isLength(value) {
  return typeof value == 'number' && value > -1 && value % 1 == 0 && value <= MAX_SAFE_INTEGER;
}

module.exports = isLength;

},{}],166:[function(_dereq_,module,exports){
/**
 * Checks if `value` is object-like.
 *
 * @private
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is object-like, else `false`.
 */
function isObjectLike(value) {
  return !!value && typeof value == 'object';
}

module.exports = isObjectLike;

},{}],167:[function(_dereq_,module,exports){
var isObject = _dereq_(185);

/**
 * Checks if `value` is suitable for strict equality comparisons, i.e. `===`.
 *
 * @private
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` if suitable for strict
 *  equality comparisons, else `false`.
 */
function isStrictComparable(value) {
  return value === value && !isObject(value);
}

module.exports = isStrictComparable;

},{"185":185}],168:[function(_dereq_,module,exports){
var arrayCopy = _dereq_(91),
    composeArgs = _dereq_(136),
    composeArgsRight = _dereq_(137),
    replaceHolders = _dereq_(174);

/** Used to compose bitmasks for wrapper metadata. */
var BIND_FLAG = 1,
    CURRY_BOUND_FLAG = 4,
    CURRY_FLAG = 8,
    ARY_FLAG = 128,
    REARG_FLAG = 256;

/** Used as the internal argument placeholder. */
var PLACEHOLDER = '__lodash_placeholder__';

/* Native method references for those with the same name as other `lodash` methods. */
var nativeMin = Math.min;

/**
 * Merges the function metadata of `source` into `data`.
 *
 * Merging metadata reduces the number of wrappers required to invoke a function.
 * This is possible because methods like `_.bind`, `_.curry`, and `_.partial`
 * may be applied regardless of execution order. Methods like `_.ary` and `_.rearg`
 * augment function arguments, making the order in which they are executed important,
 * preventing the merging of metadata. However, we make an exception for a safe
 * common case where curried functions have `_.ary` and or `_.rearg` applied.
 *
 * @private
 * @param {Array} data The destination metadata.
 * @param {Array} source The source metadata.
 * @returns {Array} Returns `data`.
 */
function mergeData(data, source) {
  var bitmask = data[1],
      srcBitmask = source[1],
      newBitmask = bitmask | srcBitmask,
      isCommon = newBitmask < ARY_FLAG;

  var isCombo =
    (srcBitmask == ARY_FLAG && bitmask == CURRY_FLAG) ||
    (srcBitmask == ARY_FLAG && bitmask == REARG_FLAG && data[7].length <= source[8]) ||
    (srcBitmask == (ARY_FLAG | REARG_FLAG) && bitmask == CURRY_FLAG);

  // Exit early if metadata can't be merged.
  if (!(isCommon || isCombo)) {
    return data;
  }
  // Use source `thisArg` if available.
  if (srcBitmask & BIND_FLAG) {
    data[2] = source[2];
    // Set when currying a bound function.
    newBitmask |= (bitmask & BIND_FLAG) ? 0 : CURRY_BOUND_FLAG;
  }
  // Compose partial arguments.
  var value = source[3];
  if (value) {
    var partials = data[3];
    data[3] = partials ? composeArgs(partials, value, source[4]) : arrayCopy(value);
    data[4] = partials ? replaceHolders(data[3], PLACEHOLDER) : arrayCopy(source[4]);
  }
  // Compose partial right arguments.
  value = source[5];
  if (value) {
    partials = data[5];
    data[5] = partials ? composeArgsRight(partials, value, source[6]) : arrayCopy(value);
    data[6] = partials ? replaceHolders(data[5], PLACEHOLDER) : arrayCopy(source[6]);
  }
  // Use source `argPos` if available.
  value = source[7];
  if (value) {
    data[7] = arrayCopy(value);
  }
  // Use source `ary` if it's smaller.
  if (srcBitmask & ARY_FLAG) {
    data[8] = data[8] == null ? source[8] : nativeMin(data[8], source[8]);
  }
  // Use source `arity` if one is not provided.
  if (data[9] == null) {
    data[9] = source[9];
  }
  // Use source `func` and merge bitmasks.
  data[0] = source[0];
  data[1] = newBitmask;

  return data;
}

module.exports = mergeData;

},{"136":136,"137":137,"174":174,"91":91}],169:[function(_dereq_,module,exports){
(function (global){
var getNative = _dereq_(158);

/** Native method references. */
var WeakMap = getNative(global, 'WeakMap');

/** Used to store function metadata. */
var metaMap = WeakMap && new WeakMap;

module.exports = metaMap;

}).call(this,typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})

},{"158":158}],170:[function(_dereq_,module,exports){
var toObject = _dereq_(177);

/**
 * A specialized version of `_.pick` which picks `object` properties specified
 * by `props`.
 *
 * @private
 * @param {Object} object The source object.
 * @param {string[]} props The property names to pick.
 * @returns {Object} Returns the new object.
 */
function pickByArray(object, props) {
  object = toObject(object);

  var index = -1,
      length = props.length,
      result = {};

  while (++index < length) {
    var key = props[index];
    if (key in object) {
      result[key] = object[key];
    }
  }
  return result;
}

module.exports = pickByArray;

},{"177":177}],171:[function(_dereq_,module,exports){
var baseForIn = _dereq_(113);

/**
 * A specialized version of `_.pick` which picks `object` properties `predicate`
 * returns truthy for.
 *
 * @private
 * @param {Object} object The source object.
 * @param {Function} predicate The function invoked per iteration.
 * @returns {Object} Returns the new object.
 */
function pickByCallback(object, predicate) {
  var result = {};
  baseForIn(object, function(value, key, object) {
    if (predicate(value, key, object)) {
      result[key] = value;
    }
  });
  return result;
}

module.exports = pickByCallback;

},{"113":113}],172:[function(_dereq_,module,exports){
/** Used to lookup unminified function names. */
var realNames = {};

module.exports = realNames;

},{}],173:[function(_dereq_,module,exports){
var arrayCopy = _dereq_(91),
    isIndex = _dereq_(161);

/* Native method references for those with the same name as other `lodash` methods. */
var nativeMin = Math.min;

/**
 * Reorder `array` according to the specified indexes where the element at
 * the first index is assigned as the first element, the element at
 * the second index is assigned as the second element, and so on.
 *
 * @private
 * @param {Array} array The array to reorder.
 * @param {Array} indexes The arranged array indexes.
 * @returns {Array} Returns `array`.
 */
function reorder(array, indexes) {
  var arrLength = array.length,
      length = nativeMin(indexes.length, arrLength),
      oldArray = arrayCopy(array);

  while (length--) {
    var index = indexes[length];
    array[length] = isIndex(index, arrLength) ? oldArray[index] : undefined;
  }
  return array;
}

module.exports = reorder;

},{"161":161,"91":91}],174:[function(_dereq_,module,exports){
/** Used as the internal argument placeholder. */
var PLACEHOLDER = '__lodash_placeholder__';

/**
 * Replaces all `placeholder` elements in `array` with an internal placeholder
 * and returns an array of their indexes.
 *
 * @private
 * @param {Array} array The array to modify.
 * @param {*} placeholder The placeholder to replace.
 * @returns {Array} Returns the new array of placeholder indexes.
 */
function replaceHolders(array, placeholder) {
  var index = -1,
      length = array.length,
      resIndex = -1,
      result = [];

  while (++index < length) {
    if (array[index] === placeholder) {
      array[index] = PLACEHOLDER;
      result[++resIndex] = index;
    }
  }
  return result;
}

module.exports = replaceHolders;

},{}],175:[function(_dereq_,module,exports){
var baseSetData = _dereq_(129),
    now = _dereq_(83);

/** Used to detect when a function becomes hot. */
var HOT_COUNT = 150,
    HOT_SPAN = 16;

/**
 * Sets metadata for `func`.
 *
 * **Note:** If this function becomes hot, i.e. is invoked a lot in a short
 * period of time, it will trip its breaker and transition to an identity function
 * to avoid garbage collection pauses in V8. See [V8 issue 2070](https://code.google.com/p/v8/issues/detail?id=2070)
 * for more details.
 *
 * @private
 * @param {Function} func The function to associate metadata with.
 * @param {*} data The metadata.
 * @returns {Function} Returns `func`.
 */
var setData = (function() {
  var count = 0,
      lastCalled = 0;

  return function(key, value) {
    var stamp = now(),
        remaining = HOT_SPAN - (stamp - lastCalled);

    lastCalled = stamp;
    if (remaining > 0) {
      if (++count >= HOT_COUNT) {
        return key;
      }
    } else {
      count = 0;
    }
    return baseSetData(key, value);
  };
}());

module.exports = setData;

},{"129":129,"83":83}],176:[function(_dereq_,module,exports){
var isArguments = _dereq_(180),
    isArray = _dereq_(181),
    isIndex = _dereq_(161),
    isLength = _dereq_(165),
    keysIn = _dereq_(192);

/** Used for native method references. */
var objectProto = Object.prototype;

/** Used to check objects for own properties. */
var hasOwnProperty = objectProto.hasOwnProperty;

/**
 * A fallback implementation of `Object.keys` which creates an array of the
 * own enumerable property names of `object`.
 *
 * @private
 * @param {Object} object The object to query.
 * @returns {Array} Returns the array of property names.
 */
function shimKeys(object) {
  var props = keysIn(object),
      propsLength = props.length,
      length = propsLength && object.length;

  var allowIndexes = !!length && isLength(length) &&
    (isArray(object) || isArguments(object));

  var index = -1,
      result = [];

  while (++index < propsLength) {
    var key = props[index];
    if ((allowIndexes && isIndex(key, length)) || hasOwnProperty.call(object, key)) {
      result.push(key);
    }
  }
  return result;
}

module.exports = shimKeys;

},{"161":161,"165":165,"180":180,"181":181,"192":192}],177:[function(_dereq_,module,exports){
var isObject = _dereq_(185);

/**
 * Converts `value` to an object if it's not one.
 *
 * @private
 * @param {*} value The value to process.
 * @returns {Object} Returns the object.
 */
function toObject(value) {
  return isObject(value) ? value : Object(value);
}

module.exports = toObject;

},{"185":185}],178:[function(_dereq_,module,exports){
var baseToString = _dereq_(132),
    isArray = _dereq_(181);

/** Used to match property names within property paths. */
var rePropName = /[^.[\]]+|\[(?:(-?\d+(?:\.\d+)?)|(["'])((?:(?!\2)[^\n\\]|\\.)*?)\2)\]/g;

/** Used to match backslashes in property paths. */
var reEscapeChar = /\\(\\)?/g;

/**
 * Converts `value` to property path array if it's not one.
 *
 * @private
 * @param {*} value The value to process.
 * @returns {Array} Returns the property path array.
 */
function toPath(value) {
  if (isArray(value)) {
    return value;
  }
  var result = [];
  baseToString(value).replace(rePropName, function(match, number, quote, string) {
    result.push(quote ? string.replace(reEscapeChar, '$1') : (number || match));
  });
  return result;
}

module.exports = toPath;

},{"132":132,"181":181}],179:[function(_dereq_,module,exports){
var LazyWrapper = _dereq_(88),
    LodashWrapper = _dereq_(89),
    arrayCopy = _dereq_(91);

/**
 * Creates a clone of `wrapper`.
 *
 * @private
 * @param {Object} wrapper The wrapper to clone.
 * @returns {Object} Returns the cloned wrapper.
 */
function wrapperClone(wrapper) {
  return wrapper instanceof LazyWrapper
    ? wrapper.clone()
    : new LodashWrapper(wrapper.__wrapped__, wrapper.__chain__, arrayCopy(wrapper.__actions__));
}

module.exports = wrapperClone;

},{"88":88,"89":89,"91":91}],180:[function(_dereq_,module,exports){
var isArrayLike = _dereq_(160),
    isObjectLike = _dereq_(166);

/** Used for native method references. */
var objectProto = Object.prototype;

/** Used to check objects for own properties. */
var hasOwnProperty = objectProto.hasOwnProperty;

/** Native method references. */
var propertyIsEnumerable = objectProto.propertyIsEnumerable;

/**
 * Checks if `value` is classified as an `arguments` object.
 *
 * @static
 * @memberOf _
 * @category Lang
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is correctly classified, else `false`.
 * @example
 *
 * _.isArguments(function() { return arguments; }());
 * // => true
 *
 * _.isArguments([1, 2, 3]);
 * // => false
 */
function isArguments(value) {
  return isObjectLike(value) && isArrayLike(value) &&
    hasOwnProperty.call(value, 'callee') && !propertyIsEnumerable.call(value, 'callee');
}

module.exports = isArguments;

},{"160":160,"166":166}],181:[function(_dereq_,module,exports){
var getNative = _dereq_(158),
    isLength = _dereq_(165),
    isObjectLike = _dereq_(166);

/** `Object#toString` result references. */
var arrayTag = '[object Array]';

/** Used for native method references. */
var objectProto = Object.prototype;

/**
 * Used to resolve the [`toStringTag`](http://ecma-international.org/ecma-262/6.0/#sec-object.prototype.tostring)
 * of values.
 */
var objToString = objectProto.toString;

/* Native method references for those with the same name as other `lodash` methods. */
var nativeIsArray = getNative(Array, 'isArray');

/**
 * Checks if `value` is classified as an `Array` object.
 *
 * @static
 * @memberOf _
 * @category Lang
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is correctly classified, else `false`.
 * @example
 *
 * _.isArray([1, 2, 3]);
 * // => true
 *
 * _.isArray(function() { return arguments; }());
 * // => false
 */
var isArray = nativeIsArray || function(value) {
  return isObjectLike(value) && isLength(value.length) && objToString.call(value) == arrayTag;
};

module.exports = isArray;

},{"158":158,"165":165,"166":166}],182:[function(_dereq_,module,exports){
var isObject = _dereq_(185);

/** `Object#toString` result references. */
var funcTag = '[object Function]';

/** Used for native method references. */
var objectProto = Object.prototype;

/**
 * Used to resolve the [`toStringTag`](http://ecma-international.org/ecma-262/6.0/#sec-object.prototype.tostring)
 * of values.
 */
var objToString = objectProto.toString;

/**
 * Checks if `value` is classified as a `Function` object.
 *
 * @static
 * @memberOf _
 * @category Lang
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is correctly classified, else `false`.
 * @example
 *
 * _.isFunction(_);
 * // => true
 *
 * _.isFunction(/abc/);
 * // => false
 */
function isFunction(value) {
  // The use of `Object#toString` avoids issues with the `typeof` operator
  // in older versions of Chrome and Safari which return 'function' for regexes
  // and Safari 8 which returns 'object' for typed array constructors.
  return isObject(value) && objToString.call(value) == funcTag;
}

module.exports = isFunction;

},{"185":185}],183:[function(_dereq_,module,exports){
var isFunction = _dereq_(182),
    isObjectLike = _dereq_(166);

/** Used to detect host constructors (Safari > 5). */
var reIsHostCtor = /^\[object .+?Constructor\]$/;

/** Used for native method references. */
var objectProto = Object.prototype;

/** Used to resolve the decompiled source of functions. */
var fnToString = Function.prototype.toString;

/** Used to check objects for own properties. */
var hasOwnProperty = objectProto.hasOwnProperty;

/** Used to detect if a method is native. */
var reIsNative = RegExp('^' +
  fnToString.call(hasOwnProperty).replace(/[\\^$.*+?()[\]{}|]/g, '\\$&')
  .replace(/hasOwnProperty|(function).*?(?=\\\()| for .+?(?=\\\])/g, '$1.*?') + '$'
);

/**
 * Checks if `value` is a native function.
 *
 * @static
 * @memberOf _
 * @category Lang
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is a native function, else `false`.
 * @example
 *
 * _.isNative(Array.prototype.push);
 * // => true
 *
 * _.isNative(_);
 * // => false
 */
function isNative(value) {
  if (value == null) {
    return false;
  }
  if (isFunction(value)) {
    return reIsNative.test(fnToString.call(value));
  }
  return isObjectLike(value) && reIsHostCtor.test(value);
}

module.exports = isNative;

},{"166":166,"182":182}],184:[function(_dereq_,module,exports){
var isObjectLike = _dereq_(166);

/** `Object#toString` result references. */
var numberTag = '[object Number]';

/** Used for native method references. */
var objectProto = Object.prototype;

/**
 * Used to resolve the [`toStringTag`](http://ecma-international.org/ecma-262/6.0/#sec-object.prototype.tostring)
 * of values.
 */
var objToString = objectProto.toString;

/**
 * Checks if `value` is classified as a `Number` primitive or object.
 *
 * **Note:** To exclude `Infinity`, `-Infinity`, and `NaN`, which are classified
 * as numbers, use the `_.isFinite` method.
 *
 * @static
 * @memberOf _
 * @category Lang
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is correctly classified, else `false`.
 * @example
 *
 * _.isNumber(8.4);
 * // => true
 *
 * _.isNumber(NaN);
 * // => true
 *
 * _.isNumber('8.4');
 * // => false
 */
function isNumber(value) {
  return typeof value == 'number' || (isObjectLike(value) && objToString.call(value) == numberTag);
}

module.exports = isNumber;

},{"166":166}],185:[function(_dereq_,module,exports){
/**
 * Checks if `value` is the [language type](https://es5.github.io/#x8) of `Object`.
 * (e.g. arrays, functions, objects, regexes, `new Number(0)`, and `new String('')`)
 *
 * @static
 * @memberOf _
 * @category Lang
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is an object, else `false`.
 * @example
 *
 * _.isObject({});
 * // => true
 *
 * _.isObject([1, 2, 3]);
 * // => true
 *
 * _.isObject(1);
 * // => false
 */
function isObject(value) {
  // Avoid a V8 JIT bug in Chrome 19-20.
  // See https://code.google.com/p/v8/issues/detail?id=2291 for more details.
  var type = typeof value;
  return !!value && (type == 'object' || type == 'function');
}

module.exports = isObject;

},{}],186:[function(_dereq_,module,exports){
var baseForIn = _dereq_(113),
    isArguments = _dereq_(180),
    isObjectLike = _dereq_(166);

/** `Object#toString` result references. */
var objectTag = '[object Object]';

/** Used for native method references. */
var objectProto = Object.prototype;

/** Used to check objects for own properties. */
var hasOwnProperty = objectProto.hasOwnProperty;

/**
 * Used to resolve the [`toStringTag`](http://ecma-international.org/ecma-262/6.0/#sec-object.prototype.tostring)
 * of values.
 */
var objToString = objectProto.toString;

/**
 * Checks if `value` is a plain object, that is, an object created by the
 * `Object` constructor or one with a `[[Prototype]]` of `null`.
 *
 * **Note:** This method assumes objects created by the `Object` constructor
 * have no inherited enumerable properties.
 *
 * @static
 * @memberOf _
 * @category Lang
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is a plain object, else `false`.
 * @example
 *
 * function Foo() {
 *   this.a = 1;
 * }
 *
 * _.isPlainObject(new Foo);
 * // => false
 *
 * _.isPlainObject([1, 2, 3]);
 * // => false
 *
 * _.isPlainObject({ 'x': 0, 'y': 0 });
 * // => true
 *
 * _.isPlainObject(Object.create(null));
 * // => true
 */
function isPlainObject(value) {
  var Ctor;

  // Exit early for non `Object` objects.
  if (!(isObjectLike(value) && objToString.call(value) == objectTag && !isArguments(value)) ||
      (!hasOwnProperty.call(value, 'constructor') && (Ctor = value.constructor, typeof Ctor == 'function' && !(Ctor instanceof Ctor)))) {
    return false;
  }
  // IE < 9 iterates inherited properties before own properties. If the first
  // iterated property is an object's own property then there are no inherited
  // enumerable properties.
  var result;
  // In most environments an object's own properties are iterated before
  // its inherited properties. If the last iterated property is an object's
  // own property then there are no inherited enumerable properties.
  baseForIn(value, function(subValue, key) {
    result = key;
  });
  return result === undefined || hasOwnProperty.call(value, result);
}

module.exports = isPlainObject;

},{"113":113,"166":166,"180":180}],187:[function(_dereq_,module,exports){
var isObjectLike = _dereq_(166);

/** `Object#toString` result references. */
var stringTag = '[object String]';

/** Used for native method references. */
var objectProto = Object.prototype;

/**
 * Used to resolve the [`toStringTag`](http://ecma-international.org/ecma-262/6.0/#sec-object.prototype.tostring)
 * of values.
 */
var objToString = objectProto.toString;

/**
 * Checks if `value` is classified as a `String` primitive or object.
 *
 * @static
 * @memberOf _
 * @category Lang
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is correctly classified, else `false`.
 * @example
 *
 * _.isString('abc');
 * // => true
 *
 * _.isString(1);
 * // => false
 */
function isString(value) {
  return typeof value == 'string' || (isObjectLike(value) && objToString.call(value) == stringTag);
}

module.exports = isString;

},{"166":166}],188:[function(_dereq_,module,exports){
var isLength = _dereq_(165),
    isObjectLike = _dereq_(166);

/** `Object#toString` result references. */
var argsTag = '[object Arguments]',
    arrayTag = '[object Array]',
    boolTag = '[object Boolean]',
    dateTag = '[object Date]',
    errorTag = '[object Error]',
    funcTag = '[object Function]',
    mapTag = '[object Map]',
    numberTag = '[object Number]',
    objectTag = '[object Object]',
    regexpTag = '[object RegExp]',
    setTag = '[object Set]',
    stringTag = '[object String]',
    weakMapTag = '[object WeakMap]';

var arrayBufferTag = '[object ArrayBuffer]',
    float32Tag = '[object Float32Array]',
    float64Tag = '[object Float64Array]',
    int8Tag = '[object Int8Array]',
    int16Tag = '[object Int16Array]',
    int32Tag = '[object Int32Array]',
    uint8Tag = '[object Uint8Array]',
    uint8ClampedTag = '[object Uint8ClampedArray]',
    uint16Tag = '[object Uint16Array]',
    uint32Tag = '[object Uint32Array]';

/** Used to identify `toStringTag` values of typed arrays. */
var typedArrayTags = {};
typedArrayTags[float32Tag] = typedArrayTags[float64Tag] =
typedArrayTags[int8Tag] = typedArrayTags[int16Tag] =
typedArrayTags[int32Tag] = typedArrayTags[uint8Tag] =
typedArrayTags[uint8ClampedTag] = typedArrayTags[uint16Tag] =
typedArrayTags[uint32Tag] = true;
typedArrayTags[argsTag] = typedArrayTags[arrayTag] =
typedArrayTags[arrayBufferTag] = typedArrayTags[boolTag] =
typedArrayTags[dateTag] = typedArrayTags[errorTag] =
typedArrayTags[funcTag] = typedArrayTags[mapTag] =
typedArrayTags[numberTag] = typedArrayTags[objectTag] =
typedArrayTags[regexpTag] = typedArrayTags[setTag] =
typedArrayTags[stringTag] = typedArrayTags[weakMapTag] = false;

/** Used for native method references. */
var objectProto = Object.prototype;

/**
 * Used to resolve the [`toStringTag`](http://ecma-international.org/ecma-262/6.0/#sec-object.prototype.tostring)
 * of values.
 */
var objToString = objectProto.toString;

/**
 * Checks if `value` is classified as a typed array.
 *
 * @static
 * @memberOf _
 * @category Lang
 * @param {*} value The value to check.
 * @returns {boolean} Returns `true` if `value` is correctly classified, else `false`.
 * @example
 *
 * _.isTypedArray(new Uint8Array);
 * // => true
 *
 * _.isTypedArray([]);
 * // => false
 */
function isTypedArray(value) {
  return isObjectLike(value) && isLength(value.length) && !!typedArrayTags[objToString.call(value)];
}

module.exports = isTypedArray;

},{"165":165,"166":166}],189:[function(_dereq_,module,exports){
var baseCopy = _dereq_(102),
    keysIn = _dereq_(192);

/**
 * Converts `value` to a plain object flattening inherited enumerable
 * properties of `value` to own properties of the plain object.
 *
 * @static
 * @memberOf _
 * @category Lang
 * @param {*} value The value to convert.
 * @returns {Object} Returns the converted plain object.
 * @example
 *
 * function Foo() {
 *   this.b = 2;
 * }
 *
 * Foo.prototype.c = 3;
 *
 * _.assign({ 'a': 1 }, new Foo);
 * // => { 'a': 1, 'b': 2 }
 *
 * _.assign({ 'a': 1 }, _.toPlainObject(new Foo));
 * // => { 'a': 1, 'b': 2, 'c': 3 }
 */
function toPlainObject(value) {
  return baseCopy(value, keysIn(value));
}

module.exports = toPlainObject;

},{"102":102,"192":192}],190:[function(_dereq_,module,exports){
var assignWith = _dereq_(99),
    baseAssign = _dereq_(100),
    createAssigner = _dereq_(139);

/**
 * Assigns own enumerable properties of source object(s) to the destination
 * object. Subsequent sources overwrite property assignments of previous sources.
 * If `customizer` is provided it's invoked to produce the assigned values.
 * The `customizer` is bound to `thisArg` and invoked with five arguments:
 * (objectValue, sourceValue, key, object, source).
 *
 * **Note:** This method mutates `object` and is based on
 * [`Object.assign`](http://ecma-international.org/ecma-262/6.0/#sec-object.assign).
 *
 * @static
 * @memberOf _
 * @alias extend
 * @category Object
 * @param {Object} object The destination object.
 * @param {...Object} [sources] The source objects.
 * @param {Function} [customizer] The function to customize assigned values.
 * @param {*} [thisArg] The `this` binding of `customizer`.
 * @returns {Object} Returns `object`.
 * @example
 *
 * _.assign({ 'user': 'barney' }, { 'age': 40 }, { 'user': 'fred' });
 * // => { 'user': 'fred', 'age': 40 }
 *
 * // using a customizer callback
 * var defaults = _.partialRight(_.assign, function(value, other) {
 *   return _.isUndefined(value) ? other : value;
 * });
 *
 * defaults({ 'user': 'barney' }, { 'age': 36 }, { 'user': 'fred' });
 * // => { 'user': 'barney', 'age': 36 }
 */
var assign = createAssigner(function(object, source, customizer) {
  return customizer
    ? assignWith(object, source, customizer)
    : baseAssign(object, source);
});

module.exports = assign;

},{"100":100,"139":139,"99":99}],191:[function(_dereq_,module,exports){
var getNative = _dereq_(158),
    isArrayLike = _dereq_(160),
    isObject = _dereq_(185),
    shimKeys = _dereq_(176);

/* Native method references for those with the same name as other `lodash` methods. */
var nativeKeys = getNative(Object, 'keys');

/**
 * Creates an array of the own enumerable property names of `object`.
 *
 * **Note:** Non-object values are coerced to objects. See the
 * [ES spec](http://ecma-international.org/ecma-262/6.0/#sec-object.keys)
 * for more details.
 *
 * @static
 * @memberOf _
 * @category Object
 * @param {Object} object The object to query.
 * @returns {Array} Returns the array of property names.
 * @example
 *
 * function Foo() {
 *   this.a = 1;
 *   this.b = 2;
 * }
 *
 * Foo.prototype.c = 3;
 *
 * _.keys(new Foo);
 * // => ['a', 'b'] (iteration order is not guaranteed)
 *
 * _.keys('hi');
 * // => ['0', '1']
 */
var keys = !nativeKeys ? shimKeys : function(object) {
  var Ctor = object == null ? undefined : object.constructor;
  if ((typeof Ctor == 'function' && Ctor.prototype === object) ||
      (typeof object != 'function' && isArrayLike(object))) {
    return shimKeys(object);
  }
  return isObject(object) ? nativeKeys(object) : [];
};

module.exports = keys;

},{"158":158,"160":160,"176":176,"185":185}],192:[function(_dereq_,module,exports){
var isArguments = _dereq_(180),
    isArray = _dereq_(181),
    isIndex = _dereq_(161),
    isLength = _dereq_(165),
    isObject = _dereq_(185);

/** Used for native method references. */
var objectProto = Object.prototype;

/** Used to check objects for own properties. */
var hasOwnProperty = objectProto.hasOwnProperty;

/**
 * Creates an array of the own and inherited enumerable property names of `object`.
 *
 * **Note:** Non-object values are coerced to objects.
 *
 * @static
 * @memberOf _
 * @category Object
 * @param {Object} object The object to query.
 * @returns {Array} Returns the array of property names.
 * @example
 *
 * function Foo() {
 *   this.a = 1;
 *   this.b = 2;
 * }
 *
 * Foo.prototype.c = 3;
 *
 * _.keysIn(new Foo);
 * // => ['a', 'b', 'c'] (iteration order is not guaranteed)
 */
function keysIn(object) {
  if (object == null) {
    return [];
  }
  if (!isObject(object)) {
    object = Object(object);
  }
  var length = object.length;
  length = (length && isLength(length) &&
    (isArray(object) || isArguments(object)) && length) || 0;

  var Ctor = object.constructor,
      index = -1,
      isProto = typeof Ctor == 'function' && Ctor.prototype === object,
      result = Array(length),
      skipIndexes = length > 0;

  while (++index < length) {
    result[index] = (index + '');
  }
  for (var key in object) {
    if (!(skipIndexes && isIndex(key, length)) &&
        !(key == 'constructor' && (isProto || !hasOwnProperty.call(object, key)))) {
      result.push(key);
    }
  }
  return result;
}

module.exports = keysIn;

},{"161":161,"165":165,"180":180,"181":181,"185":185}],193:[function(_dereq_,module,exports){
var baseMerge = _dereq_(124),
    createAssigner = _dereq_(139);

/**
 * Recursively merges own enumerable properties of the source object(s), that
 * don't resolve to `undefined` into the destination object. Subsequent sources
 * overwrite property assignments of previous sources. If `customizer` is
 * provided it's invoked to produce the merged values of the destination and
 * source properties. If `customizer` returns `undefined` merging is handled
 * by the method instead. The `customizer` is bound to `thisArg` and invoked
 * with five arguments: (objectValue, sourceValue, key, object, source).
 *
 * @static
 * @memberOf _
 * @category Object
 * @param {Object} object The destination object.
 * @param {...Object} [sources] The source objects.
 * @param {Function} [customizer] The function to customize assigned values.
 * @param {*} [thisArg] The `this` binding of `customizer`.
 * @returns {Object} Returns `object`.
 * @example
 *
 * var users = {
 *   'data': [{ 'user': 'barney' }, { 'user': 'fred' }]
 * };
 *
 * var ages = {
 *   'data': [{ 'age': 36 }, { 'age': 40 }]
 * };
 *
 * _.merge(users, ages);
 * // => { 'data': [{ 'user': 'barney', 'age': 36 }, { 'user': 'fred', 'age': 40 }] }
 *
 * // using a customizer callback
 * var object = {
 *   'fruits': ['apple'],
 *   'vegetables': ['beet']
 * };
 *
 * var other = {
 *   'fruits': ['banana'],
 *   'vegetables': ['carrot']
 * };
 *
 * _.merge(object, other, function(a, b) {
 *   if (_.isArray(a)) {
 *     return a.concat(b);
 *   }
 * });
 * // => { 'fruits': ['apple', 'banana'], 'vegetables': ['beet', 'carrot'] }
 */
var merge = createAssigner(baseMerge);

module.exports = merge;

},{"124":124,"139":139}],194:[function(_dereq_,module,exports){
var arrayMap = _dereq_(95),
    baseDifference = _dereq_(105),
    baseFlatten = _dereq_(111),
    bindCallback = _dereq_(133),
    keysIn = _dereq_(192),
    pickByArray = _dereq_(170),
    pickByCallback = _dereq_(171),
    restParam = _dereq_(87);

/**
 * The opposite of `_.pick`; this method creates an object composed of the
 * own and inherited enumerable properties of `object` that are not omitted.
 *
 * @static
 * @memberOf _
 * @category Object
 * @param {Object} object The source object.
 * @param {Function|...(string|string[])} [predicate] The function invoked per
 *  iteration or property names to omit, specified as individual property
 *  names or arrays of property names.
 * @param {*} [thisArg] The `this` binding of `predicate`.
 * @returns {Object} Returns the new object.
 * @example
 *
 * var object = { 'user': 'fred', 'age': 40 };
 *
 * _.omit(object, 'age');
 * // => { 'user': 'fred' }
 *
 * _.omit(object, _.isNumber);
 * // => { 'user': 'fred' }
 */
var omit = restParam(function(object, props) {
  if (object == null) {
    return {};
  }
  if (typeof props[0] != 'function') {
    var props = arrayMap(baseFlatten(props), String);
    return pickByArray(object, baseDifference(keysIn(object), props));
  }
  var predicate = bindCallback(props[0], props[1], 3);
  return pickByCallback(object, function(value, key, object) {
    return !predicate(value, key, object);
  });
});

module.exports = omit;

},{"105":105,"111":111,"133":133,"170":170,"171":171,"192":192,"87":87,"95":95}],195:[function(_dereq_,module,exports){
var keys = _dereq_(191),
    toObject = _dereq_(177);

/**
 * Creates a two dimensional array of the key-value pairs for `object`,
 * e.g. `[[key1, value1], [key2, value2]]`.
 *
 * @static
 * @memberOf _
 * @category Object
 * @param {Object} object The object to query.
 * @returns {Array} Returns the new array of key-value pairs.
 * @example
 *
 * _.pairs({ 'barney': 36, 'fred': 40 });
 * // => [['barney', 36], ['fred', 40]] (iteration order is not guaranteed)
 */
function pairs(object) {
  object = toObject(object);

  var index = -1,
      props = keys(object),
      length = props.length,
      result = Array(length);

  while (++index < length) {
    var key = props[index];
    result[index] = [key, object[key]];
  }
  return result;
}

module.exports = pairs;

},{"177":177,"191":191}],196:[function(_dereq_,module,exports){
var baseFlatten = _dereq_(111),
    bindCallback = _dereq_(133),
    pickByArray = _dereq_(170),
    pickByCallback = _dereq_(171),
    restParam = _dereq_(87);

/**
 * Creates an object composed of the picked `object` properties. Property
 * names may be specified as individual arguments or as arrays of property
 * names. If `predicate` is provided it's invoked for each property of `object`
 * picking the properties `predicate` returns truthy for. The predicate is
 * bound to `thisArg` and invoked with three arguments: (value, key, object).
 *
 * @static
 * @memberOf _
 * @category Object
 * @param {Object} object The source object.
 * @param {Function|...(string|string[])} [predicate] The function invoked per
 *  iteration or property names to pick, specified as individual property
 *  names or arrays of property names.
 * @param {*} [thisArg] The `this` binding of `predicate`.
 * @returns {Object} Returns the new object.
 * @example
 *
 * var object = { 'user': 'fred', 'age': 40 };
 *
 * _.pick(object, 'user');
 * // => { 'user': 'fred' }
 *
 * _.pick(object, _.isString);
 * // => { 'user': 'fred' }
 */
var pick = restParam(function(object, props) {
  if (object == null) {
    return {};
  }
  return typeof props[0] == 'function'
    ? pickByCallback(object, bindCallback(props[0], props[1], 3))
    : pickByArray(object, baseFlatten(props));
});

module.exports = pick;

},{"111":111,"133":133,"170":170,"171":171,"87":87}],197:[function(_dereq_,module,exports){
/**
 * This method returns the first argument provided to it.
 *
 * @static
 * @memberOf _
 * @category Utility
 * @param {*} value Any value.
 * @returns {*} Returns `value`.
 * @example
 *
 * var object = { 'user': 'fred' };
 *
 * _.identity(object) === object;
 * // => true
 */
function identity(value) {
  return value;
}

module.exports = identity;

},{}],198:[function(_dereq_,module,exports){
/**
 * A no-operation function that returns `undefined` regardless of the
 * arguments it receives.
 *
 * @static
 * @memberOf _
 * @category Utility
 * @example
 *
 * var object = { 'user': 'fred' };
 *
 * _.noop(object) === undefined;
 * // => true
 */
function noop() {
  // No operation performed.
}

module.exports = noop;

},{}],199:[function(_dereq_,module,exports){
var baseProperty = _dereq_(126),
    basePropertyDeep = _dereq_(127),
    isKey = _dereq_(163);

/**
 * Creates a function that returns the property value at `path` on a
 * given object.
 *
 * @static
 * @memberOf _
 * @category Utility
 * @param {Array|string} path The path of the property to get.
 * @returns {Function} Returns the new function.
 * @example
 *
 * var objects = [
 *   { 'a': { 'b': { 'c': 2 } } },
 *   { 'a': { 'b': { 'c': 1 } } }
 * ];
 *
 * _.map(objects, _.property('a.b.c'));
 * // => [2, 1]
 *
 * _.pluck(_.sortBy(objects, _.property(['a', 'b', 'c'])), 'a.b.c');
 * // => [1, 2]
 */
function property(path) {
  return isKey(path) ? baseProperty(path) : basePropertyDeep(path);
}

module.exports = property;

},{"126":126,"127":127,"163":163}],200:[function(_dereq_,module,exports){
/**
 * Set attribute `name` to `val`, or get attr `name`.
 *
 * @param {Element} el
 * @param {String} name
 * @param {String} [val]
 * @api public
 */

module.exports = function(el, name, val) {
  // get
  if (arguments.length == 2) {
    return el.getAttribute(name);
  }

  // remove
  if (val === null) {
    return el.removeAttribute(name);
  }

  // set
  el.setAttribute(name, val);

  return el;
};
},{}],201:[function(_dereq_,module,exports){
module.exports = _dereq_(23);
},{"23":23}],202:[function(_dereq_,module,exports){
module.exports = function(el) {

  var c;

  while (el.childNodes.length) {
    c = el.childNodes[0];
    el.removeChild(c);
  }

  return el;
};
},{}],203:[function(_dereq_,module,exports){
module.exports = _dereq_(25);
},{"25":25}],204:[function(_dereq_,module,exports){
module.exports = _dereq_(69);
},{"69":69}],205:[function(_dereq_,module,exports){
module.exports = _dereq_(26);
},{"26":26}],206:[function(_dereq_,module,exports){
module.exports = _dereq_(29);
},{"29":29}],207:[function(_dereq_,module,exports){
module.exports = function(el) {
  el.parentNode && el.parentNode.removeChild(el);
};
},{}],208:[function(_dereq_,module,exports){
'use strict';

function hasLowerCaseAlias(pkg) {
  return pkg.xml && pkg.xml.tagAlias === 'lowerCase';
}

module.exports.hasLowerCaseAlias = hasLowerCaseAlias;


module.exports.DEFAULT_NS_MAP = {
  'xsi': 'http://www.w3.org/2001/XMLSchema-instance'
};

var XSI_TYPE = module.exports.XSI_TYPE = 'xsi:type';

function serializeFormat(element) {
  return element.xml && element.xml.serialize;
}

module.exports.serializeAsType = function(element) {
  return serializeFormat(element) === XSI_TYPE;
};

module.exports.serializeAsProperty = function(element) {
  return serializeFormat(element) === 'property';
};
},{}],209:[function(_dereq_,module,exports){
'use strict';

var forEach = _dereq_(78),
    find = _dereq_(77),
    assign = _dereq_(190),
    defer = _dereq_(86);

var Stack = _dereq_(225),
    SaxParser = _dereq_(224),
    Moddle = _dereq_(211),
    parseNameNs = _dereq_(216).parseName,
    Types = _dereq_(219),
    coerceType = Types.coerceType,
    isSimpleType = Types.isSimple,
    common = _dereq_(208),
    XSI_TYPE = common.XSI_TYPE,
    serializeAsType = common.serializeAsType,
    hasLowerCaseAlias = common.hasLowerCaseAlias;

function capitalize(str) {
  return str.charAt(0).toUpperCase() + str.slice(1);
}

function aliasToName(aliasNs, pkg) {

  if (!hasLowerCaseAlias(pkg)) {
    return aliasNs.name;
  }

  return aliasNs.prefix + ':' + capitalize(aliasNs.localName);
}

function prefixedToName(nameNs, pkg) {

  var name = nameNs.name,
      localName = nameNs.localName;

  var typePrefix = pkg.xml && pkg.xml.typePrefix;

  if (typePrefix && localName.indexOf(typePrefix) === 0) {
    return nameNs.prefix + ':' + localName.slice(typePrefix.length);
  } else {
    return name;
  }
}

function normalizeXsiTypeName(name, model) {

  var nameNs = parseNameNs(name);
  var pkg = model.getPackage(nameNs.prefix);

  return prefixedToName(nameNs, pkg);
}

function error(message) {
  return new Error(message);
}

/**
 * Get the moddle descriptor for a given instance or type.
 *
 * @param  {ModdleElement|Function} element
 *
 * @return {Object} the moddle descriptor
 */
function getModdleDescriptor(element) {
  return element.$descriptor;
}

/**
 * A parse context.
 *
 * @class
 *
 * @param {Object} options
 * @param {ElementHandler} options.rootHandler the root handler for parsing a document
 * @param {boolean} [options.lax=false] whether or not to ignore invalid elements
 */
function Context(options) {

  /**
   * @property {ElementHandler} rootHandler
   */

  /**
   * @property {Boolean} lax
   */

  assign(this, options);

  this.elementsById = {};
  this.references = [];
  this.warnings = [];

  /**
   * Add an unresolved reference.
   *
   * @param {Object} reference
   */
  this.addReference = function(reference) {
    this.references.push(reference);
  };

  /**
   * Add a processed element.
   *
   * @param {ModdleElement} element
   */
  this.addElement = function(element) {

    if (!element) {
      throw error('expected element');
    }

    var elementsById = this.elementsById;

    var descriptor = getModdleDescriptor(element);

    var idProperty = descriptor.idProperty,
        id;

    if (idProperty) {
      id = element.get(idProperty.name);

      if (id) {

        if (elementsById[id]) {
          throw error('duplicate ID <' + id + '>');
        }

        elementsById[id] = element;
      }
    }
  };

  /**
   * Add an import warning.
   *
   * @param {Object} warning
   * @param {String} warning.message
   * @param {Error} [warning.error]
   */
  this.addWarning = function(warning) {
    this.warnings.push(warning);
  };
}

function BaseHandler() {}

BaseHandler.prototype.handleEnd = function() {};
BaseHandler.prototype.handleText = function() {};
BaseHandler.prototype.handleNode = function() {};


/**
 * A simple pass through handler that does nothing except for
 * ignoring all input it receives.
 *
 * This is used to ignore unknown elements and
 * attributes.
 */
function NoopHandler() { }

NoopHandler.prototype = Object.create(BaseHandler.prototype);

NoopHandler.prototype.handleNode = function() {
  return this;
};

function BodyHandler() {}

BodyHandler.prototype = Object.create(BaseHandler.prototype);

BodyHandler.prototype.handleText = function(text) {
  this.body = (this.body || '') + text;
};

function ReferenceHandler(property, context) {
  this.property = property;
  this.context = context;
}

ReferenceHandler.prototype = Object.create(BodyHandler.prototype);

ReferenceHandler.prototype.handleNode = function(node) {

  if (this.element) {
    throw error('expected no sub nodes');
  } else {
    this.element = this.createReference(node);
  }

  return this;
};

ReferenceHandler.prototype.handleEnd = function() {
  this.element.id = this.body;
};

ReferenceHandler.prototype.createReference = function(node) {
  return {
    property: this.property.ns.name,
    id: ''
  };
};

function ValueHandler(propertyDesc, element) {
  this.element = element;
  this.propertyDesc = propertyDesc;
}

ValueHandler.prototype = Object.create(BodyHandler.prototype);

ValueHandler.prototype.handleEnd = function() {

  var value = this.body || '',
      element = this.element,
      propertyDesc = this.propertyDesc;

  value = coerceType(propertyDesc.type, value);

  if (propertyDesc.isMany) {
    element.get(propertyDesc.name).push(value);
  } else {
    element.set(propertyDesc.name, value);
  }
};


function BaseElementHandler() {}

BaseElementHandler.prototype = Object.create(BodyHandler.prototype);

BaseElementHandler.prototype.handleNode = function(node) {
  var parser = this,
      element = this.element;

  if (!element) {
    element = this.element = this.createElement(node);

    this.context.addElement(element);
  } else {
    parser = this.handleChild(node);
  }

  return parser;
};

/**
 * @class XMLReader.ElementHandler
 *
 */
function ElementHandler(model, typeName, context) {
  this.model = model;
  this.type = model.getType(typeName);
  this.context = context;
}

ElementHandler.prototype = Object.create(BaseElementHandler.prototype);

ElementHandler.prototype.addReference = function(reference) {
  this.context.addReference(reference);
};

ElementHandler.prototype.handleEnd = function() {

  var value = this.body,
      element = this.element,
      descriptor = getModdleDescriptor(element),
      bodyProperty = descriptor.bodyProperty;

  if (bodyProperty && value !== undefined) {
    value = coerceType(bodyProperty.type, value);
    element.set(bodyProperty.name, value);
  }
};

/**
 * Create an instance of the model from the given node.
 *
 * @param  {Element} node the xml node
 */
ElementHandler.prototype.createElement = function(node) {
  var attributes = node.attributes,
      Type = this.type,
      descriptor = getModdleDescriptor(Type),
      context = this.context,
      instance = new Type({});

  forEach(attributes, function(value, name) {

    var prop = descriptor.propertiesByName[name],
        values;

    if (prop && prop.isReference) {

      if (!prop.isMany) {
        context.addReference({
          element: instance,
          property: prop.ns.name,
          id: value
        });
      } else {
        // IDREFS: parse references as whitespace-separated list
        values = value.split(' ');

        forEach(values, function(v) {
          context.addReference({
            element: instance,
            property: prop.ns.name,
            id: v
          });
        });
      }

    } else {
      if (prop) {
        value = coerceType(prop.type, value);
      }

      instance.set(name, value);
    }
  });

  return instance;
};

ElementHandler.prototype.getPropertyForNode = function(node) {

  var name = node.name;
  var nameNs = parseNameNs(name);

  var type = this.type,
      model = this.model,
      descriptor = getModdleDescriptor(type);

  var propertyName = nameNs.name,
      property = descriptor.propertiesByName[propertyName],
      elementTypeName,
      elementType;

  // search for properties by name first

  if (property) {

    if (serializeAsType(property)) {
      elementTypeName = node.attributes[XSI_TYPE];

      // xsi type is optional, if it does not exists the
      // default type is assumed
      if (elementTypeName) {

        // take possible type prefixes from XML
        // into account, i.e.: xsi:type="t{ActualType}"
        elementTypeName = normalizeXsiTypeName(elementTypeName, model);

        elementType = model.getType(elementTypeName);

        return assign({}, property, { effectiveType: getModdleDescriptor(elementType).name });
      }
    }

    // search for properties by name first
    return property;
  }

  var pkg = model.getPackage(nameNs.prefix);

  if (pkg) {
    elementTypeName = aliasToName(nameNs, pkg);
    elementType = model.getType(elementTypeName);

    // search for collection members later
    property = find(descriptor.properties, function(p) {
      return !p.isVirtual && !p.isReference && !p.isAttribute && elementType.hasType(p.type);
    });

    if (property) {
      return assign({}, property, { effectiveType: getModdleDescriptor(elementType).name });
    }
  } else {
    // parse unknown element (maybe extension)
    property = find(descriptor.properties, function(p) {
      return !p.isReference && !p.isAttribute && p.type === 'Element';
    });

    if (property) {
      return property;
    }
  }

  throw error('unrecognized element <' + nameNs.name + '>');
};

ElementHandler.prototype.toString = function() {
  return 'ElementDescriptor[' + getModdleDescriptor(this.type).name + ']';
};

ElementHandler.prototype.valueHandler = function(propertyDesc, element) {
  return new ValueHandler(propertyDesc, element);
};

ElementHandler.prototype.referenceHandler = function(propertyDesc) {
  return new ReferenceHandler(propertyDesc, this.context);
};

ElementHandler.prototype.handler = function(type) {
  if (type === 'Element') {
    return new GenericElementHandler(this.model, type, this.context);
  } else {
    return new ElementHandler(this.model, type, this.context);
  }
};

/**
 * Handle the child element parsing
 *
 * @param  {Element} node the xml node
 */
ElementHandler.prototype.handleChild = function(node) {
  var propertyDesc, type, element, childHandler;

  propertyDesc = this.getPropertyForNode(node);
  element = this.element;

  type = propertyDesc.effectiveType || propertyDesc.type;

  if (isSimpleType(type)) {
    return this.valueHandler(propertyDesc, element);
  }

  if (propertyDesc.isReference) {
    childHandler = this.referenceHandler(propertyDesc).handleNode(node);
  } else {
    childHandler = this.handler(type).handleNode(node);
  }

  var newElement = childHandler.element;

  // child handles may decide to skip elements
  // by not returning anything
  if (newElement !== undefined) {

    if (propertyDesc.isMany) {
      element.get(propertyDesc.name).push(newElement);
    } else {
      element.set(propertyDesc.name, newElement);
    }

    if (propertyDesc.isReference) {
      assign(newElement, {
        element: element
      });

      this.context.addReference(newElement);
    } else {
      // establish child -> parent relationship
      newElement.$parent = element;
    }
  }

  return childHandler;
};

/**
 * An element handler that performs special validation
 * to ensure the node it gets initialized with matches
 * the handlers type (namespace wise).
 *
 * @param {Moddle} model
 * @param {String} typeName
 * @param {Context} context
 */
function RootElementHandler(model, typeName, context) {
  ElementHandler.call(this, model, typeName, context);
}

RootElementHandler.prototype = Object.create(ElementHandler.prototype);

RootElementHandler.prototype.createElement = function(node) {

  var name = node.name,
      nameNs = parseNameNs(name),
      model = this.model,
      type = this.type,
      pkg = model.getPackage(nameNs.prefix),
      typeName = pkg && aliasToName(nameNs, pkg) || name;

  // verify the correct namespace if we parse
  // the first element in the handler tree
  //
  // this ensures we don't mistakenly import wrong namespace elements
  if (!type.hasType(typeName)) {
    throw error('unexpected element <' + node.originalName + '>');
  }

  return ElementHandler.prototype.createElement.call(this, node);
};


function GenericElementHandler(model, typeName, context) {
  this.model = model;
  this.context = context;
}

GenericElementHandler.prototype = Object.create(BaseElementHandler.prototype);

GenericElementHandler.prototype.createElement = function(node) {

  var name = node.name,
      ns = parseNameNs(name),
      prefix = ns.prefix,
      uri = node.ns[prefix + '$uri'],
      attributes = node.attributes;

  return this.model.createAny(name, uri, attributes);
};

GenericElementHandler.prototype.handleChild = function(node) {

  var handler = new GenericElementHandler(this.model, 'Element', this.context).handleNode(node),
      element = this.element;

  var newElement = handler.element,
      children;

  if (newElement !== undefined) {
    children = element.$children = element.$children || [];
    children.push(newElement);

    // establish child -> parent relationship
    newElement.$parent = element;
  }

  return handler;
};

GenericElementHandler.prototype.handleText = function(text) {
  this.body = this.body || '' + text;
};

GenericElementHandler.prototype.handleEnd = function() {
  if (this.body) {
    this.element.$body = this.body;
  }
};

/**
 * A reader for a meta-model
 *
 * @param {Object} options
 * @param {Model} options.model used to read xml files
 * @param {Boolean} options.lax whether to make parse errors warnings
 */
function XMLReader(options) {

  if (options instanceof Moddle) {
    options = {
      model: options
    };
  }

  assign(this, { lax: false }, options);
}


/**
 * Parse the given XML into a moddle document tree.
 *
 * @param {String} xml
 * @param {ElementHandler|Object} options or rootHandler
 * @param  {Function} done
 */
XMLReader.prototype.fromXML = function(xml, options, done) {

  var rootHandler = options.rootHandler;

  if (options instanceof ElementHandler) {
    // root handler passed via (xml, { rootHandler: ElementHandler }, ...)
    rootHandler = options;
    options = {};
  } else {
    if (typeof options === 'string') {
      // rootHandler passed via (xml, 'someString', ...)
      rootHandler = this.handler(options);
      options = {};
    } else if (typeof rootHandler === 'string') {
      // rootHandler passed via (xml, { rootHandler: 'someString' }, ...)
      rootHandler = this.handler(rootHandler);
    }
  }

  var model = this.model,
      lax = this.lax;

  var context = new Context(assign({}, options, { rootHandler: rootHandler })),
      parser = new SaxParser({ proxy: true }),
      stack = new Stack();

  rootHandler.context = context;

  // push root handler
  stack.push(rootHandler);


  /**
   * Handle error.
   *
   * @param  {Error} err
   * @param  {Function} getContext
   * @param  {boolean} lax
   *
   * @return {boolean} true if handled
   */
  function handleError(err, getContext, lax) {

    var ctx = getContext();

    var line = ctx.line,
        column = ctx.column,
        data = ctx.data;

    // we receive the full context data here,
    // for elements trim down the information
    // to the tag name, only
    if (data.charAt(0) === '<' && data.indexOf(' ') !== -1) {
      data = data.slice(0, data.indexOf(' ')) + '>';
    }

    var message =
      'unparsable content ' + (data ? data + ' ' : '') + 'detected\n\t' +
        'line: ' + line + '\n\t' +
        'column: ' + column + '\n\t' +
        'nested error: ' + err.message;

    if (lax) {
      context.addWarning({
        message: message,
        error: err
      });

      console.warn('could not parse node');
      console.warn(err);

      return true;
    } else {
      console.error('could not parse document');
      console.error(err);

      throw error(message);
    }
  }

  function handleWarning(err, getContext) {
    // just like handling errors in <lax=true> mode
    return handleError(err, getContext, true);
  }

  /**
   * Resolve collected references on parse end.
   */
  function resolveReferences() {

    var elementsById = context.elementsById;
    var references = context.references;

    var i, r;

    for (i = 0; (r = references[i]); i++) {
      var element = r.element;
      var reference = elementsById[r.id];
      var property = getModdleDescriptor(element).propertiesByName[r.property];

      if (!reference) {
        context.addWarning({
          message: 'unresolved reference <' + r.id + '>',
          element: r.element,
          property: r.property,
          value: r.id
        });
      }

      if (property.isMany) {
        var collection = element.get(property.name),
            idx = collection.indexOf(r);

        // we replace an existing place holder (idx != -1) or
        // append to the collection instead
        if (idx === -1) {
          idx = collection.length;
        }

        if (!reference) {
          // remove unresolvable reference
          collection.splice(idx, 1);
        } else {
          // add or update reference in collection
          collection[idx] = reference;
        }
      } else {
        element.set(property.name, reference);
      }
    }
  }

  function handleClose() {
    stack.pop().handleEnd();
  }

  function handleOpen(node, getContext) {
    var handler = stack.peek();

    try {
      stack.push(handler.handleNode(node));
    } catch (err) {

      if (handleError(err, getContext, lax)) {
        stack.push(new NoopHandler());
      }
    }
  }

  function handleCData(text) {
    stack.peek().handleText(text);
  }

  function handleText(text) {
    // strip whitespace only nodes, i.e. before
    // <!CDATA[ ... ]> sections and in between tags
    text = text.trim();

    if (!text) {
      return;
    }

    stack.peek().handleText(text);
  }

  var uriMap = model.getPackages().reduce(function(uriMap, p) {
    uriMap[p.uri] = p.prefix;

    return uriMap;
  }, {});

  parser
    .ns(uriMap)
    .on('openTag', function(obj, decodeStr, selfClosing, getContext) {

      // gracefully handle unparsable attributes (attrs=false)
      var attrs = obj.attrs || {};

      var decodedAttrs = Object.keys(attrs).reduce(function(d, key) {
        var value = decodeStr(attrs[key]);

        d[key] = value;

        return d;
      }, {});

      var node = {
        name: obj.name,
        originalName: obj.originalName,
        attributes: decodedAttrs,
        ns: obj.ns
      };

      handleOpen(node, getContext);
    })
    .on('closeTag', handleClose)
    .on('cdata', handleCData)
    .on('text', handleText)
    .on('error', handleError)
    .on('warn', handleWarning);

  // deferred parse XML to make loading really ascnchronous
  // this ensures the execution environment (node or browser)
  // is kept responsive and that certain optimization strategies
  // can kick in
  defer(function() {
    var err;

    try {
      parser.parse(xml);

      resolveReferences();
    } catch (e) {
      err = e;
    }

    var element = rootHandler.element;

    // handle the situation that we could not extract
    // the desired root element from the document
    if (!err && !element) {
      err = error('failed to parse document as <' + rootHandler.type.$descriptor.name + '>');
    }

    done(err, err ? undefined : element, context);
  });
};

XMLReader.prototype.handler = function(name) {
  return new RootElementHandler(this.model, name);
};

module.exports = XMLReader;
module.exports.ElementHandler = ElementHandler;
},{"190":190,"208":208,"211":211,"216":216,"219":219,"224":224,"225":225,"77":77,"78":78,"86":86}],210:[function(_dereq_,module,exports){
'use strict';

var map = _dereq_(80),
    forEach = _dereq_(78),
    isString = _dereq_(187),
    filter = _dereq_(76),
    assign = _dereq_(190);

var Types = _dereq_(219),
    parseNameNs = _dereq_(216).parseName,
    common = _dereq_(208),
    hasLowerCaseAlias = common.hasLowerCaseAlias,
    serializeAsType = common.serializeAsType,
    serializeAsProperty = common.serializeAsProperty;

var XML_PREAMBLE = '<?xml version="1.0" encoding="UTF-8"?>\n',
    ESCAPE_CHARS = /(<|>|'|"|&|\n\r|\n)/g,
    DEFAULT_NS_MAP = common.DEFAULT_NS_MAP,
    XSI_TYPE = common.XSI_TYPE;


function lower(string) {
  return string.charAt(0).toLowerCase() + string.slice(1);
}

function nameToAlias(name, pkg) {
  if (hasLowerCaseAlias(pkg)) {
    return lower(name);
  } else {
    return name;
  }
}

function inherits(ctor, superCtor) {
  ctor.super_ = superCtor;
  ctor.prototype = Object.create(superCtor.prototype, {
    constructor: {
      value: ctor,
      enumerable: false,
      writable: true,
      configurable: true
    }
  });
}

function nsName(ns) {
  if (isString(ns)) {
    return ns;
  } else {
    return (ns.prefix ? ns.prefix + ':' : '') + ns.localName;
  }
}

function getNsAttrs(namespaces) {

  function isUsed(ns) {
    return namespaces.used[ns.uri];
  }

  function toAttr(ns) {
    var name = 'xmlns' + (ns.prefix ? ':' + ns.prefix : '');
    return { name: name, value: ns.uri };
  }

  var allNs = [].concat(namespaces.wellknown, namespaces.custom);

  return map(filter(allNs, isUsed), toAttr);
}

function getElementNs(ns, descriptor) {
  if (descriptor.isGeneric) {
    return assign({ localName: descriptor.ns.localName }, ns);
  } else {
    return assign({ localName: nameToAlias(descriptor.ns.localName, descriptor.$pkg) }, ns);
  }
}

function getPropertyNs(ns, descriptor) {
  return assign({ localName: descriptor.ns.localName }, ns);
}

function getSerializableProperties(element) {
  var descriptor = element.$descriptor;

  return filter(descriptor.properties, function(p) {
    var name = p.name;

    if (p.isVirtual) {
      return false;
    }

    // do not serialize defaults
    if (!element.hasOwnProperty(name)) {
      return false;
    }

    var value = element[name];

    // do not serialize default equals
    if (value === p.default) {
      return false;
    }

    // do not serialize null properties
    if (value === null) {
      return false;
    }

    return p.isMany ? value.length : true;
  });
}

var ESCAPE_MAP = {
  '\n': '10',
  '\n\r': '10',
  '"': '34',
  '\'': '39',
  '<': '60',
  '>': '62',
  '&': '38'
};

/**
 * Escape a string attribute to not contain any bad values (line breaks, '"', ...)
 *
 * @param {String} str the string to escape
 * @return {String} the escaped string
 */
function escapeAttr(str) {

  // ensure we are handling strings here
  str = isString(str) ? str : '' + str;

  return str.replace(ESCAPE_CHARS, function(str) {
    return '&#' + ESCAPE_MAP[str] + ';';
  });
}

function filterAttributes(props) {
  return filter(props, function(p) { return p.isAttr; });
}

function filterContained(props) {
  return filter(props, function(p) { return !p.isAttr; });
}


function ReferenceSerializer(parent, ns) {
  this.ns = ns;
}

ReferenceSerializer.prototype.build = function(element) {
  this.element = element;
  return this;
};

ReferenceSerializer.prototype.serializeTo = function(writer) {
  writer
    .appendIndent()
    .append('<' + nsName(this.ns) + '>' + this.element.id + '</' + nsName(this.ns) + '>')
    .appendNewLine();
};

function BodySerializer() {}

BodySerializer.prototype.serializeValue = BodySerializer.prototype.serializeTo = function(writer) {
  var escape = this.escape;

  if (escape) {
    writer.append('<![CDATA[');
  }

  writer.append(this.value);

  if (escape) {
    writer.append(']]>');
  }
};

BodySerializer.prototype.build = function(prop, value) {
  this.value = value;

  if (prop.type === 'String' && value.search(ESCAPE_CHARS) !== -1) {
    this.escape = true;
  }

  return this;
};

function ValueSerializer(ns) {
  this.ns = ns;
}

inherits(ValueSerializer, BodySerializer);

ValueSerializer.prototype.serializeTo = function(writer) {

  writer
    .appendIndent()
    .append('<' + nsName(this.ns) + '>');

  this.serializeValue(writer);

  writer
    .append('</' + nsName(this.ns) + '>')
    .appendNewLine();
};

function ElementSerializer(parent, ns) {
  this.body = [];
  this.attrs = [];

  this.parent = parent;
  this.ns = ns;
}

ElementSerializer.prototype.build = function(element) {
  this.element = element;

  var otherAttrs = this.parseNsAttributes(element);

  if (!this.ns) {
    this.ns = this.nsTagName(element.$descriptor);
  }

  if (element.$descriptor.isGeneric) {
    this.parseGeneric(element);
  } else {
    var properties = getSerializableProperties(element);

    this.parseAttributes(filterAttributes(properties));
    this.parseContainments(filterContained(properties));

    this.parseGenericAttributes(element, otherAttrs);
  }

  return this;
};

ElementSerializer.prototype.nsTagName = function(descriptor) {
  var effectiveNs = this.logNamespaceUsed(descriptor.ns);
  return getElementNs(effectiveNs, descriptor);
};

ElementSerializer.prototype.nsPropertyTagName = function(descriptor) {
  var effectiveNs = this.logNamespaceUsed(descriptor.ns);
  return getPropertyNs(effectiveNs, descriptor);
};

ElementSerializer.prototype.isLocalNs = function(ns) {
  return ns.uri === this.ns.uri;
};

/**
 * Get the actual ns attribute name for the given element.
 *
 * @param {Object} element
 * @param {Boolean} [inherited=false]
 *
 * @return {Object} nsName
 */
ElementSerializer.prototype.nsAttributeName = function(element) {

  var ns;

  if (isString(element)) {
    ns = parseNameNs(element);
  } else {
    ns = element.ns;
  }

  // return just local name for inherited attributes
  if (element.inherited) {
    return { localName: ns.localName };
  }

  // parse + log effective ns
  var effectiveNs = this.logNamespaceUsed(ns);

  // strip prefix if same namespace like parent
  if (this.isLocalNs(effectiveNs)) {
    return { localName: ns.localName };
  } else {
    return assign({ localName: ns.localName }, effectiveNs);
  }
};

ElementSerializer.prototype.parseGeneric = function(element) {

  var self = this,
      body = this.body,
      attrs = this.attrs;

  forEach(element, function(val, key) {

    if (key === '$body') {
      body.push(new BodySerializer().build({ type: 'String' }, val));
    } else
    if (key === '$children') {
      forEach(val, function(child) {
        body.push(new ElementSerializer(self).build(child));
      });
    } else
    if (key.indexOf('$') !== 0) {
      attrs.push({ name: key, value: escapeAttr(val) });
    }
  });
};

/**
 * Parse namespaces and return a list of left over generic attributes
 *
 * @param  {Object} element
 * @return {Array<Object>}
 */
ElementSerializer.prototype.parseNsAttributes = function(element) {
  var self = this;

  var genericAttrs = element.$attrs;

  var model = element.$model;

  var attributes = [];

  // parse namespace attributes first
  // and log them. push non namespace attributes to a list
  // and process them later
  forEach(genericAttrs, function(value, name) {
    var nameNs = parseNameNs(name);

    var ns;

    // parse xmlns:foo="http://foo.bar"
    if (nameNs.prefix === 'xmlns') {
      ns = { prefix: nameNs.localName, uri: value };
    }

    // parse xmlns="http://foo.bar"
    if (!nameNs.prefix && nameNs.localName === 'xmlns') {
      ns = { uri: value };
    }

    if (ns) {
      if (model.getPackage(value)) {
        // register well known namespace
        self.logNamespace(ns, true);
      } else {
        // log custom namespace directly as used
        self.logNamespaceUsed(ns);
      }
    } else {
      attributes.push({ name: name, value: value });
    }
  });

  return attributes;
};

ElementSerializer.prototype.parseGenericAttributes = function(element, attributes) {

  var self = this;

  forEach(attributes, function(attr) {

    // do not serialize xsi:type attribute
    // it is set manually based on the actual implementation type
    if (attr.name === XSI_TYPE) {
      return;
    }

    try {
      self.addAttribute(self.nsAttributeName(attr.name), attr.value);
    } catch (e) {
      console.warn(
        'missing namespace information for ',
        attr.name, '=', attr.value, 'on', element,
        e);
    }
  });
};

ElementSerializer.prototype.parseContainments = function(properties) {

  var self = this,
      body = this.body,
      element = this.element;

  forEach(properties, function(p) {
    var value = element.get(p.name),
        isReference = p.isReference,
        isMany = p.isMany;

    var ns = self.nsPropertyTagName(p);

    if (!isMany) {
      value = [ value ];
    }

    if (p.isBody) {
      body.push(new BodySerializer().build(p, value[0]));
    } else
    if (Types.isSimple(p.type)) {
      forEach(value, function(v) {
        body.push(new ValueSerializer(ns).build(p, v));
      });
    } else
    if (isReference) {
      forEach(value, function(v) {
        body.push(new ReferenceSerializer(self, ns).build(v));
      });
    } else {
      // allow serialization via type
      // rather than element name
      var asType = serializeAsType(p),
          asProperty = serializeAsProperty(p);

      forEach(value, function(v) {
        var serializer;

        if (asType) {
          serializer = new TypeSerializer(self, ns);
        } else
        if (asProperty) {
          serializer = new ElementSerializer(self, ns);
        } else {
          serializer = new ElementSerializer(self);
        }

        body.push(serializer.build(v));
      });
    }
  });
};

ElementSerializer.prototype.getNamespaces = function() {

  var namespaces = this.namespaces,
      parent = this.parent;

  if (!namespaces) {
    namespaces = this.namespaces = parent ? parent.getNamespaces() : {
      prefixMap: {},
      uriMap: {},
      used: {},
      wellknown: [],
      custom: []
    };
  }

  return namespaces;
};

ElementSerializer.prototype.logNamespace = function(ns, wellknown) {
  var namespaces = this.getNamespaces();

  var nsUri = ns.uri;

  var existing = namespaces.uriMap[nsUri];

  if (!existing) {
    namespaces.uriMap[nsUri] = ns;

    if (wellknown) {
      namespaces.wellknown.push(ns);
    } else {
      namespaces.custom.push(ns);
    }
  }

  namespaces.prefixMap[ns.prefix] = nsUri;

  return ns;
};

ElementSerializer.prototype.logNamespaceUsed = function(ns) {
  var element = this.element,
      model = element.$model,
      namespaces = this.getNamespaces();

  // ns may be
  //
  //   * prefix only
  //   * prefix:uri
  //   * localName only

  var prefix = ns.prefix,
      uri = ns.uri,
      newPrefix, idx,
      wellknownUri;

  // handle anonymous namespaces (elementForm=unqualified), cf. #23
  if (!prefix && !uri) {
    return { localName: ns.localName };
  }

  wellknownUri = DEFAULT_NS_MAP[prefix] || model && (model.getPackage(prefix) || {}).uri;

  uri = uri || wellknownUri || namespaces.prefixMap[prefix];

  if (!uri) {
    throw new Error('no namespace uri given for prefix <' + prefix + '>');
  }

  ns = namespaces.uriMap[uri];

  if (!ns) {
    newPrefix = prefix;
    idx = 1;

    // find a prefix that is not mapped yet
    while (namespaces.prefixMap[newPrefix]) {
      newPrefix = prefix + '_' + idx++;
    }

    ns = this.logNamespace({ prefix: newPrefix, uri: uri }, wellknownUri === uri);
  }

  if (!namespaces.used[ns.uri]) {
    namespaces.used[ns.uri] = ns;
  }

  return ns;
};

ElementSerializer.prototype.parseAttributes = function(properties) {
  var self = this,
      element = this.element;

  forEach(properties, function(p) {

    var value = element.get(p.name);

    if (p.isReference) {

      if (!p.isMany) {
        value = value.id;
      }
      else {
        var values = [];
        forEach(value, function(v) {
          values.push(v.id);
        });
        // IDREFS is a whitespace-separated list of references.
        value = values.join(' ');
      }

    }

    self.addAttribute(self.nsAttributeName(p), value);
  });
};

ElementSerializer.prototype.addAttribute = function(name, value) {
  var attrs = this.attrs;

  if (isString(value)) {
    value = escapeAttr(value);
  }

  attrs.push({ name: name, value: value });
};

ElementSerializer.prototype.serializeAttributes = function(writer) {
  var attrs = this.attrs,
      root = !this.parent;

  if (root) {
    attrs = getNsAttrs(this.namespaces).concat(attrs);
  }

  forEach(attrs, function(a) {
    writer
      .append(' ')
      .append(nsName(a.name)).append('="').append(a.value).append('"');
  });
};

ElementSerializer.prototype.serializeTo = function(writer) {
  var firstBody = this.body[0],
      indent = firstBody && firstBody.constructor !== BodySerializer;

  writer
    .appendIndent()
    .append('<' + nsName(this.ns));

  this.serializeAttributes(writer);

  writer.append(firstBody ? '>' : ' />');

  if (firstBody) {

    if (indent) {
      writer
        .appendNewLine()
        .indent();
    }

    forEach(this.body, function(b) {
      b.serializeTo(writer);
    });

    if (indent) {
      writer
        .unindent()
        .appendIndent();
    }

    writer.append('</' + nsName(this.ns) + '>');
  }

  writer.appendNewLine();
};

/**
 * A serializer for types that handles serialization of data types
 */
function TypeSerializer(parent, ns) {
  ElementSerializer.call(this, parent, ns);
}

inherits(TypeSerializer, ElementSerializer);

TypeSerializer.prototype.build = function(element) {
  var descriptor = element.$descriptor;

  this.element = element;

  this.typeNs = this.nsTagName(descriptor);

  // add xsi:type attribute to represent the elements
  // actual type

  var typeNs = this.typeNs,
      pkg = element.$model.getPackage(typeNs.uri),
      typePrefix = (pkg.xml && pkg.xml.typePrefix) || '';

  this.addAttribute(this.nsAttributeName(XSI_TYPE),
    (typeNs.prefix ? typeNs.prefix + ':' : '') +
    typePrefix + descriptor.ns.localName);

  // do the usual stuff
  return ElementSerializer.prototype.build.call(this, element);
};

TypeSerializer.prototype.isLocalNs = function(ns) {
  return ns.uri === this.typeNs.uri;
};

function SavingWriter() {
  this.value = '';

  this.write = function(str) {
    this.value += str;
  };
}

function FormatingWriter(out, format) {

  var indent = [''];

  this.append = function(str) {
    out.write(str);

    return this;
  };

  this.appendNewLine = function() {
    if (format) {
      out.write('\n');
    }

    return this;
  };

  this.appendIndent = function() {
    if (format) {
      out.write(indent.join('  '));
    }

    return this;
  };

  this.indent = function() {
    indent.push('');
    return this;
  };

  this.unindent = function() {
    indent.pop();
    return this;
  };
}

/**
 * A writer for meta-model backed document trees
 *
 * @param {Object} options output options to pass into the writer
 */
function XMLWriter(options) {

  options = assign({ format: false, preamble: true }, options || {});

  function toXML(tree, writer) {
    var internalWriter = writer || new SavingWriter();
    var formatingWriter = new FormatingWriter(internalWriter, options.format);

    if (options.preamble) {
      formatingWriter.append(XML_PREAMBLE);
    }

    new ElementSerializer().build(tree).serializeTo(formatingWriter);

    if (!writer) {
      return internalWriter.value;
    }
  }

  return {
    toXML: toXML
  };
}

module.exports = XMLWriter;

},{"187":187,"190":190,"208":208,"216":216,"219":219,"76":76,"78":78,"80":80}],211:[function(_dereq_,module,exports){
module.exports = _dereq_(215);
},{"215":215}],212:[function(_dereq_,module,exports){
'use strict';

function Base() { }

Base.prototype.get = function(name) {
  return this.$model.properties.get(this, name);
};

Base.prototype.set = function(name, value) {
  this.$model.properties.set(this, name, value);
};


module.exports = Base;
},{}],213:[function(_dereq_,module,exports){
'use strict';

var pick = _dereq_(196),
    assign = _dereq_(190),
    forEach = _dereq_(78);

var parseNameNs = _dereq_(216).parseName;


function DescriptorBuilder(nameNs) {
  this.ns = nameNs;
  this.name = nameNs.name;
  this.allTypes = [];
  this.allTypesByName = {};
  this.properties = [];
  this.propertiesByName = {};
}

module.exports = DescriptorBuilder;


DescriptorBuilder.prototype.build = function() {
  return pick(this, [
    'ns',
    'name',
    'allTypes',
    'allTypesByName',
    'properties',
    'propertiesByName',
    'bodyProperty',
    'idProperty'
  ]);
};

/**
 * Add property at given index.
 *
 * @param {Object} p
 * @param {Number} [idx]
 * @param {Boolean} [validate=true]
 */
DescriptorBuilder.prototype.addProperty = function(p, idx, validate) {

  if (typeof idx === 'boolean') {
    validate = idx;
    idx = undefined;
  }

  this.addNamedProperty(p, validate !== false);

  var properties = this.properties;

  if (idx !== undefined) {
    properties.splice(idx, 0, p);
  } else {
    properties.push(p);
  }
};


DescriptorBuilder.prototype.replaceProperty = function(oldProperty, newProperty, replace) {
  var oldNameNs = oldProperty.ns;

  var props = this.properties,
      propertiesByName = this.propertiesByName,
      rename = oldProperty.name !== newProperty.name;

  if (oldProperty.isId) {
    if (!newProperty.isId) {
      throw new Error(
        'property <' + newProperty.ns.name + '> must be id property ' +
        'to refine <' + oldProperty.ns.name + '>');
    }

    this.setIdProperty(newProperty, false);
  }

  if (oldProperty.isBody) {

    if (!newProperty.isBody) {
      throw new Error(
        'property <' + newProperty.ns.name + '> must be body property ' +
        'to refine <' + oldProperty.ns.name + '>');
    }

    // TODO: Check compatibility
    this.setBodyProperty(newProperty, false);
  }

  // validate existence and get location of old property
  var idx = props.indexOf(oldProperty);
  if (idx === -1) {
    throw new Error('property <' + oldNameNs.name + '> not found in property list');
  }

  // remove old property
  props.splice(idx, 1);

  // replacing the named property is intentional
  //
  //  * validate only if this is a "rename" operation
  //  * add at specific index unless we "replace"
  //
  this.addProperty(newProperty, replace ? undefined : idx, rename);

  // make new property available under old name
  propertiesByName[oldNameNs.name] = propertiesByName[oldNameNs.localName] = newProperty;
};


DescriptorBuilder.prototype.redefineProperty = function(p, targetPropertyName, replace) {

  var nsPrefix = p.ns.prefix;
  var parts = targetPropertyName.split('#');

  var name = parseNameNs(parts[0], nsPrefix);
  var attrName = parseNameNs(parts[1], name.prefix).name;

  var redefinedProperty = this.propertiesByName[attrName];
  if (!redefinedProperty) {
    throw new Error('refined property <' + attrName + '> not found');
  } else {
    this.replaceProperty(redefinedProperty, p, replace);
  }

  delete p.redefines;
};

DescriptorBuilder.prototype.addNamedProperty = function(p, validate) {
  var ns = p.ns,
      propsByName = this.propertiesByName;

  if (validate) {
    this.assertNotDefined(p, ns.name);
    this.assertNotDefined(p, ns.localName);
  }

  propsByName[ns.name] = propsByName[ns.localName] = p;
};

DescriptorBuilder.prototype.removeNamedProperty = function(p) {
  var ns = p.ns,
      propsByName = this.propertiesByName;

  delete propsByName[ns.name];
  delete propsByName[ns.localName];
};

DescriptorBuilder.prototype.setBodyProperty = function(p, validate) {

  if (validate && this.bodyProperty) {
    throw new Error(
      'body property defined multiple times ' +
      '(<' + this.bodyProperty.ns.name + '>, <' + p.ns.name + '>)');
  }

  this.bodyProperty = p;
};

DescriptorBuilder.prototype.setIdProperty = function(p, validate) {

  if (validate && this.idProperty) {
    throw new Error(
      'id property defined multiple times ' +
      '(<' + this.idProperty.ns.name + '>, <' + p.ns.name + '>)');
  }

  this.idProperty = p;
};

DescriptorBuilder.prototype.assertNotDefined = function(p, name) {
  var propertyName = p.name,
      definedProperty = this.propertiesByName[propertyName];

  if (definedProperty) {
    throw new Error(
      'property <' + propertyName + '> already defined; ' +
      'override of <' + definedProperty.definedBy.ns.name + '#' + definedProperty.ns.name + '> by ' +
      '<' + p.definedBy.ns.name + '#' + p.ns.name + '> not allowed without redefines');
  }
};

DescriptorBuilder.prototype.hasProperty = function(name) {
  return this.propertiesByName[name];
};

DescriptorBuilder.prototype.addTrait = function(t, inherited) {

  var typesByName = this.allTypesByName,
      types = this.allTypes;

  var typeName = t.name;

  if (typeName in typesByName) {
    return;
  }

  forEach(t.properties, function(p) {

    // clone property to allow extensions
    p = assign({}, p, {
      name: p.ns.localName,
      inherited: inherited
    });

    Object.defineProperty(p, 'definedBy', {
      value: t
    });

    var replaces = p.replaces,
        redefines = p.redefines;

    // add replace/redefine support
    if (replaces || redefines) {
      this.redefineProperty(p, replaces || redefines, replaces);
    } else {
      if (p.isBody) {
        this.setBodyProperty(p);
      }
      if (p.isId) {
        this.setIdProperty(p);
      }
      this.addProperty(p);
    }
  }, this);

  types.push(t);
  typesByName[typeName] = t;
};

},{"190":190,"196":196,"216":216,"78":78}],214:[function(_dereq_,module,exports){
'use strict';

var forEach = _dereq_(78);

var Base = _dereq_(212);


function Factory(model, properties) {
  this.model = model;
  this.properties = properties;
}

module.exports = Factory;


Factory.prototype.createType = function(descriptor) {

  var model = this.model;

  var props = this.properties,
      prototype = Object.create(Base.prototype);

  // initialize default values
  forEach(descriptor.properties, function(p) {
    if (!p.isMany && p.default !== undefined) {
      prototype[p.name] = p.default;
    }
  });

  props.defineModel(prototype, model);
  props.defineDescriptor(prototype, descriptor);

  var name = descriptor.ns.name;

  /**
   * The new type constructor
   */
  function ModdleElement(attrs) {
    props.define(this, '$type', { value: name, enumerable: true });
    props.define(this, '$attrs', { value: {} });
    props.define(this, '$parent', { writable: true });

    forEach(attrs, function(val, key) {
      this.set(key, val);
    }, this);
  }

  ModdleElement.prototype = prototype;

  ModdleElement.hasType = prototype.$instanceOf = this.model.hasType;

  // static links
  props.defineModel(ModdleElement, model);
  props.defineDescriptor(ModdleElement, descriptor);

  return ModdleElement;
};
},{"212":212,"78":78}],215:[function(_dereq_,module,exports){
'use strict';

var isString = _dereq_(187),
    isObject = _dereq_(185),
    forEach = _dereq_(78);


var Factory = _dereq_(214),
    Registry = _dereq_(218),
    Properties = _dereq_(217);

var parseNameNs = _dereq_(216).parseName;


//// Moddle implementation /////////////////////////////////////////////////

/**
 * @class Moddle
 *
 * A model that can be used to create elements of a specific type.
 *
 * @example
 *
 * var Moddle = require('moddle');
 *
 * var pkg = {
 *   name: 'mypackage',
 *   prefix: 'my',
 *   types: [
 *     { name: 'Root' }
 *   ]
 * };
 *
 * var moddle = new Moddle([pkg]);
 *
 * @param {Array<Package>} packages the packages to contain
 */
function Moddle(packages) {

  this.properties = new Properties(this);

  this.factory = new Factory(this, this.properties);
  this.registry = new Registry(packages, this.properties);

  this.typeCache = {};
}

module.exports = Moddle;


/**
 * Create an instance of the specified type.
 *
 * @method Moddle#create
 *
 * @example
 *
 * var foo = moddle.create('my:Foo');
 * var bar = moddle.create('my:Bar', { id: 'BAR_1' });
 *
 * @param  {String|Object} descriptor the type descriptor or name know to the model
 * @param  {Object} attrs   a number of attributes to initialize the model instance with
 * @return {Object}         model instance
 */
Moddle.prototype.create = function(descriptor, attrs) {
  var Type = this.getType(descriptor);

  if (!Type) {
    throw new Error('unknown type <' + descriptor + '>');
  }

  return new Type(attrs);
};


/**
 * Returns the type representing a given descriptor
 *
 * @method Moddle#getType
 *
 * @example
 *
 * var Foo = moddle.getType('my:Foo');
 * var foo = new Foo({ 'id' : 'FOO_1' });
 *
 * @param  {String|Object} descriptor the type descriptor or name know to the model
 * @return {Object}         the type representing the descriptor
 */
Moddle.prototype.getType = function(descriptor) {

  var cache = this.typeCache;

  var name = isString(descriptor) ? descriptor : descriptor.ns.name;

  var type = cache[name];

  if (!type) {
    descriptor = this.registry.getEffectiveDescriptor(name);
    type = cache[name] = this.factory.createType(descriptor);
  }

  return type;
};


/**
 * Creates an any-element type to be used within model instances.
 *
 * This can be used to create custom elements that lie outside the meta-model.
 * The created element contains all the meta-data required to serialize it
 * as part of meta-model elements.
 *
 * @method Moddle#createAny
 *
 * @example
 *
 * var foo = moddle.createAny('vendor:Foo', 'http://vendor', {
 *   value: 'bar'
 * });
 *
 * var container = moddle.create('my:Container', 'http://my', {
 *   any: [ foo ]
 * });
 *
 * // go ahead and serialize the stuff
 *
 *
 * @param  {String} name  the name of the element
 * @param  {String} nsUri the namespace uri of the element
 * @param  {Object} [properties] a map of properties to initialize the instance with
 * @return {Object} the any type instance
 */
Moddle.prototype.createAny = function(name, nsUri, properties) {

  var nameNs = parseNameNs(name);

  var element = {
    $type: name,
    $instanceOf: function(type) {
      return type === this.$type;
    }
  };

  var descriptor = {
    name: name,
    isGeneric: true,
    ns: {
      prefix: nameNs.prefix,
      localName: nameNs.localName,
      uri: nsUri
    }
  };

  this.properties.defineDescriptor(element, descriptor);
  this.properties.defineModel(element, this);
  this.properties.define(element, '$parent', { enumerable: false, writable: true });

  forEach(properties, function(a, key) {
    if (isObject(a) && a.value !== undefined) {
      element[a.name] = a.value;
    } else {
      element[key] = a;
    }
  });

  return element;
};

/**
 * Returns a registered package by uri or prefix
 *
 * @return {Object} the package
 */
Moddle.prototype.getPackage = function(uriOrPrefix) {
  return this.registry.getPackage(uriOrPrefix);
};

/**
 * Returns a snapshot of all known packages
 *
 * @return {Object} the package
 */
Moddle.prototype.getPackages = function() {
  return this.registry.getPackages();
};

/**
 * Returns the descriptor for an element
 */
Moddle.prototype.getElementDescriptor = function(element) {
  return element.$descriptor;
};

/**
 * Returns true if the given descriptor or instance
 * represents the given type.
 *
 * May be applied to this, if element is omitted.
 */
Moddle.prototype.hasType = function(element, type) {
  if (type === undefined) {
    type = element;
    element = this;
  }

  var descriptor = element.$model.getElementDescriptor(element);

  return (type in descriptor.allTypesByName);
};

/**
 * Returns the descriptor of an elements named property
 */
Moddle.prototype.getPropertyDescriptor = function(element, property) {
  return this.getElementDescriptor(element).propertiesByName[property];
};

/**
 * Returns a mapped type's descriptor
 */
Moddle.prototype.getTypeDescriptor = function(type) {
  return this.registry.typeMap[type];
};

},{"185":185,"187":187,"214":214,"216":216,"217":217,"218":218,"78":78}],216:[function(_dereq_,module,exports){
'use strict';

/**
 * Parses a namespaced attribute name of the form (ns:)localName to an object,
 * given a default prefix to assume in case no explicit namespace is given.
 *
 * @param {String} name
 * @param {String} [defaultPrefix] the default prefix to take, if none is present.
 *
 * @return {Object} the parsed name
 */
module.exports.parseName = function(name, defaultPrefix) {
  var parts = name.split(/:/),
      localName, prefix;

  // no prefix (i.e. only local name)
  if (parts.length === 1) {
    localName = name;
    prefix = defaultPrefix;
  } else
  // prefix + local name
  if (parts.length === 2) {
    localName = parts[1];
    prefix = parts[0];
  } else {
    throw new Error('expected <prefix:localName> or <localName>, got ' + name);
  }

  name = (prefix ? prefix + ':' : '') + localName;

  return {
    name: name,
    prefix: prefix,
    localName: localName
  };
};
},{}],217:[function(_dereq_,module,exports){
'use strict';


/**
 * A utility that gets and sets properties of model elements.
 *
 * @param {Model} model
 */
function Properties(model) {
  this.model = model;
}

module.exports = Properties;


/**
 * Sets a named property on the target element.
 * If the value is undefined, the property gets deleted.
 *
 * @param {Object} target
 * @param {String} name
 * @param {Object} value
 */
Properties.prototype.set = function(target, name, value) {

  var property = this.model.getPropertyDescriptor(target, name);

  var propertyName = property && property.name;

  if (isUndefined(value)) {
    // unset the property, if the specified value is undefined;
    // delete from $attrs (for extensions) or the target itself
    if (property) {
      delete target[propertyName];
    } else {
      delete target.$attrs[name];
    }
  } else {
    // set the property, defining well defined properties on the fly
    // or simply updating them in target.$attrs (for extensions)
    if (property) {
      if (propertyName in target) {
        target[propertyName] = value;
      } else {
        defineProperty(target, property, value);
      }
    } else {
      target.$attrs[name] = value;
    }
  }
};

/**
 * Returns the named property of the given element
 *
 * @param  {Object} target
 * @param  {String} name
 *
 * @return {Object}
 */
Properties.prototype.get = function(target, name) {

  var property = this.model.getPropertyDescriptor(target, name);

  if (!property) {
    return target.$attrs[name];
  }

  var propertyName = property.name;

  // check if access to collection property and lazily initialize it
  if (!target[propertyName] && property.isMany) {
    defineProperty(target, property, []);
  }

  return target[propertyName];
};


/**
 * Define a property on the target element
 *
 * @param  {Object} target
 * @param  {String} name
 * @param  {Object} options
 */
Properties.prototype.define = function(target, name, options) {
  Object.defineProperty(target, name, options);
};


/**
 * Define the descriptor for an element
 */
Properties.prototype.defineDescriptor = function(target, descriptor) {
  this.define(target, '$descriptor', { value: descriptor });
};

/**
 * Define the model for an element
 */
Properties.prototype.defineModel = function(target, model) {
  this.define(target, '$model', { value: model });
};


function isUndefined(val) {
  return typeof val === 'undefined';
}

function defineProperty(target, property, value) {
  Object.defineProperty(target, property.name, {
    enumerable: !property.isReference,
    writable: true,
    value: value,
    configurable: true
  });
}
},{}],218:[function(_dereq_,module,exports){
'use strict';

var assign = _dereq_(190),
    forEach = _dereq_(78);

var Types = _dereq_(219),
    DescriptorBuilder = _dereq_(213);

var parseNameNs = _dereq_(216).parseName,
    isBuiltInType = Types.isBuiltIn;


function Registry(packages, properties) {
  this.packageMap = {};
  this.typeMap = {};

  this.packages = [];

  this.properties = properties;

  forEach(packages, this.registerPackage, this);
}

module.exports = Registry;


Registry.prototype.getPackage = function(uriOrPrefix) {
  return this.packageMap[uriOrPrefix];
};

Registry.prototype.getPackages = function() {
  return this.packages;
};


Registry.prototype.registerPackage = function(pkg) {

  // copy package
  pkg = assign({}, pkg);

  var pkgMap = this.packageMap;

  ensureAvailable(pkgMap, pkg, 'prefix');
  ensureAvailable(pkgMap, pkg, 'uri');

  // register types
  forEach(pkg.types, function(descriptor) {
    this.registerType(descriptor, pkg);
  }, this);

  pkgMap[pkg.uri] = pkgMap[pkg.prefix] = pkg;
  this.packages.push(pkg);
};


/**
 * Register a type from a specific package with us
 */
Registry.prototype.registerType = function(type, pkg) {

  type = assign({}, type, {
    superClass: (type.superClass || []).slice(),
    extends: (type.extends || []).slice(),
    properties: (type.properties || []).slice(),
    meta: assign(({}, type.meta || {}))
  });

  var ns = parseNameNs(type.name, pkg.prefix),
      name = ns.name,
      propertiesByName = {};

  // parse properties
  forEach(type.properties, function(p) {

    // namespace property names
    var propertyNs = parseNameNs(p.name, ns.prefix),
        propertyName = propertyNs.name;

    // namespace property types
    if (!isBuiltInType(p.type)) {
      p.type = parseNameNs(p.type, propertyNs.prefix).name;
    }

    assign(p, {
      ns: propertyNs,
      name: propertyName
    });

    propertiesByName[propertyName] = p;
  });

  // update ns + name
  assign(type, {
    ns: ns,
    name: name,
    propertiesByName: propertiesByName
  });

  forEach(type.extends, function(extendsName) {
    var extended = this.typeMap[extendsName];

    extended.traits = extended.traits || [];
    extended.traits.push(name);
  }, this);

  // link to package
  this.definePackage(type, pkg);

  // register
  this.typeMap[name] = type;
};


/**
 * Traverse the type hierarchy from bottom to top,
 * calling iterator with (type, inherited) for all elements in
 * the inheritance chain.
 *
 * @param {Object} nsName
 * @param {Function} iterator
 * @param {Boolean} [trait=false]
 */
Registry.prototype.mapTypes = function(nsName, iterator, trait) {

  var type = isBuiltInType(nsName.name) ? { name: nsName.name } : this.typeMap[nsName.name];

  var self = this;

  /**
   * Traverse the selected trait.
   *
   * @param {String} cls
   */
  function traverseTrait(cls) {
    return traverseSuper(cls, true);
  }

  /**
   * Traverse the selected super type or trait
   *
   * @param {String} cls
   * @param {Boolean} [trait=false]
   */
  function traverseSuper(cls, trait) {
    var parentNs = parseNameNs(cls, isBuiltInType(cls) ? '' : nsName.prefix);
    self.mapTypes(parentNs, iterator, trait);
  }

  if (!type) {
    throw new Error('unknown type <' + nsName.name + '>');
  }

  forEach(type.superClass, trait ? traverseTrait : traverseSuper);

  // call iterator with (type, inherited=!trait)
  iterator(type, !trait);

  forEach(type.traits, traverseTrait);
};


/**
 * Returns the effective descriptor for a type.
 *
 * @param  {String} type the namespaced name (ns:localName) of the type
 *
 * @return {Descriptor} the resulting effective descriptor
 */
Registry.prototype.getEffectiveDescriptor = function(name) {

  var nsName = parseNameNs(name);

  var builder = new DescriptorBuilder(nsName);

  this.mapTypes(nsName, function(type, inherited) {
    builder.addTrait(type, inherited);
  });

  var descriptor = builder.build();

  // define package link
  this.definePackage(descriptor, descriptor.allTypes[descriptor.allTypes.length - 1].$pkg);

  return descriptor;
};


Registry.prototype.definePackage = function(target, pkg) {
  this.properties.define(target, '$pkg', { value: pkg });
};



///////// helpers ////////////////////////////

function ensureAvailable(packageMap, pkg, identifierKey) {

  var value = pkg[identifierKey];

  if (value in packageMap) {
    throw new Error('package with ' + identifierKey + ' <' + value + '> already defined');
  }
}

},{"190":190,"213":213,"216":216,"219":219,"78":78}],219:[function(_dereq_,module,exports){
'use strict';

/**
 * Built-in moddle types
 */
var BUILTINS = {
  String: true,
  Boolean: true,
  Integer: true,
  Real: true,
  Element: true
};

/**
 * Converters for built in types from string representations
 */
var TYPE_CONVERTERS = {
  String: function(s) { return s; },
  Boolean: function(s) { return s === 'true'; },
  Integer: function(s) { return parseInt(s, 10); },
  Real: function(s) { return parseFloat(s, 10); }
};

/**
 * Convert a type to its real representation
 */
module.exports.coerceType = function(type, value) {

  var converter = TYPE_CONVERTERS[type];

  if (converter) {
    return converter(value);
  } else {
    return value;
  }
};

/**
 * Return whether the given type is built-in
 */
module.exports.isBuiltIn = function(type) {
  return !!BUILTINS[type];
};

/**
 * Return whether the given type is simple
 */
module.exports.isSimple = function(type) {
  return !!TYPE_CONVERTERS[type];
};
},{}],220:[function(_dereq_,module,exports){
module.exports = _dereq_(222);

module.exports.Collection = _dereq_(221);
},{"221":221,"222":222}],221:[function(_dereq_,module,exports){
'use strict';

/**
 * An empty collection stub. Use {@link RefsCollection.extend} to extend a
 * collection with ref semantics.
 *
 * @class RefsCollection
 */

/**
 * Extends a collection with {@link Refs} aware methods
 *
 * @memberof RefsCollection
 * @static
 *
 * @param  {Array<Object>} collection
 * @param  {Refs} refs instance
 * @param  {Object} property represented by the collection
 * @param  {Object} target object the collection is attached to
 *
 * @return {RefsCollection<Object>} the extended array
 */
function extend(collection, refs, property, target) {

  var inverseProperty = property.inverse;

  /**
   * Removes the given element from the array and returns it.
   *
   * @method RefsCollection#remove
   *
   * @param {Object} element the element to remove
   */
  Object.defineProperty(collection, 'remove', {
    value: function(element) {
      var idx = this.indexOf(element);
      if (idx !== -1) {
        this.splice(idx, 1);

        // unset inverse
        refs.unset(element, inverseProperty, target);
      }

      return element;
    }
  });

  /**
   * Returns true if the collection contains the given element
   *
   * @method RefsCollection#contains
   *
   * @param {Object} element the element to check for
   */
  Object.defineProperty(collection, 'contains', {
    value: function(element) {
      return this.indexOf(element) !== -1;
    }
  });

  /**
   * Adds an element to the array, unless it exists already (set semantics).
   *
   * @method RefsCollection#add
   *
   * @param {Object} element the element to add
   */
  Object.defineProperty(collection, 'add', {
    value: function(element) {

      if (!this.contains(element)) {
        this.push(element);

        // set inverse
        refs.set(element, inverseProperty, target);
      }
    }
  });

  // a simple marker, identifying this element
  // as being a refs collection
  Object.defineProperty(collection, '__refs_collection', {
    value: true
  });

  return collection;
}


function isExtended(collection) {
  return collection.__refs_collection === true;
}

module.exports.extend = extend;

module.exports.isExtended = isExtended;
},{}],222:[function(_dereq_,module,exports){
'use strict';

var Collection = _dereq_(221);

function hasOwnProperty(e, property) {
  return Object.prototype.hasOwnProperty.call(e, property.name || property);
}

function defineCollectionProperty(ref, property, target) {
  Object.defineProperty(target, property.name, {
    enumerable: property.enumerable,
    value: Collection.extend(target[property.name] || [], ref, property, target)
  });
}


function defineProperty(ref, property, target) {

  var inverseProperty = property.inverse;

  var _value = target[property.name];

  Object.defineProperty(target, property.name, {
    enumerable: property.enumerable,

    get: function() {
      return _value;
    },

    set: function(value) {

      // return if we already performed all changes
      if (value === _value) {
        return;
      }

      var old = _value;

      // temporary set null
      _value = null;

      if (old) {
        ref.unset(old, inverseProperty, target);
      }

      // set new value
      _value = value;

      // set inverse value
      ref.set(_value, inverseProperty, target);
    }
  });

}

/**
 * Creates a new references object defining two inversly related
 * attribute descriptors a and b.
 *
 * <p>
 *   When bound to an object using {@link Refs#bind} the references
 *   get activated and ensure that add and remove operations are applied
 *   reversely, too.
 * </p>
 *
 * <p>
 *   For attributes represented as collections {@link Refs} provides the
 *   {@link RefsCollection#add}, {@link RefsCollection#remove} and {@link RefsCollection#contains} extensions
 *   that must be used to properly hook into the inverse change mechanism.
 * </p>
 *
 * @class Refs
 *
 * @classdesc A bi-directional reference between two attributes.
 *
 * @param {Refs.AttributeDescriptor} a property descriptor
 * @param {Refs.AttributeDescriptor} b property descriptor
 *
 * @example
 *
 * var refs = Refs({ name: 'wheels', collection: true, enumerable: true }, { name: 'car' });
 *
 * var car = { name: 'toyota' };
 * var wheels = [{ pos: 'front-left' }, { pos: 'front-right' }];
 *
 * refs.bind(car, 'wheels');
 *
 * car.wheels // []
 * car.wheels.add(wheels[0]);
 * car.wheels.add(wheels[1]);
 *
 * car.wheels // [{ pos: 'front-left' }, { pos: 'front-right' }]
 *
 * wheels[0].car // { name: 'toyota' };
 * car.wheels.remove(wheels[0]);
 *
 * wheels[0].car // undefined
 */
function Refs(a, b) {

  if (!(this instanceof Refs)) {
    return new Refs(a, b);
  }

  // link
  a.inverse = b;
  b.inverse = a;

  this.props = {};
  this.props[a.name] = a;
  this.props[b.name] = b;
}

/**
 * Binds one side of a bi-directional reference to a
 * target object.
 *
 * @memberOf Refs
 *
 * @param  {Object} target
 * @param  {String} property
 */
Refs.prototype.bind = function(target, property) {
  if (typeof property === 'string') {
    if (!this.props[property]) {
      throw new Error('no property <' + property + '> in ref');
    }
    property = this.props[property];
  }

  if (property.collection) {
    defineCollectionProperty(this, property, target);
  } else {
    defineProperty(this, property, target);
  }
};

Refs.prototype.ensureRefsCollection = function(target, property) {

  var collection = target[property.name];

  if (!Collection.isExtended(collection)) {
    defineCollectionProperty(this, property, target);
  }

  return collection;
};

Refs.prototype.ensureBound = function(target, property) {
  if (!hasOwnProperty(target, property)) {
    this.bind(target, property);
  }
};

Refs.prototype.unset = function(target, property, value) {

  if (target) {
    this.ensureBound(target, property);

    if (property.collection) {
      this.ensureRefsCollection(target, property).remove(value);
    } else {
      target[property.name] = undefined;
    }
  }
};

Refs.prototype.set = function(target, property, value) {

  if (target) {
    this.ensureBound(target, property);

    if (property.collection) {
      this.ensureRefsCollection(target, property).add(value);
    } else {
      target[property.name] = value;
    }
  }
};

module.exports = Refs;


/**
 * An attribute descriptor to be used specify an attribute in a {@link Refs} instance
 *
 * @typedef {Object} Refs.AttributeDescriptor
 * @property {String} name
 * @property {boolean} [collection=false]
 * @property {boolean} [enumerable=false]
 */
},{"221":221}],223:[function(_dereq_,module,exports){
'use strict';

module['exports'] = decodeEntities;


var fromCharCode = String.fromCharCode;

var hasOwnProperty = Object.prototype.hasOwnProperty;

var ENTITY_PATTERN = /&#(\d+);|&#x([0-9a-f]+);|&(\w+);/ig;

var ENTITY_MAPPING = {
  'amp': '&',
  'apos': '\'',
  'gt': '>',
  'lt': '<',
  'quot': '"'
};

// map UPPERCASE variants of supported special chars
Object.keys(ENTITY_MAPPING).forEach(function(k) {
  ENTITY_MAPPING[k.toUpperCase()] = ENTITY_MAPPING[k];
});


function replaceEntities(_, d, x, z) {

  // reserved names, i.e. &nbsp;
  if (z) {
    if (hasOwnProperty.call(ENTITY_MAPPING, z)) {
      return ENTITY_MAPPING[z];
    } else {
      // fall back to original value
      return '&' + z + ';';
    }
  }

  // decimal encoded char
  if (d) {
    return fromCharCode(d);
  }

  // hex encoded char
  return fromCharCode(parseInt(x, 16));
}


/**
 * A basic entity decoder that can decode a minimal
 * sub-set of reserved names (&amp;) as well as
 * hex (&#xaaf;) and decimal (&#1231;) encoded characters.
 *
 * @param {string} str
 *
 * @return {string} decoded string
 */
function decodeEntities(s) {
  if (s.length > 3 && s.indexOf('&') !== -1) {
    return s.replace(ENTITY_PATTERN, replaceEntities);
  }

  return s;
}
},{}],224:[function(_dereq_,module,exports){
'use strict';

module['exports'] = Saxen;

var decodeEntities = _dereq_(223);

var XSI_URI = 'http://www.w3.org/2001/XMLSchema-instance';
var XSI_PREFIX = 'xsi';
var XSI_TYPE = 'xsi:type';

function error(msg) {
  return new Error(msg);
}

function missingNamespaceForPrefix(prefix) {
  return 'missing namespace for prefix <' + prefix + '>';
}

function getter(getFn) {
  return {
    'get': getFn,
    'enumerable': true
  };
}

function cloneNsMatrix(nsMatrix) {
  var clone = {}, key;
  for (key in nsMatrix) {
    clone[key] = nsMatrix[key];
  }
  return clone;
}

function uriPrefix(prefix) {
  return prefix + '$uri';
}

function buildNsMatrix(nsUriToPrefix) {
  var nsMatrix = {},
      uri,
      prefix;

  for (uri in nsUriToPrefix) {
    prefix = nsUriToPrefix[uri];
    nsMatrix[prefix] = prefix;
    nsMatrix[uriPrefix(prefix)] = uri;
  }

  return nsMatrix;
}

function noopGetContext() {
  return { 'line': 0, 'column': 0 };
}

function throwFunc(err) {
  throw err;
}

/**
 * Creates a new parser with the given options.
 *
 * @constructor
 *
 * @param  {!Object<string, ?>=} options
 */
function Saxen(options) {

  if (!this) {
    return new Saxen(options);
  }

  var proxy = options && options['proxy'];

  var onText,
      onOpenTag,
      onCloseTag,
      onCDATA,
      onError = throwFunc,
      onWarning,
      onComment,
      onQuestion,
      onAttention;

  var getContext = noopGetContext;

  /**
   * Do we need to parse the current elements attributes for namespaces?
   *
   * @type {boolean}
   */
  var maybeNS = false;

  /**
   * Do we process namespaces at all?
   *
   * @type {boolean}
   */
  var isNamespace = false;

  /**
   * The caught error returned on parse end
   *
   * @type {Error}
   */
  var returnError = null;

  /**
   * Should we stop parsing?
   *
   * @type {boolean}
   */
  var parseStop = false;

  /**
   * A map of { uri: prefix } used by the parser.
   *
   * This map will ensure we can normalize prefixes during processing;
   * for each uri, only one prefix will be exposed to the handlers.
   *
   * @type {!Object<string, string>}}
   */
  var nsUriToPrefix;

  /**
   * Handle parse error.
   *
   * @param  {string|Error} err
   */
  function handleError(err) {
    if (!(err instanceof Error)) {
      err = error(err);
    }

    returnError = err;

    onError(err, getContext);
  }

  /**
   * Handle parse error.
   *
   * @param  {string|Error} err
   */
  function handleWarning(err) {

    if (!onWarning) {
      return;
    }

    if (!(err instanceof Error)) {
      err = error(err);
    }

    onWarning(err, getContext);
  }

  /**
   * Register parse listener.
   *
   * @param  {string}   name
   * @param  {Function} cb
   *
   * @return {Saxen}
   */
  this['on'] = function(name, cb) {

    if (typeof cb !== 'function') {
      throw error('required args <name, cb>');
    }

    switch (name) {
    case 'openTag': onOpenTag = cb; break;
    case 'text': onText = cb; break;
    case 'closeTag': onCloseTag = cb; break;
    case 'error': onError = cb; break;
    case 'warn': onWarning = cb; break;
    case 'cdata': onCDATA = cb; break;
    case 'attention': onAttention = cb; break; // <!XXXXX zzzz="eeee">
    case 'question': onQuestion = cb; break; // <? ....  ?>
    case 'comment': onComment = cb; break;
    default:
      throw error('unsupported event: ' + name);
    }

    return this;
  };

  /**
   * Set the namespace to prefix mapping.
   *
   * @example
   *
   * parser.ns({
   *   'http://foo': 'foo',
   *   'http://bar': 'bar'
   * });
   *
   * @param  {!Object<string, string>} nsMap
   *
   * @return {Saxen}
   */
  this['ns'] = function(nsMap) {

    if (typeof nsMap === 'undefined') {
      nsMap = {};
    }

    if (typeof nsMap !== 'object') {
      throw error('required args <nsMap={}>');
    }

    var _nsUriToPrefix = {}, k;

    for (k in nsMap) {
      _nsUriToPrefix[k] = nsMap[k];
    }

    // FORCE default mapping for schema instance
    _nsUriToPrefix[XSI_URI] = XSI_PREFIX;

    isNamespace = true;
    nsUriToPrefix = _nsUriToPrefix;

    return this;
  };

  /**
   * Parse xml string.
   *
   * @param  {string} xml
   *
   * @return {Error} returnError, if not thrown
   */
  this['parse'] = function(xml) {
    if (typeof xml !== 'string') {
      throw error('required args <xml=string>');
    }

    returnError = null;

    parse(xml);

    getContext = noopGetContext;
    parseStop = false;

    return returnError;
  };

  /**
   * Stop parsing.
   */
  this['stop'] = function() {
    parseStop = true;
  };

  /**
   * Parse string, invoking configured listeners on element.
   *
   * @param  {string} xml
   */
  function parse(xml) {
    var nsMatrixStack = isNamespace ? [] : null,
        nsMatrix = isNamespace ? buildNsMatrix(nsUriToPrefix) : null,
        _nsMatrix,
        nodeStack = [],
        anonymousNsCount = 0,
        tagStart = false,
        tagEnd = false,
        i = 0, j = 0,
        x, y, q, w,
        xmlns,
        elementName,
        _elementName,
        elementProxy
        ;

    var attrsString = '',
        attrsStart = 0,
        cachedAttrs // false = parsed with errors, null = needs parsing
        ;

    /**
     * Parse attributes on demand and returns the parsed attributes.
     *
     * Return semantics: (1) `false` on attribute parse error,
     * (2) object hash on extracted attrs.
     *
     * @return {boolean|Object}
     */
    function getAttrs() {
      if (cachedAttrs !== null) {
        return cachedAttrs;
      }

      var nsUri,
          nsUriPrefix,
          nsName,
          defaultAlias = isNamespace && nsMatrix['xmlns'],
          attrList = isNamespace && maybeNS ? [] : null,
          i = attrsStart,
          s = attrsString,
          l = s.length,
          hasNewMatrix,
          newalias,
          value,
          alias,
          name,
          attrs = {},
          w,
          j;


      for (; i < l; i++) {
        w = s.charCodeAt(i);

        if (w === 32 || (w < 14 && w > 8) ) { // \f\n\r\t\v
          continue;
        }

        // wait for non whitespace character
        if (w < 65 || w > 122 || (w > 90 && w < 97) ) {
          if (w !== 95 && w !== 58) { // char 95"_" 58":"
            handleWarning('illegal first char attribute name');
            return cachedAttrs = false;
          }
        }

        // parse attribute name
        for (j = i + 1; j < l; j++) {
          w = s.charCodeAt(j);

          if ( w > 96 && w < 123 || w > 64 && w < 91 || w > 47 && w < 59 || w === 45 || w === 95) {
            continue;
          }

          if (w !== 61) { // "=" == 61
            // expected "="
            handleWarning('missing attribute value');
            return cachedAttrs = false;
          }

          break;
        }

        name = s.substring(i, j);

        if (name === 'xmlns:xmlns') {
          handleWarning('illegal declaration of xmlns');
          return cachedAttrs = false; // error. invalid name
        }

        w = s.charCodeAt(j + 1);

        if (w === 34) {  // '"'
          j = s.indexOf('"', i = j + 2 );

        } else {
          if (w !== 39) { // "'"
            handleWarning('missing attribute value quotes');
            return cachedAttrs = false; // error. invalid char
          }

          j = s.indexOf('\'', i = j + 2 );
        }

        if (j === -1) {
          handleWarning('attribute value quote missmatch');
          return cachedAttrs = false; // error. invalid char
        }

        if (j + 1 < l) {
          w = s.charCodeAt(j + 1);

          if (w > 32 || w < 9 || (w < 32 && w > 13)) {
            // error. invalid char
            handleWarning('illegal character after attribute end');
            return cachedAttrs = false;
          }
        }


        value = s.substring(i, j);

        // advance cursor to next attribute
        i = j + 1;

        if (!isNamespace) {
          attrs[name] = value;
          continue;
        }

        // try to extract namespace information
        if (maybeNS) {
          newalias = (
            name === 'xmlns'
              ? 'xmlns'
              : (name.charCodeAt(0) === 120 && name.substr(0, 6) === 'xmlns:')
                ? name.substr(6)
                : null
          );

          // handle xmlns(:alias) assignment
          if (newalias !== null) {
            nsUri = decodeEntities(value);
            nsUriPrefix = uriPrefix(newalias);

            alias = nsUriToPrefix[nsUri];

            if (!alias) {
              // no prefix defined or prefix collision
              if (
                (newalias === 'xmlns') ||
                (nsUriPrefix in nsMatrix && nsMatrix[nsUriPrefix] !== nsUri)
              ) {
                // alocate free ns prefix
                do {
                  alias = 'ns' + (anonymousNsCount++);
                } while (typeof nsMatrix[alias] !== 'undefined');
              } else {
                alias = newalias;
              }

              nsUriToPrefix[nsUri] = alias;
            }

            if (nsMatrix[newalias] !== alias) {
              if (!hasNewMatrix) {
                nsMatrix = cloneNsMatrix(nsMatrix);
                hasNewMatrix = true;
              }

              nsMatrix[newalias] = alias;
              if (newalias === 'xmlns') {
                nsMatrix[uriPrefix(alias)] = nsUri;
                defaultAlias = alias;
              }

              nsMatrix[nsUriPrefix] = nsUri;
            }

            // expose xmlns(:asd)="..." in attributes
            attrs[name] = value;
            continue;
          }

          // collect attributes until all namespace
          // declarations are processed
          attrList.push(name, value);
          continue;

        } /** end if (maybeNs) */

        // handle attributes on element without
        // namespace declarations
        w = name.indexOf(':');
        if (w === -1) {
          attrs[name] = value;
          continue;
        }

        // normalize ns attribute name
        if (!(nsName = nsMatrix[name.substring(0, w)])) {
          handleWarning(missingNamespaceForPrefix(name.substring(0, w)));
          continue;
        }

        name = defaultAlias === nsName
          ? name.substr(w + 1)
          : nsName + name.substr(w);
        // end: normalize ns attribute name

        // normalize xsi:type ns attribute value
        if (name === XSI_TYPE) {
          w = value.indexOf(':');

          if (w !== -1) {
            nsName = value.substring(0, w);
            // handle default prefixes, i.e. xs:String gracefully
            nsName = nsMatrix[nsName] || nsName;
            value = nsName + value.substring(w);
          } else {
            value = defaultAlias + ':' + value;
          }
        }
        // end: normalize xsi:type ns attribute value

        attrs[name] = value;
      }


      // handle deferred, possibly namespaced attributes
      if (maybeNS)  {

        // normalize captured attributes
        for (i = 0, l = attrList.length; i < l; i++) {

          name = attrList[i++];
          value = attrList[i];

          w = name.indexOf(':');

          if (w !== -1) {
            // normalize ns attribute name
            if (!(nsName = nsMatrix[name.substring(0, w)])) {
              handleWarning(missingNamespaceForPrefix(name.substring(0, w)));
              continue;
            }

            name = defaultAlias === nsName
              ? name.substr(w + 1)
              : nsName + name.substr(w);
            // end: normalize ns attribute name

            // normalize xsi:type ns attribute value
            if (name === XSI_TYPE) {
              w = value.indexOf(':');

              if (w !== -1) {
                nsName = value.substring(0, w);
                // handle default prefixes, i.e. xs:String gracefully
                nsName = nsMatrix[nsName] || nsName;
                value = nsName + value.substring(w);
              } else {
                value = defaultAlias + ':' + value;
              }
            }
            // end: normalize xsi:type ns attribute value
          }

          attrs[name] = value;
        }
        // end: normalize captured attributes
      }

      return cachedAttrs = attrs;
    }

    /**
     * Extract the parse context { line, column, part }
     * from the current parser position.
     *
     * @return {Object} parse context
     */
    function getParseContext() {
      var splitsRe = /(\r\n|\r|\n)/g;

      var line = 0;
      var column = 0;
      var startOfLine = 0;
      var endOfLine = j;
      var match;
      var data;

      while (i >= startOfLine) {

        match = splitsRe.exec(xml);

        if (!match) {
          break;
        }

        // end of line = (break idx + break chars)
        endOfLine = match[0].length + match.index;

        if (endOfLine > i) {
          break;
        }

        // advance to next line
        line += 1;

        startOfLine = endOfLine;
      }

      // EOF errors
      if (i == -1) {
        column = endOfLine;
        data = '';
      } else {
        column = i - startOfLine;
        data = (j == -1 ? xml.substring(i) : xml.substring(i, j + 1));
      }

      return {
        'data': data,
        'line': line,
        'column': column
      };
    }

    getContext = getParseContext;


    if (proxy) {
      elementProxy = Object.create({}, {
        'name': getter(function() {
          return elementName;
        }),
        'originalName': getter(function() {
          return _elementName;
        }),
        'attrs': getter(getAttrs),
        'ns': getter(function() {
          return nsMatrix;
        })
      });
    }

    // actual parse logic
    while (j !== -1) {

      if (xml.charCodeAt(j) === 60) { // "<"
        i = j;
      } else {
        i = xml.indexOf('<', j);
      }

      // parse end
      if (i === -1) {
        if (nodeStack.length) {
          return handleError('unexpected end of file');
        }

        if (j === 0) {
          return handleError('missing start tag');
        }

        return;
      }

      if (j !== i) {
        if (onText) {
          onText(xml.substring(j, i), decodeEntities);
          if (parseStop) {
            return;
          }
        }
      }

      w = xml.charCodeAt(i+1);

      // parse comments + CDATA
      if (w === 33) { // "!"
        w = xml.charCodeAt(i+2);
        if (w === 91 && xml.substr(i + 3, 6) === 'CDATA[') { // 91 == "["
          j = xml.indexOf(']]>', i);
          if (j === -1) {
            return handleError('unclosed cdata');
          }

          if (onCDATA) {
            onCDATA(xml.substring(i + 9, j));
            if (parseStop) {
              return;
            }
          }

          j += 3;
          continue;
        }


        if (w === 45 && xml.charCodeAt(i + 3) === 45) { // 45 == "-"
          j = xml.indexOf('-->', i);
          if (j === -1) {
            return handleError('unclosed comment');
          }


          if (onComment) {
            onComment(xml.substring(i + 4, j), decodeEntities);
            if (parseStop) {
              return;
            }
          }

          j += 3;
          continue;
        }

        j = xml.indexOf('>', i + 1);
        if (j === -1) {
          return handleError('unclosed tag');
        }

        if (onAttention) {
          onAttention(xml.substring(i, j + 1), decodeEntities);
          if (parseStop) {
            return;
          }
        }

        j += 1;
        continue;
      }

      if (w === 63) { // "?"
        j = xml.indexOf('?>', i);
        if (j === -1) { // error
          return handleError('unclosed question');
        }

        if (onQuestion) {
          onQuestion(xml.substring(i, j + 2));
          if (parseStop) {
            return;
          }
        }

        j += 2;
        continue;
      }

      j = xml.indexOf('>', i + 1);

      if (j == -1) { // error
        return handleError('unclosed tag');
      }

      // don't process attributes;
      // there are none
      cachedAttrs = {};

      //if (xml.charCodeAt(i+1) === 47) { // </...
      if (w === 47) { // </...
        tagStart = false;
        tagEnd = true;

        if (!nodeStack.length) {
          return handleError('missing open tag');
        }

        // verify open <-> close tag match
        x = elementName = nodeStack.pop();
        q = i + 2 + x.length;

        if (xml.substring(i + 2, q) !== x) {
          return handleError('closing tag mismatch');
        }

        // verify chars in close tag
        for (; q < j; q++) {
          w = xml.charCodeAt(q);

          if (w === 32 || (w > 8 && w < 14)) {  // \f\n\r\t\v space
            continue;
          }

          return handleError('close tag');
        }

      } else {
        if (xml.charCodeAt(j - 1) ===  47) { // .../>
          x = elementName = xml.substring(i + 1, j - 1);

          tagStart = true;
          tagEnd = true;

        } else {
          x = elementName = xml.substring(i + 1, j);

          tagStart = true;
          tagEnd = false;
        }

        if (!(w > 96  && w < 123 || w > 64 && w < 91 || w === 95 || w === 58)) { // char 95"_" 58":"
          return handleError('illegal first char nodeName');
        }

        for (q = 1, y = x.length; q < y; q++) {
          w = x.charCodeAt(q);

          if (w > 96 && w < 123 || w > 64 && w < 91 || w > 47 && w < 59 || w === 45 || w === 95) {
            continue;
          }

          if (w === 32 || (w < 14 && w > 8)) { // \f\n\r\t\v space
            elementName = x.substring(0, q);
            // maybe there are attributes
            cachedAttrs = null;
            break;
          }

          return handleError('invalid nodeName');
        }

        if (!tagEnd) {
          nodeStack.push(elementName);
        }
      }

      if (isNamespace) {

        _nsMatrix = nsMatrix;

        if (tagStart) {
          // remember old namespace
          // unless we're self-closing
          if (!tagEnd) {
            nsMatrixStack.push(_nsMatrix);
          }

          if (cachedAttrs === null) {
            // quick check, whether there may be namespace
            // declarations on the node; if that is the case
            // we need to eagerly parse the node attributes
            if ((maybeNS = x.indexOf('xmlns', q) !== -1)) {
              attrsStart = q;
              attrsString = x;

              getAttrs();

              maybeNS = false;
            }
          }
        }

        _elementName = elementName;

        w = elementName.indexOf(':');
        if (w !== -1) {
          xmlns = nsMatrix[elementName.substring(0, w)];

          // prefix given; namespace must exist
          if (!xmlns) {
            return handleError('missing namespace on <' + _elementName + '>');
          }

          elementName = elementName.substr(w + 1);
        } else {
          xmlns = nsMatrix['xmlns'];

          // if no default namespace is defined,
          // we'll import the element as anonymous.
          //
          // it is up to users to correct that to the document defined
          // targetNamespace, or whatever their undersanding of the
          // XML spec mandates.
        }

        // adjust namespace prefixs as configured
        if (xmlns) {
          elementName = xmlns + ':' + elementName;
        }

      }

      if (tagStart) {
        attrsStart = q;
        attrsString = x;

        if (onOpenTag) {
          if (proxy) {
            onOpenTag(elementProxy, decodeEntities, tagEnd, getContext);
          } else {
            onOpenTag(elementName, getAttrs, decodeEntities, tagEnd, getContext);
          }

          if (parseStop) {
            return;
          }
        }

      }

      if (tagEnd) {

        if (onCloseTag) {
          onCloseTag(proxy ? elementProxy : elementName, decodeEntities, tagStart, getContext);

          if (parseStop) {
            return;
          }
        }

        // restore old namespace
        if (isNamespace) {
          if (!tagStart) {
            nsMatrix = nsMatrixStack.pop();
          } else {
            nsMatrix = _nsMatrix;
          }
        }
      }

      j += 1;
    }
  } /** end parse */

}
},{"223":223}],225:[function(_dereq_,module,exports){
/**
 * Tiny stack for browser or server
 *
 * @author Jason Mulligan <jason.mulligan@avoidwork.com>
 * @copyright 2014 Jason Mulligan
 * @license BSD-3 <https://raw.github.com/avoidwork/tiny-stack/master/LICENSE>
 * @link http://avoidwork.github.io/tiny-stack
 * @module tiny-stack
 * @version 0.1.0
 */

( function ( global ) {

"use strict";

/**
 * TinyStack
 *
 * @constructor
 */
function TinyStack () {
	this.data = [null];
	this.top  = 0;
}

/**
 * Clears the stack
 *
 * @method clear
 * @memberOf TinyStack
 * @return {Object} {@link TinyStack}
 */
TinyStack.prototype.clear = function clear () {
	this.data = [null];
	this.top  = 0;

	return this;
};

/**
 * Gets the size of the stack
 *
 * @method length
 * @memberOf TinyStack
 * @return {Number} Size of stack
 */
TinyStack.prototype.length = function length () {
	return this.top;
};

/**
 * Gets the item at the top of the stack
 *
 * @method peek
 * @memberOf TinyStack
 * @return {Mixed} Item at the top of the stack
 */
TinyStack.prototype.peek = function peek () {
	return this.data[this.top];
};

/**
 * Gets & removes the item at the top of the stack
 *
 * @method pop
 * @memberOf TinyStack
 * @return {Mixed} Item at the top of the stack
 */
TinyStack.prototype.pop = function pop () {
	if ( this.top > 0 ) {
		this.top--;

		return this.data.pop();
	}
	else {
		return undefined;
	}
};

/**
 * Pushes an item onto the stack
 *
 * @method push
 * @memberOf TinyStack
 * @return {Object} {@link TinyStack}
 */
TinyStack.prototype.push = function push ( arg ) {
	this.data[++this.top] = arg;

	return this;
};

/**
 * TinyStack factory
 *
 * @method factory
 * @return {Object} {@link TinyStack}
 */
function factory () {
	return new TinyStack();
}

// Node, AMD & window supported
if ( typeof exports != "undefined" ) {
	module.exports = factory;
}
else if ( typeof define == "function" ) {
	define( function () {
		return factory;
	} );
}
else {
	global.stack = factory;
}
} )( this );

},{}],226:[function(_dereq_,module,exports){
/**
 * append utility
 */

module.exports = append;

var appendTo = _dereq_(227);

/**
 * Append a node to an element
 *
 * @param  {SVGElement} element
 * @param  {SVGElement} node
 *
 * @return {SVGElement} the element
 */
function append(element, node) {
  appendTo(node, element);
  return element;
}
},{"227":227}],227:[function(_dereq_,module,exports){
/**
 * appendTo utility
 */
module.exports = appendTo;

var ensureImported = _dereq_(236);

/**
 * Append a node to a target element and return the appended node.
 *
 * @param  {SVGElement} element
 * @param  {SVGElement} node
 *
 * @return {SVGElement} the appended node
 */
function appendTo(element, target) {
  target.appendChild(ensureImported(element, target));
  return element;
}
},{"236":236}],228:[function(_dereq_,module,exports){
/**
 * attribute accessor utility
 */

module.exports = attr;


var LENGTH_ATTR = 2;

var CSS_PROPERTIES = {
  'alignment-baseline': 1,
  'baseline-shift': 1,
  'clip': 1,
  'clip-path': 1,
  'clip-rule': 1,
  'color': 1,
  'color-interpolation': 1,
  'color-interpolation-filters': 1,
  'color-profile': 1,
  'color-rendering': 1,
  'cursor': 1,
  'direction': 1,
  'display': 1,
  'dominant-baseline': 1,
  'enable-background': 1,
  'fill': 1,
  'fill-opacity': 1,
  'fill-rule': 1,
  'filter': 1,
  'flood-color': 1,
  'flood-opacity': 1,
  'font': 1,
  'font-family': 1,
  'font-size': LENGTH_ATTR,
  'font-size-adjust': 1,
  'font-stretch': 1,
  'font-style': 1,
  'font-variant': 1,
  'font-weight': 1,
  'glyph-orientation-horizontal': 1,
  'glyph-orientation-vertical': 1,
  'image-rendering': 1,
  'kerning': 1,
  'letter-spacing': 1,
  'lighting-color': 1,
  'marker': 1,
  'marker-end': 1,
  'marker-mid': 1,
  'marker-start': 1,
  'mask': 1,
  'opacity': 1,
  'overflow': 1,
  'pointer-events': 1,
  'shape-rendering': 1,
  'stop-color': 1,
  'stop-opacity': 1,
  'stroke': 1,
  'stroke-dasharray': 1,
  'stroke-dashoffset': 1,
  'stroke-linecap': 1,
  'stroke-linejoin': 1,
  'stroke-miterlimit': 1,
  'stroke-opacity': 1,
  'stroke-width': LENGTH_ATTR,
  'text-anchor': 1,
  'text-decoration': 1,
  'text-rendering': 1,
  'unicode-bidi': 1,
  'visibility': 1,
  'word-spacing': 1,
  'writing-mode': 1
};


function getAttribute(node, name) {
  if (CSS_PROPERTIES[name]) {
    return node.style[name];
  } else {
    return node.getAttributeNS(null, name);
  }
}

function setAttribute(node, name, value) {
  var hyphenated = name.replace(/([a-z])([A-Z])/g, '$1-$2').toLowerCase();

  var type = CSS_PROPERTIES[hyphenated];

  if (type) {
    // append pixel unit, unless present
    if (type === LENGTH_ATTR && typeof value === 'number') {
      value = String(value) + 'px';
    }

    node.style[hyphenated] = value;
  } else {
    node.setAttributeNS(null, name, value);
  }
}

function setAttributes(node, attrs) {

  var names = Object.keys(attrs), i, name;

  for (i = 0, name; (name = names[i]); i++) {
    setAttribute(node, name, attrs[name]);
  }
}

/**
 * Gets or sets raw attributes on a node.
 *
 * @param  {SVGElement} node
 * @param  {Object} [attrs]
 * @param  {String} [name]
 * @param  {String} [value]
 *
 * @return {String}
 */
function attr(node, name, value) {
  if (typeof name === 'string') {
    if (value !== undefined) {
      setAttribute(node, name, value);
    } else {
      return getAttribute(node, name);
    }
  } else {
    setAttributes(node, name);
  }

  return node;
}

},{}],229:[function(_dereq_,module,exports){
/**
 * Clear utility
 */
module.exports = classes;

var index = function(arr, obj) {
  if (arr.indexOf) {
    return arr.indexOf(obj);
  }


  for (var i = 0; i < arr.length; ++i) {
    if (arr[i] === obj) {
      return i;
    }
  }

  return -1;
};

var re = /\s+/;

var toString = Object.prototype.toString;

function defined(o) {
  return typeof o !== 'undefined';
}

/**
 * Wrap `el` in a `ClassList`.
 *
 * @param {Element} el
 * @return {ClassList}
 * @api public
 */

function classes(el) {
  return new ClassList(el);
}

function ClassList(el) {
  if (!el || !el.nodeType) {
    throw new Error('A DOM element reference is required');
  }
  this.el = el;
  this.list = el.classList;
}

/**
 * Add class `name` if not already present.
 *
 * @param {String} name
 * @return {ClassList}
 * @api public
 */

ClassList.prototype.add = function(name) {

  // classList
  if (this.list) {
    this.list.add(name);
    return this;
  }

  // fallback
  var arr = this.array();
  var i = index(arr, name);
  if (!~i) {
    arr.push(name);
  }

  if (defined(this.el.className.baseVal)) {
    this.el.className.baseVal = arr.join(' ');
  } else {
    this.el.className = arr.join(' ');
  }

  return this;
};

/**
 * Remove class `name` when present, or
 * pass a regular expression to remove
 * any which match.
 *
 * @param {String|RegExp} name
 * @return {ClassList}
 * @api public
 */

ClassList.prototype.remove = function(name) {
  if ('[object RegExp]' === toString.call(name)) {
    return this.removeMatching(name);
  }

  // classList
  if (this.list) {
    this.list.remove(name);
    return this;
  }

  // fallback
  var arr = this.array();
  var i = index(arr, name);
  if (~i) {
    arr.splice(i, 1);
  }
  this.el.className.baseVal = arr.join(' ');
  return this;
};

/**
 * Remove all classes matching `re`.
 *
 * @param {RegExp} re
 * @return {ClassList}
 * @api private
 */

ClassList.prototype.removeMatching = function(re) {
  var arr = this.array();
  for (var i = 0; i < arr.length; i++) {
    if (re.test(arr[i])) {
      this.remove(arr[i]);
    }
  }
  return this;
};

/**
 * Toggle class `name`, can force state via `force`.
 *
 * For browsers that support classList, but do not support `force` yet,
 * the mistake will be detected and corrected.
 *
 * @param {String} name
 * @param {Boolean} force
 * @return {ClassList}
 * @api public
 */

ClassList.prototype.toggle = function(name, force) {
  // classList
  if (this.list) {
    if (defined(force)) {
      if (force !== this.list.toggle(name, force)) {
        this.list.toggle(name); // toggle again to correct
      }
    } else {
      this.list.toggle(name);
    }
    return this;
  }

  // fallback
  if (defined(force)) {
    if (!force) {
      this.remove(name);
    } else {
      this.add(name);
    }
  } else {
    if (this.has(name)) {
      this.remove(name);
    } else {
      this.add(name);
    }
  }

  return this;
};

/**
 * Return an array of classes.
 *
 * @return {Array}
 * @api public
 */

ClassList.prototype.array = function() {
  var className = this.el.getAttribute('class') || '';
  var str = className.replace(/^\s+|\s+$/g, '');
  var arr = str.split(re);
  if ('' === arr[0]) {
    arr.shift();
  }
  return arr;
};

/**
 * Check if class `name` is present.
 *
 * @param {String} name
 * @return {ClassList}
 * @api public
 */

ClassList.prototype.has =
ClassList.prototype.contains = function(name) {
  return (
    this.list ?
      this.list.contains(name) :
      !! ~index(this.array(), name)
  );
};

},{}],230:[function(_dereq_,module,exports){
/**
 * Clear utility
 */

module.exports = clear;


var remove = _dereq_(234);

/**
 * Removes all children from the given element
 *
 * @param  {DOMElement} element
 * @return {DOMElement} the element (for chaining)
 */
function clear(element) {
  var child;

  while ((child = element.firstChild)) {
    remove(child);
  }

  return element;
}
},{"234":234}],231:[function(_dereq_,module,exports){
/**
 * Create utility for SVG elements
 */

module.exports = create;


var attr = _dereq_(228);
var parse = _dereq_(238);
var ns = _dereq_(237);


/**
 * Create a specific type from name or SVG markup.
 *
 * @param {String} name the name or markup of the element
 * @param {Object} [attrs] attributes to set on the element
 *
 * @returns {SVGElement}
 */
function create(name, attrs) {
  var element;

  if (name.charAt(0) === '<') {
    element = parse(name).firstChild;
    element = document.importNode(element, true);
  } else {
    element = document.createElementNS(ns.svg, name);
  }

  if (attrs) {
    attr(element, attrs);
  }

  return element;
}
},{"228":228,"237":237,"238":238}],232:[function(_dereq_,module,exports){
/**
 * Geometry helpers
 */


var create = _dereq_(231);

// fake node used to instantiate svg geometry elements
var node = create('svg');

function extend(object, props) {
  var i, k, keys = Object.keys(props);

  for (i = 0; (k = keys[i]); i++) {
    object[k] = props[k];
  }

  return object;
}


function createPoint(x, y) {
  var point = node.createSVGPoint();

  switch (arguments.length) {
  case 0:
    return point;
  case 2:
    x = {
      x: x,
      y: y
    };
    break;
  }

  return extend(point, x);
}

function createMatrix(a, b, c, d, e, f) {
  var matrix = node.createSVGMatrix();

  switch (arguments.length) {
  case 0:
    return matrix;
  case 6:
    a = {
      a: a,
      b: b,
      c: c,
      d: d,
      e: e,
      f: f
    };
    break;
  }

  return extend(matrix, a);
}

function createTransform(matrix) {
  if (matrix) {
    return node.createSVGTransformFromMatrix(matrix);
  } else {
    return node.createSVGTransform();
  }
}


module.exports.createTransform = createTransform;
module.exports.createMatrix = createMatrix;
module.exports.createPoint = createPoint;
},{"231":231}],233:[function(_dereq_,module,exports){
/**
 * innerHTML like functionality for SVG elements.
 * based on innerSVG (https://code.google.com/p/innersvg)
 */

module.exports = innerSVG;


var clear = _dereq_(230);
var appendTo = _dereq_(227);
var parse = _dereq_(238);
var serialize = _dereq_(239);


function set(element, svg) {

  var node,
      documentElement = parse(svg).documentElement;

  // clear element contents
  clear(element);

  if (!svg) {
    return;
  }

  // import + append each node
  node = documentElement.firstChild;

  while (node) {
    appendTo(node, element);
    node = node.nextSibling;
  }
}

function get(element) {
  var child = element.firstChild,
      output = [];

  while (child) {
    serialize(child, output);
    child = child.nextSibling;
  }

  return output.join('');
}

function innerSVG(element, svg) {

  if (svg !== undefined) {

    try {
      set(element, svg);
    } catch (e) {
      throw new Error('error parsing SVG: ' + e.message);
    }

    return element;
  } else {
    return get(element);
  }
}
},{"227":227,"230":230,"238":238,"239":239}],234:[function(_dereq_,module,exports){
module.exports = remove;

function remove(element) {
  var parent = element.parentNode;

  if (parent) {
    parent.removeChild(element);
  }

  return element;
}
},{}],235:[function(_dereq_,module,exports){
/**
 * transform accessor utility
 */

module.exports = transform;

function wrapMatrix(transformList, transform) {
  if (transform instanceof SVGMatrix) {
    return transformList.createSVGTransformFromMatrix(transform);
  } else {
    return transform;
  }
}

function setTransforms(transformList, transforms) {
  var i, t;

  transformList.clear();

  for (i = 0; (t = transforms[i]); i++) {
    transformList.appendItem(wrapMatrix(transformList, t));
  }

  transformList.consolidate();
}

function transform(node, transforms) {
  var transformList = node.transform.baseVal;

  if (arguments.length === 1) {
    return transformList.consolidate();
  } else {
    if (transforms.length) {
      setTransforms(transformList, transforms);
    } else {
      transformList.initialize(wrapMatrix(transformList, transforms));
    }
  }
}
},{}],236:[function(_dereq_,module,exports){
module.exports = ensureImported;

function ensureImported(element, target) {

  if (element.ownerDocument !== target.ownerDocument) {
    try {
      // may fail on webkit
      return target.ownerDocument.importNode(element, true);
    } catch (e) {
      // ignore
    }
  }

  return element;
}
},{}],237:[function(_dereq_,module,exports){
var ns = {
  svg: 'http://www.w3.org/2000/svg'
};

module.exports = ns;
},{}],238:[function(_dereq_,module,exports){
/**
 * DOM parsing utility
 */

module.exports = parse;


var ns = _dereq_(237);

var SVG_START = '<svg xmlns="' + ns.svg + '"';

function parse(svg) {

  // ensure we import a valid svg document
  if (svg.substring(0, 4) === '<svg') {
    if (svg.indexOf(ns.svg) === -1) {
      svg = SVG_START + svg.substring(4);
    }
  } else {
    // namespace svg
    svg = SVG_START + '>' + svg + '</svg>';
  }

  return parseDocument(svg);
}

function parseDocument(svg) {

  var parser;

  // parse
  parser = new DOMParser();
  parser.async = false;

  return parser.parseFromString(svg, 'text/xml');
}
},{"237":237}],239:[function(_dereq_,module,exports){
/**
 * Serialization util
 */

module.exports = serialize;


var TEXT_ENTITIES = /([&<>]{1})/g;
var ATTR_ENTITIES = /([\n\r"]{1})/g;

var ENTITY_REPLACEMENT = {
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
  '"': '\''
};

function escape(str, pattern) {

  function replaceFn(match, entity) {
    return ENTITY_REPLACEMENT[entity] || entity;
  }

  return str.replace(pattern, replaceFn);
}

function serialize(node, output) {

  var i, len, attrMap, attrNode, childNodes;

  switch (node.nodeType) {
  // TEXT
  case 3:
    // replace special XML characters
    output.push(escape(node.textContent, TEXT_ENTITIES));
    break;

  // ELEMENT
  case 1:
    output.push('<', node.tagName);

    if (node.hasAttributes()) {
      attrMap = node.attributes;
      for (i = 0, len = attrMap.length; i < len; ++i) {
        attrNode = attrMap.item(i);
        output.push(' ', attrNode.name, '="', escape(attrNode.value, ATTR_ENTITIES), '"');
      }
    }

    if (node.hasChildNodes()) {
      output.push('>');
      childNodes = node.childNodes;
      for (i = 0, len = childNodes.length; i < len; ++i) {
        serialize(childNodes.item(i), output);
      }
      output.push('</', node.tagName, '>');
    } else {
      output.push('/>');
    }
    break;

  // COMMENT
  case 8:
    output.push('<!--', escape(node.nodeValue, TEXT_ENTITIES), '-->');
    break;

  // CDATA
  case 4:
    output.push('<![CDATA[', node.nodeValue, ']]>');
    break;

  default:
    throw new Error('unable to handle node ' + node.nodeType);
  }

  return output;
}
},{}]},{},[1])(1)
});
//# sourceMappingURL=./bpmn-viewer.js.map