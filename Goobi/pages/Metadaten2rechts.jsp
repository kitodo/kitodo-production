<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>

<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>
<%-- 
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
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
--%>
<f:view locale="#{SpracheForm.locale}">
	<%@include file="/newpages/inc/head.jsp"%>

	<body style="margin: 0px 2px 2px 2px;" class="metadatenRechtsBody" onload="addableTypenAnzeigen();TreeReloaden()">

		<a4j:status>
			<f:facet name="start">
				<h:graphicImage value="/newpages/images/ajaxload_small.gif" style="position: fixed;top: 35px;right: 15px;" />
			</f:facet>
		</a4j:status>

		<h:form id="formular1" style="margin:0px">
			<htm:table id="navigation" cellpadding="2" cellspacing="0" style="position: fixed; margin-right: 4px; width:99,75%; top:0"
				styleClass="main_statistikboxen">
				<htm:tr>
					<htm:td styleClass="main_statistikboxen_row1" height="1px" colspan="2">

						<htm:table id="bla" width="100%" cellspacing="0" cellpadding="0">
							<htm:tr>
								<htm:td>
									<h:panelGroup id="vdid3" rendered="#{not Metadaten.nurLesenModus}">

										<h:commandLink id="vdid4" action="#{Metadaten.AnsichtAendern}" value="#{msgs.paginierung}" style="font-size: 11px;"
											rendered="#{Metadaten.modusAnsicht != 'Paginierung'}" accesskey="P">
											<f:param id="vdid5" name="Ansicht" value="Paginierung" />
										</h:commandLink>
										<h:outputText id="vdid6" value="#{msgs.paginierung}" style="font-weight:bold;font-size: 11px"
											rendered="#{Metadaten.modusAnsicht == 'Paginierung'}" />
										<h:outputText id="vdid7" value=" | " style="font-size: 11px;" />

										<h:commandLink id="vdid8" action="#{Metadaten.AnsichtAendern}" value="#{msgs.strukturdaten}" style="font-size: 11px;"
											rendered="#{Metadaten.modusAnsicht != 'Strukturdaten'}" accesskey="S">
											<f:param id="vdid9" name="Ansicht" value="Strukturdaten" />
										</h:commandLink>

										<h:outputText id="vdid10" value="#{msgs.strukturdaten}" style="font-weight:bold;font-size: 11px;"
											rendered="#{Metadaten.modusAnsicht == 'Strukturdaten'}" />
										<h:outputText id="vdid11" value=" | " style="font-size: 11px;" />

										<h:commandLink id="vdid12" action="#{Metadaten.AnsichtAendern}" value="#{msgs.metadaten}" style="font-size: 11px;"
											rendered="#{Metadaten.modusAnsicht != 'Metadaten'}" accesskey="M">
											<f:param id="vdid13" name="Ansicht" value="Metadaten" />
										</h:commandLink>

										<h:outputText id="vdid14" value="#{msgs.metadaten}" style="font-weight:bold;font-size: 11px;"
											rendered="#{Metadaten.modusAnsicht == 'Metadaten'}" />
										<h:outputText value=" | " style="font-size: 11px;" rendered="#{Metadaten.displayFileManipulation}" />
										
										<h:commandLink id="vdid16" action="#{Metadaten.AnsichtAendern}" value="#{msgs.fileManipulation}" style="font-size: 11px;"
											rendered="#{Metadaten.modusAnsicht != 'File' && Metadaten.displayFileManipulation}">
											<f:param id="vdid17" name="Ansicht" value="File" />
										</h:commandLink >

										<h:outputText id="vdid18" value="#{msgs.fileManipulation}" style="font-weight:bold;font-size: 11px;"
											rendered="#{Metadaten.modusAnsicht == 'File' && Metadaten.displayFileManipulation}" />

									</h:panelGroup>
								</htm:td>

								<htm:td align="right">
									<h:commandLink action="#{Metadaten.BildAnzeigen}" rendered="#{Metadaten.bildAnzeigen==true}" value="#{msgs.metadatenBildAusblenden}"
										style="#{Metadaten.nurLesenModus ? 'font-size:11px':'font-size:11px;'}" />
									<h:commandLink action="#{Metadaten.BildAnzeigen}" rendered="#{Metadaten.bildAnzeigen==false}" value="#{msgs.metadatenBildAnzeigen}"
										style="#{Metadaten.nurLesenModus ? 'font-size:11px':'font-size:11px;'}" />
									<h:outputText value=" | " style="font-size: 11px;" />
									<%-- Metadaten validieren --%>
									<h:commandLink style="font-size:11px" action="#{Metadaten.Validate}" value="#{msgs.validieren}" rendered="#{not Metadaten.nurLesenModus}" />
									<h:outputText value=" | " style="font-size: 11px;" rendered="#{not Metadaten.nurLesenModus}" />
									<%-- Metadaten nicht schreiben, nur zurueck gehen--%>
									<h:commandLink style="font-size:11px" action="#{Metadaten.goZurueck}" value="#{msgs.goBack}" target="_parent" immediate="true"
										id="returnButton" />
									<h:outputText value=" | " style="font-size: 11px;" rendered="#{not Metadaten.nurLesenModus}" />
									<%-- Metadaten schreiben --%>
									<h:commandLink style="font-size:11px" action="#{Metadaten.Reload}" value="#{msgs.speichern}" rendered="#{not Metadaten.nurLesenModus}"
										immediate="false" />
									<h:outputText value=" | " style="font-size: 11px;" rendered="#{not Metadaten.nurLesenModus}" />
									<%-- Metadaten nicht schreiben, nur zurueck gehen--%>
									<h:commandLink style="font-size:11px" target="_parent" action="#{Metadaten.XMLschreiben}" value="#{msgs.speichernZurueck}"
										rendered="#{not Metadaten.nurLesenModus}" immediate="false" />
								</htm:td>
							</htm:tr>
						</htm:table>
					</htm:td>
				</htm:tr>
			</htm:table>
		</h:form>
		<br />
		<htm:table id="metadatenRechts" cellpadding="2" cellspacing="0" style="width:100%;height:100%; top:20px;margin-top:20px;margin-bottom:20px;">

			<htm:tr rendered="#{SessionForm.bitteAusloggen!=''}">
				<htm:td>
					<x:div style="border: 2px solid black; padding:7px; background-color: #ffd;position: fixed;top: 20px;left: 20px;">
						<h:outputText value="#{SessionForm.bitteAusloggen}" style="color: red;font-weight: bold;font-size:30px" />
					</x:div>
				</htm:td>
			</htm:tr>


			<htm:tr valign="top" style="width:100%;height:100%">
				<htm:td>

					<h:panelGroup id="myMessages">
						<%-- globale Warn- und Fehlermeldungen --%>
						<h:messages globalOnly="true" errorClass="text_red" infoClass="text_blue" showDetail="true" showSummary="true" tooltip="true" />
					</h:panelGroup>

					<%-- ########################################

                     				die einzelnen Details anzeigen, je nach Auswahl

      				 #########################################--%>
					<h:form id="formular2">
						<h:panelGroup rendered="#{Metadaten.modusAnsicht == 'Metadaten'}">
							<%@include file="incMeta/NeuMeta.jsp"%>
							<%@include file="incMeta/NeuPerson.jsp"%>
							<%@include file="incMeta/PersonenUndMetadaten.jsp"%>
						</h:panelGroup>

						<h:panelGroup rendered="#{Metadaten.modusAnsicht =='Strukturdaten'}">
							<htm:h3 style="margin-top:10px">
								<h:outputText value="#{msgs.strukturdatenBearbeiten}" rendered="#{not Metadaten.modusStrukturelementVerschieben}" />
								<h:outputText value="#{msgs.docstructAnAndereStelleSchieben}" rendered="#{Metadaten.modusStrukturelementVerschieben}" />
							</htm:h3>
							<%@include file="incMeta/Strukturdaten.jsp"%>
						</h:panelGroup>

						<h:panelGroup rendered="#{Metadaten.modusAnsicht =='Paginierung'}">
							<htm:h3 style="margin-top:10px">
								<h:outputText value="#{msgs.paginierungBearbeiten}" />
							</htm:h3>
							<%@include file="incMeta/Paginierung.jsp"%>
						</h:panelGroup>


					</h:form>
					<h:panelGroup rendered="#{Metadaten.modusAnsicht =='File'}">

						<%@include file="incMeta/File.jsp"%>
					</h:panelGroup>
				</htm:td>

				<htm:td rendered="#{Metadaten.bildAnzeigen==true}" style="top: 30px;">

					<h:panelGroup id="BildArea2" style="#{Metadaten.treeProperties.imageSticky?'position: fixed;top: 30px;left: 600px;':''}">
						<%@include file="incMeta/Bild.jsp"%>
					</h:panelGroup>

				</htm:td>
			</htm:tr>

			<htm:tr>
				<htm:td styleClass="main_statistikboxen_row3" height="1px" colspan="2">
					<h:form id="formular4" style="margin:0px">
						<htm:table styleClass="main_statistikboxen_bottom" width="100%" cellspacing="0" cellpadding="0" style="">
							<htm:tr>
								<htm:td align="right" style="padding-right:5px;">
									<%-- Metadaten validieren --%>
									<h:commandLink style="font-size:11px" action="#{Metadaten.Validate}" value="#{msgs.validieren}" rendered="#{not Metadaten.nurLesenModus}" />
									<h:outputText value=" | " style="font-size: 11px;" rendered="#{not Metadaten.nurLesenModus}" />
									<%-- Metadaten nicht schreiben, nur zurueck gehen--%>
									<h:commandLink style="font-size:11px" action="#{Metadaten.goZurueck}" value="#{msgs.goBack}" target="_parent" />
									<h:outputText value=" | " style="font-size: 11px;" rendered="#{not Metadaten.nurLesenModus}" />
									<%-- Metadaten schreiben --%>
									<h:commandLink style="font-size:11px" action="#{Metadaten.Reload}" value="#{msgs.speichern}" rendered="#{not Metadaten.nurLesenModus}"
										immediate="false" />
									<h:outputText value=" | " style="font-size: 11px;" rendered="#{not Metadaten.nurLesenModus}" />
									<%-- Metadaten nicht schreiben, nur zurueck gehen--%>
									<h:commandLink style="font-size:11px" action="#{Metadaten.XMLschreiben}" rendered="#{not Metadaten.nurLesenModus}"
										value="#{msgs.speichernZurueck}" target="_parent" />
								</htm:td>
							</htm:tr>
						</htm:table>
						<h:inputHidden id="DatenGeaendert" value="0" />
					</h:form>

				</htm:td>
			</htm:tr>
		</htm:table>
	</body>
</f:view>

<script type="text/javascript">
<!--
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

		//var treereloadelement = parent.oben.document.getElementById("treeReload");
		////alert(treereloadelement);
		//if (treereloadelement!=null){
		//alert(treereloadelement.value);
		//alert(treereloadelement.value=="");
		//if (treereloadelement.value!=""){
		//	alert("jetzt wird reloaded");
		//	treereloadelement.value ="";
		//	// parent.oben.document.getElementById("formularOben:treeReloadButton").click();
		//	var mybutton = parent.oben.document.getElementById("formularOben:treeReloadButton");
		//	alert (mybutton);
		//	mybutton.click();
		//	alert("reloaded");
		//}
		//}

		// alert("hallo " + document.getElementById("secretElement").value);
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

	document.documentElement.onkeypress = function(event) {
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
//-->
</script>
</html>
