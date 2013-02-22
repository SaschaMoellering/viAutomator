package com.zanox.coreservice.vmware.core.ejb.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.annotation.PreDestroy;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;

import com.vmware.vim25.ManagedEntityStatus;
import org.jboss.wsf.spi.annotation.WebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.VirtualMachinePowerState;
import com.zanox.coreservice.vmware.core.ejb.VMWareService;
import com.zanox.coreservice.vmware.core.enums.OpEnum;
import com.zanox.coreservice.vmware.core.exception.VMWareException;
import com.zanox.coreservice.vmware.core.helper.Constants;
import com.zanox.coreservice.vmware.core.helper.VMWareHelper;
import com.zanox.coreservice.vmware.core.vo.Configuration;

/**
 * Implementation of VMWare-Service-interface
 * 
 * @author Sascha Moellering (sascha.moellering@zanox.com)
 */

@WebService(name = "VMWareService", serviceName = "VMWareService", endpointInterface = "com.zanox.coreservice.vmware.core.ejb.VMWareService",
	targetNamespace = "http://coreservices.zanox.com/vmwareservice/v1")
@WebContext(contextRoot = "/vmwareservice/v1/soap")
@Stateless(mappedName = "VMWareService")
@Remote(VMWareService.class)
public class VMWareServiceImpl implements VMWareService {

	private static final Logger logger = LoggerFactory.getLogger(VMWareServiceImpl.class);

	@PreDestroy
	public void destroy() {
		try {

			Configuration conf = Configuration.getInstance();

			VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl()).logout();
		}

		catch (Exception exc) {
			logger.error("Logout", exc);
		}
	}

	@Override
	@WebMethod
	public String getResourcePools() throws VMWareException {
		Configuration conf = Configuration.getInstance();

		VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

		return helper.getResourcePools(conf.getDcName());

	}

	public String createVM(String vmName, Integer memorySize, Integer diskSize, Integer cpuCount, Integer networkCount, String poolName,
		String hostName, String comment) throws VMWareException {

		logger.debug("Name: " + vmName);
		logger.debug("Memorysize: " + memorySize);
		logger.debug("Disksize: " + diskSize);
		logger.debug("Number of CPUs: " + cpuCount);
		logger.debug("Poolname:" + poolName);
		logger.debug("HostSystem:" + hostName);

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

	public String getVMPowerState(String vmName) throws VMWareException {
        Configuration conf = Configuration.getInstance();

        VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

        VirtualMachinePowerState state = helper.getPowerState(vmName);

        if (state == null) {
            return null;
            // throw new VMWareException("Name of VM " + vmName + " not found!");
        }

        return state.name();
	}

	public Boolean deleteVM(String vmName) throws VMWareException {


            Configuration conf = Configuration.getInstance();

            VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

            return helper.deleteVM(vmName);
	}

	public Boolean isVMNameAvailable(String vmName) throws VMWareException {
        Configuration conf = Configuration.getInstance();

        VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

        return helper.isVMNameAvailable(vmName);

    }

	public Boolean startVm(String vmName) throws VMWareException {
        Configuration conf = Configuration.getInstance();

        VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

        return helper.vmOperation(vmName, OpEnum.POWER_ON);
	}

	public Boolean stopVm(String vmName) throws VMWareException {
        Configuration conf = Configuration.getInstance();

        VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

        return helper.vmOperation(vmName, OpEnum.SHUTDOWN);
	}

	public String getMacAddress(String vmName) throws VMWareException {
        Configuration conf = Configuration.getInstance();

        VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

        return helper.getMacAddressForVm(vmName);
	}

	public boolean changeVlan(String vmName, String netName, String nicName) throws VMWareException {
        Configuration conf = Configuration.getInstance();

        VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

        return helper.changeVlan(vmName, netName, nicName);
	}

	/**
	 * Closes the session
	 * 
	 * @throws com.zanox.coreservice.vmware.core.exception.VMWareException
	 * 
	 */
	@Override
	@WebMethod
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

	public String getStatusForVM(String vmName) throws VMWareException {
        Configuration conf = Configuration.getInstance();

        VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

        ManagedEntityStatus status = helper.getStatusForVm(vmName);

        if (status == null) {
            throw new VMWareException("Name of VM " + vmName + " not found");
        }

        return helper.getStatusForVm(vmName).toString();
	}

	public String getStatusForAllVm() throws VMWareException {
        Configuration conf = Configuration.getInstance();

        VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

        HashMap<String, String> status = helper.getStatusForAllVm();

        String s = status.toString();
        return s.substring(1, s.length() - 1);
	}

	public String getHostSystemForVM(String vmName) throws VMWareException {
        Configuration conf = Configuration.getInstance();

        VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

        return helper.getHostSystemForVM(vmName);
	}

	public Boolean moveVmToHostSystem(String vmName, String hostSystem) throws VMWareException {
        Configuration conf = Configuration.getInstance();

        VMWareHelper helper = VMWareHelper.getInstance(conf.getUserName(), conf.getPassword(), conf.getUrl());

        return helper.moveVmToHostSystem(vmName, hostSystem);
	}

}
