package org.sakaiproject.hierarchy.impl.ibatis.dao;

import java.util.ArrayList;
import java.util.List;

import com.ibatis.sqlmap.client.event.RowHandler;

public class ListRowHandler implements RowHandler {

	private List list = new ArrayList();
	private RowHandler rowHandler;
	
	public ListRowHandler(RowHandler rowHandler){
		this.rowHandler = rowHandler;
	}
	
	public void handleRow(Object object) {
		rowHandler.handleRow(object);
		list.add(object);
	}
	
	public List getList() {
		return list;
	}

}
