/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbpolicy.demodeploypolicy;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class EndpointProperties
{
    public EndpointProperties(String endpointPropertiesFilename)
    {
        _endpointProperties = new Properties();

        try
        {
            File       endpointConfigDir        = new File(System.getProperty("jboss.server.config.dir"));
            File       endpointConfigFile       = new File(endpointConfigDir, endpointPropertiesFilename);
            FileReader endpointConfigFileReader = new FileReader(endpointConfigFile);
            _endpointProperties.load(endpointConfigFileReader);
            endpointConfigFileReader.close();
            _loaded = true;
        }
        catch (IOException ioException)
        {
            _endpointProperties = null;
            _loaded = false;
            throw new InternalError("Failed to load property: \"" + endpointPropertiesFilename + "\"");
        }
    }

    public boolean isLoaded()
    {
        return _loaded;
    }

    public String getEndpointRootURL()
    {
        if (_endpointProperties != null)
        {
            String endpointRootURL = _endpointProperties.getProperty("endpointrooturl");

            if (endpointRootURL != null)
                return endpointRootURL;
            else
                throw new InternalError("Failed to obtain \"endpointrooturl\" property");
        }
        else
            throw new InternalError("Failed to obtain \"endpointrooturl\" property, no property file");
    }

    private boolean    _loaded;
    private Properties _endpointProperties = new Properties();
}
