package uk.ac.ox.oucs.vle;

/**
 * We need todo this from another bean so the transaction proxy works.
 * @author buckett
 *
 */
public class PopulatorInit {

	private Populator populator;

	public void setPopulator(Populator populator) {
		this.populator = populator;
	}
	
	public void init() {
		populator.update();
	}
}
