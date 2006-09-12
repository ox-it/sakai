package org.sakaiproject.hierarchy.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.hierarchy.api.HierarchyService;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;

public class SiteHierarchyServiceTest
{
	private static final Log log = LogFactory
			.getLog(SiteHierarchyServiceTest.class);

	private HierarchyService hierarchyService = null;

	private SiteService siteService = null;

	public void init()
	{
		try
		{
			// list all the sites, and load in each one into some sort of
			// hierachy
			// adding a realm for each site.

			String testRoot = "/siteHierarchy";
			Hierarchy siteBase = hierarchyService.getNode(testRoot);
			if (siteBase != null)
			{
				hierarchyService.deleteNode(siteBase);
			}
			siteBase = hierarchyService.newHierarchy(testRoot);

			List sites = siteService.getSites(SelectionType.ANY, null, null,
					null, SortType.NONE, null);
			List l = new ArrayList();
			for (Iterator is = sites.iterator(); is.hasNext();)
			{
				Site s = (Site) is.next();
				String type = s.getType();
				String title = s.getTitle();
				String firstChar = title.trim().substring(0, 1).toUpperCase();
				Hierarchy site = getPath(getPath(getPath(siteBase, type),
						firstChar), title);

				site.addToproperties("sitetitle", s.getTitle());
				List pages = s.getOrderedPages();
				for (Iterator i = pages.iterator(); i.hasNext();)
				{
					SitePage page = (SitePage) i.next();
					site.addToproperties("pageid", page.getId());
				}
			}
			hierarchyService.save(siteBase);
			print("Sites->", siteBase);

			log
					.warn("Spring Injected Test Sucessfull..... but plesae remove in production ");
		}
		catch (Exception ex)
		{
			log
					.warn(
							"Spring Injected Test Failed..... but plesae remove in production ",
							ex);
			log
					.error("Spring Injected Test Failed..... but plesae remove in production ");
			System.exit(-1);
		}
	}

	private Hierarchy getPath(Hierarchy parent, String newNode)
	{
		Hierarchy h = parent.getChild(parent.getPath() + "/" + newNode);
		if (h == null)
		{
			h = hierarchyService.newHierarchy(parent.getPath() + "/" + newNode);
			parent.addTochildren(h);
		}
		return h;
	}

	private void printList(String indent, Iterator i)
	{
		while (i.hasNext())
		{
			Object o = i.next();
			if (o instanceof Hierarchy)
			{
				print(indent, (Hierarchy) o);
			}
			else if (o instanceof HierarchyProperty)
			{
				print(indent, (HierarchyProperty) o);
			}
			else
			{
				log.info(indent + "Unrecognised Node :" + o);
			}
		}

	}

	private void print(String indent, HierarchyProperty property)
	{
		log.info(indent + "Property " + property.getName() + "("
				+ property.getVersion() + "):" + property.getPropvalue());

	}

	private void print(String indent, Hierarchy hierarchy)
	{
		log.info("Node " + hierarchy.getPath() + "(" + hierarchy.getVersion()
				+ ")");
		printList("      ", hierarchy.getProperties().values().iterator());
		printList("", hierarchy.getChildren().values().iterator());
	}

	public HierarchyService getHierarchyService()
	{
		return hierarchyService;
	}

	public void setHierarchyService(HierarchyService hierarchyService)
	{
		this.hierarchyService = hierarchyService;
	}

	public SiteService getSiteService()
	{
		return siteService;
	}

	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

}
