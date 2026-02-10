package com.gigaspaces.demo.client;

import com.gigaspaces.demo.common.Data;
import com.j_spaces.core.client.SQLQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteReadExample extends Client {

    private static final Logger logger = LoggerFactory.getLogger(WriteReadExample.class);


    public WriteReadExample()  {
        super();
    }


    public Data[] queryWithReadMultiple() {

        SQLQuery<Data> query = new SQLQuery<>(Data.class, "");

        Data[] results = gigaSpace.readMultiple(query);

        if (results == null || results.length == 0) {
            logger.info("No results returned.");
            return results;
        }

        for (Data result : results) {
            logger.info("id: " + result.getId());
        }
        return results;
    }


    public static void main(String[] args) throws Exception {
        WriteReadExample example = new WriteReadExample();
        example.initialize();
        example.writeObjects(10);

        example.queryWithReadMultiple();

    }

}
