package uk.ac.ox.oucs.vle;

import java.util.Date;

public class CourseGroupImpl implements CourseGroup {

	private CourseGroupDAO doa;
	
	public CourseGroupImpl(CourseGroupDAO doa) {
		this.doa = doa;
	}

	public String getDescription() {
		return doa.getProperties().get("description");
	}

	public String getId() {
		return doa.getId();
	}

	public int getPlaces() {
		return doa.getPlaces();
	}

	public Date getSignupCloses() {
		return doa.getCloses();
	}

	public Date getSignupOpens() {
		return doa.getOpens();
	}

	public int getSize() {
		for (CourseComponentDAO component: doa.getComponents()) {
			int size = component.getSize();
			
		}
		// Calculated.
		return 0;
	}

	public String getTitle() {
		return doa.getTitle();
	}

}
