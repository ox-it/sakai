package uk.ac.ox.oucs.vle;

import org.sakaiproject.memory.api.Cache;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class Utils {

    static Cache<Object, Object> mockCache() {
        Cache<Object, Object> cache = mock(Cache.class);
        final Map<Object, Object> map = new HashMap<>();
        doAnswer(invocation -> {
            Object[] arguments = invocation.getArguments();
            map.put(arguments[0], arguments[1]);
            return Void.TYPE;
        }).when(cache).put(any(), any());
        doAnswer(invocation -> {
            Object[] arguments = invocation.getArguments();
            return map.get(arguments[0]);
        }).when(cache).get(any());
        return cache;
    }

}
