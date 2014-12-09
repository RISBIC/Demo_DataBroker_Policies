/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved. 
 */

package com.arjuna.dbpolicy.demodeploypolicy;

import javax.ejb.Singleton;

import com.arjuna.agility.ServiceAgreement;
import com.arjuna.agility.ServiceAgreementContext;
import com.arjuna.agility.ServiceAgreementListener;
import com.arjuna.agility.ServiceAgreementListenerException;
import com.arjuna.agility.Vote;
import com.arjuna.agility.view.Relationship;
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
        ServiceAgreement previousServiceAgreement = serviceAgreementContext.getPrevious();

        if (serviceAgreement.isCompatible(Relationship.class))
        {
            if (! serviceAgreementContext.isLocal())
                return Vote.reject("Invalid Relationship", "Remote creation/modification of Relationships not supported");
            else
                return Vote.accept();
        }
        else if (serviceAgreement.isCompatible(DemoDeployView.class))
        {
            if (serviceAgreementContext.isLocal())
                return Vote.accept();
            else if ((previousServiceAgreement == null) || (! previousServiceAgreement.isCompatible(DemoDeployView.class)))
                return Vote.reject("Invalid Deploy SLA", "Remotely created or invalid history");
            else
            {
                DemoDeployView deployView         = serviceAgreement.asView(DemoDeployView.class);
                DemoDeployView previousDeployView = previousServiceAgreement.asView(DemoDeployView.class);

                Long pollInterval            = null;
                Long minPollInterval         = null;
                Long previousMinPollInterval = null;
                try
                {
                    pollInterval = Long.parseLong(deployView.getPollInterval());
                }
                catch (NumberFormatException numberFormatException)
                {
                    pollInterval = null;
                }
                try
                {
                    minPollInterval = Long.parseLong(deployView.getMinPollInterval());
                }
                catch (NumberFormatException numberFormatException)
                {
                    minPollInterval = null;
                }
                try
                {
                    previousMinPollInterval = Long.parseLong(previousDeployView.getMinPollInterval());
                }
                catch (NumberFormatException numberFormatException)
                {
                    previousMinPollInterval = null;
                }

                if (pollInterval == null)
                    return Vote.reject("Invalid Deploy SLA Change", "Invalid poll interval");
                else if (minPollInterval == null)
                    return Vote.reject("Invalid Deploy SLA Change", "Invalid min poll interval");
                else if (pollInterval < minPollInterval)
                    return Vote.reject("Invalid Deploy SLA Change", "Invalid Remote change to poll interval");
                else if (minPollInterval == previousMinPollInterval)
                    return Vote.reject("Invalid Deploy SLA Change", "Remote change to minimum poll interval");
                else
                    return Vote.accept();
            }
        }
        else
            return Vote.reject();
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
        if (serviceAgreement.isCompatible(Relationship.class))
            return Vote.accept();
        else if (serviceAgreement.isCompatible(DemoDeployView.class))
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