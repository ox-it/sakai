package uk.ac.ox.oucs.oxam.readers;

import java.io.IOException;

import uk.ac.ox.oucs.oxam.logic.TermService;

public class FlatZipPaperResolver extends ZipPaperResolver {

	public FlatZipPaperResolver(String filePath, String zipPrefix,
			TermService termService, String extension) throws IOException {
		super(filePath, zipPrefix, termService, extension);
	}
	
	public PaperResolutionResult getPaper(int year, String termCode, String paperCode) {
		// Doesn't deal with the extra onces.
		String filename = zipPrefix+ ZIP_SEPERATOR+ paperCode.toUpperCase()+"."+ extension;
		return new Result(filename);
	}

}
