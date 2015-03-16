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
import com.arjuna.databroker.data.DataFlowFactory;
import com.arjuna.databroker.data.DataFlowInventory;
import com.arjuna.databroker.data.DataFlowNodeFactory;
import com.arjuna.databroker.data.DataFlowNodeFactoryInventory;
import com.arjuna.databroker.data.DataProcessor;
import com.arjuna.databroker.data.DataService;
import com.arjuna.databroker.data.DataSource;
import com.arjuna.databroker.data.connector.ObservableDataProvider;
import com.arjuna.databroker.data.connector.ObserverDataConsumer;
import com.arjuna.databroker.data.core.DataFlowNodeLifeCycleControl;
import com.arjuna.databroker.data.core.DataFlowNodeLinkLifeCycleControl;
import com.arjuna.dbpolicy.demodeploypolicy.view.DemoDeployView;

@Stateless
public class DataConsumerPolicy implements ServiceAgreementListener
{
    private static final Logger logger = Logger.getLogger(DataConsumerPolicy.class.getName());

    public void onRegistered(String domain)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "DataConsumerPolicy.onRegistered");
    }

    public Vote onChangeProposed(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "DataConsumerPolicy.onChangeProposed");

        ServiceAgreement previousServiceAgreement = serviceAgreementContext.getPrevious();

        if (serviceAgreement.isCompatible(DemoDeployView.class))
        {
            if (previousServiceAgreement == null)
                return Vote.accept();
            else if (previousServiceAgreement.isCompatible(DemoDeployView.class))
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
        logger.log(Level.FINE, "DataConsumerPolicy.onChanged");

        ServiceAgreement previousServiceAgreement = serviceAgreementContext.getPrevious();

        if (serviceAgreement.isCompatible(DemoDeployView.class) && (previousServiceAgreement == null))
        {
            DemoDeployView deployView = serviceAgreement.asView(DemoDeployView.class);

            System.err.println("onChanged[new]: Status:   " + deployView.getStatus());
            System.err.println("onChanged[new]: FlowName: " + deployView.getFlowName());

            if ("active".equals(deployView.getStatus()))
            {
                String flowName = UUID.randomUUID().toString();
                String endpoint = createDataFlow(flowName, deployView);

                if (endpoint != null)
                {
                    deployView.setFlowName(flowName);
                    deployView.setEndpoint(endpoint);

                    serviceAgreementContext.getServiceAgreementManager().propose(serviceAgreement);
                }
            }
        }
        else if (serviceAgreement.isCompatible(DemoDeployView.class) && (previousServiceAgreement != null) && previousServiceAgreement.isCompatible(DemoDeployView.class))
        {
            DemoDeployView deployView         = serviceAgreement.asView(DemoDeployView.class);
            DemoDeployView previousDeployView = previousServiceAgreement.asView(DemoDeployView.class);

            System.err.println("onChanged: Status:                     " + deployView.getStatus());
            System.err.println("onChanged: FlowName:                   " + deployView.getFlowName());
            System.err.println("onChanged: ContainsSocialCareRecords:  " + deployView.getContainsSocialCareRecords());
            System.err.println("onChanged: ContainsEducationalRecords: " + deployView.getContainsEducationalRecords());
            System.err.println("onChanged: Previous Status:            " + previousDeployView.getStatus());
            System.err.println("onChanged: Previous FlowName:          " + previousDeployView.getFlowName());

            if ((! "active".equals(previousDeployView.getStatus())) && "active".equals(deployView.getStatus()))
            {
                String flowName = UUID.randomUUID().toString();
                String endpoint = createDataFlow(flowName, deployView);

                if (endpoint != null)
                {
                    deployView.setFlowName(flowName);
                    deployView.setEndpoint(endpoint);

                    serviceAgreementContext.getServiceAgreementManager().propose(serviceAgreement);
                }
            }

            modifyDataFlow(deployView.getFlowName(), deployView);
        }
    }

    public void onChangeRejected(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "DataConsumerPolicy.onChangeRejected");

        if (serviceAgreement.isCompatible(DemoDeployView.class))
        {
            DemoDeployView sampleDeployView = serviceAgreement.asView(DemoDeployView.class);

            System.err.println("onChangeRejected: FlowName: " + sampleDeployView.getFlowName());
        }
    }

    public Vote onTerminateProposed(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "DataConsumerPolicy.onTerminateProposed");

        if (serviceAgreement.isCompatible(DemoDeployView.class))
            return Vote.accept();
        else
            return Vote.ignore();
    }

    public void onTerminated(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "DataConsumerPolicy.onTerminated");
    }

    public void onTerminateRejected(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "DataConsumerPolicy.onTerminateRejected");
    }

    public void onUnregistered(String domain)
         throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "DataConsumerPolicy.onUnregistered");
    }

    private String createDataFlow(String flowName, DemoDeployView deployView)
    {
        logger.log(Level.FINE, "DataConsumerPolicy.createDataFlow: " + flowName);

        try
        {
            Map<String, String> metaProperties = new HashMap<String, String>();
            Map<String, String> properties     = new HashMap<String, String>();
            metaProperties.put("Type", "Standard");

            DataFlow dataFlow = _dataFlowFactory.createDataFlow(flowName, metaProperties, properties);
            for (DataFlowNodeFactory dataFlowNodeFactory: _dataFlowNodeFactoryInventory.getDataFlowNodeFactorys())
            {
                System.err.println("createDataFlow - dataFlowNodeFactory: " + dataFlowNodeFactory.getName());
                dataFlow.getDataFlowNodeFactoryInventory().addDataFlowNodeFactory(dataFlowNodeFactory);
            }
            _dataFlowInventory.addDataFlow(dataFlow);

            DataFlowNodeFactory binaryServiceDataSourceFactory               = dataFlow.getDataFlowNodeFactoryInventory().getDataFlowNodeFactory("BinaryService Data Flow Node Factories");
            DataFlowNodeFactory spreadsheetMetadataExtractorProcessorFactory = dataFlow.getDataFlowNodeFactoryInventory().getDataFlowNodeFactory("Spreadsheet Metadata Extractor Processor Factory");
            DataFlowNodeFactory directoryUpdateDataServiceFactory            = dataFlow.getDataFlowNodeFactoryInventory().getDataFlowNodeFactory("Directory Update Data Service Factory");

            if ((binaryServiceDataSourceFactory != null) && (spreadsheetMetadataExtractorProcessorFactory != null) && (directoryUpdateDataServiceFactory != null))
            {
                String endpointId = UUID.randomUUID().toString();
                Map<String, String> dataSourceProperties = new HashMap<String, String>();
                dataSourceProperties.put("Endpoint Path", endpointId);
                Map<String, String> dataProcessorProperties = new HashMap<String, String>();
                dataProcessorProperties.put("Metadata Blog ID", "23533ebc-e311-4bd4-b773-5afc34028a07");
                Map<String, String> dataServiceProperties = new HashMap<String, String>();
                dataServiceProperties.put("Directory Name", "/tmp");
                dataServiceProperties.put("File Name Prefix", "Spreadsheet-");
                dataServiceProperties.put("File Name Postfix", ".xslx");
                DataSource    dataSource    = binaryServiceDataSourceFactory.createDataFlowNode("Endpoint Source", DataSource.class, Collections.<String, String>emptyMap(), dataSourceProperties);
                DataProcessor dataProcessor = spreadsheetMetadataExtractorProcessorFactory.createDataFlowNode("Metadata Extractor Processor", DataProcessor.class, Collections.<String, String>emptyMap(), dataProcessorProperties);
                DataService   dataService   = directoryUpdateDataServiceFactory.createDataFlowNode("Distribution Service", DataService.class, Collections.<String, String>emptyMap(), dataServiceProperties);

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

                ((ObservableDataProvider<byte[]>) dataSource.getDataProvider(byte[].class)).addDataConsumer((ObserverDataConsumer<byte[]>) dataProcessor.getDataConsumer(byte[].class));
                ((ObservableDataProvider<byte[]>) dataProcessor.getDataProvider(byte[].class)).addDataConsumer((ObserverDataConsumer<byte[]>) dataService.getDataConsumer(byte[].class));

                String hostname = "localhost";
                return "http://" + hostname + "/binaryservice/servlet/endpoints/" + endpointId;
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

    private void modifyDataFlow(String flowName, DemoDeployView deployView)
    {
        logger.log(Level.FINE, "DataConsumerPolicy.modifyDataFlow: " + flowName);
    }

    @EJB(lookup="java:global/databroker/data-core-jee/DataFlowFactory")
    private DataFlowFactory _dataFlowFactory;
    @EJB(lookup="java:global/databroker/data-core-jee/DataFlowInventory")
    private DataFlowInventory _dataFlowInventory;
    @EJB(lookup="java:global/databroker/data-core-jee/DataFlowNodeFactoryInventory")
    private DataFlowNodeFactoryInventory _dataFlowNodeFactoryInventory;
    @EJB(lookup="java:global/databroker/data-core-jee/DataFlowNodeLifeCycleControl")
    private DataFlowNodeLifeCycleControl _dataFlowNodeLifeCycleControl;
    @EJB(lookup="java:global/databroker/data-core-jee/DataFlowNodeLinkLifeCycleControl")
    private DataFlowNodeLinkLifeCycleControl _dataFlowNodeLinkLifeCycleControl;
}
