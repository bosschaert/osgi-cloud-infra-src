package org.coderthoughts.cloud.framework.service.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import org.coderthoughts.cloud.framework.service.api.CloudConstants;
import org.coderthoughts.cloud.framework.service.api.FrameworkNodeAddition;
import org.coderthoughts.cloud.framework.service.api.FrameworkNodeStatus;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {
    private static final String OSGI_FRAMEWORK_UUID = "org.osgi.framework.uuid";
    private ServiceTracker frameworkStatusAdditionServiceTracker;

    @Override
    public void start(BundleContext context) throws Exception {
        String publicDNS = System.getenv("OPENSHIFT_GEAR_DNS");
        if (publicDNS == null) {
            throw new Exception("Environment variable OPENSHIFT_GEAR_DNS is not set. It should be set to the public DNS name of the current instance.");
        }

        String uuid = context.getProperty(OSGI_FRAMEWORK_UUID);
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            System.setProperty(OSGI_FRAMEWORK_UUID, uuid);
        }

        Hashtable<String, Object> props = new Hashtable<String, Object>();
        props.put(OSGI_FRAMEWORK_UUID, uuid);
        props.put("org.osgi.node.host", publicDNS);
        props.put("org.osgi.node.host.internal", System.getenv("OPENSHIFT_INTERNAL_IP"));
        props.put("org.osgi.node.type", "Red Hat Openshift/DIY/OSGi"); // TODO obtain from underlying cloud
        props.put("org.osgi.node.version", "0.1"); // TODO obtain from underlying cloud
        props.put("org.osgi.node.country", "USA"); // TODO obtain from underlying platform
        props.put("org.osgi.node.location", "US-KS"); // TODO obtain from underlying platform
        props.put(Constants.FRAMEWORK_VERSION, context.getProperty(Constants.FRAMEWORK_VERSION));
        props.put(Constants.FRAMEWORK_PROCESSOR, context.getProperty(Constants.FRAMEWORK_PROCESSOR));
        props.put(Constants.FRAMEWORK_OS_NAME, context.getProperty(Constants.FRAMEWORK_OS_NAME));
        props.put(Constants.FRAMEWORK_OS_VERSION, context.getProperty(Constants.FRAMEWORK_OS_VERSION));
        props.put("java.version", System.getProperty("java.version"));
        props.put("java.runtime.version", System.getProperty("java.runtime.version"));
        props.put("java.vm.vendor", System.getProperty("java.vm.vendor"));
        props.put("java.vm.version", System.getProperty("java.vm.version"));
        props.put("java.vm.name", System.getProperty("java.vm.name"));
        props.put("service.exported.interfaces", "*");
        props.put("service.exported.configs", new String [] {CloudConstants.CLOUD_CONFIGURATION_TYPE, "<<nodefault>>"});
        // props.put("service.exported.type", FrameworkNodeStatus.class);
        props.put("service.exported.handler", new RemoteOSGiFrameworkServiceInvocationHandler(context));

        final ServiceRegistration reg = context.registerService(FrameworkNodeStatus.class.getName(),
                new FrameworkNodeStatusImpl(new LocalClientInfo(context), context), props);
        /*
        final RemoteOSGiFrameworkFactoryService fs = new RemoteOSGiFrameworkFactoryService(context);
        final ServiceRegistration remReg = context.registerService(RemoteServiceFactory.class.getName(), fs, props);

        // TODO is this really what we want, how about updates to its properties?
        final ServiceRegistration localReg = context.registerService(FrameworkNodeStatus.class.getName(),
                new FrameworkNodeStatusImpl(new LocalClientInfo(context), context), props);
        */

        Hashtable<String, String> fnaProps = new Hashtable<String, String>();
        fnaProps.put(FrameworkNodeAddition.ADD_VARIABLES_KEY, FrameworkNodeStatus.FV_AVAILABLE_MEMORY);
        context.registerService(FrameworkNodeAddition.class.getName(), new FrameworkNodeAdditionImpl(), fnaProps);

        frameworkStatusAdditionServiceTracker = new ServiceTracker(context, FrameworkNodeAddition.class.getName(), null) {
            @Override
            public Object addingService(ServiceReference reference) {
                addStatusProperties(reg, reference);
//                addStatusProperties(remReg, reference);
//                addStatusProperties(localReg, reference);
                return super.addingService(reference);
            }

            @Override
            public void modifiedService(ServiceReference reference, Object service) {
                // TODO Auto-generated method stub
                super.modifiedService(reference, service);
            }

            @Override
            public void removedService(ServiceReference reference, Object service) {
                // TODO Auto-generated method stub
                super.removedService(reference, service);
            }
        };
        frameworkStatusAdditionServiceTracker.open();
    }

    private static Dictionary<String, Object> getCurProperties(ServiceRegistration reg) {
        Dictionary<String, Object> dict = new Hashtable<String, Object>();

        for (String key : reg.getReference().getPropertyKeys()) {
            dict.put(key, reg.getReference().getProperty(key));
        }

        return dict;
    }

    @SuppressWarnings("unchecked")
    static Collection<String> getStringPlusProperty(Object property) {
        if (property instanceof String) {
            return Collections.singleton((String) property);
        }

        if (property instanceof String[]) {
            return Arrays.asList((String[]) property);
        }

        if (property instanceof Collection<?>) {
            return (Collection<String>) property;
        }

        return Collections.emptyList();
    }


    @Override
    public void stop(BundleContext context) throws Exception {
        frameworkStatusAdditionServiceTracker.close();
    }

    private void addStatusProperties(final ServiceRegistration sreg, ServiceReference reference) {
        Dictionary<String, Object> dict = getCurProperties(sreg);
        List<String> dictKeys = Collections.list(dict.keys());
        for (String key : getStringPlusProperty(reference.getProperty(FrameworkNodeAddition.ADD_PROPERTIES_KEY))) {
            if (!dictKeys.contains(key)) {
                dict.put(key, reference.getProperty(key));
            }
        }

        sreg.setProperties(dict);
    }
}
