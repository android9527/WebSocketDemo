package org.java_websocket.framing;

import java.nio.ByteBuffer;

import org.java_websocket.exceptions.InvalidDataException;

public interface FrameBuilder extends Framedata {

	void setFin(boolean fin);

	void setOptcode(Opcode optcode);

	void setPayload(ByteBuffer payload) throws InvalidDataException;

	void setTransferemasked(boolean transferemasked);

}