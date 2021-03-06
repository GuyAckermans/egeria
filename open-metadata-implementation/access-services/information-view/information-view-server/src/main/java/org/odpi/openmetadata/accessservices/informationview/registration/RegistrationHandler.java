/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.accessservices.informationview.registration;

import org.odpi.openmetadata.accessservices.informationview.contentmanager.OMEntityDao;
import org.odpi.openmetadata.accessservices.informationview.contentmanager.OMEntityWrapper;
import org.odpi.openmetadata.accessservices.informationview.events.RegistrationRequestBody;
import org.odpi.openmetadata.accessservices.informationview.events.SoftwareServerCapabilitySource;
import org.odpi.openmetadata.accessservices.informationview.ffdc.ExceptionHandler;
import org.odpi.openmetadata.accessservices.informationview.ffdc.InformationViewErrorCode;
import org.odpi.openmetadata.accessservices.informationview.ffdc.exceptions.runtime.RegistrationException;
import org.odpi.openmetadata.accessservices.informationview.lookup.SoftwareServerCapabilityLookup;
import org.odpi.openmetadata.accessservices.informationview.utils.Constants;
import org.odpi.openmetadata.accessservices.informationview.utils.EntityPropertiesBuilder;
import org.odpi.openmetadata.repositoryservices.auditlog.OMRSAuditLog;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.EntityDetail;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.InstanceProperties;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.repositoryconnector.OMRSRepositoryConnector;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.repositoryconnector.OMRSRepositoryHelper;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.ClassificationErrorException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.EntityNotKnownException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.FunctionNotSupportedException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.InvalidParameterException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.PagingErrorException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.PropertyErrorException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.RepositoryErrorException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.StatusNotSupportedException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.TypeErrorException;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.UserNotAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Optional;

public class RegistrationHandler {


    private static final Logger log = LoggerFactory.getLogger(RegistrationHandler.class);
    private org.odpi.openmetadata.accessservices.informationview.contentmanager.OMEntityDao omEntityDao;
    private OMRSRepositoryHelper repositoryHelper;
    private OMRSAuditLog auditLog;
    private SoftwareServerCapabilityLookup lookup;

    public RegistrationHandler(OMEntityDao omEntityDao, OMRSRepositoryConnector enterpriseConnector,
                               OMRSAuditLog auditLog) {
        this.omEntityDao = omEntityDao;
        this.repositoryHelper = enterpriseConnector.getRepositoryHelper();
        this.auditLog = auditLog;
        this.lookup = new SoftwareServerCapabilityLookup(enterpriseConnector,omEntityDao,null,auditLog);
    }


    public SoftwareServerCapabilitySource registerTool(RegistrationRequestBody requestBody) {

        SoftwareServerCapabilitySource softwareServerCapability = requestBody.getSoftwareServerCapability();
        if(StringUtils.isEmpty(softwareServerCapability.getQualifiedName())){
            throw new RegistrationException(
                    InformationViewErrorCode.REGISTRATION_EXCEPTION.getHttpErrorCode(),
                    RegistrationHandler.class.getName(),
                    InformationViewErrorCode.REGISTRATION_EXCEPTION.getFormattedErrorMessage("No qualified name was provided. This is mandatory."),
                    InformationViewErrorCode.REGISTRATION_EXCEPTION.getSystemAction(),
                    InformationViewErrorCode.REGISTRATION_EXCEPTION.getUserAction(),
                    null);
        }

        String qualifiedNameForSoftwareServer = softwareServerCapability.getQualifiedName();
        InstanceProperties softwareServerProperties = new EntityPropertiesBuilder()
                .withStringProperty(Constants.QUALIFIED_NAME, qualifiedNameForSoftwareServer)
                .withStringProperty(Constants.PATCH_LEVEL, softwareServerCapability.getPatchLevel())
                .withStringProperty(Constants.TYPE, softwareServerCapability.getType())
                .withStringProperty(Constants.VERSION, softwareServerCapability.getVersion())
                .withStringProperty(Constants.SOURCE, softwareServerCapability.getSource())
                .withStringProperty(Constants.NAME, softwareServerCapability.getName())
                .withStringProperty(Constants.DESCRIPTION, softwareServerCapability.getDescription())
                .build();

        OMEntityWrapper registration;
        try {
            registration = omEntityDao.createOrUpdateEntity(Constants.SOFTWARE_SERVER_CAPABILITY,
                                                            qualifiedNameForSoftwareServer,
                                                            softwareServerProperties,
                                                            null,
                                                            true,
                                                            true);

            return buildSoftwareServerCapabilitySource(registration.getEntityDetail());
        } catch (InvalidParameterException | StatusNotSupportedException | PropertyErrorException | EntityNotKnownException | TypeErrorException | FunctionNotSupportedException | PagingErrorException | ClassificationErrorException | UserNotAuthorizedException | RepositoryErrorException e) {
            throw new RegistrationException(
                    InformationViewErrorCode.REGISTRATION_EXCEPTION.getHttpErrorCode(),
                    RegistrationHandler.class.getName(),
                    InformationViewErrorCode.REGISTRATION_EXCEPTION.getFormattedErrorMessage(e.getMessage()),
                    InformationViewErrorCode.REGISTRATION_EXCEPTION.getSystemAction(),
                    InformationViewErrorCode.REGISTRATION_EXCEPTION.getUserAction(),
                    null);
        }
    }


    public SoftwareServerCapabilitySource lookupSoftwareServerCapability(RegistrationRequestBody requestBody) {
        EntityDetail entity =
                Optional.ofNullable(lookup.lookupEntity(requestBody.getSoftwareServerCapability()))
                        .orElseThrow(() -> ExceptionHandler.buildEntityNotFoundException(Constants.SOURCE,
                                                                                        requestBody.getSoftwareServerCapability().toString(),
                                                                                        Constants.SOFTWARE_SERVER_CAPABILITY,
                                                                                        this.getClass().getName()));
        return buildSoftwareServerCapabilitySource(entity);
    }

    private SoftwareServerCapabilitySource buildSoftwareServerCapabilitySource(EntityDetail entity) {
        SoftwareServerCapabilitySource source = new SoftwareServerCapabilitySource();
        source.setName(repositoryHelper.getStringProperty(Constants.INFORMATION_VIEW_OMAS_NAME, Constants.NAME, entity.getProperties(),  ""));
        source.setQualifiedName(repositoryHelper.getStringProperty(Constants.INFORMATION_VIEW_OMAS_NAME, Constants.QUALIFIED_NAME, entity.getProperties(),  ""));
        source.setVersion(repositoryHelper.getStringProperty(Constants.INFORMATION_VIEW_OMAS_NAME, Constants.VERSION, entity.getProperties(),  ""));
        source.setPatchLevel(repositoryHelper.getStringProperty(Constants.INFORMATION_VIEW_OMAS_NAME, Constants.PATCH_LEVEL, entity.getProperties(),  ""));
        source.setType(repositoryHelper.getStringProperty(Constants.INFORMATION_VIEW_OMAS_NAME, Constants.TYPE, entity.getProperties(),  ""));
        source.setDescription(repositoryHelper.getStringProperty(Constants.INFORMATION_VIEW_OMAS_NAME, Constants.DESCRIPTION, entity.getProperties(),  ""));
        source.setGuid(entity.getGUID());
        return source;
    }


}
