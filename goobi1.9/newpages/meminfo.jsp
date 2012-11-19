<%@ page import="java.lang.management.*"%>
<%@ page import="java.util.*"%>
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
<html>
<head>
<title>JVM Memory Monitor</title>
</head>

<body>

	<%
		Iterator iter = ManagementFactory.getMemoryPoolMXBeans().iterator();

		while (iter.hasNext()) {
			MemoryPoolMXBean itemA = (MemoryPoolMXBean) iter.next();
	%>

	<table border="0" width="100%">
		<tr>
			<td colspan="2" align="center"><h3>Memory MXBean</h3>
			</td>
		</tr>

		<tr>
			<td width="200">Heap Memory Usage</td>
			<td><%=ManagementFactory.getMemoryMXBean().getHeapMemoryUsage()%></td>
		</tr>

		<tr>
			<td>Non-Heap Memory Usage</td>
			<td><%=ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage()%></td>
		</tr>
		<tr>
			<td colspan="2">&nbsp;</td>
		</tr>
		<tr>
			<td colspan="2" align="center"><h3>Memory Pool MXBeans</h3>
			</td>
		</tr>

		<%
			iter = ManagementFactory.getMemoryPoolMXBeans().iterator();
				while (iter.hasNext()) {
					MemoryPoolMXBean item = (MemoryPoolMXBean) iter.next();
		%>
		<tr>
			<td colspan="2">
				<table border="0" width="100%" style="border: 1px #98AAB1 solid;">
					<tr>
						<td colspan="2" align="center"><b><%=item.getName()%></b>
						</td>
					</tr>
					<tr>
						<td width="200">Type</td>
						<td><%=item.getType()%></td>
					</tr>
					<tr>
						<td>Usage</td>
						<td><%=item.getUsage()%></td>
					</tr>
					<tr>
						<td>Peak Usage</td>
						<td><%=item.getPeakUsage()%></td>
					</tr>
					<tr>
						<td>Collection Usage</td>
						<td><%=item.getCollectionUsage()%></td>
					</tr>
				</table></td>
		</tr>
		<tr>
			<td colspan="2">&nbsp;</td>
		</tr>
		<%
			}
			}
		%>

	</table>
</body>
</html>



