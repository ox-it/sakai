           <!-- Show details of the course -->
		   <div class="messages"></div>
            <div id="summary" class="">
                <h3>${title}</h3>
                <table width="100%">
                	<tr>
                        <th>
                            Lecturer(s)
                        </th>
                        <td>
                            {for presenter in presenters}
								{if presenter.email}
									<a href="mailto:${presenter.email}">${presenter.name}</a>
								{else}
									${presenter.name}
								{/if}
							{/for}
                        </td>
                    </tr>
                    <tr>
                        <th>
                            Module Administrator
                        </th>
                        <td>
                        	{if administrator.email}
								<a href="mailto:${administrator.email}">${administrator.name}</a>
							{else}
								${administrator.name}
							{/if}
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
                            	${signup}
							{else}
								Not bookable
							{/if}		
                        </td>
                    </tr>
                </table>
            </div>
            <div id="description">
            	<h4>Description</h4>
				${description}
            </div>
			<div id="parts">
                <h4>Module Parts</h4>
				<span class="error" style="display:none"></span>
                <form id="signup" action="#">
                    <table width="100%">
                    	{for part in parts}
						<tr>
                            <th colspan="3">
                                ${part.type.name}
                            </th>
                        </tr>
						{var oneOpen = false}
						{for option in part.options}
                        <tr>
                            <td class="option-details">
                                <label for="option-${option.id}">${option.slot} for ${option.sessions} sessions starts in ${option.when}, 
								{if option.presenter}{if option.presenter.email}<a href="mailto:${option.presenter.email}">{/if}${option.presenter.name}{if option.presenter.email}</a>{/if}{/if}
								</label>
                                <br/>
                                <span class="location">
                                	{if option.starts}teaching starts on ${new Date(option.starts).toDateString()}{/if}
                                	{if option.ends} and ends on ${new Date(option.ends).toDateString()}{/if}
                                	{if option.location}
                                		{if option.starts || option.ends}<br/>{/if}
                                		${option.location}
                                	{/if}
                                </span>
                            </td>
                            <td>
                            	{if option.bookable}
                            		{if option.full}
										full
									{else}
										${option.places} places
									{/if}
								{/if}
                            </td>
                            <td>
                            	
								{if option.signup && option.signup.status != "WITHDRAWN"}
									Signup: ${option.signup.status}
								{else}
								{if signup}
                                <input type="radio" name="${part.type.id}" id="option-${option.id}" value="${option.id}"
								 {if option.full || !option.open }disabled="true"{else}{var oneOpen = true}{/if}/>
								 {/if}
								{/if}
                            </td>
                        </tr>
						{/for}
						{if parts.length > 1 && oneOpen}
						<tr>
							<td class="option-details">
								<label for="option-none-${part.type.id}">Nothing for this option</label>
							</td>
							<td>N/A</td>
							<td><input type="radio" name="${part.type.id}" id="option-none-${part.type.id}" value="none"/></td>
						</tr>
						{/if}
						{/for}
                    </table>
					{if signup}
						<input type="submit" value="Signup" {if full || !open}disabled="true"{/if}/>
					{else}
						<input type="submit" value="Not Bookable">
					{/if}
                </form>
            </div>