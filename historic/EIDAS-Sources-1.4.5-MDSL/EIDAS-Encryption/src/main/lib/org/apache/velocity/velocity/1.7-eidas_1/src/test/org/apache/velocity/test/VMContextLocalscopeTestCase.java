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

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.InternalContextAdapterImpl;
import org.apache.velocity.context.ProxyVMContext;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;

/**
 * Tests scope of velocimacros with localscope setting. 
 * 
 * @author <a href="mailto:stephenh@chase3000.com">Stephen Habermann</a>
 * @version $Id: VMContextLocalscopeTestCase.java 747235 2009-02-24 00:32:03Z nbubna $
 */
public class VMContextLocalscopeTestCase extends BaseTestCase {

    public VMContextLocalscopeTestCase(String name)
    {
        super(name);
    }

    public void testViaEval()
    {
        engine.setProperty(RuntimeConstants.VM_CONTEXT_LOCALSCOPE, Boolean.TRUE);
        assertEvalEquals("$a", "#macro(a)#set($a = 'b')#end#a$a");
        context.put("b", "b");
        assertEvalEquals("b", "#macro(b)$b#set($b = 'c')#end#b");
        assertContextValue("b", "b");
    }

    public void testLocalscopePutDoesntLeakButGetDoes() 
    {
        RuntimeInstance instance = new RuntimeInstance();
        instance.setProperty(RuntimeConstants.VM_CONTEXT_LOCALSCOPE, Boolean.TRUE);
        instance.init();

        VelocityContext base = new VelocityContext();
        base.put("outsideVar", "value1");

        ProxyVMContext vm =
            new ProxyVMContext(new InternalContextAdapterImpl(base), instance, true);
        vm.put("newLocalVar", "value2");

        // New variable put doesn't leak
        assertNull(base.get("newLocalVar"));
        assertEquals("value2", vm.get("newLocalVar"));

        // But we can still get to "outsideVar"
        assertEquals("value1", vm.get("outsideVar"));

        // If we decide to try and set outsideVar it won't leak
        vm.put("outsideVar", "value3");
        assertEquals("value3", vm.get("outsideVar"));
        assertEquals("value1", base.get("outsideVar"));
    }

}
