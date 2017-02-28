/**
 * Copyright (c) 2015 Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.coursemanagement.impl;

import java.util.ArrayList;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.CourseSiteRemovalService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;


/**
 * This class is an implementation of the auto site removal service interface.
 */
public class CourseSiteRemovalServiceImpl extends HibernateDaoSupport implements CourseSiteRemovalService {

   // logger
   private final transient Logger logger = LoggerFactory.getLogger(getClass());

   // class members
   private static final long ONE_DAY_IN_MS = 1000L * 60L * 60L * 24L;    // one day in ms = 1000ms/s · 60s/m · 60m/h · 24h/day

   // batch sizes to silently unpublish sites
   private static final long SILENT_UNPUBLISH_BATCH_SIZE = 1000;

   private static final String SAK_PROP_HANDLE_CROSSLISTING = "course_site_removal_service.handle.crosslisting";
   private static final String SAK_PROP_SILENT_UNPUBLISH = "course_site_removal_service.silently.unpublish";
   private static final boolean SAK_PROP_HANDLE_CROSSLISTING_DEFAULT = false;
   private static final boolean SAK_PROP_SILENT_UNPUBLISH_DEFAULT = false;

   // sakai services
   private AuthzGroupService authzGroupService;
   private CourseManagementService courseManagementService;
   private FunctionManager functionManager;
   private SecurityService securityService;
   private ServerConfigurationService serverConfigurationService;
   private SiteService siteService;

   /**
    * called by the spring framework.
    */
   public void destroy() {
      logger.debug("destroy()");

       // no code necessary
   }

   /**
    * called by the spring framework after this class has been instantiated, this method registers the permissions necessary to invoke the course site removal service.
    */
   public void init() {
      logger.debug("init()");

      // register permissions with sakai
      functionManager.registerFunction(PERMISSION_COURSE_SITE_REMOVAL);
   }

    /**
    * returns the instance of the AuthzGroupService injected by the spring framework specified in the components.xml file via IoC.
    *
    * @return the instance of the AuthzGroupService injected by the spring framework specified in the components.xml file via IoC.
    */
   public AuthzGroupService getAuthzGroupService() {
      return authzGroupService;
   }

   /**
    * called by the spring framework to initialize the authzGroupService data member specified in the components.xml file via IoC.
    *
    * @param authzGroupService   the implementation of the AuthzGroupService interface provided by the spring framework.
    */
   public void setAuthzGroupService(AuthzGroupService authzGroupService) {
      this.authzGroupService = authzGroupService;
   }

   /**
    * returns the instance of the CourseManagementService injected by the spring framework specified in the components.xml file via IoC.
    *
    * @return the instance of the CourseManagementService injected by the spring framework specified in the components.xml file via IoC.
    */
   public CourseManagementService getCourseManagementService() {
      return courseManagementService;
   }

   /**
    * called by the spring framework to initialize the courseManagementService data member specified in the components.xml file via IoC.
    * 
    * @param courseManagementService   the implementation of the CourseManagementService interface provided by the spring framework.
    */
   public void setCourseManagementService(CourseManagementService courseManagementService) {
      this.courseManagementService = courseManagementService;
   }
   /**
    * returns the instance of the FunctionManager injected by the spring framework specified in the components.xml file via IoC.
    * 
    * @return the instance of the FunctionManager injected by the spring framework specified in the components.xml file via IoC.
    */
   public FunctionManager getFunctionManager() {
      return functionManager;
   }

   /**
    * called by the spring framework to initialize the functionManager data member specified in the components.xml file via IoC.
    * 
    * @param functionManager   the implementation of the FunctionManager interface provided by the spring framework.
    */
   public void setFunctionManager(FunctionManager functionManager) {
      this.functionManager = functionManager;
   }

   /**
    * returns the instance of the SecurityService injected by the spring framework specified in the components.xml file via IoC.
    * 
    * @return the instance of the SecurityService injected by the spring framework specified in the components.xml file via IoC.
    */
   public SecurityService getSecurityService() {
      return securityService;
   }

   /**
    * called by the spring framework to initialize the securityService data member specified in the components.xml file via IoC.
    * 
    * @param securityService   the implementation of the SecurityService interface provided by the spring framework.
    */
   public void setSecurityService(SecurityService securityService) {
      this.securityService = securityService;
   }

