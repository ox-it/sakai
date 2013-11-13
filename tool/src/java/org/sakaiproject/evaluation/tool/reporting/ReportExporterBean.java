
package org.sakaiproject.evaluation.tool.reporting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.ReportingPermissions;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.viewparams.DownloadReportViewParams;
import uk.org.ponder.util.UniversalRuntimeException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 
 * @author Steven Githens
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ReportExporterBean {

    private static Log log = LogFactory.getLog(ReportExporterBean.class);

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private ReportingPermissions reportingPermissions;
    public void setReportingPermissions(ReportingPermissions perms) {
        this.reportingPermissions = perms;
    }
    
    private Map<String, ReportExporter> exportersMap;
    public void setExportersMap(Map<String, ReportExporter> exportersMap) {
        this.exportersMap = exportersMap;
    }

    public boolean export(DownloadReportViewParams drvp, HttpServletResponse response) {
        // get evaluation and template from DAO
        EvalEvaluation evaluation = evaluationService.getEvaluationById(drvp.evalId);

        // do a permission check
        if (!reportingPermissions.canViewEvaluationResponses(evaluation, drvp.groupIds)) {
            String currentUserId = commonLogic.getCurrentUserId();
            throw new SecurityException("Invalid user attempting to access report downloads: "
                    + currentUserId);
        }

        OutputStream resultsOutputStream = null;
        
        if( ! isXLS(drvp.viewID)){
	        ReportExporter exporter = exportersMap.get(drvp.viewID);
	
	        if (exporter == null) {
	            throw new IllegalArgumentException("No exporter found for ViewID: " + drvp.viewID);
	        }
	        if (log.isDebugEnabled()) {
	            log.debug("Found exporter: " + exporter.getClass() + " for drvp.viewID " + drvp.viewID);
	        }
	        
	        resultsOutputStream = getOutputStream(response);
	        
		    // All response Headers that are the same for all Output types
	        response.setHeader("Content-disposition", buildContentDisposition(drvp.filename));
		    response.setContentType(exporter.getContentType());
	        
	        exporter.buildReport(evaluation, drvp.groupIds, resultsOutputStream);
        }else{
        	XLSReportExporter xlsReportExporter = (XLSReportExporter) exportersMap.get(drvp.viewID);
        	resultsOutputStream = getOutputStream(response);
        	int columnSize = xlsReportExporter.getEvalTDIsize(evaluation, drvp.groupIds);
	        response.setHeader("Content-disposition", "inline");
	        if( columnSize > 255 ){
		        response.setHeader("Content-disposition", buildContentDisposition(drvp.filename + "x"));
			    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	        }else{
		        response.setHeader("Content-disposition", buildContentDisposition(drvp.filename));
			    response.setContentType( xlsReportExporter.getContentType() );
	        }	        
		    xlsReportExporter.buildReport(evaluation, drvp.groupIds, resultsOutputStream);
        }
        return true;
    }
    
    private boolean isXLS(String viewID){
    	return viewID.equals("xlsResultsReport");
    }

	/**
	 * This attempts to build the value of the content disposition header. It provides a ISO-8859-1 representation
	 * and a full UTF-8 version. This allows browser that understand the full version to use that and
	 * for mainly IE 8 the old limited one.
	 * @param filename The filename to encode
	 * @return The value of the content disposition header specifying it's inline content.
	 */
	public String buildContentDisposition(String filename) {
		try {
			// This will replace all non US-ASCII characters with '?'
			// Although this behaviour is unspecified doing it manually is overkill (too much work).
			// Make sure we escape double quotes.
			String iso8859Filename = new String(filename.getBytes("ISO-8859-1"))
					.replace("\\", "\\\\")
					.replace("\"", "\\\"");
			String utf8Filename = URLEncoder.encode(filename, "UTF-8").replace("+", "%20");
			return new StringBuilder()
					.append("inline; ")
					.append("filename=\"").append(iso8859Filename).append("\"; ")
					// For sensible browser give them a full UTF-8 encoded string.
					.append("filename*=UTF-8''").append(utf8Filename)
					.toString();
		} catch (UnsupportedEncodingException shouldNeverHappen) {
			throw new RuntimeException(shouldNeverHappen);
		}
	}
    
    private OutputStream getOutputStream(HttpServletResponse response){
    	try {
            return response.getOutputStream();
        } catch (IOException ioe) {
            throw UniversalRuntimeException.accumulate(ioe,
                    "Unable to get response stream for Evaluation Results Export");
        }
    }

}
