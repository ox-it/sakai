package org.sakaiproject.authz.api;


/**
 * This class is for dealing with two factor authentication.
 * It can tell if a session has the second factor authentication
 * and holds the rules about what authz requests need a second factor.
 * This performance of the implmentation of this interface needs to be
 * very good as it will get called for all authorization requests.
 */
public interface TwoFactorAuthentication {

	/**
	 * Is the current session authenticated by a second factor and it is still valid?
	 * @return <code>true</code> if the session has a valid second factor.
	 */
	public boolean hasTwoFactor();

	/**
	 * Mark the current session as being two factor authenticated.
	 * This should only be called after login with a second factor has happened.
	 */
	public void markTwoFactor();

	/**
	 * Check if a second factor is required.
	 * @param ref A entity reference. This could be a site reference or something else. The reference will be something like "/site/{id}"
	 * @return <code>true</code> if the session must have a second factor to be allowed access to this resource.
	 */
	public boolean isTwoFactorRequired(String ref);

}
