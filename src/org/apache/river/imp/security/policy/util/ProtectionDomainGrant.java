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

package org.apache.river.imp.security.policy.util;

import java.lang.ref.WeakReference;
import java.security.CodeSource;
import java.security.Permission;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Peter Firmstone
 */
class ProtectionDomainGrant extends PrincipalGrant implements PermissionGrant {
    private final boolean hasDomain;
    private final WeakReference<ProtectionDomain> domain;
    private final Collection<Permission> permissions;
    
    @SuppressWarnings("unchecked")
    ProtectionDomainGrant(WeakReference<ProtectionDomain> domain, Principal[] groups, 
            Permission[] perm){
        super(groups);
        if (domain == null){
            hasDomain = false;
        }else{
            hasDomain = true;
        }
        this.domain = domain;
        if (perm == null || perm.length == 0) {
            this.permissions = Collections.EMPTY_LIST;
        }else{
            this.permissions = new HashSet<Permission>(perm.length);
            this.permissions.addAll(Arrays.asList(perm));
        }
    }
    
    @Override
    public boolean equals(Object o){
        if (o == null) return false;
        if (o == this) return true;
        if (o instanceof ProtectionDomainGrant){
            ProtectionDomainGrant c = (ProtectionDomainGrant) o;
            if ( !super.equals(o)) return false;
            if (domain.equals(c.domain) && permissions.equals(c.permissions) 
                    && hasDomain == c.hasDomain) return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + (this.hasDomain ? 1 : 0);
        hash = 13 * hash + (this.domain != null ? this.domain.hashCode() : 0);
        hash = 13 * hash + (this.permissions != null ? this.permissions.hashCode() : 0);
        hash = 13 * hash + super.hashCode();
        return hash;
    }
    
    /*
     * Checks if passed ProtectionDomain matches this PolicyEntry. Null ProtectionDomain of
     * PolicyEntry implies any ProtectionDomain; non-null ProtectionDomain's are
     * compared with equals();
     */   
    // for grant
    public boolean implies(ProtectionDomain pd) {
        // ProtectionDomain comparison
        if (hasDomain == false) return true;
        if (pd == null) return false;       
        if (domain.get() == null ) return false; // hasDomain already true
        return pd.equals(domain.get()); // pd not null
    }

    // This is only here for revoke.
    protected boolean impliesClassLoader(ClassLoader cl) {
        if (hasDomain == false) return true;
        if (cl == null) return false;       
        if (domain.get() == null ) return false; // hasDomain already true
        return domain.get().getClassLoader().equals(cl); // pd not null
    }
    // This is only here for revoke.
    protected boolean impliesCodeSource(CodeSource codeSource) {
        ProtectionDomain pd = domain.get();
        if (pd == null) return true;
        CodeSource cs = normalizeCodeSource(pd.getCodeSource());
        if (cs == null) return true;
        if (codeSource == null) return false;       
        return cs.implies(normalizeCodeSource(codeSource));
    }
    
    protected boolean impliesCertificates(Certificate[] signers){
        ProtectionDomain pd = domain.get();
        if (pd == null) return true;
        List<Certificate> certs = Arrays.asList(pd.getCodeSource().getCertificates());
        if ( certs.isEmpty()) return true;
        if ( signers == null || signers.length == 0 ) return false;
        List<Certificate> certificates = Arrays.asList(signers);
        return certificates.containsAll(certs);    
    }

    public Collection<Permission> getPermissions() {
        return Collections.unmodifiableCollection(permissions);
    }

    public boolean isVoid() {        
        if (permissions.size() == 0 ) return true;
        if (hasDomain == true && domain.get() == null) return true;
        return false;
    }

    public PermissionGrantBuilder getBuilderTemplate() {
        PermissionGrantBuilder pgb = super.getBuilderTemplate();
        return pgb
                .domain(domain)
                .permissions(permissions.toArray(new Permission[permissions.size()]))
                .context(PermissionGrantBuilder.PROTECTIONDOMAIN);
    }
  
}