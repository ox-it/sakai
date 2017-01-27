package uk.ac.ox.it.shoal.logic;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * This just re-indexs all shoal sites.
 */
public class IndexerJob implements Job {

    private Indexer indexer;

    public void setIndexer(Indexer indexer) {
        this.indexer = indexer;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        indexer.reIndex();
    }
}
