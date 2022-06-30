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

import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.apache.velocity.util.introspection.IntrospectorCacheImpl;

/**
 * Tests if we can hand Velocity an arbitrary class for logging.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ClassloaderChangeTestCase.java 680817 2008-07-29 19:49:41Z nbubna $
 */
public class ClassloaderChangeTestCase extends TestCase implements LogChute
{
    private VelocityEngine ve = null;
    private boolean sawCacheDump = false;

    private static String OUTPUT = "Hello From Foo";


    /**
     * Default constructor.
     */
    public ClassloaderChangeTestCase(String name)
    {
        super(name);
    }

    public void setUp()
            throws Exception
    {
        ve = new VelocityEngine();
        ve.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, this );
        ve.init();
    }

    public void init( RuntimeServices rs )
    {
        // do nothing with it
    }

    public static Test suite ()
    {
        return new TestSuite(ClassloaderChangeTestCase.class);
    }

    /**
     * Runs the test.
     */
    public void testClassloaderChange()
        throws Exception
    {
        sawCacheDump = false;

        VelocityContext vc = new VelocityContext();
        Object foo = null;

        /*
         *  first, we need a classloader to make our foo object
         */

        TestClassloader cl = new TestClassloader();
        Class fooclass = cl.loadClass("Foo");
        foo = fooclass.newInstance();

        /*
         *  put it into the context
         */
        vc.put("foo", foo);

        /*
         *  and render something that would use it
         *  that will get it into the introspector cache
         */
        StringWriter writer = new StringWriter();
        ve.evaluate( vc, writer, "test", "$foo.doIt()");

        /*
         *  Check to make sure ok.  note the obvious
         *  dependency on the Foo class...
         */

        if ( !writer.toString().equals( OUTPUT ))
        {
            fail("Output from doIt() incorrect");
        }

        /*
         * and do it again :)
         */
        cl = new TestClassloader();
        fooclass = cl.loadClass("Foo");
        foo = fooclass.newInstance();

        vc.put("foo", foo);

        writer = new StringWriter();
        ve.evaluate( vc, writer, "test", "$foo.doIt()");

        if ( !writer.toString().equals( OUTPUT ))
        {
            fail("Output from doIt() incorrect");
        }

        if (!sawCacheDump)
        {
            fail("Didn't see introspector cache dump.");
        }
    }

    /**
     *  method to catch Velocity log messages.  When we
     *  see the introspector dump message, then set the flag
     */
    public void log(int level, String message)
    {
        if (message.equals( IntrospectorCacheImpl.CACHEDUMP_MSG) )
        {
            sawCacheDump = true;
        }
    }

    /**
     *  method to catch Velocity log messages.  When we
     *  see the introspector dump message, then set the flag
     */
    public void log(int level, String message, Throwable t)
    {
        // ignore the Throwable for this test
        log(level, message);
    }

    public boolean isLevelEnabled(int level)
    {
        return true;
    }
}

/**
 *  Simple (real simple...) classloader that depends
 *  on a Foo.class being located in the classloader
 *  directory under test
 */
class TestClassloader extends ClassLoader
{
    private final static String testclass =
        "test/classloader/Foo.class";

    private Class fooClass = null;

    public TestClassloader()
            throws Exception
    {
        File f = new File( testclass );

        byte[] barr = new byte[ (int) f.length() ];

        FileInputStream fis = new FileInputStream( f );
        fis.read( barr );
        fis.close();

        fooClass = defineClass("Foo", barr, 0, barr.length);
    }


    public Class findClass(String name)
    {
        return fooClass;
    }
}
