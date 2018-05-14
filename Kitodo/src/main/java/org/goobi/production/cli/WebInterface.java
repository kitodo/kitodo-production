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

package org.goobi.production.cli;

import de.sub.goobi.config.ConfigCore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.ICommandPlugin;
import org.goobi.production.plugin.interfaces.IPlugin;

@WebServlet(name = "WebInterface", urlPatterns = {"/wi" })
public class WebInterface extends HttpServlet {
    private static final Logger logger = LogManager.getLogger(WebInterface.class);
    private static final long serialVersionUID = 6187229284187412768L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("text/html");
        if (ConfigCore.getBooleanParameter("useWebApi", false)) {
            String ip;
            String password;
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
            } catch (RuntimeException e) {
                resp.setContentType("");
                generateAnswer(resp, 401, "Internal error", "Missing credentials");
                return;
            }

            Map<String, String[]> parameter = req.getParameterMap();
            // command
            if (parameter.size() == 0) {
                generateAnswer(resp, 400, "Empty request", "no parameters given");
                return;
            }
            if (parameter.get("command") == null) {
                // error, no command found
                generateAnswer(resp, 400, "Empty command", "no command given");
                return;
            }

            String command = parameter.get("command")[0];
            if (command == null) {
                // error, no command found
                generateAnswer(resp, 400, "Empty command",
                    "No command given. Use help as command to get more information.");
                return;
            }
            logger.debug("command: {}", command);

            // check if command is allowed for used IP
            List<String> allowedCommands = WebInterfaceConfig.getCredentials(ip, password);
            if (!allowedCommands.contains(command)) {
                // error, no command found
                generateAnswer(resp, 401, "command not allowed", "command " + StringEscapeUtils.escapeHtml(command)
                        + " not allowed for your IP (" + StringEscapeUtils.escapeHtml(ip) + ")");
                return;
            }

            if (command.equals("help")) {
                generateHelp(resp);
                return;
            }

            // get correct plugin from list
            ICommandPlugin myCommandPlugin = (ICommandPlugin) PluginLoader.getPluginByTitle(PluginType.COMMAND,
                command);
            if (myCommandPlugin == null) {
                generateAnswer(resp, 400, "invalid command", "command not found in list of command plugins");
                return;
            }

            // hand parameters over to command
            Map<String, String[]> map = req.getParameterMap();
            HashMap<String, String> params = new HashMap<>();
            for (Entry<String, String[]> entry : map.entrySet()) {
                if (entry.getValue()[0] != null) {
                    params.put(entry.getKey(), entry.getValue()[0]);
                }
            }
            myCommandPlugin.setParameterMap(params);

            // let command validate if all parameters are correct: null means
            // valid
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
        } else {
            generateAnswer(resp, 404, "web api deactivated", "web api not configured");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        doGet(req, resp);
    }

    private void generateHelp(HttpServletResponse resp) {
        StringBuilder allHelp = new StringBuilder();
        List<IPlugin> commands = PluginLoader.getPluginList(PluginType.COMMAND);
        for (IPlugin iPlugin : commands) {
            ICommandPlugin icp = (ICommandPlugin) iPlugin;
            allHelp.append("<h4>");
            allHelp.append(icp.help().getTitle());
            allHelp.append("</h4>");
            allHelp.append(icp.help().getMessage());
            allHelp.append("<br/><br/>");
        }
        generateAnswer(resp, 200, "Goobi Web API Help", allHelp.toString());
    }

    private void generateAnswer(HttpServletResponse resp, int status, String title, String message) {
        generateAnswer(resp, new CommandResponse(status, title, message));
    }

    private void generateAnswer(HttpServletResponse resp, CommandResponse cr) {
        resp.setStatus(cr.getStatus());
        String answer = "";
        answer += "<html><head></head><body>";
        answer += "<h3>";
        answer += cr.getTitle();
        answer += "</h3>";
        answer += cr.getMessage();
        answer += "</body></html>";
        try {
            resp.getOutputStream().print(answer);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
