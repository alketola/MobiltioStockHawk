package com.udacity.stockhawk.ui;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by antti on 2017-04-25.
 */
public class UiUtilTest {
    @Test
    public void spaceOutAcronym() throws Exception {
        String stringIn = "ABCDEFGH";
        String expected = "A B C D E F G H ";
        String stringOut = UiUtil.spaceOutAcronym(stringIn);
        assertArrayEquals(expected.toCharArray(), stringOut.toCharArray());
    }

}