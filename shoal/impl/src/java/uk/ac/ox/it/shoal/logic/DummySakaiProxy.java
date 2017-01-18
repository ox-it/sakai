package uk.ac.ox.it.shoal.logic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import uk.ac.ox.it.shoal.model.TeachingItem;


public class DummySakaiProxy implements SakaiProxy {
	
	private static final Log LOG = LogFactory.getLog(DummySakaiProxy.class);
	
	private Properties props;

	private SolrServer solrServer;

	public void setSolrServer(SolrServer solrServer) {
		this.solrServer = solrServer;
	}

	public DummySakaiProxy() throws IOException {
//		String name = getClass().getSimpleName()+".properties";
//		InputStream stream = getClass().getResourceAsStream(name);
		props = new Properties();
//		props.load(stream);
	}

	public String getCurrentSiteId() {
		return "currentSiteId";
	}

	public String getCurrentUserId() {
		return "currentUserId";
	}

	public String getCurrentUserDisplayName() {
		return "Current User";
	}

	public boolean isSuperUser() {
		return false;
	}

	public void postEvent(String event, String reference, boolean modify) {
		LOG.info("Posted event: "+ event+ " reference: "+ reference+ " modify: "+ modify);
	}

	public String getSkinRepoProperty() {
		return "skin";
	}

	public String getToolSkinCSS(String skinRepo) {
		return skinRepo+"";
	}

	public boolean getConfigParam(String param, boolean dflt) {
		return Boolean.parseBoolean(props.getProperty(param, Boolean.toString(dflt)));
	}

	public String getConfigParam(String param, String dflt) {
		return props.getProperty(param, dflt);
	}

	@Override
	public TeachingItem getTeachingItem() {
		return null;
	}

	@Override
	public void saveTeachingItem(TeachingItem model) {
	    LOG.info("Adding new teaching item: "+model);
	    model.setId(UUID.randomUUID().toString());
	    model.setUrl("http://example.com");
	    model.setAdded(Instant.now());

		SolrInputDocument document = new SolrInputDocument();
		document.setField("id", model.getId());
		document.setField("title", model.getTitle());
		document.setField("description", model.getDescription());
		document.setField("subject", model.getSubject());
		document.setField("level", model.getLevel());
		document.setField("purpose", model.getPurpose());
		document.setField("interactivity", model.getInteractivity());
		document.setField("type", model.getType());
		document.setField("url", model.getUrl());
		document.setField("author", model.getAuthor());
		document.setField("contact", model.getContact());
		document.setField("added", Date.from(model.getAdded()));
		document.setField("license", model.getLicense());
		document.setField("thumbnail", model.getThumbnail());
		try {
			solrServer.add(document);
			solrServer.commit();
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String saveThumbnail(InputStream inputStream) throws IOException {
		Path image = Files.createTempFile("image", "thumbnail");
		Files.copy(inputStream, image, StandardCopyOption.REPLACE_EXISTING);
		// TODO should be injected, not hard coded.
        String url = "/shoal-tool-1.0-SNAPSHOT/search/thumbnail/" + image.getFileName();
		return url;
	}
}
