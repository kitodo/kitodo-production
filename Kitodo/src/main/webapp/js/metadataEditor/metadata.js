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

/*
 * The function toAjaxUrl() converts an absolute URL of a norm data record
 * to the relative URL we need to actually retrieve this norm data record,
 * using a reverse proxy to get along with Javascript's same origin policy.
 *
 * @param URL
 *            of a norm data record
 * @return access URL via reverse proxy
 */
function toAjaxUrl(url) {
    return url.replace(new RegExp("^.*?://[^/]+/(.*)$", ""), function ($0, $1) {
        return "${pageContext.request.contextPath}/" + $1 + "/about/rdf";
    });
}

/*
 * The function setNameFromRecord() retrieves a norm data record using AJAX
 * and puts the first and last name in the corresponding form fields.
 *
 * @param recordID
 *            id of the input element holding the URL of the record
 * @param firstnameID
 *            id of the input element to put the first name
 * @param lastnameID
 *            id of the input element to put the last name
 */
function setNameFromRecord(recordID, firstnameID, lastnameID) {
    var url = toAjaxUrl(document.getElementById(recordID).value);
    jQuery.ajax({
        url: url,
        dataType: "xml",
        error: function (jqXHR, textStatus, errorThrown) {
            alert("${msgs.getNormDataRecordFailed} " + errorThrown);
        }
    })
        .done(function (data) {
            var preferredName = data.getElementsByTagName("gndo:preferredNameEntityForThePerson")[0];
            document.getElementById(firstnameID).value = preferredName.getElementsByTagName("gndo:forename")[0].textContent;
            document.getElementById(lastnameID).value = preferredName.getElementsByTagName("gndo:surname")[0].textContent;
            try {
                A4J.AJAX.Submit("_viewRoot", "formular2", null, null);
            } catch (e) {
                document.getElementById("formular2").submit();
            }
        });
}

/*
 * The function getNormDateNeuPerson() retrieves a norm data record using
 * AJAX from the form to add a new person as metadata. The first and last
 * name from the records will be put in the corresponding form fields.
 */
function getNormDataNeuPerson() {
    setNameFromRecord("formular2:normDataRecord", "formular2:vorname", "formular2:nachname");
}

/*
 * The function getNormDataPersonenUndMetadaten() retrieves a norm data
 * record using AJAX from the form showing all metadata. The first and last
 * name from the records will be put in the corresponding form fields.
 *
 * @param actionLink
 *            link corresponding to the group of fields to update
 */
function getNormDataPersonenUndMetadaten(actionLink) {
    var actionLinkID = actionLink.id;
    var recordID = actionLinkID.replace(/:clicker$/, ":record");
    var firstnameID = actionLinkID.replace(/:clicker$/, ":firstname");
    var lastnameID = actionLinkID.replace(/:clicker$/, ":lastname");
    setNameFromRecord(recordID, firstnameID, lastnameID);
}

// Funktion, die Aenderungen prueft
function styleAnpassen(element) {
    // element.className = "metadatenInputChange";
    // document.getElementById("formular4:DatenGeaendert").value = "1";
    // document.getElementById("formular2:y1").style.display='block';
    // document.getElementById("formular2:x1").style.display='block';
    // document.getElementById("formular2:y2").style.border='2px dashed red';
    // document.getElementById("formular2:x2").style.border='2px dashed silver';
    // document.getElementById("formular2:y2").style.padding='3px';
    // document.getElementById("formular2:x2").style.padding='3px';
}

function styleAnpassenPerson(element) {
    // bei den DropDowns-den Parent (als die _$ta) als Rahmen aendern
    // element.parentNode.className = "metadatenInputChange";
    // document.getElementById("formular4:DatenGeaendert").value = "1";
    // document.getElementById("formular2:y1").style.display='block';
    // document.getElementById("formular2:x1").style.display='block';
    // document.getElementById("formular2:y2").style.border='2px dashed red';
    // document.getElementById("formular2:x2").style.border='2px dashed silver';
    // document.getElementById("formular2:y2").style.padding='3px';
    // document.getElementById("formular2:x2").style.padding='3px';
}

function setSecretElement(invalue) {
    document.getElementById("secretElement").value = invalue;
    addableTypenAnzeigen();
}

function TreeReloaden() {
    addableTypenAnzeigen();
    var mybutton = parent["links"].document.getElementById("reloadMyTree");
    if (mybutton != null) {
        mybutton.click;
    }

}

