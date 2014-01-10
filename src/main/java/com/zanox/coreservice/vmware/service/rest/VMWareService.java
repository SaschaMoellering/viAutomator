package com.zanox.coreservice.vmware.service.rest;

import com.vmware.vim25.ManagedEntityStatus;
import com.vmware.vim25.VirtualMachinePowerState;
import com.zanox.coreservice.vmware.service.exception.VMWareException;
import com.zanox.coreservice.vmware.service.helper.VMWareHelper;
import com.zanox.coreservice.vmware.service.vo.Configuration;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.zanox.coreservice.vmware.service.enums.OpEnum.POWER_ON;
import static com.zanox.coreservice.vmware.service.enums.OpEnum.SHUTDOWN;

/**
 * Implementation of VMWare-Service-interface
 * 
 * @author Sascha Moellering (sascha.moellering@zanox.com)
 */

@ApplicationScoped
@Path("/vmware")
public class VMWareService {

	private static final Logger logger = Logger.getLogger(VMWareService.class.getName());

	@PreDestroy
	public void destroy() {
		try {

			Configuration conf = Configuration.getInstance();

			VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl()).logout();
		}

		catch (Exception exc) {
			logger.log(Level.SEVERE, "Logout", exc);
		}
	}

    @GET
    @Path("/resourcePools")
	public String getResourcePools() throws VMWareException {
		Configuration conf = Configuration.getInstance();

		VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

		return helper.getResourcePools(conf.getDcName());

	}

    @PUT
    @Path("/vm/{vmname}")
	public String createVM(@PathParam("vmname") String vmName,
                           @QueryParam("memorySize") Integer memorySize,
                           @QueryParam("diskSize") Integer diskSize,
                           @QueryParam("cpuCount") Integer cpuCount,
                           @QueryParam("networkCount") Integer networkCount,
                           @QueryParam("poolName") String poolName,
                           @QueryParam("hostName") String hostName,
                           @QueryParam("comment") String comment) throws VMWareException {

		logger.log(Level.FINE, "Name: " + vmName);
		logger.log(Level.FINE, "Memorysize: " + memorySize);
		logger.log(Level.FINE, "Disksize: " + diskSize);
		logger.log(Level.FINE, "Number of CPUs: " + cpuCount);
		logger.log(Level.FINE, "Poolname:" + poolName);
		logger.log(Level.FINE, "HostSystem:" + hostName);

		if (vmName == null)
			throw new VMWareException("Name of VM is null");

		if (memorySize == null)
			throw new VMWareException("MemorySize is null");

		if (diskSize == null)
			throw new VMWareException("DiskSize is null");

		if (cpuCount == null)
			throw new VMWareException("CPU-count is null");

		if (poolName == null)
			throw new VMWareException("Poolname is null");

		if (networkCount == null || networkCount < 1)
			throw new VMWareException("NetworkCount is NULL or < 1");

		Configuration conf = Configuration.getInstance();

		String dataStoreName = conf.getDataStoreName();
		String netName = conf.getNetName();
		String virtualNetwork = conf.getVirtualNetwork();

		VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

		if (!helper.isVMNameAvailable(vmName))
			throw new VMWareException("VM \"" + vmName + "\" exists already!");

		Boolean retVal =
			helper.createVM(conf.getDcName(), vmName, (long) (memorySize * 1024), cpuCount, networkCount, conf.getGuestOsId(), poolName, hostName,
				(long) (diskSize * 1024 * 1024), conf.getDiskMode(), dataStoreName, netName, conf.getNicName(), comment, virtualNetwork);

		if (retVal != null && retVal == Boolean.TRUE)
			return helper.getMacAddressForVm(vmName);

		return null;

	}

    @GET
    @Path("/hostSystems")
	public String getAllHostSystems() throws VMWareException {
        Configuration conf = Configuration.getInstance();

        VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

        List<String> hostSystems = helper.getAllHostSystems();

        StringBuilder builder = new StringBuilder();

        for (String hostSystem : hostSystems) {
            builder.append(hostSystem);
            builder.append(",");
        }

        String s = builder.toString();

        return s.substring(0, s.length() - 1);
	}

    @GET
    @Path("/powerstate/{vmname}")
	public String getVMPowerState(@PathParam("vmname") String vmName) throws VMWareException {
        Configuration conf = Configuration.getInstance();

        VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

        VirtualMachinePowerState state = helper.getPowerState(vmName);

        if (state == null) {
            return null;
            // throw new VMWareException("Name of VM " + vmName + " not found!");
        }

        return state.name();
	}

    @DELETE
    @Path("/vm/{vmname}")
	public Boolean deleteVM(@PathParam("vmname") String vmName) throws VMWareException {


            Configuration conf = Configuration.getInstance();

            VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

            return helper.deleteVM(vmName);
	}

    @GET
    @Path("/name/{vmname}")
	public Boolean isVMNameAvailable(@PathParam("vmname") String vmName) throws VMWareException {
        Configuration conf = Configuration.getInstance();

        VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

        return helper.isVMNameAvailable(vmName);

    }

    @GET
    @Path("/vmstart/{vmname}")
	public Boolean startVm(@PathParam("vmname") String vmName) throws VMWareException {
        Configuration conf = Configuration.getInstance();

        VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

        return helper.vmOperation(vmName, POWER_ON);
	}

    @GET
    @Path("/vmstop/{vmname}")
	public Boolean stopVm(@PathParam("vmname") String vmName) throws VMWareException {
        Configuration conf = Configuration.getInstance();

        VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

        return helper.vmOperation(vmName, SHUTDOWN);
	}

    @GET
    @Path("/macAddress/{vmname}")
	public String getMacAddress(@PathParam("vmname") String vmName) throws VMWareException {
        Configuration conf = Configuration.getInstance();

        VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

        return helper.getMacAddressForVm(vmName);
	}

    @POST
    @Path("/vlan/{vmname}")
	public boolean changeVlan(@PathParam("vmname") String vmName,
                              @QueryParam("netname") String netName,
                              @QueryParam("nicname") String nicName) throws VMWareException {
        Configuration conf = Configuration.getInstance();

        VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

        return helper.changeVlan(vmName, netName, nicName);
	}

	/**
	 * Closes the session
	 * 
	 * @throws com.zanox.coreservice.vmware.service.exception.VMWareException
	 * 
	 */
	@POST
    @Path("/session")
	public void closeSession() throws VMWareException {
        Configuration conf = Configuration.getInstance();

        VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

        helper.logout();
	}

	private Properties readConfig() throws IOException {
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("props/vmware.properties");

		Properties props = new Properties();
		props.load(inputStream);

		return props;
	}

    @GET
    @Path("/vmstatus/{vmname}")
	public String getStatusForVM(@PathParam("vmname") String vmName) throws VMWareException {
        Configuration conf = Configuration.getInstance();

        VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

        ManagedEntityStatus status = helper.getStatusForVm(vmName);

        if (status == null) {
            throw new VMWareException("Name of VM " + vmName + " not found");
        }

        return helper.getStatusForVm(vmName).toString();
	}

    @GET
    @Path("/vmstatus")
	public String getStatusForAllVm() throws VMWareException {
        Configuration conf = Configuration.getInstance();

        VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

        HashMap<String, String> status = helper.getStatusForAllVm();

        String s = status.toString();
        return s.substring(1, s.length() - 1);
	}

    @GET
    @Path("/hostsystem/{vmname}")
	public String getHostSystemForVM(@PathParam("vmname") String vmName) throws VMWareException {
        Configuration conf = Configuration.getInstance();

        VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

        return helper.getHostSystemForVM(vmName);
	}

    @POST
    @Path("/hostmove/{vmname}")
	public Boolean moveVmToHostSystem(@PathParam("vmname") String vmName, @QueryParam("hostsystem") String hostSystem) throws VMWareException {
        Configuration conf = Configuration.getInstance();

        VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

        return helper.moveVmToHostSystem(vmName, hostSystem);
	}

}
