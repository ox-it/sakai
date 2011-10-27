package uk.ac.ox.oucs.oxam.dao;

import java.util.List;

import uk.ac.ox.oucs.oxam.model.Term;

public interface TermDao {

	public Term getTerm(long id);
	
	public List<Term> getTerms(int start, int length);
	
	public void saveTerm(Term term);
}
