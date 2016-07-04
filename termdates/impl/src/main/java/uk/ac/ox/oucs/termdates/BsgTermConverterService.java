package uk.ac.ox.oucs.termdates;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.joda.time.DateTime;

/**
 * @author Colin Hebert
 */
public class BsgTermConverterService extends AbstractTermConverterService {

    private static final BiMap<Integer, String> WEEK_NAMES = HashBiMap.create(53);
    private static final DateTime REFERENTIAL = new DateTime(2011, 9, 12, 0, 0);


    public BsgTermConverterService() {
        int i = 0;
        WEEK_NAMES.put(i++, "Presession 1/Michaelmas -2");
        WEEK_NAMES.put(i++, "Presession 2/Michaelmas -1");
        WEEK_NAMES.put(i++, "Michaelmas 0");
        WEEK_NAMES.put(i++, "Michaelmas 1");
        WEEK_NAMES.put(i++, "Michaelmas 2");
        WEEK_NAMES.put(i++, "Michaelmas 3");
        WEEK_NAMES.put(i++, "Michaelmas 4");
        WEEK_NAMES.put(i++, "Michaelmas 5");
        WEEK_NAMES.put(i++, "Michaelmas 6");
        WEEK_NAMES.put(i++, "Michaelmas 7");
        WEEK_NAMES.put(i++, "Michaelmas 8");
        WEEK_NAMES.put(i++, "Michaelmas 9");
        WEEK_NAMES.put(i++, "Michaelmas 10");
        WEEK_NAMES.put(i++, "Michaelmas 11");
        WEEK_NAMES.put(i++, "Michaelmas 12");
        WEEK_NAMES.put(i++, "Hilary -1");
        WEEK_NAMES.put(i++, "Hilary 0");
        WEEK_NAMES.put(i++, "Hilary 1");
        WEEK_NAMES.put(i++, "Hilary 2");
        WEEK_NAMES.put(i++, "Hilary 3");
        WEEK_NAMES.put(i++, "Hilary 4");
        WEEK_NAMES.put(i++, "Hilary 5");
        WEEK_NAMES.put(i++, "Hilary 6");
        WEEK_NAMES.put(i++, "Hilary 7");
        WEEK_NAMES.put(i++, "Hilary 8");
        WEEK_NAMES.put(i++, "Hilary 9");
        WEEK_NAMES.put(i++, "Hilary 10");
        WEEK_NAMES.put(i++, "Hilary 11");
        WEEK_NAMES.put(i++, "Hilary 12");
        WEEK_NAMES.put(i++, "Trinity -1");
        WEEK_NAMES.put(i++, "Trinity 0");
        WEEK_NAMES.put(i++, "Trinity 1");
        WEEK_NAMES.put(i++, "Trinity 2");
        WEEK_NAMES.put(i++, "Trinity 3");
        WEEK_NAMES.put(i++, "Trinity 4");
        WEEK_NAMES.put(i++, "Trinity 5");
        WEEK_NAMES.put(i++, "Trinity 6");
        WEEK_NAMES.put(i++, "Trinity 7");
        WEEK_NAMES.put(i++, "Trinity 8");
        WEEK_NAMES.put(i++, "Trinity 9");
        WEEK_NAMES.put(i++, "Trinity 10");
        WEEK_NAMES.put(i++, "Trinity 11");
        WEEK_NAMES.put(i++, "Trinity 12");
        WEEK_NAMES.put(i++, "Summer 1");
        WEEK_NAMES.put(i++, "Summer 2");
        WEEK_NAMES.put(i++, "Summer 3");
        WEEK_NAMES.put(i++, "Summer 4");
        WEEK_NAMES.put(i++, "Summer 5");
        WEEK_NAMES.put(i++, "Summer 6");
        WEEK_NAMES.put(i++, "Summer 7");
        WEEK_NAMES.put(i++, "Summer 8");
        WEEK_NAMES.put(i++, "Summer 9");
        WEEK_NAMES.put(i++, "Summer 10");
    }

    @Override
    public DateTime getReferential() {
        return REFERENTIAL;
    }

    @Override
    public BiMap<Integer, String> getWeekNames() {
        return WEEK_NAMES;
    }

}
