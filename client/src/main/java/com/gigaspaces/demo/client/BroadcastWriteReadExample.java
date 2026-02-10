package com.gigaspaces.demo.client;

import com.gigaspaces.demo.common.BroadcastData;
import com.gigaspaces.demo.common.Data;
import com.j_spaces.core.client.SQLQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BroadcastWriteReadExample extends Client {

    private static final Logger logger = LoggerFactory.getLogger(BroadcastWriteReadExample.class);


    public BroadcastWriteReadExample()  {
        super();
    }

    public void writeObjects(int numObjects) {
        for (int i = 0; i < numObjects; i++) {
            BroadcastData data = new BroadcastData();
            data.setId(i);
            data.setMessage(String.valueOf(i));
            data.setProcessed(Boolean.FALSE);
            gigaSpace.write(data);
        }
        logger.info("write is done...");
    }

    public BroadcastData[] queryWithReadMultiple() {

        SQLQuery<BroadcastData> query = new SQLQuery<>(BroadcastData.class, "");

        BroadcastData[] results = gigaSpace.readMultiple(query);

        if (results == null || results.length == 0) {
            logger.info("No results returned.");
            return results;
        }

        for (BroadcastData result : results) {
            logger.info("id: " + result.getId());
        }
        return results;
    }


    public static void main(String[] args) throws Exception {
        BroadcastWriteReadExample example = new BroadcastWriteReadExample();
        example.initialize();
        example.writeObjects(10);

        example.queryWithReadMultiple();
    }

}
