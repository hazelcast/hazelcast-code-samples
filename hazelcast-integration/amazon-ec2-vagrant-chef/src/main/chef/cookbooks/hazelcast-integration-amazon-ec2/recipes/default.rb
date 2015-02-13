#
# Cookbook Name:: hazelcast-integration-amazon-ec2
# Recipe:: default
#
# Apache License, Version 2.0

hazelcast_currentdir = node['hazelcast']['current_dir']

include_recipe 'java'

directory "#{hazelcast_currentdir}"

# Replaces environment specific settings in hazelcast.xml
template "#{hazelcast_currentdir}/hazelcast.xml" do
  source "hazelcast.xml.erb"
  notifies :reload, 'service[hazelcast]', :immediately
end

# Hazelcast Service Configuration
template "/etc/init/hazelcast.conf" do
  source "hazelcast.conf.erb"
  mode "0755"
  owner "root"
  group "root"
  notifies :reload, 'service[hazelcast]', :immediately
end

# Creates the Hazelcast Service
service 'hazelcast' do
  supports :status => true, :restart => true, :reload => true
  provider Chef::Provider::Service::Upstart if platform?("ubuntu")
  start_command "/usr/bin/service hazelcast start" if platform?("ubuntu")
  action [:enable, :start]
end
