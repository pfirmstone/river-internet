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
package org.apache.river.test.spec.lookupservice.test_set01;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.StubNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import net.jini.core.entry.Entry;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.lease.UnknownLeaseException;
import net.jini.core.lookup.ServiceEvent;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceRegistrar;
import net.jini.core.lookup.ServiceRegistration;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.io.MarshalledInstance;
import org.apache.river.api.io.AtomicMarshalledInstance;
import org.apache.river.qa.harness.QAConfig;
import org.apache.river.qa.harness.Test;
import org.apache.river.qa.harness.TestException;
import org.apache.river.test.spec.lookupservice.QATestRegistrar;
import org.apache.river.test.spec.lookupservice.QATestUtils;
import org.apache.river.test.spec.lookupservice.RemoteEventComparator;

/** This class is used to verify that after using templates containing a 
 *  combination of service types and attributes with all Null fields to
 *  request event notification, upon the expiration of each registered
 *  service, the expected set of events will be generated by the lookup
 *  service.
 *
 *  @see org.apache.river.qa.harness.TestEnvironment
 *  @see org.apache.river.test.spec.lookupservice.QATestRegistrar
 *  @see org.apache.river.test.spec.lookupservice.QATestUtils
 */
public class NotifyOnComboSrvcLeaseExp extends QATestRegistrar {

    private static boolean SHOW_TIMINGS = false;

    /** Class which handles all events sent by the lookup service */
    private class Listener extends BasicListener 
                          implements RemoteEventListener
    {
        public Listener() throws RemoteException {
            super();
        }
        /** Method called remotely by lookup to handle the generated event. */
        public void notify(RemoteEvent ev) {
            ServiceEvent srvcEvnt = (ServiceEvent)ev;
            synchronized (NotifyOnComboSrvcLeaseExp.this){
                evntVec.add(srvcEvnt);
                try {
                    QATestUtils.SrvcAttrTuple tuple = (QATestUtils.SrvcAttrTuple)
                                          (srvcEvnt.getRegistrationInstance().get(false));

                    receivedTuples.add(new QATestUtils.SrvcAttrTuple
                                                       (srvcItems,tmplAttrs,
                                                        tuple.getSrvcObj(),
                                                        tuple.getAttrObj(),
                                                        srvcEvnt.getTransition()));
                } catch (ClassNotFoundException e) {
                    logger.log(Level.INFO, "Unexpected exception", e);
                } catch (IOException e) {
                    logger.log(Level.INFO, "Unexpected exception", e);
                }
            }
        }
    }

    protected QATestUtils.SrvcAttrTuple[][][] oldState;
    protected QATestUtils.SrvcAttrTuple[][][] newState;
    protected List expectedTuples = new ArrayList(2850);
    protected List receivedTuples = new ArrayList(2850);
    protected List<ServiceEvent> evntVec = new ArrayList<ServiceEvent>(2850);

    private ServiceItem[] srvcItems;
    private ServiceItem[] srvcsForEquals;
    private ServiceTemplate[][] tmpl;
    private ServiceTemplate[][] sTmpl;
    private ServiceRegistrar proxy;

    private int nClasses = getNTestClasses();
    private int nAttrClasses = getNAttrClasses();
    private int nSrvcInstances;
    private int nSrvcsPerClass;

    private int totalNSrvcs;
    private int srvcChainLen = MAX_SRVC_CHAIN_LEN;

    private Entry[] attrEntries;
    private Entry[][] tmplAttrs;
    private Entry[] srvcAttrs;

    private long durationForSrvcLease = 3*QATestUtils.N_MS_PER_MIN;
    private long maxNMsToWaitForEvents;

    /** The event handler for the services registered by this class */
    private RemoteEventListener listener;

