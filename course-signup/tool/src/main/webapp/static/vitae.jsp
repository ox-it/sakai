 <%--
  #%L
  Course Signup Webapp
  %%
  Copyright (C) 2010 - 2013 University of Oxford
  %%
  Licensed under the Educational Community License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
              http://opensource.org/licenses/ecl2
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  #L%
  --%>
<%@ page import="org.sakaiproject.tool.cover.ToolManager"%>
<%@ page import="org.sakaiproject.component.cover.ServerConfigurationService" %>
<%@ page import="org.sakaiproject.user.cover.UserDirectoryService" %>
<%@ page session="false" %> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
 <meta http-equiv="Content-Type" content="text/html; charset=utf-8">

 <title>Researcher Development</title>

 <link href='<c:out value="${skinRepo}" />/tool_base.css' type="text/css" rel="stylesheet" media="all" />
 <link href="<c:out value="${skinRepo}" />/<c:out value="${skinPrefix}" /><c:out value="${skinDefault}" />/tool.css" type="text/css" rel="stylesheet" media="all" />
 <link rel="stylesheet" type="text/css" href="lib/tool.css">

 <script type="text/javascript" src="lib/jquery/jquery-1.4.4.min.js"></script>
 <script type="text/javascript" src="lib/jqmodal-r14/jqModal.js"></script>
 <script type="text/javascript" src="lib/jquery-ui-1.8.4.custom/js/jquery-ui-1.8.4.custom.min.js"></script>
 <script type="text/javascript" src="lib/trimpath-template-1.0.38/trimpath-template.js"></script>
 <script type="text/javascript" src="lib/signup.min.js"></script>
 <script type="text/javascript" src="lib/Text.js"></script>
 <script type="text/javascript" src="lib/serverDate.js"></script>
 <script type="text/javascript" src="lib/datejs/date-en-GB.js"></script>

 <script type="text/javascript">

  /* Adjust with the content. */
  $(function(){
   Signup.util.resize(window.name);
  });

 </script>
