/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.wicket.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.LessonBuilderStat;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.Util;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.user.api.UserNotDefinedException;

public class LessonsWidget extends Panel {

    private static final long       serialVersionUID    = 1L;
    private static Log              LOG                 = LogFactory.getLog(LessonsWidget.class);

    /** The site id. */
    private String                  siteId              = null;
    private PrefsData               prefsdata           = null;
    
    private int                     totalPages          = -1;

    /**
     * Default constructor.
     * @param id The wicket:id
     * @param siteId The related site id
     */
    public LessonsWidget(String id, final String siteId) {

        super(id);
        this.siteId = siteId;
        setRenderBodyOnly(true);
        setOutputMarkupId(true);
        
        // Single values (MiniStat)
        List<WidgetMiniStat> widgetMiniStats = new ArrayList<WidgetMiniStat>();
        widgetMiniStats.add(getMiniStatPages());
        widgetMiniStats.add(getMiniStatReadPages());
        widgetMiniStats.add(getMiniStatMostReadPage());
        widgetMiniStats.add(getMiniStatUserThatReadMorePages());        
        
        // Tabs
        List<AbstractTab> tabs = new ArrayList<AbstractTab>();
        tabs.add(new AbstractTab(new ResourceModel("overview_tab_bydate")) {
            private static final long   serialVersionUID    = 1L;
            @Override
            public Panel getPanel(String panelId) {
                return getWidgetTabByDate(panelId);
            }           
        });
        tabs.add(new AbstractTab(new ResourceModel("overview_tab_byuser")) {
            private static final long   serialVersionUID    = 1L;
            @Override
            public Panel getPanel(String panelId) {
                return getWidgetTabByUser(panelId);
            }           
        });
        tabs.add(new AbstractTab(new ResourceModel("overview_tab_bypage")) {
            private static final long   serialVersionUID    = 1L;
            @Override
            public Panel getPanel(String panelId) {
                return getWidgetTabByResource(panelId);
            }           
        });

        // Final Widget object      
        String icon = StatsManager.SILK_ICONS_DIR + "folder_page.png";
        String title = (String) new ResourceModel("overview_title_lessonpages").getObject();
        Widget widget = new Widget("widget", icon, title, widgetMiniStats, tabs);
        add(widget);
    }

    // -------------------------------------------------------------------------------  
    /** MiniStat:: Pages count */
    private WidgetMiniStat getMiniStatPages() {

        return new WidgetMiniStat() {

            private static final long   serialVersionUID    = 1L;
            @Override
            public String getValue() {

                long start = 0;
                if (LOG.isDebugEnabled()) start = System.currentTimeMillis();
                String val = Integer.toString(getTotalPages());
                if (LOG.isDebugEnabled()) LOG.debug("getMiniStatPages() in " + (System.currentTimeMillis() - start) + " ms");
                return val;
            }
            @Override
            public String getSecondValue() {
                return null;
            }
            @Override
            public String getTooltip() {
                return null;
            }
            @Override
            public boolean isWiderText() {
                return false;
            }
            @Override
            public String getLabel() {
                return (String) new ResourceModel("overview_title_pages_sum").getObject();
            }
            @Override
            public ReportDef getReportDefinition() {

                ReportDef r = new ReportDef();
                r.setId(0);
                r.setSiteId(siteId);
                ReportParams rp = new ReportParams(siteId);
                // what
                rp.setWhat(ReportManager.WHAT_LESSONPAGES);
                rp.setWhatLimitedAction(true);
                rp.setWhatResourceAction(ReportManager.WHAT_LESSONS_ACTION_READ);
                rp.setWhatLimitedResourceIds(true);
                rp.setWhatResourceIds(Arrays.asList("/lessonbuilder/"));
                // when
                rp.setWhen(ReportManager.WHEN_ALL);
                // who
                rp.setWho(ReportManager.WHO_ALL);
                // grouping
                List<String> totalsBy = new ArrayList<String>();
                totalsBy.add(StatsManager.T_RESOURCE);
                rp.setHowTotalsBy(totalsBy);
                // sorting
                rp.setHowSort(true);
                rp.setHowSortBy(StatsManager.T_RESOURCE);
                rp.setHowSortAscending(true);
                // chart
                rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
                r.setReportParams(rp);
                return r;
            }
        };
    }
    
