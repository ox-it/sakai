package uk.ac.ox.oucs.oxam.readers;

import java.io.IOException;

import uk.ac.ox.oucs.oxam.logic.TermService;

public class ExtraZipPaperResolver extends ZipPaperResolver {

	public ExtraZipPaperResolver(String filePath, String zipPrefix,
			TermService termService, String extension) throws IOException {
		super(filePath, zipPrefix, termService, extension);
	}
	
	public PaperResolutionResult getPaper(int year, String termCode, String paperCode) {
		// This is for the extra ones.
		String filename = zipPrefix+ ZIP_SEPERATOR+ paperCode.toUpperCase()+"("+termCode+")."+ extension;
		return new Result(filename);
	}

}
