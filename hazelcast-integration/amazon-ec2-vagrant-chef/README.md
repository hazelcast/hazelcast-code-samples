# Amazon EC2 Hazelcast

## Introduction

This project allows you to:

- deploy a Hazelcast project onto Amazon EC2 using third party tools such as [Vagrant](https://www.vagrantup.com/) and [Chef](https://www.chef.io/chef/), and

- demonstrate Hazelcast's auto discovery mechanisms for client connections and cluster formation.

## Assumptions

This project is not intended as a beginners guide to Amazon EC2, Vagrant, or Chef. If you are new to these tools, please familiarize yourself with them before proceeding.

### Amazon EC2

You should have prior knowledge of Amazon EC2; for example, you should know how to set up your own instances, maybe via the EC2 control panel.

You can try Amazon EC2 by running through the following exercise [EC2 Get Started](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/EC2_GetStarted.html).

### Vagrant

Vagrant is a command line tool that lets you start up virtual machines either on your own desktop (using VirtualBox) or via another provider such as Amazon EC2. Vagrant also takes care of running provisioners, such as Chef. It simplifies mundane tasks such as connecting to the machines once they are up.

### Chef

Chef installs software on the newly created virtual machine. The instructions to do this are created in files called recipes.

## Project Structure

The project follows a standard java quickstart maven layout with some additional directories

`/src/main/vagrant` - contains the Vagranfile used for providing the linux virtual machines upon which the hazelcast project will be installed. These linux virtual machines can use either VirtualBox or Amazon EC2.

`/src/main/chef` - contains the Chef cookbook that installs the sample application.

`/src/main/java` - contains some simple java code for a Hazelcast Cluster Node and also a Hazelcast Client that connects into an Amazon EC2 Hazelcast Cluster

## Required Software

1. Install [VirtualBox](https://www.virtualbox.org/) this is the default Vagrant Provider, you'll use this to test out deployments and creating Virtual Machines on your own desktop prior to running out on Amazon EC2.

2. Install the [Chef Developers Kit](https://downloads.chef.io/chef-dk/) for your platform.  This will provide a set of command line tools, please ensure you are running at least version 0.3.5

3. Install [Vagrant](https://www.vagrantup.com/downloads) for your platform, please ensure you are running at least version 1.7.1

4. Install [Vagrant Plug-ins](https://docs.vagrantup.com/v2/plugins/usage.html) required for the Vagrant script to run.  Try to ensure you at least have the following installed...

* vagrant-aws (0.6.0)
* vagrant-berkshelf (4.0.1)
* vagrant-omnibus (1.4.1)
* vagrant-share (1.1.4, system)

For example to install vagrant-aws plugin you'll need to run on the command line

`vagrant plugin install vagrant-aws`

Additionally for the AWS provider to work you'll need to add a dummy box to Vagrant

`vagrant box add dummy https://github.com/mitchellh/vagrant-aws/raw/master/dummy.box`
## Build the Java Binaries

Before we can deploy anything we need to build the java project and generate the jar file with dependencies.  This jar file will contain the Hazelcast server and client classes along with dependent jars, in this case the Hazelcast jar.

`mvn assembly:assembly` at the project root.

Once this is built we will have a `/target` directory that should contain `amazon-ec2-0.1-SNAPSHOT-jar-with-dependencies.jar`

## First Deployment (Virtual Box as a provider)

To begin with lets validate that everything is installed correctly.  If it is, we should be able to start up a simple hazelcast cluster running on some Ubuntu virtual machines running on your own desktop.  We'll use Vagrants default platform provider, VirtualBox to do this.

Take a look at the [VagrantFile](./src/main/vagrant/Vagrantfile) within `/src/main/vagrant`

You should see the following block of configuration that tells Vagrant to set-up an Ubuntu instance and then to run Chef Solo. 

```ruby
# VirtualBox Provider

config.vm.provider :virtualbox do |vb, override|

    override.vm.box = "ubuntu/trusty64"

    # Make sure you put your own network interface name in here
    override.vm.network :public_network, bridge: "en3: Thunderbolt Ethernet"

    override.vm.provision "chef_solo" do |chef|
      chef.cookbooks_path = CHEF_COOKBOOKS_PATH
      chef.add_recipe "hazelcast-integration-amazon-ec2"
    end
end
```

This is the default provider that vagrant will run and you can see we are doing three things in this block of code.

1. Create a virtual machine based on "ubuntu/trusty64"

2. When starting the Virtual Machine create a bridged network, remember you'll have to change the network interface name to something that matches on your system replacing "en3: Thunderbolt Ethernet"

3. Once the machine is running it will execute Chef Solo and run the "hazelcast-integration-amazon-ec2" recipe.  This recipe is responsible for installing Java on this machine and then installing our Hazelcast project as a service.

One other important line of code in the VagrantFile, which is right at the top
`NUM_BOXES=2`
This configures the number of virtual machines we will create, lets leave it at 2 for now.

## How do we get the binaries onto the virtual machines?

As we're experimenting with development workflows from the desktop we need a simple and quick way to deploy our binaries to the virtual machines.

As workflow progresses to UAT/QA and ultimately production you would amend your CHEF recipies to download your developed code from a Maven repository.

But for our purposes we can simply mount the `/target` directory onto the virtual machines.  You'll see we achieve that in our VagrantFile where we mount `target` to a directory on the virtual machines called `/mnt`

```ruby
# Attach Local Release Directory to Virtual Machines
  config.vm.synced_folder "../../../target/","/mnt/hazelcast-integration-amazon-ec2/target"
```

## What is the Chef recipe doing?

You'll have noted that once Vagrant has created the virtual machines it installs Chef on those machines using the Omnibus plug-in and then executes the Chef recipe called `hazelcast-integration-amazon-ec2`.  Let's take a look at that code now, you'll find the source for that in a file called [default.rb](./src/main/chef/cookbooks/hazelcast-integration-amazon-ec2/recipes/default.rb) under `/src/main/chef/cookbooks/hazelcast-integration-amazon-ec2/recipes`

```ruby
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
```

There are five steps in this Chef Recipe that we should examine.

1. Including other recipes, we know we need Java installed on our fresh Ubuntu box to run Hazelcast, so we `include-recipe 'java'`

2. We create any directories that we need using the `directory` command

3. Now we come to a really handy feature, templating.  What we're doing here is to take the [hazelcast.xml.erb](./src/main/chef/cookbooks/hazelcast-integration-amazon-ec2/templates/default) file which is under `/src/main/chef/cookbooks/hazelcast-integration-amazon-ec2/templates/default`,Chef then replaces the placeholders in this file firstly with any defaults found in the [default.rb](./src/main/chef/cookbooks/hazelcast-integration-amazon-ec2/attributes/default.rb) which is under `src/main/chef/cookbooks/hazelcast-integration-amazon-ec2/attributes`.  Also these defaults can be overridden and you'll see we do that later on in the Vagrant script when we use the chef_json attribute to set Amazon network settings for Hazelcast.  Finally it places this file in the following directory on the virtual machine `#{hazelcast_currentdir}/hazelcast.xml`

4. We use templating again when we create the hazelcast.conf file which defines the Hazelcast Upstart Service.

5. Lastly we create and start the upstart service.


### Vagrant ARISE !

Now we're ready to fire up our environment.

Within the vagrant directory where you have the VagrantFile simply type

`vagrant up`

If all goes to plan, you should see vagrant begin to provide the virtual machines and then Chef will run on this machine and install Java and finally our Hazelcast service.

### Check your virtual machines

When the command line returns we can use vagrant to ssh into the boxes.  This is one of the areas that Vagrant really makes easy, you do not have to know any IP addresses or keys to connect. You simply type

`vagrant ssh <box name>`

So in our case this would be either 'hazelcast1' or 'hazelcast2' if we created the 2 machines.

You'll recall that the Chef recipe firstly installed Java and then it installed a service which runs from our java code.  Lets check that's all started up correctly, so once you've ssh to one of the machines...

`cd /var/log/upstart`

This is where the `hazelcast.log` file will be if the service started correctly.  When you examine this log file make sure you `sudo` as it is owned by root.  If you don't you'll just be presented with an empty file.  

If all went to plan you should see something like this...

```
Dec 31, 2014 2:53:21 PM com.hazelcast.config.XmlConfigLocator
INFO: Loading configuration /opt/hazelcast/hazelcast.xml from System property 'hazelcast.config'
Dec 31, 2014 2:53:21 PM com.hazelcast.config.XmlConfigLocator
INFO: Using configuration file at /opt/hazelcast/hazelcast.xml
Dec 31, 2014 2:53:21 PM com.hazelcast.instance.DefaultAddressPicker
INFO: [LOCAL] [dev] [3.4-RC1-SNAPSHOT] Prefer IPv4 stack is true.
Dec 31, 2014 2:53:21 PM com.hazelcast.instance.DefaultAddressPicker
INFO: [LOCAL] [dev] [3.4-RC1-SNAPSHOT] Picked Address[192.168.0.19]:5701, using socket ServerSocket[addr=/0.0.0.$
Dec 31, 2014 2:53:22 PM com.hazelcast.spi.OperationService
INFO: [192.168.0.19]:5701 [dev] [3.4-RC1-SNAPSHOT] Backpressure is disabled
Dec 31, 2014 2:53:22 PM com.hazelcast.spi.impl.BasicOperationScheduler
INFO: [192.168.0.19]:5701 [dev] [3.4-RC1-SNAPSHOT] Starting with 2 generic operation threads and 2 partition ope$
Dec 31, 2014 2:53:22 PM com.hazelcast.system
INFO: [192.168.0.19]:5701 [dev] [3.4-RC1-SNAPSHOT] Hazelcast 3.4-RC1-SNAPSHOT (20141215 - 24f7902) starting at A$
Dec 31, 2014 2:53:22 PM com.hazelcast.system
INFO: [192.168.0.19]:5701 [dev] [3.4-RC1-SNAPSHOT] Copyright (C) 2008-2014 Hazelcast.com
Dec 31, 2014 2:53:22 PM com.hazelcast.instance.Node
INFO: [192.168.0.19]:5701 [dev] [3.4-RC1-SNAPSHOT] Creating MulticastJoiner
Dec 31, 2014 2:53:22 PM com.hazelcast.core.LifecycleService
INFO: [192.168.0.19]:5701 [dev] [3.4-RC1-SNAPSHOT] Address[192.168.0.19]:5701 is STARTING
Dec 31, 2014 2:53:26 PM com.hazelcast.cluster.impl.MulticastJoiner
INFO: [192.168.0.19]:5701 [dev] [3.4-RC1-SNAPSHOT]


Members [1] {
        Member [192.168.0.19]:5701 this
}

Dec 31, 2014 2:53:26 PM com.hazelcast.core.LifecycleService
INFO: [192.168.0.19]:5701 [dev] [3.4-RC1-SNAPSHOT] Address[192.168.0.19]:5701 is STARTED
Dec 31, 2014 2:55:16 PM com.hazelcast.nio.tcp.SocketAcceptor
INFO: [192.168.0.19]:5701 [dev] [3.4-RC1-SNAPSHOT] Accepting socket connection from /192.168.0.20:46709
Dec 31, 2014 2:55:21 PM com.hazelcast.nio.tcp.TcpIpConnectionManager
INFO: [192.168.0.19]:5701 [dev] [3.4-RC1-SNAPSHOT] Established socket connection between /192.168.0.19:5701 and $
Dec 31, 2014 2:55:27 PM com.hazelcast.cluster.ClusterService
INFO: [192.168.0.19]:5701 [dev] [3.4-RC1-SNAPSHOT]

Members [2] {
        Member [192.168.0.19]:5701 this
        Member [192.168.0.20]:5701
}

```

## Getting Big (Hazelcast and Amazon EC2)

You could use the above steps to tinker and experiment with your Java code and your Hazelcast cluster from your own desktop machine.  However you may want to run your cluster on much more powerful hardware.  This is where vagrant comes to the rescue again.

Lets take another look at the [VagrantFile](./src/main/vagrant/Vagrantfile) within `/src/main/vagrant`

If you scroll down a little further past the VirtualBox provider code you'll see another block that deals with AWS explicitly.

```ruby
# AMAZON EC2 PROVIDER

  # To use the Amazon provider you'll need to install the AWS Vagrant Plugin
  # Follow the instructions found here :-
  # https://github.com/mitchellh/vagrant-aws

  config.vm.provider :aws do |aws, override|

    override.vm.box = "dummy"
    override.vm.box_url = "https://github.com/mitchellh/vagrant-aws/raw/master/dummy.box"

    aws.region =  "us-east-1"
    aws.access_key_id = ENV['AWS_ACCESS_KEY']
    aws.secret_access_key = ENV['AWS_SECRET_KEY']
    aws.instance_type = 'm3.medium'
    aws.ami = "ami-9eaa1cf6" # Ubuntu Server 14.04 LTS (HVM), SSD Volume Type
    aws.keypair_name = 'david'
    aws.security_groups = 'david-us-east-1-sg'

    aws.tags = {'hazelcast_service' => 'true'}

    override.ssh.username = 'ubuntu'
    override.ssh.private_key_path = '~/.ssh/aws.pem'

    override.vm.provision "chef_solo" do |chef|
      chef.cookbooks_path = CHEF_COOKBOOKS_PATH
      chef.json = {
                      :hazelcast => {
                      :network_aws_access_key => ENV['AWS_ACCESS_KEY'],
                      :network_aws_secret_key => ENV['AWS_SECRET_KEY'],
                      :network_aws_region => "us-east-1",
                      :network_aws_host_header => "https://ec2.us-east-1.amazonaws.com",
                      :network_aws_security_group => "david-us-east-1-sg",
                      :network_aws_tag_key => "hazelcast_service",
                      :network_aws_tag_value => "true",
                      :network_multicast_enabled => "false",
                      :network_aws_enabled => "true" 
                    }}
      chef.add_recipe "hazelcast-integration-amazon-ec2"
    end

  end
```

This whole section is slightly more convoluted than the previous Virtual Box deployment.  There are a number of areas that are worth discussion in more depth.

1. Observe the override.vm entries.  This is telling vagrant to use an AWS box.  This box is configured with various information which allows Vagrant to connect to AWS.  You'll need to provide your own AWS credentials here such as your access key and secret key.  Also notice that keypair_name, security_groups and ssh.private_key_path will all have to be changed to match your own personal values you have set-up in EC2.

2. By default Hazelcast uses Multicast for peer discovery and cluster set-up.  This isn't possible in Amazon. To get around this Hazelcast can discover it's peers within the Amazon region by using tags.  Each member on start-up uses the Amazon EC2 Rest API to query for IP address that hold the tag, in this case {"hazelcast_service":"true"}.  So the following line places this tag on each virtual machine `aws.tags = {'hazelcast_service' => 'true'}`

3. Finally you'll see the chef_solo configuration. The entries attributed to `chef.json` are manipulating the hazelcast.xml configuration for each member.  You can see we're setting the secret keys and access keys again, which make it possible for the Hazelcast Java Instance to query the Amazon REST API.  You'll also see we're enabling aws member discovery and disabling multicast.

### Let's do this thing

In order to run up within Amazon EC2 we have to give an extra argument to our `vagrant up` command....

`vagrant up --provider=aws`

Running this should now execute the Amazon EC2 portion of the VagrantFile.

If you have configured your amazon values correctly you should now see the Amazon instances being created.  Once finished ssh into the boxes as we have done before and check in the `/var/log/upstart` directory to confirm everything is ok with the Hazelcast cluster.

To bring the cluster down always remember 

`vagrant destroy`

## Scale Up

Remember that variable we had in the VagrantFile?

`NUM_BOXES=2`

Now we're running in Amazon rather than our desktop we can create a much larger cluster to play with.  For example why not try creating a cluster of 20 members, or 50 members?

When you run with this number of instances remember to destroy the machines once you're finished or you'll end up with a very large bill from Amazon.

`vagrant destroy --force`

Using the --force flag will stop you having to confirm each machine kill, which could get boring if you have many many machines.

## Hazelcast Clients connecting to an Amazon EC2 Cluster

Now we have our Hazelcast Cluster running in EC2 we can demonstrate a cool feature of Hazelcast Clients.  Using the same Amazon REST API the Hazelcast Client can search in an EC2 region for machines that poses a particular tag.

Take a look at the Java [Client.java](./src/main/java/com/hazelcast/samples/amazon/ec2/client/Client.java) within `/src/main/java/com/hazelcast/samples/amazon/ec2` 

```java
package com.hazelcast.samples.amazon.ec2.client;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientAwsConfig;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * Example of a client connecting to a Hazelcast Cluster running on Amazon EC2.
 *
 * By default we have set insideAws to false, this means that you can run this client from your desktop and it will
 * connect into Amazon EC2.
 */
public class Client {

    public static void main(String args[]){

        ClientConfig clientConfig = new ClientConfig();
        ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig();
        ClientAwsConfig awsConfig = new ClientAwsConfig();

        awsConfig.setInsideAws(false);
        awsConfig.setEnabled(true);
        awsConfig.setAccessKey("-- YOUR AWS ACCESS KEY --");
        awsConfig.setSecretKey("-- YOUR AWS SECRET KEY --");
        awsConfig.setRegion("us-east-1");
        awsConfig.setSecurityGroupName("david-us-east-1-sg");
        awsConfig.setTagKey("hazelcast_service");
        awsConfig.setTagValue("true");

        clientConfig.setNetworkConfig(clientNetworkConfig.setAwsConfig(awsConfig));

        HazelcastInstance hazelcastClientInstance = HazelcastClient.newHazelcastClient(clientConfig);

        // Now do something...
        IMap<Object, Object> testMap = hazelcastClientInstance.getMap("test");
        testMap.put("testKey","testValue");

    }

}
```

If you run this you should be able to connect into the Amazon Hazelcast cluster you have already running and perform the simple Map Put.  Remember though you'll have to provide you credentials in the same way that you edited the VagrantFile.  So this means providing the Access Key and Secret Key along with your Security Group Name.

## Amazon Security Gotchas

Make sure your security group allows connections to the hazelcast ports otherwise your client will not be able to connect.  By default Hazelcast searches for a port on 5701 and works its way up on a machine until it finds a free one.

Also make sure your Amazon security group allows SSH connections, otherwise when you run `vagrant ssh` it will fail.

## Finally

Once you've finished experimenting within Amazon remember to stop everything, otherwise it can get very expensive leaving instances running.  To do this you can run another vagrant command

`vagrant destroy`








