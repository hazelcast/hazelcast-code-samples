package uk.co.jtnet.security.kerberos.pac;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

import javax.security.auth.Subject;

import uk.co.jtnet.datatypes.microsoft.windows.FileTime;

public class PacClientInfo extends PacInfoBuffer {

	/*The PAC_CLIENT_INFO structure is a variable length buffer of the PAC that contains the client's
	name and authentication time. It is used to verify that the PAC corresponds to the client of the
	ticket.*/

	private Date tgtAuthenticationTime;  //This is the ClientId in the standards documentation
	private short nameLengthBytes; 
	private String clientAccountName;

	@Override
	public void process(int pacInfoBufferType, byte[] pacInfoBufferBytes) throws IOException {
		super.process(pacInfoBufferType, pacInfoBufferBytes);
		ByteBuffer pacDataStream = ByteBuffer.wrap(pacInfoBufferBytes);
		pacDataStream.order(ByteOrder.LITTLE_ENDIAN);

		this.tgtAuthenticationTime = readFileTime(pacDataStream);
		this.nameLengthBytes = pacDataStream.getShort();
		int nameLengthChars = nameLengthBytes / 2;
		//Each char is Little Endian encoded.
		char[] charArray = new char[nameLengthChars];
		for (int i = 0; i < nameLengthChars; i++) {
			charArray[i] = pacDataStream.getChar();
		}
		this.clientAccountName = new String(charArray);
	}

	private Date readFileTime(ByteBuffer pacDataStream) throws IOException {
		int lowOrder = pacDataStream.getInt();
		int highOrder = pacDataStream.getInt();

		FileTime fileTime = new FileTime(lowOrder, highOrder);
		return fileTime.getDate();

	}

	public Date getTgtAuthenticationTime() {
		return tgtAuthenticationTime;
	}

	public String getClientAccountName() {
		return clientAccountName;
	}

}
