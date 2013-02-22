package com.zanox.coreservice.vmware.core.ejb;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import com.zanox.coreservice.vmware.core.exception.VMWareException;

/**
 * Business interface for EJB
 * 
 * @author Sascha Moellering (sascha.moellering@zanox.com)
 */
@WebService(targetNamespace = "http://coreservices.zanox.com/vmwareservice/v1")
@SOAPBinding(style = Style.RPC)
public interface VMWareService {

	@WebMethod
	public String getResourcePools() throws VMWareException;

	/**
	 * Service to create VMWare-images
	 * 
	 * @param vmName Name of the VM
	 * @param memorySize Size of memory in GB
	 * @param diskSize Size of disk in GB
	 * @param cpuCount Number of CPUs
	 * @param poolName Name of the ResourcePool
	 * @param hostName Name of the HostSystem
	 * @param comment Comment for VM
	 * @return The Mac-Address
	 * @throws VMWareException If creation went wrong
	 */
	@WebMethod
	public String createVM(@WebParam(name = "vmName", mode = Mode.IN) String vmName,
		@WebParam(name = "memorySize", mode = Mode.IN) Integer memorySize, @WebParam(name = "diskSize", mode = Mode.IN) Integer diskSize, @WebParam(
			name = "cpuCount", mode = Mode.IN) Integer cpuCount, @WebParam(name = "networkCount", mode = Mode.IN) Integer networkCount, @WebParam(
			name = "poolName", mode = Mode.IN) String poolName, @WebParam(name = "hostName", mode = Mode.IN) final String hostName, @WebParam(
			name = "comment", mode = Mode.IN) final String comment) throws VMWareException;

	/**
	 * Deletes a VM
	 * 
	 * @param vmName Name of the VM to delete
	 * @return true is deletion succeeded
	 * @throws VMWareException if deletion went wrong
	 */
	@WebMethod
	public Boolean deleteVM(@WebParam(name = "vmName", mode = Mode.IN) String vmName) throws VMWareException;

	/**
	 * Starts a specific VM
	 * 
	 * @param vmName Name of the VM
	 * @return true if start succeeded
	 * @throws VMWareException If starting went wrong
	 */
	@WebMethod
	public Boolean startVm(@WebParam(name = "vmName", mode = Mode.IN) String vmName) throws VMWareException;

	/**
	 * Stops a specific VM
	 * 
	 * @param vmName Name of the VM
	 * @return true if stop succeeded
	 * @throws VMWareException If starting went wrong
	 */
	@WebMethod
	public Boolean stopVm(@WebParam(name = "vmName", mode = Mode.IN) String vmName) throws VMWareException;

	/**
	 * Returns the MAC-Address of a specific VM
	 * 
	 * @param vmName Name of the VM
	 * @return MAC-Address
	 * @throws VMWareException If collecting the MAC-Address went wrong
	 */
	@WebMethod
	public String getMacAddress(@WebParam(name = "vmName", mode = Mode.IN) String vmName) throws VMWareException;

	/**
	 * Changes the given VLAN of a specific VM
	 * 
	 * @param vmName Name of VM
	 * @param netName VLAN of VM
	 * @param nicName NIC of VM
	 * @return true if change succeeded
	 * @throws VMWareException if changing went wrong
	 */
	@WebMethod
	public boolean changeVlan(@WebParam(name = "vmName", mode = Mode.IN) String vmName, @WebParam(name = "netName", mode = Mode.IN) String netName,
		@WebParam(name = "nicName", mode = Mode.IN) String nicName) throws VMWareException;

	/**
	 * Returns the status of one specific VM
	 * 
	 * @param vmName Name of the VM
	 * @return Status of VM
	 * @throws VMWareException if collection of the data went wrong
	 */
	@WebMethod
	public String getStatusForVM(@WebParam(name = "vmName", mode = Mode.IN) String vmName) throws VMWareException;

	/**
	 * Collects the status for all VMs
	 * 
	 * @return A Map containing name of the VM and the status
	 * @throws VMWareException if collection of the data went wrong
	 */
	@WebMethod
	public String getStatusForAllVm() throws VMWareException;

	/**
	 * Collects all HostSystems
	 * 
	 * @return A comma separated string containing all HostSystems
	 * @throws VMWareException if collection of data went wrong
	 */
	@WebMethod
	public String getAllHostSystems() throws VMWareException;

	/**
	 * Returns the HostSystem for a VMName
	 * 
	 * @param vmName The name of the VM
	 * @return The name of the HostSystem
	 * @throws VMWareException if collection of data went wrong
	 */
	@WebMethod
	public String getHostSystemForVM(@WebParam(name = "vmName", mode = Mode.IN) String vmName) throws VMWareException;

	/**
	 * Moves a VM to a different HostSystem
	 * 
	 * @param vmName Name of the VM
	 * @param hostSystem Name of the HostSystem
	 * @return if move succeeded
	 * @throws VMWareException if moving the VM went wrong
	 */
	@WebMethod
	public Boolean moveVmToHostSystem(@WebParam(name = "vmName", mode = Mode.IN) String vmName,
		@WebParam(name = "hostSystem", mode = Mode.IN) String hostSystem) throws VMWareException;

	/**
	 * Returns the power-state of a VM
	 * 
	 * @param vmName Name of the VM
	 * @return The power-state
	 * @throws VMWareException If something goes wrong
	 */
	@WebMethod
	public String getVMPowerState(@WebParam(name = "vmName", mode = Mode.IN) String vmName) throws VMWareException;

	/**
	 * Returns if the name of the VM is available
	 * 
	 * @param vmName The name of the VM
	 * @return true if the name is available
	 * @throws VMWareException If something goes wrong
	 */
	@WebMethod
	public Boolean isVMNameAvailable(@WebParam(name = "vmName", mode = Mode.IN) String vmName) throws VMWareException;

    /**
     * Closes the session
     *
     * @throws VMWareException
     */
    @WebMethod
    public void closeSession() throws VMWareException;
}