    /** Performs actions necessary to prepare for execution of the 
     *  current QA test.
     *
     *  Creates the lookup service. Creates the event handler. Retrieves the 
     *  proxies to the lookup service. Computes the maximimum time to wait 
     *  for all events to arrive from lookup; based on that event wait time, 
     *  computes and sets the value for the maximum duration of all event
     *  leases. The maximum duration should be large enough to allow all 
     *  events to arrive and be processed before any of the event leases
     *  expire. Sets the service lease duration to an appropriately short
     *  enough value. Loads each of the attribute classes and creates one 
     *  set of non-initialized instances (null fields) of the loaded 
     *  attributes. Loads and instantiates each of the service classes with 
     *  each of the attribute instances; then registers each service 
     *  instance with the lookup service. Creates another set of instances 
     *  of the service classes that will be used to determine equality 
     *  in the method objEquals() from the class QATestUtils.SrvcAttrTuple. 
     *  For each combination of service and attribute, creates a template 
     *  containing a combination of both the service's class type and the 
     *  attribute instance. Registers an event notification request based 
     *  on the contents of that template and the appropriate transition mask;
     *  requests a handback which contains the associated service/attribute 
     *  "tuple" to be associated with the event.
     *  @exception QATestException will usually indicate an "unresolved"
     *  condition because at this point the test has not yet begun.
     */
    public synchronized Test construct(QAConfig sysConfig) throws Exception {
        int i,j,k;
        int transitionMask =   ServiceRegistrar.TRANSITION_MATCH_NOMATCH
                             | ServiceRegistrar.TRANSITION_NOMATCH_MATCH
	                     | ServiceRegistrar.TRANSITION_MATCH_MATCH;

        /* compute the maximum the number of milliseconds to wait for all of
         * the events to arrive from lookup
         */
        nSrvcsPerClass = sysConfig.getIntConfigVal
            ("org.apache.river.test.spec.lookupservice.nInstancesPerClass",10);
        nSrvcInstances = TEST_SRVC_CLASSES.length * nSrvcsPerClass;
        /* based on the number of service class instances and the number of
         * attributes per service participating in this test run, get
         * the number of milliseconds to wait for all events to arrive
         * from the lookup service before attempting to verify the events.
         */
        maxNMsToWaitForEvents = QATestUtils.getMaxNMsToWaitForEvents
                                    (nSrvcInstances,nAttrClasses,SHOW_TIMINGS);
        /* based on the event wait time computed above, compute and set 
         * values for the maximum duration of all event leases; and a
         * resonably short value for all service leases
         */
        QATestUtils.setLeaseDuration(sysConfig,durationForSrvcLease,
                                     maxNMsToWaitForEvents);

        /* create the lookup service */
	super.construct(sysConfig);
        /* create the event handler */
	listener = new Listener();
        ((BasicListener) listener).export();
        /* retrieve the proxies to the lookup service */
	proxy = super.getProxy();
        /* load each of the attribute classes and create a non-initialized
         * instance (null fields) of each loaded class
         */
        attrEntries = super.createNullFieldAttributes();
        tmplAttrs = new Entry[attrEntries.length][1];
        srvcAttrs = new Entry[attrEntries.length];

        for (i=0;i<attrEntries.length;i++) {
	    tmplAttrs[i][0] = attrEntries[i];
	}
        srvcItems   = new ServiceItem[nClasses*nSrvcsPerClass];
        totalNSrvcs = srvcItems.length; 
        for (i=0;i<attrEntries.length;i++) {
	    srvcAttrs[i] = attrEntries[i];
	}
        /* load and instantiate each of the service classes with each of the
         * attribute instances; then register each service instance with 
         * the lookup service
         */
        for (i=0,k=0;i<nClasses;i++) {
            for (j=0;j<nSrvcsPerClass;j++) {
                srvcItems[k] = super.createServiceItem(TEST_SRVC_CLASSES[i],k,
                                                       srvcAttrs);
		proxy.register(srvcItems[k],Long.MAX_VALUE);
                k++;
            }
        }
        /* create another set of instances of the service classes that will
         * be used to determine equality in the method objEquals() in the
         * class QATestUtils.SrvcAttrTuple
         */
        srvcsForEquals = createSrvcsForEquals(nClasses,totalNSrvcs);
        /* for each combination of service and attribute, create a template
         * containing both the service's class type and the attribute
         * instance from the set of attributes created above, then register
         * an event notification request based on the contents of that
         * template and the appropriate transition mask; request a handback
         * which contains the associated service/attribute "tuple"
         */
	tmpl = new ServiceTemplate[nClasses][nAttrClasses];
	sTmpl = new ServiceTemplate[nClasses][nAttrClasses];
        for (i=0;i<nClasses;i++) {
	    Class c = Class.forName(TEST_SRVC_CLASSES[i]);
	    Class[] classType = {c};

	    /* XXXX need to make this code more general; loop thru
                 *      the super classes populating the sClassType[] array
		 */
	    Class sc = c.getSuperclass();
	    Class[] sClassType;
	    if ( sc.equals(Object.class) ){
		sClassType = new Class[1];
		sClassType[0] = c;
	    } else {
		sClassType = new Class[srvcChainLen];
		sClassType[0] = c;
		sClassType[1] = sc;
	    }
	    /* XXXX end */

	    for (j=0;j<nAttrClasses;j++) {
		tmpl[i][j] = new ServiceTemplate(null,classType,
						 tmplAttrs[j]);
		sTmpl[i][j] = new ServiceTemplate(null,sClassType,
						  tmplAttrs[j]);
		proxy.notiFy(tmpl[i][j],
			     transitionMask,
			     listener,
			     new AtomicMarshalledInstance
				 (new QATestUtils.SrvcAttrTuple
				     (srvcItems[nSrvcsPerClass*i].service,
				      tmplAttrs[j][0])),
			     Long.MAX_VALUE);
	    }
	}
        return this;
    }

