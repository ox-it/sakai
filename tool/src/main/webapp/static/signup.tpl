<h2>Signup to: ${course}</h2>
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
        {for componentId in componentIds}<input type="hidden" name="components" value="${componentId}"/>{/for}<input type="hidden" name="courseId" value="${courseId}"/>
        <table>
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
            <tr>
                <th>
                    <label for="supervisor-note">
                        Message to supervisor
                    </label>
                </th>
                <td>
<${textarea} name="message" id="supervisor-note" cols="40" rows="8">Reason for requesting to attend this module:
 
Other comments: </${textarea}>
                </td>
            </tr>
        </table>
        <input type="submit" value="Confirm Signup"/><input type="submit" class="cancel" value="Cancel"/>
    </form>
</div>