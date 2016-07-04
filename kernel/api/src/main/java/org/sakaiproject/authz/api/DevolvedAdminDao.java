package org.sakaiproject.authz.api;

import java.util.List;

import org.sakaiproject.authz.hbm.DevolvedAdmin;


public interface DevolvedAdminDao {

	List<DevolvedAdmin> findByAdminRealm(String adminRealm);
	
	DevolvedAdmin findByRealm(String realm);
	
	void save(DevolvedAdmin devolvedAdmin);
	
	void delete(String realm);
}
