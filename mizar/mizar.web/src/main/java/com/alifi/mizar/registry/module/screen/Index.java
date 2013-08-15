package com.alifi.mizar.registry.module.screen;

import com.alibaba.citrus.turbine.Navigator;

public class Index {
    public void execute(Navigator nav) {
    	nav.forwardTo("services.vm");
    }
}
