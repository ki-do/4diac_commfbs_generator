/**
* @author  sieffert
* @author  dorofeev
*/
package org.eclipse.fordiac.ide.application.utilities;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.emf.common.util.EList;
import org.eclipse.fordiac.ide.application.editors.FBNetworkEditor;
import org.eclipse.fordiac.ide.model.commands.change.ChangeValueCommand;
import org.eclipse.fordiac.ide.model.commands.create.AdapterConnectionCreateCommand;
import org.eclipse.fordiac.ide.model.libraryElement.AdapterConnection;
import org.eclipse.fordiac.ide.model.libraryElement.DataConnection;
import org.eclipse.fordiac.ide.model.libraryElement.Event;
import org.eclipse.fordiac.ide.model.libraryElement.EventConnection;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetworkElement;
import org.eclipse.fordiac.ide.model.libraryElement.VarDeclaration;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The Class OpcUaCommunicationFBSpecifics.
 */
public final class OpcUaCommunicationSpecifics implements CommunicationProtocolSpecifics {

	// Source adapter connection
	private AdapterConnection adapterConnection;
	// Event
	private ExecutionEvent event = null;
	// Connections
	private List<DataConnection> dataConnectionList = null;
	private List<EventConnection> eventConnectionList = null;
	private List<AdapterConnection> adapterConnectionList = null;
	// FBs DEMUX/ Consumer
	private FBNetworkElement demux = null;
	private List<FBNetworkElement> demuxClientList = null;
	private List<FBNetworkElement> demuxSubscriberList = null;
	private List<EventConnection> demuxInitializationConnectionList = null;
	// FBs MUX/ Producer
	private FBNetworkElement mux = null;
	private List<FBNetworkElement> muxServerList = null;
	private List<FBNetworkElement> muxPublisherList = null;
	private List<EventConnection> muxInitializationConnectionList = null;

	@Override
	public void generateCommunicationComponents(final ExecutionEvent event, final FBNetworkElement demux,
			final FBNetworkElement mux, final AdapterConnection adapterConnection) {
		this.event = event;
		this.demux = demux;
		this.mux = mux;
		this.adapterConnection = adapterConnection;

		demuxClientList = new ArrayList<>();
		demuxSubscriberList = new ArrayList<>();
		muxServerList = new ArrayList<>();
		muxPublisherList = new ArrayList<>();
		eventConnectionList = new ArrayList<>();
		dataConnectionList = new ArrayList<>();
		adapterConnectionList = new ArrayList<>();
		demuxInitializationConnectionList = new ArrayList<>();
		muxInitializationConnectionList = new ArrayList<>();

		generateCommNetwork(false);
		generateCommNetwork(true);

	}

