package uk.co.jtnet.security.kerberos.pac;

public interface PacConstants {

	static final int PAC_VERSION = 0;
	
	static final int LOGON_INFO = 1;
	static final int KERB_VALIDATION_INFO = 1;
	static final int PAC_CREDENTIALS = 2;
    static final int SERVER_CHECKSUM = 6;
    static final int KDC_PRIVSERVER_CHECKSUM = 7;
    static final int PAC_CLIENT_INFO = 10;
    static final int CONSTRAINED_DELEGATION_INFO = 11;
    static final int UPN_DNS_INFO = 12;
    static final int PAC_CLIENT_CLAIMS_INFO = 13;
    static final int PAC_DEVICE_INFO = 14;
    static final int PAC_DEVICE_CLAIMS_INFO = 15;
    
    static final int KERB_NON_KERB_SALT = 16;
    static final int KERB_NON_KERB_CKSUM_SALT = 17;
    
	
}
