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
public class DataBrokerDeployPolicy implements ServiceAgreementListener
{
    private static final Logger logger = Logger.getLogger(DataBrokerDeployPolicy.class.getName());

    public void onRegistered(String domain)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "DataBrokerDeployPolicy.onRegistered");
    }

    public Vote onChangeProposed(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "DataBrokerDeployPolicy.onChangeProposed");

        ServiceAgreement previousServiceAgreement = serviceAgreementContext.getPrevious();

        if (serviceAgreement.isCompatible(PrivacyImpactAssessmentView.class))
        {
            logger.log(Level.FINE, "DataBrokerDeployPolicy.onChangeProposed: matches");

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
        logger.log(Level.FINE, "DataBrokerDeployPolicy.onChanged");

        ServiceAgreement previousServiceAgreement = serviceAgreementContext.getPrevious();

        if (serviceAgreement.isCompatible(PrivacyImpactAssessmentView.class) && (previousServiceAgreement == null))
        {
            PrivacyImpactAssessmentView privacyImpactAssessmentView = serviceAgreement.asView(PrivacyImpactAssessmentView.class);

            logger.log(Level.FINE, "  onChanged[new]: State:    " + privacyImpactAssessmentView.getState());
            logger.log(Level.FINE, "  onChanged[new]: FlowName: " + privacyImpactAssessmentView.getFlowName());
            logger.log(Level.FINE, "  onChanged[new]: Endpoint: " + privacyImpactAssessmentView.getEndpoint());

            String state = privacyImpactAssessmentView.getState();

            if ((state != null) && "active".equals(state) && (privacyImpactAssessmentView.getFlowName() == null))
            {
                logger.log(Level.FINE, "DataBrokerDeployPolicy.onChanged: now active");

                String flowName = UUID.randomUUID().toString();
                String endpoint = createDataFlow(flowName, privacyImpactAssessmentView);

                if (endpoint != null)
                {
                    logger.log(Level.FINE, "DataBrokerDeployPolicy.onChanged: endpointed");

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
                logger.log(Level.FINE, "DataBrokerDeployPolicy.onChanged: now active");

                String flowName = UUID.randomUUID().toString();
                String endpoint = createDataFlow(flowName, privacyImpactAssessmentView);

                if (endpoint != null)
                {
                    logger.log(Level.FINE, "DataBrokerDeployPolicy.onChanged: endpointed");

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
        logger.log(Level.FINE, "DataBrokerDeployPolicy.onChangeRejected");

        if (serviceAgreement.isCompatible(PrivacyImpactAssessmentView.class))
        {
            PrivacyImpactAssessmentView sampleDeployView = serviceAgreement.asView(PrivacyImpactAssessmentView.class);

            logger.log(Level.FINE, "onChangeRejected: FlowName: " + sampleDeployView.getFlowName());
        }
    }

    public Vote onTerminateProposed(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "DataBrokerDeployPolicy.onTerminateProposed");

        if (serviceAgreement.isCompatible(PrivacyImpactAssessmentView.class))
            return Vote.accept();
        else
            return Vote.ignore();
    }

    public void onTerminated(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "DataBrokerDeployPolicy.onTerminated");
    }

    public void onTerminateRejected(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "DataBrokerDeployPolicy.onTerminateRejected");
    }

    public void onUnregistered(String domain)
         throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "DataBrokerDeployPolicy.onUnregistered");
    }

    private String createDataFlow(String flowName, PrivacyImpactAssessmentView privacyImpactAssessmentView)
    {
        logger.log(Level.FINE, "DataBrokerDeployPolicy.createDataFlow: " + flowName);

        try
        {
            Map<String, String> metaProperties = new HashMap<String, String>();
            Map<String, String> properties     = new HashMap<String, String>();
            metaProperties.put("Type", "Standard");

            DataFlow            dataFlow                                     = _dataFlowLifeCycleControl.createDataFlow(flowName, metaProperties, properties);
            DataFlowNodeFactory binaryServiceDataSourceFactory               = dataFlow.getDataFlowNodeFactoryInventory().getDataFlowNodeFactory("BinaryService Data Flow Node Factories");
            DataFlowNodeFactory spreadsheetMetadataExtractorProcessorFactory = dataFlow.getDataFlowNodeFactoryInventory().getDataFlowNodeFactory("Spreadsheet Metadata Extractor Processor Factory");
            DataFlowNodeFactory directoryUpdateDataServiceFactory            = dataFlow.getDataFlowNodeFactoryInventory().getDataFlowNodeFactory("Directory Update Data Service Factory");

            if ((binaryServiceDataSourceFactory != null) && (spreadsheetMetadataExtractorProcessorFactory != null) && (directoryUpdateDataServiceFactory != null))
            {
                String endpointId = UUID.randomUUID().toString();
                Map<String, String> dataSourceProperties = new HashMap<String, String>();
                dataSourceProperties.put("Endpoint Path", endpointId);
                Map<String, String> dataProcessorProperties = new HashMap<String, String>();
                dataProcessorProperties.put("Metadata Blog ID", UUID.randomUUID().toString());
                Map<String, String> dataServiceProperties = new HashMap<String, String>();
                dataServiceProperties.put("Directory Name", "/tmp");
                dataServiceProperties.put("File Name Prefix", "Spreadsheet-");
                dataServiceProperties.put("File Name Postfix", ".xslx");
                DataSource    dataSource    = _dataFlowNodeLifeCycleControl.createDataFlowNode(dataFlow, binaryServiceDataSourceFactory, "Endpoint Source", DataSource.class, Collections.<String, String>emptyMap(), dataSourceProperties);
                DataProcessor dataProcessor = _dataFlowNodeLifeCycleControl.createDataFlowNode(dataFlow, spreadsheetMetadataExtractorProcessorFactory, "Metadata Extractor Processor", DataProcessor.class, Collections.<String, String>emptyMap(), dataProcessorProperties);
                DataService   dataService   = _dataFlowNodeLifeCycleControl.createDataFlowNode(dataFlow, directoryUpdateDataServiceFactory, "Distribution Service", DataService.class, Collections.<String, String>emptyMap(), dataServiceProperties);

                if (dataFlow == null)
                    logger.log(Level.WARNING, "dataFlow is null");
                if (dataSource == null)
                    logger.log(Level.WARNING, "dataSource is null");
                if (dataProcessor == null)
                    logger.log(Level.WARNING, "dataProcessor is null");
                if (dataService == null)
                    logger.log(Level.WARNING, "dataService is null");

                _dataFlowNodeLifeCycleControl.completeCreationAndActivateDataFlowNode(UUID.randomUUID().toString(), dataSource, dataFlow);
                _dataFlowNodeLifeCycleControl.completeCreationAndActivateDataFlowNode(UUID.randomUUID().toString(), dataProcessor, dataFlow);
                _dataFlowNodeLifeCycleControl.completeCreationAndActivateDataFlowNode(UUID.randomUUID().toString(), dataService, dataFlow);

                _dataFlowNodeLinkLifeCycleControl.createDataFlowNodeLink(dataSource, dataProcessor, dataFlow);
                _dataFlowNodeLinkLifeCycleControl.createDataFlowNodeLink(dataProcessor, dataService, dataFlow);

//                String hostname = "publisherportal-arjunatech.rhcloud.com";
                String hostname = System.getProperty("jboss.bind.address");

                return "http://" + hostname + "/binaryservice/ws/endpoints/" + endpointId;
            }
            else
                logger.log(Level.WARNING, "Unable to find both DataFlowNode Factory");
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem when creating DataFlow", throwable);
        }

        return null;
    }

    @EJB(lookup="java:global/databroker/data-core-jee/DataFlowLifeCycleControl")
    private DataFlowLifeCycleControl _dataFlowLifeCycleControl;
    @EJB(lookup="java:global/databroker/data-core-jee/DataFlowNodeLifeCycleControl")
    private DataFlowNodeLifeCycleControl _dataFlowNodeLifeCycleControl;
    @EJB(lookup="java:global/databroker/data-core-jee/DataFlowNodeLinkLifeCycleControl")
    private DataFlowNodeLinkLifeCycleControl _dataFlowNodeLinkLifeCycleControl;
}