    /** MiniStat:: Read pages */
    private WidgetMiniStat getMiniStatReadPages() {

        return new WidgetMiniStat() {

            private static final long   serialVersionUID            = 1L;
            private long                totalDistinctPageReads      = -1;
            
            @Override
            public String getValue() {

                long start = 0;
                if (LOG.isDebugEnabled()) start = System.currentTimeMillis();
                processData();
                String val = Long.toString(totalDistinctPageReads);
                if (LOG.isDebugEnabled()) LOG.debug("getMiniStatReadPages() in " + (System.currentTimeMillis() - start) + " ms");
                return val;
                
            }
            
            @Override
            public String getSecondValue() {

                double percentage = getTotalPages()==0 ? 0 : Util.round(100 * totalDistinctPageReads / (double) getTotalPages(), 0);
                return String.valueOf((int) percentage) + '%';
            }
            
            @Override
            public String getTooltip() {
                return null;
            }
            
            @Override
            public boolean isWiderText() {
                return false;
            }
            
            @Override
            public String getLabel() {
                return (String) new ResourceModel("overview_title_readpages_sum").getObject();
            }
            
            private ReportDef getCommonReportDefition() {

                ReportDef r = new ReportDef();
                r.setId(0);
                r.setSiteId(siteId);
                ReportParams rp = new ReportParams(siteId);
                // what
                rp.setWhat(ReportManager.WHAT_LESSONPAGES);
                rp.setWhatLimitedAction(true);
                rp.setWhatResourceAction(ReportManager.WHAT_LESSONS_ACTION_READ);
                rp.setWhatLimitedResourceIds(true);
                rp.setWhatResourceIds(Arrays.asList("/lessonbuilder/"));
                // when
                rp.setWhen(ReportManager.WHEN_ALL);
                // who
                rp.setWho(ReportManager.WHO_ALL);
                // grouping
                List<String> totalsBy = new ArrayList<String>();
                totalsBy.add(StatsManager.T_RESOURCE);
                rp.setHowTotalsBy(totalsBy);
                // sorting
                rp.setHowSort(true);
                rp.setHowSortBy(StatsManager.T_TOTAL);
                rp.setHowSortAscending(false);
                // chart
                rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
                r.setReportParams(rp);
                return r;
            }
            
            private void processData() {

                if (totalDistinctPageReads == -1) {
                    Report r = Locator.getFacade().getReportManager().getReport(getCommonReportDefition(), true, null, false);
                    try {
                        totalDistinctPageReads = 0;
                        for (Stat s : r.getReportData()) {
                            try {
                                String resId = ((LessonBuilderStat)s).getResourceRef();
                                System.out.println("resId: " + resId);
                                String prefix = "/lessonbuilder/page/";
                                if (resId.startsWith(prefix)) {
                                    resId = resId.substring(prefix.length());
                                }
                                totalDistinctPageReads++;
                            } catch (Exception e) {
                                // skip: unknown error
                            }
                        }
                    } catch (Exception e) {
                        totalDistinctPageReads = 0;
                    }
                }
            }
            
            @Override
            public ReportDef getReportDefinition() {
                return getCommonReportDefition();
            }
        };
    }
    
