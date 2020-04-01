package org.sakaiproject.assignment.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses event references for assignment attachments and extracts out the useful data.
 */
public class AttachmentParser {

    // Pattern is thread-safe
    private Pattern pattern = Pattern.compile("(/content)?(?<attachment>/attachment/(?<siteId>[^/]+)/Assignments/(?<id>[^/]+)/(?<filename>.*))");

    public Details parse(String attachment) {
        Matcher matcher = pattern.matcher(attachment);
        if (matcher.matches()) {
            return new Details(matcher.group("attachment"), matcher.group("siteId"), matcher.group("id"), matcher.group("filename"));
        }
        return null;
    }

    /**
     * Object containing details of the parsed data out of the file reference.
     * Wouldn't be needed if we had multivalue returns.
     */
    public static class Details {

        private final String siteId;
        private final String assignmentId;
        private final String filename;
        private final String attachment;

        public Details(String attachment, String siteId, String assignmentId, String filename) {
            this.attachment = attachment;
            this.siteId = siteId;
            this.assignmentId = assignmentId;
            this.filename = filename;
        }

        public String getAttachment() {
            return attachment;
        }

        public String getSiteId() {
            return siteId;
        }

        public String getAssignmentId() {
            return assignmentId;
        }

        public String getFilename() {
            return filename;
        }
    }

}
