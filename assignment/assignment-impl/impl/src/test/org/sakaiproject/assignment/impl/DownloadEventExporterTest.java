package org.sakaiproject.assignment.impl;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.assignment.api.DownloadEvent;
import org.sakaiproject.assignment.api.DownloadTrackingService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DownloadEventExporterTest {

    private final String ATTACHMENT = "/content/attachment/siteId/Assignments/assignmentId/filename.txt";
    private Date submissionTime;
    private Date downloadTime;
    private DownloadTrackingService service;
    private DownloadEventExporter exporter;

    @Before
    public void setUp() throws ParseException {
         submissionTime = new SimpleDateFormat("yyyy/MM/dd").parse("2000/01/02");
        downloadTime = new SimpleDateFormat("yyyy/MM/dd").parse("2000/01/01");
        service = mock(DownloadTrackingService.class);
        exporter = new DownloadEventExporter(service, ATTACHMENT);
        exporter.setLocale(Locale.UK);
        exporter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Test
    public void testNoDownloads() throws IOException {
        exporter.addUser("userId", "anonId", submissionTime);
        when(service.getDownloads(ATTACHMENT)).thenReturn(Collections.emptyList());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        exporter.write(bos);
        String output = bos.toString(StandardCharsets.UTF_8.name());
        assertEquals(quote("'User','Submission Time','Download of: filename.txt','Time before submission of: filename.txt'\n" +
                "'anonId','02 January 2000 00:00:00 GMT','',''\n"), output);
    }

    @Test
    public void testDifferentDownloads() throws IOException {
        exporter.addUser("userId", "anonId", submissionTime);
        when(service.getDownloads(ATTACHMENT)).thenReturn(Collections.singletonList(new DownloadEvent(downloadTime, "otherId")));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        exporter.write(bos);
        String output = bos.toString(StandardCharsets.UTF_8.name());
        assertEquals(quote("'User','Submission Time','Download of: filename.txt','Time before submission of: filename.txt'\n" +
                "'anonId','02 January 2000 00:00:00 GMT','',''\n"), output);
    }

    @Test
    public void testNoSubmission() throws IOException {
        exporter.addUser("userId", "anonId", null);
        when(service.getDownloads(ATTACHMENT)).thenReturn(Collections.singletonList(new DownloadEvent(downloadTime, "userId")));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        exporter.write(bos);
        String output = bos.toString(StandardCharsets.UTF_8.name());
        assertEquals(quote("'User','Submission Time','Download of: filename.txt','Time before submission of: filename.txt'\n" +
                "'anonId','','01 January 2000 00:00:00 GMT',''\n"), output);
    }

    @Test
    public void testSubmissionAndDownload() throws IOException {
        exporter.addUser("userId", "anonId", submissionTime);
        when(service.getDownloads(ATTACHMENT)).thenReturn(Collections.singletonList(new DownloadEvent(downloadTime, "userId")));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        exporter.write(bos);
        String output = bos.toString(StandardCharsets.UTF_8.name());
        assertEquals(quote("'User','Submission Time','Download of: filename.txt','Time before submission of: filename.txt'\n" +
                        "'anonId','02 January 2000 00:00:00 GMT','01 January 2000 00:00:00 GMT','24:00:00'\n"),
                output);
        System.out.println(output);
    }

    // Replaces single quotes with double quotes (to make literals more readable)
    private String quote(String original) {
        return original.replaceAll("'", "\\\"");
    }


}