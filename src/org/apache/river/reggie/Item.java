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
package org.apache.river.reggie;

import org.apache.river.action.GetBooleanAction;
import org.apache.river.logging.Levels;
import org.apache.river.proxy.MarshalledWrapper;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.rmi.MarshalException;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.security.Security;
import org.apache.river.api.io.AtomicSerial;
import org.apache.river.api.io.AtomicSerial.GetArg;

/**
 * An Item contains the fields of a ServiceItem packaged up for
 * transmission between client-side proxies and the registrar server.
 * Instances are never visible to clients, they are private to the
 * communication between the proxies and the server.
 * <p>
 * This class only has a bare minimum of methods, to minimize
 * the amount of code downloaded into clients.
 *
 * @author Sun Microsystems, Inc.
 *
 */
@AtomicSerial
final class Item implements Serializable, Cloneable {

    private static final long serialVersionUID = 2L;
    private static final ObjectStreamField[] serialPersistentFields = 
    { 
        /** @serialField ServiceItem.serviceID. */
        new ObjectStreamField("serviceID", ServiceID.class),
        /** @serialField The Class of ServiceItem.service converted to ServiceType. */
        new ObjectStreamField("serviceType", ServiceType.class),
	/** @serialField The codebase of the service object. */
        new ObjectStreamField("codebase", String.class),
	/** @serialField ServiceItem.service as a MarshalledWrapper. */
        new ObjectStreamField("service", MarshalledWrapper.class),
	/** @serialField ServiceItem.attributeSets converted to EntryReps. */
        new ObjectStreamField("attributeSets", EntryRep[].class)
    };

    /** Logger for Reggie. */
    private static final Logger logger = 
	Logger.getLogger("org.apache.river.reggie");
    /**
     * Flag to enable JRMP impl-to-stub replacement during marshalling of
     * service proxy.
     */
    private static final boolean enableImplToStubReplacement;
    static {
	Boolean b;
	try {
	    b = (Boolean) Security.doPrivileged(new GetBooleanAction(
		"org.apache.river.reggie.enableImplToStubReplacement"));
	} catch (SecurityException e) {
	    logger.log(Levels.HANDLED, "failed to read system property", e);
	    b = Boolean.FALSE;
	}
	enableImplToStubReplacement = b.booleanValue();
    }

    /**
     * ServiceItem.serviceID.
     *
     * @serial
     */
    private ServiceID serviceID; // mutated by RegistrarImpl
    /**
     * The Class of ServiceItem.service converted to ServiceType.
     *
     * @serial
     */
    final ServiceType serviceType;
    /**
     * The codebase of the service object.
     *
     * @serial
     */
    final String codebase;
    /**
     * ServiceItem.service as a MarshalledWrapper.
     *
     * @serial
     */
    final MarshalledWrapper service;
    /**
     * ServiceItem.attributeSets converted to EntryReps.
     *
     * @serial
     */
    private EntryRep[] attributeSets; // mutated by RegistrarImpl

    /**
     * List view of attributeSets
     */
//    final List<EntryRep> attribSets;
     
    /**
     * Checks all invariants are satisfied during de-serialization.
     * @param arg
     * @return
     * @throws IOException 
     * @throws ClassCastException
     * @throws NullPointerException
     */
    private static boolean check(GetArg arg) throws IOException{
	ServiceID serviceID = (ServiceID) arg.get("serviceID", null); // Throws ClassCastException
	// serviceID is assigned by Reggie if null.
	ServiceType serviceType = (ServiceType) arg.get("serviceType", null); // Throws ClassCastException
	// serviceType allowed to be null
	String codebase = (String) arg.get("codebase", null); // Throws ClassCastException
	// codebase allowed to be null
	MarshalledWrapper service = (MarshalledWrapper) arg.get("service", null); // Throws ClassCastException
	if (service == null) throw new NullPointerException();
	EntryRep[] attributeSets = (EntryRep[]) arg.get("attributeSets", null); // Throws ClassCastException
	// attributeSets can be null and can contain null
	return true;
    }
    
    /**
     * Failure is atomic, if any invariants aren't satisfied, construction fails
     * before an instance can be created.
     * @param arg
     * @throws IOException 
     */
    public Item(GetArg arg) throws IOException{
	this(arg, check(arg));
    };
    
