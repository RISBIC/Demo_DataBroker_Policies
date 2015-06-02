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

            if ((state != null) && "deployed".equals(state) && (privacyImpactAssessmentView.getFlowName() == null))
            {
                logger.log(Level.FINE, "DataBrokerDeployPolicy.onChanged: now deployed");

                String flowName = UUID.randomUUID().toString();
                String endpoint = createDataFlow(flowName, "ckanapi.properties", "endpoint.properties", privacyImpactAssessmentView);

                if (endpoint != null)
                {
                    logger.log(Level.FINE, "DataBrokerDeployPolicy.onChanged: endpointed");

                    privacyImpactAssessmentView.setState("active");
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

            if (((previousState == null) || (! "deployed".equals(previousState))) && (state != null) && "deployed".equals(state) && (privacyImpactAssessmentView.getFlowName() == null))
            {
                logger.log(Level.FINE, "DataBrokerDeployPolicy.onChanged: now active");

                String flowName = UUID.randomUUID().toString();
                String endpoint = createDataFlow(flowName, "dkanapi.properties", "endpoint.properties", privacyImpactAssessmentView);

                if (endpoint != null)
                {
                    logger.log(Level.FINE, "DataBrokerDeployPolicy.onChanged: endpointed");

                    privacyImpactAssessmentView.setState("active");
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

    private String createDataFlow(String flowName, String dkanAPIPropertiesFilename, String endpointPropertiesFilename, PrivacyImpactAssessmentView privacyImpactAssessmentView)
    {
        logger.log(Level.FINE, "DataBrokerDeployPolicy.createDataFlow: " + flowName);

        try
        {
            Map<String, String> metaProperties = new HashMap<String, String>();
            Map<String, String> properties     = new HashMap<String, String>();
            metaProperties.put("Type", "Standard");

            DataFlow            dataFlow                                     = _dataFlowLifeCycleControl.createDataFlow(flowName, metaProperties, properties);
            DataFlowNodeFactory binaryServiceDataSourceFactory               = dataFlow.getDataFlowNodeFactoryInventory().getDataFlowNodeFactory("BinaryService Data Flow Node Factories");
            DataFlowNodeFactory spreadsheetMetadataExtractorProcessorFactory = dataFlow.getDataFlowNodeFactoryInventory().getDataFlowNodeFactory("Extra Spreadsheet Metadata Extractor Processor Factory");
            DataFlowNodeFactory ckanFileStoreDataServiceFactory              = dataFlow.getDataFlowNodeFactoryInventory().getDataFlowNodeFactory("File Store DKAN Data Flow Node Factories");

            if ((binaryServiceDataSourceFactory != null) && (spreadsheetMetadataExtractorProcessorFactory != null) && (ckanFileStoreDataServiceFactory != null))
            {
                DKANAPIProperties  dkanAPIProperties =  new DKANAPIProperties(dkanAPIPropertiesFilename);
                EndpointProperties endpointProperties = new EndpointProperties(endpointPropertiesFilename);

                String endpointId = UUID.randomUUID().toString();
                Map<String, String> dataSourceProperties = new HashMap<String, String>();
                dataSourceProperties.put("Endpoint Path", endpointId);
                Map<String, String> dataProcessorProperties = new HashMap<String, String>();
                dataProcessorProperties.put("Metadata Blog ID", UUID.randomUUID().toString());
                if (privacyImpactAssessmentView.getServiceRootURL().endsWith("/"))
                    dataProcessorProperties.put("Location", privacyImpactAssessmentView.getServiceRootURL() + "dataset/" + dkanAPIProperties.getPackageId());
                else
                    dataProcessorProperties.put("Location", privacyImpactAssessmentView.getServiceRootURL() + "/dataset/" + dkanAPIProperties.getPackageId());
                Map<String, String> dataServiceProperties = new HashMap<String, String>();
                dataServiceProperties.put("DKAN Root URL", privacyImpactAssessmentView.getServiceRootURL());
                dataServiceProperties.put("Package Id", dkanAPIProperties.getPackageId());
                dataServiceProperties.put("Username", dkanAPIProperties.getUsername());
                dataServiceProperties.put("Password", dkanAPIProperties.getPassword());
                DataSource    dataSource    = _dataFlowNodeLifeCycleControl.createDataFlowNode(dataFlow, binaryServiceDataSourceFactory, "Endpoint Source", DataSource.class, Collections.<String, String>emptyMap(), dataSourceProperties);
                DataProcessor dataProcessor = _dataFlowNodeLifeCycleControl.createDataFlowNode(dataFlow, spreadsheetMetadataExtractorProcessorFactory, "Metadata Extractor Processor", DataProcessor.class, Collections.<String, String>emptyMap(), dataProcessorProperties);
                DataService   dataService   = _dataFlowNodeLifeCycleControl.createDataFlowNode(dataFlow, ckanFileStoreDataServiceFactory, "Distribution Service", DataService.class, Collections.<String, String>emptyMap(), dataServiceProperties);

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

                return "http://" + endpointProperties.getHostname() + "/binaryservice/ws/endpoints/" + endpointId;
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
