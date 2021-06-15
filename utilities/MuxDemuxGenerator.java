/**
* @author  sieffert
* @author  dorofeev
*/
package org.eclipse.fordiac.ide.application.utilities;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.EList;
import org.eclipse.fordiac.ide.application.editors.FBNetworkEditor;
import org.eclipse.fordiac.ide.model.Palette.DataTypePaletteEntry;
import org.eclipse.fordiac.ide.model.Palette.FBTypePaletteEntry;
import org.eclipse.fordiac.ide.model.Palette.PaletteFactory;
import org.eclipse.fordiac.ide.model.commands.create.CreateInterfaceElementCommand;
import org.eclipse.fordiac.ide.model.commands.create.FBCreateCommand;
import org.eclipse.fordiac.ide.model.data.AnyDerivedType;
import org.eclipse.fordiac.ide.model.dataexport.AbstractBlockTypeExporter;
import org.eclipse.fordiac.ide.model.dataexport.DataTypeExporter;
import org.eclipse.fordiac.ide.model.libraryElement.AdapterConnection;
import org.eclipse.fordiac.ide.model.libraryElement.AdapterType;
import org.eclipse.fordiac.ide.model.libraryElement.CompositeFBType;
import org.eclipse.fordiac.ide.model.libraryElement.Event;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetworkElement;
import org.eclipse.fordiac.ide.model.libraryElement.LibraryElementFactory;
import org.eclipse.fordiac.ide.model.libraryElement.VarDeclaration;
import org.eclipse.fordiac.ide.model.libraryElement.With;
import org.eclipse.fordiac.ide.systemmanagement.Activator;

import org.eclipse.ui.handlers.HandlerUtil;

/** The Class MuxDemuxGenerator. */
public class MuxDemuxGenerator {

	static int demuxCounter = 0;
	private ExecutionEvent event = null;
	private AdapterConnection adapterConnection = null;
	private AdapterType adapterType = null;
	private FBNetworkElement demuxFB_Consumer = null;
	private FBNetworkElement muxFB_Producer = null;
	private CommunicationProtocolSpecifics cps = null;
	private int xPos = -400;
	private int yPos = 400;

	/**
	 * With MuxDemuxGenerator-Object, Mux-, Demux-, Server-, Client-, Pubublish-,
	 * Subscribe-FBs can be generated for the specified communication protocol
	 * 
	 * @param event
	 * @param communicationProtocol
	 */
	public MuxDemuxGenerator(final ExecutionEvent event, final String communicationProtocol) {
		this.event = event;
		this.cps = CommunicationProtocolsSpecificsFactory.getCommunicationProtocolSpecifics(communicationProtocol);
	}

