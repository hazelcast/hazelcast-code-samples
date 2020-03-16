package uk.co.jtnet.security.kerberos;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;

public class DynamicJaasConfiguration extends Configuration {

	Map<String, AppConfigurationEntry[]> AppConfigEntries = new HashMap<String, AppConfigurationEntry[]>();

	void init() {
		Configuration.setConfiguration(this);
	}

	DynamicJaasConfiguration(){
		super();
	}

	public void addAppConfigEntry(String loginModuleName, String loginClass, LoginModuleControlFlag controlFlag, Map<String, ?> options){
		AppConfigurationEntry[] jaasConfigEntry = new AppConfigurationEntry[1];
		jaasConfigEntry[0] = new AppConfigurationEntry(loginClass, controlFlag, options);
		AppConfigEntries.put(loginModuleName, jaasConfigEntry);
	}

	@Override
	public AppConfigurationEntry[] getAppConfigurationEntry(String loginModuleName) {
		return AppConfigEntries.get(loginModuleName);
	}

}
