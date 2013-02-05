package uk.ac.ox.oucs.vle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class PopulatorInstanceData {
	
	private int departmentSeen;
	private int departmentCreated;
	private int departmentUpdated;
	private int subunitSeen;
	private int subunitCreated;
	private int subunitUpdated;
	private int groupSeen;
	private int groupCreated;
	private int groupUpdated;
	private int componentSeen;
	private int componentCreated;
	private int componentUpdated;
	
	private String lastGroup = null;
	
	public PopulatorInstanceData() {
		
		departmentSeen = 0;
		departmentCreated = 0;
		departmentUpdated = 0;
		subunitSeen = 0;
		subunitCreated = 0;
		subunitUpdated = 0;
		groupSeen = 0;
		groupCreated = 0;
		groupUpdated = 0;
		componentSeen = 0;
		componentCreated = 0;
		componentUpdated = 0;
		
		lastGroup = null;
	}
	
	public int getDepartmentSeen() {
		return departmentSeen;
	}
	public void incrDepartmentSeen() {
		departmentSeen++;
	}
	
	public int getDepartmentCreated() {
		return departmentCreated;
	}
	public void incrDepartmentCreated() {
		departmentCreated++;
	}
	
	public int getDepartmentUpdated() {
		return departmentUpdated;
	}
	public void incrDepartmentUpdated() {
		departmentUpdated++;
	}
	
	public int getSubunitSeen() {
		return subunitSeen;
	}
	public void incrSubunitSeen() {
		subunitSeen++;
	}
	
	public int getSubunitCreated() {
		return subunitCreated;
	}
	public void incrSubunitCreated() {
		subunitCreated++;
	}
	
	public int getSubunitUpdated() {
		return subunitUpdated;
	}
	public void incrSubunitUpdated() {
		subunitUpdated++;
	}
	
	public int getGroupSeen() {
		return groupSeen;
	}
	public void incrGroupSeen() {
		groupSeen++;
	}
	
	public int getGroupCreated() {
		return groupCreated;
	}
	public void incrGroupCreated() {
		groupCreated++;
	}
	
	public int getGroupUpdated() {
		return groupUpdated;
	}
	public void incrGroupUpdated() {
		groupUpdated++;
	}
	
	public int getComponentSeen() {
		return componentSeen;
	}
	public void incrComponentSeen() {
		componentSeen++;
	}
	
	public int getComponentCreated() {
		return componentCreated;
	}
	public void incrComponentCreated() {
		componentCreated++;
	}
	
	public int getComponentUpdated() {
		return componentUpdated;
	}
	public void incrComponentUpdated() {
		componentUpdated++;
	}
	
	public String getLastGroup() {
		return this.lastGroup;
	}
	
	public void setLastGroup(String lastGroup) {
		this.lastGroup = lastGroup;
	}
	

}
