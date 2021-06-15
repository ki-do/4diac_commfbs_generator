/**
* @author  sieffert
*/
package org.eclipse.fordiac.ide.application.utilities;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.emf.common.util.EList;
import org.eclipse.fordiac.ide.application.editors.FBNetworkEditor;
import org.eclipse.fordiac.ide.model.FordiacKeywords;
import org.eclipse.fordiac.ide.model.Palette.FBTypePaletteEntry;
import org.eclipse.fordiac.ide.model.Palette.PaletteFactory;
import org.eclipse.fordiac.ide.model.commands.create.AdapterConnectionCreateCommand;
import org.eclipse.fordiac.ide.model.commands.create.DataConnectionCreateCommand;
import org.eclipse.fordiac.ide.model.commands.create.EventConnectionCreateCommand;
import org.eclipse.fordiac.ide.model.commands.create.FBCreateCommand;
import org.eclipse.fordiac.ide.model.libraryElement.AdapterConnection;
import org.eclipse.fordiac.ide.model.libraryElement.AdapterDeclaration;
import org.eclipse.fordiac.ide.model.libraryElement.AdapterType;
import org.eclipse.fordiac.ide.model.libraryElement.BasicFBType;
import org.eclipse.fordiac.ide.model.libraryElement.CompositeFBType;
import org.eclipse.fordiac.ide.model.libraryElement.Connection;
import org.eclipse.fordiac.ide.model.libraryElement.DataConnection;
import org.eclipse.fordiac.ide.model.libraryElement.Event;
import org.eclipse.fordiac.ide.model.libraryElement.EventConnection;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetwork;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetworkElement;
import org.eclipse.fordiac.ide.model.libraryElement.FBType;
import org.eclipse.fordiac.ide.model.libraryElement.IInterfaceElement;
import org.eclipse.fordiac.ide.model.libraryElement.LibraryElementFactory;
import org.eclipse.fordiac.ide.model.libraryElement.Mapping;
import org.eclipse.fordiac.ide.model.libraryElement.ServiceInterfaceFBType;
import org.eclipse.fordiac.ide.model.libraryElement.VarDeclaration;
import org.eclipse.fordiac.ide.model.typelibrary.DataTypeLibrary;
import org.eclipse.fordiac.ide.model.typelibrary.TypeLibrary;
import org.eclipse.ui.handlers.HandlerUtil;

public class DestributedOpcUaAdapterCommunicationNetworkGenerator {

	// ExecutionEvent
	private static ExecutionEvent event = null;
	// Adapter
	private static AdapterConnection adapterConnection = null;
	private static AdapterType adapterType = null;
	// Consumer/ Producer Mapping
	private static Mapping consumerMapping = null;
	private static Mapping producerMapping = null;
	// FBs
	private static FBNetworkElement demuxFB_Consumer = null;
	private static FBNetworkElement muxFB_Producer = null;
	private static List<ServiceInterfaceFBType> demuxClientList_Consumer = null;
	private static List<ServiceInterfaceFBType> muxServerList_Producer = null;
	private static ServiceInterfaceFBType demuxSubscriber_Consumer = null;
	private static ServiceInterfaceFBType muxPublisher_Producer = null;
	// Connections
	private static List<DataConnection> dataConnectionList = null;
	private static List<EventConnection> eventConnectionList = null;
	private static List<AdapterConnection> adapterConnectionList = null;
	// Coordinates
	private static int xPos = -180;
	private static int yPos = 300;

	// Constructor
	public DestributedOpcUaAdapterCommunicationNetworkGenerator(ExecutionEvent event,
			AdapterConnection adapterConnection) {
		this.event = event;
		this.adapterConnection = adapterConnection;
		this.adapterType = (AdapterType) adapterConnection
				.getAdapterSource().getType();
		this.consumerMapping = adapterConnection.getAdapterSource()
				.getFBNetworkElement().getMapping();
		this.producerMapping = adapterConnection.getAdapterDestination()
				.getFBNetworkElement().getMapping();

		this.demuxFB_Consumer = addFBToApplication(createDemuxFB());
		this.demuxFB_Consumer.setMapping(DestributedOpcUaAdapterCommunicationNetworkGenerator.consumerMapping);

		this.muxFB_Producer = addFBToApplication(createMuxFB());
		//this.muxFB_Producer.setMapping(DestributedOpcUaAdapterCommunicationNetworkGenerator.producerMapping);
	}

	// public Methods

	public static void generate(CompositeFBType cfb) {

	}

	// private Methods
	// MUX-/ DEMUX-FBs
	private CompositeFBType createDemuxFB() {
		return createMuxDemuxFBtype(true);
	}

	private CompositeFBType createMuxFB() {
		return createMuxDemuxFBtype(false);
	}

