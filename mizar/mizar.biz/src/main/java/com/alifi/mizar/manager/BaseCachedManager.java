package com.alifi.mizar.manager;

import java.util.Calendar;
import java.util.Date;

import com.alifi.mizar.manager.util.MapCache;

public abstract class BaseCachedManager {

    protected MapCache memCachedManager;

    protected abstract String addNameSpace(Object key);

    protected Date nextDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();
    }

    protected Date nextHour() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        return calendar.getTime();
    }

    public void setMemCachedManager(MapCache memCachedManager) {
        this.memCachedManager = memCachedManager;
    }
}
