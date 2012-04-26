package com.taskadapter.launcher;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import java.awt.*;
import java.net.URI;

public class TALauncher {

    static final String PARAMETER_OPEN_TASK_ADAPTER_PAGE_IN_WEB_BROWSER = "--openTaskAdapterPageInWebBrowser";

    private static final int DEFAULT_HTTP_SERVER_PORT = 8080;
    private static final String WEB_APPLICATION_ROOT_CONTEXT = "ta";
    private static final String WAR_FILE = "files/ta.war";

    public static void main(String[] args) {

        int portNumber = findPortNumberInArgs(args);
        System.out.println("Starting HTTP server on port " + portNumber);

        final Server server = new Server(portNumber);
        server.setHandler(new WebAppContext(WAR_FILE, "/" + WEB_APPLICATION_ROOT_CONTEXT));

        try {
            server.start();
            while (!server.isStarted() || !server.getHandler().isStarted()) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    //logger.error(e);
                }
            }
            String uri = "http://localhost:" + portNumber + "/" + WEB_APPLICATION_ROOT_CONTEXT;
            System.out.println("=======================================================================");
            System.out.println("Task Adapter is started as a WEB-server running on port " + portNumber);
            System.out.println("Please OPEN your web browser with this URL: " + uri);
            System.out.println("=======================================================================");

            if (needToOpenBrowser(args)) {
                Desktop desktop = Desktop.getDesktop();
                desktop.browse(new URI(uri));
            } else {
                System.out.println("Task Adapter launcher will open the browser automatically if you provide this parameter in the start script: " + PARAMETER_OPEN_TASK_ADAPTER_PAGE_IN_WEB_BROWSER);
            }
        } catch (Exception e) {
            System.out.println("Error starting server: " + e.toString());
        }
    }

    static int findPortNumberInArgs(String[] args) {
        for (String arg : args) {
            String prefix = "--port=";
            if (arg.startsWith(prefix)) {
                String portString = arg.substring(prefix.length());
                return Integer.parseInt(portString);
            }
        }
        return DEFAULT_HTTP_SERVER_PORT;
    }

    static boolean needToOpenBrowser(String[] args) {
        for (String arg : args) {
            if (arg.equals(PARAMETER_OPEN_TASK_ADAPTER_PAGE_IN_WEB_BROWSER)) {
                return true;
            }
        }
        return false;
    }
}
