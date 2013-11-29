<div>
    <h2>Signup made by: ${user.name} and is currently ${status}</h2>
    <form action="/course-signup/rest/signup/${id}/split" method="POST">
    <div>Select components to split: <span class="errors"></span></div>
    <ul>
        {for component in components}
        <li>
            <label>
            <input type="checkbox" name="componentPresentationId" value="${component.presentationId}" />
            <div class="course-component" style="display: inline-block">
                ${component.title} ${component.slot} in ${component.when} ${component.places|placesFormat} <br/>
                Teaching starts ${component.starts|dateFormat}
            </div>
            </label>
        </li>
        {/for}
    </ul>
    <input type="submit" value="Split"/>
    <input type="button" name="cancel" value="Cancel"/>
    </form>
</div>
