/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was basaed on the Same Code in the SpringFramework project,
 * hence the above license. It was unbound from the SpringFramework code to remove
 * classloader problems associated with inheritance
 */

package org.sakaiproject.hierarchy.tool.vm.spring;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;

/**
 * JavaBean to configure Velocity for web usage, via the "configLocation" and/or "velocityProperties" and/or
 * "resourceLoaderPath" bean properties. The simplest way to use this class is to specify just a "resourceLoaderPath";
 * you do not need any further configuration then.
 * 
 * <pre>
 *   &lt;bean id=&quot;velocityConfig&quot; class=&quot;org.springframework.web.servlet.view.velocity.VelocityConfigurer&quot;&gt;
 *     &lt;property name=&quot;resourceLoaderPath&quot;&gt;&lt;value&gt;/WEB-INF/velocity/&lt;/value&gt;&lt;/property&gt;
 *   &lt;/bean&gt;
 * </pre>
 * 
 * This bean must be included in the application context of any application using Spring's VelocityView for web MVC. It
 * exists purely to configure Velocity; it is not meant to be referenced by application components but just internally
 * by VelocityView. Implements VelocityConfig to be found by VelocityView without depending on the bean name of the
 * configurer. Each DispatcherServlet can define its own VelocityConfigurer if desired.
 * <p>
 * Note that you can also refer to a preconfigured VelocityEngine instance, for example one set up by
 * VelocityEngineFactoryBean, via the "velocityEngine" property. This allows to share a VelocityEngine for web and email
 * usage, for example.
 * <p>
 * This configurer registers the "spring.vm" Velocimacro library for web views (contained in this package and thus in
 * spring.jar), which makes all macros defined in it implicitly available:
 * 
 * <pre>
 *   #springBind(&quot;person.age&quot;)
 *   age is ${status.value}
 * </pre>
 * 
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Darren Davison
 * @see #setConfigLocation
 * @see #setVelocityProperties
 * @see #setResourceLoaderPath
 * @see #setVelocityEngine
 * @see org.springframework.ui.velocity.VelocityEngineFactoryBean
 * @see VelocityView
 */
public class VelocityConfigurer implements VelocityConfig {

    protected final Log logger = LogFactory.getLog(getClass());

    private final Map velocityProperties = new HashMap();

    private VelocityEngine velocityEngine;

    private ServletContextHolder servletContextHolder;

    /**
     * Set a preconfigured VelocityEngine to use for the Velocity web config, e.g. a shared one for web and email usage,
     * set up via VelocityEngineFactoryBean. If this is not set, VelocityEngineFactory's properties (inherited by this
     * class) have to be specified.
     * 
     * @see org.springframework.ui.velocity.VelocityEngineFactoryBean
     */
    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    public void init() throws IOException, VelocityException {
        this.velocityEngine = createVelocityEngine();
    }

    public VelocityEngine getVelocityEngine() {
        if (this.velocityEngine == null) {
            try {
                this.velocityEngine = createVelocityEngine();
            } catch (Exception e) {
                logger.error("Failed to load Engine ", e);
            }
            logger.info("Engine Loaded");
        }
        return this.velocityEngine;
    }

    /**
     * Set Velocity properties, like "file.resource.loader.path". Can be used to override values in a Velocity config
     * file, or to specify all necessary properties locally.
     * <p>
     * Note that the Velocity resource loader path also be set to any Spring resource location via the
     * "resourceLoaderPath" property. Setting it here is just necessary when using a non-file-based resource loader.
     * 
     * @see #setVelocityPropertiesMap
     * @see #setConfigLocation
     * @see #setResourceLoaderPath
     */
    public void setVelocityProperties(Properties velocityProperties) {
        setVelocityPropertiesMap(velocityProperties);
    }

    /**
     * Set Velocity properties as Map, to allow for non-String values like "ds.resource.loader.instance".
     * 
     * @see #setVelocityProperties
     */
    public void setVelocityPropertiesMap(Map velocityPropertiesMap) {
        if (velocityPropertiesMap != null) {
            this.velocityProperties.putAll(velocityPropertiesMap);
        }
    }

    /**
     * Prepare the VelocityEngine instance and return it.
     * 
     * @return the VelocityEngine instance
     * @throws IOException
     *             if the config file wasn't found
     * @throws VelocityException
     *             on Velocity initialization failure
     */
    public VelocityEngine createVelocityEngine() throws IOException, VelocityException {
        VelocityEngine velocityEngine = newVelocityEngine();
        if (servletContextHolder == null || servletContextHolder.getServletContext() == null) {
            logger.warn("Servlet ContextHolder  not set, you will not be able to use WebApp based templates, "
                    + "please \n" + "1. Add a ServletContextHolder bean into applicationContext.xml with the name \n"
                    + "2. add the VelocityContextListener to web.xml ");
        } else {
            logger.info("ServletContextHolder Set on path " + servletContextHolder.getServletContext().getRealPath("/"));
            velocityEngine.setApplicationAttribute(ServletContext.class.getName(),
                    servletContextHolder.getServletContext());

        }

        Properties props = new Properties();

        // Merge local properties if set.
        if (!this.velocityProperties.isEmpty()) {
            props.putAll(this.velocityProperties);
        }

        // Apply properties to VelocityEngine.
        for (Iterator it = props.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            if (!(entry.getKey() instanceof String)) {
                throw new IllegalArgumentException("Illegal property key [" + entry.getKey()
                        + "]: only Strings allowed");
            }

            velocityEngine.setProperty((String) entry.getKey(), entry.getValue());
        }

        try {
            // Perform actual initialization.
            velocityEngine.init();
        } catch (IOException ex) {
            throw ex;
        } catch (VelocityException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Why does VelocityEngine throw a generic checked exception, after all?", ex);
            throw new VelocityException(ex.getMessage());
        }

        return velocityEngine;
    }

    /**
     * Return a new VelocityEngine. Subclasses can override this for custom initialization, or for using a mock object
     * for testing.
     * <p>
     * Called by <code>createVelocityEngine()</code>.
     * 
     * @return the VelocityEngine instance
     * @throws IOException
     *             if a config file wasn't found
     * @throws VelocityException
     *             on Velocity initialization failure
     * @see #createVelocityEngine()
     */
    protected VelocityEngine newVelocityEngine() throws IOException, VelocityException {
        return new VelocityEngine();
    }

    public ServletContextHolder getServletContextHolder() {
        return servletContextHolder;
    }

    public void setServletContextHolder(ServletContextHolder servletContextHolder) {
        this.servletContextHolder = servletContextHolder;
    }

}
