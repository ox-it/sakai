package uk.ac.ox.it.vle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.api.Session;
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

    private final int SLEEP_LIMIT_DEFAULT = 5000; // Don't sleep for more than 5 seconds.
    private final int SESSION_AGE_DEFAULT = 1200; // 20 minutes.
    private final int SESSION_SLEEP_DEFAULT = 10; // Sleep for 10ms per session.

    private SessionManager sessionManager;
    private ServerConfigurationService serverConfigurationService;

    @Override
    public void init() {
        sessionManager = ComponentManager.get(SessionManager.class);
        serverConfigurationService = ComponentManager.get(ServerConfigurationService.class);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int sessions = 0;
        long now = System.currentTimeMillis();
        // This finds all the sessions that are logged in and have been accessed recently
        for (Session session: sessionManager.getSessions()) {
            if (session.getUserId() != null) {
                if ((now - session.getLastAccessedTime()) < (getSessionAge() * 1000))
                {
                    sessions++;
                }
            }
        }

        int delay = sessions * getSessionSleep();
        if (delay > getSleepLimit()) {
            log.error(String.format("Sleep has become capped at: %d with %d sessions.", getSleepLimit(), sessions));
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
        return getConfigInt("sleepLimitDefault", SLEEP_LIMIT_DEFAULT);
    }

    private int getSessionAge() {
        return getConfigInt("sessionAgeDefault", SESSION_AGE_DEFAULT);
    }

    private int getSessionSleep() {
        return getConfigInt("sessionSleepDefault", SESSION_SLEEP_DEFAULT);
    }

    /**
     * Get an int from the configuration.
     * @param key The String to convert.
     * @param defaultValue The default value to return if it can't be converted.
     * @return The int value of a string.
     */
    int getConfigInt(String key, int defaultValue) {
        return serverConfigurationService.getInt("current-load."+ key, defaultValue);
    }
}
