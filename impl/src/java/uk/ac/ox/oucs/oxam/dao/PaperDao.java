package uk.ac.ox.oucs.oxam.dao;

import java.util.Map;

import uk.ac.ox.oucs.oxam.model.Paper;

public interface PaperDao {
	
	public Paper getPaper(long id);
	
	public Paper get(String code, int year);
	
	public Map<String, Paper> getCodes(String[] codes);

	public void savePaper(Paper paper) throws RuntimeException;	
}
