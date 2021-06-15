/**
* @author  sieffert
* @author  dorofeev
*/
package org.eclipse.fordiac.ide.application.utilities;

import org.eclipse.fordiac.ide.model.commands.change.MapToCommand;
import org.eclipse.fordiac.ide.model.libraryElement.AdapterDeclaration;
import org.eclipse.fordiac.ide.model.libraryElement.Event;
import org.eclipse.fordiac.ide.model.libraryElement.FBNetworkElement;
import org.eclipse.fordiac.ide.model.libraryElement.LibraryElementFactory;
import org.eclipse.fordiac.ide.model.libraryElement.Resource;
import org.eclipse.fordiac.ide.model.libraryElement.VarDeclaration;

public class CloneUtilities {

	public static Event cloneEvent(final Event event, final boolean inverseInAndOutput) {
		final Event eventClone = LibraryElementFactory.eINSTANCE.createEvent();
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

	public static VarDeclaration cloneVarDeclaration(final VarDeclaration data, final boolean inverseInAndOutput) {
		final VarDeclaration dataClone = LibraryElementFactory.eINSTANCE.createVarDeclaration();
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

	public static AdapterDeclaration cloneAdapterDeclaration(final AdapterDeclaration adapterDeclaration) {
		final AdapterDeclaration adapterDeclarationClone = LibraryElementFactory.eINSTANCE.createAdapterDeclaration();
		adapterDeclarationClone.setArraySize(adapterDeclaration.getArraySize());
		adapterDeclarationClone.setComment(adapterDeclaration.getComment());
		adapterDeclarationClone.setName(adapterDeclaration.getName());
		adapterDeclarationClone.setPaletteEntry(adapterDeclaration.getPaletteEntry());
		adapterDeclarationClone.setType(adapterDeclaration.getType());
		adapterDeclarationClone.setTypeName(adapterDeclaration.getTypeName());
		adapterDeclarationClone.setValue(adapterDeclaration.getValue());

		adapterDeclarationClone.setIsInput(!adapterDeclaration.isIsInput());


		return adapterDeclarationClone;

	}

	/*public static Mapping cloneMapping(final Mapping mapping) {
		final Mapping mappingClone = LibraryElementFactory.eINSTANCE.createMapping();
		mappingClone.setFrom(mapping.getFrom());
		mappingClone.setTo(mapping.getTo());

		return mappingClone;
	}
	 */

	public static void setMapping(final FBNetworkElement fbne, final Resource resource) {
		//TODO: 31.03.21 um keine Exception zu bekommen
		//final MapToCommand cmd = new MapToCommand(fbne, resource);
		//if (cmd.canExecute()) {
		//	cmd.execute();
		//}
	}


}
