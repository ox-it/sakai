package uk.ac.ox.oucs.vle;

import java.util.Date;

/**
 * A service for the current time which allows you to set it, but it still ticks along.
 * @author Matthew Buckett
 */
public class SettableNowService extends NowService {

	private long adjustment;

	public Date getNow() {
		return (adjustment != 0)?new Date(new Date().getTime() + adjustment):new Date();
	}

	public void setNow(Date newNow) {
		adjustment = newNow.getTime() - new Date().getTime();
	}

}
