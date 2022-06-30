package org.apache.velocity.test.eventhandler;

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

import org.apache.velocity.app.event.IncludeEventHandler;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.apache.velocity.app.event.NullSetEventHandler;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler;

/**
 * This is a test set of event handlers, used to test event handler sequences.
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @version $Id: Handler1.java 463298 2006-10-12 16:10:32Z henning $
 */
public class Handler1
    implements NullSetEventHandler, ReferenceInsertionEventHandler, MethodExceptionEventHandler, IncludeEventHandler {

        /**
         * never log
         */
        public boolean shouldLogOnNullSet(String lhs, String rhs)
        {
            return false;
        }

        /**
         * display output twice, once uppercase and once lowercase
         */
        public Object referenceInsert(String reference, Object value)
        {
            if (value == null)
                return null;
            else
                return value.toString().toUpperCase() + value.toString().toLowerCase();
        }

        /**
         * throw the exception
         */
        public Object methodException(Class claz, String method, Exception e) throws Exception
        {
            throw e;
        }

        /*
         * redirect all requests to a page "login.vm" (simulates access control).
         */
        public String includeEvent(
            String includeResourcePath,
            String currentResourcePath,
            String directiveName)
        {

            return "notfound.vm";

        }

}
