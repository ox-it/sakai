package uk.ac.ox.it.shoal;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.bean.validation.BeanValidationConfiguration;
import org.apache.wicket.resource.loader.ClassStringResourceLoader;
import org.apache.wicket.resource.loader.PackageStringResourceLoader;
import uk.ac.ox.it.shoal.pages.EditPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Main application class for our app
 *
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 */
public class EditApplication extends SakaiApplication {


    public static final MetaDataKey<List<String>> SUBJECT = new MetaDataKey<List<String>>() {
    };
    public static final MetaDataKey<List<String>> LEVEL = new MetaDataKey<List<String>>() {
    };
    public static final MetaDataKey<List<String>> PURPOSE = new MetaDataKey<List<String>>() {
    };
    public static final MetaDataKey<List<String>> INTERACTIVITY = new MetaDataKey<List<String>>() {
    };
    public static final MetaDataKey<List<String>> TYPE = new MetaDataKey<List<String>>() {
    };

    /**
     * Configure your app here
     */
    @Override
    protected void init() {
        super.init();
        mountPage("/", EditPage.class);
        mountPage("/edit", EditPage.class);
        getResourceSettings().getStringResourceLoaders().add(new ClassStringResourceLoader(EditApplication.class));
        getResourceSettings().getStringResourceLoaders().add(new PackageStringResourceLoader()) ;
        setMetaData(SUBJECT, readFile("/subjects.txt"));
        setMetaData(LEVEL, readFile("/levels.txt"));
        setMetaData(PURPOSE, readFile("/purposes.txt"));
        setMetaData(INTERACTIVITY, readFile("/interactivity.txt"));
        setMetaData(TYPE, readFile("/types.txt"));
        new BeanValidationConfiguration().configure(this);

    }

    /**
     * The main page for our app
     *
     * @see org.apache.wicket.Application#getHomePage()
     */
    public Class<? extends Page> getHomePage() {
        return EditPage.class;
    }

    @Override
    public boolean isToolbarEnabled() {
        return false;
    }


    /**
     * O
     * Reads a file into a list of Strings (trimmed).
     *
     * @param path The path to the file on the classpath.
     * @return A list of strings, one for each line.
     */
    public List<String> readFile(String path) {
        ArrayList<String> list = new ArrayList<>();
        try (Scanner s = new Scanner(getClass().getResourceAsStream(path))) {
            while (s.hasNextLine()) {
                list.add(s.nextLine().trim());
            }
        }
        return list;
    }


}
