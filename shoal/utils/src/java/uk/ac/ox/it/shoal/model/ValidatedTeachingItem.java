package uk.ac.ox.it.shoal.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;

/**
 * This proxies through to a teaching item to allow validation.
 * We can't have the annotations in the API so we need to wrap a teaching item so that bean validation
 * works on it.
 */
public class ValidatedTeachingItem implements TeachingItem, Serializable {

    private TeachingItem item;

    public ValidatedTeachingItem(TeachingItem item) {
        this.item = item;
    }

    public String getId() {
        return item.getId();
    }

    public void setId(String id) {
        item.setId(id);
    }

    // This is
    @NotNull @Size(min=5, max=30)
    public String getTitle() {
        return item.getTitle();
    }

    public void setTitle(String title) {
        item.setTitle(title);
    }

    @NotNull @Size(min=10, max=4096)
    public String getDescription() {
        return item.getDescription();
    }

    public void setDescription(String description) {
        item.setDescription(description);
    }

    @NotNull @Size(min=1)
    public Collection<String> getSubject() {
        return item.getSubject();
    }

    public void setSubject(Collection<String> subject) {
        item.setSubject(subject);
    }

    @NotNull @Size(min=1)
    public Collection<String> getLevel() {
        return item.getLevel();
    }

    public void setLevel(Collection<String> level) {
        item.setLevel(level);
    }

    @NotNull
    @Size(min=1)
    public Collection<String> getPurpose() {
        return item.getPurpose();
    }

    public void setPurpose(Collection<String> purpose) {
        item.setPurpose(purpose);
    }

    @NotNull
    public String getInteractivity() {
        return item.getInteractivity();
    }

    public void setInteractivity(String interactivity) {
        item.setInteractivity(interactivity);
    }

    @NotNull
    @Size(min=1)
    public Collection<String> getType() {
        return item.getType();
    }

    public void setType(Collection<String> type) {
        item.setType(type);
    }

    @NotNull
    @Size(min=5, max=128)
    public String getAuthor() {
        return item.getAuthor();
    }

    public void setAuthor(String author) {
        item.setAuthor(author);
    }

    @Size(min=5, max=128)
    public String getContact() {
        return item.getContact();
    }

    public void setContact(String contact) {
        item.setContact(contact);
    }

    public Instant getAdded() {
        return item.getAdded();
    }

    public void setAdded(Instant added) {
        item.setAdded(added);
    }

    @Size(min=10, max=1024)
    public String getPermission() {
        return item.getPermission();
    }

    public void setPermission(String permission) {
        item.setPermission(permission);
    }

    public String getThumbnail() {
        return item.getThumbnail();
    }

    public void setThumbnail(String thumbnail) {
        item.setThumbnail(thumbnail);
    }

    @NotNull
    public String getLicense() {
        return item.getLicense();
    }

    public void setLicense(String license) {
        item.setLicense(license);
    }

    public String getUrl() {
        return item.getUrl();
    }

    public void setUrl(String url) {
        item.setUrl(url);
    }

}
