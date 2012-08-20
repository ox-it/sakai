package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.xcri.profiles.x12.catalog.CatalogDocument;


public class XcriGenerateTree extends XCRIImport implements GenerateTree {
	
	private static final Log log = LogFactory.getLog(XcriGenerateTree.class);
	
	/**
	 * 
	 */
	private JsonFactory factory;
	public void setFactory(JsonFactory factory) {
		this.factory = factory;
	}
	
	/**
	 * 
	 */
	/*
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	*/
	private JsonGenerator generator;
	
	/**
	 * 
	 * @param inputStream
	 * @return
	 */
	public String process(InputStream inputStream) {
		
		StringWriter writer = new StringWriter();
		
		try {
			CatalogDocument catalog = CatalogDocument.Factory.parse(inputStream);
		
			Set<MyDepartment> myDepartments = new TreeSet<MyDepartment>();
			
			XmlObject[] providers = XcriUtils.selectPath(catalog, "provider");
			
			for (int i=0; i<providers.length; i++) {
				
				Map.Entry<String, String> entry = XcriUtils.getEntry(providers[i], "providerDivision", "code");
				
				if (null != entry) {
					myDepartments.add(new MyDepartment(
						entry.getKey(),
						entry.getValue(),
						XcriUtils.getString(providers[i], "providerIdentifier"), 
						XcriUtils.getString(providers[i], "providerTitle")));
				}
			}
		
			generator = factory.createJsonGenerator(writer);
			generator.writeStartArray();
			startNode(generator, "root", "University of Oxford");
			String lastDivisionCode = null;
			
			for (MyDepartment myDepartment : myDepartments) {
				
				if (!myDepartment.getDivisionCode().equals(lastDivisionCode)) {
					if (lastDivisionCode != null) {
						endNode(generator);
					}
					startNode(generator,myDepartment.getDivisionCode(), myDepartment.getDivisionName());
					lastDivisionCode = myDepartment.getDivisionCode();
				}
					
				startNode(generator,myDepartment.getCode(), myDepartment.getName());
				endNode(generator);
				
			}
			
			if (lastDivisionCode != null) {
				endNode(generator);
			}
					
			generator.writeEndArray();
			generator.close();
			
			return writer.toString();
			
		} catch (XmlException e) {
			log.warn("Problem writing tree file.", e);
			
		} catch (JsonGenerationException e) {
			log.warn("Problem writing tree file.", e);
			
		} catch (IOException e) {
			log.warn("Problem writing tree file.", e);
			
		}
		
		return null;
	}

	/**
	 * 
	 * @param generator
	 * @param id
	 * @param name
	 * @throws JsonGenerationException
	 * @throws IOException
	 */
	private void startNode(JsonGenerator generator, String id, String name) 
			throws JsonGenerationException, IOException	{
		generator.writeStartObject();
		generator.writeObjectFieldStart("attr");
		generator.writeStringField("id", id);
		generator.writeEndObject();
		generator.writeStringField("data", name);
		generator.writeStringField("state", "closed");
		generator.writeArrayFieldStart("children");
	}
	
	private void endNode(JsonGenerator generator) 
			throws JsonGenerationException, IOException	{
		generator.writeEndArray();
		generator.writeEndObject();
	}
		
	/**
	 * 
	 */
	public	String generateDepartmentTree() {
			
		DefaultHttpClient httpclient = new DefaultHttpClient();
		
		try {
			URL xcri = new URL(getXcriURL());
		
			HttpHost targetHost = new HttpHost(xcri.getHost(), xcri.getPort(), xcri.getProtocol());

	        httpclient.getCredentialsProvider().setCredentials(
	                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
	                new UsernamePasswordCredentials(getXcriAuthUser(), getXcriAuthPassword()));

            HttpGet httpget = new HttpGet(xcri.getPath());

            HttpResponse response = httpclient.execute(targetHost, httpget);
            HttpEntity entity = response.getEntity();
            
            return process(entity.getContent());

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
        } catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		} finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }
		
		return null;
	}
	/*
	protected String getXcriURL() {
		if (null != serverConfigurationService) {
			return serverConfigurationService.getString("xcri.url", 
					"http://daisy-feed.socsci.ox.ac.uk/XCRI_SES.php");
		}
		return "http://daisy-feed.socsci.ox.ac.uk/XCRI_SES.php";
	}
	
	protected String getXcriAuthUser() {
		if (null != serverConfigurationService) {
			return serverConfigurationService.getString("xcri.auth.user", "sesuser");
		}
		return "sesuser";
	}
	
	protected String getXcriAuthPassword() {
		if (null != serverConfigurationService) {
			return serverConfigurationService.getString("xcri.auth.password", "blu3D0lph1n");
		}
		return "blu3D0lph1n";
	}
	*/
	public static void main(String[] args) {
		String json = null;
		try {	
			XcriGenerateTree reader = new XcriGenerateTree();
			reader.setFactory(new JsonFactory());
			json = reader.generateDepartmentTree();
			System.out.println(json);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private class MyDepartment implements Comparable<MyDepartment> {
		
		private String divisionCode;
		private String divisionName;
		private String code;
		private String name;
		
		public MyDepartment(String divisionCode, String divisionName, 
							String code, String name) {
			
			this.divisionCode = divisionCode;
			this.divisionName = divisionName;
			this.code = code;
			this.name = name;
		}
		
		public String getDivisionCode() {
			return divisionCode;
		}
		
		public String getDivisionName() {
			return divisionName;
		}
		
		public String getCode() {
			return code;
		}
		
		public String getName() {
			return name;
		}

		public int compareTo(MyDepartment that) {
			
		    final int EQUAL = 0;
			int comparison;
			
			comparison = compare(this.getDivisionName(), that.getDivisionName());
			if ( comparison != EQUAL ) return comparison;	
			
			comparison = compare(this.getDivisionCode(), that.getDivisionCode());
			if ( comparison != EQUAL ) return comparison;	
			
			comparison = compare(this.getName(), that.getName());
			if ( comparison != EQUAL ) return comparison;	
				
			return compare(this.getCode(), that.getCode());
		}
		
		//null safe!
		private int compare(String a, String b) {
			
			final int BEFORE = -1;
		    final int EQUAL = 0;
		    final int AFTER = 1;
		    
			if (null == a) {
				if (null != b) {
					return BEFORE;
				}
				return EQUAL;
			}
			
			if (null == b) {
				if (null != a) {
					return AFTER;
				}
			}
			
			return a.compareToIgnoreCase(b);
		}
	}

}