    /** Executes the current QA test.
     *
     *  Creates and initializes the appropriate state tuple arrays. Retrieves
     *  the expected event information corresponding to each service and 
     *  attribute instance. Populates the "accumulation" vector with that 
     *  information. Waits for all expected events to arrive after each of
     *  the service leases expires. Verifies that the events expected are 
     *  actually sent by the lookup service
     *  @exception QATestException usually indicates test failure
     */
    public synchronized void run() throws Exception {
	int i=0;
	int j=0;
	oldState =
    new QATestUtils.SrvcAttrTuple[totalNSrvcs][srvcAttrs.length][srvcChainLen];
	newState =
    new QATestUtils.SrvcAttrTuple[totalNSrvcs][srvcAttrs.length][srvcChainLen];
	for (i=0;i<totalNSrvcs;i++) {
	    for (j=0;j<srvcAttrs.length;j++) {
	        QATestUtils.initStateTupleArray(i,j,srvcChainLen,
	                                        srvcItems,srvcsForEquals,
	                                        tmplAttrs,oldState);
	        QATestUtils.initStateTupleArray(i,j,srvcChainLen,
	                                        srvcItems,srvcsForEquals,
	                                        tmplAttrs,newState);
	    }
	    setStateAttrInfo(i,tmplAttrs,newState);
	    QATestUtils.setExpectedEvents(srvcItems[i].service,
	                                  srvcItems,srvcsForEquals,
	                                  tmplAttrs,nSrvcsPerClass,
	                                  sTmpl,oldState,newState,
	                                  expectedTuples);
	}
	QATestUtils.verifyEventTuples
	  (receivedTuples,expectedTuples,maxNMsToWaitForEvents,SHOW_TIMINGS, this);
        Collections.sort(evntVec, new RemoteEventComparator());
	QATestUtils.verifyEventItems(evntVec);
    }

    /** Performs cleanup actions necessary to achieve a graceful exit of 
     *  the current QA test.
     *
     *  Unexports the listener and then performs any remaining standard
     *  cleanup duties.
     *  @exception QATestException will usually indicate an "unresolved"
     *  condition because at this point the test has completed.
     */
    public synchronized void tearDown() {
	try {
	    unexportListener(listener, true);
	} finally {
	    super.tearDown();
	}
    }

    /* Convenience method so setting the info doesn't have to be done inline */
    private void setStateAttrInfo(int srvcIndx,
                                  Entry[][] attrs,
                                  QATestUtils.SrvcAttrTuple[][][] state)
                                                        throws Exception
    {
        for(int i=0;i<state[srvcIndx].length;i++) {
            for(int j=0;j<state[srvcIndx][i].length;j++) {
                    state[srvcIndx][i][j]
                    = new QATestUtils.SrvcAttrTuple(srvcsForEquals,attrs,
                                                    null,null);
	    }
	}
    }
}