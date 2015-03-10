/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbpolicy.demodeploypolicy.view;

import com.arjuna.agility.annotation.Feature;

public interface DemoDeployView
{
    @Feature(name = "agility.demodeploy.status")
    public String getStatus();
    public void setStatus(String status);

    @Feature(name = "agility.demodeploy.flowname")
    public String getFlowName();
    public void setFlowName(String flowName);
}
