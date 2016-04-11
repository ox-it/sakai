package org.sakaiproject.content.impl;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.*;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.exception.*;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import static org.junit.Assert.fail;

public class ContentCopyIntTest extends SakaiKernelTestBase {

	@BeforeClass
	public static void setUp() throws Exception {
		oneTimeSetup();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		oneTimeTearDown();
	}

	@Test
	public void testSimpleCopy() throws Exception {
		ContentHostingService chs = (ContentHostingService)ComponentManager.get(ContentHostingService.class);
		SessionManager sessionManager = (SessionManager)ComponentManager.get(SessionManager.class);

		// set the user information into the current session
		Session sakaiSession = sessionManager.getCurrentSession();
		sakaiSession.setUserEid("admin");
		sakaiSession.setUserId("admin");
		
		String collectionId = chs.getSiteCollection("original");
		ContentCollectionEdit collection = chs.addCollection(collectionId);
		chs.commitCollection(collection);
		
		String resourceId = addHtmlFile(chs, collectionId+ "index.html", "<html><body><h1>Hello World</h1><a href='other.html'>Other File</a></body></html>");
		addHtmlFile(chs, collectionId+ "other.html", "<html><body><h1>Other File</h1></body></html>");
		
		ContentCopy contentCopy = (ContentCopy)ComponentManager.get(ContentCopy.class);
		ContentCopyContext context = contentCopy.createCopyContext("original", "new", true);
		context.addResource(resourceId);
		contentCopy.copyReferences(context);
		
		try {
			chs.getResource(chs.getSiteCollection("new")+"index.html");
			chs.getResource(chs.getSiteCollection("new")+"other.html");
		} catch (Exception e) {
			fail("Should be present now.");
		}
	}
	
	public void testCopySiteContent() throws Exception {
		ContentHostingService chs = (ContentHostingService)ComponentManager.get(ContentHostingService.class);
		SessionManager sessionManager = (SessionManager)ComponentManager.get(SessionManager.class);
		
		// set the user information into the current session
		Session sakaiSession = sessionManager.getCurrentSession();
		sakaiSession.setUserEid("admin");
		sakaiSession.setUserId("admin");
		
		String collectionId = chs.getSiteCollection("source");
		ContentCollectionEdit collection = chs.addCollection(collectionId);
		chs.commitCollection(collection);
		
		String destId = chs.getSiteCollection("dest");
		
		addHtmlFile(chs, collectionId+ "index.html", "<html><body><h1>Hello World</h1><a href='other.html'>Other File</a></body></html>");
		addHtmlFile(chs, collectionId+ "other.html", "<html><body><h1>Other File</h1></body></html>");
		
		((EntityTransferrer)chs).transferCopyEntities(collectionId, destId, null);
		
		try {
			chs.getResource(destId+"index.html");
			chs.getResource(destId+"other.html");
		} catch (Exception e) {
			fail("Should be present now.");
		}
	}

	private String addHtmlFile(ContentHostingService chs, String resourceId, String content)
			throws PermissionException, IdUsedException, IdInvalidException,
			InconsistentException, ServerOverloadException, OverQuotaException {
		ContentResourceEdit resource = chs.addResource(resourceId);
		resource.setContentType("text/html");
		resource.setContent(content.getBytes());
		chs.commitResource(resource);
		return resourceId;
	}

}
