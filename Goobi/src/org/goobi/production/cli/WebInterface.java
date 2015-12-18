package org.goobi.production.cli;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- https://github.com/goobi/goobi-production
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.ICommandPlugin;
import org.goobi.production.plugin.interfaces.IPlugin;

import de.sub.goobi.config.ConfigMain;

public class WebInterface extends HttpServlet {
	private static final Logger logger = Logger.getLogger(WebInterface.class);
	private static final long serialVersionUID = 6187229284187412768L;

	private String command = null;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/html");
		if (ConfigMain.getBooleanParameter("useWebApi", false)) {
			String ip = "";
			String password = "";
			try {
				ip = req.getRemoteHost();
				if (ip.startsWith("127.0.0.1")) {
					ip = req.getHeader("x-forwarded-for");
					if (ip == null) {
						ip = "127.0.0.1";
					}
				}
				
				Map<String, String[]> map = req.getParameterMap();
				String[] pwMap = map.get("token");
				password = pwMap[0];
			} catch (Exception e) {
				resp.setContentType("");
				generateAnswer(resp, 401, "Internal error", "Missing credentials");
				return;

			}

			Map<String, String[]> parameter = req.getParameterMap();
			// command
			if (parameter.size() == 0) {
				generateAnswer(resp, 400,"Empty request", "no parameters given");
				return;
			}
			if (parameter.get("command") == null) {
				// error, no command found
				generateAnswer(resp, 400, "Empty command", "no command given");
				return;
			}

			this.command = parameter.get("command")[0];
			if (this.command == null) {
				// error, no command found
				generateAnswer(resp, 400,"Empty command", "No command given. Use help as command to get more information.");
				return;
			}
			if(logger.isDebugEnabled()){
				logger.debug("command: " + this.command);
			}

			// check if command is allowed for used IP
			List<String> allowedCommandos = WebInterfaceConfig.getCredencials(ip, password);
			if (!allowedCommandos.contains(this.command)) {
				// error, no command found
				generateAnswer(resp, 401, "command not allowed", "command " + this.command + " not allowed for your IP (" + ip + ")");
				return;
			}

			if (this.command.equals("help")) {
				generateHelp(resp);
				return;
			}
			
			
			// get correct plugin from list
			ICommandPlugin myCommandPlugin = (ICommandPlugin) PluginLoader.getPluginByTitle(PluginType.Command, this.command);
			if (myCommandPlugin == null) {
				generateAnswer(resp, 400, "invalid command", "command not found in list of command plugins");
				return;
			}

			// hand parameters over to command
			Map<String, String[]> map = req.getParameterMap();
			HashMap<String, String> params = new HashMap<String, String>();
			Iterator<Entry<String, String[]>> i = map.entrySet().iterator();
			while (i.hasNext()) {
				Entry<String, String[]> entry =  i.next();
				if (entry.getValue()[0] != null) {
					params.put(entry.getKey(), entry.getValue()[0]);
				}
			}
			myCommandPlugin.setParameterMap(params);

			// let command validate if all parameters are correct: null means valid
			CommandResponse cr = myCommandPlugin.validate();
			if (cr != null) {
				generateAnswer(resp, cr);
				return;
			}

			// no validation errors, so call the command
			if (myCommandPlugin.usesHttpSession()) {
				myCommandPlugin.setHttpResponse(resp);
			}
			cr = myCommandPlugin.execute();
			generateAnswer(resp, cr.getStatus(), cr.getTitle(), cr.getMessage());
			return;

		} else {
			generateAnswer(resp, 404, "web api deactivated", "web api not configured");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	private void generateHelp(HttpServletResponse resp) throws IOException {
		String allHelp = "";
		List<IPlugin> mycommands = PluginLoader.getPluginList(PluginType.Command);
		for (IPlugin iPlugin : mycommands) {
			ICommandPlugin icp = (ICommandPlugin) iPlugin;
			allHelp += "<h4>" + icp.help().getTitle() + "</h4>" + icp.help().getMessage() + "<br/><br/>";
		}
		generateAnswer(resp, 200, "Goobi Web API Help", allHelp);
	}

	private void generateAnswer(HttpServletResponse resp, int status, String title, String message) throws IOException {
		generateAnswer(resp, new CommandResponse(status, title, message));
	}

	private void generateAnswer(HttpServletResponse resp, CommandResponse cr) throws IOException {
		resp.setStatus(cr.getStatus());
		String answer = "";
		answer += "<html><head></head><body>";
		answer += "<h3>";
		answer += cr.getTitle();
		answer += "</h3>";
		answer += cr.getMessage();
		answer += "</body></html>";
		resp.getOutputStream().print(answer);
	}

}
