package com.gigaspaces.demo.client;

import com.gigaspaces.demo.common.Data;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.SpaceProxyConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static final String SPACE_NAME = "demo";
    protected GigaSpace gigaSpace;

    public Client() {
    }

    public void initialize() {
        SpaceProxyConfigurer configurer = new SpaceProxyConfigurer(SPACE_NAME);
        gigaSpace = new GigaSpaceConfigurer(configurer).gigaSpace();
    }

    public void setGigaSpace(GigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
    }

    // Write Data objects
    public void writeObjects(int numObjects) {
        for (int i = 0; i < numObjects; i++) {
            Data data = new Data();
            data.setId(i);
            data.setMessage(String.valueOf(i));
            data.setProcessed(Boolean.FALSE);
            gigaSpace.write(data);
        }
        logger.info("write is done...");
    }
}
