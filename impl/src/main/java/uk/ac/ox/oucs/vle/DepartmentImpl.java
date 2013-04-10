package uk.ac.ox.oucs.vle;

import java.util.Collection;

public class DepartmentImpl implements Department {
	
	private String pracCode;
	private String oucsCode;
	private String name;
	private boolean approve;
	private Collection<String> approvers;
	
	public DepartmentImpl(String pracCode, String oucsCode, String name) {
		this.pracCode = pracCode;
		this.oucsCode = oucsCode;
		this.name = name;
	}
	
	public DepartmentImpl(CourseDepartmentDAO dao) {
		this.pracCode = dao.getCode();
		this.name = dao.getName();
		this.approve = dao.getApprove();
		this.approvers = dao.getApprovers();
	}

	public String getPracCode() {
		return pracCode;
	}
	
	public String getOucsCode() {
		return oucsCode;
	}

	public String getName() {
		return name;
	}
	
	public boolean getApprove() {
		return approve;
	}
	
	public Collection<String> getApprovers() {
		return approvers;
	}

}
