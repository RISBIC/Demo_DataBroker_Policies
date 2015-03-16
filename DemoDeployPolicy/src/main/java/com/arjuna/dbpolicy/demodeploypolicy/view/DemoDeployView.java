/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbpolicy.demodeploypolicy.view;

import com.arjuna.agility.annotation.Feature;

public interface DemoDeployView
{
    @Feature(name = "demo.owner")
    public String getOwner();
    public void setOwner(String owner);

    @Feature(name = "demo.status")
    public String getStatus();
    public void setStatus(String status);

    @Feature(name = "demo.endpoint")
    public String getEndpoint();
    public void setEndpoint(String endpoint);

    @Feature(name = "demo.containsSocialCareRecords")
    public String getContainsSocialCareRecords();
    public void setContainsSocialCareRecords(String containsSocialCareRecords);

    @Feature(name = "demo.containsEducationalRecords")
    public String getContainsEducationalRecords();
    public void setContainsEducationalRecords(String containsEducationalRecords);

    @Feature(name = "demo.flowName", optional=true)
    public String getFlowName();
    public void setFlowName(String flowName);
}
