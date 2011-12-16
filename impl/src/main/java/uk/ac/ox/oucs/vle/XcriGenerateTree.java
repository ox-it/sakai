package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.xcri.profiles.x12.catalog.CatalogDocument;


public class XcriGenerateTree implements GenerateTree {
	
	private static final Log log = LogFactory.getLog(XcriGenerateTree.class);
	
	/**
	 * 
	 */
	private String inputSource;
	public void setSource(String inputSource) {
		this.inputSource = inputSource;
	}
	
	/**
	 * 
	 */
	private JsonFactory factory;
	public void setFactory(JsonFactory factory) {
		this.factory = factory;
	}
	
	private JsonGenerator generator;
	
	private final static String XCRI_URL = "http://localhost:8080/test/XCRI_test.xml"; 

	public String generateDepartmentTree() {
		
		StringWriter writer = new StringWriter();
		
		try {
			// Bind the incoming XML to an XMLBeans type.
			URL xcri = new URL(inputSource);
			CatalogDocument catalog = CatalogDocument.Factory.parse(xcri.openStream());
		
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
	
	public static void main(String[] args) {
		String json = null;
		try {	
			XcriGenerateTree reader = new XcriGenerateTree();
			reader.setSource(XCRI_URL);
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
