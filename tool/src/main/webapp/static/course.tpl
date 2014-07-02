<!--
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
  -->
		<!-- Show details of the course -->
		<div class="courseDetail">
			<div class="messages"></div>
			<div id="summary">
				<h1>${title}</h1>
				{if hide}
					<div class="noAuth">
						If you are a member of the University of Oxford, please
						<a class="login" href="../../pages/login/setup?returnUrl=${returnurl}" target="_top">login to WebLearn</a>
						to see more information.
					</div>
				{else}

				<table width="100%">
					{if presenters.length > 0}
					<tr>
						<th>Lecturer(s)</th>
						<td>
							{for presenter in presenters}
								{if presenter.email}
									<a href="mailto:${presenter.email}">${presenter.name}</a>{if presenter_index != presenters.length-1},{/if}
								{else}
									${presenter.name}{if presenter_index != presenters.length-1},{/if}
								{/if}
							{/for}
						</td>
					</tr>
					{/if}

					{if administrators.length > 0}
					<tr>
						<th>Module Administrator</th>
						<td>
							{for administrator in administrators}
							{if administrator.email}
									<a href="mailto:${administrator.email}">${administrator.name}</a>{if administrator_index != administrators.length-1},{/if}
								{else}
									${administrator.name}{if administrator_index != administrators.length-1},{/if}
								{/if}
							{/for}
						</td>
					</tr>
					{/if}

					<tr>
						<th>Department</th>
						<td>
							{if defined('department')}
								${department}
							{else}
								${departmentCode}
							{/if}
						</td>
					</tr>

					{if regulations}
					<tr>
						<th>Eligibility</th>
						<td>
							${regulations}	
						</td>
					</tr>
					{/if}

					{if source == "Daisy"}
					<tr>
						<th>Signup Available</th>
						<td>
							{if signup}
								{if signup == "full" || waiting}
									Waiting List of ${waiting}
								{else}
									${signup} 
								{/if}
							{else}
								Not bookable
							{/if}		
						</td>
					</tr>
					{/if}

					{if categories_rdf.length > 0}
						<tr>
							<th>Skills Categories</th>
							<td>
								{for category in categories_rdf}
									${category}{if category_index != categories_rdf.length-1},{/if}
								{/for}
							</td>
						</tr>
					{/if}

					{if categories_rm.length > 0}
						<tr>
							<th>Research Methods</th>
							<td>
								{for category in categories_rm}
									${category}{if category_index != categories_rm.length-1},{/if}
								{/for}
							</td>
						</tr>
					{/if}

				</table>
				{/if}
			</div>

			<div id="prerequisite">
				{if prerequisite}
					<h2>Intended Audience</h2>
					${prerequisite}

				{/if}
			</div>

			<div id="description">
				<h2>Description</h2>
				${description}
			</div>

			<div id="parts">
				<h2>Booking Information</h2>
				<span class="error" style="display:none"></span>
				<form id="signup" action="#">
					{var anyOpenParts = false}
					<table width="100%">
						{for part in parts}

							{if !hide}
								<tr>
									<td colspan="3">
										<h3>&nbsp;&nbsp;${part.type.name}</h3>
									</td>
								</tr>
								{var oneOpen = false}
								{for option in part.options}
									<tr>
										<td class="option-details">
											<label for="option-${option.id}">
											{if option.slot}${option.slot} 
												for 
											{else}
												For
											{/if}

											{if option.sessionCount}
												${option.sessionCount} sessions 
											{else}
												1 session
											{/if}

											{if option.when}
												starts in ${option.when}, 
											{/if}

											{if option.presenter}
												{if option.presenter.email}
													<a href="mailto:${option.presenter.email}">
												{/if}
												${option.presenter.name}
												{if option.presenter.email}
													</a>
												{/if}
											{/if}
											</label>
											<br/>

											<span class="location">
											{if option.starts}
												teaching starts on ${option.starts|dateFormat}
											{else}
												{if option.startsText} 
													teaching starts ${option.startsText}
												{/if}
											{/if}

											{if option.ends} 
												and ends on ${option.ends|dateFormat}
											{else}
												{if option.endsText} 
													and ends ${option.endsText}
												{/if}
											{/if}

											{if option.opens || option.closes}
												{if option.starts || option.ends}
													<br/>
												{/if}
											{/if}

											{if option.opens}
												signup opens on ${option.opens|dateFormat}
											{else}
												{if option.opensText} 
													signup opens ${option.opensText}
												{/if}
											{/if}

											{if option.closes} 
												{if option.opens || option.opensText}
													and closes on ${option.closes|dateFormat}
												{else}
													signup closes on ${option.closes|dateFormat}
												{/if}
											{else}
												{if option.closesText} 
													{if option.opens || option.opensText}
														and ends ${option.closesText}
													{else}
														signup closes ${option.closesText}
													{/if}
												{/if}
											{/if}

											{if option.location}
												{if option.opens || option.closes}
													<br/>
												{/if}
												Venue: ${option.location}
											{/if}

											{if option.teachingDetails}
												{if option.opens || option.closes || option.location}
													<br/>
												{/if}
												${option.teachingDetails}
											{/if}

											{if option.sessionData}
												<ul>
												{for session in option.sessionData}
													<li>
													{if session.singleDay }
														${session.start|dateFormat} ${session.start|timeFormat} to
														${session.end|timeFormat} ${session.location}
													{else}
														${session.start|dateFormat} ${session.start|timeFormat} to
														${session.end|dateFormat} ${session.end|timeFormat} ${session.location}
													{/if}
												{/for}
												</ul>
											{/if}

											</span>
										</td>
										<td style="width:6em">
											{if option.bookable}
												{if option.full}
													full
												{else}
													{if waiting}
														Waiting List (${waiting})
													{else}
														{var anyOpenParts = true}
														${option.places} of ${option.size} places remaining 
													{/if}
												{/if}
											{/if}
										</td>
										<td>
											{if option.signup && option.signup.status != "WITHDRAWN"}
												Signup: ${option.signup.status}
											{else}
												{if signup}
													{if option.bookable}
														{if part.options.length == 1}
															<input type="checkbox" 
																name="${part.type.id}" 
																id="option-${option.id}" 
																value="${option.id}" 
																{if !option.open}
																	disabled="true"
																{else}
																	{var oneOpen = true}
																	{if parts.length == 1}
																		checked="yes"
																	{/if}
																{/if}/>
														{else}
															<input type="radio" 
																name="${part.type.id}" 
																id="option-${option.id}" 
																value="${option.id}"
																{if !option.open }
																	disabled="true"
																{else}
																	{var oneOpen = true}
																{/if}/>
														{/if}
													{/if}
												{/if}
											{/if}	
										</td>
									</tr>
								{/for}
								{if parts.length > 1 && part.options.length > 1 && oneOpen}
									<tr>
										<td class="option-details">
											<label for="option-none-${part.type.id}">Nothing for this option</label>
										</td>
										<td>N/A</td>
										<td>
											<input type="radio" name="${part.type.id}" id="option-none-${part.type.id}" value="none"/>
										</td>
									</tr>
								{/if}
							{/for}
						{/if}
					</table>

					{if !hide}
						{if bookable}
							{if signup}
								{if open && !anyOpenParts}
									<input type="submit" value="Join Waiting List" />
								{else}
									<input type="submit" value="Signup" 
									{if !open}
										disabled="true"
									{/if}/>
								{/if}
							{else}
								<input type="submit" value="Not Bookable">
							{/if}
						{else}
							{if defined('memberApplyTo')}
								<p>Apply to:<a href="${memberApplyTo}" target="_blank">${memberApplyTo}</a></p>
							{else}
								{if defined('applyTo')}
									<p>Apply to: <a href="${applyTo}" target="_blank">${applyTo}</a></p>
								{/if}
							{/if}
						{/if}
					{else}
						<h3>University Members</h3>
						<div class="noAuth">
							<p>
							<a class="login" href="../../pages/login/setup?returnUrl=${returnurl}" target="_top">login for booking details</a>
							</p>
						</div>
						<h3>Non-University Members</h3>
						<p>Non Oxford users cannot be given a username.</p>
						{if defined('contactEmail')}
							<p><a href="mailto:${contactEmail}">Make an Enquiry</a></p>
						{/if}
					{/if}
				</form>
			</div>
			{if isAdmin}
				<div id="directLink">
					<h4>Direct Link</h4> 
					${url}
				</div>
			{/if}
		</div>
