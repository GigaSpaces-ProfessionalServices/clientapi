package com.gigaspaces.demo.event;

import com.gigaspaces.demo.common.Data;
import org.openspaces.events.EventDriven;
import org.openspaces.events.EventTemplate;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.Polling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventDriven
@Polling
public class SimplePollingContainer {

    private static final Logger logger = LoggerFactory.getLogger(SimplePollingContainer.class);


    @EventTemplate
    Data selectData() {
        Data template = new Data();
        template.setProcessed(false);
        return template;
    }

    @SpaceDataEvent
    public Data eventListener(Data event) {
        //process Data here
        event.setProcessed(true);
        logger.info("Data with id: {} has been processed.", event.getId());
        return event;
    }
}