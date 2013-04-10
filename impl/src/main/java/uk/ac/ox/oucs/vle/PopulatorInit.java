package uk.ac.ox.oucs.vle;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * We need todo this from another bean so the transaction proxy works.
 * @author buckett
 *
 */
public class PopulatorInit {

	private Populator populator;
	private static final Log log = LogFactory.getLog(PopulatorInit.class);

	public void setPopulator(Populator populator) {
		this.populator = populator;
	}
	
	public void init() {
		
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("config.properties"));
			PopulatorContext context = new PopulatorContext("xcri.oxcap.populator", properties);
			populator.update(context);
			
		} catch (IOException e) {
			log.error("IOException [config.properties]", e);
		}
	}
}