function addableTypenAnzeigen() {

    var treereloadelement = parent.oben.document.getElementById("treeReload");
    //alert(treereloadelement);
    if (treereloadelement!=null){
        alert(treereloadelement.value);
        alert(treereloadelement.value=="");
        if (treereloadelement.value!=""){
            alert("jetzt wird reloaded");
            treereloadelement.value ="";
            // parent.oben.document.getElementById("formularOben:treeReloadButton").click();
            var mybutton = parent.oben.document.getElementById("formularOben:treeReloadButton");
            alert (mybutton);
            mybutton.click();
            alert("reloaded");
        }
    }

    alert("hallo " + document.getElementById("secretElement").value);
    wert = 1;
    element = document.getElementById("secretElement");
    if (element != null) {
        if (element.value != null && element.value != "")
            wert = element.value;
    }

    if (document.getElementById("auswahlAddable1") == null
        || document.getElementById("auswahlAddable2") == null)
        return;

    if (wert == 1 || wert == 2) {
        //alert("ist eins oder zwei");
        document.getElementById("auswahlAddable1").style.display = 'block';
        document.getElementById("auswahlAddable2").style.display = 'none';
    }

    if (wert == 3 || wert == 4) {
        //alert("ist drei oder vier");
        document.getElementById("auswahlAddable1").style.display = 'none';
        document.getElementById("auswahlAddable2").style.display = 'block';
    }
}

function paginierungWertAnzeigen(element) {
    var inputBoxElement = document.getElementById("paginierungWert");
    inputBoxElement.style.display = (element.value == 3 ? 'none' : '');
    if (element.value == 2 || element.value == 5) {
        inputBoxElement.value = 'I';
    }
    if (element.value == 1 || element.value == 4) {
        inputBoxElement.value = '1';
    }
    if (element.value == 6) {
        inputBoxElement.value = '';
    }
}

function focusForPicture() {
    //alert(document.getElementById("hiddenBildNummer").value);
    //alert(document.getElementById("formular1:BildNummer").value);
    //alert(document.getElementsByName("formular2:myCheckboxes").length);
    for (i = 0; i < document.getElementsByName("formular2:myCheckboxes").length; i++) {
        if (i == document.getElementById("hiddenBildNummer").value - 1) {
            if (i + 1 < document
                    .getElementsByName("formular2:myCheckboxes").length) {
                document.getElementsByName("formular2:myCheckboxes")[i + 1]
                    .focus();
            }
            document.getElementsByName("formular2:myCheckboxes")[i].focus();
        }
    }
}

function submitEnter(commandId, e) {
    var keycode;
    if (window.event)
        keycode = window.event.keyCode;
    else if (e)
        keycode = e.which;
    else
        return true;

    if (keycode == 13) {
        document.getElementById(commandId).click();
        return false;
    } else
        return true;
}

document.documentElement.onkeypress = function (event) {
    //alert("Sie haben die Taste mit dem Wert " + event.which + " gedrueckt");
    myButton = null;
    event = event || window.event; // IE sucks
    var key = event.which || event.keyCode; // IE uses .keyCode, Moz uses .which

    // -----------  previous20 image - cursor up
    if ((key == 76 || key == 12 || key == 40) && // "L" "^L" or "l"
        event.shiftKey && event.ctrlKey) {
        myButton = document.getElementById("formularBild:imageBack20");
        //goToImageBack();
        if (event.preventDefault) {
            event.preventDefault();
        } else {
            event.returnValue = false;
        } // IE sucks
    }

    // -----------  previous image - cursor left
    if ((key == 76 || key == 12 || key == 37) && // "L" "^L" or "l"
        event.shiftKey && event.ctrlKey) {
        myButton = document.getElementById("formularBild:imageBack");
        //goToImageBack();
        if (event.preventDefault) {
            event.preventDefault();
        } else {
            event.returnValue = false;
        } // IE sucks
    }

    // -----------  next image - cursor right
    if ((key == 76 || key == 12 || key == 39) && // "L" "^L" or "l"
        event.shiftKey && event.ctrlKey) {
        myButton = document.getElementById("formularBild:imageNext");
        //goToImageNext();
        if (event.preventDefault) {
            event.preventDefault();
        } else {
            event.returnValue = false;
        } // IE sucks
    }

    // -----------  next image - cursor down
    if ((key == 76 || key == 12 || key == 38) && // "L" "^L" or "l"
        event.shiftKey && event.ctrlKey) {
        myButton = document.getElementById("formularBild:imageNext20");
        //goToImageNext();
        if (event.preventDefault) {
            event.preventDefault();
        } else {
            event.returnValue = false;
        } // IE sucks
    }

    // -----------  first image - pos1
    if ((key == 76 || key == 12 || key == 36) && // "L" "^L" or "l"
        event.shiftKey && event.ctrlKey) {
        myButton = document.getElementById("formularBild:imageFirst");
        //goToImageNext();
        if (event.preventDefault) {
            event.preventDefault();
        } else {
            event.returnValue = false;
        } // IE sucks
    }

    // -----------  last image - end
    if ((key == 76 || key == 12 || key == 35) && // "L" "^L" or "l"
        event.shiftKey && event.ctrlKey) {
        myButton = document.getElementById("formularBild:imageLast");
        //goToImageNext();
        if (event.preventDefault) {
            event.preventDefault();
        } else {
            event.returnValue = false;
        } // IE sucks
    }

    // ---------- click my Button
    try {
        if (myButton != null)
            myButton.click();
    } catch (e) {
    }
}

