package uk.ac.ox.oucs.vle.resources;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import uk.ac.ox.oucs.vle.CourseComponent;
import uk.ac.ox.oucs.vle.CourseSignup;

import java.util.Set;

/**
 * This allows us to keep generic information at runtime so that when serialising mocks
 * we don't blow up when we don't know the type. Although we will also need this to allow
 * the signups to be smaller when the are serialised.
 */
public abstract class CourseSignupMixin implements CourseSignup {
    
    @Override
    @JsonSerialize(as=Set.class, contentAs=CourseComponent.class)
    public abstract Set<CourseComponent> getComponents();
}
