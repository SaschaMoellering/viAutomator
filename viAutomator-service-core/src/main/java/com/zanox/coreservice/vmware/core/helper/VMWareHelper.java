package com.zanox.coreservice.vmware.core.helper;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vim25.*;
import com.vmware.vim25.mo.*;
import com.zanox.coreservice.vmware.core.enums.OpEnum;
import com.zanox.coreservice.vmware.core.exception.VMWareException;

/**
 * VMWare-Helper Singleton
 *
 * @author sascha
 */
public class VMWareHelper {

    private static Logger logger = LoggerFactory.getLogger(VMWareHelper.class);

    private static VMWareHelper VMWARE = null;

    private LoginConfiguration loginConfiguration;

    private ServiceInstance serviceInstance;

    private VMWareHelper(final String username, final String password, final String vmwareUrl) {
        loginConfiguration = new LoginConfiguration(username, password, vmwareUrl);
    }

    /**
     * Returns an instance of VMWare-Helper
     *
     * @param username Username
     * @param password Password
     * @param vmwareUrl URL
     * @return The instance
     */
    public static VMWareHelper getInstance(final String username, final String password, final String vmwareUrl) {

        if (VMWARE == null) {
            VMWARE = new VMWareHelper(username, password, vmwareUrl);
        }

        return VMWARE;
    }

    /**
     * Returns a server-instance
     *
     * @return The server-instance
     * @throws Exception The something goes wrong
     */
    private ServiceInstance getServerInstance() throws MalformedURLException, RemoteException {

        if (serviceInstance == null) {

            logger.info("Server instance is NULL ... creating a new connection");

            URL url = new URL(loginConfiguration.getVmwareUrl());
            serviceInstance = new ServiceInstance(url, loginConfiguration.getUserName(), loginConfiguration.getPassword(), true);
        }

        return serviceInstance;
    }

    public void logout() throws VMWareException {

        logger.info("Closing session");

        try {
            if (serviceInstance != null) {
                serviceInstance.getSessionManager().logout();
                serviceInstance = null;
            }
        } catch (PlatformConfigFault f) {
            throw new VMWareException(f);
        } catch (com.vmware.vim25.NotAuthenticated authExc) {
            logger.info("Problem: NotAuthenticated, setting instance to NULL");
            serviceInstance = null;
        } catch (RemoteException exc) {
            throw new VMWareException(exc);
        } catch (Exception exc) {
            throw new VMWareException(exc);
        }
    }

