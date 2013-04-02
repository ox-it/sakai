package uk.ac.ox.oucs.vle;

/*
 * #%L
 * Course Signup Implementation
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
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
 * #L%
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.sakaiproject.component.api.ServerConfigurationService;

public class OucsDeptImpl implements Module {

	/**
	 * The DAO to update our entries through.
	 */
	private CourseDAO dao;
	
	/**
	 * The proxy for getting users.
	 */
	private SakaiProxy proxy;
	public void setProxy(SakaiProxy proxy) {
		this.proxy = proxy;
	}
	
	public void setCourseDao(CourseDAO dao) {
		this.dao = dao;
	}
	
	protected String getDepartentResource() {
		return proxy.getConfigParam("oucs-department-resource", 
				"https://register.oucs.ox.ac.uk/upload/card/publish/dept_codes.txt");
	}
	
	public void update() {
		
		try {
		    // Create a URL for the desired page
		    URL url = new URL(getDepartentResource());
		    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		    String str;
		    while ((str = in.readLine()) != null) {
		    	process(str);
		    }
		    in.close();
		    
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
	}
	
	private void process(String str) {
		
		String nickname = null;
		String fullname = null;
		String oucs_code = null;
		String f4_char = null;
		String mailer = null;
		String t2_char = null;
		String card_code = null;

		String tokens[] = str.split(":");
		int numTokens = tokens.length;

		for (int i = 0; i < numTokens; i++) {
			
			switch(i) {
			case 0:
				nickname=tokens[i];
				break;
			case 1:
				fullname=tokens[i];
				break;
			case 2:
				oucs_code=tokens[i];
				break;
			case 3:
				f4_char=tokens[i];
				break;
			case 4:
				mailer=tokens[i];
				break;
			case 5:
				t2_char=tokens[i];
				break;
			case 6:
				card_code=tokens[i];
				break;
			default:
				break;
			}
		}
		
		CourseOucsDepartmentDAO oucsDao = dao.findOucsDeptByCode(card_code);
		if (null == oucsDao) {
			oucsDao = new CourseOucsDepartmentDAO(card_code);
		}
		oucsDao.setOucsCode(oucs_code);
		oucsDao.setNickName(nickname);
		oucsDao.setFullName(fullname);
		oucsDao.setF4Char(f4_char);
		oucsDao.setT2Char(t2_char);
		oucsDao.setMailer(mailer);
		
		dao.save(oucsDao);
	}
}
