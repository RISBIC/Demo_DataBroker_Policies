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
public class DKANDeployPolicy implements ServiceAgreementListener
{
    private static final Logger logger = Logger.getLogger(DKANDeployPolicy.class.getName());

    public void onRegistered(String domain)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "DKANDeployPolicy.onRegistered");
    }

    public Vote onChangeProposed(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "DKANDeployPolicy.onChangeProposed");

        ServiceAgreement previousServiceAgreement = serviceAgreementContext.getPrevious();

        if (serviceAgreement.isCompatible(PrivacyImpactAssessmentView.class))
        {
            logger.log(Level.FINE, "DKANDeployPolicy.onChangeProposed: matches");

            if (previousServiceAgreement == null)
                return Vote.accept();
            else if (previousServiceAgreement.isCompatible(PrivacyImpactAssessmentView.class))
                return Vote.accept();
            else
                return Vote.reject();
        }
        else
            return Vote.ignore();
    }

    public void onChanged(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "DKANDeployPolicy.onChanged");

        ServiceAgreement previousServiceAgreement = serviceAgreementContext.getPrevious();

        if (serviceAgreement.isCompatible(PrivacyImpactAssessmentView.class) && (previousServiceAgreement == null))
        {
            PrivacyImpactAssessmentView privacyImpactAssessmentView = serviceAgreement.asView(PrivacyImpactAssessmentView.class);

            logger.log(Level.FINE, "  onChanged[new]: State:    " + privacyImpactAssessmentView.getState());
            logger.log(Level.FINE, "  onChanged[new]: FlowName: " + privacyImpactAssessmentView.getFlowName());
            logger.log(Level.FINE, "  onChanged[new]: Endpoint: " + privacyImpactAssessmentView.getEndpoint());

            String state = privacyImpactAssessmentView.getState();

            if ((state != null) && "checked".equals(state) && (privacyImpactAssessmentView.getDKANHostname() == null))
            {
                logger.log(Level.FINE, "DKANDeployPolicy.onChanged: now ckecked");

                String flowName = UUID.randomUUID().toString();
                String endpoint = createDKAN(flowName, "ckanapi.properties", "endpoint.properties", privacyImpactAssessmentView);

                if (endpoint != null)
                {
                    logger.log(Level.FINE, "DKANDeployPolicy.onChanged: endpointed");

                    privacyImpactAssessmentView.setEndpoint(endpoint);
                    privacyImpactAssessmentView.setFlowName(flowName);

                    serviceAgreementContext.getServiceAgreementManager().propose(serviceAgreement);
                }
            }
        }
        else if (serviceAgreement.isCompatible(PrivacyImpactAssessmentView.class) && (previousServiceAgreement != null) && previousServiceAgreement.isCompatible(PrivacyImpactAssessmentView.class))
        {
            PrivacyImpactAssessmentView privacyImpactAssessmentView         = serviceAgreement.asView(PrivacyImpactAssessmentView.class);
            PrivacyImpactAssessmentView previousPrivacyImpactAssessmentView = previousServiceAgreement.asView(PrivacyImpactAssessmentView.class);

            logger.log(Level.FINE, "  onChanged: State:    " + privacyImpactAssessmentView.getState());
            logger.log(Level.FINE, "  onChanged: FlowName: " + privacyImpactAssessmentView.getFlowName());
            logger.log(Level.FINE, "  onChanged: Endpoint: " + privacyImpactAssessmentView.getEndpoint());

            String previousState = previousPrivacyImpactAssessmentView.getState();
            String state         = privacyImpactAssessmentView.getState();

            if (((previousState == null) || (! "active".equals(previousState))) && (state != null) && "active".equals(state) && (privacyImpactAssessmentView.getFlowName() == null))
            {
                logger.log(Level.FINE, "DKANDeployPolicy.onChanged: now active");

                String flowName = UUID.randomUUID().toString();
                String endpoint = createDKAN(flowName, "ckanapi.properties", "endpoint.properties", privacyImpactAssessmentView);

                if (endpoint != null)
                {
                    logger.log(Level.FINE, "DKANDeployPolicy.onChanged: endpointed");

                    privacyImpactAssessmentView.setFlowName(flowName);
                    privacyImpactAssessmentView.setEndpoint(endpoint);

                    serviceAgreementContext.getServiceAgreementManager().propose(serviceAgreement);
                }
            }
        }
    }

    public void onChangeRejected(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "DKANDeployPolicy.onChangeRejected");

        if (serviceAgreement.isCompatible(PrivacyImpactAssessmentView.class))
        {
            PrivacyImpactAssessmentView sampleDeployView = serviceAgreement.asView(PrivacyImpactAssessmentView.class);

            logger.log(Level.FINE, "onChangeRejected: FlowName: " + sampleDeployView.getFlowName());
        }
    }

    public Vote onTerminateProposed(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "DKANDeployPolicy.onTerminateProposed");

        if (serviceAgreement.isCompatible(PrivacyImpactAssessmentView.class))
            return Vote.accept();
        else
            return Vote.ignore();
    }

    public void onTerminated(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "DKANDeployPolicy.onTerminated");
    }

    public void onTerminateRejected(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "DKANDeployPolicy.onTerminateRejected");
    }

    public void onUnregistered(String domain)
         throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "DKANDeployPolicy.onUnregistered");
    }

    private String createDKAN(String flowName, String ckanAPIPropertiesFilename, String endpointPropertiesFilename, PrivacyImpactAssessmentView privacyImpactAssessmentView)
    {
        logger.log(Level.FINE, "DKANDeployPolicy.createDKAN: " + flowName);

        try
        {
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem when creating DKAN", throwable);
        }

        return null;
    }
}
