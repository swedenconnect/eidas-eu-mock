package org.apache.velocity.test;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.test.misc.TestLogChute;

/**
 * Tests if the VM template-locality is working.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 * @version $Id: InlineScopeVMTestCase.java 832247 2009-11-03 01:29:30Z wglass $
 */
public class InlineScopeVMTestCase extends BaseTestCase implements TemplateTestBase
{
    VelocityEngine engine;
    public InlineScopeVMTestCase(String name)
    {
        super(name);
    }

    public void setUp()
            throws Exception
    {
        engine = new VelocityEngine();
        
        engine.setProperty(
                RuntimeConstants.VM_PERM_ALLOW_INLINE_REPLACE_GLOBAL, "true");

        engine.setProperty(
                RuntimeConstants.VM_PERM_INLINE_LOCAL, "true");

        engine.setProperty(
                RuntimeConstants.FILE_RESOURCE_LOADER_PATH, FILE_RESOURCE_LOADER_PATH);
        
        engine.setProperty(
                RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, TestLogChute.class.getName());

        engine.init();
    }

    public static Test suite ()
    {
        return new TestSuite(InlineScopeVMTestCase.class);
    }

    /**
     * Runs the test.
     */
    public void testInlineScopeVM ()
            throws Exception
    {
        assureResultsDirectoryExists(RESULT_DIR);

        /*
         * Get the template and the output. Do them backwards.
         * vm_test2 uses a local VM and vm_test1 doesn't
         */

        Template template2 = engine.getTemplate(
            getFileName(null, "vm_test2", TMPL_FILE_EXT));

        Template template1 = engine.getTemplate(
            getFileName(null, "vm_test1", TMPL_FILE_EXT));

        FileOutputStream fos1 =
            new FileOutputStream (
                getFileName(RESULT_DIR, "vm_test1", RESULT_FILE_EXT));

        FileOutputStream fos2 =
            new FileOutputStream (
                getFileName(RESULT_DIR, "vm_test2", RESULT_FILE_EXT));

        Writer writer1 = new BufferedWriter(new OutputStreamWriter(fos1));
        Writer writer2 = new BufferedWriter(new OutputStreamWriter(fos2));

        /*
         *  put the Vector into the context, and merge both
         */

        VelocityContext context = new VelocityContext();

        template1.merge(context, writer1);
        writer1.flush();
        writer1.close();

        template2.merge(context, writer2);
        writer2.flush();
        writer2.close();

        if (!isMatch(RESULT_DIR,COMPARE_DIR,"vm_test1",
                RESULT_FILE_EXT,CMP_FILE_EXT) ||
            !isMatch(RESULT_DIR,COMPARE_DIR,"vm_test2",
                RESULT_FILE_EXT,CMP_FILE_EXT))
        {
            fail("Output incorrect.");
        }
    }
}
