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

 <!-- IE8 runnung in IE7 compatability mode breaks this page 
     work around is the line below --> 
 <meta http-equiv="X-UA-Compatible" content="IE=8">

 <title>Researcher Development</title>

 <link href='<c:out value="${skinRepo}" />/tool_base.css' type="text/css" rel="stylesheet" media="all" />
 <link href="<c:out value="${skinRepo}" />/<c:out value="${skinDefault}" />/tool.css" type="text/css" rel="stylesheet" media="all" />
 <link rel="stylesheet" type="text/css" href="lib/tool.css">

 <script type="text/javascript" src="lib/jquery/jquery-1.4.4.min.js"></script>
 <script type="text/javascript" src="lib/jqmodal-r14/jqModal.js"></script>
 <script type="text/javascript" src="lib/jquery-ui-1.8.4.custom/js/jquery-ui-1.8.4.custom.min.js"></script>
 <script type="text/javascript" src="lib/trimpath-template-1.0.38/trimpath-template.js"></script>
 <script type="text/javascript" src="lib/signup.js"></script>
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
<div id="toolbar">
 <ul class="navIntraTool actionToolBar">

  <li><span><a href="home.jsp">Home</a></span></li>
  <li><span><a href="search.jsp">Search Modules</a></span></li>
  <li><span><a href="index.jsp">Browse by Department</a></span></li> 
  <li><span><a href="calendar.jsp">Browse by Calendar</a></span></li>
  <li><span>Researcher Development</span></li>
  <c:if test="${!externalUser}" >
   <li><span><a href="my.jsp">My Modules</a></span></li>
   <c:if test="${isPending}" >
    <li><span><a href="pending.jsp">Pending Acceptances</a></span></li> 
   </c:if>
   <c:if test="${isApprover}" >
    <li><span><a href="approve.jsp">Pending Confirmations</a></span></li>
   </c:if>
   <c:if test="${isAdministrator}" >
    <li><span><a href="admin.jsp">Module Administration</a></span></li>
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
    coords="576,31,572,187,678,201,764,238,841,104,701,44" href="search.jsp?fq=course_subject_vitae_subdomain%3A%22A1%20Knowledge%20base%22" />
  <area shape="poly" alt="Cognitive abilities (A2)" title="Analysing&#10;Synthesising&#10;Critical thinking&#10;Evaluating&#10;Problem solving"
    coords="850,108,772,240,853,306,908,381,1043,303,954,188" href="search.jsp?fq=course_subject_vitae_subdomain%3A%22A2 Cognitive abilities%22" />
  <area shape="poly" alt="Creativity (A3)" title="Inquiring mind&#10;Intellectual insight&#10;Innovation&#10;Argument construction&#10;Intellectual risk"
    coords="1048,314,913,387,950,483,962,576,1113,577,1097,441" href="search.jsp?fq=course_subject_vitae_subdomain%3A%22A3 Creativity%22" />
  <area shape="poly" alt="Personal qualities (B1)" title="Enthusiasm&#10;Perseverance&#10;Integrity&#10;Self-confidence&#10;Self-reflection&#10;Responsibility"
    coords="1114,586,958,583,943,694,907,776,1043,849,1096,710" href="search.jsp?fq=course_subject_vitae_subdomain%3A%22B1 Personal qualities%22" />
  <area shape="poly" alt="Self-management (B2)" title="Preparation and prioritisation&#10;Commitment to research&#10;Time management&#10;Responsiveness to change&#10;Work-life balance"
    coords="1038,857,906,778,843,856,770,913,845,1049,961,957" href="search.jsp?fq=course_subject_vitae_subdomain%3A%22B2 Self-management%22" />
  <area shape="poly" alt="Professional and career development (B3)" title="Career management&#10;Continuing professional development&#10;Responsiveness to opportunities&#10;Networking&#10;Reputation and esteem"
    coords="838,1053,759,917,671,952,576,963,574,1123,715,1104" href="search.jsp?fq=course_subject_vitae_subdomain%3A%22B3 Professional and career development%22" />
  <area shape="poly" alt="Professional conduct (C1)" title="Health and safety&#10;Ethics, principles and sustainability&#10;Legal requirements&#10;IPR and copyright&#10;Respect and confidentiality&#10;Attribution and co-authorship&#10;Appropriate practice"
    coords="101,847,236,768,197,670,186,585,31,585,47,713" href="search.jsp?fq=course_subject_vitae_subdomain%3A%22C1 Professional conduct%22" />
  <area shape="poly" alt="Research management (C2)" title="Research strategy&#10;Project planning and delivery&#10;Risk management"
    coords="296,1046,375,910,288,841,241,778,106,855,180,954" href="search.jsp?fq=course_subject_vitae_subdomain%3A%22C2 Research management%22" />
  <area shape="poly" alt="Finance, funding and resources (C3)" title="Income and funding generation&#10;Financial management&#10;Infrastructure and resources"
    coords="566,1122,566,963,455,947,385,914,306,1051,424,1102" href="search.jsp?fq=course_subject_vitae_subdomain%3A%22C3%20Finance%5C%2C%20funding%20and%20resources%22" />
  <area shape="poly" alt="Working with others (D1)" title="Collegiality&#10;Team working&#10;People management&#10;Supervision&#10;Mentoring&#10;Influence and leadership&#10;Collaboration&#10;Equality and diversity"
     coords="301,104,378,239,470,200,567,185,566,31,414,55" href="search.jsp?fq=course_subject_vitae_subdomain%3A%22D1 Working with others%22" />
  <area shape="poly" alt="Communication and dissemination (D2)" title="Communication methods&#10;Communication media&#10;Publication"
    coords="101,305,236,382,298,301,371,242,294,109,176,203" href="search.jsp?fq=course_subject_vitae_subdomain%3A%22D2 Communication and dissemination%22" />
  <area shape="poly" alt="Engagement and impact (D3)" title="Teaching&#10;Public engagement&#10;Enterprise&#10;Policy&#10;Society and culture&#10;Global citizenship"
    coords="30,575,184,575,199,470,231,389,99,311,48,433" href="search.jsp?fq=course_subject_vitae_subdomain%3A%22D3 Engagement and impact%22" />
  <area shape="poly" alt="Domain A: Knowledge and intellectual abilities" title="This domain contains the knowledge and intellectual abilities needed to be able to carry out excellent research."
    coords="575,577,576,196,675,209,763,246,839,306,903,386,944,488,953,574" href="search.jsp?fq=course_subject_vitae_domain%3A%22A Knowledge and intellectual abilities%22" />
  <area shape="poly" alt="Domain B: Personal effectiveness" title="This domain contains the personal qualities, career and self-management skills required to take ownership for and control of professional development."
    coords="576,585,952,585,934,695,896,773,839,846,760,907,667,942,576,954" href="search.jsp?fq=course_subject_vitae_domain%3A%22B Personal effectiveness%22" />
  <area shape="poly" alt="Domain C: Research governance and organisation" title="This domain contains the knowledge of the standards, requirements and professional conduct that are needed for the effective management of research."
    coords="567,585,566,953,464,939,386,905,300,837,246,769,204,668,192,585" href="search.jsp?fq=course_subject_vitae_domain%3A%22C Research governance and organisation%22" />
  <area shape="poly" alt="Domain D: Engagement, influence and impact" title="This domain contains the knowledge, understanding and skills needed to engage with%5C influence and impact on the academic, social, cultural and economic context."
    coords="566,577,194,576,208,472,241,390,304,308,382,246,475,209,567,194" href="search.jsp?fq=course_subject_vitae_domain%3A%22D Engagement%5C%2C influence and impact%22" />

  <area shape="poly" alt="RDF Conditions of use" title="RDF Conditions of use" coords="802,1122,875,1095,944,1050,1006,992,1054,930,1100,864,1122,815,1091,803,1059,862,1018,921,964,978,890,1042,826,1080,794,1098" href="http://www.vitae.ac.uk/RDFconditionsofuse" target="_blank" />
 </map>

 <img style="border: none; display: block; margin-left: auto; margin-right: auto" src="images/vitae-wheel.jpg" usemap="#vitae-wheel" width="1143px" height="1201px"/>

</div>
 
<br clear="all" />
</body></html>
