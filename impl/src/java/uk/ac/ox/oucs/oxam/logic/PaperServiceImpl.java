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
	
	public void setSakaiProxy(SakaiProxy proxy) {
		this.proxy = proxy;
	}
	
	public Paper getPaper(long id) {
		return dao.getPaper(id);
	}

	public void savePaper(Paper paper) throws RuntimeException {
		dao.savePaper(paper);
	}

	public Upload mapToUpload(int year, String term, String paper) {
		
		return null;//  year+ "/"+ term.toLowerCase()+ "/"+ paper.toLowerCase();
	}

	public void depositUpload(Upload path, Callback<OutputStream> callback) {
		//proxy.depositFile(path, callback);
	}

	public boolean uploadExists(Upload upload) {
		// TODO Auto-generated method stub
		return false;
	}

}
