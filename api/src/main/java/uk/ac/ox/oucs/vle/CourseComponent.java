package uk.ac.ox.oucs.vle;

import java.util.Date;

public interface CourseComponent {

	public String getPresentationId();
	
	public String getSubject();
	
	public String getTitle();
	
	public int getPlaces();
	
	public int getSize();
	
	public Person getPresenter();
	
	public String getLocation();
	
	public Date getOpens();
	
	public String getOpensText();
	
	public Date getCloses();
	
	public String getClosesText();
	
	public Date getStarts();
	
	public String getStartsText();
	
	public Date getEnds();
	
	public String getEndsText();
	
	public Date getCreated();
	
	public Date getBaseDate();
	
	public String getSlot();
	
	public String getWhen();
	
	public String getSessions();
	
	public boolean getBookable();
	
	public String getApplyTo();
	
	public String getMemberApplyTo();

	/**
	 * The ID of the component set that this component belongs to.
	 * This is used when there are multiple copies of a component running at once to discover
	 * which ones the user can select from.
	 * @return
	 */ 
	public String getComponentSet();
	
}