	/**
	 * generates the Mux- and Demux-FB for a given adapterConnection
	 * @param adapterConnection 
	 */
	public void generateMuxDemux(final AdapterConnection adapterConnection) {
		this.adapterConnection = adapterConnection;
		this.adapterType = this.adapterConnection.getAdapterSource().getType();

		try {
			demuxFB_Consumer = addFBToApplication(adapterType.getName().toUpperCase() + "_DEMUX");
			CloneUtilities.setMapping(demuxFB_Consumer,
					adapterConnection.getAdapterSource().getFBNetworkElement().getResource());
		} catch (final Exception e) {
			e.printStackTrace();
		}

		try {
			muxFB_Producer = addFBToApplication(adapterType.getName().toUpperCase() + "_MUX");
			CloneUtilities.setMapping(muxFB_Producer,
					adapterConnection.getAdapterDestination().getFBNetworkElement().getResource());
		} catch (final Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * generates Server-, Client-, Pubublish-, Subscribe-FBs, if Mux- and Demux-FBs
	 * were generated first
	 */
	public void generateCommunicationFBs() {
		if (!muxFB_Producer.equals(null) && !demuxFB_Consumer.equals(null)) {
			cps.generateCommunicationComponents(event, demuxFB_Consumer, muxFB_Producer, adapterConnection);
		}
	}

	private CompositeFBType createMuxDemuxFB(final CompositeFBType cfb, final boolean mux) {
		cfb.setName(adapterType.getName().toUpperCase() + (mux ? "_MUX" : "_DEMUX"));

		// ---------Setup Interface---------
		// EventInputs
		cfb.getInterfaceList().getEventInputs().clear();
		final EList<Event> adapterEventInputList = mux ? adapterType.getInterfaceList().getEventOutputs()
				: adapterType.getInterfaceList().getEventInputs();
		for (final Event adapterEventInput : adapterEventInputList) {
			final Event demuxEventInput = CloneUtilities.cloneEvent(adapterEventInput, mux);
			cfb.getInterfaceList().getEventInputs().add(demuxEventInput);
		}

		// EventOutputs
		cfb.getInterfaceList().getEventOutputs().clear();
		final EList<Event> adapterEventOutputList = mux ? adapterType.getInterfaceList().getEventInputs()
				: adapterType.getInterfaceList().getEventOutputs();
		for (final Event adapterEventOutput : adapterEventOutputList) {
			final Event demuxEventOutput = CloneUtilities.cloneEvent(adapterEventOutput, mux);
			cfb.getInterfaceList().getEventOutputs().add(demuxEventOutput);
		}

		// DataInputs
		cfb.getInterfaceList().getInputVars().clear();
		final EList<VarDeclaration> adapterDataInputList = mux ? adapterType.getInterfaceList().getOutputVars()
				: adapterType.getInterfaceList().getInputVars();
		for (final VarDeclaration adapterDataInput : adapterDataInputList) {
			final VarDeclaration demuxDataInput = CloneUtilities.cloneVarDeclaration(adapterDataInput, mux);
			cfb.getInterfaceList().getInputVars().add(demuxDataInput);
		}

		// DataOutputs
		cfb.getInterfaceList().getOutputVars().clear();
		final EList<VarDeclaration> adapterDataOutputList = mux ? adapterType.getInterfaceList().getInputVars()
				: adapterType.getInterfaceList().getOutputVars();
		for (final VarDeclaration adapterDataOutput : adapterDataOutputList) {
			final VarDeclaration demuxDataOutput = CloneUtilities.cloneVarDeclaration(adapterDataOutput, mux);
			cfb.getInterfaceList().getOutputVars().add(demuxDataOutput);
		}

		// Withs EventInput
		for (final Event adapterEventInput : adapterEventInputList) {
			final List<With> clonedWiths = new ArrayList<>();
			for (final With withVarDeclaration : adapterEventInput.getWith()) {
				final With with = LibraryElementFactory.eINSTANCE.createWith();
				with.setVariables(cfb.getInterfaceList().getVariable(withVarDeclaration.getVariables().getName()));
				clonedWiths.add(with);
			}
			cfb.getInterfaceList().getEvent(adapterEventInput.getName()).getWith().addAll(clonedWiths);
		}

		// Withs EventOutput
		for (final Event adapterEventOutput : adapterEventOutputList) {
			final List<With> clonedWiths = new ArrayList<>();
			for (final With withVarDeclaration : adapterEventOutput.getWith()) {
				final With with = LibraryElementFactory.eINSTANCE.createWith();
				with.setVariables(cfb.getInterfaceList().getVariable(withVarDeclaration.getVariables().getName()));
				clonedWiths.add(with);
			}
			cfb.getInterfaceList().getEvent(adapterEventOutput.getName()).getWith().addAll(clonedWiths);
		}

		// Plug/ Socket (Source of AdapterConnection is always Plug and so IsInput=true)
		cfb.getInterfaceList().getSockets().clear();
		cfb.getInterfaceList().getPlugs().clear();

		final CreateInterfaceElementCommand cmd = new CreateInterfaceElementCommand(adapterType, cfb.getInterfaceList(),
				!mux, 0);
		if (cmd.canExecute()) {
			cmd.execute();
		}

		cmd.getInterfaceElement().setName(cmd.getInterfaceElement().getTypeName());

		// Internal Connections
		for (final Event ev : cfb.getInterfaceList().getEventInputs()) {
			ApplicationUtilities.addEventConnectionInApplication(cfb.getFBNetwork(), ev,
					cfb.getFBNetwork().getFBNamed(adapterType.getName()).getInterfaceElement(ev.getName()));
		}
		for (final Event ev : cfb.getInterfaceList().getEventOutputs()) {
			ApplicationUtilities.addEventConnectionInApplication(cfb.getFBNetwork(),
					cfb.getFBNetwork().getFBNamed(adapterType.getName()).getInterfaceElement(ev.getName()), ev);
		}
		for (final VarDeclaration vd : cfb.getInterfaceList().getInputVars()) {
			ApplicationUtilities.addDataConnectionToApplication(cfb.getFBNetwork(), vd,
					cfb.getFBNetwork().getFBNamed(adapterType.getName()).getInterfaceElement(vd.getName()));
		}
		for (final VarDeclaration vd : cfb.getInterfaceList().getOutputVars()) {
			ApplicationUtilities.addDataConnectionToApplication(cfb.getFBNetwork(),
					cfb.getFBNetwork().getFBNamed(adapterType.getName()).getInterfaceElement(vd.getName()), vd);
		}

		return cfb;
	}

	private FBNetworkElement addFBToApplication(final String requiredFbName) throws Exception {
		final FBNetworkEditor fbEditor = (FBNetworkEditor) HandlerUtil.getActiveEditor(event);
		// must be a checked if such type exists, otherwise do the following...
		final String templateFolderPath = Platform.getInstallLocation().getURL().getFile();
		final File templateFolder = new File(templateFolderPath + File.separatorChar + "template"); //$NON-NLS-1$
		final File[] files = templateFolder.listFiles();
		final FBTypePaletteEntry pallEntry = PaletteFactory.eINSTANCE.createFBTypePaletteEntry();
		for (final File file : files) {
			if (file.getName().equals("Composite.fbt")) {
				try {
					final IFile iFile;
					if (!ResourcesPlugin.getWorkspace().getRoot().getProject(fbEditor.getSystem().getName())
							.getFile(requiredFbName + ".fbt").exists()) {
						iFile = ResourcesPlugin.getWorkspace().getRoot().getProject(fbEditor.getSystem().getName())
								.getFile(requiredFbName + ".fbt");
						try {
							final InputStream targetStream = new FileInputStream(file);
							iFile.create(targetStream, true, null);
						} catch (final CoreException e1) {
							e1.printStackTrace();
						}
						Files.copy(file.toPath(),
								Path.of(fbEditor.getSystem().getApplicationNamed(fbEditor.getPartName())
										.getAutomationSystem().getPalette().getProject().getLocationURI().getPath()
										.substring(3) + "/" + requiredFbName + ".fbt"), // Path.of doesn't handle 'C:/'
								// that's why substring(3)..
								StandardCopyOption.REPLACE_EXISTING);
						try {
							iFile.refreshLocal(IResource.DEPTH_ZERO, null); // https://stackoverflow.com/questions/27272258/error-converting-java-io-file-to-org-core-resource-ifile
						} catch (final CoreException e) {
							e.printStackTrace();
						}
					} else {
						iFile = ResourcesPlugin.getWorkspace().getRoot().getProject(fbEditor.getSystem().getName())
								.getFile(requiredFbName + ".fbt");
					}
					pallEntry.setFile(iFile);
					pallEntry.setPalette(fbEditor.getSystem().getApplicationNamed(fbEditor.getPartName())
							.getAutomationSystem().getPalette());
					CompositeFBType fb = (CompositeFBType) pallEntry.getFBType();
					fb.setPaletteEntry(pallEntry);
					fb.setName(requiredFbName);
					fb.setInterfaceList(LibraryElementFactory.eINSTANCE.createInterfaceList());
					fb.setFBNetwork(LibraryElementFactory.eINSTANCE.createFBNetwork());
					if (requiredFbName.contains("DEMUX")) {
						fb = createMuxDemuxFB(fb, false);
					} else if (requiredFbName.contains("MUX")) {
						fb = createMuxDemuxFB(fb, true);
					} else {
						throw new Exception("not a valid FB name. should contain either _DEMUX or _MUX");
					}

					if (pallEntry instanceof DataTypePaletteEntry) {
						final DataTypeExporter exporter = new DataTypeExporter((AnyDerivedType) pallEntry.getType());
						try {
							exporter.saveType(pallEntry.getFile());
						} catch (final XMLStreamException e) {
							Activator.getDefault().logError(e.getMessage(), e);
						}
					} else {
						AbstractBlockTypeExporter.saveType(pallEntry);
					}

					pallEntry.setLabel(fb.getName());
					pallEntry.setType(fb);
					fbEditor.getSystem().getApplicationNamed(fbEditor.getPartName()).getAutomationSystem().getPalette()
							.addPaletteEntry(pallEntry);
				} catch (final IOException e2) {
					e2.printStackTrace();
				}
			}
		}

		// TODO: 29.03.2021
		// final FBCreateCommand fbcmd = new FBCreateCommand(pallEntry,
		// fbEditor.getSystem().getApplicationNamed(fbEditor.getPartName()).getFBNetwork(),
		// xPos, yPos);
		
		final FBCreateCommand fbcmd;
		if(requiredFbName.contains("DEMUX")) {
			fbcmd = new FBCreateCommand(pallEntry,
					adapterConnection.getAdapterSource().getFBNetworkElement().getResource().getFBNetwork(),
					getXPosition(false), getYPosition(false));	
			demuxCounter++;
		} else {
			fbcmd = new FBCreateCommand(pallEntry,
					adapterConnection.getAdapterDestination().getFBNetworkElement().getResource().getFBNetwork(),
					getXPosition(true), getYPosition(true));
		}
		
		if (fbcmd.canExecute()) {
			fbcmd.execute();
		}
		return fbcmd.getElement();
	}

	private int getXPosition(boolean isMux) {
		if(isMux) {
			return 400;
		} else {
			return 400 + 600*demuxCounter;
		}
		
	}	
	
	private int getYPosition(boolean isMux) {
		return 250;
	}
	
}