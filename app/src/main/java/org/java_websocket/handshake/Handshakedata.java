package org.java_websocket.handshake;

import java.util.Iterator;

public interface Handshakedata {
	Iterator<String> iterateHttpFields();
	String getFieldValue(String name);
	boolean hasFieldValue(String name);
	byte[] getContent();
}
