package org.sakaiproject.search.indexing;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * Simple hamcrest matcher checking that a Task has a specific type.
 *
 * @author Colin Hebert
 */
public class TaskMatcher extends BaseMatcher<Task> {
    private final String taskType;

    public TaskMatcher(String taskType) {
        this.taskType = taskType;
    }

    @Override
    public boolean matches(Object o) {
        if (!(o instanceof Task))
            return false;

        return taskType.equals(((Task) o).getType());
    }

    @Override
    public void describeTo(Description description) {
    }
}
