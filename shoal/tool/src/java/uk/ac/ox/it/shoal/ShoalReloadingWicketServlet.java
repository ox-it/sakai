package uk.ac.ox.it.shoal;

import org.apache.wicket.application.ReloadingClassLoader;
import org.apache.wicket.protocol.http.ReloadingWicketServlet;
import org.apache.wicket.protocol.http.WicketServlet;

/**
 * Created by buckett on 09/12/2016.
 */
public class ShoalReloadingWicketServlet extends ReloadingWicketServlet {
    static
    {
        ReloadingClassLoader.includePattern("uk.ac.ox.it.vle.shoal.*");
        ReloadingClassLoader.excludePattern("org.apache.wicket.*");
    }
}