	private static FBNetworkElement addFBToApplication(FBType fb) {

		final FBNetworkEditor fbEditor = (FBNetworkEditor) HandlerUtil.getActiveEditor(event);
		// add to paletteEntry
		final FBTypePaletteEntry pallEntry = PaletteFactory.eINSTANCE.createFBTypePaletteEntry();
		// final TypeLibrary typelib =
		// fbEditor.getSystem().getPalette().getTypeLibrary();
		final TypeLibrary typelib = TypeLibrary.getTypeLibrary(null);

		pallEntry.setPalette(typelib.getBlockTypeLib());
		pallEntry.setLabel(fb.getName());
		pallEntry.setType(fb);
		typelib.addPaletteEntry(pallEntry);
		fb.setPaletteEntry(pallEntry);

		setXPosYPos();

		FBCreateCommand fbcmd = new FBCreateCommand(pallEntry,
				fbEditor.getSystem().getApplication().get(0).getFBNetwork(), xPos, yPos);
		if (fbcmd.canExecute()) {
			fbcmd.execute();
		}
		return fbcmd.getElement();
	}

	private static CompositeFBType createMuxDemuxFBtype(boolean isDemux) {
		final CompositeFBType cfb = LibraryElementFactory.eINSTANCE.createCompositeFBType();
		cfb.setInterfaceList(LibraryElementFactory.eINSTANCE.createInterfaceList());

		if (isDemux) {
			// Set name of DemuxFB
			cfb.setName(adapterType.getName().toUpperCase() + "_DEMUX");

			// ---------Setup Interface---------
			// Add EventInputs to DemuxFB-InterfaceList
			EList<Event> adapterEventInputList = adapterType.getInterfaceList().getEventInputs();
			for (Event adapterEventInput : adapterEventInputList) {
				final Event demuxEventInput = cloneEvent(adapterEventInput, false);
				cfb.getInterfaceList().getEventInputs().add(demuxEventInput);
			}

			// Add EventOutputs to DemuxFB-InterfaceList
			EList<Event> adapterEventOutputList = adapterType.getInterfaceList().getEventOutputs();
			for (Event adapterEventOutput : adapterEventOutputList) {
				final Event demuxEventOutput = cloneEvent(adapterEventOutput, false);
				cfb.getInterfaceList().getEventOutputs().add(demuxEventOutput);
			}

			// Add DataInputs to DemuxFb-InterfaceList
			EList<VarDeclaration> adapterDataInputList = adapterType.getInterfaceList().getInputVars();
			for (VarDeclaration adapterDataInput : adapterDataInputList) {
				final VarDeclaration demuxDataInput = cloneVarDecleration(adapterDataInput, false);
				cfb.getInterfaceList().getInputVars().add(demuxDataInput);
			}

			// Add DataOutputs to DemuxFB-InterfaceList
			EList<VarDeclaration> adapterDataOutputList = adapterType.getInterfaceList().getOutputVars();
			for (VarDeclaration adapterDataOutput : adapterDataOutputList) {
				final VarDeclaration demuxDataOutput = cloneVarDecleration(adapterDataOutput, false);
				cfb.getInterfaceList().getOutputVars().add(demuxDataOutput);
			}

			// Source of AdapterConnection is always Plug and so IsInput=true
			cfb.getInterfaceList().getSockets().add(cloneAdapterDecleration(false));
		} else {// is Multiplexer
			// Set name of DemuxFB
			cfb.setName(adapterType.getName().toUpperCase() + "_MUX");

			// ---------Setup Interface---------
			// Add EventInputs to DemuxFB-EventOutputs
			EList<Event> adapterEventInputList = adapterType.getInterfaceList().getEventInputs();
			for (Event adapterEventInput : adapterEventInputList) {
				final Event demuxEventOutput = cloneEvent(adapterEventInput, true);
				cfb.getInterfaceList().getEventOutputs().add(demuxEventOutput);
			}

			// Add EventOutputs to DemuxFB-InterfaceList
			EList<Event> adapterEventOutputList = adapterType.getInterfaceList().getEventOutputs();
			for (Event adapterEventOutput : adapterEventOutputList) {
				final Event demuxEventInput = cloneEvent(adapterEventOutput, true);
				cfb.getInterfaceList().getEventInputs().add(demuxEventInput);
			}

			// Add DataInputs to DemuxFb-InterfaceList
			EList<VarDeclaration> adapterDataInputList = adapterType.getInterfaceList().getInputVars();
			for (VarDeclaration adapterDataInput : adapterDataInputList) {
				final VarDeclaration demuxDataOutput = cloneVarDecleration(adapterDataInput, true);
				cfb.getInterfaceList().getOutputVars().add(demuxDataOutput);
			}

			// Add DataOutputs to DemuxFB-InterfaceList
			EList<VarDeclaration> adapterDataOutputList = adapterType.getInterfaceList().getOutputVars();
			for (VarDeclaration adapterDataOutput : adapterDataOutputList) {
				final VarDeclaration demuxDataInput = cloneVarDecleration(adapterDataOutput, true);
				cfb.getInterfaceList().getInputVars().add(demuxDataInput);
			}

			// Destination of AdapterConnection is always Socket, and so IsInput=false
			cfb.getInterfaceList().getPlugs().add(cloneAdapterDecleration(true));
		}
		return cfb;
	}

