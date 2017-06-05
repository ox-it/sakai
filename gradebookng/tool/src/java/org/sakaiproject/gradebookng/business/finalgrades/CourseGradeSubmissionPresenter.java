package org.sakaiproject.gradebookng.business.finalgrades;

import javax.servlet.http.HttpServletResponse;
import org.sakaiproject.gradebookng.business.model.GbGroup;

/**
 * Interfaces CourseGradeSubmitter with presentation framework for better isolation
 * @author plukasew
 */
public class CourseGradeSubmissionPresenter
{
	// OWLTODO: convert all this to wicket
	
	public static String getUserIp()
	{
		// OWLTODO: convert to wicket, trim and return empty string if not found
		//userIp = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getRemoteAddr();
		return "127.0.0.1";
	}
	
	public static void presentError(String msg)
	{
        //FacesUtil.addErrorMessage("Please select a valid Registrar's section before submitting grades.");
	}
	
	public static void presentMsg(String msg)
	{
		// FacesUtil.addMessage(message);
		
	}
	
	public static GbGroup getSelectedSection()
	{
		// OWLTODO: get the selected section from the UI
		return GbGroup.all("ALL");
	}
	
	public static String getSelectedSectionUid()
	{
		// OWLTODO: get the selected section from the UI
		return "";
	}
	
	public static String getSelectedSectionEid()
	{
		// OWLTODO: get the selected section from the UI
		return "";
	}
	
	public static HttpServletResponse getServletResponse()
	{
		// FacesContext fc = FacesContext.getCurrentInstance();
        // HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();
		return null;
	}
	
	public static void completeResponse()
	{
		//FacesContext.getCurrentInstance().responseComplete();
	}
	
	public static String getLocalizedString(String key)
	{
		return "";
	}
	
	public static String getLocalizedString(String key, String[] params)
	{
		return "";
	}
}
