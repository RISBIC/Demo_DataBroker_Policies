/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbpolicy.demodeploypolicy;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class CKANAPIProperties
{
    public CKANAPIProperties(String ckanAPIPropertiesFilename)
    {
        _ckanAPIProperties = new Properties();

        try
        {
            File       ckanAPIConfigFile       = new File(System.getProperty("jboss.server.config.dir"), ckanAPIPropertiesFilename);
            FileReader ckanAPIConfigFileReader = new FileReader(ckanAPIPropertiesFilename);
            _ckanAPIProperties.load(ckanAPIConfigFileReader);
            ckanAPIConfigFileReader.close();
            _loaded = true;
        }
        catch (IOException ioException)
        {
            _ckanAPIProperties = null;
            _loaded = false;
        }
    }

    public boolean isLoaded()
    {
        return _loaded;
    }

    public String getCKANRootURL()
    {
        if (_ckanAPIProperties != null)
        {
            String ckanRootURL = _ckanAPIProperties.getProperty("ckanrooturl");

            if (ckanRootURL != null)
                return ckanRootURL;
            else
                throw new InternalError("Failed to obtain \"ckanrooturl\" property");
        }
        else
            throw new InternalError("Failed to obtain \"ckanrooturl\" property, no property file");
    }

    public String getPackageId()
    {
        if (_ckanAPIProperties != null)
        {
            String packageId = _ckanAPIProperties.getProperty("package_id");

            if (packageId != null)
                return packageId;
            else
                throw new InternalError("Failed to obtain \"package_id\" property");
        }
        else
            throw new InternalError("Failed to obtain \"package_id\" property, no property file");
    }

    public String getAPIKey()
    {
        if (_ckanAPIProperties != null)
        {
            String apiKey = _ckanAPIProperties.getProperty("apikey");

            if (apiKey != null)
                return apiKey;
            else
                throw new InternalError("Failed to obtain \"apikey\" property");
        }
        else
            throw new InternalError("Failed to obtain \"apikey\" property, no property file");
    }

    private boolean    _loaded;
    private Properties _ckanAPIProperties = new Properties();
}
