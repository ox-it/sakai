package uk.ac.ox.oucs.oxam.readers;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.ox.oucs.oxam.logic.TermService;
import uk.ac.ox.oucs.oxam.model.Term;

public class MockTermService  implements TermService{

	Map<String, Term> terms = new LinkedHashMap<String, Term>();
	
	public MockTermService() {
		addTerm(new Term("H", "Hilary"));
		addTerm(new Term("M","Michaelmas"));
		addTerm(new Term("T","Trinity"));
	}
	
	private void addTerm(Term term) {
		terms.put(term.getCode(), term);
	}

	public Term getByCode(String code) {
		return terms.get(code);
	}

	public Collection<Term> getAll() {
		return terms.values();
	}

}
