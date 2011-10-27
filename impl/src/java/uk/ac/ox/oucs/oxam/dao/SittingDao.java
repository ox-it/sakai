package uk.ac.ox.oucs.oxam.dao;

import java.util.List;

import uk.ac.ox.oucs.oxam.model.Sitting;

public interface SittingDao {

	public Sitting getSitting(long id);
	
	public List<Sitting> getSittings(int start, int length);
	
	public void saveSitting(Sitting sitting);
}
