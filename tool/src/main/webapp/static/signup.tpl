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
        <table>
        	{if courseApproval}
            <tr>
                <th>
                    <label for="supervisor-email">
                        Your Supervisor's Email
                    </label>
                </th>
                <td>
                    <input type="text" class="valid-email" name="email" id="supervisor-email" size="40"/>
                </td>
            </tr>
            {/if}
            <tr>
                <th>
                    <label for="supervisor-note">
                        Please enter your reason/s for enrolling for module
                    </label>
                </th>
                <td>
<${textarea} name="message" id="supervisor-note" cols="40" rows="8">Reason for requesting to attend this module:
 
Other comments: </${textarea}>
                </td>
            </tr>
        </table>
        <p>Note that your place is not guaranteed until you receive a confirmation email</p>
        <input type="submit" value="Confirm Signup"/>
        <input type="submit" class="cancel" value="Cancel"/>
    </form>
</div>