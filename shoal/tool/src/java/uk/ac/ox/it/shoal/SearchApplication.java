package uk.ac.ox.it.shoal;

import de.agilecoders.wicket.core.Bootstrap;
import org.apache.wicket.Page;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.resource.FileSystemResource;
import org.apache.wicket.resource.loader.ClassStringResourceLoader;
import uk.ac.ox.it.shoal.pages.DisplayPage;
import uk.ac.ox.it.shoal.pages.SimpleSearchPage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SearchApplication extends SakaiApplication {


    /**
     * Configure your app here
     */
    @Override
    protected void init() {
        super.init();

        Bootstrap.install(this);

        //to put this app into deployment mode, see web.xml
        mountPage("/", SimpleSearchPage.class);
        mountPage("/display", DisplayPage.class);

        getResourceSettings().getStringResourceLoaders().add(new ClassStringResourceLoader(SearchApplication.class));

        getSharedResources().add("thumbnails", new FolderContentResource(Paths.get(System.getProperty("java.io.tmpdir"))));
        mountResource("/thumbnail/${filename}", new SharedResourceReference("thumbnails"));


    }

    /**
     * This serves up any file inside the root folder.
     * TODO This doesn't handle 404s correctly or well
     */
    static class FolderContentResource extends FileSystemResource {
        private final Path rootFolder;

        public FolderContentResource(Path rootFolder) {
            this.rootFolder = rootFolder;
        }

        public void respond(Attributes attributes) {
            PageParameters parameters = attributes.getParameters();
            String filename = parameters.get("filename").toString();
            Path fileName = Paths.get(filename);
            // This shouldn't be needed as we are only getting one part of the URL.
            if (fileName.getRoot() != null || fileName.isAbsolute()) {
                throw new IllegalArgumentException("Can only pass relative paths.");
            }
            Path file = rootFolder.resolve(fileName);
            if (Files.isReadable(file)) {
                // This streams the file and sets the HTTP Headers correctly
                new FileSystemResource(file).respond(attributes);
            } else {
                throw new AbortWithHttpErrorCodeException(404, "Could not find image.");
            }
        }
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return SimpleSearchPage.class;
    }

    @Override
    public boolean isToolbarEnabled() {
        return false;
    }



}
