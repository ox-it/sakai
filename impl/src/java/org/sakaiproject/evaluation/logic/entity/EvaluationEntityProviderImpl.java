/******************************************************************************
 * EvaluationEntityProviderImpl.java - created by aaronz on 23 May 2007
 * 
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.logic.entity;

import org.azeckoski.reflectutils.ReflectUtils;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.model.EvalEvaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation for the entity provider for evaluations, this allows basic CRUD operations as well
 * as searching for the evaluations a user has access to.
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationEntityProviderImpl implements EvaluationEntityProvider, CoreEntityProvider,
        AutoRegisterEntityProvider, RESTful, Describeable {

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private EvalEvaluationSetupService evalEvaluationSetupService;
    public void setEvalEvaluationSetupService(EvalEvaluationSetupService evalEvaluationSetupService) {
        this.evalEvaluationSetupService = evalEvaluationSetupService;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    @Override
    public String getEntityPrefix() {
        return ENTITY_PREFIX;
    }

    @Override
    public String[] getHandledInputFormats() {
        // Must not handle HTML format as that is done by the tool itself.
        return new String[]{Formats.JSON, Formats.XML};
    }

    @Override
    public String[] getHandledOutputFormats() {
        // Must not handle HTML format as that is done by the tool itself.
        return new String[]{Formats.JSON, Formats.XML};
    }

    @Override
    public Object getSampleEntity() {
        return new EvalEvaluation();
    }

    @Override
    public boolean entityExists(String id) {
        Long evaluationId;
        try {
            evaluationId = new Long(id);
            if (evaluationService.checkEvaluationExists(evaluationId)) {
                return true;
            }
        } catch (NumberFormatException e) {
            // invalid number so roll through to the false
        }
        return false;
    }

    @Override
    public List<?> getEntities(EntityReference ref, Search search) {
        String userId = commonLogic.getCurrentUserId();
        Boolean active = getBoolean(search, "active");
        Boolean untaken = getBoolean(search, "untaken");
        Boolean anonymous = getBoolean(search, "anonymous");
        List<EvalEvaluation> evaluations = evalEvaluationSetupService.getEvaluationsForUser(userId, active, untaken, anonymous);
        List<EvalEvaluation> clones = new ArrayList<EvalEvaluation>();
        for(EvalEvaluation evaluation: evaluations) {
            clones.add(clone(evaluation));
        }
        return clones;
    }

    @Override
    public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        EvalEvaluation evalEvaluation = (EvalEvaluation) entity;
        String userId = commonLogic.getCurrentUserId();
        evalEvaluationSetupService.saveEvaluation(evalEvaluation, userId, true);
        return evalEvaluation.getId().toString();
    }

    @Override
    public void deleteEntity(EntityReference ref, Map<String, Object> params) {
        Long evalId = getIdFromRef(ref);
        String userId = commonLogic.getCurrentUserId();
        evalEvaluationSetupService.deleteEvaluation(evalId, userId);
    }


    @Override
    public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        EvalEvaluation evalEvaluation = (EvalEvaluation) entity;
        String userId = commonLogic.getCurrentUserId();
        if (evaluationService.canControlEvaluation(userId, getIdFromRef(ref))) {
            evalEvaluationSetupService.saveEvaluation(evalEvaluation, userId, false);
        } else {
            throw new SecurityException("Cannot update: "+ ref);
        }
    }

    @Override
    public Object getEntity(EntityReference ref) {
        Long id = getIdFromRef(ref);
        String userId = commonLogic.getCurrentUserId();
        EvalEvaluation evaluation = evaluationService.getEvaluationById(id);
        if (evaluation != null)
            if (evaluationService.canControlEvaluation(userId, id)) {
                return clone(evaluation);
            } else {
                throw new SecurityException("Cannot update: "+ ref);
            }
        return null;
    }

    protected EvalEvaluation clone(EvalEvaluation evaluation) {
        // handler is in the list so that when a lazy proxy is returned by hibernate we don't copy the
        // internal handler.
        return ReflectUtils.getInstance().clone(evaluation, 1, new String[] {
                "responses", "availableEmailTemplate", "reminderEmailTemplate", "template", "evalAssignGroups", "handler"
        });
    }

    /**
     * Extract a numeric id from the ref if possible
     * @param ref the entity reference
     * @return the Long number version of the id
     * @throws IllegalArgumentException if the number cannot be extracted
     */
    protected Long getIdFromRef(EntityReference ref) {
        Long id;
        String refId = ref.getId();
        if (refId != null) {
            try {
                id = Long.valueOf(refId);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number found in reference ("+ref+") id: " + e);
            }
        } else {
            throw new IllegalArgumentException("No id in reference ("+ref+") id, cannot extract numeric id");
        }
        return id;
    }

    /**
     * Just looks for a property in a Search returning null if it's not present
     * @param search The search to look in.
     * @param property The property to look for.
     * @return <code>null</code> if the property isn't present otherwise it's boolean value.
     */
    protected Boolean getBoolean(Search search, String property) {
        Restriction activeProp = search.getRestrictionByProperty(property);
        return activeProp!= null?activeProp.getBooleanValue():null;
    }
}
