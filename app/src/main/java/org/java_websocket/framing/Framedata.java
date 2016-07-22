package org.java_websocket.framing;

import java.nio.ByteBuffer;

import org.java_websocket.exceptions.InvalidFrameException;

public interface Framedata {
	enum Opcode {
		CONTINUOUS, TEXT, BINARY, PING, PONG, CLOSING
		// more to come
	}
	boolean isFin();
	boolean getTransfereMasked();
	Opcode getOpcode();
	ByteBuffer getPayloadData();// TODO the separation of the application data and the extension data is yet to be done
	void append(Framedata nextframe) throws InvalidFrameException;
}
