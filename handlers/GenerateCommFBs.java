/**
* @author  sieffert
*/
package org.eclipse.fordiac.ide.application.handlers;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.fordiac.ide.application.editors.FBNetworkEditor;
import org.eclipse.fordiac.ide.application.utilities.MuxDemuxGenerator;
import org.eclipse.fordiac.ide.model.libraryElement.AdapterConnection;
import org.eclipse.fordiac.ide.model.libraryElement.AdapterType;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The Class GenerateCommFBs.
 */
public class GenerateCommFBs extends AbstractHandler {
	/**
	 * Generates Communication FBs for OPC UA Communication Protocol
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		System.out.println("Start Of Excecution");
		String communicationProtocol = "OpcUa";

		for (final AdapterConnection adapterConnection : getAllDestributedAdapterConnections(event)) {
			MuxDemuxGenerator gen = new MuxDemuxGenerator(event, communicationProtocol);
			gen.generateMuxDemux(adapterConnection);
			gen.generateCommunicationFBs();
		}
		
		System.out.println("End Of Excecution");
		return Status.OK_STATUS;
	}

	private void generateMuxDemux(final ExecutionEvent event, final String communicationProtocol) {
		final List<AdapterConnection> adapterConnectionList = getAllDestributedAdapterConnections(event);
		
		for (final AdapterConnection adapterConnection : adapterConnectionList) {
			//MuxDemuxGenerator.generate(event, adapterConnection, communicationProtocol);
			//MuxDemuxGenerator.generateCommunicationFBs();
		}
	}
	


	private List<AdapterConnection> getAllDestributedAdapterConnections(final ExecutionEvent event) {
		final FBNetworkEditor fbEditor = (FBNetworkEditor) HandlerUtil.getActiveEditor(event);
		final List<AdapterConnection> allAdapterConnections = fbEditor.getSystem()
				.getApplicationNamed(fbEditor.getPartName()).getFBNetwork()
				.getAdapterConnections();

		final List<AdapterConnection> allDestributedAdapterConnections = new ArrayList<>();

		for (final AdapterConnection adapterConnection : allAdapterConnections) {
			if(isDistributedConnection(adapterConnection)) {
				allDestributedAdapterConnections.add(adapterConnection);
			}
		}

		return allDestributedAdapterConnections;
	}

	private boolean isDistributedConnection(final AdapterConnection adapterConnection) {
		final boolean onSameDevice = adapterConnection.getDestination().getFBNetworkElement().getResource().getDevice()
				.equals(adapterConnection.getSource().getFBNetworkElement().getResource().getDevice());
		return !onSameDevice;
	}


	//-------------------- Debug --------------------
	private void understandAdapterConnections(final ExecutionEvent event) {
		final List<AdapterConnection> adps = getAllDestributedAdapterConnections(event);
		System.out.println("Number of Adapter Connections: " + adps.size());

		for (final AdapterConnection adapterConnection : adps) {

			// general
			System.out.println("---General---");
			System.out.println("Adapter Name: " + adapterConnection.getName());
			//System.out.println("Adapter Type Name: " + adapterConnection);
			System.out.println("Adapter Destination Name: " + adapterConnection.getDestination().getName());
			System.out.println("Adapter Source Name: " + adapterConnection.getSource().getName());

			// Adapter Type Source/ Destination (must be same and can be checked)
			System.out.println("---AdapterTypes---");
			System.out.println("Adapter Destination Type Name: " + adapterConnection.getDestination().getTypeName());
			System.out.println("Adapter Source Type Name: " + adapterConnection.getSource().getTypeName());

			// FBNetworkElementName
			System.out.println("---FBNetworkElementName---");
			System.out.println("Adapter Destination FBNetworkElement Name: "
					+ adapterConnection.getDestination().getFBNetworkElement().getName());
			System.out.println("Adapter Source FBNetworkElement Name: "
					+ adapterConnection.getSource().getFBNetworkElement().getName());

			// Mapping between FBs
			System.out.println("---Mapping---");
			System.out.println("Adapter Destination FBNetworkElement Mapping To Name: "
					+ adapterConnection.getDestination().getFBNetworkElement().getMapping().getTo().getName());
			System.out.println("Adapter Destination FBNetworkElement Mapping From Name: "
					+ adapterConnection.getDestination().getFBNetworkElement().getMapping().getFrom().getName());
			System.out.println("Adapter Source FBNetworkElement Mapping To Name: "
					+ adapterConnection.getSource().getFBNetworkElement().getMapping().getTo().getName());
			System.out.println("Adapter Source FBNetworkElement Mapping From Name: "
					+ adapterConnection.getSource().getFBNetworkElement().getMapping().getFrom().getName());

			// Resource
			System.out.println("---Resource---");
			System.out.println("Adapter Destination FBNetworkElement Resource Name: "
					+ adapterConnection.getDestination().getFBNetworkElement().getResource().getName());
			System.out.println("Adapter Destination FBNetworkElement Resource Type Name: "
					+ adapterConnection.getDestination().getFBNetworkElement().getResource().getTypeName());
			System.out.println("Adapter Source FBNetworkElement Resource Name: "
					+ adapterConnection.getSource().getFBNetworkElement().getResource().getName());
			System.out.println("Adapter Source FBNetworkElement Resource Type Name: "
					+ adapterConnection.getSource().getFBNetworkElement().getResource().getTypeName());

			// Device
			System.out.println("---Device---");
			System.out.println("Adapter Destination FBNetworkElement Resource Device Name: "
					+ adapterConnection.getDestination().getFBNetworkElement().getResource().getDevice().getName());
			System.out.println("Adapter Destination FBNetworkElement Resource Device Type Name: "
					+ adapterConnection.getDestination().getFBNetworkElement().getResource().getDevice().getTypeName());
			System.out.println("Adapter Source FBNetworkElement Resource Device Name: "
					+ adapterConnection.getSource().getFBNetworkElement().getResource().getDevice().getName());
			System.out.println("Adapter Source FBNetworkElement Resource Device Type Name: "
					+ adapterConnection.getSource().getFBNetworkElement().getResource().getDevice().getTypeName());

			// AdapterConnection Device Comparison (is the adapter connection distributed
			// over the network?)
			System.out.println("---Device Comparison---");
			System.out.println("Same Device Compatison works: " + (true == adapterConnection.getDestination()
					.getFBNetworkElement().getResource().getDevice()
					.equals(adapterConnection.getDestination().getFBNetworkElement().getResource().getDevice())));
			System.out.println("Different Device Compatison works: "
					+ (false == adapterConnection.getDestination().getFBNetworkElement().getResource().getDevice()
					.equals(adapterConnection.getSource().getFBNetworkElement().getResource().getDevice())));

			// Adapter Event Inputs
			final AdapterType adapterFBDestination = (AdapterType) adapterConnection.getDestination().getType();
			final AdapterType adapterFBSource = (AdapterType) adapterConnection.getSource().getType();
			System.out.println("--- --- Adapter --- ---");
			System.out.println("---Adapter Events---");
			System.out.println("Destination Adapter - Number of Event Inputs: " + adapterFBDestination.getInterfaceList().getEventInputs().size());
			System.out.println("Destination Adapter - Number of Event Outputs: " + adapterFBDestination.getInterfaceList().getEventOutputs().size());
			System.out.println("Source Adapter - Number of Event Inputs: " + adapterFBSource.getInterfaceList().getEventInputs().size());
			System.out.println("Source Adapter - Number of Event Outputs: " + adapterFBSource.getInterfaceList().getEventOutputs().size());

			System.out.println("---Adapter Plugs/Sockets---");
			System.out.println("Destination Adapter - Number of Plugs: " + adapterFBDestination.getInterfaceList().getPlugs().size());
			System.out.println("Destination Adapter - Number of Sockets: " + adapterFBDestination.getInterfaceList().getSockets().size());
			System.out.println("Source Adapter - Number of Plugs: " + adapterFBSource.getInterfaceList().getPlugs().size());
			System.out.println("Source Adapter - Number of Sockets: " + adapterFBSource.getInterfaceList().getSockets().size());

			System.out.println("---Adapter Data---");
			System.out.println("Destination Adapter - Number of DataInput: " + adapterFBDestination.getInterfaceList().getInputVars().size());
			System.out.println("Destination Adapter - Number of DataOutput: " + adapterFBDestination.getInterfaceList().getOutputVars().size());
			System.out.println("Source Adapter - Number of DataInput: " + adapterFBSource.getInterfaceList().getInputVars().size());
			System.out.println("Source Adapter - Number of DataOutput: " + adapterFBSource.getInterfaceList().getOutputVars().size());


			System.out.print('\n');
			System.out.print('\n');
		}
	}
}


