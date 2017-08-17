package org.sakaiproject.site.tool.helper.participantlist.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.wicket.AttributeModifier;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;

import org.sakaiproject.authz.api.Role;
import org.sakaiproject.site.tool.helper.participantlist.providers.ParticipantsProvider;
import org.sakaiproject.site.tool.helper.participantlist.model.Participant;
import org.sakaiproject.site.tool.helper.participantlist.pages.BasePage;
import org.sakaiproject.site.tool.helper.participantlist.service.ParticipantService;

/**
 *
 * @author mweston4, bjones86
 */
public class ParticipantListPanel extends Panel
{
    private Set<Participant> selectedStatuses = new HashSet<>();
    private Set<Participant> selectedRoles = new HashSet<>();
    private ParticipantService participantService;

    public ParticipantListPanel(String id, int rowsPerPage, final String filterType, final String filterID)
    {
        super(id);

        final Form manageParticipantForm = new Form("manageParticipantForm");
        final CheckGroup<Participant> checkBoxGroup = new CheckGroup<>("checkboxGroup", new ArrayList<>());

        this.participantService = new ParticipantService();

        // Participant List Table
        List<IColumn<Participant, String>> columns = new ArrayList<>();
        columns.add(new PropertyColumn<>(Model.of("Name"), "name", "name"));
        columns.add(new PropertyColumn<>(Model.of("Id"), "id", "id"));

        if (participantService.hasProviderSet())
        {
            columns.add(new PropertyColumn<Participant, String>(Model.of("Enrolled In"), "courseSite", "courseSite")
            {
                @Override
                public void populateItem(Item item, String componentId, IModel model)
                {
                    item.add(new Label(componentId, new PropertyModel(model, "courseSite")).setEscapeModelStrings(false));
                }
            });
        }

        if (participantService.isCourseSite())
        {
            columns.add(new PropertyColumn<Participant, String>(Model.of("Credits"), "credits", "credits")
            {
                @Override
                public void populateItem(Item item, String componentId, IModel model)
                {
                    item.add(new Label(componentId, new PropertyModel(model, "credits")).setEscapeModelStrings(false));
                }
            });
        }

        columns.add(new DropDownChoiceRoleColumn(Model.of("Role"), "role")
        {
            @Override
            protected IModel<String> newDropDownChoiceModel(final IModel<Participant> rowModel)
            {
                Participant p = rowModel.getObject();
                selectedRoles.add(p);

                return new IModel<String>()
                {
                    @Override
                    public final void setObject(String object)
                    {
                        //update the object
                        if (selectedRoles.contains(rowModel.getObject()))
                        {
                            selectedRoles.remove(rowModel.getObject());
                        }

                        Participant p = rowModel.getObject();
                        p.setRole(object);
                        selectedRoles.add(p);
                    }

                    @Override
                    public final String getObject()
                    {
                        String role = "";
                        List<Role> allowedRoles = participantService.getAllowedRoles();
                        if (selectedRoles.contains(rowModel.getObject()))
                        {
                            for (Role r : allowedRoles)
                            {
                                if (r.getId().equals(rowModel.getObject().getRole()))
                                {
                                    role = r.getId();
                                }
                            }
                        }

                        return role;
                    }

                    @Override
                    public void detach()
                    {
                        rowModel.detach();
                    }
                };
            }
        });

        if (participantService.allowUpdateSiteMembership())
        {
            if (participantService.isActiveInactiveUser() && !participantService.isMyWorkspace())
            {
                columns.add(new DropDownChoiceStatusColumn(Model.of("Status"), "status")
                {
                    @Override
                    protected IModel<String> newDropDownChoiceModel(final IModel<Participant> rowModel)
                    {
                        Participant p = rowModel.getObject();
                        selectedStatuses.add(p);

                        return new IModel<String>()
                        {
                            @Override
                            public final void setObject(String object)
                            {
                                //update the object
                                if (selectedStatuses.contains(rowModel.getObject()))
                                {
                                    selectedStatuses.remove(rowModel.getObject());
                                }

                                Participant p = rowModel.getObject();
                                if (object.equals(new ResourceModel("participantlist.status.active").getObject()))
                                {
                                    p.setStatus("true");
                                }
                                else
                                {
                                    p.setStatus("false");
                                }

                                selectedStatuses.add(p);
                            }

                            @Override
                            public final String getObject()
                            {
                                String status = "";
                                if (selectedStatuses.contains(rowModel.getObject()))
                                {
                                    if ("true".equals(rowModel.getObject().getStatus()))
                                    {
                                        status = new ResourceModel("participantlist.status.active").getObject();
                                    }
                                    else
                                    {
                                        status = new ResourceModel("participantlist.status.inactive").getObject();
                                    }
                                }

                                return status;
                            }

                            @Override
                            public void detach()
                            {
                                rowModel.detach();
                            }
                        };
                    }
                });
            }

            columns.add(new CheckBoxColumn(Model.of("Remove")));
        }

        final WebMarkupContainer wmc = new WebMarkupContainer("tableContainer");
        wmc.setOutputMarkupId(true);

        final SakaiDataTable participantTable = new SakaiDataTable("siteMembers", columns, new ParticipantsProvider( filterType, filterID ), rowsPerPage);
        participantTable.add(new AttributeAppender("class", new Model<>("participantTable"), " "));

        checkBoxGroup.add(participantTable);
        wmc.add(checkBoxGroup);
        manageParticipantForm.add(wmc);

        // Add update buttons to form
        SakaiAjaxButton btnUpdate1 = new SakaiAjaxButton("btnUpdateParticipants1")
        {
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form)
            {
                updateParticipants(checkBoxGroup.getModelObject(), filterType, filterID);
                target.add(wmc);
                BasePage page = (BasePage) getPage();
                if (!page.hasFeedbackMessage())
                {
                    page.clearFeedback(page.feedbackPanel);
                }
                target.add(page.feedbackPanel);
            }
        };