</head> 
<body>
 <div class="portletBody container-fluid">
   <div id="toolbar">
   <ul class="navIntraTool actionToolBar">
  
    <li><span><a href="home.jsp">Home</a></span></li>
    <li><span><a href="search.jsp">Search Courses</a></span></li>
    <li><span><a href="index.jsp">Browse by Department</a></span></li> 
    <li><span><a href="calendar.jsp">Browse by Calendar</a></span></li>
    <li><span class="current">Researcher Development</span></li>
    <c:if test="${!externalUser}" >
     <li><span><a href="my.jsp">My Courses</a></span></li>
     <c:if test="${isPending}" >
      <li><span><a href="pending.jsp">Pending Acceptances</a></span></li> 
     </c:if>
     <c:if test="${isApprover}" >
      <li><span><a href="approve.jsp">Pending Confirmations</a></span></li>
     </c:if>
     <c:if test="${isAdministrator}" >
      <li><span><a href="admin.jsp">Course Administration</a></span></li>
     </c:if>
     <c:if test="${isLecturer}">
      <li><span><a href="lecturer.jsp">Lecturer View</a></span></li>
     </c:if>
    </c:if>
   </ul>
  </div>
   
  <div class="wrapper" >
  
  <h2>Introduction</h2>
  <p>
  You can use the <a href="http://www.vitae.ac.uk/CMS/files/upload/Vitae-Researcher-Development-Framework.pdf" target="_blank">Researcher Development Framework</a> (RDF) (see the interactive wheel diagram below) to reflect on the multifaceted nature of research and to help you think strategically about your developmental aims, needs and aspirations as a researcher and plan your steps towards them by assessing and developing the skills and attributes you will need. The RDF brings together key skills, knowledge, behaviours and attitudes relevant to researchers. The RDF, created by <a href="http://www.vitae.ac.uk/" target="_blank">Vitae</a> from interviews with researchers, is used by universities across the UK to guide researcher learning and development.
  </p>
  <h2>The Framework</h2>
  <p>
  The RDF has four Domains, each of which incorporates three Subdomains. Each Subdomain is broken down further into Descriptors. Hover over a Subdomain on the wheel to view its associated Descriptors. These are the key skills, knowledge, behaviours and attitudes that researchers learn, develop or improve as they progress in their work.
  </p>
  <h2>How to use the RDF</h2>
  <p>
  As a researcher, you can use the RDF Domains, Subdomains and Descriptors strategically to help you plan your learning and development, according to your career stage, plans and aspirations. For example, if you would like to improve your knowledge of research governance and organisation (Domain C) by learning more about research management (Subdomain C2), then you might ask yourself, `At this stage of my research career, is my biggest priority strategy, planning projects or managing risk (all Descriptors tied to Subdomain C2), and how can I develop the knowledge and skills I need to meet this priority I've identified?`
  </p>
  <p>
  At Oxford, our researcher development training is tagged with RDF Domains and Subdomains to make it clear how the training relates to specific parts of the RDF. It is important to recognise that the RDF is holistic. Over time, you should aim to develop across all four Domains and to integrate your experience, development and learning. As well as attending courses, consider how you might learn in other ways - for example reflecting on what you might learn from observing and discussing with others; from your reading; or through experience. You may find <a href="http://www.vitae.ac.uk/CMS/files/upload/Vitae-Excel-RDF-Planner-Prototype-2010.xls" target="_blank">Vitae's interactive downloadable RDF Planner</a> a useful tool for assisting this process.
  </p>
  
   <map name="vitae-wheel">
    <area shape="poly" alt="Knowledge base (A1)" title="Subject knowledge&#10;Research methods: theoretical knowledge&#10;Research methods: practical application&#10;Information seeking&#10;Information literacy and management&#10;Languages&#10;Academic literacy and numeracy"
     coords="288,15,286,93,339,100,382,119,420,52,350,22" href="search.jsp?fq=course_subject_vitae_subdomain%3A%22A1%20%2D%20Knowledge%20base%22" />
    <area shape="poly" alt="Cognitive abilities (A2)" title="Analysing&#10;Synthesising&#10;Critical thinking&#10;Evaluating&#10;Problem solving"
     coords="425,54,386,120,426,153,454,190,521,151,477,94" href="search.jsp?fq=course_subject_vitae_subdomain%3A%22A2%20%2D Cognitive abilities%22" />
    <area shape="poly" alt="Creativity (A3)" title="Inquiring mind&#10;Intellectual insight&#10;Innovation&#10;Argument construction&#10;Intellectual risk"
     coords="524,157,456,193,475,241,481,288,556,288,548,220" href="search.jsp?fq=course_subject_vitae_subdomain%3A%22A3%20%2D Creativity%22" />
    <area shape="poly" alt="Personal qualities (B1)" title="Enthusiasm&#10;Perseverance&#10;Integrity&#10;Self-confidence&#10;Self-reflection&#10;Responsibility"
     coords="557,293,479,291,471,347,453,388,521,424,548,355" href="search.jsp?fq=course_subject_vitae_subdomain%3A%22B1%20%2D Personal qualities%22" />
    <area shape="poly" alt="Self-management (B2)" title="Preparation and prioritisation&#10;Commitment to research&#10;Time management&#10;Responsiveness to change&#10;Work-life balance"
     coords="519,428,453,389,421,428,385,456,422,524,480,478" href="search.jsp?fq=course_subject_vitae_subdomain%3A%22B2%20%2D Self-management%22" />
    <area shape="poly" alt="Professional and career development (B3)" title="Career management&#10;Continuing professional development&#10;Responsiveness to opportunities&#10;Networking&#10;Reputation and esteem"
     coords="419,526,379,458,335,476,288,481,287,561,357,552" href="search.jsp?fq=course_subject_vitae_subdomain%3A%22B3%20%2D Professional and career development%22" />
    <area shape="poly" alt="Professional conduct (C1)" title="Health and safety&#10;Ethics, principles and sustainability&#10;Legal requirements&#10;IPR and copyright&#10;Respect and confidentiality&#10;Attribution and co-authorship&#10;Appropriate practice"
     coords="50.5,423,118,384,98,335,93,292,15,292,23,356" href="search.jsp?fq=course_subject_vitae_subdomain%3A%22C1%20%2D Professional conduct%22" />
    <area shape="poly" alt="Research management (C2)" title="Research strategy&#10;Project planning and delivery&#10;Risk management"
     coords="148,523,187,455,144,420,120,389,53,427,90,477" href="search.jsp?fq=course_subject_vitae_subdomain%3A%22C2%20%2D Research management%22" />
    <area shape="poly" alt="Finance, funding and resources (C3)" title="Income and funding generation&#10;Financial management&#10;Infrastructure and resources"
     coords="283,561,283,481,227,473,192,457,153,525,212,551" href="search.jsp?fq=course_subject_vitae_subdomain%3A%22C3%20%2D%20Finance%5C%2C%20funding%20and%20resources%22" />
    <area shape="poly" alt="Working with others (D1)" title="Collegiality&#10;Team working&#10;People management&#10;Supervision&#10;Mentoring&#10;Influence and leadership&#10;Collaboration&#10;Equality and diversity"
      coords="150.5,52,189,119,235,100,283,92,283,15,207,27" href="search.jsp?fq=course_subject_vitae_subdomain%3A%22D1%20%2D Working with others%22" />
    <area shape="poly" alt="Communication and dissemination (D2)" title="Communication methods&#10;Communication media&#10;Publication"
     coords="50.5,152,118,191,149,150,185,121,147,54,88,101" href="search.jsp?fq=course_subject_vitae_subdomain%3A%22D2%20%2D Communication and dissemination%22" />
    <area shape="poly" alt="Engagement and impact (D3)" title="Teaching&#10;Public engagement&#10;Enterprise&#10;Policy&#10;Society and culture&#10;Global citizenship"
     coords="15,287,92,287,99,235,115,194,49,155,24,216" href="search.jsp?fq=course_subject_vitae_subdomain%3A%22D3%20%2D Engagement and impact%22" />
    <area shape="poly" alt="Domain A: Knowledge and intellectual abilities" title="This domain contains the knowledge and intellectual abilities needed to be able to carry out excellent research."
     coords="287.5,288,288,98,337,104,381,123,419,153,451,193,472,244,476,287" href="search.jsp?fq=course_subject_vitae_domain%3A%22A%20%2D%20Knowledge and intellectual abilities%22" />
    <area shape="poly" alt="Domain B: Personal effectiveness" title="This domain contains the personal qualities, career and self-management skills required to take ownership for and control of professional development."
     coords="288,292,476,292,467,347,448,386,419,423,380,453,333,471,288,477" href="search.jsp?fq=course_subject_vitae_domain%3A%22B%20%2D%20Personal effectiveness%22" />
    <area shape="poly" alt="Domain C: Research governance and organisation" title="This domain contains the knowledge of the standards, requirements and professional conduct that are needed for the effective management of research."
     coords="283.5,292,283,476,232,469,193,452,150,418,123,384,102,334,96,292" href="search.jsp?fq=course_subject_vitae_domain%3A%22C%20%2D%20Research governance and organisation%22" />
    <area shape="poly" alt="Domain D: Engagement, influence and impact" title="This domain contains the knowledge, understanding and skills needed to engage with, influence and impact on the academic, social, cultural and economic context."
     coords="283,288,97,288,104,236,120,195,152,154,191,123,237,104,283,97" href="search.jsp?fq=course_subject_vitae_domain%3A%22D%20%2D%20Engagement%5C%2C influence and impact%22" />
  
    <area shape="poly" alt="RDF Conditions of use" title="RDF Conditions of use"
     coords="401,561,437,547,472,525,503,496,527,465,550,432,561,407,545,401,529,431,509,460,482,489,445,521,413,540,397,549" href="http://www.vitae.ac.uk/RDFconditionsofuse" target="_blank" />
   </map>
  
   <img style="border: none; display: block; margin-left: auto; margin-right: auto" src="images/vitae-wheel.jpg" usemap="#vitae-wheel" width="571px" height="600px"/>
  
  </div>
   
  <br clear="all" />
 </div>
</body></html>
