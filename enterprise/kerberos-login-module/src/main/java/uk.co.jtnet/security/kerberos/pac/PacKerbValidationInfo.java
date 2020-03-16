package uk.co.jtnet.security.kerberos.pac;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.security.auth.Subject;

import uk.co.jtnet.datatypes.microsoft.windows.KerbSidAndAttributes;
import uk.co.jtnet.datatypes.microsoft.windows.RpcSid;
import uk.co.jtnet.datatypes.microsoft.windows.RpcUnicodeString;
import uk.co.jtnet.encoding.ndr.MsRpceNdrStream;

public class PacKerbValidationInfo extends PacInfoBuffer {

	/*The KERB_VALIDATION_INFO structure defines the user's logon and authorization information
	provided by the DC. The KERB_VALIDATION_INFO structure is a subset of the
	NETLOGON_VALIDATION_SAM_INFO4 structure ([MS-NRPC] section 2.2.1.4.13). It is a subset
	due to historical reasons and to the use of the common Active Directory to generate this
	information.
	The KERB_VALIDATION_INFO structure is marshaled by RPC [MS-RPCE].*/
	//Ref: MS-PAC.pdf section 2.5 (https://msdn.microsoft.com/en-us/library/cc237948.aspx)

	private Date logonTime;
	private Date logoffTime;
	private Date kickOffTime;
	private Date passwordLastSetTime;
	private Date passwordCanChangeTime;
	private Date passwordMustChangeTime;
	private RpcUnicodeString effectiveName;  //This is the samAccountName
	private RpcUnicodeString fullName;
	private RpcUnicodeString logonScript;
	private RpcUnicodeString profilePath;
	private RpcUnicodeString homeDirectory;
	private RpcUnicodeString homeDrive;
	private short logonCount;
	private short badPasswordCount;
	private long userId; //This is the user's sid.
	private long primaryGroupId; //Is this a sid?
	private long groupCount;
	private long groupMembershipPointer; //A pointer to a list of GROUP_MEMBERSHIP //May need a custom type for this
	private long userFlags;
	// private String userSessionKey; //A session key that is used for cryptographic operations on a session. This field is valid only when authentication is performed using NTLM. For any other protocol, this field MUST be zero. - We will therefore ignore this.
	private RpcUnicodeString logonServer;
	private RpcUnicodeString logonDomainName;
	private long logonDomainIdPointer; //Is this a sid?
	private long userAccountControl;
	private long subAuthStatus;
	private Date lastSuccessfullLogon;
	private Date lastFailedLogon;
	private long failedLogonCount;
	private long sidCount;
	private long extraSidsPointer;
	private long resourceGroupDomainSidPointer; //Is this a sid?
	private long resourceGroupCount;
	private long resourceGroupIdPointer; //May need a custom type for this

	private RpcSid domainSid;
	private RpcSid[] groupMembershipSids;
	
