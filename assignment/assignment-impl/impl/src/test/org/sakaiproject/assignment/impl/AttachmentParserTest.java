package org.sakaiproject.assignment.impl;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AttachmentParserTest {

    private AttachmentParser parser;

    @Before
    public void setUp() {
        parser = new AttachmentParser();
    }

    @Test
    public void testAssignmentRef() {
        AttachmentParser.Details parse = parser.parse("/attachment/db52f761-fda4-447a-a757-2c83effb99c9/Assignments/30a7c9f8-efb4-4fbe-876c-438787f9f93d/cover final.pdf");
        assertNotNull(parse);
        assertEquals("/attachment/db52f761-fda4-447a-a757-2c83effb99c9/Assignments/30a7c9f8-efb4-4fbe-876c-438787f9f93d/cover final.pdf", parse.getAttachment());
        assertEquals(parse.getAssignmentId(), "30a7c9f8-efb4-4fbe-876c-438787f9f93d");
        assertEquals(parse.getSiteId(), "db52f761-fda4-447a-a757-2c83effb99c9");
        assertEquals(parse.getFilename(), "cover final.pdf");
    }

    @Test
    public void testContentEvent() {
        AttachmentParser.Details parse = parser.parse("/content/attachment/db52f761-fda4-447a-a757-2c83effb99c9/Assignments/30a7c9f8-efb4-4fbe-876c-438787f9f93d/cover final.pdf");
        assertNotNull(parse);
        assertEquals("/attachment/db52f761-fda4-447a-a757-2c83effb99c9/Assignments/30a7c9f8-efb4-4fbe-876c-438787f9f93d/cover final.pdf", parse.getAttachment());
        assertEquals(parse.getAssignmentId(), "30a7c9f8-efb4-4fbe-876c-438787f9f93d");
        assertEquals(parse.getSiteId(), "db52f761-fda4-447a-a757-2c83effb99c9");
        assertEquals(parse.getFilename(), "cover final.pdf");
    }

}