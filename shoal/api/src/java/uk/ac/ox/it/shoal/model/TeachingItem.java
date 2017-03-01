package uk.ac.ox.it.shoal.model;

import java.time.Instant;
import java.util.Collection;

/**
 * Interface so we don't don't have to annotate it because then we need to have the annotation library in the
 * shared library which is more complicated to deploy.
 */
public interface TeachingItem {

    String getId();

    void setId(String id);

    String getTitle();

    void setTitle(String title);

    String getDescription();

    void setDescription(String description);

    Collection<String> getSubject();

    void setSubject(Collection<String> subject);

    Collection<String> getLevel();

    void setLevel(Collection<String> level);

    Collection<String> getPurpose();

    void setPurpose(Collection<String> purpose);

    String getInteractivity();

    void setInteractivity(String interactivity);

    Collection<String> getType();

    void setType(Collection<String> type);

    String getAuthor();

    void setAuthor(String author);

    String getContact();

    void setContact(String contact);

    Instant getAdded();

    void setAdded(Instant added);

    String getPermission();

    void setPermission(String permission);

    String getThumbnail();

    void setThumbnail(String thumbnail);

    String getLicense();

    void setLicense(String license);

    String getUrl();

    void setUrl(String url);

    boolean isHidden();

    void setHidden(boolean hidden);

}
