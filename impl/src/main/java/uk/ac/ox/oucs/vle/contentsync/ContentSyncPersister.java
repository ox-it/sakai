package uk.ac.ox.oucs.vle.contentsync;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;

import uk.ac.ox.oucs.vle.contentsync.ContentSyncTableDAO;

public class ContentSyncPersister implements Runnable {
	
	private BlockingQueue<ContentSyncToken> queue;
	
	private ContentSyncDAO dao;
	
	public ContentSyncPersister(BlockingQueue<ContentSyncToken> q, ContentSyncDAO dao) { 
		this.queue = q; 
		this.dao = dao;
	}
	
	public void run() {
		
		try {
	        ContentSyncToken token = null;
	        while (!((token = queue.take())
	        		.getResourceEvent().equals(ContentSyncTracker.EVENT_RESOURCE_DONE))) {
	        	
	        	ContentSyncTableDAO resource = new ContentSyncTableDAO();
	        	resource.setEvent(token.getResourceEvent());
	        	resource.setReference(token.getResourceReference());
	        	resource.setContext(token.getResourceContext());
	        	resource.setTimeStamp(Calendar.getInstance().getTime());
	        	dao.save(resource);
	        	
	        }
	            
	    } catch (InterruptedException intEx) {
	        System.out.println("Interrupted! " + 
	                "Last one out, turn out the lights!");
	    }
	}

}
