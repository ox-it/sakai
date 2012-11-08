           <!-- Show details of the course -->
         <div class="courseDetail">
		    <div class="messages"></div>
            <div id="summary">
                <h1>${title}</h1>
                {if hide}
                <div class="noAuth">
					If you are a member of the University of Oxford, please
					<a class="login" href="dologin.jsp" target="_top">login to WebLearn</a>
					to see more information.
					</div>
                {else}
                <table width="100%">
                	<tr>
                        <th>
                            Lecturer(s)
                        </th>
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
                    <tr>
                        <th>
                            Module Administrator
                        </th>
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
                    <tr>
                        <th>
                            Department
                        </th>
                        <td>
                        	{if defined('department')}
                            	${department}
							{else}
								${departmentCode}
							{/if}
                        </td>
                    </tr>
                    <tr>
                        <th>
                            Signup Available
                        </th>
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
                    
                    {if categories_rdf.length > 0}
                    	<tr>
                        	<th>
                            	Skills Categories
                        	</th>
                        	<td>
								{for category in categories_rdf}
									${category}{if category_index != categories_rdf.length-1},{/if}
								{/for}
                        	</td>
                    	</tr>
                    {/if}
                    
                    {if categories_jacs.length > 0}
                    	<tr>
                        	<th>
                            	Subject categories
                        	</th>
                        	<td>
								{for category in categories_jacs}
									${category}{if category_index != categories_jacs.length-1},{/if}
								{/for}
                        	</td>
                    	</tr>
                    {/if}
                    
                    {if categories_rm.length > 0}
                    	<tr>
                        	<th>
                            	Research Methods
                        	</th>
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
                    		{if part.subject}
								<tr>
                     	       		<th colspan="3">
                     	       			${part.subject}
                            		</th>
                        		</tr>
                       		{/if}
                        	{if !hide}
                        		<tr>
                            		<td colspan="3">
                                		&nbsp;&nbsp;${part.type.name}
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
                                			${option.sessions} sessions starts in ${option.when}, 
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
                                				teaching starts on ${new Date(option.starts).toDateString()}
                                			{/if}
                                			{if option.ends} 
                                				and ends on ${new Date(option.ends).toDateString()}
                                			{/if}
                                			{if option.location}
                                				{if option.starts || option.ends}
                                					<br/>
                                				{/if}
                                				${option.location}
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
						{if signup}
                    		{if !anyOpenParts}
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
					
						<h3>University Members</h3>
						<div class="noAuth">
							<p><a class="login" href="dologin.jsp" target="_top">Login for booking details</a></p>
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
    