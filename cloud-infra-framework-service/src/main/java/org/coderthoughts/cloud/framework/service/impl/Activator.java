package org.coderthoughts.cloud.framework.service.impl;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.UUID;

import org.coderthoughts.cloud.framework.service.api.FrameworkStatus;
import org.coderthoughts.cloud.framework.service.api.FrameworkStatusAddition;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.monitor.MonitorAdmin;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {
    private static final String OSGI_FRAMEWORK_UUID = "org.osgi.framework.uuid";
    private ServiceTracker monitorAdminServiceTracker, frameworkStatusAdditionServiceTracker;
    private ServiceRegistration reg;

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

        monitorAdminServiceTracker = new ServiceTracker(context, MonitorAdmin.class.getName(), null);
        monitorAdminServiceTracker.open();

        Hashtable<String, Object> props = new Hashtable<String, Object>();
        props.put(OSGI_FRAMEWORK_UUID, uuid);
        props.put("org.coderthoughts.framework.ip", publicDNS);
        props.put("org.coderthoughts.cloud.name", "Red Hat Openshift/DIY/OSGi"); // TODO obtain from underlying cloud
        props.put("org.coderthoughts.cloud.version", "0.9"); // TODO obtain from underlying cloud
        props.put("org.coderthoughts.cloud.location", "USA"); // TODO obtain from underlying platform
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
        props.put("service.exported.configs", new String [] {"org.coderthoughts.configtype.cloud", "<<nodefault>>"});
        props.put("service.exported.type", FrameworkStatus.class);

        /*
        RemoteServiceFactory svcFactory = new OSGiFrameworkServiceFactory(monitorAdminServiceTracker);
        props.put("org.coderthoughts.remote.service.factory", svcFactory);
        OSGiFramework fwkService = new OSGiFrameworkImpl("localhost", monitorAdminServiceTracker);
        reg = context.registerService(OSGiFramework.class.getName(), fwkService, props);
        */

//        // context.registerService(OSGiFramework.class.getName(), service, properties)
        RemoteOSGiFrameworkFactoryService fs = new RemoteOSGiFrameworkFactoryService(context, monitorAdminServiceTracker);

        reg = context.registerService(RemoteOSGiFrameworkFactoryService.class.getName(), fs, props);

        ServiceTracker frameworkStatusAdditionServiceTracker = new ServiceTracker(context, FrameworkStatusAddition.class.getName(), null) {
            @Override
            public Object addingService(ServiceReference reference) {
                Object svc = super.addingService(reference);
                if (svc instanceof FrameworkStatusAddition) {
                    FrameworkStatusAddition fsa = (FrameworkStatusAddition) svc;
                    Dictionary<String, Object> dict = getCurProperties(reg);
                    for (String key : fsa.getAdditionalPropertyKeys()) {
                        dict.put(key, fsa.getAdditionalProperty(key));
                    }
                    reg.setProperties(dict);
                }
                return svc;
            }
        };
        frameworkStatusAdditionServiceTracker.open();

//        FrameworkMetadataPublisher publisher = new FrameworkMetadataPublisherImpl(reg);
//        context.registerService(FrameworkMetadataPublisher.class.getName(), publisher, null);
    }

    private Dictionary<String, Object> getCurProperties(ServiceRegistration reg) {
        Dictionary<String, Object> dict = new Hashtable<String, Object>();

        for (String key : reg.getReference().getPropertyKeys()) {
            dict.put(key, reg.getReference().getProperty(key));
        }

        return dict;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // reg.unregister();
        monitorAdminServiceTracker.close();
        frameworkStatusAdditionServiceTracker.close();
    }
}
