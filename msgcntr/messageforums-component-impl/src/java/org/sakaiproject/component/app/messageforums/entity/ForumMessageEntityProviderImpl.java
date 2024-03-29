package org.sakaiproject.component.app.messageforums.entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.*;
import org.sakaiproject.api.app.messageforums.entity.ForumMessageEntityProvider;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.*;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;

import java.text.DateFormat;
import java.util.*;

public class ForumMessageEntityProviderImpl implements ForumMessageEntityProvider,
    AutoRegisterEntityProvider, PropertyProvideable, RESTful, RequestStorable, RequestAware, ActionsExecutable {

	private DiscussionForumManager forumManager;
  	public void setForumManager(DiscussionForumManager forumManager) {
  		this.forumManager = forumManager;
  	}
	/**
	 *
	 */
	private PrivateMessageManager privateMessageManager;
	public void setPrivateMessageManager(PrivateMessageManager privateMessageManager) {
		this.privateMessageManager = privateMessageManager;
	}
	public PrivateMessageManager getPrivateMessageManager() {
		return privateMessageManager;
	}

	/**
	 *
	 */
	private UIPermissionsManager uiPermissionsManager;
	public void setUiPermissionsManager(UIPermissionsManager uiPermissionsManager) {
		this.uiPermissionsManager = uiPermissionsManager;
	}
	public UIPermissionsManager getUiPermissionsManager() {
		return uiPermissionsManager;
	}

	/**
	 *
	 */
	private MessageForumsMessageManager messageManager;
	public void setMessageManager(MessageForumsMessageManager messageManager) {
		this.messageManager = messageManager;
	}
	public MessageForumsMessageManager getMessageManager() {
		return messageManager;
	}
  
  	/**
  	 * 
  	 */
	/** Dependency: ServerConfigurationService. */
	protected org.sakaiproject.component.api.ServerConfigurationService serverConfigurationService = null;
	public void setServerConfigurationService(org.sakaiproject.component.api.ServerConfigurationService service) {
		serverConfigurationService = service;
	}

  	/**
  	 * 
  	 */
	private MessageParsingService messageParsingService;
	public void setMessageParsingService(
			MessageParsingService messageParsingService) {
		this.messageParsingService = messageParsingService;
	}
  

  private static final Log LOG = LogFactory.getLog(ForumMessageEntityProviderImpl.class);
	private static final String MESSAGECENTER_BUNDLE = "org.sakaiproject.api.app.messagecenter.bundle.Messages";

	private static final String LAST_REVISE_BY = "cdfm_last_revise_msg";
	private static final String LAST_REVISE_ON = "cdfm_last_revise_msg_on";


	private RequestStorage requestStorage;
  public void setRequestStorage(RequestStorage requestStorage) {
      this.requestStorage = requestStorage;
  }
  
  private RequestGetter requestGetter;
  public void setRequestGetter(RequestGetter requestGetter){
  	this.requestGetter = requestGetter;
  }
  
  public String getEntityPrefix() {
    return ENTITY_PREFIX;
  }

  public boolean entityExists(String id) {
    Topic topic = null;
    try {
      topic = forumManager.getTopicById(Long.valueOf(id));
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return (topic != null);
  }

  public List<String> findEntityRefs(String[] prefixes, String[] name, String[] searchValue,
      boolean exactMatch) {
    List<String> rv = new ArrayList<String>();

    String userId = null;
    String siteId = null;
    String topicId = null;

    if (ENTITY_PREFIX.equals(prefixes[0])) {

      for (int i = 0; i < name.length; i++) {
        if ("context".equalsIgnoreCase(name[i]) || "site".equalsIgnoreCase(name[i]))
          siteId = searchValue[i];
        else if ("user".equalsIgnoreCase(name[i]) || "userId".equalsIgnoreCase(name[i]))
          userId = searchValue[i];
        else if ("topic".equalsIgnoreCase(name[i]) || "topicId".equalsIgnoreCase(name[i]))
          topicId = searchValue[i];
        else if ("parentReference".equalsIgnoreCase(name[i])) {
          String[] parts = searchValue[i].split("/");
          topicId = parts[parts.length - 1];
        }
      }
      
      String siteRef = siteId;
      if(siteRef != null && !siteRef.startsWith("/site/")){
    	  siteRef = "/site/" + siteRef;
      }
      // TODO: support search by something other then topic id...
      if (topicId != null) {
        List<Message> messages =
          forumManager.getTopicByIdWithMessagesAndAttachments(Long.valueOf(topicId)).getMessages();
        for (int i = 0; i < messages.size(); i++) {
          // TODO: authz is way too basic, someone more hip to message center please improve...
          //This should also allow people with read access to an item to link to it
          if (forumManager.isInstructor(userId, siteRef)
              || userId.equals(messages.get(i).getCreatedBy())) {
            rv.add("/" + ENTITY_PREFIX + "/" + messages.get(i).getId().toString());
          }
        }
      }
    }

    return rv;
  }

  public Map<String, String> getProperties(String reference) {
    Map<String, String> props = new HashMap<String, String>();
    Message message =
      forumManager.getMessageById(Long.valueOf(reference.substring(reference.lastIndexOf("/") + 1)));

    props.put("title", message.getTitle());
    props.put("author", message.getCreatedBy());
    if (message.getCreated() != null)
      props.put("date", DateFormat.getInstance().format(message.getCreated()));
    if (message.getModifiedBy() != null) {
      props.put("modified_by", message.getModifiedBy());
      props.put("modified_date", DateFormat.getInstance().format(message.getModified()));
    }
    props.put("label", message.getLabel());
    if (message.getDraft() != null)
      props.put("draft", message.getDraft().toString());
    if (message.getApproved() != null)
      props.put("approved", message.getApproved().toString());
    if (message.getGradeAssignmentName() != null)
      props.put("assignment_name", message.getGradeAssignmentName());
    
    return props;
  }

  public String getPropertyValue(String reference, String name) {
    // TODO: don't be so lazy, just get what we need...
    Map<String, String> props = getProperties(reference);
    return props.get(name);
  }

  public void setPropertyValue(String reference, String name, String value) {
	  // This does nothing for now... we could all the setting of many published assessment properties
	  // here though... if you're feeling jumpy feel free.
  }

  	/**
  	 * 
  	 */
    public String createEntity(EntityReference ref, Object entity,
                               Map<String, Object> params) {

      String userId = UserDirectoryService.getCurrentUser().getId();
      if (userId == null || "".equals(userId)){
        throw new SecurityException("Could not create entity, permission denied: " + ref);
      }

      Long messageId = null;

      if (entity.getClass().isAssignableFrom(DecoratedMessage.class)) {
        // if they instead pass in the DecoratedMessage object
        DecoratedMessage dMessage = (DecoratedMessage) entity;
        if (messageId == null && dMessage.getMessageId() != null) {
          messageId = dMessage.getMessageId();
        }

        Message replyToMessage = forumManager.getMessageById(dMessage.getReplyTo());
        DiscussionTopic topic = forumManager.getTopicById(dMessage.getTopicId());


        if (!forumManager.canUserPostMessage(topic.getId(), "createEntity")) {
          throw new SecurityException("Could not create entity, permission denied: " + ref);
        }
        // We only handle discussion messages :-(
        Message aMsg = messageManager.createDiscussionMessage();
        DiscussionForum forum = (DiscussionForum)topic.getBaseForum();

        if (aMsg != null) {
          StringBuilder alertMsg = new StringBuilder();
          aMsg.setTitle(FormattedText.processFormattedText(dMessage.getTitle(), alertMsg));
          String body = (forum.getMarkupFree())?
                  messageParsingService.parse(dMessage.getBody()):
                  FormattedText.processFormattedText(dMessage.getBody(), alertMsg);
          aMsg.setBody(body);

          if (userId!=null) {
            aMsg.setAuthor(getUserNameOrEid());
            aMsg.setModifiedBy(getUserNameOrEid());
          } else if (userId==null && this.forumManager.getAnonRole()==true) {
            aMsg.setAuthor(".anon");
            aMsg.setModifiedBy(".anon");
          }

          aMsg.setDraft(Boolean.FALSE);
          aMsg.setDeleted(Boolean.FALSE);

          // if the topic is moderated, we want to leave approval null.
          // if the topic is not moderated, all msgs are approved
          // if the author has moderator perm, the msg is automatically approved\

          if (!(topic.getModerated().booleanValue() &&
                  uiPermissionsManager.isModeratePostings(topic, forum))) {
            aMsg.setApproved(Boolean.TRUE);
          }

          aMsg.setTopic(topic);
          aMsg.setInReplyTo(replyToMessage);

          forumManager.saveMessage(aMsg);
          messageId = aMsg.getId();
        }
      } else {
        throw new IllegalArgumentException("Invalid entity for creation, must be Message or DecoratedMessage object");
      }

      if (null == messageId) {
        return null;
      }
      return messageId.toString();
    }

	public Object getSampleEntity() {
		return new DecoratedMessage();
	}

	public String getUserNameOrEid()
	{
		try {

			String currentUserId = UserDirectoryService.getCurrentUser().getId();

			String userString = "";
			userString = UserDirectoryService.getUser(currentUserId).getDisplayName();
			String userEidString = "";
			userEidString = UserDirectoryService.getUser(currentUserId).getDisplayId();

			if((userString != null && userString.length() > 0) &&
					serverConfigurationService.getBoolean("msg.displayEid", true)) {
				return userString + " (" + userEidString + ")";

			}  else if ((userString != null && userString.length() > 0) &&
					!serverConfigurationService.getBoolean("msg.displayEid", true)) {
				return userString;

			} else {
				return userEidString;
			}

		} catch(Exception e) {
			e.printStackTrace();
		}

		return UserDirectoryService.getCurrentUser().getId();
	}

	public void updateEntity(EntityReference ref, Object entity,
	                         Map<String, Object> params) {

		String userId = UserDirectoryService.getCurrentUser().getId();
		if (userId == null || "".equals(userId)){
			throw new SecurityException("Could not get entity, permission denied: " + ref);
		}

		String id = ref.getId();
		if (id == null || "".equals(id)) {
			throw new IllegalArgumentException("Cannot update, No id in provided reference: " + ref);
		}

		Message message = forumManager.getMessageById(new Long(ref.getId()));
		if (null == message) {
			throw new IllegalArgumentException("Cannot update, No message in provided reference: " + ref);
		}

		if (entity.getClass().isAssignableFrom(DecoratedMessage.class)) {
			// if they instead pass in the DecoratedMessage object
			DecoratedMessage dMessage = (DecoratedMessage) entity;

			DiscussionTopic topic = forumManager.getTopicById(dMessage.getTopicId());
			DiscussionForum forum = (DiscussionForum)topic.getBaseForum();

			if(!uiPermissionsManager.isReviseAny(topic, forum) && !(message.getCreatedBy().equals(userId)
					&& uiPermissionsManager.isReviseOwn(topic, forum))) {
				throw new SecurityException("Could not create entity, permission denied: " + ref);
			}

			// This is duplicated from the tool and should be refactored out into the service.
			String revisedInfo = getResourceBundleString(LAST_REVISE_BY);

			revisedInfo += getUserNameOrEid();

			revisedInfo  += " " + getResourceBundleString(LAST_REVISE_ON);
			Date now = new Date();
			revisedInfo += now.toString();

			// Need a different header if it's a markup free forum.
			if (forum.getMarkupFree())
			{
				revisedInfo = revisedInfo + "\n";
			} else {
				revisedInfo = "<p class=\"lastRevise textPanelFooter\">"+ revisedInfo + " </p>";
			}

			revisedInfo = revisedInfo.concat(dMessage.getBody());

			StringBuilder alertMsg = new StringBuilder();
			message.setTitle(FormattedText.processFormattedText(dMessage.getTitle(), alertMsg));
			String filteredBody = forum.getMarkupFree()?
					messageParsingService.parse(revisedInfo):
					FormattedText.processFormattedText(revisedInfo, alertMsg);
			message.setBody(filteredBody);
			message.setDraft(Boolean.FALSE);
			message.setModified(new Date());

			// Note. This seems to be a silly thing to do, but if the topic is not set
			// we get a hibernate lazyloading exception because the session is lost.
			message.setTopic((DiscussionTopic)forumManager
					.getTopicByIdWithMessages(dMessage.getTopicId()));

			if (message.getInReplyTo() != null) {
				//grab a fresh copy of the message incase it has changes (staleobjectexception)
				message.setInReplyTo(forumManager.getMessageById(message.getInReplyTo().getId()));
			}

			forumManager.saveMessage(message);
		}

	}

	public static String getResourceBundleString(String key)
	{
		final ResourceLoader rb = new ResourceLoader(MESSAGECENTER_BUNDLE);
		return rb.getString(key);
	}

  public Object getEntity(EntityReference ref) {

	  String userId = UserDirectoryService.getCurrentUser().getId();
	  if (userId == null || "".equals(userId)){
		  throw new SecurityException("Could not get entity, permission denied: " + ref);
	  }
	  Message message = forumManager.getMessageById(new Long(ref.getId()));
	  if (null == message) {
		  throw new IllegalArgumentException("IdUnusedException in Resource Entity Provider");
	  }
	  DiscussionTopic topic = forumManager.getTopicById(message.getTopic().getId());
	  if (null == topic) {
		  throw new IllegalArgumentException("IdInvalidException in Resource Entity Provider");
	  }
	  DiscussionForum forum = forumManager.getForumById(topic.getBaseForum().getId());
	  DecoratedMessage dMessage = null;

	  if (!message.getDeleted()){

		  List<String> attachments = new ArrayList<String>();
		  if(message.getHasAttachments()){
			  for(Attachment attachment : (List<Attachment>) message.getAttachments()){
				  attachments.add(attachment.getAttachmentName());
			  }
		  }

		  Map msgIdReadStatusMap = forumManager.getReadStatusForMessagesWithId(
				  Collections.singletonList(message.getId()), userId);
		  Boolean readStatus = (Boolean)msgIdReadStatusMap.get(message.getId());
		  if(readStatus == null)
			  readStatus = Boolean.FALSE;


		  dMessage =
				  new DecoratedMessage(message.getId(), message.getTopic().getId(),
						  message.getTitle(),
						  forum.getMarkupFree() ? messageParsingService.format(message.getBody()) : message.getBody(),
						  "" + message.getModified().getTime(),
						  attachments, Collections.EMPTY_LIST,
						  message.getAuthor(), getProfileImageURL(message.getAuthorId()),
						  message.getInReplyTo() == null ? null : message.getInReplyTo().getId(),
								  "" + message.getCreated().getTime(), readStatus.booleanValue(), "", "");
	  }

	  return dMessage;
	  // TODO Auto-generated method stub
  }

  public void deleteEntity(EntityReference ref, Map<String, Object> params) {
	  // TODO Auto-generated method stub

  }

	private String getProfileImageURL(String authorId) {

		if (null == authorId || authorId.trim().length() == 0 ) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(serverConfigurationService.getServerUrl());
		sb.append("/direct/profile/");
		sb.append(authorId);
		sb.append("/image/thumb");
		return sb.toString();
	}
  
  public List<DecoratedMessage> findReplies(List<Message> messages, Long messageId, Long topicId, Map msgIdReadStatusMap){
	  List<DecoratedMessage> replies = new ArrayList<DecoratedMessage>();

	  for (Message message : messages) {
		  DiscussionTopic dTopic = forumManager.getTopicById(message.getTopic().getId());
		  DiscussionForum dForum = forumManager.getForumById(dTopic.getBaseForum().getId());
		  if(message.getInReplyTo() != null){
			  if(messageId.equals(message.getInReplyTo().getId())){
				  if(!message.getDeleted()){
					  List<String> attachments = new ArrayList<String>();
					  if(message.getHasAttachments()){
						  for(Attachment attachment : (List<Attachment>) message.getAttachments()){
							  attachments.add(attachment.getAttachmentName());
						  }
					  }
					  Boolean readStatus = (Boolean)msgIdReadStatusMap.get(message.getId());
					  if(readStatus == null)
						  readStatus = Boolean.FALSE;

					  DecoratedMessage dMessage = new DecoratedMessage(message
							  .getId(), topicId, 
							  message.getTitle(),
							  dForum.getMarkupFree() ? messageParsingService.format(message.getBody()) : message.getBody(), 
							  "" + message.getModified().getTime(),
							  attachments, findReplies(messages, message.getId(),
									  topicId, msgIdReadStatusMap), message.getAuthor(), message.getInReplyTo() == null ? null : message.getInReplyTo().getId(),
											  "" + message.getCreated().getTime(), readStatus.booleanValue(), "", "");
					  replies.add(dMessage);
				  }		  
			  }
		  }		  
	  }

	  Collections.sort(replies, new Comparator<DecoratedMessage>(){

		public int compare(DecoratedMessage arg0, DecoratedMessage arg1) {
			Long date1 = Long.parseLong(arg0.getCreatedOn());
			Long date2 = Long.parseLong(arg1.getCreatedOn());
			return date1.compareTo(date2);
		}
		  
	  });
	  
	  return replies;
  }
  
  public List<DecoratedMessage> generateFlattenedMessagesListHelper(List<DecoratedMessage> messages, int indent){
		List<DecoratedMessage> flattenedList = new ArrayList<DecoratedMessage>();
		for (DecoratedMessage message : messages) {
			message.setIndentIndex(indent);
			List<DecoratedMessage> helperList = new ArrayList<DecoratedMessage>();
			if(message.getReplies().size() > 0){
				helperList = generateFlattenedMessagesListHelper(message.getReplies(), indent+1);
			}
			message.setReplies(null);
			flattenedList.add(message);
			flattenedList.addAll(helperList);
		}
		return flattenedList;
	}

  public List<?> getEntities(EntityReference ref, Search search) {
	  
	  String topicId = "";
	  String typeUuid = "";
	  String siteId = "";
	  String userId = UserDirectoryService.getCurrentUser().getId();
	  if (userId == null || "".equals(userId)){
		  return null;
	  }
	  if (! search.isEmpty()) {
		  Restriction topicRes = search.getRestrictionByProperty("topicId");
		  if(topicRes != null){
			  topicId = topicRes.getStringValue();
		  }
		  Restriction typeRes = search.getRestrictionByProperty("typeUuid");
		  if(typeRes != null){
			  typeUuid = typeRes.getStringValue();
		  }
		  Restriction siteRes = search.getRestrictionByProperty("siteId");
		  if(siteRes != null){
			  siteId = siteRes.getStringValue();
		  }
	  }
	  List<DecoratedMessage> dMessages = new ArrayList<DecoratedMessage>();
	  

	  if(topicId != null && !"".equals(topicId)){
		  List<Message> messages =
			  forumManager.getTopicByIdWithMessagesAndAttachments(new Long(topicId)).getMessages();

		  DiscussionTopic dTopic = forumManager.getTopicById(Long.valueOf(topicId));
		  DiscussionForum dForum = forumManager.getForumById(dTopic.getBaseForum().getId());
		  siteId = forumManager.getContextForForumById(dForum.getId());

		  //make sure the user has access too this forum and topic and site:
		  if(dForum.getDraft().equals(Boolean.FALSE) && dTopic.getDraft().equals(Boolean.FALSE) && SecurityService.unlock(userId, SiteService.SITE_VISIT, "/site/" + siteId)){

			  if (getUiPermissionsManager().isRead(dTopic.getId(), false, false, userId, siteId))
			  {

				  messages = filterModeratedMessages(messages, dTopic, dForum, userId, siteId);
				  List<Long> messageIds = new ArrayList<Long>();
				  for (Message message : messages) {
					  if(message != null && !message.getDraft().booleanValue() && !message.getDeleted().booleanValue())
					  {
						  messageIds.add(message.getId());
					  }
				  }

				  Map msgIdReadStatusMap = forumManager.getReadStatusForMessagesWithId(messageIds, userId);
				  for (Message message : messages) {
					  if(message.getInReplyTo() == null){
						  if(!message.getDeleted()){
							  //this is a top message, so now create the replies list (if any exists)
							  List<String> attachments = new ArrayList<String>();
							  if(message.getHasAttachments()){
								  for(Attachment attachment : (List<Attachment>) message.getAttachments()){
									  attachments.add(attachment.getAttachmentName());
								  }
							  }
							  Boolean readStatus = (Boolean)msgIdReadStatusMap.get(message.getId());
							  if(readStatus == null)
								  readStatus = Boolean.FALSE;

							  DecoratedMessage dMessage = new DecoratedMessage(message
									  .getId(), new Long(topicId),
									  message.getTitle(),
									  dForum.getMarkupFree() ? messageParsingService.format(message.getBody()) : message.getBody(), 
									  "" + message.getModified().getTime(),
									  attachments, findReplies(messages, message.getId(),
											  new Long(topicId), msgIdReadStatusMap), message.getAuthor(), message.getInReplyTo() == null ? null : message.getInReplyTo().getId(),
													  "" + message.getCreated().getTime(), readStatus.booleanValue(), "", "");				  

							  dMessages.add(dMessage);
						  }
					  }
				  }
			  }
		  }
		  //return a sorted list
		  Collections.sort(dMessages, new Comparator<DecoratedMessage>(){

			  public int compare(DecoratedMessage arg0, DecoratedMessage arg1) {
				  Long date1 = Long.parseLong(arg0.getCreatedOn());
				  Long date2 = Long.parseLong(arg1.getCreatedOn());
				  return date1.compareTo(date2);
			  }

		  });
		  //now that we have the hierarchy and ordered list, we need to flatten the returned list since there is a max depth
		  //the entity broken can return  (currently set at 8)
		  List<DecoratedMessage> flattenedList = new ArrayList<DecoratedMessage>();
		  for (DecoratedMessage message : dMessages) {

			  List<DecoratedMessage> helperList = new ArrayList<DecoratedMessage>();
			  if(message.getReplies().size() > 0){
				  helperList = generateFlattenedMessagesListHelper(message.getReplies(), 1);
			  }
			  //clear out the replies field since this can return bad data with the max depth issues.
			  message.setReplies(null);
			  flattenedList.add(message);
			  flattenedList.addAll(helperList);
		  }

		  dMessages = flattenedList;


	  }else if(typeUuid != null && !"".equals(typeUuid) && siteId != null && !"".equals(siteId)){
		  List decoratedPvtMsgs= getPrivateMessageManager().getMessagesByTypeByContext(typeUuid, siteId, userId, PrivateMessageManager.SORT_COLUMN_DATE,
				  PrivateMessageManager.SORT_DESC);

		  for (PrivateMessage pvtMessage : (List<PrivateMessage>) decoratedPvtMsgs) {
			  PrivateMessage initPvtMessage = getPrivateMessageManager().initMessageWithAttachmentsAndRecipients(pvtMessage);
			  //this is a top message, so now create the replies list (if any exists)
			  List<String> attachments = new ArrayList<String>();
			  if(initPvtMessage.getHasAttachments()){
				  for(Attachment attachment : (List<Attachment>) initPvtMessage.getAttachments()){
					  attachments.add(attachment.getAttachmentName());
				  }
			  }

			  //getRecipients() is filtered for this particular user i.e. returned list of only one PrivateMessageRecipient object
			  boolean read = false;
			  for (Iterator iterator = pvtMessage.getRecipients().iterator(); iterator.hasNext();)
			  {
				  PrivateMessageRecipient el = (PrivateMessageRecipient) iterator.next();
				  if (el != null){
					  read = el.getRead().booleanValue();
				  }
			  }


			  DecoratedMessage dMessage = new DecoratedMessage(pvtMessage
					  .getId(), null, pvtMessage.getTitle(),
					  pvtMessage.getBody(), "" + pvtMessage.getModified().getTime(),
					  attachments, null, pvtMessage.getAuthor(), pvtMessage.getInReplyTo() == null ? null : pvtMessage.getInReplyTo().getId(),
							  "" + pvtMessage.getCreated().getTime(), read, pvtMessage.getRecipientsAsText(), pvtMessage.getLabel());				  

			  dMessages.add(dMessage);
		  }	  
	  }
	  
	  return dMessages;
  }
  
  
  public void markAsRead(String userId, String siteId, String readMessageId, int numOfAttempts) {
	  try {
		  Message msg = getMessageManager().getMessageById(new Long(readMessageId));
		  if(msg instanceof PrivateMessage){
			  String toolId = DiscussionForumService.MESSAGES_TOOL_ID;				  
			  getPrivateMessageManager().markMessageAsReadForUser((PrivateMessage) msg, siteId, userId, toolId);					  
		  }else{
			  String toolId = DiscussionForumService.FORUMS_TOOL_ID;
			  String topicId = msg.getTopic().getId().toString();
			  messageManager.markMessageReadForUser(new Long(topicId), new Long(readMessageId), true, userId, siteId, toolId); 
		  }
	  } catch (HibernateOptimisticLockingFailureException holfe) {

		  // failed, so wait and try again
		  try {
			  Thread.sleep(SynopticMsgcntrManager.OPT_LOCK_WAIT);
		  } catch (InterruptedException e) {
			  e.printStackTrace();
		  }

		  numOfAttempts--;

		  if (numOfAttempts <= 0) {
			  System.out
			  .println("ForumMessageEntityProviderImpl: markAsRead: HibernateOptimisticLockingFailureException no more retries left");
			  holfe.printStackTrace();
		  } else {
			  System.out
			  .println("ForumMessageEntityProviderImpl: markAsRead: HibernateOptimisticLockingFailureException: attempts left: "
					  + numOfAttempts);
			  markAsRead(userId, siteId, readMessageId, numOfAttempts);
		  }
	  } catch (Exception e){
		  // failed, so wait and try again
		  try {
			  Thread.sleep(SynopticMsgcntrManager.OPT_LOCK_WAIT);
		  } catch (InterruptedException ie) {
			  ie.printStackTrace();
		  }

		  numOfAttempts--;

		  if (numOfAttempts <= 0) {
			  System.out
			  .println("ForumMessageEntityProviderImpl: markAsRead: no more retries left");
			  e.printStackTrace();
		  } else {
			  System.out
			  .println("ForumMessageEntityProviderImpl: markAsRead:  attempts left: "
					  + numOfAttempts);
			  markAsRead(userId, siteId, readMessageId, numOfAttempts);
		  }
	  }

  }
  
  /**
	 * Given a list of messages, will return all messages that meet at
	 * least one of the following criteria:
	 * 1) message is approved
	 * 
	 */
	private List filterModeratedMessages(List messages, DiscussionTopic topic, DiscussionForum forum, String userId, String siteId)
	{
		List viewableMsgs = new ArrayList();
		if (messages != null && messages.size() > 0)
		{
			boolean hasModeratePerm = getUiPermissionsManager().isModeratePostings(topic, forum, userId, siteId);
			
			if (hasModeratePerm)
				return messages;
			
			Iterator msgIter = messages.iterator();
			while (msgIter.hasNext())
			{
				Message msg = (Message) msgIter.next();
				if (msg.getApproved() != null && msg.getApproved())
					viewableMsgs.add(msg);
			}
		}
		
		return viewableMsgs;
	}
  
	/**
	 * markread/messageId/site/siteId
	 * markread/messageId/site/siteId
	 */
	@EntityCustomAction(action="markread",viewKey=EntityView.VIEW_NEW)
    public boolean getForum(EntityView view, Map<String, Object> params) {
        String messageId = view.getPathSegment(2);
        String siteId = "";
        if("site".equals(view.getPathSegment(3))){
        	siteId = view.getPathSegment(4);
        }
        String userId = UserDirectoryService.getCurrentUser().getId();
		if (userId == null || "".equals(userId) || siteId == null
				|| "".equals(siteId) || messageId == null
				|| "".equals(messageId)) {
			return false;
		}
        
        markAsRead(userId, siteId, messageId, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
        
        return true;
    }
	
	/**
	 * topic/topicId
	 */
	@EntityCustomAction(action="topic",viewKey=EntityView.VIEW_LIST)
    public List<?> getTopicMessagesInSite(EntityView view, Map<String, Object> params) {
        String topicId = view.getPathSegment(2);
        if (topicId == null) {
        	topicId = (String) params.get("topicId");
            if (topicId == null) {
                throw new IllegalArgumentException("topicId must be set in order to get the topic messages, set in params or in the URL /forum_message/topic/topicId");
            }
        }
        List<?> l = getEntities(new EntityReference(ENTITY_PREFIX, ""), 
                new Search("topicId", topicId));
        return l;
    }
	
	/**
	 * private/typeUuid/site/siteId
	 */
	@EntityCustomAction(action="private",viewKey=EntityView.VIEW_LIST)
    public List<?> getPrivateTopicMessagesInSite(EntityView view, Map<String, Object> params) {
        String topicId = view.getPathSegment(2);
        if (topicId == null) {
        	topicId = (String) params.get("typeUuid");
            if (topicId == null) {
                throw new IllegalArgumentException("typeUuid and siteId must be set in order to get the topic messages, set in params or in the URL /forum_message/private/typeUuid/site/siteId");
            }
        }
        String siteId = "";
        if("site".equals(view.getPathSegment(3))){
        	siteId = view.getPathSegment(4);
        }
        List<?> l = getEntities(new EntityReference(ENTITY_PREFIX, ""), 
                new Search(new String[]{"typeUuid", "siteId"}, new String[]{topicId, siteId}));
        return l;
    }


  public String[] getHandledOutputFormats() {
	  // TODO Auto-generated method stub
	  return null;
  }

  public String[] getHandledInputFormats() {
	  // TODO Auto-generated method stub
	  return null;
  }

  public class DecoratedMessagesTopic{
	private String title;
	private Long id;
	private int totalMessages = 0;
	private int totalUnreadMessages = 0;
	
	public DecoratedMessagesTopic(String title, Long id, int totalMessages, int totalUnreadMessages){
		this.title = title;
		this.id = id;
		this.totalMessages = totalMessages;
		this.totalUnreadMessages = totalUnreadMessages;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public int getTotalMessages() {
		return totalMessages;
	}
	public void setTotalMessages(int totalMessages) {
		this.totalMessages = totalMessages;
	}
	public int getTotalUnreadMessages() {
		return totalUnreadMessages;
	}
	public void setTotalUnreadMessages(int totalUnreadMessages) {
		this.totalUnreadMessages = totalUnreadMessages;
	}
	
	
  }

  public class DecoratedMessage{
	 private Long messageId;
	 private Long topicId;
	 private String title;
	 private String body;
	 private String lastModified;
	 private List<String> attachments;
	 private List<DecoratedMessage> replies;
	 private String authoredBy;
	 private int indentIndex = 0;
	  private String profileImageURL;
	  private Long replyTo;
	 private String createdOn;
	 private boolean read;
	 private String recipients;
	 private String label;

	  public DecoratedMessage(){

	  }

	public DecoratedMessage(Long messageId, Long topicId, String title, String body, String lastModified, List<String> attachments, List<DecoratedMessage> replies, String authoredBy, Long replyTo, String createdOn, boolean read, String recipients, String label){
		  this.messageId = messageId;
		  this.topicId = topicId;
		  this.title = title;
		  this.body = body;
		  this.attachments = attachments;
		  this.replies = replies;
		  this.lastModified = lastModified;
		  this.authoredBy = authoredBy;
		  this.replyTo = replyTo;
		  this.createdOn = createdOn;
		  this.read = read;
		  this.recipients = recipients;
		  this.label = label;
	  }

	  public DecoratedMessage(Long messageId, Long topicId, String title,
	                          String body, String lastModified,
	                          List<String> attachments, List<DecoratedMessage> replies,
	                          String authoredBy, String profileImageURL, Long replyTo, String createdOn, boolean read,
	                          String recipients, String label) {

		  this.messageId = messageId;
		  this.topicId = topicId;
		  this.title = title;
		  this.body = body;
		  this.attachments = attachments;
		  this.replies = replies;
		  this.lastModified = lastModified;
		  this.authoredBy = authoredBy;
		  this.profileImageURL = profileImageURL;
		  this.replyTo = replyTo;
		  this.createdOn = createdOn;
		  this.read = read;
		  this.recipients = recipients;
		  this.label = label;
	  }
	  public Long getMessageId() {
		  return messageId;
	  }

	  public void setMessageId(Long messageId) {
		  this.messageId = messageId;
	  }

	  public Long getTopicId() {
		  return topicId;
	  }

	  public void setTopicId(Long topicId) {
		  this.topicId = topicId;
	  }

	  public String getTitle() {
		  return title;
	  }

	  public void setTitle(String title) {
		  this.title = title;
	  }

	  public String getBody() {
		  return body;
	  }

	  public void setBody(String body) {
		  this.body = body;
	  }

	  public List<String> getAttachments() {
		  return attachments;
	  }
	  public void setAttachments(List<String> attachments) {
		  this.attachments = attachments;
	  }

	  public List<DecoratedMessage> getReplies() {
		  return replies;
	  }
	  public void setReplies(List<DecoratedMessage> replies) {
		  this.replies = replies;
	  }
	  public String getLastModified() {
		  return lastModified;
	  }
	  public void setLastModified(String lastModified) {
		  this.lastModified = lastModified;
	  }
	public String getAuthoredBy() {
		return authoredBy;
	}
	public void setAuthoredBy(String authoredBy) {
		this.authoredBy = authoredBy;
	}
	public String getProfileImageURL() {
		return profileImageURL;
	}
	public void setProfileImageURL(String profileImageURL) {
		this.profileImageURL = profileImageURL;
	}
	public int getIndentIndex() {
		return indentIndex;
	}
	public void setIndentIndex(int indentIndex) {
		this.indentIndex = indentIndex;
	}
	public Long getReplyTo() {
		return replyTo;
	}
	public void setReplyTo(Long replyTo) {
		this.replyTo = replyTo;
	}
	public String getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}
	public boolean isRead() {
		return read;
	}
	public void setRead(boolean read) {
		this.read = read;
	}
	public String getRecipients() {
		return recipients;
	}
	public void setRecipients(String recipients) {
		this.recipients = recipients;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

  }


}
