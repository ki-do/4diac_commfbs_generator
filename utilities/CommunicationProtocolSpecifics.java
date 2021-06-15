/**
* @author  sieffert
*/
package org.eclipse.fordiac.ide.application.utilities;

import org.eclipse.fordiac.ide.application.editors.FBNetworkEditor;
import org.eclipse.fordiac.ide.model.Palette.FBTypePaletteEntry;
import org.eclipse.fordiac.ide.model.Palette.PaletteFactory;
import org.eclipse.fordiac.ide.model.commands.create.FBCreateCommand;
import org.eclipse.fordiac.ide.model.libraryElement.AdapterConnection;
import org.eclipse.fordiac.ide.model.libraryElement.Connection;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetworkElement;
import org.eclipse.fordiac.ide.model.libraryElement.FBType;
import org.eclipse.fordiac.ide.model.libraryElement.Event;
import org.eclipse.fordiac.ide.model.typelibrary.TypeLibrary;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.emf.common.util.EList;



public interface CommunicationProtocolSpecifics {
	void generateCommunicationComponents(ExecutionEvent event, FBNetworkElement demuxFB_Consumer, FBNetworkElement muxFB_Producer, AdapterConnection adapterConnection);	
}
