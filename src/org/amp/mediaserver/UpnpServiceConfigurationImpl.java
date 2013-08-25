package org.amp.mediaserver;

import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.binding.xml.DeviceDescriptorBinder;
import org.fourthline.cling.binding.xml.RecoveringUDA10DeviceDescriptorBinderImpl;
import org.fourthline.cling.binding.xml.ServiceDescriptorBinder;
import org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.transport.impl.RecoveringGENAEventProcessorImpl;
import org.fourthline.cling.transport.impl.RecoveringSOAPActionProcessorImpl;
import org.fourthline.cling.transport.impl.apache.StreamClientConfigurationImpl;
import org.fourthline.cling.transport.impl.apache.StreamClientImpl;
import org.fourthline.cling.transport.impl.apache.StreamServerConfigurationImpl;
import org.fourthline.cling.transport.impl.apache.StreamServerImpl;
import org.fourthline.cling.transport.spi.GENAEventProcessor;
import org.fourthline.cling.transport.spi.NetworkAddressFactory;
import org.fourthline.cling.transport.spi.SOAPActionProcessor;
import org.fourthline.cling.transport.spi.StreamClient;
import org.fourthline.cling.transport.spi.StreamServer;

/**
 * 
 */
public class UpnpServiceConfigurationImpl extends DefaultUpnpServiceConfiguration {

	// final private static Logger log = Logger.getLogger(UpnpServiceConfigurationImpl.class.getName());
			
	/////////////////////////////////////////////////////////////////////////////////
	// 
	/////////////////////////////////////////////////////////////////////////////////	
	protected SOAPActionProcessor createSOAPActionProcessor() {		
		return new RecoveringSOAPActionProcessorImpl();
	}

	protected GENAEventProcessor createGENAEventProcessor() {
		return new RecoveringGENAEventProcessorImpl();
	}

	/////////////////////////////////////////////////////////////////////////////////
	// 
	/////////////////////////////////////////////////////////////////////////////////
	protected DeviceDescriptorBinder createDeviceDescriptorBinderUDA10() {
		return new RecoveringUDA10DeviceDescriptorBinderImpl();
	}

	protected ServiceDescriptorBinder createServiceDescriptorBinderUDA10() {
		return new UDA10ServiceDescriptorBinderSAXImpl();
	}

	
	/////////////////////////////////////////////////////////////////////////////////
	//  
	/////////////////////////////////////////////////////////////////////////////////
	@Override
	public StreamClient<?> createStreamClient() {	
		return new StreamClientImpl( new StreamClientConfigurationImpl(getSyncProtocolExecutorService()) );		
	}
	
	@Override
	public StreamServer<?> createStreamServer(NetworkAddressFactory networkAddressFactory) {	
		return new StreamServerImpl( new StreamServerConfigurationImpl(networkAddressFactory.getStreamListenPort()) );
	}

	/////////////////////////////////////////////////////////////////////////////////
	//  Cling 2.0 alpha2, section 5.4.3 on configuring discovery.
	/////////////////////////////////////////////////////////////////////////////////
	@Override
	public ServiceType[] getExclusiveServiceTypes() {
		return null;
	}
	
}