	private void generateCommNetwork(final boolean isMux) {
		// TODO: added 2021-03-16
		final FBNetworkEditor fbEditor = (FBNetworkEditor) HandlerUtil.getActiveEditor(event);

		// mux positions
		int yStartPosition_Server = mux.getY();
		int yOffsetServerFBs = 130;
		int xPosServerFBs = mux.getX()-200;
		
		int yStartPosition_Publish = mux.getY();
		int yOffsetPublisherFBs = 160;
		int xPosPublisherFBs = mux.getX()+200;

		
		// demux positions
		int yStartPosition_Client = demux.getY();
		int yOffsetClientFBs = 130;
		int xPosClientFBs = demux.getX()+200;
		
		int yStartPosition_Subscribe = demux.getY();
		int yOffsetSubscriberFBs = 160;
		int xPosSubscriberFBs = demux.getX()-200;
		
		
		final DataConnection currentDataConnection;
		final EventConnection currentEventConnection;
		FBNetworkElement fBNetworkElement;

		// create all the Clients and Publishers
		EList<Event> eventOutputList;

		if (isMux) {
			eventOutputList = mux.getInterface().getEventOutputs();
		} else {
			eventOutputList = demux.getInterface().getEventOutputs();
		}
		
		// create all the DEMUX-Clients and MUX-Publishers
		for (final Event eventOutput : eventOutputList) {

			fBNetworkElement = isMux
					? CommunicationFBGenerator.createPublisherFBType(event, mux.getResource().getFBNetwork(),
							eventOutput.getWith().size(), xPosPublisherFBs,
							yStartPosition_Publish += yOffsetPublisherFBs)
					: CommunicationFBGenerator.createClientFBType(event, demux.getResource().getFBNetwork(),
							eventOutput.getWith().size(), eventOutput.getWith().size(), xPosClientFBs,
							yStartPosition_Client += yOffsetClientFBs);

			if (isMux) {
				CloneUtilities.setMapping(fBNetworkElement, mux.getResource());
				muxPublisherList.add(fBNetworkElement);
			} else {
				CloneUtilities.setMapping(fBNetworkElement, demux.getResource());
				demuxClientList.add(fBNetworkElement);
			}

			// add Demux-Client- and Mux-Publisher-Connections

			// TODO: 29.03.2021
			// ApplicationUtilities.addEventConnectionInApplication(
			// fbEditor.getSystem().getApplicationNamed(fbEditor.getPartName()).getFBNetwork(),
			// eventOutput,
			// fBNetworkElement.getInterfaceElement("REQ"));
			ApplicationUtilities.addEventConnectionInApplication(
					isMux ? mux.getResource().getFBNetwork() : demux.getResource().getFBNetwork(), eventOutput,
					fBNetworkElement.getInterfaceElement("REQ"));
			// eventConnectionList.add(currentEventConnection);
			// add demux-client-data-connections (OPC_UA: from all demux-data-outputs to
			// client-RDs if event is connected with data (with))
			for (int i = 0; i < eventOutput.getWith().size(); i++) {

				// TODO: 29.03.2021
				// ApplicationUtilities.addDataConnectionToApplication(
				// fbEditor.getSystem().getApplicationNamed(fbEditor.getPartName()).getFBNetwork(),
				// eventOutput.getWith().get(i).getVariables(),
				// fBNetworkElement.getInterfaceElement("SD_" + (i + 1)));
				ApplicationUtilities.addDataConnectionToApplication(
						isMux ? mux.getResource().getFBNetwork() : demux.getResource().getFBNetwork(),
						eventOutput.getWith().get(i).getVariables(),
						fBNetworkElement.getInterfaceElement("SD_" + (i + 1)));
				// dataConnectionList.add(currentDataConnection);
			}

		}

		// create all the DEMUX-Subscribers and MUX-Servers
		for (final Event inputEvent : isMux ? mux.getInterface().getEventInputs()
				: demux.getInterface().getEventInputs()) {
			fBNetworkElement = isMux
					? CommunicationFBGenerator.createServerFBType(event, mux.getResource().getFBNetwork(),
							inputEvent.getWith().size(), inputEvent.getWith().size(), xPosServerFBs,
							yStartPosition_Server += yOffsetServerFBs)
					: CommunicationFBGenerator.createSubscriberFBType(event, demux.getResource().getFBNetwork(),
							inputEvent.getWith().size(), xPosSubscriberFBs, yStartPosition_Subscribe += yOffsetSubscriberFBs);

			if (isMux) {
				CloneUtilities.setMapping(fBNetworkElement, mux.getResource());
				muxServerList.add(fBNetworkElement);
			} else {
				CloneUtilities.setMapping(fBNetworkElement, demux.getResource());
				demuxSubscriberList.add(fBNetworkElement);
			}

			// add Subscriber-DEMUX-and Server-MUX-Event-Connection (OPC_UA: always from
			// IND-Event (subscriber) to EventInputs (DEMUX))

			// TODO: 29.03.2021
			// ApplicationUtilities.addEventConnectionInApplication(
			// fbEditor.getSystem().getApplicationNamed(fbEditor.getPartName()).getFBNetwork(),
			// fBNetworkElement.getInterfaceElement("IND"), inputEvent);
			ApplicationUtilities.addEventConnectionInApplication(
					isMux ? mux.getResource().getFBNetwork() : demux.getResource().getFBNetwork(),
					fBNetworkElement.getInterfaceElement("IND"), inputEvent);
			// eventConnectionList.add(currentEventConnection);

			// add sub-demux-data-connections (OPC_UA: from all subscribe-data-outputs to
			// demux-data-input if IND-Event is connected with data (with))
			for (int i = 0; i < inputEvent.getWith().size(); i++) {

				// TODO: 29.03.2021
				// ApplicationUtilities.addDataConnectionToApplication(
				// fbEditor.getSystem().getApplicationNamed(fbEditor.getPartName()).getFBNetwork(),
				// fBNetworkElement.getInterfaceElement("RD_" + (i + 1)),
				// inputEvent.getWith().get(i).getVariables());
				ApplicationUtilities.addDataConnectionToApplication(
						isMux ? mux.getResource().getFBNetwork() : demux.getResource().getFBNetwork(),
						fBNetworkElement.getInterfaceElement("RD_" + (i + 1)),
						inputEvent.getWith().get(i).getVariables());
				// dataConnectionList.add(currentDataConnection);
			}

		}

		// Initial-Sequences

		final List<FBNetworkElement> allCommFbElementsByMuxDemux = new ArrayList<>();
		if (isMux) {
			allCommFbElementsByMuxDemux.addAll(muxServerList);
			allCommFbElementsByMuxDemux.addAll(muxPublisherList);
		} else {
			allCommFbElementsByMuxDemux.addAll(demuxSubscriberList);
			allCommFbElementsByMuxDemux.addAll(demuxClientList);
		}

		// TODO: Check if at least 1/2 Elements in list
		// add first Inital-Connection when migrated to internal commnunication
		ApplicationUtilities.addEventConnectionInApplication(
				isMux ? mux.getResource().getFBNetwork() : demux.getResource().getFBNetwork(),
				isMux ? mux.getResource().getFBNetwork().getFBNamed("START").getInterfaceElement("COLD")
						: demux.getResource().getFBNetwork().getFBNamed("START").getInterfaceElement("COLD"),
				allCommFbElementsByMuxDemux.get(0).getInterfaceElement("INIT"));

		ApplicationUtilities.addEventConnectionInApplication(
				isMux ? mux.getResource().getFBNetwork() : demux.getResource().getFBNetwork(),
				isMux ? mux.getResource().getFBNetwork().getFBNamed("START").getInterfaceElement("WARM")
						: demux.getResource().getFBNetwork().getFBNamed("START").getInterfaceElement("WARM"),
				allCommFbElementsByMuxDemux.get(0).getInterfaceElement("INIT"));

		for (int i = 0; i < allCommFbElementsByMuxDemux.size() - 1; i++) {
			final FBNetworkElement prev = allCommFbElementsByMuxDemux.get(i);
			final FBNetworkElement next = allCommFbElementsByMuxDemux.get(i + 1);

			// TODO: 29.03.2021
			// ApplicationUtilities.addEventConnectionInApplication(
			// fbEditor.getSystem().getApplicationNamed(fbEditor.getPartName()).getFBNetwork(),
			// prev.getInterfaceElement("INITO"), next.getInterfaceElement("INIT"));
			ApplicationUtilities.addEventConnectionInApplication(
					isMux ? mux.getResource().getFBNetwork() : demux.getResource().getFBNetwork(),
					prev.getInterfaceElement("INITO"), next.getInterfaceElement("INIT"));
		}

		// TODO: add adapter connection

		final AdapterConnectionCreateCommand createAdapterConnection = new AdapterConnectionCreateCommand(
				isMux ? mux.getResource().getFBNetwork() : demux.getResource().getFBNetwork());

		createAdapterConnection
				.setSource(isMux ? mux.getInterfaceElement(adapterConnection.getDestination().getTypeName())
						: demux.getResource().getFBNetwork()
								.getElementNamed(adapterConnection.getSource().getFBNetworkElement().getName())
								.getInterfaceElement(adapterConnection.getSource().getName()));

		createAdapterConnection.setDestination(isMux
				? mux.getResource().getFBNetwork()
						.getElementNamed(adapterConnection.getDestination().getFBNetworkElement().getName())
						.getInterfaceElement(adapterConnection.getDestination().getName())
				: demux.getInterfaceElement(adapterConnection.getSource().getTypeName()));

		if (createAdapterConnection.canExecute()) {
			createAdapterConnection.execute();
		}

		// add OPC_UA specific Connection from IND to RSP in Server-FBs
		for (final FBNetworkElement networkElement : muxServerList) {

			// TODO: 29.03.2021
			// ApplicationUtilities.addEventConnectionInApplication(
			// fbEditor.getSystem().getApplicationNamed(fbEditor.getPartName()).getFBNetwork(),
			// networkElement.getInterfaceElement("IND"),
			// networkElement.getInterfaceElement("RSP"));
			ApplicationUtilities.addEventConnectionInApplication(
					isMux ? mux.getResource().getFBNetwork() : demux.getResource().getFBNetwork(),
					networkElement.getInterfaceElement("IND"), networkElement.getInterfaceElement("RSP"));
		}

		// Set QI to "1"
		final ArrayList<FBNetworkElement> allFbNetworkElements = new ArrayList<>();
		allFbNetworkElements.addAll(demuxClientList);
		allFbNetworkElements.addAll(demuxSubscriberList);
		allFbNetworkElements.addAll(muxPublisherList);
		allFbNetworkElements.addAll(muxServerList);

		for (final FBNetworkElement networkElement : allFbNetworkElements) {
			final VarDeclaration varDeclQI = (VarDeclaration) networkElement.getInterfaceElement("QI");
			ChangeValueCommand cmd = new ChangeValueCommand(varDeclQI, "1");
			if (cmd.canExecute()) {
				cmd.execute();
			}

			final VarDeclaration varDeclID = (VarDeclaration) networkElement.getInterfaceElement("ID");
			String idValue = "";
			if (networkElement.getName().contains("CLIENT")) {
				idValue = opcUaParamStringBuilder("CLIENT", networkElement);
			} else if (networkElement.getName().contains("SERVER")) {
				idValue = opcUaParamStringBuilder("SERVER", networkElement);
			} else if (networkElement.getName().contains("PUBLISH")) {
				idValue = opcUaParamStringBuilder("PUBLISH", networkElement);
			} else if (networkElement.getName().contains("SUBSCRIBE")) {
				idValue = opcUaParamStringBuilder("SUBSCRIBE", networkElement);
			}

			cmd = new ChangeValueCommand(varDeclID, idValue);
			if (cmd.canExecute()) {
				cmd.execute();
			}
		}

	}

