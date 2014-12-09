/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved. 
 */

package com.arjuna.dbpolicy.demodeploypolicy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.w3c.dom.Document;

import com.arjuna.agility.ServiceAgreement;
import com.arjuna.agility.ServiceAgreementContext;
import com.arjuna.agility.ServiceAgreementListener;
import com.arjuna.agility.ServiceAgreementListenerException;
import com.arjuna.agility.Vote;
import com.arjuna.agility.view.Relationship;
import com.arjuna.databroker.data.DataFlow;
import com.arjuna.databroker.data.DataFlowFactory;
import com.arjuna.databroker.data.DataFlowInventory;
import com.arjuna.databroker.data.DataFlowNode;
import com.arjuna.databroker.data.DataFlowNodeFactory;
import com.arjuna.databroker.data.DataFlowNodeFactoryInventory;
import com.arjuna.databroker.data.DataProcessor;
import com.arjuna.databroker.data.DataService;
import com.arjuna.databroker.data.DataSource;
import com.arjuna.databroker.data.connector.ObservableDataProvider;
import com.arjuna.databroker.data.connector.ObserverDataConsumer;
import com.arjuna.databroker.data.jee.DataFlowNodeLifeCycleControl;
import com.arjuna.dbpolicy.demodeploypolicy.view.DemoDeployView;

@Stateless
public class DataConsumerPolicy implements ServiceAgreementListener
{
    private static final Logger logger = Logger.getLogger(DataConsumerPolicy.class.getName());

    public void onRegistered(String domain)
        throws ServiceAgreementListenerException
    {
    }

    public Vote onChangeProposed(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        ServiceAgreement previousServiceAgreement = serviceAgreementContext.getPrevious();

        if (serviceAgreement.isCompatible(Relationship.class))
            return Vote.accept();
        else if (serviceAgreement.isCompatible(DemoDeployView.class))
        {
            if (serviceAgreementContext.isLocal())
                return Vote.accept();
            else if (previousServiceAgreement == null)
            {
                DemoDeployView deployView = serviceAgreement.asView(DemoDeployView.class);

                Long pollInterval    = null;
                Long minPollInterval = null;
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

                if (pollInterval == null)
                    return Vote.reject("Invalid Deploy SLA Change", "Invalid poll interval");
                else if (minPollInterval == null)
                    return Vote.reject("Invalid Deploy SLA Change", "Invalid min poll interval");
                else if ((deployView.getFlowName() == null) || deployView.getFlowName().trim().equals(""))
                    return Vote.reject("Invalid Deploy SLA", "Invalid Flow Name");
                else if (minPollInterval < 0)
                    return Vote.reject("Invalid Deploy SLA", "Invalid negative");
                else
                    return Vote.accept();
            }
            else
            {
                DemoDeployView deployView         = serviceAgreement.asView(DemoDeployView.class);
                DemoDeployView previousDeployView = previousServiceAgreement.asView(DemoDeployView.class);

                Long previousPollInterval    = null;
                Long previousMinPollInterval = null;
                Long pollInterval            = null;
                Long minPollInterval         = null;
                try
                {
                    previousPollInterval = Long.parseLong(previousDeployView.getPollInterval());
                }
                catch (NumberFormatException numberFormatException)
                {
                    previousPollInterval = null;
                }
                try
                {
                    previousMinPollInterval = Long.parseLong(previousDeployView.getMinPollInterval());
                }
                catch (NumberFormatException numberFormatException)
                {
                    previousMinPollInterval = null;
                }
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

                if (previousPollInterval == pollInterval)
                    return Vote.reject("Invalid Deploy SLA Change", "Invalid Remote change to poll interval");
                else if (previousMinPollInterval == minPollInterval)
                    return Vote.reject("Invalid Deploy SLA Change", "Invalid Remote change to minimum poll interval");
                else
                    return Vote.accept();
            }
        }
        else
            return Vote.ignore();
    }

    public void onChanged(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        ServiceAgreement previousServiceAgreement = serviceAgreementContext.getPrevious();

        if (serviceAgreement.isCompatible(DemoDeployView.class) && (previousServiceAgreement == null))
        {
            DemoDeployView deployView = serviceAgreement.asView(DemoDeployView.class);

            System.err.println("onChanged[new]: FlowName:        " + deployView.getFlowName());
            System.err.println("onChanged[new]: PollInterval:    " + deployView.getPollInterval());
            System.err.println("onChanged[new]: MinPollInterval: " + deployView.getMinPollInterval());

            createDataFlow(deployView.getFlowName(), deployView.getPollInterval());
        }
        else if (serviceAgreement.isCompatible(DemoDeployView.class) && (previousServiceAgreement != null))
        {
            DemoDeployView deployView = serviceAgreement.asView(DemoDeployView.class);

            System.err.println("onChanged: FlowName:        " + deployView.getFlowName());
            System.err.println("onChanged: PollInterval:    " + deployView.getPollInterval());
            System.err.println("onChanged: MinPollInterval: " + deployView.getMinPollInterval());

            modifyDataFlow(deployView.getFlowName(), deployView.getPollInterval());
        }
    }