	@Override
	public void process(int pacInfoBufferType, byte[] pacInfoBufferBytes) throws IOException {
		super.process(pacInfoBufferType, pacInfoBufferBytes);
		MsRpceNdrStream pacDataStream = new MsRpceNdrStream(pacInfoBufferBytes);
		pacDataStream.initializeStream();

		//Order of the following is important to get it off the byte stream for the correct field. The pacDataStream has already been initialised to be at the start of the data, after the RPC headers.

		// The first 4 bytes at the start of the KERB_VALIDATION_INFO is an RPC unique pointer referent which we can skip.		 *
		pacDataStream.shiftPosition(4);

		//First 6 fields are FILETIME structures.
		this.logonTime = pacDataStream.readFileTime();
		this.logoffTime = pacDataStream.readFileTime();
		this.kickOffTime = pacDataStream.readFileTime();
		this.passwordLastSetTime = pacDataStream.readFileTime();
		this.passwordCanChangeTime = pacDataStream.readFileTime();
		this.passwordMustChangeTime = pacDataStream.readFileTime();

		//The next 6 fields are RPC_UNICODE_STRING structures. These contain the length of the string and a pointer to the string data. This is found after the main body of the KERB_VALIDATION_INFO.
		this.effectiveName = pacDataStream.readRpcUnicodeString();
		this.fullName = pacDataStream.readRpcUnicodeString();
		this.logonScript = pacDataStream.readRpcUnicodeString();
		this.profilePath = pacDataStream.readRpcUnicodeString();
		this.homeDirectory = pacDataStream.readRpcUnicodeString();
		this.homeDrive = pacDataStream.readRpcUnicodeString();
		this.logonCount = pacDataStream.readShort();
		this.badPasswordCount = pacDataStream.readShort();

		//!! Would be better to have a SID object for this userID
		this.userId = pacDataStream.readUnsignedInt();
		this.primaryGroupId = pacDataStream.readUnsignedInt();
		this.groupCount = pacDataStream.readUnsignedInt();
		this.groupMembershipPointer = pacDataStream.readUnsignedInt();
		this.userFlags = pacDataStream.readUnsignedInt();
		//Skip the userSessionkey as this is only non-zero when using NTLM
		pacDataStream.shiftPosition(16);
		this.logonServer = pacDataStream.readRpcUnicodeString();
		this.logonDomainName = pacDataStream.readRpcUnicodeString();
		this.logonDomainIdPointer = pacDataStream.readInt();
		//Skip next 8 bytes: Reserved1: A two-element array of unsigned 32-bit integers. This member is reserved, and each element of the array MUST be zero when sent and MUST be ignored on receipt.
		pacDataStream.shiftPosition(8);
		this.userAccountControl = pacDataStream.readUnsignedInt();
		this.subAuthStatus = pacDataStream.readUnsignedInt();
		this.lastSuccessfullLogon = pacDataStream.readFileTime();
		this.lastFailedLogon = pacDataStream.readFileTime();
		this.failedLogonCount = pacDataStream.readUnsignedInt();
		//Skip next 4 bytes: Reserved3: A 32-bit integer. This member is reserved, and MUST be zero when sent and MUST be ignored on receipt.
		pacDataStream.shiftPosition(4);
		this.sidCount = pacDataStream.readUnsignedInt();
		this.extraSidsPointer = pacDataStream.readUnsignedInt();
		this.resourceGroupDomainSidPointer = pacDataStream.readUnsignedInt();
		this.resourceGroupCount = pacDataStream.readUnsignedInt();
		this.resourceGroupIdPointer = pacDataStream.readUnsignedInt();

		/* All fields are now loaded from the stream.
		 * The remaining data is that referenced by the various pointers.
		 */

		//We need to get the strings in the order they appear
		this.effectiveName.deserializeString(pacDataStream);
		this.fullName.deserializeString(pacDataStream);
		this.logonScript.deserializeString(pacDataStream);
		this.profilePath.deserializeString(pacDataStream);
		this.homeDirectory.deserializeString(pacDataStream);
		this.homeDrive.deserializeString(pacDataStream);

		// Read the sids for the AD groups
		KerbSidAndAttributes[] groupMembershipRids = new KerbSidAndAttributes[0];
		this.groupMembershipSids = new RpcSid[0];
		if(groupMembershipPointer != 0) {
			int actualGroupCount = pacDataStream.readInt();
			if (groupCount != actualGroupCount) {
				throw new IOException("GroupCount does not equal actual serialized Group toByteArray count");
			}
			groupMembershipRids = new KerbSidAndAttributes[actualGroupCount];
			this.groupMembershipSids = new RpcSid[actualGroupCount];
			for(int i = 0; i < groupCount; i++) {
				groupMembershipRids[i] = pacDataStream.readKerbSidAndAttributes();
			}
		}

		logonServer.deserializeString(pacDataStream);
		logonDomainName.deserializeString(pacDataStream);

		if (logonDomainIdPointer != 0){
			domainSid = pacDataStream.readRpcSid();
			for (int i = 0; i < groupMembershipRids.length; i++){
				ArrayList<Long> subAuthorities = new ArrayList<Long>(domainSid.getSubAuthorities());
				subAuthorities.add(groupMembershipRids[i].getRid());
				this.groupMembershipSids[i] = new RpcSid(domainSid.getRevision(), domainSid.getIdentifierAuthority(), subAuthorities);
			}

		}
	}
	
	public Date getLogonTime() {
		return logonTime;
	}


	public Date getLogoffTime() {
		return logoffTime;
	}


	public Date getKickOffTime() {
		return kickOffTime;
	}


	public Date getPasswordLastSetTime() {
		return passwordLastSetTime;
	}


	public Date getPasswordCanChangeTime() {
		return passwordCanChangeTime;
	}


	public Date getPasswordMustChangeTime() {
		return passwordMustChangeTime;
	}


	public String getEffectiveName() {
		return effectiveName.getStringValue();
	}


	public String getFullName() {
		return fullName.getStringValue();
	}


	public String getLogonScript() {
		return logonScript.getStringValue();
	}


	public String getProfilePath() {
		return profilePath.getStringValue();
	}


	public String getHomeDirectory() {
		return homeDirectory.getStringValue();
	}


	public String getHomeDrive() {
		return homeDrive.getStringValue();
	}


	public short getLogonCount() {
		return logonCount;
	}


	public short getBadPasswordCount() {
		return badPasswordCount;
	}


	public long getUserId() {
		return userId;
	}


	public long getPrimaryGroupId() {
		return primaryGroupId;
	}


	public long getGroupCount() {
		return groupCount;
	}


	public long getUserFlags() {
		return userFlags;
	}


	public String getLogonServer() {
		return logonServer.getStringValue();
	}


	public String getLogonDomainName() {
		return logonDomainName.getStringValue();
	}


	public long getUserAccountControl() {
		return userAccountControl;
	}


	public long getSubAuthStatus() {
		return subAuthStatus;
	}


	public Date getLastSuccessfullLogon() {
		return lastSuccessfullLogon;
	}


	public Date getLastFailedLogon() {
		return lastFailedLogon;
	}


	public long getFailedLogonCount() {
		return failedLogonCount;
	}


	public long getSidCount() {
		return sidCount;
	}


	public long getResourceGroupCount() {
		return resourceGroupCount;
	}


	public RpcSid getDomainSid() {
		return domainSid;
	}


	public RpcSid[] getGroupMembershipSids() {
		return groupMembershipSids;
	}

}
