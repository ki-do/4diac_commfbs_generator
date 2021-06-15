/**
* @author  sieffert
* @author  dorofeev
*/

package org.eclipse.fordiac.ide.application.utilities;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.fordiac.ide.application.editors.ApplicationEditor;
import org.eclipse.fordiac.ide.application.editors.FBNetworkEditor;
import org.eclipse.fordiac.ide.model.FordiacKeywords;
import org.eclipse.fordiac.ide.model.Palette.FBTypePaletteEntry;
import org.eclipse.fordiac.ide.model.Palette.PaletteFactory;
import org.eclipse.fordiac.ide.model.commands.create.FBCreateCommand;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetwork;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetworkElement;
import org.eclipse.fordiac.ide.model.libraryElement.FBType;
import org.eclipse.fordiac.ide.model.libraryElement.LibraryElementFactory;
import org.eclipse.fordiac.ide.model.libraryElement.VarDeclaration;
import org.eclipse.fordiac.ide.model.libraryElement.With;
import org.eclipse.fordiac.ide.model.typelibrary.DataTypeLibrary;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * The Class CommunicationFBsGenerator.
 */
public final class CommunicationFBGenerator {

	public static FBNetworkElement createServerFBType(final ExecutionEvent event, FBNetwork network, final int sendDataAmount,
			final int readDataAmount, final int xpos, final int ypos) {
		return createCommunicationFBType(event, network, "SERVER", sendDataAmount, readDataAmount, xpos, ypos);
	}

	public static FBNetworkElement createClientFBType(final ExecutionEvent event, FBNetwork network, final int sendDataAmount,
			final int readDataAmount, final int xpos, final int ypos) {
		return createCommunicationFBType(event, network, "CLIENT", sendDataAmount, readDataAmount, xpos, ypos);
	}

	public static FBNetworkElement createSubscriberFBType(final ExecutionEvent event, FBNetwork network, final int readDataAmount,
			final int xpos, final int ypos) {
		return createCommunicationFBType(event, network, "SUBSCRIBE", 0, readDataAmount, xpos, ypos);
	}

	public static FBNetworkElement createPublisherFBType(final ExecutionEvent event, FBNetwork network, final int sendDataAmount,
			final int xpos, final int ypos) {
		return createCommunicationFBType(event, network, "PUBLISH", sendDataAmount, 0, xpos, ypos);
	}

