package org.sakaiproject.hierarchy.impl.ibatis.dao;

import com.ibatis.sqlmap.client.event.RowHandler;

public class FirstRowHandler  implements RowHandler {

	private RowHandler rowhandler;
	private Object first;
	
	public FirstRowHandler ( RowHandler rowHandler ) {
		this.rowhandler = rowHandler;
	}
	
	public void handleRow(Object object) {
		rowhandler.handleRow(object);
		if (first == null) {
			first = object;
		}
	}
	
	public Object getObject() {
		return first;
	}

	
	
}