    public void onChangeRejected(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        if (serviceAgreement.isCompatible(DemoDeployView.class))
        {
            DemoDeployView sampleDeployView = serviceAgreement.asView(DemoDeployView.class);

            System.err.println("onChangeRejected: FlowName:        " + sampleDeployView.getFlowName());
            System.err.println("onChangeRejected: PollInterval:    " + sampleDeployView.getPollInterval());
            System.err.println("onChangeRejected: MinPollInterval: " + sampleDeployView.getMinPollInterval());
        }
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

    private void createDataFlow(String flowName, String pollInterval)
    {
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

            DataFlowNodeFactory webserviceDataFlowNodeFactories   = dataFlow.getDataFlowNodeFactoryInventory().getDataFlowNodeFactory("WebService Data Flow Node Factories");
            DataFlowNodeFactory directoryUpdateDataServiceFactory = dataFlow.getDataFlowNodeFactoryInventory().getDataFlowNodeFactory("Directory Update Data Service Factory");
            if ((webserviceDataFlowNodeFactories != null) && (directoryUpdateDataServiceFactory != null))
            {
                Map<String, String> dataSourceProperties = new HashMap<String, String>();
                dataSourceProperties.put("Service URL", "http://www.freewebservicesx.com/GetGoldPrice.asmx");
                dataSourceProperties.put("Operation Namespace", "http://freewebservicesx.com/");
                dataSourceProperties.put("Operation Name", "GetCurrentGoldPrice");
                dataSourceProperties.put("Schedule Delay (ms)", "0");
                dataSourceProperties.put("Schedule Period (ms)", pollInterval);
                dataSourceProperties.put("User Name", "A User Name");
                dataSourceProperties.put("Password", "A Password");
                Map<String, String> dataProcessorProperties = new HashMap<String, String>();
                Map<String, String> dataServiceProperties = new HashMap<String, String>();
                dataServiceProperties.put("Directory Name", "/tmp/out_dir");
                dataServiceProperties.put("File Name Prefix", "GoldPrice-");
                dataServiceProperties.put("File Name Postfix", ".xml");
                DataSource    dataSource    = webserviceDataFlowNodeFactories.createDataFlowNode("Gold Price Source", DataSource.class, Collections.<String, String>emptyMap(), dataSourceProperties);
                DataProcessor dataProcessor = webserviceDataFlowNodeFactories.createDataFlowNode("Converter Processor", DataProcessor.class, Collections.<String, String>emptyMap(), dataProcessorProperties);
                DataService   dataService   = directoryUpdateDataServiceFactory.createDataFlowNode("Gold Prices Service", DataService.class, Collections.<String, String>emptyMap(), dataServiceProperties);

                if (dataFlow == null)
                    logger.log(Level.WARNING, "dataFlow is null");
                if (dataSource == null)
                    logger.log(Level.WARNING, "dataSource is null");
                if (dataProcessor == null)
                    logger.log(Level.WARNING, "dataProcessor is null");
                if (dataService == null)
                    logger.log(Level.WARNING, "dataService is null");

                DataFlowNodeLifeCycleControl.processCreatedDataFlowNode(dataSource, dataFlow);
                DataFlowNodeLifeCycleControl.processCreatedDataFlowNode(dataProcessor, dataFlow);
                DataFlowNodeLifeCycleControl.processCreatedDataFlowNode(dataService, dataFlow);

                ((ObservableDataProvider<Document>) dataSource.getDataProvider(Document.class)).addDataConsumer((ObserverDataConsumer<Document>) dataProcessor.getDataConsumer(Document.class));
                ((ObservableDataProvider<String>) dataProcessor.getDataProvider(String.class)).addDataConsumer((ObserverDataConsumer<String>) dataService.getDataConsumer(String.class));
            }
            else
                logger.log(Level.WARNING, "Unable to find both DataFlowNode Factory");
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem when creating DataFlow", throwable);
        }
    }

    private void modifyDataFlow(String flowName, String pollInterval)
    {
        try
        {
            DataFlow dataFlow = _dataFlowInventory.getDataFlow(flowName);

            if (dataFlow != null)
            {
                DataFlowNode dataSource = dataFlow.getDataFlowNodeInventory().getDataFlowNode("Gold Price Source");

                if (dataSource != null)
                {
                    DataFlowNodeLifeCycleControl.enterReconfigDataFlowNode(dataSource);
                    Map<String, String> properties = new HashMap<String, String>(dataSource.getProperties());
                    properties.put("Schedule Period (ms)", pollInterval);
                    dataSource.setProperties(properties);
                    DataFlowNodeLifeCycleControl.exitReconfigDataFlowNode(dataSource);
                }
            }
            else
                logger.log(Level.WARNING, "Unable to find DataFlowNode: " + flowName);
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Unable to find DataFlow", throwable);
        }
    }

    @EJB(lookup="java:global/databroker/control-core/DataFlowFactory")
    private DataFlowFactory _dataFlowFactory;
    @EJB(lookup="java:global/databroker/control-core/DataFlowInventory")
    private DataFlowInventory _dataFlowInventory;
    @EJB(lookup="java:global/databroker/control-core/DataFlowNodeFactoryInventory")
    private DataFlowNodeFactoryInventory _dataFlowNodeFactoryInventory;
}
