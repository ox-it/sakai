package uk.ac.ox.oucs.vle.contentsync;

import java.util.List;

import org.sakaiproject.time.api.Time;


public interface ContentSyncDAO {

	void save(ContentSyncTableDAO resourceTrackerDao);
	
	List<ContentSyncTableDAO> findResourceTrackers(final String context, final Time timestamp);

}
