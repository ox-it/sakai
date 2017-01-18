package uk.ac.ox.it.shoal.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;

/**
 * This is the model of one of the teaching items that we are editing.
 */
public class TeachingItemModel implements Serializable, TeachingItem {

    private String id;
    private String title;
    private String description;
    private Collection<String> subject;
    private Collection<String> level;
    private Collection<String> purpose;
    private String interactivity;
    private Collection<String> type;
    private String author;
    private String contact;
    private Instant added;
    private String thumbnail;
    private String license;
    private String url;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Collection<String> getSubject() {
        return subject;
    }

    @Override
    public void setSubject(Collection<String> subject) {
        this.subject = subject;
    }

    @Override
    public Collection<String> getLevel() {
        return level;
    }

    @Override
    public void setLevel(Collection<String> level) {
        this.level = level;
    }

    @Override
    public Collection<String> getPurpose() {
        return purpose;
    }

    @Override
    public void setPurpose(Collection<String> purpose) {
        this.purpose = purpose;
    }

    @Override
    public String getInteractivity() {
        return interactivity;
    }

    @Override
    public void setInteractivity(String interactivity) {
        this.interactivity = interactivity;
    }

    @Override
    public Collection<String> getType() {
        return type;
    }

    @Override
    public void setType(Collection<String> type) {
        this.type = type;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public String getContact() {
        return contact;
    }

    @Override
    public void setContact(String contact) {
        this.contact = contact;
    }

    @Override
    public Instant getAdded() {
        return added;
    }

    @Override
    public void setAdded(Instant added) {
        this.added = added;
    }

    @Override
    public String getThumbnail() {
        return thumbnail;
    }

    @Override
    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Override
    public String getLicense() {
        return license;
    }

    @Override
    public void setLicense(String license) {
        this.license = license;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }
}
