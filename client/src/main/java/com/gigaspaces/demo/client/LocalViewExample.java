package com.gigaspaces.demo.client;

import com.gigaspaces.demo.common.Data;
import com.j_spaces.core.client.SQLQuery;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.SpaceProxyConfigurer;
import org.openspaces.core.space.cache.LocalViewSpaceConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LocalViewExample extends Client {

    private static final Logger logger = LoggerFactory.getLogger(LocalViewExample.class);

    private GigaSpace localView;

    public LocalViewExample() {
        super();
    }
    public void setLocalView(GigaSpace localView) {
        this.localView = localView;
    }
    @Override
    public void initialize() throws Exception {
        super.initialize();
        SpaceProxyConfigurer configurer = new SpaceProxyConfigurer(Client.SPACE_NAME);
        gigaSpace = new GigaSpaceConfigurer(configurer).gigaSpace();

        LocalViewSpaceConfigurer localViewSpaceConfigurer =
                new LocalViewSpaceConfigurer(configurer).
                        addViewQuery(new SQLQuery(com.gigaspaces.demo.common.Data.class, ""));
        localView = new GigaSpaceConfigurer(localViewSpaceConfigurer).gigaSpace();
    }

    public void populate(int numObjects) {
        for(int i=0; i < numObjects; i++) {
            Data data = new Data();
            data.setId(i);
            data.setProcessed(false);
            data.setMessage("Message " + i);
            gigaSpace.write(data);
        }
    }

    public Object readFromLocalView() {


        /*
        localViewSpaceConfigurer.addProperty("space-config.engine.memory_usage.high_watermark_percentage", "97");
        localViewSpaceConfigurer.addProperty("space-config.engine.memory_usage.write_only_block_percentage", "96");
        localViewSpaceConfigurer.addProperty("space-config.engine.memory_usage.write_only_check_percentage", "95");
        localViewSpaceConfigurer.addProperty("space-config.engine.memory_usage.low_watermark_percentage", "94");
         */

        Object object = localView.read(new Object());
        logger.info("object is: " + object);
        return object;
    }


    public static void main(String[] args) throws Exception {
        LocalViewExample example = new LocalViewExample();
        example.initialize();
        try {
            example.populate(10);
            example.readFromLocalView();

            Thread.sleep(300000);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

}