	private static Event cloneEvent(Event event, boolean inverseInAndOutput) {
		Event eventClone = LibraryElementFactory.eINSTANCE.createEvent();
		eventClone.setName(event.getName());
		eventClone.setComment(event.getComment());
		eventClone.setType(event.getType());
		eventClone.setTypeName(event.getTypeName());

		if (inverseInAndOutput) {
			eventClone.setIsInput(!event.isIsInput());
		} else {
			eventClone.setIsInput(event.isIsInput());
		}

		return eventClone;
	}

	private static VarDeclaration cloneVarDecleration(VarDeclaration data, boolean inverseInAndOutput) {
		VarDeclaration dataClone = LibraryElementFactory.eINSTANCE.createVarDeclaration();
		dataClone.setName(data.getName());
		dataClone.setType(data.getType());
		dataClone.setTypeName(data.getTypeName());
		dataClone.setArraySize(data.getArraySize());
		dataClone.setComment(data.getComment());
		dataClone.setValue(data.getValue());

		if (inverseInAndOutput) {
			dataClone.setIsInput(!data.isIsInput());
		} else {
			dataClone.setIsInput(data.isIsInput());
		}

		return dataClone;
	}

	private static AdapterDeclaration cloneAdapterDecleration(boolean inverseInAndOutput) {
		AdapterDeclaration adapterDecleration = LibraryElementFactory.eINSTANCE.createAdapterDeclaration();
		adapterDecleration.setArraySize(adapterConnection.getAdapterDestination().getArraySize());
		adapterDecleration.setComment(adapterConnection.getAdapterDestination().getComment());
		adapterDecleration.setIsInput(adapterConnection.getAdapterDestination().isIsInput());
		adapterDecleration.setName(adapterConnection.getAdapterDestination().getName());
		adapterDecleration.setPaletteEntry(adapterConnection.getAdapterDestination().getPaletteEntry());
		adapterDecleration.setType(adapterConnection.getAdapterDestination().getType());
		adapterDecleration.setTypeName(adapterConnection.getAdapterDestination().getTypeName());
		adapterDecleration.setValue(adapterConnection.getAdapterDestination().getValue());

		if (inverseInAndOutput) {
			adapterDecleration.setIsInput(!adapterConnection.getAdapterDestination().isIsInput());
		} else {
			adapterDecleration.setIsInput(adapterConnection.getAdapterDestination().isIsInput());
		}

		return adapterDecleration;

	}

	// Other CommFBs (Server, Client, Subscribe, Publish)
	private List<ServiceInterfaceFBType> createDemuxClientList() {
		return null;
	}

	private List<ServiceInterfaceFBType> createMuxServerList() {
		return null;
	}

	private ServiceInterfaceFBType createDemuxSubscriber() {
		return null;
	}

	private ServiceInterfaceFBType createMuxPublisher() {
		return null;
	}

	// Connections
	private static FBNetworkElement addEventConnectionToApplication(EventConnection eventConnection) {

		final FBNetworkEditor fbEditor = (FBNetworkEditor) HandlerUtil.getActiveEditor(event);
		EventConnectionCreateCommand conCmd = new EventConnectionCreateCommand(
				fbEditor.getSystem().getApplication().get(0).getFBNetwork());

		if (conCmd.canExecute()) {
			conCmd.execute();
		}
		return null;
	}

	private static FBNetworkElement addDataConnectionToApplication(DataConnection dataConnection) {

		final FBNetworkEditor fbEditor = (FBNetworkEditor) HandlerUtil.getActiveEditor(event);
		DataConnectionCreateCommand conCmd = new DataConnectionCreateCommand(
				fbEditor.getSystem().getApplication().get(0).getFBNetwork());

		if (conCmd.canExecute()) {
			conCmd.execute();
		}
		return null;
	}

	private static FBNetworkElement addAdapterConnectionToApplication(AdapterConnection adapterConnection) {

		final FBNetworkEditor fbEditor = (FBNetworkEditor) HandlerUtil.getActiveEditor(event);
		AdapterConnectionCreateCommand conCmd = new AdapterConnectionCreateCommand(
				fbEditor.getSystem().getApplication().get(0).getFBNetwork());

		if (conCmd.canExecute()) {
			conCmd.execute();
		}
		return null;
	}

	private List<EventConnection> createEventConnectionList() {
		return null;
	}

	private List<DataConnection> createDataConnectionList() {
		return null;
	}

	private List<AdapterConnection> createAdapterConnectionList() {
		return null;
	}

	// General
	private static void setXPosYPos() {

		int xPosCurrent = xPos;

		if (xPosCurrent > 800) {
			xPos = 20;
			yPos += 150;
		} else {
			xPos += 200;
		}
	}

} // class
