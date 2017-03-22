<%--
 *
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 *
--%>

<%@ page session="false" contentType="text/html;charset=utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://jsftutorials.net/htmLib" prefix="htm" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="x" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>
<f:view locale="#{SpracheForm.locale}">

    <%@include file="/newpages/inc/head.jsp" %>

    <script type="text/javascript">

        function checkFrameLoad() {

            var testObject = rechts.document.getElementById("metadatenRechts");
            if (testObject != null) {
            } else {
                rechts.location.href = rechts.location.href;
            }
            testObject = oben.document.getElementById("formularOben");
            if (testObject != null) {
            } else {
                oben.location.href = oben.location.href;
            }

            testObject = links.document.getElementById("treeform:tabelle");
            if (testObject != null) {
            } else {
                links.location.href = links.location.href;
            }
        }

    </script>

    <frameset rows="59px,*" bordercolor="#003399" onload="setTimeout('checkFrameLoad()',100);">
        <frame name="oben" src="../pages/Metadaten2oben.jsf" scrolling="no"/>
        <frameset cols="210px,*" bordercolor="#003399">
            <frame name="links" src="../pages/Metadaten3links.jsf"
                   scrolling="auto"/>
            <frame name="rechts" src="../pages/Metadaten2rechts.jsf"/>
        </frameset>
    </frameset>
</f:view>
</html>