        btnUpdate1.add(AttributeModifier.replace("value", new ResourceModel("updateParticipants").getObject()));
        btnUpdate1.add(new AttributeAppender("class", new Model<>("udpateButton"), " "));
        manageParticipantForm.add(btnUpdate1);

        final SakaiAjaxButton btnCancel1 = new SakaiAjaxButton("btnCancel1")
        {
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form)
            {
                returnToSiteInfo();
            }
        };

        btnCancel1.setDefaultFormProcessing(false);
        manageParticipantForm.add(btnCancel1);

        final SakaiAjaxButton btnUpdate2 = new SakaiAjaxButton("btnUpdateParticipants2")
        {
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form)
            {
                updateParticipants(checkBoxGroup.getModelObject(), filterType, filterID);
                target.add(wmc);
                BasePage page = (BasePage) getPage();
                if (!page.hasFeedbackMessage())
                {
                    page.clearFeedback(page.feedbackPanel);
                }
                target.add(page.feedbackPanel);
            }
        };

        btnUpdate2.add(AttributeModifier.replace("value", new ResourceModel("updateParticipants").getObject()));
        btnUpdate2.add(new AttributeAppender("class", new Model<>("udpateButton"), " "));
        manageParticipantForm.add(btnUpdate2);

        final SakaiAjaxButton btnCancel2 = new SakaiAjaxButton("btnCancel2")
        {
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form)
            {
                returnToSiteInfo();
            }
        };

        btnCancel2.setDefaultFormProcessing(false);
        manageParticipantForm.add(btnCancel2);

        // Last Updated D/T
        IModel dtModel = new LoadableDetachableModel()
        {
            @Override
            protected Object load()
            {
                return new ResourceModel("lastUpdatedLbl").getObject() + ": " + participantService.getSiteLastModifiedDT();
            }

        };

        wmc.add(new Label("lastUpdateDateTop", dtModel));
        wmc.add(new Label("lastUpdateDateBottom", dtModel));
        add(manageParticipantForm);
    }

    /*
     * Cancel button functionality
     */
    private void returnToSiteInfo()
    {
        String redirectUrl = participantService.getResetToolUrl();
        if (!redirectUrl.isEmpty())
        {
            RedirectRequestHandler rrt = new RedirectRequestHandler(redirectUrl);
            getRequestCycle().scheduleRequestHandlerAfterCurrent(rrt);
        }
    }

    private void updateParticipants(Collection<Participant> participants, String filterType, String filterID)
    {
        Map<String, Participant> participantStatus = new HashMap<>();
        for (Participant p : selectedStatuses)
        {
            participantStatus.put(p.getUniqName(),p);
        }

        Map<String, Participant> participantRoles = new HashMap<>();
        for (Participant p : selectedRoles)
        {
            participantRoles.put(p.getUniqName(), p);
        }

        //if an error is thrown, display to the screen
        String alertMsg = participantService.updateParticipants(participantStatus, participantRoles, participants, filterType, filterID);
        if (alertMsg != null)
        {
            error(alertMsg);
        }
    }
}
