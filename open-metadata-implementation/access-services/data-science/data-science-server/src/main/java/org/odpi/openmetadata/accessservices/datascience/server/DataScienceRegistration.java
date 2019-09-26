/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.accessservices.datascience.server;


import org.odpi.openmetadata.accessservices.datascience.admin.DataScienceAdmin;
import org.odpi.openmetadata.adminservices.configuration.OMAGAccessServiceRegistration;
import org.odpi.openmetadata.adminservices.configuration.registration.AccessServiceDescription;
import org.odpi.openmetadata.adminservices.configuration.registration.AccessServiceOperationalStatus;
import org.odpi.openmetadata.adminservices.configuration.registration.AccessServiceRegistration;

/**
 * DataScienceRegistration registers the access service with the OMAG Server administration services.
 * This registration must be driven once at server start up.  The OMAG Server administration services
 * then use this registration information as confirmation that there is an implementation of this
 * access service in the server and it can be configured and used.
 */
public class DataScienceRegistration
{
    /**
     * Pass information about this access service to the OMAG Server administration services.
     */
    public static void registerAccessService()
    {
        AccessServiceDescription myDescription = AccessServiceDescription.DATA_SCIENCE_OMAS;

        AccessServiceRegistration myRegistration = new AccessServiceRegistration(myDescription,
                                                                                 AccessServiceOperationalStatus.ENABLED,
                                                                                 DataScienceAdmin.class.getName());
        OMAGAccessServiceRegistration.registerAccessService(myRegistration);
    }
}
