/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.river.qa.harness;

import java.io.IOException;
import org.apache.river.api.io.AtomicSerial;
import org.apache.river.api.io.AtomicSerial.GetArg;

/**
 * A <code>SlaveTestRequest</code> which sets a dynamic parameter
 * on the slave.
 */
@AtomicSerial
class SetDynamicParameterRequest implements SlaveRequest {

    /** the name of the parameter */
    String name;

    /** the value of the parameter */
    String value;

    /**
     * Construct the request.
     *
     * @param name the parameter name
     * @param value the parameter value
     */
    public SetDynamicParameterRequest(String name, String value) {
	this.name = name;
	this.value = value;
    }
    
    public SetDynamicParameterRequest(GetArg arg) throws IOException, ClassNotFoundException{
	this(arg.get("name", null, String.class),
	    arg.get("value", null, String.class));
    }

    /**
     * Called by the <code>SlaveTest</code> to set the value of the
     * dynamic parameter for the test.
     *
     * @param slaveTest a reference to the <code>SlaveTest</code>.
     * @return <code>null</code>
     * @throws Exception never
     */
    public Object doSlaveRequest(SlaveTest slaveTest) throws Exception {
	QAConfig config = slaveTest.getConfig();
	config.setDynamicParameter(name, value);
	return null;
    }
}
