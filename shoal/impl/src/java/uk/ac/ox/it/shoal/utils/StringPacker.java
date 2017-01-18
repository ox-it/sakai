package uk.ac.ox.it.shoal.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This just packs multiple strings into a longer string and then unpacks them again.
 * This was created because we need to store collections of strings in string properties.
 * It initially looked like ResourceProperties could do this but there is just a note
 * saying it would be good to support in the future.
 */
public class StringPacker {

    static String SEP = "|";
    static String ESC = "\\";

    public static String pack(Collection<String> items) {
        if (items == null) {
            return null;
        }
        return items.stream().map(i -> i.replace(ESC, ESC+ESC).replace(SEP, ESC+SEP)).collect(Collectors.joining(SEP)) ;
    }

    public static Collection<String> unpack(String packed) {
        if (packed == null) {
            return null;
        }
        // This looks for an event number of backslashes not preceeded by a backslash
        Matcher matcher = Pattern.compile("("+Pattern.quote(ESC)+"*)"+ Pattern.quote(SEP)).matcher(packed);
        Collection<String> found = new ArrayList<>();
        int start = 0;
        while(matcher.find()) {
            String escapes = matcher.group(1);
            // It's a greedy matcher so if the seperator is escaped ignore it.
            if (escapes.length() % 2 == 0) {
                int stop = matcher.end() - SEP.length();
                String value = packed.substring(start, stop);
                found.add(value.replace(ESC+SEP, SEP).replace(ESC+ESC, ESC));
                start = matcher.end();
            }
        }
        // Add the last one
        found.add(packed.substring(start, packed.length()).replace(ESC+SEP, SEP).replace(ESC+ESC, ESC));
        return found;
    }
}
