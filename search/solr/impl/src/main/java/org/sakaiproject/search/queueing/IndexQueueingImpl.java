package org.sakaiproject.search.queueing;

import org.sakaiproject.search.indexing.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

import static org.sakaiproject.search.indexing.DefaultTask.Type.INDEX_DOCUMENT;
import static org.sakaiproject.search.indexing.DefaultTask.Type.REMOVE_DOCUMENT;

/**
 * Basic queueing system putting Tasks in an ExecutorService.
 * <p>
 * IndexQueueingImpl uses two ExecutorServices:
 * <ul>
 * <li>one to queue simple Tasks (index document, remove document from the index) that should be mostly busy</li>
 * <li>the second one is in charge of splitting big tasks (such as reindex everything) in smaller ones then queued in
 * the first Executor</li>
 * </ul>
 * This is done to avoid having only one queue filling itself while not being able to process new tasks<br />
 * ie. if there is a lot of new tasks being "reindex everything", having only one queue would cause troubles.
 * </p>
 * <p>
 * It is recommended to have a large queue and more threads for the indexingExecutor in charge of basic tasks.<br />
 * Usually one thread and a small queue should be enough for the TaskSplittingExecutor.
 * </p>
 * <p>
 * This implementation stores everything in memory, and while it's easier to setup, doesn't scale.<br />
 * The tasks are queued and executed on only one server, the memory consumption can get out of hand.<br />
 * An external queueing system (such as an AMQP server) will allow to dispatch tasks and will scale independently.
 * </p>
 *
 * @author Colin Hebert
 */
public class IndexQueueingImpl extends WaitingTaskRunner implements IndexQueueing {
    private static final Logger logger = LoggerFactory.getLogger(IndexQueueingImpl.class);
    private ExecutorService taskSplittingExecutor;
    private ExecutorService indexingExecutor;

    /**
     * Creates an {@link IndexQueueing} automatically coupled with a {@link TaskRunner}.
     */
    public IndexQueueingImpl() {
        setIndexQueueing(this);
    }

    /**
     * Shuts the queuing system down and forces the executors to stop.
     */
    public void destroy() {
        indexingExecutor.shutdownNow();
        taskSplittingExecutor.shutdownNow();
    }

    @Override
    public void addTaskToQueue(Task task) {
        if (INDEX_DOCUMENT.getTypeName().equals(task.getType())
                || REMOVE_DOCUMENT.getTypeName().equals(task.getType())) {
            logger.debug("Add task '{}' to the indexing executor", task);
            indexingExecutor.execute(new RunnableTask(task));
        } else {
            logger.debug("Add task '{}' to the task splitting executor", task);
            taskSplittingExecutor.execute(new RunnableTask(task));
        }
    }

    public void setIndexingExecutor(ExecutorService indexingExecutor) {
        this.indexingExecutor = indexingExecutor;
    }

    public void setTaskSplittingExecutor(ExecutorService taskSplittingExecutor) {
        this.taskSplittingExecutor = taskSplittingExecutor;
    }

    /**
     * Wrapper allowing Tasks to be run by an executor.
     */
    private final class RunnableTask implements Runnable {
        private final Task task;

        private RunnableTask(Task task) {
            this.task = task;
        }

        @Override
        public void run() {
            runTask(task);
        }
    }
}
