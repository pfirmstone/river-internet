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
// Stub class generated by rmic, do not edit.
// Contents subject to change without notice.

public final class NonExistentActivatable_Stub
    extends java.rmi.server.RemoteStub
    implements ActivateMe, java.rmi.Remote
{
    private static final java.rmi.server.Operation[] operations = {
	new java.rmi.server.Operation("void ping()"),
	new java.rmi.server.Operation("void shutdown()"),
	new java.rmi.server.Operation("void unregister()")
    };
    
    private static final long interfaceHash = 4395146122524413703L;
    
    private static final long serialVersionUID = 2;
    
    private static boolean useNewInvoke;
    private static java.lang.reflect.Method $method_ping_0;
    private static java.lang.reflect.Method $method_shutdown_1;
    private static java.lang.reflect.Method $method_unregister_2;
    
    static {
	try {
	    java.rmi.server.RemoteRef.class.getMethod("invoke",
		new java.lang.Class[] {
		    java.rmi.Remote.class,
		    java.lang.reflect.Method.class,
		    java.lang.Object[].class,
		    long.class
		});
	    useNewInvoke = true;
	    $method_ping_0 = ActivateMe.class.getMethod("ping", new java.lang.Class[] {});
	    $method_shutdown_1 = ActivateMe.class.getMethod("shutdown", new java.lang.Class[] {});
	    $method_unregister_2 = ActivateMe.class.getMethod("unregister", new java.lang.Class[] {});
	} catch (java.lang.NoSuchMethodException e) {
	    useNewInvoke = false;
	}
    }
    
    // constructors
    public NonExistentActivatable_Stub() {
	super();
    }
    public NonExistentActivatable_Stub(java.rmi.server.RemoteRef ref) {
	super(ref);
    }
    
    // methods from remote interfaces
    
    // implementation of ping()
    public void ping()
	throws java.rmi.RemoteException
    {
	try {
	    if (useNewInvoke) {
		ref.invoke(this, $method_ping_0, null, 5866401369815527589L);
	    } else {
		java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 0, interfaceHash);
		ref.invoke(call);
		ref.done(call);
	    }
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of shutdown()
    public void shutdown()
	throws java.lang.Exception
    {
	if (useNewInvoke) {
	    ref.invoke(this, $method_shutdown_1, null, -7207851917985848402L);
	} else {
	    java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 1, interfaceHash);
	    ref.invoke(call);
	    ref.done(call);
	}
    }
    
    // implementation of unregister()
    public void unregister()
	throws java.lang.Exception
    {
	if (useNewInvoke) {
	    ref.invoke(this, $method_unregister_2, null, -5366864281862648102L);
	} else {
	    java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 2, interfaceHash);
	    ref.invoke(call);
	    ref.done(call);
	}
    }
}
