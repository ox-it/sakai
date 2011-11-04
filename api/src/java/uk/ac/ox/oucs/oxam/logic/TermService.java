package uk.ac.ox.oucs.oxam.logic;

import java.util.Collection;

import uk.ac.ox.oucs.oxam.model.Term;

public interface TermService {
	
	public Term getByCode(String code);
	
	public Collection<Term> getAll();

}
