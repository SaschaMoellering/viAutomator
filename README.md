# Introduction

## Purpose

viAutomator is a simple webservice to expose several functionalities of VMWare vSphere for easier integration in your
current environment. The service is based on pure Java (JDK 1.6, Java EE 5) and VI Java-library (http://vijava.sourceforge.net/).

## Overview

### Core

- contains the logic to connect to vSphere
- exposes a simple SOAP-webservice
- exposes an MBean to create encrypted passwords for the vSphere-connection

### Functionalities

- Get all resource-pools
- Create VMs
- Delete VMs
- Start VMs
- Stop VMs
- Get the MAC-address of a VM
- Change the VLAN of a VM
- Get the status for one VM
- Get the status for all VMs
- Get a list of all Host-Systems
- Get the Host-System for a VM
- Move a VM to a Host-System
- Get the Power-State of a VM
- Check if the name for a VM is available
- Close the session to the vSphere-server

## Installation

- Build the artifact using Maven (mvn clean && mvn package)
- Deploy the artifact in JBoss 5
- Use the JMX-console to create an encrypted password (MBean: )
- Copy vmware.properties from resources to <jboss-server>/conf/props
- Configure the missing parameters in vmware.properties (using the encrypted password)
- Connect to the SOAP-service
- Done

## Usage

Due to the fact that the connection-handling is a little bit weird in VIJava, it is necessary to call closeSession sometimes,
recommendation is:

- calling closeSession after every service-call
- except if you iterate a list of vm-names and call getStatusForVM, then you should call closeSession after the list is iterated
completely