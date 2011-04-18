<%@ page session="false" contentType="text/html;charset=utf-8"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://myfaces.apache.org/sandbox" prefix="s"%>

<html>


<!--
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
//-->

<body>

<f:view locale="#{SpracheForm.locale}">

   <h:form id="testform2">

     <f:verbatim><br/><br/></f:verbatim>

     <t:dojoInitializer bindEncoding="utf-8"/>

     <h:panelGrid columns="9">
         <h:outputText value="default suggest"/>
      <s:inputSuggestAjax suggestedItemsMethod="#{Metadaten.getItems}"
                             value="#{Metadaten.ajaxSeiteStart}" charset="utf-8"/>

     </h:panelGrid>

    </h:form>
    
</f:view>

</body>

</html>