   /**
    * returns the instance of the ServerConfigurationService injected by the spring framework specified in the components.xml file via IoC.
    *
    * @return the instance of the ServerConfigurationService injected by the spring framework specified in the components.xml file via IoC.
    */
   public ServerConfigurationService getServerConfigurationService() {
      return serverConfigurationService;
   }

   /**
    * called by the spring framework to initialize the serverConfigurationService data member specified in the components.xml file via IoC.
    *
    * @param serverConfigurationService   the implementation of the ServerConfigurationService interface provided by the spring framework.
    */
   public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
      this.serverConfigurationService = serverConfigurationService;
   }

   /**
    * returns the instance of the SiteService injected by the spring framework specified in the components.xml file via IoC.
    * 
    * @return the instance of the SiteService injected by the spring framework specified in the components.xml file via IoC.
    */
   public SiteService getSiteService() {
      return siteService;
   }

   /**
    * called by the spring framework to initialize the siteService data member specified in the components.xml file via IoC.
    * 
    * @param siteService   the implementation of the SiteService interface provided by the spring framework.
    */
   public void setSiteService(SiteService siteService) {
      this.siteService = siteService;
   }

   /**
    * removes\\unpublishes course sites whose terms have ended and a specified number of days have passed.
    * Once a term has ended, the course sites for that term remain available for a specified number of days, whose duration is specified in sakai.properties
    * via the <i>course_site_removal_service.num_days_after_term_ends</i> property.  After the specified period has elapsed, this invoking this service will either
    * remove or unpublish the course site, depending on the value of the <i>course_site_removal_service.action</i> sakai property.
    * 
    * @param action                 whether to delete the course site or to simply unpublish it.
    * @param numDaysAfterTermEnds   number of days after a term ends when course sites expire.
    * 
    * @return the number of course sites that were removed\\unpublished.
    */

    public int removeCourseSites(CourseSiteRemovalService.Action action, int numDaysAfterTermEnds) {

       logger.info("removeCourseSites(" + action + " course sites, " + numDaysAfterTermEnds + " days after the term ends)");
       Date today           = new Date();
       Date expirationDate  = new Date(today.getTime() - numDaysAfterTermEnds * ONE_DAY_IN_MS);

       boolean handleCrosslistedTerms = serverConfigurationService.getBoolean(SAK_PROP_HANDLE_CROSSLISTING, SAK_PROP_HANDLE_CROSSLISTING_DEFAULT);
       if (handleCrosslistedTerms) {
          return removeCourseSitesWithCriteria(action, null, expirationDate);
       }

       // get the list of the academic term(s)
       List<AcademicSession> academicSessions = courseManagementService.getAcademicSessions();

       int numSitesRemoved = 0;
       for(AcademicSession academicSession : academicSessions) {
          // see if the academic session ended more than the specified number of days ago
          if (academicSession.getEndDate().getTime() < expirationDate.getTime()) {
             // get a list of all published course sites in ascending creation date order which are associated with the specified academic session
             Map<String, String> propertyCriteria = new HashMap<>();
             propertyCriteria.put("term_eid", academicSession.getEid());
             numSitesRemoved += removeCourseSitesWithCriteria(action, propertyCriteria, expirationDate);
          }
       }

       return numSitesRemoved;
    }

    /**
     * Removes all sites matching the specified propertyCriteria except sites that:
     * 1) have been touched by this job in the past, or that have academic sessions that are not yet expired
     * 2) are attached to rosters that have academic sessions that are not yet expired (applies only if isHandleCrosslistedTerms())
     * @param action action to perform: remove / unpublish (default: unpublish)
     * @param propertyCriteria map of propertyCriteria for SiteService.getSites(...)
     * @param expirationDate sessions are considered expired if they fall before this date
     * @return the number of sites that were successfully removed
     */
    private int removeCourseSitesWithCriteria(CourseSiteRemovalService.Action action, Map<String, String> propertyCriteria, Date expirationDate) {

        int numSitesRemoved = 0;

        // Omit sites that have already been processed by this job in the past
        SelectionType publishedNonDeletedOnly = new SelectionType("any", false, false, true, true);
        Map<String, String> propertyRestrictions = Collections.singletonMap(SITE_PROPERTY_COURSE_SITE_REMOVAL, "set");
        List<String> siteIds = (List<String>) siteService.getSiteIds(publishedNonDeletedOnly, "course", null, propertyCriteria, propertyRestrictions,
                                                                     SortType.CREATED_ON_ASC, null, null);

        /*
         * Two ways to unpublish a site:
         * 1) SiteService.save(Site site)
         *     -triggers SiteAdvisors
         *     -deletes all pages, tools, properties, etc associated with the site, then inserts them all back with any modifications
         *     -handles authz group changes
         *     -notifies all ContextObservers to do their own work related to the site modification
         *     -triggers EventTrackingService
         * 2) SiteService.silentlyUnpublish(List<String> siteIds)
         *     -sets PUBLISHED flag to 0 on all SAKAI_SITE matches
         *     -triggers EventTrackingService
         */
        boolean silentlyUnpublish = action == CourseSiteRemovalService.Action.unpublish &&
                                    serverConfigurationService.getBoolean(SAK_PROP_SILENT_UNPUBLISH, SAK_PROP_SILENT_UNPUBLISH_DEFAULT);

        // toUnpublish will collect siteIds to unpublish in bulk
        // It is only used if we are silently unpublishing sites, otherwise we unpublish / remove sites one at a time
        // Size will grow towards siteIds.size(), but it will not necessarily reach that size
        List<String> toUnpublish = silentlyUnpublish ? new ArrayList<>(siteIds.size()) : Collections.EMPTY_LIST;

        for(String siteId : siteIds) {
            try {
                if (isHandleCrosslistedTerms()) {
                    if (isSiteCrosslistedWithEndDateAfterExpirationDate(siteId, expirationDate)) {
                        // This site is attached to an academic session that has not yet expired
                        continue;
                    }
                }

                // check permissions
                if (!checkPermission(PERMISSION_COURSE_SITE_REMOVAL, siteId)) {
                    logger.error("You do not have permission to " + action + " the site with id " + siteId);
                } else if (action == CourseSiteRemovalService.Action.remove) {
                    // remove the course site
                    Site site = siteService.getSite(siteId);
                    logger.debug(action + "removing course site " + site.getTitle() + " (" + site.getId() + ").");
                    siteService.removeSite(site);
                    numSitesRemoved++;
                } else {
                    // unpublish the course site (default)
                    logger.debug("unpublishing course site " + siteId);

                    if (silentlyUnpublish) {
                        toUnpublish.add(siteId);
                    } else {
                        Site site = siteService.getSite(siteId);

                        // Add site property
                        ResourcePropertiesEdit siteProperties = site.getPropertiesEdit();
                        siteProperties.addProperty(SITE_PROPERTY_COURSE_SITE_REMOVAL, "set");

                        // Unpublish the site and commit site property addition
                        site.setPublished(false);
                        siteService.save(site);
                        numSitesRemoved++;
                    }
                }
            } catch(PermissionException | IdUnusedException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        if (silentlyUnpublish) {
            // bulk unpublish
            siteService.silentlyUnpublish(toUnpublish);

            // Add site property on sites
            siteService.saveSitePropertyOnSites(toUnpublish.toArray(new String[toUnpublish.size()]), SITE_PROPERTY_COURSE_SITE_REMOVAL, "set");
            numSitesRemoved += toUnpublish.size();
        }

        return numSitesRemoved;
    }

     /**
     * For crosslisted sites, the sections may belong to multiple academic sessions which have differing end dates.
     * If we find a date that isn't before the grace period, this site is not supposed to be removed / unpublished.
     * @return true if this site has an academic session with an end date after the expiration date
     */
    private boolean isSiteCrosslistedWithEndDateAfterExpirationDate(String siteId, Date expirationDate) {

        String siteReference = siteService.siteReference(siteId);
        Set<String> providerIds = authzGroupService.getProviderIds(siteReference);
        for (String providerId : providerIds) {
            Section section = courseManagementService.getSection(providerId);
            if (section != null) {
                CourseOffering offering = courseManagementService.getCourseOffering(section.getCourseOfferingEid());
                if (offering != null) {
                    AcademicSession session = offering.getAcademicSession();
                    if (session != null) {
                        Date endDate = session.getEndDate();
                        if (endDate != null && endDate.getTime() >= expirationDate.getTime()) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean checkPermission(String lock, String reference) {
        return securityService.unlock(lock, reference);
    }

    private boolean isHandleCrosslistedTerms() {
        return serverConfigurationService.getBoolean("course_site_removal_service.handle.crosslisting", false);
    }
}