    /**
     * Checks if VM-name is available
     *
     * @param vmName Name of the VM
     * @return If VMName is available
     * @throws VMWareException If something goes wrong.
     */
    public Boolean isVMNameAvailable(final String vmName) throws VMWareException {
        ServiceInstance si = null;

        try {
            si = this.getServerInstance();
            Folder rootFolder = si.getRootFolder();
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmName);

            if (vm == null)
                return Boolean.TRUE;
            else
                return Boolean.FALSE;

        } catch (PlatformConfigFault f) {
            throw new VMWareException(f);
        } catch (RemoteException exc) {
            throw new VMWareException(exc);
        } catch (Exception exc) {
            throw new VMWareException(exc);
        }
    }

    public String getMacAddressForVm(final String vmName) throws VMWareException {
        ServiceInstance si = null;

        try {
            si = this.getServerInstance();
            Folder rootFolder = si.getRootFolder();
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmName);

            VirtualDevice[] devices = vm.getConfig().getHardware().getDevice();
            VirtualEthernetCard vec = null;
            for (VirtualDevice dev : devices) {
                if (dev instanceof VirtualEthernetCard) {
                    vec = (VirtualEthernetCard) dev;
                    break;
                }
            }

            return vec.getMacAddress();
        } catch (PlatformConfigFault f) {
            throw new VMWareException(f);
        } catch (RemoteException exc) {
            throw new VMWareException(exc);
        } catch (Exception exc) {
            throw new VMWareException(exc);
        }
    }

    public ManagedEntityStatus getStatusForVm(final String vmName) throws VMWareException {

        ServiceInstance si = null;

        try {

            si = this.getServerInstance();
            Folder rootFolder = si.getRootFolder();
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmName);

            return vm.getOverallStatus();
        } catch (PlatformConfigFault f) {
            throw new VMWareException(f);
        } catch (RemoteException exc) {
            throw new VMWareException(exc);
        } catch (Exception exc) {
            throw new VMWareException(exc);
        }
    }

    public HashMap<String, String> getStatusForAllVm() throws VMWareException {

        ServiceInstance si = null;

        HashMap<String, String> statusMap = new HashMap<String, String>();

        try {

            si = this.getServerInstance();
            Folder rootFolder = si.getRootFolder();
            ManagedEntity[] mangedEntities = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");

            for (ManagedEntity entity : mangedEntities) {

                VirtualMachine vm = (VirtualMachine) entity;
                statusMap.put(vm.getName(), vm.getOverallStatus().name());
            }

            return statusMap;
        } catch (PlatformConfigFault f) {
            throw new VMWareException(f);
        } catch (RemoteException exc) {
            throw new VMWareException(exc);
        } catch (Exception exc) {
            throw new VMWareException(exc);
        }
    }

    public Boolean changeVlan(final String vmName, final String netName, final String nicName) throws VMWareException {

        boolean retVal = false;

        ServiceInstance si = null;

        try {

            si = this.getServerInstance();
            Folder rootFolder = si.getRootFolder();
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmName);

            Network network = (Network) new InventoryNavigator(rootFolder).searchManagedEntity("Network", netName);

            if (network == null)
                throw new VMWareException("Could not find network " + netName);

            DistributedVirtualSwitch dvs = null;

            ManagedEntity[] entity = new InventoryNavigator(rootFolder).searchManagedEntities("DistributedVirtualSwitch");

            String key = "";
            DVPortgroupConfigInfo description = null;

            boolean found = false;
            for (ManagedEntity me : entity) {
                if (me instanceof DistributedVirtualSwitch) {
                    DistributedVirtualSwitch tmpDvs = (DistributedVirtualSwitch) me;
                    DistributedVirtualPortgroup[] vpgs = tmpDvs.getPortgroup();
                    for (DistributedVirtualPortgroup vpg : vpgs) {

                        if (netName.equals(vpg.getName())) {

                            key = vpg.getConfig().getKey();
                            description = vpg.getConfig();

                            dvs = tmpDvs;
                            found = true;
                            break;
                        }
                    }

                    if (found) {
                        break;
                    }
                }
            }

            VirtualMachineConfigSpec vmSpec = new VirtualMachineConfigSpec();

            VirtualMachineConfigInfo vmConfigInfo = vm.getConfig();

            String uuid = dvs.getConfig().getUuid();

            ArrayList<VirtualDeviceConfigSpec> nicSpecList = new ArrayList<VirtualDeviceConfigSpec>();

            boolean nicFound = false;

            VirtualDevice[] vds = vmConfigInfo.getHardware().getDevice();
            for (VirtualDevice vd : vds) {
                if (vd instanceof VirtualEthernetCard) {

                    VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
                    nicSpec.setOperation(VirtualDeviceConfigSpecOperation.edit);

                    VirtualEthernetCard nic = (VirtualEthernetCard) vd;

                    logger.debug("Nic: " + nic.getDeviceInfo().getLabel());
                    if (nic.getDeviceInfo().getLabel().equalsIgnoreCase(nicName)) {
                        logger.debug("Found nic " + nicName);

                        VirtualEthernetCard newNic = new VirtualVmxnet3();
                        newNic.setKey(nic.getKey());
                        newNic.setDeviceInfo(nic.getDeviceInfo());

                        newNic.getDeviceInfo().setLabel(nicName);

                        VirtualEthernetCardDistributedVirtualPortBackingInfo backing9 = new VirtualEthernetCardDistributedVirtualPortBackingInfo();

                        DistributedVirtualSwitchPortConnection port10 = new DistributedVirtualSwitchPortConnection();
                        port10.setSwitchUuid(uuid);
                        port10.setPortgroupKey(key);
                        backing9.setPort(port10);

                        newNic.setBacking(backing9);
                        newNic.setAddressType("assigned");
                        newNic.setMacAddress(nic.getMacAddress());
                        newNic.setControllerKey(nic.getControllerKey());
                        newNic.setUnitNumber(nic.getUnitNumber());

                        VirtualDeviceConnectInfo connectable11 = new VirtualDeviceConnectInfo();
                        connectable11.startConnected = true;
                        connectable11.allowGuestControl = true;
                        connectable11.connected = true;
                        connectable11.status = "untried";

                        newNic.setConnectable(connectable11);

                        logger.debug("Setting UUID: " + uuid);
                        logger.debug("Setting portgroupKey: " + key);
                        logger.debug("Setting summary: " + netName);
                        logger.debug("Port description: " + description.getNumPorts());

                        nicSpec.setDevice(newNic);

                        nicSpecList.add(nicSpec);

                        nicFound = true;
                    }

                }
            }

            if (!nicFound)
                throw new VMWareException("Could not find nic " + nicName);

            VirtualDeviceConfigSpec[] configSpec = new VirtualDeviceConfigSpec[nicSpecList.size()];
            nicSpecList.toArray(configSpec);

            vmSpec.setDeviceChange(configSpec);

            Task vmTask = vm.reconfigVM_Task(vmSpec);

            String result = vmTask.waitForTask();

            retVal = result.equals(Task.SUCCESS);

        } catch (PlatformConfigFault f) {
            throw new VMWareException(f);
        } catch (RemoteException exc) {
            throw new VMWareException(exc);
        } catch (Exception exc) {
            throw new VMWareException(exc);
        }

        return retVal;
    }

    public Boolean vmOperation(final String vmName, final OpEnum opEnum) throws VMWareException {

        boolean retVal;

        ServiceInstance si = null;

        try {

            si = this.getServerInstance();
            Folder rootFolder = si.getRootFolder();
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmName);

            Task task = null;

            switch (opEnum) {
                case POWER_OFF:
                    task = vm.powerOffVM_Task();
                    break;
                case POWER_ON:
                    task = vm.powerOnVM_Task(null);
                    break;
                case REBOOT:
                    vm.rebootGuest();
                    break;
                case RESET:
                    task = vm.resetVM_Task();
                    break;
                case SHUTDOWN:
                    vm.shutdownGuest();
                    break;
                case STANDBY:
                    vm.standbyGuest();
                    break;
                case SUSPEND:
                    task = vm.suspendVM_Task();
                    break;
            }

            String result = Task.SUCCESS;

            if (task != null)
                task.waitForTask();

            retVal = result.equals(Task.SUCCESS);

            return retVal;

        } catch (ToolsUnavailable exc) {
            if (opEnum == OpEnum.SHUTDOWN) {
                return this.vmOperation(vmName, OpEnum.POWER_OFF);
            }

            return Boolean.FALSE;
        } catch (PlatformConfigFault f) {
            logger.debug(f.toString(), f);
            throw new VMWareException(f);
        } catch (RemoteException exc) {
            logger.debug(exc.toString(), exc);
            throw new VMWareException(exc);
        } catch (Exception exc) {
            logger.debug(exc.toString(), exc);
            throw new VMWareException(exc);
        }
    }

    public Boolean moveVmToHostSystem(final String vmName, final String hostName) throws VMWareException {

        ServiceInstance si = null;
        boolean retVal;

        try {

            si = this.getServerInstance();
            Folder rootFolder = si.getRootFolder();
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmName);

            HostSystem hostSystem = null;
            ManagedEntity[] hostSystems = new InventoryNavigator(si.getRootFolder()).searchManagedEntities("HostSystem");
            for (ManagedEntity hostSystem1 : hostSystems) {
                HostSystem host = (HostSystem) hostSystem1;

                if (host.getName().equalsIgnoreCase(hostName)) {
                    logger.debug("Found HostSystem: " + hostName);
                    hostSystem = host;
                } else {
                    logger.debug("HostSystem: " + hostName + " not found! Using default HostSystem.");
                }
            }

            VirtualMachineRelocateSpec relSpec = new VirtualMachineRelocateSpec();
            relSpec.setHost(hostSystem.getMOR());

            Task task = vm.relocateVM_Task(relSpec);

            String result = task.waitForTask();

            retVal = result.equals(Task.SUCCESS);

            return retVal;
        } catch (PlatformConfigFault f) {
            throw new VMWareException(f);
        } catch (RemoteException exc) {
            throw new VMWareException(exc);
        } catch (Exception exc) {
            throw new VMWareException(exc);
        }
    }

    public String getHostSystemForVM(final String vmName) throws VMWareException {

        ServiceInstance si = null;

        try {

            si = this.getServerInstance();
            boolean found = false;
            HostSystem hostSystem = null;
            ManagedEntity[] hostsystems = new InventoryNavigator(si.getRootFolder()).searchManagedEntities("HostSystem");
            for (ManagedEntity hostsystem : hostsystems) {
                HostSystem host = (HostSystem) hostsystem;

                VirtualMachine[] vms = host.getVms();

                for (VirtualMachine vm : vms) {
                    if (vm.getName().equals(vmName)) {
                        hostSystem = host;
                        found = true;
                        break;
                    }
                }

                if (found)
                    break;
            }

            return hostSystem.getName();
        } catch (PlatformConfigFault f) {
            throw new VMWareException(f);
        } catch (RemoteException exc) {
            throw new VMWareException(exc);
        } catch (Exception exc) {
            throw new VMWareException(exc);
        }
    }

    public VirtualMachinePowerState getPowerState(final String vmName) throws VMWareException {
        ServiceInstance si = null;

        try {

            si = this.getServerInstance();
            Folder rootFolder = si.getRootFolder();
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmName);

            if (vm != null) {
                return vm.getRuntime().getPowerState();
            }

            return null;

        } catch (PlatformConfigFault f) {
            throw new VMWareException("Problems with " + vmName, f);
        } catch (RemoteException exc) {
            throw new VMWareException("Problems with " + vmName, exc);
        } catch (MalformedURLException exc) {
            throw new VMWareException("Problems with " + vmName, exc);
        }
    }

    /**
     * Deletes a VM
     *
     * @param vmName Name of VM
     * @return A boolean value that indicates if deletion succeeded or failed
     * @throws VMWareException wraps exceptions from vijava
     */
    public Boolean deleteVM(final String vmName) throws VMWareException {

        boolean retVal = false;

        ServiceInstance si = null;

        try {

            si = this.getServerInstance();
            Folder rootFolder = si.getRootFolder();
            VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmName);

            if (vm == null)
                throw new VMWareException("VM " + vmName + " is not found!");

            logger.debug("VM-State: " + vm.getRuntime().getPowerState().name());
            if (vm.getRuntime().getPowerState() == VirtualMachinePowerState.poweredOn)
                this.vmOperation(vmName, OpEnum.SHUTDOWN);

            Task task = vm.destroy_Task();

            String result = task.waitForTask();

            retVal = result.equals(Task.SUCCESS);

            return retVal;

        } catch (PlatformConfigFault f) {
            throw new VMWareException(f);
        } catch (RemoteException exc) {
            throw new VMWareException(exc);
        } catch (Exception exc) {
            throw new VMWareException(exc);
        }

    }

    public List<String> getAllHostSystems() throws VMWareException {

        ServiceInstance si = null;

        List<String> hostSystems = new ArrayList<String>();

        try {

            si = this.getServerInstance();
            ManagedEntity[] hostsystems = new InventoryNavigator(si.getRootFolder()).searchManagedEntities("HostSystem");
            for (ManagedEntity hostsystem : hostsystems) {
                HostSystem host = (HostSystem) hostsystem;

                hostSystems.add(host.getName());
            }

            return hostSystems;

        } catch (PlatformConfigFault f) {
            throw new VMWareException(f);
        } catch (RemoteException exc) {
            throw new VMWareException(exc);
        } catch (Exception exc) {
            throw new VMWareException(exc);
        }
    }

    public String getResourcePools(final String dcName) throws VMWareException {
        ServiceInstance si = null;

        StringBuilder buffer = new StringBuilder();

        try {

            si = this.getServerInstance();
            Folder rootFolder = si.getRootFolder();

            Datacenter dc = (Datacenter) new InventoryNavigator(rootFolder).searchManagedEntity("Datacenter", dcName);

            ManagedEntity[] entity = new InventoryNavigator(dc).searchManagedEntities("ResourcePool");

            for (ManagedEntity me : entity) {
                if (me instanceof ResourcePool) {
                    ResourcePool pool = (ResourcePool) me;
                    buffer.append(pool.getName()).append(", ");
                }
            }

            return buffer.toString();
        } catch (PlatformConfigFault f) {
            throw new VMWareException(f);
        } catch (RemoteException exc) {
            throw new VMWareException(exc);
        } catch (Exception exc) {
            throw new VMWareException(exc);
        }
    }

    /**
     * Creates a VM on the ZX-VMWare-Cluster using vijava (http://vijava.sourceforge.net/)
     *
     * @param dcName Name of the datacenter
     * @param vmName Name of the VMWare-image
     * @param memorySizeMB Size of memory in MB
     * @param cpuCount Number of CPUs
     * @param networkCount Number of network-interfaces
     * @param guestOsId ID of the guest-OS (e.g. debian6_64Guest)
     * @param poolName Name of the ResourcePool
     * @param hostName Name of the HostSystem
     * @param diskSizeKB Size of the disk in KB
     * @param diskMode Mode the the disk (e.g. persistent)
     * @param dataStoreName Name of the datastore
     * @param netName Name of the VLAN (e.g. VLAN551_DEVSubLinux)
     * @param nicTemplate Template of the network adapter (e.g. Network Adapter 1)
     * @param comment Comment for VM
     * @return A boolean value that indicates if creation succeeded or failed
     * @throws VMWareException wraps exceptions from vijava
     */
    public Boolean createVM(final String dcName, final String vmName, final Long memorySizeMB, final Integer cpuCount, final Integer networkCount,
                            final String guestOsId, final String poolName, final String hostName, final Long diskSizeKB, final String diskMode,
                            final String dataStoreName, final String netName, final String nicTemplate, final String comment, final String virtualNetwork)
            throws VMWareException {

        logger.debug("ResourcePool: " + poolName);
        logger.debug("NetName: " + netName);

        boolean retVal = false;

        ServiceInstance si = null;

        try {

            si = this.getServerInstance();
            Folder rootFolder = si.getRootFolder();

            Datacenter dc = (Datacenter) new InventoryNavigator(rootFolder).searchManagedEntity("Datacenter", dcName);

            Network network = (Network) new InventoryNavigator(rootFolder).searchManagedEntity("Network", netName);

            DistributedVirtualSwitch dvs =
                    (DistributedVirtualSwitch) new InventoryNavigator(dc).searchManagedEntity("DistributedVirtualSwitch", virtualNetwork);

            if (network == null)
                throw new VMWareException("Could not find network " + netName);

            ResourcePool rp = null;

            ManagedEntity[] entity = new InventoryNavigator(dc).searchManagedEntities("ResourcePool");

            for (ManagedEntity me : entity) {
                if (me instanceof ResourcePool) {
                    ResourcePool pool = (ResourcePool) me;
                    if (pool.getName().equals(poolName)) {
                        rp = pool;
                    }
                }
            }

            Folder vmFolder = dc.getVmFolder();

            // create vm config spec
            VirtualMachineConfigSpec vmSpec = new VirtualMachineConfigSpec();
            vmSpec.setName(vmName);
            vmSpec.setAnnotation(comment);
            vmSpec.setMemoryMB(memorySizeMB);
            vmSpec.setNumCPUs(cpuCount);
            vmSpec.setGuestId(guestOsId);

            // create virtual devices
            int cKey = 1000;
            VirtualDeviceConfigSpec scsiSpec = createScsiSpec(cKey);
            VirtualDeviceConfigSpec diskSpec = createDiskSpec(dataStoreName, cKey, diskSizeKB, diskMode);

            VirtualDeviceConfigSpec[] configSpec = new VirtualDeviceConfigSpec[2 + networkCount];

            configSpec[0] = scsiSpec;
            configSpec[1] = diskSpec;

            for (int i = 0; i < networkCount; i++) {
                String nicName = String.format(nicTemplate, i + 1);
                VirtualDeviceConfigSpec nicSpec = createNicSpec(netName, nicName, network, dvs, VirtualDeviceConfigSpecOperation.add);
                configSpec[2 + i] = nicSpec;
            }

            vmSpec.setDeviceChange(configSpec);

            // create vm file info for the vmx file
            VirtualMachineFileInfo vmfi = new VirtualMachineFileInfo();
            vmfi.setVmPathName("[" + dataStoreName + "]");
            vmSpec.setFiles(vmfi);

            HostSystem hostSystem = null;
            ManagedEntity[] hostsystems = new InventoryNavigator(si.getRootFolder()).searchManagedEntities("HostSystem");
            for (ManagedEntity hostsystem : hostsystems) {
                HostSystem host = (HostSystem) hostsystem;

                if (host.getName().equalsIgnoreCase(hostName)) {
                    logger.debug("Found HostSystem: " + hostName);
                    hostSystem = host;
                } else {
                    logger.debug("HostSystem: " + hostName + " not found! Using default HostSystem.");
                }
            }

            // call the createVM_Task method on the vm folder
            Task task = vmFolder.createVM_Task(vmSpec, rp, hostSystem);
            String result = task.waitForTask();

            retVal = result.equals(Task.SUCCESS);
        } catch (PlatformConfigFault f) {
            throw new VMWareException(f);
        } catch (RemoteException exc) {
            throw new VMWareException(exc);
        } catch (Exception exc) {
            throw new VMWareException(exc);
        }

        return retVal;
    }

    private static VirtualDeviceConfigSpec createScsiSpec(int cKey) {
        VirtualDeviceConfigSpec scsiSpec = new VirtualDeviceConfigSpec();
        scsiSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
        VirtualLsiLogicController scsiCtrl = new VirtualLsiLogicController();
        scsiCtrl.setKey(cKey);
        scsiCtrl.setBusNumber(0);
        scsiCtrl.setSharedBus(VirtualSCSISharing.noSharing);
        scsiSpec.setDevice(scsiCtrl);
        return scsiSpec;
    }

    private static VirtualDeviceConfigSpec createDiskSpec(String dsName, int cKey, long diskSizeKB, String diskMode) {
        VirtualDeviceConfigSpec diskSpec = new VirtualDeviceConfigSpec();
        diskSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
        diskSpec.setFileOperation(VirtualDeviceConfigSpecFileOperation.create);

        VirtualDisk vd = new VirtualDisk();
        vd.setCapacityInKB(diskSizeKB);
        diskSpec.setDevice(vd);
        vd.setKey(0);
        vd.setUnitNumber(0);
        vd.setControllerKey(cKey);

        VirtualDiskFlatVer2BackingInfo diskfileBacking = new VirtualDiskFlatVer2BackingInfo();
        String fileName = "[" + dsName + "]";
        diskfileBacking.setFileName(fileName);
        diskfileBacking.setDiskMode(diskMode);
        diskfileBacking.setThinProvisioned(true);
        vd.setBacking(diskfileBacking);
        return diskSpec;
    }

    private static VirtualDeviceConfigSpec createNicSpec(String netName, String nicName, Network network, DistributedVirtualSwitch dvs,
                                                         VirtualDeviceConfigSpecOperation op) throws Exception {

        logger.debug("Netname: " + netName);
        logger.debug("Nicname: " + nicName);
        logger.debug("Network: " + network.getName());
        logger.debug("DVS: " + dvs.getName());

        VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
        nicSpec.setOperation(op);

        String key = "";

        for (DistributedVirtualPortgroup dvpg : dvs.getPortgroup()) {
            if (dvpg.getName().equalsIgnoreCase(netName)) {
                key = dvpg.getConfig().getKey();
            }
        }

        String uuid = dvs.getConfig().getUuid();

        DistributedVirtualSwitchPortConnection switchCon = new DistributedVirtualSwitchPortConnection();
        switchCon.setSwitchUuid(uuid);
        switchCon.setPortgroupKey(key);

        VirtualEthernetCardDistributedVirtualPortBackingInfo portBacking = new VirtualEthernetCardDistributedVirtualPortBackingInfo();
        portBacking.setPort(switchCon);

        VirtualEthernetCard nic = new VirtualVmxnet3();

        Description info = new Description();
        info.setLabel(nicName);
        info.setSummary(netName);
        nic.setDeviceInfo(info);

        // type: "generated", "manual", "assigned" by VC
        nic.setAddressType("generated");
        nic.setBacking(portBacking);
        nic.setKey(0);

        nicSpec.setDevice(nic);
        return nicSpec;
    }

}
