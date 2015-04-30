/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbpolicy.demodeploypolicy.view;

import com.arjuna.agility.annotation.Feature;

public interface AdvertAgreementView
{
    @Feature(name = "advert.name")
    public String getName();
    public void setName(String name);


    @Feature(name = "gateway.state", optional=true)
    public String getState();
    public void setState(String state);

    @Feature(name = "gateway.endpoint", optional=true)
    public String getEndpoint();
    public void setEndpoint(String endpoint);


    @Feature(name = "advert.flowname", optional=true)
    public String getFlowName();
    public void setFlowName(String flowName);
}
