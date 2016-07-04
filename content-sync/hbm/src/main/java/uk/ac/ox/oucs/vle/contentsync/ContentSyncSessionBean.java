package uk.ac.ox.oucs.vle.contentsync;

/**
 * This exists here purely because spring uses the wrong classloader when creating proxies and
 * the interface needs to be in shared.
 * @author buckett
 *
 */
public interface ContentSyncSessionBean {

	public long getTopicId(long messageId);
}
