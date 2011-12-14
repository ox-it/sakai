package uk.ac.ox.oucs.oxam.readers;

import java.io.IOException;

import uk.ac.ox.oucs.oxam.logic.TermService;

/**
 * This paper resolver is specifically for finding papers which have come across in 
 * an archive. The old OXAM service would use the ending year to store the paper,
 * so paper ABCD in academic year 1999-2000, ends up in the folder 2000.
 * 
 * @author buckett
 *
 */
public class ArchivePaperResolver extends ZipPaperResolver {

	public ArchivePaperResolver(String filePath, String zipPrefix,
			TermService termService, String extension) throws IOException {
		super(filePath, zipPrefix, termService, extension);
	}
	
	@Override
	public PaperResolutionResult getPaper(int year, String termCode, String paperCode) {
		return super.getPaper(year+1, termCode, paperCode);
	}

}
