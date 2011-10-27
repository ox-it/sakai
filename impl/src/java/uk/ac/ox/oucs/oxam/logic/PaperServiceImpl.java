package uk.ac.ox.oucs.oxam.logic;

import java.io.OutputStream;
import java.util.List;

import pom.logic.SakaiProxy;
import uk.ac.ox.oucs.oxam.dao.PaperDao;
import uk.ac.ox.oucs.oxam.model.Paper;

public class PaperServiceImpl implements PaperService {

	private PaperDao dao;
	private SakaiProxy proxy;
	
	public void setPaperDao(PaperDao dao) {
		this.dao = dao;
	}
	
	private void setSakaiProxy(SakaiProxy proxy) {
		this.proxy = proxy;
	}
	
	public Paper getPaper(long id) {
		return dao.getPaper(id);
	}

	public List<Paper> getPapers(int start, int length) {
		return dao.getPapers(start, length);
	}

	public void savePaper(Paper paper) throws RuntimeException {
		dao.savePaper(paper);
	}

	public String mapToFile(int year, String term, String paper) {
		
		return year+ "/"+ term.toLowerCase()+ "/"+ paper.toLowerCase();
	}

	public void depositFile(String path, Callback<OutputStream> callback) {
		proxy.depositFile(path, callback);
	}

}
