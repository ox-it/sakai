package org.sakaiproject.sitestats.impl.event.detailed.refresolvers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedRef;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.utils.RefResolverUtils;

public class ContentReferenceResolver
{
	private static final Log LOG = LogFactory.getLog(ContentReferenceResolver.class);

	private static final String CONTENT_AVAILABLE = "content.available";
	private static final String CONTENT_DELETE = "content.delete";
	private static final String CONTENT_NEW = "content.new";
	private static final String CONTENT_READ = "content.read";
	private static final String CONTENT_REVISE = "content.revise";

	public static final List<String> CONTENT_RESOLVABLE_EVENTS = Arrays.asList(CONTENT_AVAILABLE, CONTENT_DELETE, CONTENT_NEW, CONTENT_READ, CONTENT_REVISE);

	// Content permissions that we're requiring to resolve references
	private static final String READ_PERM = "content.read";
	public static final List<String> REQUIRED_PERMS = Arrays.asList(READ_PERM);

	public static List<ResolvedRef> resolveReference(String eventType, String ref, ContentHostingService contentHostServ)
	{
		/**
		 * Our goal is to return a List with a capacity that matches exactly the amount of data we are returning.
		 * But firstly, we'll set our return value - eventDetails - to Collections.emptyList(), this ensures we're returning *something*.
		 * Collections.emptyList() is constant (doesn't instantiate anything), and it's immutable.
		 */
		List<ResolvedRef> eventDetails = Collections.emptyList();

		if (StringUtils.isEmpty(ref))
		{
			return eventDetails;
		}

		if (CONTENT_AVAILABLE.equals(eventType) || CONTENT_DELETE.equals(eventType) || CONTENT_NEW.equals(eventType) || CONTENT_READ.equals(eventType) || CONTENT_REVISE.equals(eventType))
		{
			try
			{
				if (ref.startsWith("/content/"))
				{
					ref = ref.substring(8);
					if (contentHostServ.isCollection(ref))
					{
						ContentCollection cc = contentHostServ.getCollection(ref);
						if (cc != null)
						{
							String id = cc.getId();
							if (contentHostServ.isAttachmentResource(id))
							{
								// httpHandler for /access/content/ treats attachments as a special case and delivers a 404 on the containing collections
								return Collections.singletonList(ResolvedRef.newText("Folder", "Hidden attachment folder"));
							}
							else if (contentHostServ.isAvailable(id))
							{
								String directoryName = getDirectoryName(id);
								if (directoryName != null)
								{
									return Collections.singletonList(ResolvedRef.newLink("Folder", directoryName, cc.getUrl()));
								}
							}
							else
							{
								return Collections.singletonList(ResolvedRef.newText("Folder", "Hidden folder"));
							}
						}
					}
					else
					{
						ContentResource cr = contentHostServ.getResource(ref);
						if (cr != null)
						{
							String id = cr.getId();
							String resourceName = RefResolverUtils.getResourceName(cr);
							String fileName = RefResolverUtils.getResourceFileName(id);
							if (resourceName != null)
							{
								ContentCollection parent = cr.getContainingCollection();
								if (contentHostServ.isAttachmentResource(id))
								{
									//Attachment filenames might be revealing too much if the user doesn't have read permissions in the tool that the attachment pertains to.
									return Collections.singletonList(ResolvedRef.newText("File", "Hidden attachment"));
								}
								else if (contentHostServ.isAvailable(id))
								{
									if (resourceName.equals(fileName))
									{
										return Collections.singletonList(ResolvedRef.newLink("File title", resourceName, parent.getUrl()));
									}

									ArrayList<ResolvedRef> resolvedRefs = new ArrayList<>(2);
									resolvedRefs.add(ResolvedRef.newLink("File title", resourceName, parent.getUrl()));
									resolvedRefs.add(ResolvedRef.newText("Filename", fileName));
									return resolvedRefs;
								}
								else
								{
									return Collections.singletonList(ResolvedRef.newText("File", "Hidden resource"));
								}
							}
						}
					}
				}
			}
			catch (IdUnusedException | TypeException iue)
			{
			}
			catch (PermissionException pe)
			{
				return Collections.singletonList(ResolvedRef.newText("File", "You are not authorized to access this resource"));
			}
		}

		return eventDetails;
	}

	private static String getDirectoryName(String collectionId)
	{
		if (collectionId == null)
		{
			return null;
		}

		String[] delims = {"/", "\\"};
		for (String delim : delims)
		{
			int lastIndex = collectionId.lastIndexOf(delim);
			if (lastIndex >= 0)
			{
				if (lastIndex == collectionId.length() - 1)
				{
					// ends with "/"; find second last "/"
					lastIndex = collectionId.lastIndexOf(delim, lastIndex - 1);
					collectionId = collectionId.substring(lastIndex + 1, collectionId.length() - 1);
				}
				else
				{
					collectionId = collectionId.substring(lastIndex + 1);
				}
			}
		}

		return collectionId;
	}
}