    /** MiniStat:: Most opened file */
    private WidgetMiniStat getMiniStatMostReadPage() {

        return new WidgetMiniStat() {
            private static final long   serialVersionUID            = 1L;
            private String              mostOpenedFile              = null;
            
            @Override
            public String getValue() {

                long start = 0;
                if (LOG.isDebugEnabled()) start = System.currentTimeMillis();
                processData();
                String val = null;
                if (mostOpenedFile != null) {
                    val = Locator.getFacade().getStatsManager().getResourceName(mostOpenedFile, false);
                } else {
                    val = "-";
                }
                if (LOG.isDebugEnabled()) LOG.debug("getMiniStatMostReadPage() in " + (System.currentTimeMillis() - start) + " ms");
                return val;
            }
            
            @Override
            public String getSecondValue() {
                return null;
            }
            
            @Override
            public String getTooltip() {

                if (mostOpenedFile != null) {
                    return Locator.getFacade().getStatsManager().getResourceName(mostOpenedFile, true);
                } else {
                    return null;
                }
            }
            
            @Override
            public boolean isWiderText() {
                return true;
            }
            
            @Override
            public String getLabel() {
                return (String) new ResourceModel("overview_title_mostreadpage_sum").getObject();
            }
            
            private ReportDef getCommonReportDefition() {

                ReportDef r = new ReportDef();
                r.setId(0);
                r.setSiteId(siteId);
                ReportParams rp = new ReportParams(siteId);
                // what
                rp.setWhat(ReportManager.WHAT_LESSONPAGES);
                rp.setWhatLimitedAction(true);
                rp.setWhatResourceAction(ReportManager.WHAT_LESSONS_ACTION_READ);
                rp.setWhatLimitedResourceIds(true);
                rp.setWhatResourceIds(Arrays.asList("/lessonbuilder/page/"));
                // when
                rp.setWhen(ReportManager.WHEN_ALL);
                // who
                rp.setWho(ReportManager.WHO_ALL);
                // grouping
                List<String> totalsBy = new ArrayList<String>();
                totalsBy.add(StatsManager.T_RESOURCE);
                rp.setHowTotalsBy(totalsBy);
                // sorting
                rp.setHowSort(true);
                rp.setHowSortBy(StatsManager.T_TOTAL);
                rp.setHowSortAscending(false);
                // chart
                rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
                r.setReportParams(rp);
                return r;
            }
            
            private void processData() {

                if (mostOpenedFile == null) {
                    Report r = Locator.getFacade().getReportManager().getReport(getCommonReportDefition(), true, null, false);
                    try {
                        boolean first = true;
                        for (Stat s : r.getReportData()) {
                            LessonBuilderStat es = (LessonBuilderStat) s;
                            if (first) {
                                mostOpenedFile = es.getResourceRef();
                                first = false;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        mostOpenedFile = null;
                    }
                }
            }
            
            @Override
            public ReportDef getReportDefinition() {
                return getCommonReportDefition();
            }
        };
    }
    
    /** MiniStat:: User that opened more file */
    private WidgetMiniStat getMiniStatUserThatReadMorePages() {

        return new WidgetMiniStat() {

            private static final long   serialVersionUID            = 1L;
            private String              user                        = null;
            
            @Override
            public String getValue() {
                long start = 0;
                if (LOG.isDebugEnabled()) start = System.currentTimeMillis();
                processData();
                String val = null;
                if (user != null) {
                    String id = null;
                    if (("-").equals(user) || ("?").equals(user)){
                        id = "-";
                    } else {
                        try {
                            id = Locator.getFacade().getUserDirectoryService().getUser(user).getDisplayId();
                        } catch (UserNotDefinedException e1){
                            id = user;
                        }
                    }
                    val = id;
                } else {
                    val = "-";
                }
                if (LOG.isDebugEnabled()) LOG.debug("getMiniStatUserThatReadMorePages() in " + (System.currentTimeMillis() - start) + " ms");
                return val;
            }
            
            @Override
            public String getSecondValue() {
                return null;
            }
            
            @Override
            public String getTooltip() {

                if (user != null) {
                    String name = null;
                    if (("-").equals(user)) {
                        name = (String) new ResourceModel("user_anonymous").getObject();
                    }else if (("?").equals(user)) {
                        name = (String) new ResourceModel("user_anonymous_access").getObject();
                    } else {
                        name = Locator.getFacade().getStatsManager().getUserNameForDisplay(user);
                    }
                    return name;
                } else {
                    return null;
                }
            }
            
            @Override
            public boolean isWiderText() {
                return true;
            }
            
            @Override
            public String getLabel() {
                return (String) new ResourceModel("overview_title_userreadmorepage_sum").getObject();
            }
            
            private ReportDef getCommonReportDefition() {
                ReportDef r = new ReportDef();
                r.setId(0);
                r.setSiteId(siteId);
                ReportParams rp = new ReportParams(siteId);
                // what
                rp.setWhat(ReportManager.WHAT_LESSONPAGES);
                rp.setWhatLimitedAction(true);
                rp.setWhatResourceAction(ReportManager.WHAT_LESSONS_ACTION_READ);
                rp.setWhatLimitedResourceIds(true);
                rp.setWhatResourceIds(Arrays.asList("/lessonbuilder/page/"));
                // when
                rp.setWhen(ReportManager.WHEN_ALL);
                // who
                rp.setWho(ReportManager.WHO_ALL);
                // grouping
                List<String> totalsBy = new ArrayList<String>();
                totalsBy.add(StatsManager.T_USER);
                rp.setHowTotalsBy(totalsBy);
                // sorting
                rp.setHowSort(true);
                rp.setHowSortBy(StatsManager.T_TOTAL);
                rp.setHowSortAscending(false);
                // chart
                rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_TABLE);
                r.setReportParams(rp);
                return r;
            }
            
            private void processData() {

                if (user == null) {
                    Report r = Locator.getFacade().getReportManager().getReport(getCommonReportDefition(), true, null, false);
                    try {
                        boolean first = true;
                        for (Stat s : r.getReportData()) {
                            LessonBuilderStat es = (LessonBuilderStat) s;
                            if (first) {
                                user = es.getUserId();
                                first = false;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        user = null;
                    }
                }
            }
            
            @Override
            public ReportDef getReportDefinition() {
                return getCommonReportDefition();
            }
        };
    }
    
    /** WidgetTab: By date */
    protected WidgetTabTemplate getWidgetTabByDate(String panelId) {

        WidgetTabTemplate wTab = new WidgetTabTemplate(panelId, LessonsWidget.this.siteId) {
            private static final long   serialVersionUID    = 1L;

            @Override
            public List<Integer> getFilters() {
                return Arrays.asList(FILTER_DATE, FILTER_ROLE);
            }

            @Override
            public boolean useChartReportDefinitionForTable() {
                return true;
            }
            
            @Override
            public ReportDef getChartReportDefinition() {
                return getTableReportDefinition();
            }
            
            @Override
            public ReportDef getTableReportDefinition() {
                String dateFilter = getDateFilter();
                String roleFilter = getRoleFilter();
                String resActionFilter = getResactionFilter();
                
                ReportDef r = new ReportDef();
                r.setSiteId(siteId);
                ReportParams rp = new ReportParams(siteId);
                // what
                rp.setWhat(ReportManager.WHAT_LESSONPAGES);
                // limit to Resources tool:
                rp.setWhatLimitedResourceIds(true);
                rp.setWhatResourceIds(Arrays.asList("/lessonbuilder/page/"));
                if (resActionFilter != null) {
                    rp.setWhatLimitedAction(true);
                    rp.setWhatResourceAction(resActionFilter);
                }
                // when
                rp.setWhen(dateFilter);
                // who
                if (!ReportManager.WHO_ALL.equals(roleFilter)) {
                    rp.setWho(ReportManager.WHO_ROLE);
                    rp.setWhoRoleId(roleFilter);
                }
                // grouping
                List<String> totalsBy = new ArrayList<String>();
                if (dateFilter.equals(ReportManager.WHEN_LAST365DAYS) || dateFilter.equals(ReportManager.WHEN_ALL)) {
                    totalsBy.add(StatsManager.T_DATEMONTH);
                } else {
                    totalsBy.add(StatsManager.T_DATE);
                }
                rp.setHowTotalsBy(totalsBy);
                // sorting
                rp.setHowSort(true);
                /*if (dateFilter.equals(ReportManager.WHEN_LAST365DAYS) || dateFilter.equals(ReportManager.WHEN_ALL)) {
                    rp.setHowSortBy(StatsManager.T_DATEMONTH);
                } else {
                    rp.setHowSortBy(StatsManager.T_DATE);
                }*/
                rp.setHowSortBy(StatsManager.T_TOTAL);
                rp.setHowSortAscending(false);
                // chart
                rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
                rp.setHowChartType(StatsManager.CHARTTYPE_TIMESERIESBAR);
                rp.setHowChartSource(StatsManager.T_DATE);
                rp.setHowChartSeriesSource(StatsManager.T_NONE);
                if (dateFilter.equals(ReportManager.WHEN_LAST365DAYS) || dateFilter.equals(ReportManager.WHEN_ALL)) {
                    rp.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_MONTH);
                }else if (dateFilter.equals(ReportManager.WHEN_LAST30DAYS)) {
                    rp.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_DAY);
                } else {
                    rp.setHowChartSeriesPeriod(StatsManager.CHARTTIMESERIES_WEEKDAY);
                }
                r.setReportParams(rp);
                
                return r;
            }                   
        };
        return wTab;
    }
    
    /** WidgetTab: By user */
    protected WidgetTabTemplate getWidgetTabByUser(String panelId) {
        WidgetTabTemplate wTab = new WidgetTabTemplate(panelId, LessonsWidget.this.siteId) {
            private static final long   serialVersionUID    = 1L;

            @Override
            public List<Integer> getFilters() {
                return Arrays.asList(FILTER_DATE, FILTER_ROLE);
            }

            @Override
            public boolean useChartReportDefinitionForTable() {
                return false;
            }
            
            @Override
            public ReportDef getTableReportDefinition() {
                ReportDef r = getChartReportDefinition();
                ReportParams rp = r.getReportParams();
                rp.setHowTotalsBy(Arrays.asList(StatsManager.T_USER));
                rp.setHowSort(true);
                rp.setHowSortBy(StatsManager.T_TOTAL);
                rp.setHowSortAscending(false);      
                r.setReportParams(rp);
                return r;
            }
            
            @Override
            public ReportDef getChartReportDefinition() {
                String dateFilter = getDateFilter();
                String roleFilter = getRoleFilter();
                String resActionFilter = getResactionFilter();
                
                ReportDef r = new ReportDef();
                r.setSiteId(siteId);
                ReportParams rp = new ReportParams(siteId);
                // what
                rp.setWhat(ReportManager.WHAT_LESSONPAGES);
                // limit to Resources tool:
                rp.setWhatLimitedResourceIds(true);
                rp.setWhatResourceIds(Arrays.asList("/lessonbuilder/page/"));
                if (resActionFilter != null) {
                    rp.setWhatLimitedAction(true);
                    rp.setWhatResourceAction(resActionFilter);
                }
                // when
                rp.setWhen(dateFilter);
                // who
                if (!ReportManager.WHO_ALL.equals(roleFilter)) {
                    rp.setWho(ReportManager.WHO_ROLE);
                    rp.setWhoRoleId(roleFilter);
                }
                // grouping
                List<String> totalsBy = new ArrayList<String>();
                if (dateFilter.equals(ReportManager.WHEN_LAST365DAYS) || dateFilter.equals(ReportManager.WHEN_ALL)) {
                    totalsBy.add(StatsManager.T_DATEMONTH);
                } else {
                    totalsBy.add(StatsManager.T_DATE);
                }
                totalsBy.add(StatsManager.T_USER);
                rp.setHowTotalsBy(totalsBy);
                // sorting
                rp.setHowSort(true);
                if (dateFilter.equals(ReportManager.WHEN_LAST365DAYS) || dateFilter.equals(ReportManager.WHEN_ALL)) {
                    rp.setHowSortBy(StatsManager.T_DATEMONTH);
                } else {
                    rp.setHowSortBy(StatsManager.T_DATE);
                }                           
                rp.setHowSortAscending(false);
                // chart
                rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
                rp.setHowChartType(StatsManager.CHARTTYPE_PIE);
                rp.setHowChartSource(StatsManager.T_USER);
                r.setReportParams(rp);
                
                return r;
            }                   
        };
        return wTab;
    }
    
    /** WidgetTab: By resource */
    protected WidgetTabTemplate getWidgetTabByResource(String panelId) {
        WidgetTabTemplate wTab = new WidgetTabTemplate(panelId, LessonsWidget.this.siteId) {
            private static final long   serialVersionUID    = 1L;

            @Override
            public List<Integer> getFilters() {
                return Arrays.asList(FILTER_DATE, FILTER_ROLE);
            }

            @Override
            public boolean useChartReportDefinitionForTable() {
                return false;
            }
            
            @Override
            public ReportDef getTableReportDefinition() {
                ReportDef r = getChartReportDefinition();
                ReportParams rp = r.getReportParams();
                rp.setHowTotalsBy(Arrays.asList(StatsManager.T_RESOURCE));
                rp.setHowSort(true);
                rp.setHowSortBy(StatsManager.T_TOTAL);
                rp.setHowSortAscending(false);
                r.setReportParams(rp);
                return r;
            }
            
            @Override
            public ReportDef getChartReportDefinition() {
                String dateFilter = getDateFilter();
                String roleFilter = getRoleFilter();
                String resActionFilter = getResactionFilter();
                
                ReportDef r = new ReportDef();
                r.setSiteId(siteId);
                ReportParams rp = new ReportParams(siteId);
                // what
                rp.setWhat(ReportManager.WHAT_LESSONPAGES);
                // limit to Resources tool:
                rp.setWhatLimitedResourceIds(true);
                rp.setWhatResourceIds(Arrays.asList("/lessonbuilder/page/"));
                if (resActionFilter != null) {
                    rp.setWhatLimitedAction(true);
                    rp.setWhatResourceAction(resActionFilter);
                }
                // when
                rp.setWhen(dateFilter);
                // who
                if (!ReportManager.WHO_ALL.equals(roleFilter)) {
                    rp.setWho(ReportManager.WHO_ROLE);
                    rp.setWhoRoleId(roleFilter);
                }
                // grouping
                List<String> totalsBy = new ArrayList<String>();
                if (dateFilter.equals(ReportManager.WHEN_LAST365DAYS) || dateFilter.equals(ReportManager.WHEN_ALL)) {
                    totalsBy.add(StatsManager.T_DATEMONTH);
                } else {
                    totalsBy.add(StatsManager.T_DATE);
                }
                totalsBy.add(StatsManager.T_RESOURCE);
                rp.setHowTotalsBy(totalsBy);
                // sorting
                rp.setHowSort(true);
                if (dateFilter.equals(ReportManager.WHEN_LAST365DAYS) || dateFilter.equals(ReportManager.WHEN_ALL)) {
                    rp.setHowSortBy(StatsManager.T_DATEMONTH);
                } else {
                    rp.setHowSortBy(StatsManager.T_DATE);
                }                           
                rp.setHowSortAscending(false);
                // chart
                rp.setHowPresentationMode(ReportManager.HOW_PRESENTATION_BOTH);
                rp.setHowChartType(StatsManager.CHARTTYPE_PIE);
                rp.setHowChartSource(StatsManager.T_RESOURCE);
                r.setReportParams(rp);
                
                return r;
            }                   
        };
        return wTab;
    }

    private PrefsData getPrefsdata() {

        if (prefsdata == null) {
            prefsdata = Locator.getFacade().getStatsManager().getPreferences(siteId, true);
        }
        return prefsdata;
    }
    
    /** Return total (existent) files (excluding collections). */
    private int getTotalPages() {

        if (totalPages == -1) {
            try {
                totalPages = Locator.getFacade().getStatsManager().getTotalLessonPages(siteId);
            } catch (Exception e){
                totalPages = 0;
            }       
        }
        return totalPages;
    }
}
