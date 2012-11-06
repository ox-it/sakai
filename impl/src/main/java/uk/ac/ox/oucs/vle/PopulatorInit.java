package uk.ac.ox.oucs.vle;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

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
		
		Properties properties = new Properties();
		try {
			
			properties.load(new FileInputStream("config.properties"));
			
			PopulatorContext context = new PopulatorContext();
			context.setURI(properties.getProperty("xcri.oxcap.populator.uri"));
			context.setUser(properties.getProperty("xcri.oxcap.populator.username"));
			context.setPassword(properties.getProperty("xcri.oxcap.populator.password"));
			context.setName(properties.getProperty("xcri.oxcap.populator.name"));
			
			populator.update(context);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
