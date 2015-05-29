/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbpolicy.demodeploypolicy;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import org.risbic.databroker.openshift.v2.DKANApplication;
import org.risbic.databroker.openshift.v2.PHPApplication;
import com.arjuna.agility.ServiceAgreement;
import com.arjuna.agility.ServiceAgreementContext;
import com.arjuna.agility.ServiceAgreementListener;
import com.arjuna.agility.ServiceAgreementListenerException;
import com.arjuna.agility.Vote;
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

            if ((state != null) && "checked".equals(state) && (privacyImpactAssessmentView.getServiceRootURL() == null))
            {
                logger.log(Level.FINE, "DKANDeployPolicy.onChanged: now dkan deployed");

                String serviceRootURL = createDKAN("openshift.properties", privacyImpactAssessmentView);

                if (serviceRootURL != null)
                {
                    logger.log(Level.FINE, "DKANDeployPolicy.onChanged: dkanHostname");

                    privacyImpactAssessmentView.setState("deployed");
                    privacyImpactAssessmentView.setServiceRootURL(serviceRootURL);

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

            if (((previousState == null) || (! "checked".equals(previousState))) && (state != null) && "checked".equals(state) && (privacyImpactAssessmentView.getServiceRootURL() == null))
            {
                logger.log(Level.FINE, "DKANDeployPolicy.onChanged: now checked");

                String serviceRootURL = createDKAN("openshift.properties", privacyImpactAssessmentView);

                if (serviceRootURL != null)
                {
                    logger.log(Level.FINE, "DKANDeployPolicy.onChanged: serviceRootURL");

                    privacyImpactAssessmentView.setState("deployed");
                    privacyImpactAssessmentView.setServiceRootURL(serviceRootURL);

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

    private static final String RISBIC_DKAN_REPO = "git@github.com:RISBIC/dkan-openshift";

    private String generateAppName()
    {
       return UUID.randomUUID().toString().replace("-", "");
    }

    private String createDKAN(String openshiftPropertiesFilename, PrivacyImpactAssessmentView privacyImpactAssessmentView)
    {
        logger.log(Level.FINE, "DKANDeployPolicy.createDKAN");

        try
        {
            logger.log(Level.WARNING, "DKANDeployPolicy.createDKAN: body");

            OpenShiftProperties openshiftProperties = new OpenShiftProperties(openshiftPropertiesFilename);

            PHPApplication dkan = new DKANApplication(openshiftProperties.getUsername(), openshiftProperties.getPassword(), openshiftProperties.getDomain(), generateAppName(), RISBIC_DKAN_REPO);

            try
            {
                logger.log(Level.FINE, "DKANDeployPolicy.createDKAN: start deploy");
                
                dkan.deploy();
                dkan.start();

                logger.log(Level.FINE, "DKANDeployPolicy.createDKAN: end deploy");

                return dkan.getUrl();
            }
            catch (Throwable throwable)
            {
                logger.log(Level.FINE, "DKANDeployPolicy.createDKAN: deploy failed", throwable);
            }
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem when creating DKAN", throwable);
        }

        return null;
    }
}
