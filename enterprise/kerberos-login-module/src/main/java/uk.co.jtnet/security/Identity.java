package uk.co.jtnet.security;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Identity {

	private String username;
	private String realm;
	private String displayName;
	private Date authenticationDateTime;
	private List<String> groups = new ArrayList<String>();
	private List<String> roles = new ArrayList<String>();
	private Map<String, Object> attributes = new HashMap<String, Object>();

	public Identity(){

	}
	public Identity(String username){
		this.username = username;
	}
	public Identity(String username, String realm){
		this.username = username;
		this.realm = realm;
	}

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getRealm() {
		return realm;
	}
	public void setRealm(String realm) {
		this.realm = realm;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public Date getAuthenticationDateTime(){
		return authenticationDateTime;
	}
	public void setAuthenticationDateTime(Date authenticationDateTime){
		this.authenticationDateTime = authenticationDateTime;
	}
	public List<String> getGroups() {
		return groups;
	}
	public List<String> getRoles() {
		return roles;
	}
	public Map<String, Object> getAttributes() {
		return attributes;
	}
	public Object getAttribute(String key){
		return attributes.get(key);
	}
	public void addRole(String role){
		roles.add(role);
	}
	public void addGroup(String group){
		groups.add(group);
	}
	public void addAttribute(String key, Object value){
		attributes.put(key, value);
	}
	public void addAllAttributes(Map<String,Object> attributesToAdd){
		attributes.putAll(attributesToAdd);
	}
	public void removeRole(String role){
		roles.remove(role);
	}
	public void removeGroup(String group){
		groups.remove(group);
	}
	public void removeAttribute(String key){
		attributes.remove(key);
	}
	public String toString(){
		String identityStr = String.format("Username: %s ; Realm: %s ; Display name: %s ; ", 
				username,
				realm,
				displayName,
				authenticationDateTime.toString());
		for (Entry<String, Object> attribute : attributes.entrySet()) {
		    identityStr = identityStr + String.format("Attribute - %s: %s ; ", attribute.getKey(), attribute.getValue().toString());
		}
		identityStr = identityStr + " Groups: ";
		for (String group : groups){
			identityStr = identityStr + group + ", ";
		}
		identityStr = identityStr + " Roles: ";
		for (String role : roles){
			identityStr = identityStr + role + ", ";
		}
		return identityStr;
	}
}
