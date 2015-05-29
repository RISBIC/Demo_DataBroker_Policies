/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbpolicy.demodeploypolicy;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class OpenShiftProperties
{
    public OpenShiftProperties(String openshiftPropertiesFilename)
    {
        _openshiftProperties = new Properties();

        try
        {
            File       openshiftConfigDir        = new File(System.getProperty("jboss.server.config.dir"));
            File       openshiftConfigFile       = new File(openshiftConfigDir, openshiftPropertiesFilename);
            FileReader openshiftConfigFileReader = new FileReader(openshiftConfigFile);
            _openshiftProperties.load(openshiftConfigFileReader);
            openshiftConfigFileReader.close();
            _loaded = true;
        }
        catch (IOException ioException)
        {
            _openshiftProperties = null;
            _loaded = false;
            throw new InternalError("Failed to load property: \"" + openshiftPropertiesFilename + "\"");
        }
    }

    public boolean isLoaded()
    {
        return _loaded;
    }

    public String getUsername()
    {
        if (_openshiftProperties != null)
        {
            String username = _openshiftProperties.getProperty("username");

            if (username != null)
                return username;
            else
                throw new InternalError("Failed to obtain \"username\" property");
        }
        else
            throw new InternalError("Failed to obtain \"username\" property, no property file");
    }

    public String getPassword()
    {
        if (_openshiftProperties != null)
        {
            String password = _openshiftProperties.getProperty("password");

            if (password != null)
                return password;
            else
                throw new InternalError("Failed to obtain \"password\" property");
        }
        else
            throw new InternalError("Failed to obtain \"password\" property, no property file");
    }

    public String getDomain()
    {
        if (_openshiftProperties != null)
        {
            String domain = _openshiftProperties.getProperty("domain");

            if (domain != null)
                return domain;
            else
                throw new InternalError("Failed to obtain \"domain\" property");
        }
        else
            throw new InternalError("Failed to obtain \"domain\" property, no property file");
    }

    private boolean    _loaded;
    private Properties _openshiftProperties = new Properties();
}
