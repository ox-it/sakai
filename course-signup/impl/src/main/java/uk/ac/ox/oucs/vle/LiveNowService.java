package uk.ac.ox.oucs.vle;

import java.util.Date;

/**
 * A version of the now service that just returns the current time.
 * This should be used in production.
 * @author Matthew Buckett
 */
public class LiveNowService extends NowService{

	@Override
	Date getNow() {
		return new Date();
	}
}
