package org.sakaiproject.assignment.impl;

import au.com.bytecode.opencsv.CSVWriter;
import org.sakaiproject.assignment.api.DownloadEvent;
import org.sakaiproject.assignment.api.DownloadTrackingService;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This exports a spreadsheet containing details of each submitter, when they submitted and when they downloaded the attachments.
 */
public class DownloadEventExporter {

    // Maps from Sakai ID to display ID (eg anon identifier)
    private final Map<String, SubmissionDetails> users;
    private DownloadTrackingService service;
    private String[] attachments;
    private AttachmentParser parser;
    // We don't at the moment want to adjust the timezone as we will just use the server timezone
    private TimeZone timeZone;
    private Locale locale;


    public DownloadEventExporter(DownloadTrackingService service , String... attachments) {
        this.service = service;
        this.attachments = attachments;
        // We want to keep insertion order.
        users = new LinkedHashMap<>();
        parser = new AttachmentParser();
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public void addUser(String id, String displayId, Date submissionTime) {
        users.put(id, new SubmissionDetails(displayId, submissionTime));
    }

    public void write(OutputStream output) throws IOException {
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(output, StandardCharsets.UTF_8);
        CSVWriter writer = new CSVWriter(outputStreamWriter);
        // Maps attachment to filename
        Map<String, String> downloadColumns = new LinkedHashMap<>();
        for (String attachment: attachments) {
            AttachmentParser.Details parse = parser.parse(attachment);
            if (parse != null) {
                downloadColumns.put(attachment, parse.getFilename());
            }
        }
        List<String> columns = new ArrayList<>();
        columns.add("User");
        columns.add("Submission Time");
        downloadColumns.forEach((attachment, filename) -> {
            columns.add("Download of: "+filename);
            columns.add("Time before submission of: "+filename);
        });
        writeRow(writer, columns.toArray(new String[]{}));


        // Get the results from the DB
        Map<String, Map<String, Date>> downloads = new LinkedHashMap<>();
        for (String attachmentPath : downloadColumns.keySet()) {
            downloads.put(attachmentPath, getDownloadTimes(attachmentPath));
        }

        // Output a row for each user.
        users.forEach((userId, submissionDetails) -> {
            List<String> row = new ArrayList<>();
            row.add(submissionDetails.displayId);
            row.add(formatDate(submissionDetails.submission));
            for(String attachmentPath : downloadColumns.keySet()) {
                Date downloadDate = downloads.get(attachmentPath).get(userId);
                Duration taken = between(submissionDetails.submission, downloadDate);
                row.add(formatDate(downloadDate));
                row.add(formatDuration(taken));
            }
            writeRow(writer, row.toArray(new String[]{}));
        });
        writer.flush();
    }

    protected Duration between(Date end, Date start) {
        if (end!= null && start != null) {
            long ms = end.getTime() - start.getTime();
            return Duration.ofMillis(ms);
        }
        return null;
    }

    private String formatDuration(Duration duration) {
        if(duration == null) {
            return "";
        }
        long s = duration.getSeconds();
        return String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        DateFormat dateInstance = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.LONG, SimpleDateFormat.LONG, locale);
        return dateInstance.format(date);
    }

    private Map<String, Date> getDownloadTimes(String attachmentPath) {
        List<DownloadEvent> downloads = service.getDownloads(attachmentPath);
        // We need to allow fast lookups on userID.
        return downloads.stream().collect(Collectors.toMap(DownloadEvent::getUserId, DownloadEvent::getDatetime));
    }

    private static void writeRow(CSVWriter writer, String... items) {
        writer.writeNext(items);
    }

    private static class SubmissionDetails {
        private final String displayId;
        private final Date submission;

        public SubmissionDetails(String displayId, Date submission) {
            this.displayId = displayId;
            this.submission = submission;
        }
    }

}
