package uk.ac.ox.it.vle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.api.SessionManager;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This is a servlet that is slowed down as more session are allocated to the server.
 */
@WebServlet(name="load",
        urlPatterns={"/*"},
        initParams={ })
public class LoadServlet extends HttpServlet {

    private final Log log = LogFactory.getLog(LoadServlet.class);

    private int sleepLimitDefault = 5000; // Don't sleep for more than 5 seconds.
    private int sessionAgeDefault = 1200; // 20 minutes.
    private int sessionSleepDefault = 10; // Sleep for 10ms per session.

    private SessionManager sessionManager;
    private ServerConfigurationService serverConfigurationService;

    @Override
    public void init() {
        sessionManager = (SessionManager) ComponentManager.get(SessionManager.class);
        serverConfigurationService = (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // This call doesn't include admin/postmaster users so don't get caught out in testing.
        int sessions = sessionManager.getActiveUserCount(getSessionAge());
        int delay = sessions * getSessionSleep();
        if (delay > getSleepLimit()) {
            log.info(String.format("Sleep has become capped at: %d with %d sessions.", getSleepLimit(), sessions));
            delay = getSleepLimit();
        }
        try {
            Thread.sleep(delay);
            resp.setStatus(HttpServletResponse.SC_OK);
            // Return the number of sessions to aid debugging.
            resp.getOutputStream().print(sessions);
        } catch (InterruptedException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.warn("Got woken up: "+ e.getMessage());
        }
    }

    private int getSleepLimit() {
        return getConfigInt("sleepLimitDefault", sleepLimitDefault);
    }

    private int getSessionAge() {
        return getConfigInt("sessionAgeDefault", sessionAgeDefault);
    }

    private int getSessionSleep() {
        return getConfigInt("sessionSleepDefault", sessionSleepDefault);
    }

    /**
     * Get an int from the configuration.
     * @param value The String to convert.
     * @param defaultValue The default value to return if it can't be converted.
     * @return The int value of a string.
     */
    int getConfigInt(String key, int defaultValue) {
        try {
            return serverConfigurationService.getInt("current-load."+ key, defaultValue);
        } catch(NumberFormatException nfe) {
            // Log exception.
            return defaultValue;
        }
    }
}
