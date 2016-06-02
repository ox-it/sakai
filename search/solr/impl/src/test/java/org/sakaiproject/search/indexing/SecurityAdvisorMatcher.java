package org.sakaiproject.search.indexing;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.sakaiproject.authz.api.SecurityAdvisor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;

import static org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;

/**
 * @author Colin Hebert
 */
public class SecurityAdvisorMatcher extends BaseMatcher<SecurityAdvisor> {
    private Collection<SecurityAdvisorUnlock> unlockTests = new LinkedList<SecurityAdvisorUnlock>();

    @Override
    public boolean matches(Object item) {
        if (!(item instanceof SecurityAdvisor))
            return false;

        SecurityAdvisor securityAdvisor = (SecurityAdvisor) item;

        for (SecurityAdvisorUnlock unlockTest : unlockTests) {
            if (!securityAdvisor.isAllowed(unlockTest.userId, unlockTest.function, unlockTest.reference)
                    .equals(unlockTest.response))
                return false;
        }

        return true;
    }

    @Override
    public void describeTo(Description description) {
    }

    public SecurityAdvisorMatcher addUnlockCheck(String userId, String function, String reference,
                                                 SecurityAdvice response) {
        unlockTests.add(new SecurityAdvisorUnlock(userId, function, reference, response));
        return this;
    }

    public SecurityAdvisorMatcher addUnlockCheckRandom(SecurityAdvice response) {
        return addUnlockCheck(UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                response);
    }

    private class SecurityAdvisorUnlock {
        private final String userId;
        private final String function;
        private final String reference;
        private final SecurityAdvice response;

        private SecurityAdvisorUnlock(String userId, String function, String reference, SecurityAdvice response) {
            this.userId = userId;
            this.function = function;
            this.reference = reference;
            this.response = response;
        }
    }
}
