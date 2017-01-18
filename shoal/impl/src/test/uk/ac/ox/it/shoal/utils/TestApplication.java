package uk.ac.ox.it.shoal.utils;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * This is here because at the moment spring-test doesn't support both locations and resources.
 *
 */
@Configuration
@ImportResource({"classpath:sakai-beans.xml", "classpath:shoal-beans.xml"})
public class TestApplication {

}
