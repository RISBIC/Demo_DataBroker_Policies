/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbpolicy.demodeploypolicy;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class DKANAPIProperties
{
    public DKANAPIProperties(String dkanAPIPropertiesFilename)
    {
        _dkanAPIProperties = new Properties();

        try
        {
            File       dkanAPIConfigDir        = new File(System.getProperty("jboss.server.config.dir"));
            File       dkanAPIConfigFile       = new File(dkanAPIConfigDir, dkanAPIPropertiesFilename);
            FileReader dkanAPIConfigFileReader = new FileReader(dkanAPIConfigFile);
            _dkanAPIProperties.load(dkanAPIConfigFileReader);
            dkanAPIConfigFileReader.close();
            _loaded = true;
        }
        catch (IOException ioException)
        {
            _dkanAPIProperties = null;
            _loaded = false;
            throw new InternalError("Failed to load property: \"" + dkanAPIPropertiesFilename + "\"");
        }
    }

    public boolean isLoaded()
    {
        return _loaded;
    }

    public String getDKANRootURL()
    {
        if (_dkanAPIProperties != null)
        {
            String dkanRootURL = _dkanAPIProperties.getProperty("dkanrooturl");

            if (dkanRootURL != null)
                return dkanRootURL;
            else
                throw new InternalError("Failed to obtain \"dkanrooturl\" property");
        }
        else
            throw new InternalError("Failed to obtain \"dkanrooturl\" property, no property file");
    }

    public String getPackageId()
    {
        if (_dkanAPIProperties != null)
        {
            String packageId = _dkanAPIProperties.getProperty("package_id");

            if (packageId != null)
                return packageId;
            else
                throw new InternalError("Failed to obtain \"package_id\" property");
        }
        else
            throw new InternalError("Failed to obtain \"package_id\" property, no property file");
    }

    public String getUsername()
    {
        if (_dkanAPIProperties != null)
        {
            String username = _dkanAPIProperties.getProperty("username");

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
        if (_dkanAPIProperties != null)
        {
            String password = _dkanAPIProperties.getProperty("password");

            if (password != null)
                return password;
            else
                throw new InternalError("Failed to obtain \"password\" property");
        }
        else
            throw new InternalError("Failed to obtain \"password\" property, no property file");
    }

    private boolean    _loaded;
    private Properties _dkanAPIProperties = new Properties();
}