    private Item(GetArg arg, boolean check) throws IOException{
	super(); // instance has been created here.
	serviceID = (ServiceID) arg.get("serviceID", null);
	serviceType = (ServiceType) arg.get("serviceType", null);
	codebase = (String) arg.get("codebase", null);
	service = (MarshalledWrapper) arg.get("service", null);
	EntryRep [] attributeSets = ((EntryRep[]) arg.get("attributeSets", null));
	if (attributeSets != null) attributeSets = attributeSets.clone();
	this.attributeSets = attributeSets;
//	attribSets = Arrays.asList(attributeSets);
    }

    /**
     * Converts a ServiceItem to an Item.  Any exception that results
     * is bundled up into a MarshalException.
     */
    public Item(ServiceItem item) throws RemoteException {
	Object svc = item.service;
	if (enableImplToStubReplacement && svc instanceof Remote) {
	    try {
		svc = RemoteObject.toStub((Remote) svc);
		if (logger.isLoggable(Level.FINER)) {
		    logger.log(Level.FINER, "replacing {0} with {1}",
			       new Object[]{ item.service, svc });
		}
	    } catch (NoSuchObjectException e) {
	    }
	}
	serviceID = item.serviceID;
	ServiceTypeBase stb = ClassMapper.toServiceTypeBase(svc.getClass());
	serviceType = stb.type;
	codebase = stb.codebase;
	try {
	    service = new MarshalledWrapper(svc);
	} catch (IOException e) {
	    throw new MarshalException("error marshalling arguments", e);
	}
	attributeSets = EntryRep.toEntryRep(item.attributeSets, true);
//	attribSets = Arrays.asList(attributeSets);
    }
    
    Item(ServiceID serviceID,
            ServiceType serviceType,
            String codebase,
            MarshalledWrapper service,
            EntryRep [] attrSets)
    {
        this.serviceID = serviceID;
        this.serviceType = serviceType;
        this.codebase = codebase;
        this.service = service;
        attributeSets = attrSets;
//	attribSets = Arrays.asList(attributeSets);
    }

    /**
     * Convert back to a ServiceItem.  If the service object cannot be
     * constructed, it is set to null.  If an Entry cannot be constructed,
     * it is set to null.  If a field of an Entry cannot be unmarshalled,
     * it is set to null.
     */
    public ServiceItem get() {
	Object obj = null;
	try {
	    obj = service.get();
	} catch (Throwable e) {
	    RegistrarProxy.handleException(e);
	}
	synchronized (this){
	return new ServiceItem(serviceID,
			       obj,
			       EntryRep.toEntry(attributeSets));
    }
    }

    /**
     * Deep clone.  This is really only needed in the server,
     * but it's very convenient to have here.
     */
    @Override
    public Object clone() {
	    EntryRep[] attrSets = (EntryRep[])attributeSets.clone();
	    for (int i = attrSets.length; --i >= 0; ) {
		attrSets[i] = (EntryRep)attrSets[i].clone();
	    }
	    return new Item(serviceID, serviceType, codebase, service, attrSets);
    }

    /**
     * Converts an ArrayList of Item to an array of ServiceItem.
     */
    public static ServiceItem[] toServiceItem(List reps)
    {
	ServiceItem[] items = null;
	if (reps != null) {
	    items = new ServiceItem[reps.size()];
	    for (int i = items.length; --i >= 0; ) {
		items[i] = ((Item)reps.get(i)).get();
	    }
	}
	return items;
    }
    
    private synchronized void writeObject(ObjectOutputStream out) throws IOException {
	out.defaultWriteObject();
}

    /**
     * @return the serviceID
     */
    public synchronized ServiceID getServiceID() {
	return serviceID;
    }

    /**
     * @param serviceID the serviceID to set
     */
    public synchronized void setServiceID(ServiceID serviceID) {
	this.serviceID = serviceID;
    }

    /**
     * @return the attributeSets
     */
    public synchronized EntryRep[] getAttributeSets() {
	return attributeSets == null ? null : attributeSets.clone();
    }

    /**
     * @param attributeSets the attributeSets to set
     */
    public synchronized void setAttributeSets(EntryRep[] attributeSets) {
	this.attributeSets = attributeSets;
    }
}