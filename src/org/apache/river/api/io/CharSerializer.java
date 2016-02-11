/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.river.api.io;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;

/**
 *
 * @author peter
 */
@AtomicExternal
class CharSerializer implements Externalizable {
    private final static long serialVersionUID = 1L;
    
    private final char c;
    
    CharSerializer(char c){
	this.c = c;
    }
    
    public CharSerializer(ObjectInput in) throws IOException{
	this(in.readChar());
    }
    
    Character readResolve() throws ObjectStreamException {
	return c;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
	out.writeChar(c);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public boolean equals(Object o){
	if (o == this) return true;
	if (!(o instanceof CharSerializer)) return false;
	CharSerializer that = (CharSerializer) o;
	return that.c == c;
    }

    @Override
    public int hashCode() {
	int hash = 3;
	hash = 53 * hash + this.c;
	return hash;
    }
}
