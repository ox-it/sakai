package org.sakaiproject.exporter.util;

public class AssignmentResource extends Resource {
	private String instructions;
	private boolean gradable;
	private boolean forPoints;
	private double maxPoints;
	private boolean allowText;
	private boolean allowFile;

	public String getInstructions() {
		return instructions;
	}

	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	public boolean isGradable() {
		return gradable;
	}

	public void setGradable(boolean gradable) {
		this.gradable = gradable;
	}

	public boolean isForPoints() {
		return forPoints;
	}

	public void setForPoints(boolean forPoints) {
		this.forPoints = forPoints;
	}

	public double getMaxPoints() {
		return maxPoints;
	}

	public void setMaxPoints(double maxPoints) {
		this.maxPoints = maxPoints;
	}

	public boolean isAllowText() {
		return allowText;
	}

	public void setAllowText(boolean allowText) {
		this.allowText = allowText;
	}

	public boolean isAllowFile() {
		return allowFile;
	}

	public void setAllowFile(boolean allowFile) {
		this.allowFile = allowFile;
	}
}
