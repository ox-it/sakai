package uk.ac.ox.oucs.vle;

public interface CourseComponent {

	public String getId();
	
	public String getTitle();
	
	public int getPlaces();
	
	public int getSize();
	
	public String getPresenter();
	
	public String getPresenterEmail();
	
	public String getLocation();
	
	/**
	 * The ID of the component set that this component belongs to.
	 * This is used when there are multiple copies of a component running at once to discover
	 * which ones the user can select from.
	 * @return
	 */ 
	public String getComponentSet();
}
