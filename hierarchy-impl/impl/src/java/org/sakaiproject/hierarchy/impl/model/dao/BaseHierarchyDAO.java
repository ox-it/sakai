package org.sakaiproject.hierarchy.impl.model.dao;

//BaseDAOImports
//import org.sakaiproject.hierarchy.impl.model.dao.HierarchyDAO;

//CustomBaseDAOClassComments
/**
 * This is an automatically generated DAO class which should not be edited.
 */
// CustomBaseDAODefinition

public abstract class BaseHierarchyDAO extends org.sakaiproject.hierarchy.impl.model.dao._RootDAO {

// CustomBaseDAOClassConstructors

	public BaseHierarchyDAO () {}
	
	

// CustomBaseDAOQueryNames

	// query name references


// CustomBaseDAOInstanceMethods

// CustomBaseDAORequiredMethods    
    
    public Class getReferenceClass () {
		return org.sakaiproject.hierarchy.model.Hierarchy.class;
	}


	/**
	 * Cast the object as a org.sakaiproject.hierarchy.model.Hierarchy
	 */
	public org.sakaiproject.hierarchy.model.Hierarchy cast (Object object) {
		return (org.sakaiproject.hierarchy.model.Hierarchy) object;
	}
	

//CustomBaseDAOFinderMethods


	public org.sakaiproject.hierarchy.model.Hierarchy load(java.lang.String key)
		throws org.springframework.dao.DataAccessException {
		return (org.sakaiproject.hierarchy.model.Hierarchy) load(getReferenceClass(), key);
	}



	public org.sakaiproject.hierarchy.model.Hierarchy get(java.lang.String key)
		throws org.springframework.dao.DataAccessException {
		return (org.sakaiproject.hierarchy.model.Hierarchy) get(getReferenceClass(), key);
	}



	public java.util.List loadAll()
		throws org.springframework.dao.DataAccessException {
		return loadAll(getReferenceClass());
	}




/* Generic methods */

	/**
	 * Return all objects related to the implementation of this DAO with no filter.
	 */
	public java.util.List findAll () {
		return super.loadAll(getReferenceClass());
	}




// CustomBaseDAOActionMethods

    /**
	 * Persist the given transient instance, first assigning a generated identifier. (Or using the current value
	 * of the identifier property if the assigned generator is used.) 
	 * @param hierarchy a transient instance of a persistent class 
	 * @return the class identifier
	 */
	public java.lang.String save(org.sakaiproject.hierarchy.model.Hierarchy hierarchy)
		throws org.springframework.dao.DataAccessException {
		return (java.lang.String) super.save(hierarchy);
	}

	/**
	 * Either save() or update() the given instance, depending upon the value of its identifier property. By default
	 * the instance is always saved. This behaviour may be adjusted by specifying an unsaved-value attribute of the
	 * identifier property mapping. 
	 * @param hierarchy a transient instance containing new or updated state 
	 */
	public void saveOrUpdate(org.sakaiproject.hierarchy.model.Hierarchy hierarchy)
		throws org.springframework.dao.DataAccessException {
		super.saveOrUpdate(hierarchy);
	}


	/**
	 * Update the persistent state associated with the given identifier. An exception is thrown if there is a persistent
	 * instance with the same identifier in the current session.
	 * @param hierarchy a transient instance containing updated state
	 */
	public void update(org.sakaiproject.hierarchy.model.Hierarchy hierarchy) 
		throws org.springframework.dao.DataAccessException {
		super.update(hierarchy);
	}
	
	public void delete(org.sakaiproject.hierarchy.model.Hierarchy hierarchy) {
		super.delete(hierarchy);
	}

// CustomBaseDAOCustomComments
}