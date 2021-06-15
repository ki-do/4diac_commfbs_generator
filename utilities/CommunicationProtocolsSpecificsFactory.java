/**
* @author  sieffert
*/
package org.eclipse.fordiac.ide.application.utilities;

public final class CommunicationProtocolsSpecificsFactory {

	public static CommunicationProtocolSpecifics getCommunicationProtocolSpecifics(String communicationProtocol) {
		if (communicationProtocol == null) {
			return null;
		} else if (communicationProtocol.equalsIgnoreCase("OPCUA")) {
			return new OpcUaCommunicationSpecifics();
		} else if (communicationProtocol.equalsIgnoreCase("MQTT")) {
			return null;
		} else if (communicationProtocol.equalsIgnoreCase("Other")) {
			return null;
		}
		return null;
	}
}