	private String opcUaParamStringBuilder(final String commFBType, final FBNetworkElement fBNetworkElement) {
		String connectionString = "opc_ua[";

		String opcUaAction = "";
		if (commFBType.equalsIgnoreCase("CLIENT")) {
			opcUaAction = "CALL_METHOD";
		} else if (commFBType.equalsIgnoreCase("SERVER")) {
			opcUaAction = "CREATE_METHOD";
		} else if (commFBType.equalsIgnoreCase("PUBLISH")) {
			opcUaAction = "WRITE";
		} else if (commFBType.equalsIgnoreCase("SUBSCRIBE")) {
			opcUaAction = "SUBSCRIBE";
		} else {
			return null;
		}

		connectionString += opcUaAction + ";";

		if (commFBType.equalsIgnoreCase("CLIENT") || commFBType.equalsIgnoreCase("SUBSCRIBE")) {
			String providerDevice = "opc.tcp://";

			// IP
			String ipAndPort = mux.getResource().getDevice().getVarDeclarations().get(0).getValue().getValue();
			ipAndPort = ipAndPort.replaceAll("\"", "");

			// dynamic port form 4diac variable or default
			if (mux.getResource().getDevice().getAttributeValue("opcua") != null) {
				ipAndPort = ipAndPort.substring(0, ipAndPort.indexOf(':') + 1)
						+ mux.getResource().getDevice().getAttributeValue("opcua");
			} else {
				ipAndPort = ipAndPort.substring(0, ipAndPort.indexOf(':') + 1) + "4840";
			}
			providerDevice += ipAndPort + "#;";

			connectionString += providerDevice;
		}

		final String fbName = adapterConnection.getDestination().getFBNetworkElement().getName();

		if (commFBType.equalsIgnoreCase("PUBLISH") || commFBType.equalsIgnoreCase("SUBSCRIBE")) {
			final boolean isPublish = commFBType.equalsIgnoreCase("PUBLISH");

			final List<VarDeclaration> varDeclarationList = ApplicationUtilities
					.getWithsByEvent(isPublish ? "REQ" : "IND", fBNetworkElement);

			String varDeclarationsString = "";
			for (final VarDeclaration varDeclaration : varDeclarationList) {
				varDeclarationsString += "/Objects/" + fbName + "/" + varDeclaration.getName() + ";";
			}
			connectionString += varDeclarationsString;

		} else if (commFBType.equalsIgnoreCase("SERVER") || commFBType.equalsIgnoreCase("CLIENT")) {
			final boolean isServer = commFBType.equalsIgnoreCase("SERVER");

			final String opcUaNode = (isServer
					? fBNetworkElement.getInterfaceElement("IND").getOutputConnections().get(0).getDestination()
							.getName()
					: fBNetworkElement.getInterfaceElement("REQ").getInputConnections().get(0).getSource().getName());
			String eventString = "/Objects/" + fbName + "/" + opcUaNode;
			eventString += ",1:s=" + fbName + "_" + opcUaNode;

			connectionString += eventString;
		
		}
		
		//connectionString = connectionString.substring(0, connectionString.length() - 1); 

		if (connectionString.endsWith(";")) {
			// last semicolon must not be there
			connectionString = connectionString.substring(0, connectionString.length() - 1); 
		}
		
		connectionString += "]";
		return connectionString;
	}

}