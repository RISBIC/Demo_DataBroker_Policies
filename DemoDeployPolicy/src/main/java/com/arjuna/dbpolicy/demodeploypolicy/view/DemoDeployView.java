/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved. 
 */

package com.arjuna.dbpolicy.demodeploypolicy.view;

import com.arjuna.agility.annotation.Feature;

public interface DemoDeployView
{
    @Feature(name = "agility.demodeploy.flowname")
    public String getFlowName();

    public void setFlowName(String flowName);

    @Feature(name = "agility.demodeploy.pollinterval")
    public String getPollInterval();

    public void setPollInterval(String pollInterval);

    @Feature(name = "agility.demodeploy.minpollinterval")
    public String getMinPollInterval();

    public void setMinPollInterval(String minPollInterval);
}
