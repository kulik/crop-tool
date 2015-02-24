package com.tac.cropsample;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.tac.cropsample.tools.ImageUtil;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testPow2() {
        for (int i = 1; i < 200; i++) {
            Log.d("POW", "i=" + i + ", " + ImageUtil.pow2less(i));
        }
    }
}