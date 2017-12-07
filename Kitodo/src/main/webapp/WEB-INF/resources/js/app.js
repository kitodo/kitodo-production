/**
 * bpmn-js-seed
 *
 * This is an example script that loads an embedded diagram file <diagramXML>
 * and opens it using the bpmn-js modeler.
 */

// create modeler
var bpmnModeler = new BpmnJS({
        container: '#canvas'
});

var diagramXML;

function setDiagramXML() {
    diagramXML = document.getElementById('xmlDiagramReadForm:xmlDiagram').value;
    importXML(diagramXML);
}

// import function
function importXML(xml) {

    // import diagram
    bpmnModeler.importXML(xml, function(err) {

        if (err) {
            return console.error('could not import BPMN 2.0 diagram', err);
        }

        var canvas = bpmnModeler.get('canvas');

        // zoom to fit full viewport
        canvas.zoom('fit-viewport');
    });


    // save diagram on button click
    var saveButton = document.querySelector('#xmlDiagramSaveForm\\:save');

    saveButton.addEventListener('click', function() {

        // get the diagram contents
        bpmnModeler.saveXML({ format: true }, function(err, xml) {

            if (err) {
                console.error('diagram save failed', err);
            } else {
                console.info('diagram saved');
                console.info(xml);
                xmlContent([{name:'xml', value: xml}]);
            }

            alert('diagram saved (see console (F12))');
        });
    });
}
