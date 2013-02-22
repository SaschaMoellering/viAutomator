package com.zanox.coreservice.vmware.core.vo;

import com.zanox.coreservice.vmware.core.helper.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration-class
 *
 * @author Sascha Moellering (sascha.moellering@zanox.com)
 *
 */
public class Configuration {

    private static Configuration conf;

    private Properties props;

    private static final String CONFIG = "props/vmware.properties";

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);


    private String userName;
    private String password;
    private String url;
    private String dcName;
    private String guestOsId;
    private String diskMode;
    private String dataStoreName;
    private String netName;
    private String nicName;
    private String virtualNetwork;

    /**
     * private constructor for Configuration
     */
    private Configuration() {
        this.reload();
    }

    /**
     * Returns an instance of the Configuration-object
     *
     * @return The Configuration-object
     */
    public synchronized static Configuration getInstance() {

        if (conf == null) {
            conf = new Configuration();
        }
        return conf;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getUrl() {
        return url;
    }

    public String getDcName() {
        return dcName;
    }

    public String getGuestOsId() {
        return guestOsId;
    }

    public String getDiskMode() {
        return diskMode;
    }

    public String getDataStoreName() {
        return dataStoreName;
    }

    public String getNetName() {
        return netName;
    }

    public String getNicName() {
        return nicName;
    }

    public String getVirtualNetwork() {
        return virtualNetwork;
    }

    /**
     * Reloading the config
     */
    public void reload() {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(CONFIG);

        props = new Properties();
        try {
            logger.info("Loaging config ...");
            props.load(inputStream);
            this.init();
        }

        catch (IOException exc) {
            logger.error("Error", exc);
        }
    }

    private void init() {
        logger.info("In init");
        if (props != null) {
            logger.info("props != null");
            userName = props.getProperty(Constants.USERNAME);
            password = props.getProperty(Constants.PASSWORD);
            url = props.getProperty(Constants.URL);
            dcName = props.getProperty(Constants.DC_NAME);
            guestOsId = props.getProperty(Constants.GUEST_OS);
            diskMode = props.getProperty(Constants.DISK_MODE);
            dataStoreName = props.getProperty(Constants.DATA_STORE);
            netName = props.getProperty(Constants.NET_NAME);
            nicName = props.getProperty(Constants.NIC_NAME);
            virtualNetwork = props.getProperty(Constants.VIRTUAL_NETWORK);
        }
    }

}
