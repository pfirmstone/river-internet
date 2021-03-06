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
package org.apache.river.test.spec.loader.downloadPermission;

import java.util.logging.Level;

// org.apache.river.qa.harness
import org.apache.river.qa.harness.TestException;
import org.apache.river.qa.harness.QAConfig;
import org.apache.river.qa.harness.Test;
// org.apache.river.qa
import org.apache.river.qa.harness.QATestEnvironment;
import org.apache.river.qa.harness.QAConfig;

// java.util.logging
import java.util.logging.Logger;
import java.util.logging.Level;

// java.security
import java.security.BasicPermission;

// davis packages
import net.jini.loader.DownloadPermission;


/**
 * <b>Purpose</b><br><br>
 *
 * This test verifies the behavior of the<br>
 *    <code>public DownloadPermission()</code> and
 *    <code>public DownloadPermission(String name)</code>
 *    constructors of the<br>
 * <code>net.jini.loader.DownloadPermission</code> class:
 *
 * <br><blockquote>
 * Creates a new <code>DownloadPermission</code> with "permit" as
 * the symbolic name of the permission.  Note that in a security
 * policy file it is sufficient to grant
 * <code>DownloadPermission</code> with no explicit name argument
 * in order to permit downloading from a particular codebase.
 * This default constructor will supply "permit" as the required
 * permission name.
 * </blockquote>
 *
 * <b>Test Description</b><br><br>
 *
 *  This test do the following:
 *  <ul>
 *   <li><lh>for DownloadPermission() constructor</lh>
 *     <ol>
 *      <li>create DownloadPermission() object</li>
 *      <li>check that created object is instance of  BasicPermission</li>
 *      <li>check that getName() returns "permit" string.</li>
 *     </ol>
 *   </li>
 *  </ul>
 *
 * <b>Infrastructure</b><br><br>
 *
 * <ul><lh>This test requires the following infrastructure:</lh>
 *  <li> infrastructure is not required</li>
 * </ul>
 *
 * <b>Actions</b><br><br>
 *
 * <ol>
 *  <li>This test do the following:
 *    <li>for default DownloadPermission() constructor
 *     <ol>
 *       <li>create DownloadPermission() object</li>
 *       <li>check that created object is instance of  BasicPermission</li>
 *       <li>check that getName() returns "permit" string</li>
 *     </ol>
 *    </li>
 *  </li>
 * </ol>
 *
 */
public class Constructor extends QATestEnvironment implements Test {

    /** Symbolic name of default DownloadPermission object */
    private static final String DEFAULT_NAME = "permit";

    /** The string that indicates fail status */
    String message = "";

    /**
     * Sets up the testing environment.
     *
     * @param sysConfig QAConfig from the runner for construct.
     */
    public Test construct(QAConfig sysConfig) throws Exception {
        // Set shared vm mode to be disabled in all cases
        ((QAConfig)
                sysConfig).setDynamicParameter("org.apache.river.qa.harness.shared",
                "false");

        // mandatory call to parent
        super.construct(sysConfig);
        return this;
    }

    /**
     * Run the test according <b>Test Description</b>
     */
    public void run() throws Exception {
        DownloadPermission downloadPermission = new DownloadPermission();

        if (!(downloadPermission instanceof BasicPermission)) {
            message += "DownloadPermission()\n"
                    + "creates object that is not instance of BasicPermission";
        }
        String returned = downloadPermission.getName();
        String expected = DEFAULT_NAME;

        if (!returned.equals(expected)) {
            message += "DownloadPermission()\n" + "  created: \"" + returned
                    + "\" DownloadPermission" + "\n expected: \"" + expected
                    + "\" DownloadPermission";
        } else {
            String msg = "DownloadPermission()" + "  created: \"" + returned
                    + "\" DownloadPermission" + " as expected";
            logger.log(Level.FINE, msg);
        }

        if (message.length() > 0) {
            throw new TestException(message);
        }
    }
}
