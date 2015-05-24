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
import com.arjuna.databroker.data.DataSource;
import com.arjuna.databroker.data.core.DataFlowLifeCycleControl;
import com.arjuna.databroker.data.core.DataFlowNodeLifeCycleControl;
import com.arjuna.databroker.data.core.DataFlowNodeLinkLifeCycleControl;
import com.arjuna.dbpolicy.demodeploypolicy.view.AdvertAgreementView;;

@Stateless
public class CreateAdvertDeployPolicy implements ServiceAgreementListener
{
    private static final Logger logger = Logger.getLogger(CreateAdvertDeployPolicy.class.getName());

    public void onRegistered(String domain)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "CreateAdvertDeployPolicy.onRegistered");
    }

    public Vote onChangeProposed(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "CreateAdvertDeployPolicy.onChangeProposed");

        ServiceAgreement previousServiceAgreement = serviceAgreementContext.getPrevious();

        if (serviceAgreement.isCompatible(AdvertAgreementView.class))
        {
            logger.log(Level.FINE, "CreateAdvertDeployPolicy.onChangeProposed: matches");

            if (previousServiceAgreement == null)
                return Vote.accept();
            else if (previousServiceAgreement.isCompatible(AdvertAgreementView.class))
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
        logger.log(Level.FINE, "CreateAdvertDeployPolicy.onChanged");

        if (serviceAgreement.isCompatible(AdvertAgreementView.class))
        {
            AdvertAgreementView advertAgreementView = serviceAgreement.asView(AdvertAgreementView.class);

            logger.log(Level.FINE, "  onChanged[new]: FlowName: " + advertAgreementView.getFlowName());
            logger.log(Level.FINE, "  onChanged[new]: Endpoint: " + advertAgreementView.getEndpoint());

            if (advertAgreementView.getFlowName() == null)
            {
                logger.log(Level.FINE, "CreateAdvertDeployPolicy.onChanged: now active");

                String flowName = UUID.randomUUID().toString();
                String endpoint = createDataFlow(flowName, "endpoint.properties", advertAgreementView);

                if (endpoint != null)
                {
                    logger.log(Level.FINE, "CreateAdvertDeployPolicy.onChanged: endpointed");

                    advertAgreementView.setEndpoint(endpoint);
                    advertAgreementView.setFlowName(flowName);

                    serviceAgreementContext.getServiceAgreementManager().propose(serviceAgreement);
                }
            }
        }
    }

    public void onChangeRejected(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "CreateAdvertDeployPolicy.onChangeRejected");

        if (serviceAgreement.isCompatible(AdvertAgreementView.class))
        {
            AdvertAgreementView sampleDeployView = serviceAgreement.asView(AdvertAgreementView.class);

            logger.log(Level.FINE, "onChangeRejected: FlowName: " + sampleDeployView.getFlowName());
        }
    }

    public Vote onTerminateProposed(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "CreateAdvertDeployPolicy.onTerminateProposed");

        if (serviceAgreement.isCompatible(AdvertAgreementView.class))
            return Vote.accept();
        else
            return Vote.ignore();
    }

    public void onTerminated(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "CreateAdvertDeployPolicy.onTerminated");
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

    private String createDataFlow(String flowName, String endpointPropertiesFilename, AdvertAgreementView advertAgreementView)
    {
        logger.log(Level.FINE, "DataBrokerDeployPolicy.createDataFlow: " + flowName);

        try
        {
            Map<String, String> metaProperties = new HashMap<String, String>();
            Map<String, String> properties     = new HashMap<String, String>();
            metaProperties.put("Type", "Standard");

            DataFlow dataFlow = _dataFlowLifeCycleControl.createDataFlow(flowName, metaProperties, properties);

            DataFlowNodeFactory binaryServiceDataSourceFactory               = null;
            DataFlowNodeFactory spreadsheetMetadataExtractorProcessorFactory = null;
            if (dataFlow != null)
            {
                 binaryServiceDataSourceFactory               = dataFlow.getDataFlowNodeFactoryInventory().getDataFlowNodeFactory("BinaryService Data Flow Node Factories");
                 spreadsheetMetadataExtractorProcessorFactory = dataFlow.getDataFlowNodeFactoryInventory().getDataFlowNodeFactory("Spreadsheet Metadata Extractor Processor Factory");
            }
            else
                logger.log(Level.WARNING, "dataFlow is null");

            if ((binaryServiceDataSourceFactory != null) && (spreadsheetMetadataExtractorProcessorFactory != null))
            {
                EndpointProperties endpointProperties = new EndpointProperties(endpointPropertiesFilename);

                String endpointId = UUID.randomUUID().toString();
                Map<String, String> dataSourceProperties = new HashMap<String, String>();
                dataSourceProperties.put("Endpoint Path", endpointId);
                Map<String, String> dataProcessorProperties = new HashMap<String, String>();
                dataProcessorProperties.put("Metadata Blog ID", UUID.randomUUID().toString());
                DataSource    dataSource    = _dataFlowNodeLifeCycleControl.createDataFlowNode(dataFlow, binaryServiceDataSourceFactory, "Endpoint Source", DataSource.class, Collections.<String, String>emptyMap(), dataSourceProperties);
                DataProcessor dataProcessor = _dataFlowNodeLifeCycleControl.createDataFlowNode(dataFlow, spreadsheetMetadataExtractorProcessorFactory, "Metadata Extractor Processor", DataProcessor.class, Collections.<String, String>emptyMap(), dataProcessorProperties);

                if (dataSource == null)
                    logger.log(Level.WARNING, "dataSource is null");
                if (dataProcessor == null)
                    logger.log(Level.WARNING, "dataProcessor is null");

                _dataFlowNodeLifeCycleControl.completeCreationAndActivateDataFlowNode(UUID.randomUUID().toString(), dataSource, dataFlow);
                _dataFlowNodeLifeCycleControl.completeCreationAndActivateDataFlowNode(UUID.randomUUID().toString(), dataProcessor, dataFlow);

                _dataFlowNodeLinkLifeCycleControl.createDataFlowNodeLink(dataSource, dataProcessor, dataFlow);


                String hostname = System.getProperty("jboss.bind.address");

                return "http://" + endpointProperties.getEndpointRootURL() + "/binaryservice/ws/endpoints/" + endpointId;
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

    private String destroyDataFlow(String flowName)
    {
        logger.log(Level.FINE, "DataBrokerDeployPolicy.destroyDataFlow: " + flowName);

        try
        {
            DataFlow dataflow = null;

            if (dataflow != null)
                _dataFlowLifeCycleControl.removeDataFlow(dataflow);
            else
                logger.log(Level.WARNING, "Unable to find both DataFlow");
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