	public static FBNetworkElement createCommunicationFBType(final ExecutionEvent event, FBNetwork network, final String fBTypeUpperString,
			final int sendDataAmount, final int readDataAmount, final int xpos, final int ypos) {
		final String templateFBName = fBTypeUpperString + "_1";
		final String FBTypeName = fBTypeUpperString;
		final String FBTypeSDNumber = "_" + sendDataAmount;
		final String FBTypeRDNumber = "_" + readDataAmount;
		final String requiredFbName;
		if (sendDataAmount == readDataAmount && !(fBTypeUpperString.equalsIgnoreCase("PUBLISH") || fBTypeUpperString.equalsIgnoreCase("SUBSCRIBE"))) {
			requiredFbName = FBTypeName + FBTypeSDNumber; // e.g. "SERVER_1"
		} else if (fBTypeUpperString.equalsIgnoreCase("PUBLISH")) {
			requiredFbName = FBTypeName + FBTypeSDNumber; // e.g. "PUBLISH_1"
		} else if (fBTypeUpperString.equalsIgnoreCase("SUBSCRIBE")) {
			requiredFbName = FBTypeName + FBTypeRDNumber; // e.g. "SUBSCRIBE_1"
		}
		else {
			requiredFbName = FBTypeName + FBTypeSDNumber + FBTypeRDNumber; // e.g. "SERVER_4_15"
		}

		final IEditorPart ep = HandlerUtil.getActiveEditor(event);
		if (ep instanceof ApplicationEditor) {
			final ApplicationEditor ae = (ApplicationEditor) ep;
			final EList<FBNetworkElement> nelements = ae.getModel().getNetworkElements();
			FBTypePaletteEntry fbtpe = ae.getModel().getAutomationSystem().getPalette().getFBTypeEntry(requiredFbName);

			if (fbtpe == null) {
				fbtpe = PaletteFactory.eINSTANCE.createFBTypePaletteEntry();
				final IFile fbtFile = ae.getModel().getAutomationSystem().getPalette().getFBTypeEntry(templateFBName)
						.getFile();
				final IPath newFbtPath = new Path(
						fbtFile.getFullPath().toString().replaceAll(templateFBName, requiredFbName));
				try {
					fbtFile.copy(newFbtPath, false, null);
				} catch (final CoreException e) {
					e.printStackTrace();
				}
				fbtpe.setFile(
						ae.getModel().getAutomationSystem().getPalette().getFBTypeEntry(requiredFbName).getFile());
				fbtpe.setLabel(requiredFbName);
				final String[] parsedName = requiredFbName.split("_");
				if (parsedName[0].equals(fBTypeUpperString)) { // $NON-NLS-1$

					ae.getModel().getAutomationSystem().getPalette().getFBTypeEntry(requiredFbName).getFBType()
							.getInterfaceList().getInputVars().remove(2);
					ae.getModel().getAutomationSystem().getPalette().getFBTypeEntry(requiredFbName).getFBType()
							.getInterfaceList().getOutputVars().remove(2);

					if (fBTypeUpperString.equalsIgnoreCase("PUBLISH") || fBTypeUpperString.equalsIgnoreCase("CLIENT")
							|| fBTypeUpperString.equalsIgnoreCase("SERVER")) {
						for (int i = 0; i < sendDataAmount; i++) {
							final VarDeclaration inputData = LibraryElementFactory.eINSTANCE.createVarDeclaration();
							inputData.setType(new DataTypeLibrary().getType(FordiacKeywords.ANY));
							inputData.setTypeName(FordiacKeywords.ANY);
							inputData.setName("SD_" + (i + 1));
							inputData.setArraySize(-1);
							inputData.setIsInput(true);
							ae.getModel().getAutomationSystem().getPalette().getFBTypeEntry(requiredFbName).getFBType()
									.getInterfaceList().getInputVars().add(inputData);
							final With with = LibraryElementFactory.eINSTANCE.createWith();
							with.setVariables(inputData);
							ae.getModel().getAutomationSystem().getPalette().getFBTypeEntry(requiredFbName).getFBType()
									.getInterfaceList().getEventInputs().get(1).getWith().add(with);
							inputData.getWiths().add(with);
						}
					}

					if (fBTypeUpperString.equalsIgnoreCase("SUBSCRIBE") || fBTypeUpperString.equalsIgnoreCase("CLIENT")
							|| fBTypeUpperString.equalsIgnoreCase("SERVER")) {
						for (int i = 0; i < readDataAmount; i++) {
							// for (int i = 0; i < Integer.parseInt(parsedName[2]) - 1; i++) {
							final VarDeclaration outputData = LibraryElementFactory.eINSTANCE.createVarDeclaration();
							outputData.setType(new DataTypeLibrary().getType(FordiacKeywords.ANY));
							outputData.setTypeName(FordiacKeywords.ANY);
							outputData.setName("RD_" + (i + 1));
							outputData.setArraySize(-1);
							ae.getModel().getAutomationSystem().getPalette().getFBTypeEntry(requiredFbName).getFBType()
									.getInterfaceList().getOutputVars().add(outputData);
							final With with = LibraryElementFactory.eINSTANCE.createWith();
							with.setVariables(outputData);
							ae.getModel().getAutomationSystem().getPalette().getFBTypeEntry(requiredFbName).getFBType()
									.getInterfaceList().getEventOutputs().get(1).getWith().add(with);
							outputData.getWiths().add(with);
						}
					}
					
					ae.getModel().getAutomationSystem().getPalette().addPaletteEntry(
							ae.getModel().getAutomationSystem().getPalette().getFBTypeEntry(requiredFbName));
				}
			}
			return createFBType(network, fbtpe.getFBType(), xpos, ypos);
		}
		return null;
	}

	
	private static FBNetworkElement createFBType(FBNetwork network, final FBType fbType, final int xpos,
			final int ypos) {
		final FBCreateCommand fbcmd = new FBCreateCommand((FBTypePaletteEntry) fbType.getPaletteEntry(),
				network, xpos, ypos);
		if (fbcmd.canExecute()) {
			fbcmd.execute();
		}
		return fbcmd.getElement();
	}

}
