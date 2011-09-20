package uk.ac.ox.oucs.vle;

import java.util.Collection;

public interface Department {
	
	public String getPracCode();
	
	public String getOucsCode();
		
	public String getName();
	
	public boolean getApprove();
	
	public Collection<String> getApprovers();

}
