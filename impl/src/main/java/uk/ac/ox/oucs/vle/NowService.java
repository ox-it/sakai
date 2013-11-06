package uk.ac.ox.oucs.vle;

import java.util.Date;

/**
 * Returns the current time. As so much of this tool is based around the current time being
 * able to set the current time makes testing much easier.
 * @author Matthew Buckett
 */
public abstract class NowService {

	abstract Date getNow();
}
