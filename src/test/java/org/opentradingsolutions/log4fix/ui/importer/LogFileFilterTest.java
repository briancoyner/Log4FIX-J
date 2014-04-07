/*
 * The Log4FIX Software License
 * Copyright (c) 2006 - 2011 Brian M. Coyner All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 3. Neither the name of the product (Log4FIX), nor Brian M. Coyner,
 * nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL BRIAN M. COYNER OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

package org.opentradingsolutions.log4fix.ui.importer;

import junit.framework.TestCase;

import java.io.File;

/**
 * @author Brian M. Coyner
 */
public class LogFileFilterTest extends TestCase {

    private LogFileFilter filter;

    @Override
    protected void setUp() throws Exception {
        filter = new LogFileFilter();
    }

    public void testNullFileIsNotAccepted() {
        assertFalse("A null file should not be available.", filter.accept(null));
    }

    public void testExistingDirectoryIsAccepted() {
        assertTrue("An existing directory should be accepted.", filter.accept(MockFile.createExistingDirectory()));
    }

    public void testNonExistingFileIsNotAccepted() {
        assertFalse("An non-existent file should not be accepted.",
                filter.accept(MockFile.createMissingFile("junk.log")));
    }

    public void testDotLogFile() {
        assertTrue("*.log file should be accepted.", filter.accept(MockFile.createExistingFile("today.log")));
    }

    public void testDotInFile() {
        assertTrue("*.in file should be accepted.", filter.accept(MockFile.createExistingFile("today.in")));
    }

    public void testDotOutFile() {
        assertTrue("*.out file should be accepted.", filter.accept(MockFile.createExistingFile("today.out")));
    }

    public void testDotTextFile() {
        assertFalse("*.txt file should not be accepted.", filter.accept(MockFile.createExistingFile("today.txt")));
    }

    // @todo - make the LogFileFilter retrieve valid extensions from an ImporterModel

    private static class MockFile extends File {

        private boolean isFile;
        private boolean doesExist;

        public static MockFile createExistingFile(String filename) {
            MockFile mockFile = new MockFile(filename);
            mockFile.isFile = true;
            mockFile.doesExist = true;
            return mockFile;
        }

        public static MockFile createMissingFile(String filename) {
            MockFile mockFile = new MockFile(filename);
            mockFile.isFile = true;
            mockFile.doesExist = false;
            return mockFile;
        }

        public static MockFile createExistingDirectory() {
            MockFile mockFile = new MockFile("");
            mockFile.isFile = false;
            mockFile.doesExist = true;
            return mockFile;
        }

        private MockFile(String filename) {
            super("/path/to/file/does/not/matter/" + filename);
        }

        @Override
        public boolean isFile() {
            return isFile;
        }

        @Override
        public boolean isDirectory() {
            return !isFile;
        }

        @Override
        public boolean exists() {
            return doesExist;
        }
    }
}