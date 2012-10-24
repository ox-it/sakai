package uk.ac.ox.oucs.vle.contentsync;

import java.util.Date;
import java.util.List;

public interface ContentSyncDAO {

	void save(ContentSyncTableDAO resourceTrackerDao);
	
	List<ContentSyncTableDAO> findResourceTrackers(String context, Date timestamp);

}
