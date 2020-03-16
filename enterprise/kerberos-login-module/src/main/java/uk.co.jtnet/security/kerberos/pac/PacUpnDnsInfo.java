package uk.co.jtnet.security.kerberos.pac;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.security.auth.Subject;

public class PacUpnDnsInfo extends PacInfoBuffer {

	/*The UPN_DNS_INFO structure contains the client's UPN (User Principal Name) and fully qualified domain name
	(FQDN). It is used to provide the UPN and FQDN that corresponds to the client of the ticket.*/

	int upnLength;
	short upnOffset;
	int dnsDomainNameLength;
	short dnsDomainNameOffset;
	byte[] flags = new byte[4];
	String upn;
	String dnsDomainName;

	@Override
	public void process(int pacInfoBufferType, byte[] pacInfoBufferBytes) throws IOException {
		super.process(pacInfoBufferType, pacInfoBufferBytes);
		ByteBuffer pacDataStream = ByteBuffer.wrap(pacInfoBufferBytes);
		pacDataStream.order(ByteOrder.LITTLE_ENDIAN);

		short upnLengthByteCount = pacDataStream.getShort();
		this.upnLength = upnLengthByteCount / 2;
		this.upnOffset = pacDataStream.getShort();
		short dnsDomainNameLengthByteCount = pacDataStream.getShort();
		this.dnsDomainNameLength = dnsDomainNameLengthByteCount / 2;
		this.dnsDomainNameOffset = pacDataStream.getShort();
		pacDataStream.get(this.flags);

		pacDataStream.position(upnOffset);
		this.upn = getStringOfLittleEndianCharsInByteBuffer(pacDataStream, upnLengthByteCount);

		pacDataStream.position(dnsDomainNameOffset);
		this.dnsDomainName = getStringOfLittleEndianCharsInByteBuffer(pacDataStream, dnsDomainNameLengthByteCount);
	}

	private String getStringOfLittleEndianCharsInByteBuffer(ByteBuffer buffer, int lengthByteCount){
		int lengthCharsCount = lengthByteCount / 2;
		//Each char is Little Endian encoded.
		char[] charArray = new char[lengthCharsCount];
		for (int i = 0; i < lengthCharsCount; i++) {
			charArray[i] = buffer.getChar();
		}
		return new String(charArray);
	}

	public int getUpnLength() {
		return upnLength;
	}

	public int getDnsDomainNameLength() {
		return dnsDomainNameLength;
	}

	public byte[] getFlags() {
		return flags;
	}

	public String getUpn() {
		return upn;
	}

	public String getDnsDomainName() {
		return dnsDomainName;
	}


}
