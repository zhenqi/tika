/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tika.parser.microsoft.ooxml.xwpf;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.tika.TikaTest;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Office;
import org.apache.tika.metadata.OfficeOpenXMLCore;
import org.apache.tika.metadata.OfficeOpenXMLExtended;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.RecursiveParserWrapper;
import org.apache.tika.parser.microsoft.MSOfficeParserConfig;
import org.junit.Test;


public class Word2006MLParserTest extends TikaTest {

    @Test
    public void basicTest() throws Exception {



        List<Metadata> metadataList = getRecursiveMetadata("testWORD_2006ml.xml");

        assertEquals(5, metadataList.size());

        Metadata m = metadataList.get(0);

        assertEquals("2016-11-23T12:07:00Z", m.get(TikaCoreProperties.CREATED));
        assertEquals("2016-11-23T12:07:00Z", m.get(TikaCoreProperties.MODIFIED));
        assertEquals("My Document Title", m.get(TikaCoreProperties.TITLE));
        assertEquals("This is the Author", m.get(TikaCoreProperties.CREATOR));
        assertEquals("2", m.get(OfficeOpenXMLCore.REVISION));
        assertEquals("Allison, Timothy B.", m.get(OfficeOpenXMLCore.LAST_MODIFIED_BY));
        assertEquals("0", m.get(OfficeOpenXMLExtended.DOC_SECURITY));
        assertEquals("225", m.get(Office.WORD_COUNT));
        assertEquals("3", m.get(Office.PARAGRAPH_COUNT));
        assertEquals("1506", m.get(Office.CHARACTER_COUNT_WITH_SPACES));
        assertEquals("10", m.get(Office.LINE_COUNT));
        assertEquals("16.0000", m.get(OfficeOpenXMLExtended.APP_VERSION));


        String content = m.get(RecursiveParserWrapper.TIKA_CONTENT);


        assertContainsCountTimes("engaging title page", content, 1);
        assertContainsCountTimes("<p>This is the Author</p>", content, 1);
        assertContainsCountTimes("<p>This is an engaging title page</p>", content, 1);

        assertContains("<p>My Document Title</p>", content);
        assertContains("<p>My Document Subtitle</p>", content);

        assertContains("<p>\tHeading1\t3</p>", content);


        //TODO: integrate numbering
        assertContains("Really basic 2.", content);

        assertContainsCountTimes("This is a text box", content, 1);

        assertContains("<p>This is a hyperlink: <a href=\"http://tika.apache.org\">tika</a></p>", content);

        assertContains("<p>This is a link to a local file: <a href=\"file:///C:\\data\\test.png\">test.png</a></p>", content);

        assertContains("<p>This is          10 spaces</p>", content);

        //caption
        assertContains("<p>Table 1: Table1 Caption</p>", content);

        //embedded table
        //TODO: figure out how to handle embedded tables in html
        assertContains("<p>Embedded table r1c1</p>", content);

        //shape
        assertContainsCountTimes("<p>This is text within a shape", content, 1);

        //sdt rich text
        assertContains("<p>Rich text content control", content);

        //sdt simple text
        assertContains("<p>Simple text content control", content);

        //sdt repeating
        assertContains("Repeating content", content);

        //sdt dropdown
        //TODO: get options for dropdown
        assertContains("Drop down1", content);

        //sdt date
        assertContains("<p>11/16/2016</p>", content);

        //test that <tab/> works
        assertContains("tab\ttab", content);

        assertContainsCountTimes("serious word art", content, 1);
        assertContainsCountTimes("Wordartr1c1", content, 1);

        //glossary document contents
        assertContains("Click or tap to enter a date", content);

        //basic formatting
        assertContains("<p>The <i>quick</i> brown <b>fox </b>j<i>um</i><b><i>ped</i></b> over",
                content);

        //TODO: add chart parsing
//        assertContains("This is the chart", content);

        assertContains("This is a comment", content);

        assertContains("This is an endnote", content);

        assertContains("this is the footnote", content);

        assertContains("First page header", content);

        assertContains("Even page header", content);

        assertContains("Odd page header", content);

        assertContains("First page footer", content);

        assertContains("Even page footer", content);

        assertContains("Odd page footer", content);

        //test default includes deleted
        assertContains("frog", content);

        assertContains("Mattmann", content);

        //TODO: extract this...Note that it is in "Backup" not "Choice"!!!
//        assertContains("This is the chart title", content);



    }

    private void assertContainsCountTimes(String needle, String haystack, int expectedCount) {
        int i = haystack.indexOf("engaging title page");
        int cnt = 0;
        while (i > -1) {
            cnt++;
            i = haystack.indexOf("engaging title page", i+1);
        }
        assertEquals("found needle >"+ needle+"<"+cnt+" times instead of expected: "+expectedCount,
                expectedCount, cnt);

    }

    @Test
    public void testSkipDeleted() throws Exception {
        ParseContext pc = new ParseContext();
        MSOfficeParserConfig msOfficeParserConfig = new MSOfficeParserConfig();
        msOfficeParserConfig.setIncludeDeletedContent(false);
        pc.set(MSOfficeParserConfig.class, msOfficeParserConfig);

        XMLResult r = getXML("testWORD_2006ml.xml", pc);
        assertNotContained("frog", r.xml);
    }

}
