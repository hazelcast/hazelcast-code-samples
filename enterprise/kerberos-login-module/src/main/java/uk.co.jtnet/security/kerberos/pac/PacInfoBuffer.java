package uk.co.jtnet.security.kerberos.pac;

import java.io.IOException;

public abstract class PacInfoBuffer {

	private int pacInfoBufferType;
	private byte[] pacInfoBufferBytes;
	
	public PacInfoBuffer() {}
	
	public PacInfoBuffer(int pacInfoBufferType, byte[] pacInfoBufferBytes) throws IOException {
		process(pacInfoBufferType, pacInfoBufferBytes);
	}

	public void process(int pacInfoBufferType, byte[] pacInfoBufferBytes) throws IOException {
		this.pacInfoBufferType = pacInfoBufferType;
		this.pacInfoBufferBytes = pacInfoBufferBytes;
	}

	public int getPacInfoBufferType() {
		return pacInfoBufferType;
	}
	
	public byte[] getPacInfoBufferBytes() {
		return pacInfoBufferBytes;
	}

}
