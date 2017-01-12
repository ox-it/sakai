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
<h2>Signup to: ${course} </h2>
{var textarea = "textarea"}
<div>
    <ul>
        {for component in components}
        <li>
            ${component}
        </li>
        {/for}
    </ul>
</div>
<div>
    <form id="signup-confirm" action="#">
        {for componentId in componentIds}
        	<input type="hidden" name="components" value="${componentId}"/>
        {/for}
        <input type="hidden" name="courseId" value="${courseId}"/>
        <table style="width:100%">
        	{if courseApproval}
                <tr>
                    <th>
                        <label for="supervisor-email">
                            Your Supervisor's Email
                        </label>
                    </th>
                </tr>
                <tr>
                    <td>
                        <input type="text" class="valid-email" name="email" id="supervisor-email" size="40"/>
                    </td>
                </tr>
            {/if}
            <fieldset>
                <tr>
                    <th>
                        <label for="supervisor-note" class="signup-reason">
                            Please enter your reason/s for enrolling for module
                        </label>
                    </th>
                </tr>
                <tr>
                    <td>
<${textarea} name="message" id="supervisor-note" cols="60" rows="8">Reason for requesting to attend this module:
 
Other comments: </${textarea}>
                    </td>
                </tr>
            </fieldset>
            <tr>
            </tr>
            <fieldset>
                <tr>
                    <th>
                        <label for="special-requirements"  class="signup-reason">
                            Please tell us any information we need to know in order to ensure that you are able to attend and participate fully in the module
                        </label>
                    </th>
                </tr>
                <tr>
                    <td>
<${textarea} name="specialReq" id="special-requirements" cols="60" rows="8"></${textarea}>
                    </td>
                 </tr>
            </fieldset>
        </table>
        <br>
        <p>Note that your place is not guaranteed until you receive a confirmation email</p>
        <br>
        <input type="submit" value="Confirm Signup"/>
        <input type="submit" class="cancel" value="Cancel"/>
    </form>
</div>