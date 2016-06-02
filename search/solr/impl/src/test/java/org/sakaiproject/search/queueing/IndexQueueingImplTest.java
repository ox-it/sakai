package org.sakaiproject.search.queueing;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.search.indexing.DefaultTask;
import org.sakaiproject.search.indexing.Task;
import org.sakaiproject.search.indexing.TaskHandler;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Colin Hebert
 */
public class IndexQueueingImplTest {
    private IndexQueueingImpl indexQueueing;
    @Mock
    private SecurityService mockSecurityService;
    @Mock
    private TaskHandler mockTaskHandler;
    @Mock
    private IndexQueueing mockIndexQueueing;
    @Mock
    private ThreadLocalManager mockThreadLocalManager;
    @Mock
    private ExecutorService mockTaskSplittingExecutor;
    @Mock
    private ExecutorService mockIndexingExecutor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        indexQueueing = new IndexQueueingImpl();
        indexQueueing.setIndexQueueing(mockIndexQueueing);
        indexQueueing.setTaskHandler(mockTaskHandler);
        indexQueueing.setSecurityService(mockSecurityService);
        indexQueueing.setThreadLocalManager(mockThreadLocalManager);
        indexQueueing.setIndexingExecutor(mockIndexingExecutor);
        indexQueueing.setTaskSplittingExecutor(mockTaskSplittingExecutor);
    }

    @Test
    public void testAddingNewSimpleTaskToQueue() {
        Task indexDocumentTask = mock(Task.class);
        when(indexDocumentTask.getType()).thenReturn(DefaultTask.Type.INDEX_DOCUMENT.getTypeName());
        Task removeDocumentTask = mock(Task.class);
        when(removeDocumentTask.getType()).thenReturn(DefaultTask.Type.REMOVE_DOCUMENT.getTypeName());

        indexQueueing.addTaskToQueue(indexDocumentTask);
        indexQueueing.addTaskToQueue(removeDocumentTask);

        verify(mockIndexingExecutor, times(2)).execute(any(Runnable.class));
    }

    @Test
    public void testAddingNewCompoundTaskToQueue() {
        Task task = mock(Task.class);
        when(task.getType()).thenReturn("anyString");

        indexQueueing.addTaskToQueue(task);

        verify(mockTaskSplittingExecutor).execute(any(Runnable.class));
    }

    @Test
    public void testAddedTaskIsExecuted() {
        Task task = mock(Task.class);
        when(task.getType()).thenReturn(DefaultTask.Type.INDEX_DOCUMENT.getTypeName());
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Runnable runnable = (Runnable) invocation.getArguments()[0];
                // We don't really need to spawn a new thread here.
                runnable.run();
                return null;
            }
        }).when(mockIndexingExecutor).execute(any(Runnable.class));

        indexQueueing.addTaskToQueue(task);

        verify(mockTaskHandler).executeTask(task);
    }
}
