package com.kongtech.plutocon.sdk;

import java.util.ArrayList;
import java.util.List;

public class MonitoringResult {

    private List<Plutocon> plutocons;

    public MonitoringResult(){
        plutocons = new ArrayList<>();
    }

    public void updateSensor(Plutocon plutocon,int position){
        Plutocon p = plutocons.get(position);
        plutocon.updateInterval(p.getLastSeenMillis());
        plutocons.set(position, plutocon);
    }

    public void addSensor(Plutocon plutocon){
        if(plutocons.contains(plutocon)) return;
        plutocon.updateInterval(plutocon.getLastSeenMillis());
        plutocons.add(plutocon);
    }

    public int isContained(Plutocon plutocon){
        return plutocons.indexOf(plutocon);
    }

    public List<Plutocon> getList() {
        return plutocons;
    }

    public void clear(){
        plutocons.clear();
    }
}
