#org.apache.river.qa.harness.verifier=org.apache.river.qa.harness.SkipTestVerifier
include0=../servicediscovery_1.properties
testClass=LookupDropProxyTaskRace
testCategories=servicediscovery,servicediscovery_impl,servicediscoveryevent,implservicediscovery,implservicediscoveryevent
net.jini.lookup.nLookupServices=1
net.jini.lookup.nAddLookupServices=0
net.jini.lookup.nServices=1
# To override activatable and/or persistent version of
# the lookp service. Uncomment the following items.
#org.apache.river.qa.harness.runactivation=false
#net.jini.core.lookup.ServiceRegistrar.type=transient
