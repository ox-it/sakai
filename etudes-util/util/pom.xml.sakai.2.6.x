<?xml version="1.0"?>

<!--
/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2009 Etudes, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
-->

<project xmlns="http://maven.apache.org/POM/4.0.0">
	<modelVersion>4.0.0</modelVersion>

	<parent>
		<artifactId>util</artifactId>
		<groupId>org.etudes.util</groupId>
		<version>1.0</version>
		<relativePath>../pom.xml</relativePath>
	</parent>

	<name>etudes-util</name>
	<groupId>org.etudes.util</groupId>
	<artifactId>etudes-util</artifactId>
	<packaging>jar</packaging>

	<dependencies>
		<dependency>
			<groupId>commons-logging</groupId>
			<artifactId>commons-logging</artifactId>
			<version>1.0.4</version>
		</dependency>
		
		
<!-- 
		<dependency>
			<groupId>org.sakaiproject</groupId>
			<artifactId>sakai-authz-api</artifactId>
			<version>${sakai.version}</version>
		</dependency>

		<dependency>
			<groupId>org.sakaiproject</groupId>
			<artifactId>sakai-component-api</artifactId>
			<version>${sakai.version}</version>
		</dependency>

		<dependency>
			<groupId>org.sakaiproject</groupId>
			<artifactId>sakai-entity-api</artifactId>
			<version>${sakai.version}</version>
		</dependency>

		<dependency>
			<groupId>org.sakaiproject</groupId>
			<artifactId>sakai-content-api</artifactId>
			<version>${sakai.version}</version>
		</dependency>

		<dependency>
			<groupId>org.sakaiproject</groupId>
			<artifactId>sakai-util-api</artifactId>
			<version>${sakai.version}</version>
		</dependency>
		<dependency>
			<groupId>org.sakaiproject</groupId>
			<artifactId>sakai-util</artifactId>
			<version>${sakai.version}</version>
		</dependency>
		
		-->
		
		<dependency>
			<groupId>org.sakaiproject.kernel</groupId>
			<artifactId>sakai-kernel-util</artifactId>
		</dependency>

		<dependency>
			<groupId>org.sakaiproject.kernel</groupId>
			<artifactId>sakai-kernel-api</artifactId>
		</dependency>

		<dependency>
			<groupId>org.sakaiproject.kernel</groupId>
			<artifactId>sakai-component-manager</artifactId>
		</dependency>

		<dependency>
			<groupId>org.etudes.util</groupId>
			<artifactId>etudes-util-api</artifactId>
			<version>1.0</version>
			<scope>provided</scope>
		</dependency>
	</dependencies>
	<build>
	
		<sourceDirectory>src/java</sourceDirectory>
	
	</build>

</project>
