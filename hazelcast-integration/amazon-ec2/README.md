# Amazon EC2 Hazelcast

## Introduction

This project demonstrates two things

1. An example of deploying a Hazelcast project onto Amazon EC2 using third party tools such as [Vagrant](https://www.vagrantup.com/) and [Chef](https://www.chef.io/chef/)

2. Demonstration of Hazelcasts auto discovery mechanisms for client connections and also for cluster formation.

## Project Structure

The project follows a standard java quickstart maven layout with some additional directories

`/src/main/vagrant` - contains the Vagranfile used for providing the linux virtual machines upon which the hazelcast project will be installed. These linux virtual machines can use either VirtualBox or Amazon EC2.

`/src/main/chef` - contains the Chef cookbook that installs the sample application.

## Required Software

1. Install [VirtualBox](https://www.virtualbox.org/) this is the default Vagrant Provider, you'll use this to test out deployments and creating Virtual Machines on your own desktop prior to running out on Amazon EC2.

2. Install the [Chef Developers Kit](https://downloads.chef.io/chef-dk/) for your platform.  This will provide a set of command line tools, please ensure you are running at least version 0.3.5

3. Install [Vagrant](https://www.vagrantup.com/downloads) for your platform, please ensure you are running at least version 1.7.1

4. Install [Vagrant Plug-ins](https://docs.vagrantup.com/v2/plugins/usage.html) required for the Vagrant script to run.  Try to ensure you at least have the following installed...

* berkshelf (3.2.2)
* vagrant-aws (0.6.0)
* vagrant-berkshelf (4.0.1)
* vagrant-omnibus (1.4.1)
* vagrant-share (1.1.4, system)

For example to install vagrant-aws plugin you'll need to run on the command line

`vagrant plugin install vagrant-aws`

5. Additionally for the AWS provider to work you'll need to add a dummy box to Vagrant

`vagrant box add dummy https://github.com/mitchellh/vagrant-aws/raw/master/dummy.box`

## First Steps (Virtual Box as a provider)

To begin with lets validate that everything is installed correctly.  If it is, we should be able to start up a simple hazelcast cluster running on some Ubuntu virtual machines running on your own desktop.  We'll use the Vagrants default platform provider, VirtualBox to do this.

Take a look at the VagrantFile within `/src/main/vagrant`

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

Now we're ready to fire up our environment.

Within the vagrant directory where you have the VagrantFile simply type

`vagrant up`


