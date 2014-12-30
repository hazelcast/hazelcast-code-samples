# Amazon EC2 Hazelcast

## Introduction

This project demonstrates two things

(1) An example of deploying a Hazelcast project onto Amazon EC2 using third party tools such as [Vagrant](https://www.vagrantup.com/) and [Chef](https://www.chef.io/chef/)

(2) Demonstration of Hazelcasts auto discovery mechanisms for client connections and also for cluster formation.

## Project Structure

The project follows a standard maven layout with some additional directories

`/src/main/vagrant` - contains the Vagranfile used for providing the linux virtual machines upon which the hazelcast project will be installed. These linux virtual machines can use either VirtualBox or Amazon EC2.

`/src/main/chef` - contains the Chef cookbook that installs the sample application.

## Required Software

(1) Install the [Chef Developers Kit](https://downloads.chef.io/chef-dk/) for your platform.  This will provide a set of command line tools, please ensure you are running at least version 0.3.5

(2) Install [Vagrant](https://www.vagrantup.com/downloads) for your platform, please ensure you are running at least version 1.7.1

(3) Install [Vagrant Plug-ins](https://docs.vagrantup.com/v2/plugins/usage.html) required for the Vagrant script to run.  Try to ensure you at least have the following installed...

* berkshelf (3.2.2)
* vagrant-aws (0.6.0)
* vagrant-awsinfo (0.0.8)
* vagrant-berkshelf (4.0.1)
* vagrant-cachier (1.1.0)
* vagrant-omnibus (1.4.1)
* vagrant-share (1.1.4, system)

