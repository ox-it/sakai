package uk.ac.ox.oucs.oxam.logic;

import java.io.OutputStream;
import java.util.List;

import uk.ac.ox.oucs.oxam.dao.PaperDao;
import uk.ac.ox.oucs.oxam.model.Paper;

public class PaperServiceImpl implements PaperService {

	private PaperDao dao;
	private SakaiProxy proxy;
	
	public void setPaperDao(PaperDao dao) {
		this.dao = dao;
	}
	
	public void setSakaiProxy(SakaiProxy proxy) {
		this.proxy = proxy;
	}
	
	public Paper getPaper(long id) {
		return dao.getPaper(id);
	}

	public void savePaper(Paper paper) throws RuntimeException {
		dao.savePaper(paper);
	}

}
