package com.udacity.stockhawk.ui;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by antti on 2017-04-25.
 */
public class NetworkReceiverTest {
    @Test
    public void isEnabled() throws Exception {
        NetworkReceiver networkReceiver = new NetworkReceiver(null);
        networkReceiver.setEnabled(true);
        assertTrue(networkReceiver.isEnabled());
    }

    @Test
    public void setEnabled() throws Exception {
        NetworkReceiver networkReceiver = new NetworkReceiver(null);
        networkReceiver.setEnabled(false);
        assertFalse(networkReceiver.isEnabled());

    }

}