package org.sakaiproject.rubrics.controller;

import java.util.Map;

import org.sakaiproject.rubrics.logic.RubricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RubricDetailsController {

    @Autowired
    private RubricsService rubricsService;

    private final String HEADER_AUTHORIZATION = "authorization";

    @GetMapping("/getSiteTitleForRubric")
    @ResponseBody
    public String getSiteTitleForRubric(@RequestHeader Map<String, String> headers, @RequestParam String rubricId) {
        try
        {
            return rubricsService.getSiteTitleForRubric(Long.parseLong(rubricId), headers.get(HEADER_AUTHORIZATION));
        } catch (SecurityException e) {
            throw new org.springframework.security.access.AccessDeniedException("401 returned");
        } catch (Exception e) {
            // throw exception - 500 error will be handled by UI
            throw e instanceof RuntimeException ? (RuntimeException)e : new RuntimeException(e);
        }
    }

    @GetMapping("/getCreatorDisplayNameForRubric")
    @ResponseBody
    public String getCreatorDisplayNameForRubric(@RequestHeader Map<String, String> headers, @RequestParam String rubricId) {
        try
        {
            return rubricsService.getCreatorDisplayNameForRubric(Long.parseLong(rubricId), headers.get(HEADER_AUTHORIZATION));
        } catch (SecurityException e) {
            throw new org.springframework.security.access.AccessDeniedException("401 returned");
        } catch (Exception e) {
            // throw exception - 500 error will be handled by UI
            throw e instanceof RuntimeException ? (RuntimeException)e : new RuntimeException(e);
        }
    }
}
