/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbpolicy.demodeploypolicy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import com.arjuna.agility.ServiceAgreement;
import com.arjuna.agility.ServiceAgreementContext;
import com.arjuna.agility.ServiceAgreementListener;
import com.arjuna.agility.ServiceAgreementListenerException;
import com.arjuna.agility.Vote;
import com.arjuna.databroker.data.DataFlow;
import com.arjuna.databroker.data.DataFlowNodeFactory;
import com.arjuna.databroker.data.DataProcessor;
import com.arjuna.databroker.data.DataService;
import com.arjuna.databroker.data.DataSource;
import com.arjuna.databroker.data.core.DataFlowLifeCycleControl;
import com.arjuna.databroker.data.core.DataFlowNodeLifeCycleControl;
import com.arjuna.databroker.data.core.DataFlowNodeLinkLifeCycleControl;
import com.arjuna.dbpolicy.demodeploypolicy.view.PrivacyImpactAssessmentView;

@Stateless
public class SelectCKANPolicy implements ServiceAgreementListener
{
    private static final Logger logger = Logger.getLogger(SelectCKANPolicy.class.getName());

    public void onRegistered(String domain)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "SelectCKANPolicy.onRegistered");
    }

    public Vote onChangeProposed(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "SelectCKANPolicy.onChangeProposed");

        return Vote.accept();
    }

    public void onChanged(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "SelectCKANPolicy.onChanged");
    }

    public void onChangeRejected(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "SelectCKANPolicy.onChangeRejected");
    }

    public Vote onTerminateProposed(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "SelectCKANPolicy.onTerminateProposed");

        return Vote.accept();
    }

    public void onTerminated(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "SelectCKANPolicy.onTerminated");
    }

    public void onTerminateRejected(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "SelectCKANPolicy.onTerminateRejected");
    }

    public void onUnregistered(String domain)
         throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "SelectCKANPolicy.onUnregistered");
    }
}
