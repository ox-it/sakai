package uk.ac.ox.oucs.oxam.readers;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.ox.oucs.oxam.logic.TermService;
import uk.ac.ox.oucs.oxam.model.Term;

public class MockTermService  implements TermService{

	Map<String, Term> terms = new LinkedHashMap<String, Term>();
	
	public MockTermService() {
		addTerm(new Term("M","Michaelmas",1,false));
		addTerm(new Term("T","Trinity",2,true));
		addTerm(new Term("H", "Hilary",3,true));
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
