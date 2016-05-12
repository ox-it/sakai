/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.importer.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.archive.api.ImportMetadata;
import org.sakaiproject.importer.api.ImportFileParser;
import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.impl.importables.FileResource;
import org.sakaiproject.importer.impl.importables.Folder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ContentPackagingFileParser extends IMSFileParser {
	private static final String CP_SCHEMA_NAME = "IMS Content";
	
	public ContentPackagingFileParser() {
		// add resource translators here (currently not using any):
		resourceHelper = new CPResourceHelper();
		itemHelper = new CPItemHelper();
		fileHelper = new CPFileHelper();
		manifestHelper = new CPManifestHelper();
	}

	public boolean isValidArchive(InputStream fileData) {
		if (super.isValidArchive(fileData)) {
			Document manifest = extractFileAsDOM("/imsmanifest.xml", fileData);
			return CP_SCHEMA_NAME.equals(XPathHelper.getNodeValue("/manifest/metadata/schema", manifest));
		} else return false;
	}
	
	public ImportFileParser newParser() {
		return new ContentPackagingFileParser();
	}

	protected Collection getCategoriesFromArchive(String pathToData) {
		Collection categories = new ArrayList();
		ImportMetadata im;
		Node topLevelItem;
		List topLevelItems = manifestHelper.getTopLevelItemNodes(this.archiveManifest);
		for(Iterator i = topLevelItems.iterator(); i.hasNext(); ) {
			topLevelItem = (Node)i.next();
			im = new BasicImportMetadata();
			im.setId(itemHelper.getId(topLevelItem));
			im.setLegacyTool(itemHelper.getTitle(topLevelItem));
			im.setMandatory(false);
			im.setFileName(".xml");
			im.setSakaiServiceName("ContentHostingService");
			im.setSakaiTool("Resources");
			categories.add(im);
		}
		return categories;
	}
	
	@Override
	protected Collection translateFromNodeToImportables(Node node, String contextPath, int priority, Importable parent) {
		
		//TODO: contextPath not being set/modified/used.
		
		Collection branchOfImportables = new ArrayList();
		String tag = node.getNodeName();
		String id = null;
		if ("item".equals(tag)) {
			id = itemHelper.getResourceId(node);
		} else if ("resource".equals(tag)) {
			id = resourceHelper.getId(node);
		} else if ("file".equals(tag)) {
			id = resourceHelper.getId(node.getParentNode());
		}

		if ("item".equals(tag)) {
			Node resourceNode = manifestHelper.getResourceForId(id, archiveManifest);

			Folder folder = new Folder();
			String title = itemHelper.getTitle(node);
			folder.setTitle(title);
			// TODO handle lang attribute?
			String desc = itemHelper.getDescription(node);
			folder.setDescription(desc);
			String href = resourceHelper.getHref(resourceNode);
			// TODO handle case where there is no associated resource element (and href is "").
			folder.setPath(href);
			folder.setSequenceNum(priority);

			if (parent != null) {
				folder.setParent(parent);
				folder.setLegacyGroup(parent.getLegacyGroup());
			} else {
				parent = folder;
				parent.setLegacyGroup(itemHelper.getTitle(node));
			}
			
			branchOfImportables.add(folder);
			
			if (resourceNode != null) {
				// add the resource node, will add files listed there to the folder relating to this item node:
				branchOfImportables.addAll(translateFromNodeToImportables(resourceNode, contextPath, priority, parent));
			}
			
			//TODO parent? Is currently set to same instance...
			List itemChildren = XPathHelper.selectNodes("./item", node);
			for (int i = 0; i < itemChildren.size(); i++) {
				branchOfImportables.addAll(translateFromNodeToImportables((Node)itemChildren.get(i), contextPath, i+1, parent));
			}

			
		} else if ("file".equals(tag)) {
			FileResource file = new FileResource();
			try {
				String fileName = fileHelper.getFilenameForNode(node);
				file.setFileName(fileName);
				file.setInputStream(fileHelper.getInputStreamForNode(node, contextPath));
				file.setDestinationResourcePath(fileHelper.getFilePathForNode(node, contextPath));
				file.setContentType(this.mimeTypes.getContentType(fileName));
				file.setTitle(fileHelper.getTitle(node));
				// TODO Need to set priority properly for files with regard to sibling folders.
//				file.setSequenceNum(priority);
				
				if (parent != null) {
					file.setParent(parent);
					file.setLegacyGroup(parent.getLegacyGroup());
				} else file.setLegacyGroup("");
			} catch (IOException e) {
				resourceMap.remove(resourceHelper.getId(node.getParentNode()));
				return branchOfImportables;
			}
			branchOfImportables.add(file);
			resourceMap.remove(resourceHelper.getId(node.getParentNode()));
			return branchOfImportables;
			
		} else if ("resource".equals(tag)) {
			// add all file element children:
			List itemChildren = XPathHelper.selectNodes("./file", node);
			for (int i = 0; i < itemChildren.size(); i++) {
				Node child = (Node)itemChildren.get(i);
				branchOfImportables.addAll(translateFromNodeToImportables(child, contextPath, i+1, parent));
			}
			resourceMap.remove(id);
		}
		return branchOfImportables;
	}

	protected boolean isCompoundDocument(Node node, Document resourceDescriptor) {
		// TODO Auto-generated method stub
		return false;
	}
	
	protected class CPResourceHelper extends ResourceHelper {
		
		public String getTitle(Node resourceNode) {
			
			String title = null;
			Node itemNode = XPathHelper.selectNode("//item[@identifierref='" + this.getId(resourceNode) + "']", resourceNode.getOwnerDocument());
			if (itemNode != null) {
				title = XPathHelper.getNodeValue("./title", itemNode);
			}
			return title;
		}
		
		public String getType(Node resourceNode) {
			return XPathHelper.getNodeValue("./@type", resourceNode);
		}
		
		public String getId(Node resourceNode) {
			return XPathHelper.getNodeValue("./@identifier", resourceNode);
		}
		
		public Document getDescriptor(Node resourceNode) {
			// TODO: not very helpful, but what else to do:
			return archiveManifest;
		}

		public String getDescription(Node resourceNode) {
			//TODO: no need to get from item anymore?
			String descrip = "";
			Node itemNode = XPathHelper.selectNode("//item[@identifierref='" + this.getId(resourceNode) + "']", resourceNode.getOwnerDocument());
			if (itemNode != null)
				descrip = XPathHelper.getNodeValue("./imsmd:lom/imsmd:general/imsmd:description/imsmd:langstring", itemNode);
			if (descrip.equals(""))
				descrip = XPathHelper.getNodeValue("./imsmd:lom/imsmd:general/imsmd:description/imsmd:langstring", resourceNode);
			
			return descrip;
		}
		
		public boolean isFolder(Document resourceDescriptor) {
			//TODO: not used.
			return false;
		}
	}
	
	protected class CPItemHelper extends ItemHelper {

		public String getId(Node itemNode) {
			return XPathHelper.getNodeValue("./@identifier", itemNode);
		}

		public String getTitle(Node itemNode) {
			String title = XPathHelper.getNodeValue("./title",itemNode);
			return title;
		}

		public String getDescription(Node itemNode) {
			return XPathHelper.getNodeValue("./imsmd:lom/imsmd:general/imsmd:description/imsmd:langstring", itemNode);
		}
		
	}
	
	protected class CPManifestHelper extends ManifestHelper {

		public List getTopLevelItemNodes(Document manifest) {
			// Gets top level 'item' elements. If there's only one item at the top level
			// it's probably a container for the main items below, so look one level down:
			
			List items = XPathHelper.selectNodes("//organization/item", manifest);
			if (items != null && items.size() > 1) return items;

			items = XPathHelper.selectNodes("//organization/item/item", manifest);
			
			if (items != null && items.size() > 1) return items;
			
			return XPathHelper.selectNodes("//organization/item", manifest);
		}
		
		public List getItemNodes(Document manifest) {
			return XPathHelper.selectNodes("//item", manifest);
		}

		public Node getResourceForId(String resourceId, Document manifest) {
			return XPathHelper.selectNode("//resource[@identifier='" + resourceId + "']", archiveManifest);
		}

		public List getResourceNodes(Document manifest) {
			return XPathHelper.selectNodes("//resource", manifest);
		}
		
	}
	
	protected class CPFileHelper extends FileHelper {
		
		public String getFilePathForNode(Node node, String basePath) {
			return XPathHelper.getNodeValue("./@href", node);
		}
		
	}

	@Override
	protected Importable getCompanionForCompoundDocument(Document resourceDescriptor, Folder folder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean wantsCompanionForCompoundDocument() {
		// TODO Auto-generated method stub
		return false;
	}

}
