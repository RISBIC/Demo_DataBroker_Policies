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
                logger.log(Level.FINE, "DKANDeployPolicy.onChanged: try to deploy");

                DeployWorker deployWorker = new DeployWorker(privacyImpactAssessmentView, serviceAgreement, serviceAgreementContext);
                deployWorker.start();

                logger.log(Level.FINE, "DKANDeployPolicy.onChanged: finish of deploying");
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
                logger.log(Level.FINE, "DKANDeployPolicy.onChanged: try to deploy");

                DeployWorker deployWorker = new DeployWorker(privacyImpactAssessmentView, serviceAgreement, serviceAgreementContext);
                deployWorker.start();

                logger.log(Level.FINE, "DKANDeployPolicy.onChanged: finish of deploying");
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

    private class DeployWorker extends Thread
    {
        private static final String RISBIC_DKAN_REPO = "https://github.com/RISBIC/dkan-openshift.git";

        public DeployWorker(PrivacyImpactAssessmentView privacyImpactAssessmentView, ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        {
            logger.log(Level.FINE, "DeployWorker: created");

            _privacyImpactAssessmentView = privacyImpactAssessmentView;
            _serviceAgreement            = serviceAgreement;
            _serviceAgreementContext     = serviceAgreementContext;
        }

        public void run()
        {
            logger.log(Level.FINE, "DeployWorker: deploy started");

            String serviceRootURL = createDKAN("openshift.properties");

            if (serviceRootURL != null)
            {
                logger.log(Level.FINE, "DeployWorker: deploy success");

                _privacyImpactAssessmentView.setState("deployed");
                _privacyImpactAssessmentView.setServiceRootURL(serviceRootURL);

                _serviceAgreementContext.getServiceAgreementManager().propose(_serviceAgreement);
            }

            logger.log(Level.FINE, "DeployWorker: deploy ended");
        }

        private String generateAppName()
        {
           return UUID.randomUUID().toString().replace("-", "");
        }

        private String createDKAN(String openshiftPropertiesFilename)
        {
            logger.log(Level.FINE, "DeployWorker.createDKAN");

            try
            {
                logger.log(Level.WARNING, "DeployWorker.createDKAN: body");

                OpenShiftProperties openshiftProperties = new OpenShiftProperties(openshiftPropertiesFilename);

                PHPApplication dkan = new DKANApplication(openshiftProperties.getUsername(), openshiftProperties.getPassword(), openshiftProperties.getDomain(), generateAppName(), RISBIC_DKAN_REPO);

                try
                {
                    logger.log(Level.FINE, "DeployWorker.createDKAN: start deploy");

                    dkan.deploy();
                    dkan.start();

                    logger.log(Level.FINE, "DeployWorker.createDKAN: end deploy [" + dkan.getUrl() + "]");

                    return dkan.getUrl();
                }
                catch (Throwable throwable)
                {
                    logger.log(Level.FINE, "DeployWorker.createDKAN: deploy failed", throwable);
                }
            }
            catch (Throwable throwable)
            {
                logger.log(Level.WARNING, "Problem when creating DKAN", throwable);
            }

            return null;
        }

        private PrivacyImpactAssessmentView _privacyImpactAssessmentView;
        private ServiceAgreement            _serviceAgreement;
        private ServiceAgreementContext     _serviceAgreementContext;
    }
}
