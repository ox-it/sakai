package org.springframework.web.servlet.mvc.annotation;

import java.util.Arrays;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This filters the beans found based on matching of the TargettedController annotation. This allows
 * multiple servlets to be defined but limit which controllers are used by which servlet.
 * @see TargettedController
 * 
 */
public class FilteredAnnotationHandlerMapping extends DefaultAnnotationHandlerMapping {

    private String target;

    public void setTarget(String target) {
        this.target = target;
    }

    protected String[] determineUrlsForHandler(String beanName) {
        if (accepted(beanName)) {
            return super.determineUrlsForHandler(beanName);
        } else {
            return null;
        }
    }

    protected boolean accepted(String beanName) {
        ApplicationContext context = getApplicationContext();
        ListableBeanFactory bf = (context instanceof ConfigurableApplicationContext ? ((ConfigurableApplicationContext) context)
                .getBeanFactory() : context);
        DefaultListableBeanFactory bfa = new DefaultListableBeanFactory(bf);
        TargettedController controller = bfa.findAnnotationOnBean(beanName, TargettedController.class);
        // Might not be annotated with TargettedController
        if (controller != null) {
            return (Arrays.asList(controller.value()).contains(target));
        } else {
            // If we don't set a target then catch all the "other controllers"
            return (target == null);
        }
    }
}
