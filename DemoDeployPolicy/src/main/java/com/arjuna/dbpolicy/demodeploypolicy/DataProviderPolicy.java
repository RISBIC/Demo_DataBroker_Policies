/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved. 
 */

package com.arjuna.dbpolicy.demodeploypolicy;

import javax.ejb.Singleton;

import com.arjuna.agility.ServiceAgreement;
import com.arjuna.agility.ServiceAgreementContext;
import com.arjuna.agility.ServiceAgreementListener;
import com.arjuna.agility.ServiceAgreementListenerException;
import com.arjuna.agility.Vote;
import com.arjuna.dbpolicy.demodeploypolicy.view.DemoDeployView;

@Singleton
public class DataProviderPolicy implements ServiceAgreementListener
{
    public void onRegistered(String domain)
        throws ServiceAgreementListenerException
    {
    }

    public Vote onChangeProposed(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
         throws ServiceAgreementListenerException
    {
        if (serviceAgreement.isCompatible(DemoDeployView.class))
        {
            DemoDeployView deployView = serviceAgreement.asView(DemoDeployView.class);

            if ("active".equals(deployView.getStatus()))
            {
            	if ((! "no".equals(deployView.getContainsSocialCareRecords())) && (! "no".equals(deployView.getContainsEducationalRecords())))
                    return Vote.reject("Invalid SLA", "Inappropriate Service Agreement values have an 'Active' status.");
            	else
            	    return Vote.accept();
            }
            else
                return Vote.accept();
        }
        else
            return Vote.ignore();
    }

    public void onChanged(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
    }

    public void onChangeRejected(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
    }

    public Vote onTerminateProposed(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        if (serviceAgreement.isCompatible(DemoDeployView.class))
            return Vote.accept();
        else
            return Vote.ignore();
    }

    public void onTerminated(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
    }

    public void onTerminateRejected(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
    }

    public void onUnregistered(String domain)
        throws ServiceAgreementListenerException
    {
    }
}