package uk.co.jtnet.datatypes.microsoft.windows;

import java.util.ArrayList;

public class RpcSid {

	//Reference: https://msdn.microsoft.com/en-us/library/cc230371.aspx
	//S-1-IdentifierAuthority-SubAuthority1-SubAuthority2-...-SubAuthorityn

	private byte revision;
	private byte subAuthorityCount;
	private RpcSidIdentifierAuthority identifierAuthority;
	private ArrayList<Long> subAuthorities = new ArrayList<Long>();  //need to use an ArrayList as order is important

	public RpcSid(){
		this.subAuthorityCount = (byte)0;
	}

	public RpcSid(byte revision){
		this.revision = revision;
		this.subAuthorityCount = (byte)0;
	}

	public RpcSid(byte revision, RpcSidIdentifierAuthority identifierAuthority, ArrayList<Long> subAuthorities){
		this.revision = revision;
		this.subAuthorityCount = (byte)subAuthorities.size();
		this.identifierAuthority = identifierAuthority;
		this.subAuthorities = subAuthorities;
	}



	/*The SID string format syntax, a format commonly used for a string representation of the SID type (as specified in section 2.4.2), is described by the following ABNF syntax, as specified in [RFC5234].
	SID= "S-1-" IdentifierAuthority 1*SubAuthority
	IdentifierAuthority= IdentifierAuthorityDec / IdentifierAuthorityHex
	  ; If the identifier authority is < 2^32, the
	  ; identifier authority is represented as a decimal 
	  ; number
	  ; If the identifier authority is >= 2^32,
	  ; the identifier authority is represented in 
	  ; hexadecimal
	IdentifierAuthorityDec =  1*10DIGIT
	  ; IdentifierAuthorityDec, top level authority of a 
	  ; security identifier is represented as a decimal number
	IdentifierAuthorityHex = "0x" 12HEXDIG
	  ; IdentifierAuthorityHex, the top-level authority of a
	  ; security identifier is represented as a hexadecimal number
	SubAuthority= "-" 1*10DIGIT
	  ; Sub-Authority is always represented as a decimal number 
	  ; No leading "0" characters are allowed when IdentifierAuthority
	  ; or SubAuthority is represented as a decimal number
	  ; All hexadecimal digits must be output in string format,
	  ; pre-pended by "0x"*/
	public String toString(){
		StringBuilder sb = new StringBuilder("S-");
		sb.append(revision);
		sb.append("-");
		sb.append(identifierAuthority.toString());
		for (long subAuthority: subAuthorities){
			sb.append("-");
			sb.append(subAuthority);
		}
		return sb.toString();
	}

	public void appendSubAuthorityList(ArrayList<Long> subAuthorityList){
		this.subAuthorities.addAll(subAuthorityList);
	}

	public void addSubAuthority(long subAuthorityRid){
		this.subAuthorities.add(subAuthorityRid);
		this.subAuthorityCount++;
	}

	public void setIdentifierAuthority(RpcSidIdentifierAuthority identifierAuthority){
		this.identifierAuthority = identifierAuthority;
	}

	public byte getRevision() {
		return revision;
	}

	public RpcSidIdentifierAuthority getIdentifierAuthority() {
		return identifierAuthority;
	}

	public ArrayList<Long> getSubAuthorities() {
		return subAuthorities;
	}

}
