/**
* @author  sieffert
*/
package org.eclipse.fordiac.ide.application.utilities;

import java.util.ArrayList;

import org.eclipse.fordiac.ide.model.commands.create.DataConnectionCreateCommand;
import org.eclipse.fordiac.ide.model.commands.create.EventConnectionCreateCommand;
import org.eclipse.fordiac.ide.model.libraryElement.Event;
import org.eclipse.fordiac.ide.model.libraryElement.EventConnection;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetwork;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetworkElement;
import org.eclipse.fordiac.ide.model.libraryElement.IInterfaceElement;
import org.eclipse.fordiac.ide.model.libraryElement.VarDeclaration;

public class ApplicationUtilities {
	public static void addDataConnectionToApplication(final FBNetwork fbNetwork, final IInterfaceElement source,
			final IInterfaceElement destination) {
		final DataConnectionCreateCommand dataconn = new DataConnectionCreateCommand(fbNetwork);
		dataconn.setSource(source);
		dataconn.setDestination(destination);
		if (dataconn.canExecute()) {
			dataconn.execute();
		}
	}

	public static void addEventConnectionInApplication(final FBNetwork fbNetwork, final IInterfaceElement source,
			final IInterfaceElement destination) {
		final EventConnectionCreateCommand eventconn = new EventConnectionCreateCommand(fbNetwork);
		eventconn.setSource(source);
		eventconn.setDestination(destination);
		if (eventconn.canExecute()) {
			eventconn.execute();
		}
	}

	public static ArrayList<VarDeclaration> getWithsByEvent(String eventName, FBNetworkElement fbNetworkElement) {
		IInterfaceElement fbInterfaceElement = fbNetworkElement.getInterfaceElement(eventName);

		if (fbInterfaceElement != null) {
			ArrayList<VarDeclaration> varDeclList = new ArrayList<>();
			EventConnection eventCon;
			Event otherEvent;

			if (fbInterfaceElement.isIsInput()) {
				eventCon = (EventConnection) fbInterfaceElement.getInputConnections().get(0);
				otherEvent = (Event) eventCon.getSource();

			} else {
				eventCon = (EventConnection) fbInterfaceElement.getOutputConnections().get(0);
				otherEvent = (Event) eventCon.getDestination();
			}
			
			for (int i = 0; i < otherEvent.getWith().size(); i++) {
				varDeclList.add(otherEvent.getWith().get(i).getVariables());
			}
			
			return varDeclList;

		}
		return null;
	}

}
