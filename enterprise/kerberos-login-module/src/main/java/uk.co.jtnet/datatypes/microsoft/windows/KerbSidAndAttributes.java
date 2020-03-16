package uk.co.jtnet.datatypes.microsoft.windows;

public class KerbSidAndAttributes {

	private long rid;
	private int attributes;

	public KerbSidAndAttributes(long rid, int attributes){
		this.rid = rid;
		this.attributes = attributes;
	}

	public long getRid() {
		return rid;
	}

	public int getAttributes() {
		return attributes;
	}



}
