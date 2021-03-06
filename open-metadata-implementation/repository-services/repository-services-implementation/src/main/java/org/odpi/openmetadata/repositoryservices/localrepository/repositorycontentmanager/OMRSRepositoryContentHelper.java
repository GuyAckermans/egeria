/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.repositoryservices.localrepository.repositorycontentmanager;

import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.SequencingOrder;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.utilities.OMRSRepositoryPropertiesUtilities;
import org.odpi.openmetadata.repositoryservices.ffdc.OMRSErrorCode;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.instances.*;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.properties.typedefs.*;
import org.odpi.openmetadata.repositoryservices.connectors.stores.metadatacollectionstore.repositoryconnector.OMRSRepositoryHelper;
import org.odpi.openmetadata.repositoryservices.ffdc.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;

/**
 * OMRSRepositoryContentHelper provides methods to repository connectors and repository event mappers to help
 * them build valid type definitions (TypeDefs), entities and relationships.  It is a facade to the
 * repository content manager which holds an in memory cache of all the active TypeDefs in the local server.
 * OMRSRepositoryContentHelper's purpose is to create an object that the repository connectors and event mappers can
 * create, use and discard without needing to know how to connect to the repository content manager.
 */
public class OMRSRepositoryContentHelper extends OMRSRepositoryPropertiesUtilities implements OMRSRepositoryHelper
{
    private static final Logger log = LoggerFactory.getLogger(OMRSRepositoryContentHelper.class);

    private OMRSRepositoryContentManager repositoryContentManager;


    /**
     * Creates a repository helper linked to the supplied repository content manager.
     *
     * @param repositoryContentManager object associated with the local repository.
     */
    public OMRSRepositoryContentHelper(OMRSRepositoryContentManager repositoryContentManager)
    {
        this.repositoryContentManager = repositoryContentManager;
    }


    /**
     * Return the list of typedefs active in the local repository.
     *
     * @return TypeDef gallery
     */
    public TypeDefGallery getActiveTypeDefGallery()
    {
        final String methodName = "getActiveTypeDefGallery";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.getActiveTypeDefGallery();
    }


    /**
     * Return the list of typeDefs active in the local repository.
     *
     * @return TypeDef list
     */
    public List<TypeDef>  getActiveTypeDefs()
    {
        final String methodName = "getActiveTypeDefs";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.getActiveTypeDefs();
    }


    /**
     * Return the list of typeDefs known in the cohort.
     *
     * @return TypeDef list
     */
    public List<TypeDef>  getKnownTypeDefs()
    {
        final String methodName = "getKnownTypeDefs";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.getKnownTypeDefs();
    }


    /**
     * Return the list of attributeTypeDefs active in the local repository.
     *
     * @return AttributeTypeDef list
     */
    public List<AttributeTypeDef>  getActiveAttributeTypeDefs()
    {
        final String methodName = "getActiveAttributeTypeDefs";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.getActiveAttributeTypeDefs();
    }


    /**
     * Return the list of attributeTypeDefs active in the local repository.
     *
     * @return AttributeTypeDef list
     */
    public List<AttributeTypeDef>  getKnownAttributeTypeDefs()
    {
        final String methodName = "getKnownAttributeTypeDefs";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.getKnownAttributeTypeDefs();
    }



    /**
     * Return the list of typedefs known by the local repository.
     *
     * @return TypeDef gallery
     */
    public TypeDefGallery getKnownTypeDefGallery()
    {
        final String methodName = "getKnownTypeDefGallery";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.getKnownTypeDefGallery();
    }


    /**
     * Return the TypeDef identified by the name supplied by the caller.  This is used in the connectors when
     * validating the actual types of the repository with the known open metadata types. It is looking specifically
     * for types of the same name but with different content.
     *
     * @param sourceName  source of the request (used for logging)
     * @param typeDefName unique name for the TypeDef
     * @return TypeDef object or null if TypeDef is not known.
     */
    public TypeDef getTypeDefByName(String sourceName,
                                    String typeDefName)
    {
        final String methodName = "getTypeDefByName";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.getTypeDefByName(typeDefName);
    }


    /**
     * Return the attribute name for the related entity.
     *
     * @param sourceName  source of the request (used for logging)
     * @param anchorEntityGUID unique identifier of the anchor entity
     * @param relationship relationship to another entity
     * @return proxy to the other entity.
     */
    public  String  getOtherEndName(String                 sourceName,
                                    String                 anchorEntityGUID,
                                    Relationship           relationship)
    {
        if (relationship != null)
        {
            RelationshipDef relationshipTypeDef = (RelationshipDef)this.getTypeDefByName(sourceName,
                                                                                         relationship.getType().getTypeDefName());

            String          endOneName = relationshipTypeDef.getEndDef1().getAttributeName();
            String          endTwoName = relationshipTypeDef.getEndDef2().getAttributeName();

            EntityProxy     entityProxy = relationship.getEntityOneProxy();

            if (entityProxy != null)
            {
                if (anchorEntityGUID.equals(entityProxy.getGUID()))
                {
                    return endTwoName;
                }
                else
                {
                    return endOneName;
                }
            }
        }

        return null;
    }


    /**
     * Return the AttributeTypeDef identified by the name supplied by the caller.  This is used in the connectors when
     * validating the actual types of the repository with the known open metadata types.  It is looking specifically
     * for types of the same name but with different content.
     *
     * @param sourceName           source of the request (used for logging)
     * @param attributeTypeDefName unique name for the TypeDef
     * @return AttributeTypeDef object or null if AttributeTypeDef is not known.
     */
    public AttributeTypeDef getAttributeTypeDefByName(String sourceName,
                                                      String attributeTypeDefName)
    {
        final String methodName = "getAttributeTypeDefByName";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.getAttributeTypeDefByName(attributeTypeDefName);
    }


    /**
     * Return the TypeDef identified by the guid supplied by the caller.  This call is used when
     * retrieving a type that only the guid is known.
     *
     * @param sourceName  source of the request (used for logging)
     * @param parameterName name of guid parameter
     * @param typeDefGUID unique identifier for the TypeDef
     * @param methodName calling method
     * @return TypeDef object
     * @throws TypeErrorException unknown or invalid type
     */
    public TypeDef getTypeDef(String sourceName,
                              String parameterName,
                              String typeDefGUID,
                              String methodName) throws TypeErrorException
    {
        validateRepositoryContentManager(methodName);

        return repositoryContentManager.getTypeDef(sourceName, parameterName, typeDefGUID, methodName);
    }


    /**
     * Return the AttributeTypeDef identified by the guid and name supplied by the caller.  This call is used when
     * retrieving a type that only the guid is known.
     *
     * @param sourceName           source of the request (used for logging)
     * @param attributeTypeDefGUID unique identifier for the AttributeTypeDef
     * @return TypeDef object
     * @throws TypeErrorException unknown or invalid type
     */
    public AttributeTypeDef getAttributeTypeDef(String sourceName,
                                                String attributeTypeDefGUID,
                                                String methodName) throws TypeErrorException
    {
        validateRepositoryContentManager(methodName);

        return repositoryContentManager.getAttributeTypeDef(sourceName, attributeTypeDefGUID, methodName);
    }


    /**
     * Return the TypeDef identified by the guid and name supplied by the caller.  This call is used when
     * retrieving a type that should exist.  For example, retrieving the type of a metadata instance.
     *
     * @param sourceName  source of the request (used for logging)
     * @param guidParameterName name of guid parameter
     * @param nameParameterName name of type name parameter
     * @param typeDefGUID unique identifier for the TypeDef
     * @param typeDefName unique name for the TypeDef
     * @param methodName  calling method
     * @return TypeDef object
     * @throws TypeErrorException unknown or invalid type
     */
    public TypeDef getTypeDef(String sourceName,
                              String guidParameterName,
                              String nameParameterName,
                              String typeDefGUID,
                              String typeDefName,
                              String methodName) throws TypeErrorException
    {
        validateRepositoryContentManager(methodName);

        return repositoryContentManager.getTypeDef(sourceName,
                                                   guidParameterName,
                                                   nameParameterName,
                                                   typeDefGUID,
                                                   typeDefName,
                                                   methodName);
    }


    /**
     * Return the AttributeTypeDef identified by the guid and name supplied by the caller.  This call is used when
     * retrieving a type that should exist.  For example, retrieving the type definition of a metadata instance's
     * property.
     *
     * @param sourceName           source of the request (used for logging)
     * @param attributeTypeDefGUID unique identifier for the AttributeTypeDef
     * @param attributeTypeDefName unique name for the AttributeTypeDef
     * @param methodName calling method
     * @return TypeDef object
     * @throws TypeErrorException unknown or invalid type
     */
    public AttributeTypeDef getAttributeTypeDef(String sourceName,
                                                String attributeTypeDefGUID,
                                                String attributeTypeDefName,
                                                String methodName) throws TypeErrorException
    {
        validateRepositoryContentManager(methodName);

        return repositoryContentManager.getAttributeTypeDef(sourceName,
                                                            attributeTypeDefGUID,
                                                            attributeTypeDefName,
                                                            methodName);
    }


    /**
     * Returns an updated TypeDef that has had the supplied patch applied.  It throws an exception if any part of
     * the patch is incompatible with the original TypeDef.  For example, if there is a mismatch between
     * the type or version that either represents.
     *
     * @param sourceName      source of the TypeDef (used for logging)
     * @param originalTypeDef typeDef to update
     * @param typeDefPatch    patch to apply
     * @return updated TypeDef
     * @throws InvalidParameterException the original typeDef or typeDefPatch is null
     * @throws PatchErrorException       the patch is either badly formatted, or does not apply to the supplied TypeDef
     */
    public TypeDef applyPatch(String       sourceName,
                              TypeDef      originalTypeDef,
                              TypeDefPatch typeDefPatch) throws InvalidParameterException, PatchErrorException
    {
        final String  methodName = "applyPatch";

        validateRepositoryContentManager(methodName);

        return this.applyPatch(sourceName, originalTypeDef, typeDefPatch, methodName);
    }


    /**
     * Return the list of type names for all of the subtypes of an entity type.
     *
     * @param sourceName source of the request (used for logging)
     * @param superTypeName name of the super type - this value is not included in the result.
     * @return list of type names (a null means the type is not know or it has no sub types)
     */
    public List<String>  getSubTypesOf(String sourceName,
                                       String superTypeName)
    {
        final String  methodName = "getSubTypesOf";

        validateRepositoryContentManager(methodName);

        List<String>  subTypeNames = new ArrayList<>();
        List<TypeDef> typeDefs = repositoryContentManager.getKnownTypeDefs();

        if (typeDefs != null)
        {
            for (TypeDef typeDef : typeDefs)
            {
                if (typeDef != null)
                {
                    if (! superTypeName.equals(typeDef.getName()))
                    {
                        if (repositoryContentManager.isTypeOf(sourceName,
                                                              typeDef.getName(),
                                                              superTypeName))
                        {
                            subTypeNames.add(typeDef.getName());
                        }
                    }
                }
            }
        }

        if (subTypeNames.isEmpty())
        {
            return null;
        }
        else
        {
            return subTypeNames;
        }
    }


    /**
     * Return the names of all of the properties in the supplied TypeDef and all of its super-types.
     *
     * @param sourceName name of caller.
     * @param typeDef TypeDef to query.
     * @param methodName calling method.
     * @return list of property names.
     */
    public List<TypeDefAttribute> getAllPropertiesForTypeDef(String  sourceName,
                                                             TypeDef typeDef,
                                                             String  methodName)
    {
        validateRepositoryContentManager(methodName);

        return repositoryContentManager.getAllPropertiesForTypeDef(sourceName, typeDef, methodName);
    }


    /**
     * Validate that the type of an instance is of the expected/desired type.  The actual instance may be a subtype
     * of the expected type of course.
     *
     * @param sourceName source of the request (used for logging)
     * @param actualTypeName name of the entity type
     * @param expectedTypeName name of the expected type
     * @return boolean if they match (a null in either results in false)
     */
    public boolean  isTypeOf(String   sourceName,
                             String   actualTypeName,
                             String   expectedTypeName)
    {
        final String  methodName = "isTypeOf";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.isTypeOf(sourceName, actualTypeName, expectedTypeName);
    }


    /**
     * Remember the metadata collection name for this metadata collection Id. If the metadata collection id
     * is null, it is ignored.
     *
     * @param metadataCollectionId unique identifier (guid) for the metadata collection.
     * @param metadataCollectionName display name for the metadata collection (can be null).
     */
    public void registerMetadataCollection(String    metadataCollectionId,
                                           String    metadataCollectionName)
    {
        final String methodName = "registerMetadataCollection";

        validateRepositoryContentManager(methodName);

        repositoryContentManager.registerMetadataCollection(metadataCollectionId, metadataCollectionName);
    }


    /**
     * Return the metadata collection name (or null) for a metadata collection id.
     *
     * @param metadataCollectionId unique identifier (guid) for the metadata collection.
     * @return display name
     */
    public String getMetadataCollectionName(String    metadataCollectionId)
    {
        final String methodName = "getMetadataCollectionName";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.getMetadataCollectionName(metadataCollectionId);
    }


    /**
     * Return an entity with the header and type information filled out.  The caller only needs to add properties
     * and classifications to complete the set up of the entity.
     *
     * @param sourceName           source of the request (used for logging)
     * @param metadataCollectionId unique identifier for the home metadata collection
     * @param provenanceType       origin of the entity
     * @param userName             name of the creator
     * @param typeName             name of the type
     * @return partially filled out entity needs classifications and properties
     * @throws TypeErrorException the type name is not recognized.
     */
    public EntityDetail getSkeletonEntity(String                 sourceName,
                                          String                 metadataCollectionId,
                                          InstanceProvenanceType provenanceType,
                                          String                 userName,
                                          String                 typeName) throws TypeErrorException
    {
        final String methodName = "getSkeletonEntity";

        validateRepositoryContentManager(methodName);

        EntityDetail entity = new EntityDetail();

        populateSkeletonEntity(
                entity,
                UUID.randomUUID().toString(),
                sourceName,
                metadataCollectionId,
                provenanceType,
                userName,
                typeName,
                methodName
        );

        return entity;
    }


    /**
     * Return an entity with the header and type information filled out.  The caller only needs to classifications
     * to complete the set up of the entity.
     *
     * @param sourceName            source of the request (used for logging)
     * @param metadataCollectionId  unique identifier for the home metadata collection
     * @param provenanceType        origin of the entity
     * @param userName              name of the creator
     * @param typeName              name of the type
     * @return partially filled out entity needs classifications
     * @throws TypeErrorException  the type name is not recognized.
     */
    public EntitySummary getSkeletonEntitySummary(String                 sourceName,
                                                  String                 metadataCollectionId,
                                                  InstanceProvenanceType provenanceType,
                                                  String                 userName,
                                                  String                 typeName) throws TypeErrorException
    {
        final String methodName = "getSkeletonEntitySummary";

        validateRepositoryContentManager(methodName);

        EntitySummary entity = new EntitySummary();

        populateSkeletonEntity(
                entity,
                UUID.randomUUID().toString(),
                sourceName,
                metadataCollectionId,
                provenanceType,
                userName,
                typeName,
                methodName
        );

        return entity;
    }


    /**
     * Populate the skeleton entity with vital header and type information, regardless of whether it is an EntityDetail
     * or EntitySummary.
     *
     * @param entity               the skeleton entity to populate
     * @param guid                 the GUID to give to the entity
     * @param sourceName           source of the request (used for logging)
     * @param metadataCollectionId unique identifier for the home metadata collection
     * @param provenanceType       origin of the entity
     * @param userName             name of the creator
     * @param typeName             name of the type
     * @param methodName           name of the invoking method (used for logging)
     * @throws TypeErrorException  the type name is not recognized.
     */
    private void populateSkeletonEntity(EntitySummary          entity,
                                        String                 guid,
                                        String                 sourceName,
                                        String                 metadataCollectionId,
                                        InstanceProvenanceType provenanceType,
                                        String                 userName,
                                        String                 typeName,
                                        String                 methodName) throws TypeErrorException {

        entity.setHeaderVersion(InstanceAuditHeader.CURRENT_AUDIT_HEADER_VERSION);
        entity.setInstanceProvenanceType(provenanceType);
        entity.setMetadataCollectionId(metadataCollectionId);
        entity.setCreateTime(new Date());
        entity.setGUID(guid);
        entity.setVersion(1L);

        entity.setType(repositoryContentManager.getInstanceType(sourceName, TypeDefCategory.ENTITY_DEF, typeName, methodName));
        entity.setStatus(repositoryContentManager.getInitialStatus(sourceName, typeName, methodName));
        entity.setCreatedBy(userName);
        entity.setInstanceURL(repositoryContentManager.getEntityURL(sourceName, guid));

    }


    /**
     * Return a classification with the header and type information filled out.  The caller only needs to add properties
     * and possibility origin information if it is propagated to complete the set up of the classification.
     *
     * @param sourceName             source of the request (used for logging)
     * @param userName               name of the creator
     * @param classificationTypeName name of the classification type
     * @param entityTypeName         name of the type for the entity that this classification is to be attached to.
     * @return partially filled out classification needs properties and possibly origin information
     * @throws TypeErrorException the type name is not recognized as a classification type.
     */
    public Classification getSkeletonClassification(String sourceName,
                                                    String userName,
                                                    String classificationTypeName,
                                                    String entityTypeName) throws TypeErrorException
    {
        final String methodName = "getSkeletonClassification";

        validateRepositoryContentManager(methodName);


        if (repositoryContentManager.isValidTypeCategory(sourceName,
                                                         TypeDefCategory.CLASSIFICATION_DEF,
                                                         classificationTypeName,
                                                         methodName))
        {
            if (repositoryContentManager.isValidClassificationForEntity(sourceName,
                                                                        classificationTypeName,
                                                                        entityTypeName,
                                                                        methodName))
            {
                Classification classification = new Classification();

                classification.setHeaderVersion(InstanceAuditHeader.CURRENT_AUDIT_HEADER_VERSION);
                classification.setName(classificationTypeName);
                classification.setCreateTime(new Date());
                classification.setCreatedBy(userName);
                classification.setVersion(1L);
                classification.setType(repositoryContentManager.getInstanceType(sourceName,
                                                                                TypeDefCategory.CLASSIFICATION_DEF,
                                                                                classificationTypeName,
                                                                                methodName));
                classification.setStatus(repositoryContentManager.getInitialStatus(sourceName,
                                                                                   classificationTypeName,
                                                                                   methodName));

                return classification;
            }
            else
            {
                OMRSErrorCode errorCode = OMRSErrorCode.INVALID_CLASSIFICATION_FOR_ENTITY;
                String errorMessage = errorCode.getErrorMessageId()
                        + errorCode.getFormattedErrorMessage(classificationTypeName, entityTypeName);

                throw new TypeErrorException(errorCode.getHTTPErrorCode(),
                                             this.getClass().getName(),
                                             methodName,
                                             errorMessage,
                                             errorCode.getSystemAction(),
                                             errorCode.getUserAction());
            }
        }
        else
        {
            OMRSErrorCode errorCode = OMRSErrorCode.UNKNOWN_CLASSIFICATION;
            String errorMessage = errorCode.getErrorMessageId()
                                + errorCode.getFormattedErrorMessage(classificationTypeName);

            throw new TypeErrorException(errorCode.getHTTPErrorCode(),
                                         this.getClass().getName(),
                                         methodName,
                                         errorMessage,
                                         errorCode.getSystemAction(),
                                         errorCode.getUserAction());
        }
    }


    /**
     * Return a relationship with the header and type information filled out.  The caller only needs to add properties
     * to complete the set up of the relationship.
     *
     * @param sourceName           source of the request (used for logging)
     * @param metadataCollectionId unique identifier for the home metadata collection
     * @param provenanceType       origin type of the relationship
     * @param userName             name of the creator
     * @param typeName             name of the relationship's type
     * @return partially filled out relationship needs properties
     * @throws TypeErrorException the type name is not recognized as a relationship type.
     */
    public Relationship getSkeletonRelationship(String                 sourceName,
                                                String                 metadataCollectionId,
                                                InstanceProvenanceType provenanceType,
                                                String                 userName,
                                                String                 typeName) throws TypeErrorException
    {
        final String methodName = "getSkeletonRelationship";

        validateRepositoryContentManager(methodName);

        Relationship relationship = new Relationship();
        String       guid         = UUID.randomUUID().toString();

        relationship.setHeaderVersion(InstanceAuditHeader.CURRENT_AUDIT_HEADER_VERSION);
        relationship.setInstanceProvenanceType(provenanceType);
        relationship.setMetadataCollectionId(metadataCollectionId);
        relationship.setCreateTime(new Date());
        relationship.setGUID(guid);
        relationship.setVersion(1L);

        relationship.setType(repositoryContentManager.getInstanceType(sourceName,
                                                                      TypeDefCategory.RELATIONSHIP_DEF,
                                                                      typeName,
                                                                      methodName));
        relationship.setStatus(repositoryContentManager.getInitialStatus(sourceName, typeName, methodName));
        relationship.setCreatedBy(userName);
        relationship.setInstanceURL(repositoryContentManager.getRelationshipURL(sourceName, guid));

        return relationship;
    }


    /**
     * Return a relationship with the header and type information filled out.  The caller only needs to add properties
     * to complete the set up of the relationship.
     *
     * @param sourceName     source of the request (used for logging)
     * @param typeDefSummary details of the new type
     * @return instance type
     * @throws TypeErrorException the type name is not recognized as a relationship type.
     */
    public InstanceType getNewInstanceType(String         sourceName,
                                           TypeDefSummary typeDefSummary) throws TypeErrorException
    {
        final String methodName = "getNewInstanceType";

        validateRepositoryContentManager(methodName);

        return repositoryContentManager.getInstanceType(sourceName,
                                                        typeDefSummary.getCategory(),
                                                        typeDefSummary.getName(),
                                                        methodName);
    }


    /**
     * Return a filled out entity.  It just needs to add the classifications.
     *
     * @param sourceName           source of the request (used for logging)
     * @param metadataCollectionId unique identifier for the home metadata collection
     * @param provenanceType       origin of the entity
     * @param userName             name of the creator
     * @param typeName             name of the type
     * @param properties           properties for the entity
     * @param classifications      list of classifications for the entity
     * @return an entity that is filled out
     * @throws TypeErrorException the type name is not recognized as an entity type
     */
    public EntityDetail getNewEntity(String                 sourceName,
                                     String                 metadataCollectionId,
                                     InstanceProvenanceType provenanceType,
                                     String                 userName,
                                     String                 typeName,
                                     InstanceProperties     properties,
                                     List<Classification>   classifications) throws TypeErrorException
    {
        EntityDetail entity = this.getSkeletonEntity(sourceName,
                                                     metadataCollectionId,
                                                     provenanceType,
                                                     userName,
                                                     typeName);

        entity.setProperties(properties);
        entity.setClassifications(classifications);

        return entity;
    }


    /**
     * Return a filled out relationship, caller just needs the entity proxies added.
     *
     * @param sourceName           source of the request (used for logging)
     * @param metadataCollectionId unique identifier for the home metadata collection
     * @param provenanceType       origin of the relationship
     * @param userName             name of the creator
     * @param typeName             name of the type
     * @param properties           properties for the relationship
     * @return a relationship that is filled out
     * @throws TypeErrorException the type name is not recognized as a relationship type
     */
    public Relationship getNewRelationship(String                 sourceName,
                                           String                 metadataCollectionId,
                                           InstanceProvenanceType provenanceType,
                                           String                 userName,
                                           String                 typeName,
                                           InstanceProperties     properties) throws TypeErrorException
    {
        Relationship relationship = this.getSkeletonRelationship(sourceName,
                                                                 metadataCollectionId,
                                                                 provenanceType,
                                                                 userName,
                                                                 typeName);

        relationship.setProperties(properties);

        return relationship;
    }


    /**
     * Return a classification with the header and type information filled out.  The caller only needs to add properties
     * to complete the set up of the classification.
     *
     * @param sourceName     source of the request (used for logging)
     * @param userName       name of the creator
     * @param typeName       name of the type
     * @param entityTypeName name of the type for the entity that this classification is to be attached to.
     * @param properties     properties for the classification
     * @return partially filled out classification needs properties and possibly origin information
     * @throws TypeErrorException the type name is not recognized as a classification type.
     */
    public Classification getNewClassification(String               sourceName,
                                               String               userName,
                                               String               typeName,
                                               String               entityTypeName,
                                               ClassificationOrigin classificationOrigin,
                                               String               classificationOriginGUID,
                                               InstanceProperties   properties) throws TypeErrorException
    {
        Classification classification = this.getSkeletonClassification(sourceName,
                                                                       userName,
                                                                       typeName,
                                                                       entityTypeName);

        classification.setClassificationOrigin(classificationOrigin);
        classification.setClassificationOriginGUID(classificationOriginGUID);
        classification.setProperties(properties);

        return classification;
    }


    /**
     * Add a classification to an existing entity.
     *
     * @param sourceName        source of the request (used for logging)
     * @param entity            entity to update
     * @param newClassification classification to update
     * @param methodName        calling method
     * @return updated entity
     */
    public EntityDetail addClassificationToEntity(String         sourceName,
                                                  EntityDetail   entity,
                                                  Classification newClassification,
                                                  String         methodName)
    {
        EntityDetail updatedEntity = new EntityDetail(entity);

        if (newClassification != null)
        {
            /*
             * Duplicate classifications are not allowed so a hash map is used to remove duplicates.
             */
            Map<String, Classification> entityClassificationsMap = new HashMap<>();
            List<Classification>        entityClassifications    = updatedEntity.getClassifications();

            if (entityClassifications != null)
            {
                for (Classification existingClassification : entityClassifications)
                {
                    if (existingClassification != null)
                    {
                        entityClassificationsMap.put(existingClassification.getName(), existingClassification);
                    }
                }
            }

            entityClassificationsMap.put(newClassification.getName(), newClassification);

            if (entityClassificationsMap.isEmpty())
            {
                updatedEntity.setClassifications(null);
            }
            else
            {
                entityClassifications = new ArrayList<>(entityClassificationsMap.values());

                updatedEntity.setClassifications(entityClassifications);
            }

            return updatedEntity;
        }
        else
        {
            final String thisMethodName = "addClassificationToEntity";

            OMRSErrorCode errorCode = OMRSErrorCode.NULL_CLASSIFICATION_CREATED;
            String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(sourceName,
                                                                                                     thisMethodName,
                                                                                                     methodName);

            throw new OMRSLogicErrorException(errorCode.getHTTPErrorCode(),
                                              this.getClass().getName(),
                                              methodName,
                                              errorMessage,
                                              errorCode.getSystemAction(),
                                              errorCode.getUserAction());
        }
    }


    /**
     * Return the names classification from an existing entity.
     *
     * @param sourceName         source of the request (used for logging)
     * @param entity             entity to update
     * @param classificationName classification to retrieve
     * @param methodName         calling method
     * @return located classification
     * @throws ClassificationErrorException the classification is not attached to the entity
     */
    public Classification getClassificationFromEntity(String       sourceName,
                                                      EntityDetail entity,
                                                      String       classificationName,
                                                      String       methodName) throws ClassificationErrorException
    {
        final String thisMethodName = "getClassificationFromEntity";

        if ((entity == null) || (classificationName == null))
        {
            OMRSErrorCode errorCode = OMRSErrorCode.VALIDATION_LOGIC_ERROR;
            String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(sourceName,
                                                                                                     thisMethodName,
                                                                                                     methodName);

            throw new OMRSLogicErrorException(errorCode.getHTTPErrorCode(),
                                              this.getClass().getName(),
                                              methodName,
                                              errorMessage,
                                              errorCode.getSystemAction(),
                                              errorCode.getUserAction());
        }

        List<Classification> entityClassifications = entity.getClassifications();

        if (entityClassifications != null)
        {
            for (Classification entityClassification : entityClassifications)
            {
                if (classificationName.equals(entityClassification.getName()))
                {
                    return entityClassification;
                }
            }
        }

        OMRSErrorCode errorCode = OMRSErrorCode.ENTITY_NOT_CLASSIFIED;
        String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(methodName,
                                                                                                 sourceName,
                                                                                                 classificationName,
                                                                                                 entity.getGUID());
        throw new ClassificationErrorException(errorCode.getHTTPErrorCode(),
                                               this.getClass().getName(),
                                               methodName,
                                               errorMessage,
                                               errorCode.getSystemAction(),
                                               errorCode.getUserAction());
    }


    /**
     * Replace an existing classification with a new one
     *
     * @param sourceName        source of the request (used for logging)
     * @param userName          name of the editor
     * @param entity            entity to update
     * @param newClassification classification to update
     * @param methodName        calling method
     * @return updated entity
     */
    public EntityDetail updateClassificationInEntity(String         sourceName,
                                                     String         userName,
                                                     EntityDetail   entity,
                                                     Classification newClassification,
                                                     String         methodName)
    {
        if (newClassification != null)
        {
            Classification updatedClassification = new Classification(newClassification);

            updatedClassification = incrementVersion(userName, newClassification, updatedClassification);

            return this.addClassificationToEntity(sourceName, entity, updatedClassification, methodName);
        }
        else
        {
            final String thisMethodName = "updateClassificationInEntity";

            OMRSErrorCode errorCode = OMRSErrorCode.NULL_CLASSIFICATION_CREATED;
            String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(sourceName,
                                                                                                     thisMethodName,
                                                                                                     methodName);

            throw new OMRSLogicErrorException(errorCode.getHTTPErrorCode(),
                                              this.getClass().getName(),
                                              methodName,
                                              errorMessage,
                                              errorCode.getSystemAction(),
                                              errorCode.getUserAction());
        }
    }


    /**
     * Return a oldClassification with the header and type information filled out.  The caller only needs to add properties
     * to complete the set up of the oldClassification.
     *
     * @param sourceName            source of the request (used for logging)
     * @param entity                entity to update
     * @param oldClassificationName classification to remove
     * @param methodName            calling method
     * @return updated entity
     * @throws ClassificationErrorException the entity was not classified with this classification
     */
    public EntityDetail deleteClassificationFromEntity(String       sourceName,
                                                       EntityDetail entity,
                                                       String       oldClassificationName,
                                                       String       methodName) throws ClassificationErrorException
    {
        EntityDetail updatedEntity = new EntityDetail(entity);

        if (oldClassificationName != null)
        {
            /*
             * Duplicate classifications are not allowed so a hash map is used to remove duplicates.
             */
            Map<String, Classification> entityClassificationsMap = new HashMap<>();
            List<Classification>        entityClassifications    = updatedEntity.getClassifications();

            if (entityClassifications != null)
            {
                for (Classification existingClassification : entityClassifications)
                {
                    if (existingClassification != null)
                    {
                        entityClassificationsMap.put(existingClassification.getName(), existingClassification);
                    }
                }
            }

            Classification oldClassification = entityClassificationsMap.remove(oldClassificationName);

            if (oldClassification == null)
            {
                OMRSErrorCode errorCode = OMRSErrorCode.ENTITY_NOT_CLASSIFIED;
                String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(methodName,
                                                                                                         sourceName,
                                                                                                         oldClassificationName,
                                                                                                         entity.getGUID());
                throw new ClassificationErrorException(errorCode.getHTTPErrorCode(),
                                                       this.getClass().getName(),
                                                       methodName,
                                                       errorMessage,
                                                       errorCode.getSystemAction(),
                                                       errorCode.getUserAction());
            }

            if (entityClassificationsMap.isEmpty())
            {
                updatedEntity.setClassifications(null);
            }
            else
            {
                entityClassifications = new ArrayList<>(entityClassificationsMap.values());

                updatedEntity.setClassifications(entityClassifications);
            }

            return updatedEntity;
        }
        else
        {
            final String thisMethodName = "deleteClassificationFromEntity";

            OMRSErrorCode errorCode = OMRSErrorCode.NULL_CLASSIFICATION_NAME;
            String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(sourceName,
                                                                                                     thisMethodName,
                                                                                                     methodName);

            throw new OMRSLogicErrorException(errorCode.getHTTPErrorCode(),
                                              this.getClass().getName(),
                                              methodName,
                                              errorMessage,
                                              errorCode.getSystemAction(),
                                              errorCode.getUserAction());
        }
    }


    /**
     * Merge two sets of instance properties.
     *
     * @param sourceName         source of the request (used for logging)
     * @param existingProperties current set of properties
     * @param newProperties      properties to add/update
     * @return merged properties
     */
    public InstanceProperties mergeInstanceProperties(String             sourceName,
                                                      InstanceProperties existingProperties,
                                                      InstanceProperties newProperties)
    {
        InstanceProperties mergedProperties;

        if (existingProperties == null)
        {
            mergedProperties = newProperties;
        }
        else
        {
            mergedProperties = existingProperties;

            if (newProperties != null)
            {
                Iterator<String> newPropertyNames = newProperties.getPropertyNames();

                while (newPropertyNames.hasNext())
                {
                    String newPropertyName = newPropertyNames.next();

                    mergedProperties.setProperty(newPropertyName, newProperties.getPropertyValue(newPropertyName));
                }
            }
        }

        return mergedProperties;
    }


    /**
     * Changes the control information to reflect an update in an instance.
     *
     * @param userId           user making the change.
     * @param originalInstance original instance before the change
     * @param updatedInstance  new version of the instance that needs updating
     * @return updated instance
     */
    public Relationship incrementVersion(String              userId,
                                         InstanceAuditHeader originalInstance,
                                         Relationship        updatedInstance)
    {
        updatedInstance.setUpdatedBy(userId);
        updatedInstance.setUpdateTime(new Date());

        long currentVersion = originalInstance.getVersion();
        updatedInstance.setVersion(currentVersion+1);

        return updatedInstance;
    }


    /**
     * Changes the control information to reflect an update in an instance.
     *
     * @param userId           user making the change.
     * @param originalInstance original instance before the change
     * @param updatedInstance  new version of the instance that needs updating
     * @return updated instance
     */
    public Classification incrementVersion(String              userId,
                                           InstanceAuditHeader originalInstance,
                                           Classification      updatedInstance)
    {
        updatedInstance.setUpdatedBy(userId);
        updatedInstance.setUpdateTime(new Date());

        long currentVersion = originalInstance.getVersion();
        updatedInstance.setVersion(currentVersion+1);

        return updatedInstance;
    }


    /**
     * Changes the control information to reflect an update in an instance.
     *
     * @param userId           user making the change.
     * @param originalInstance original instance before the change
     * @param updatedInstance  new version of the instance that needs updating
     * @return updated instance
     */
    public EntityDetail incrementVersion(String              userId,
                                         InstanceAuditHeader originalInstance,
                                         EntityDetail        updatedInstance)
    {
        updatedInstance.setUpdatedBy(userId);
        updatedInstance.setUpdateTime(new Date());

        long currentVersion = originalInstance.getVersion();
        updatedInstance.setVersion(currentVersion+1);

        return updatedInstance;
    }




    /**
     * Generate an entity proxy from an entity and its TypeDef.
     *
     * @param sourceName source of the request (used for logging)
     * @param entity     entity instance
     * @return new entity proxy
     * @throws RepositoryErrorException logic error in the repository corrupted entity
     */
    public EntityProxy getNewEntityProxy(String       sourceName,
                                         EntityDetail entity) throws RepositoryErrorException
    {
        final String  methodName = "getNewEntityProxy";
        final String  parameterName = "entity";

        validateRepositoryContentManager(methodName);

        if (entity != null)
        {
            InstanceType type = entity.getType();

            if (type != null)
            {
                try
                {
                    TypeDef typeDef = repositoryContentManager.getTypeDef(sourceName,
                                                                          parameterName,
                                                                          parameterName,
                                                                          type.getTypeDefGUID(),
                                                                          type.getTypeDefName(),
                                                                          methodName);

                    EntityProxy            entityProxy          = new EntityProxy(entity);
                    InstanceProperties     entityProperties     = entity.getProperties();

                    if (entityProperties != null)
                    {
                        List<TypeDefAttribute> propertiesDefinition = repositoryContentManager.getAllPropertiesForTypeDef(sourceName,
                                                                                                                          typeDef,
                                                                                                                          methodName);
                        InstanceProperties uniqueAttributes = new InstanceProperties();

                        if (propertiesDefinition != null)
                        {
                            for (TypeDefAttribute typeDefAttribute : propertiesDefinition)
                            {
                                if (typeDefAttribute != null)
                                {
                                    String propertyName = typeDefAttribute.getAttributeName();

                                    if ((typeDefAttribute.isUnique()) && (propertyName != null))
                                    {
                                        InstancePropertyValue propertyValue = entityProperties.getPropertyValue(
                                                propertyName);

                                        if (propertyValue != null)
                                        {
                                            uniqueAttributes.setProperty(propertyName, propertyValue);
                                        }
                                    }
                                }
                            }
                        }

                        if (uniqueAttributes.getPropertyCount() > 0)
                        {
                            entityProxy.setUniqueProperties(uniqueAttributes);
                        }
                    }

                    return entityProxy;
                }
                catch (TypeErrorException error)
                {
                    OMRSErrorCode errorCode = OMRSErrorCode.REPOSITORY_LOGIC_ERROR;
                    String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(sourceName,
                                                                                                             methodName,
                                                                                                             error.getErrorMessage());

                    throw new RepositoryErrorException(errorCode.getHTTPErrorCode(),
                                                       this.getClass().getName(),
                                                       methodName,
                                                       errorMessage,
                                                       errorCode.getSystemAction(),
                                                       errorCode.getUserAction());
                }
            }
        }

        return null;
    }


    /**
     * Return a filled out entity.
     *
     * @param sourceName            source of the request (used for logging)
     * @param metadataCollectionId  unique identifier for the home metadata collection
     * @param provenanceType        origin of the entity
     * @param userName              name of the creator
     * @param typeName              name of the type
     * @param properties            properties for the entity
     * @param classifications       list of classifications for the entity
     * @return                      an entity that is filled out
     * @throws TypeErrorException   the type name is not recognized as an entity type
     */
    public EntityProxy getNewEntityProxy(String                    sourceName,
                                         String                    metadataCollectionId,
                                         InstanceProvenanceType    provenanceType,
                                         String                    userName,
                                         String                    typeName,
                                         InstanceProperties        properties,
                                         List<Classification>      classifications) throws TypeErrorException
    {
        EntityProxy entity = this.getSkeletonEntityProxy(sourceName,
                                                         metadataCollectionId,
                                                         provenanceType,
                                                         userName,
                                                         typeName);

        entity.setUniqueProperties(properties);
        entity.setClassifications(classifications);

        return entity;
    }

    /**
     * Return an entity with the header and type information filled out.  The caller only needs to add properties
     * and classifications to complete the set up of the entity.
     *
     * @param sourceName                source of the request (used for logging)
     * @param metadataCollectionId      unique identifier for the home metadata collection
     * @param provenanceType            origin of the entity
     * @param userName                  name of the creator
     * @param typeName                  name of the type
     * @return                          partially filled out entity  needs classifications and properties
     * @throws TypeErrorException       the type name is not recognized.
     */
    public EntityProxy getSkeletonEntityProxy(String                  sourceName,
                                              String                  metadataCollectionId,
                                              InstanceProvenanceType  provenanceType,
                                              String                  userName,
                                              String                  typeName) throws TypeErrorException
    {
        final String methodName = "getSkeletonEntityProxy";

        validateRepositoryContentManager(methodName);

        EntityProxy entity = new EntityProxy();
        String       guid   = UUID.randomUUID().toString();

        entity.setInstanceProvenanceType(provenanceType);
        entity.setMetadataCollectionId(metadataCollectionId);
        entity.setCreateTime(new Date());
        entity.setGUID(guid);
        entity.setVersion(1L);

        entity.setType(repositoryContentManager.getInstanceType(sourceName, TypeDefCategory.ENTITY_DEF, typeName, methodName));
        entity.setStatus(repositoryContentManager.getInitialStatus(sourceName, typeName, methodName));
        entity.setCreatedBy(userName);
        entity.setInstanceURL(repositoryContentManager.getEntityURL(sourceName, guid));

        return entity;
    }


    /**
     * Return boolean true if entity is linked by this relationship.
     *
     * @param sourceName   name of source requesting help
     * @param entityGUID   unique identifier of entity
     * @param relationship relationship to test
     * @return boolean indicating whether the entity is mentioned in the relationship
     */
    public boolean relatedEntity(String       sourceName,
                                 String       entityGUID,
                                 Relationship relationship)
    {
        if (relationship != null)
        {
            EntityProxy entityOneProxy = relationship.getEntityOneProxy();
            EntityProxy entityTwoProxy = relationship.getEntityTwoProxy();

            if (entityOneProxy != null)
            {
                if (entityGUID.equals(entityOneProxy.getGUID()))
                {
                    return true;
                }
            }

            if (entityTwoProxy != null)
            {
                return entityGUID.equals(entityTwoProxy.getGUID());
            }
        }

        return false;
    }




    /**
     * Returns the type name from an instance (entity, relationship or classification).
     *
     * @param instance instance to read
     * @return String type name
     * @throws InvalidParameterException if the parameters are null or invalid
     * @throws RepositoryErrorException if the instance does not have a type name
     */
    public String   getTypeName(InstanceAuditHeader      instance) throws RepositoryErrorException,
                                                                          InvalidParameterException
    {
        final String methodName = "getTypeName";

        if (instance != null)
        {
            InstanceType type = instance.getType();

            if (type != null)
            {
                if (type.getTypeDefName() != null)
                {
                    return type.getTypeDefName();
                }
            }

            throwRepositoryContentError(methodName, instance);
        }

        throwParameterError(methodName);
        return null;
    }


    /**
     * Return the guid of an entity linked to end 1 of the relationship.
     *
     * @param relationship relationship to parse
     * @return String unique identifier
     */
    public String  getEnd1EntityGUID(Relationship   relationship)
    {
        if (relationship != null)
        {
            EntityProxy entityProxy = relationship.getEntityOneProxy();

            if (entityProxy != null)
            {
                if (entityProxy.getGUID() != null)
                {
                    return entityProxy.getGUID();
                }
            }
        }

        return null;
    }


    /**
     * Return the guid of an entity linked to end 2 of the relationship.
     *
     * @param relationship relationship to parse
     * @return String unique identifier
     */
    public String  getEnd2EntityGUID(Relationship   relationship)
    {
        if (relationship != null)
        {
            EntityProxy entityProxy = relationship.getEntityTwoProxy();

            if (entityProxy != null)
            {
                if (entityProxy.getGUID() != null)
                {
                    return entityProxy.getGUID();
                }
            }
        }

        return null;
    }


    /**
     * Use the paging and sequencing parameters to format the results for a repository call that returns a list of
     * entity instances.
     *
     * @param fullResults - the full list of results in an arbitrary order
     * @param fromElement - the starting element number of the instances to return. This is used when retrieving elements
     *                    beyond the first page of results. Zero means start from the first element.
     * @param sequencingProperty - String name of the property that is to be used to sequence the results.
     *                           Null means do not sequence on a property name (see SequencingOrder).
     * @param sequencingOrder - Enum defining how the results should be ordered.
     * @param pageSize - the maximum number of result entities that can be returned on this request.  Zero means
     *                 unrestricted return results size.
     * @return results array as requested
     * @throws PropertyErrorException the sequencing property specified is not valid for any of the requested types of
     *                                  entity.
     * @throws PagingErrorException the paging/sequencing parameters are set up incorrectly.
     */
    public List<EntityDetail>  formatEntityResults(List<EntityDetail>   fullResults,
                                                   int                  fromElement,
                                                   String               sequencingProperty,
                                                   SequencingOrder      sequencingOrder,
                                                   int                  pageSize) throws PagingErrorException,
                                                                                         PropertyErrorException
    {
        if (fullResults == null)
        {
            return null;
        }

        if (fullResults.isEmpty())
        {
            return null;
        }

        if (pageSize == 0)
        {
            return fullResults;
        }

        int fullResultsSize = fullResults.size();

        Collections.sort(fullResults,
                         new java.util.Comparator<EntityDetail>()
                         {
                             @Override
                             public int compare(final EntityDetail object1, final EntityDetail object2)
                             {
                                 return OMRSRepositoryContentHelper.compareProperties(object1.getProperties(),
                                                                                      object2.getProperties(),
                                                                                      sequencingProperty,
                                                                                      sequencingOrder);
                             }
                         });

        if ((fromElement == 0) && (pageSize > fullResults.size()))
        {
            return fullResults;
        }

        int toIndex = getToIndex(fromElement, pageSize, fullResultsSize);
        return new ArrayList<>(fullResults.subList(fromElement, toIndex));
    }


    /**
     * Use the paging and sequencing parameters to format the results for a repository call that returns a list of
     * relationship instances.
     *
     * @param fullResults - the full list of results in an arbitrary order. This is supplied not empty.
     * @param fromElement - the starting element number of the instances to return. This is used when retrieving elements
     *                    beyond the first page of results. Zero means start from the first element.
     * @param sequencingProperty - String name of the property that is to be used to sequence the results.
     *                           Null means do not sequence on a property name (see SequencingOrder).
     * @param sequencingOrder - Enum defining how the results should be ordered.
     * @param pageSize - the maximum number of result entities that can be returned on this request.  Zero means
     *                 unrestricted return results size.
     * @return results array as requested
     * @throws PropertyErrorException the sequencing property specified is not valid for any of the requested types of
     *                                  relationship.
     * @throws PagingErrorException the paging/sequencing parameters are set up incorrectly.
     */
    public List<Relationship>  formatRelationshipResults(List<Relationship>   fullResults,
                                                         int                  fromElement,
                                                         String               sequencingProperty,
                                                         SequencingOrder      sequencingOrder,
                                                         int                  pageSize) throws PagingErrorException,
                                                                                               PropertyErrorException
    {
        if (fullResults == null)
        {
            return null;
        }

        if (fullResults.isEmpty())
        {
            return null;
        }

        if (pageSize == 0)
        {
            return fullResults;
        }

        int fullResultsSize = fullResults.size();

        if (fromElement > fullResultsSize)
        {
            return null;
        }

        Collections.sort(fullResults,
                         new java.util.Comparator<Relationship>()
                        {
                            @Override
                            public int compare(final Relationship object1, final Relationship object2)
                            {
                                return OMRSRepositoryContentHelper.compareProperties(object1.getProperties(),
                                                                                     object2.getProperties(),
                                                                                     sequencingProperty,
                                                                                     sequencingOrder);
                            }
                        });

        if ((fromElement == 0) && (pageSize > fullResults.size()))
        {
            return fullResults;
        }

        int toIndex = getToIndex(fromElement, pageSize, fullResultsSize);

        return new ArrayList<>(fullResults.subList(fromElement, toIndex));
    }


    /**
     * Compare the properties of two instances and determine the sort order based on the nominated property value and
     * sort order.
     *
     * @param instance1Properties properties from first instance
     * @param instance2Properties properties from second instance
     * @param propertyName name of property to compare
     * @param sequencingOrder ascending or descending order
     * @return sort result
     */
    private static int  compareProperties(InstanceProperties     instance1Properties,
                                          InstanceProperties     instance2Properties,
                                          String                 propertyName,
                                          SequencingOrder        sequencingOrder)
    {

        // todo need to add support for properties in the instance header eg createdBy
         /*
          * Ideally we would not include all this type inspection in the comparison
          * function - but we do not know the types until we are comparing the
          * pair of instances. There is no guarantee the list is homogeneous or that
          * the objects to be compared are of the same type.
          */

         int    sortResult;
         String o1PropertyTypeName = null;
         String o2PropertyTypeName = null;
         Object o1PropertyValue    = null;
         Object o2PropertyValue    = null;

         /*
          * If instance1 has the named property, retrieve its value. Same for instance2.
          * If neither object has the property return 0
          * If one object has the property sort that higher: +1 if instance1, -1 if instance2
          * If both have a value for the property, of different types, return 0.
          * If both have a value for the property, of the same type, compare them...
          * This is only performed for primitives, anything else is treated as ignored
          */
         if (instance1Properties != null)
         {
             InstancePropertyValue o1PropValue = instance1Properties.getPropertyValue(propertyName);
             if (o1PropValue != null)
             {
                 InstancePropertyCategory o1PropCat = o1PropValue.getInstancePropertyCategory();
                 if (o1PropCat == InstancePropertyCategory.PRIMITIVE)
                 {
                     o1PropertyTypeName = o1PropValue.getTypeName();
                     o1PropertyValue = ((PrimitivePropertyValue) o1PropValue).getPrimitiveValue();
                 }
             }
         }

         if (instance2Properties != null)
         {
             InstancePropertyValue o2PropValue = instance2Properties.getPropertyValue(propertyName);
             if (o2PropValue != null)
             {
                 InstancePropertyCategory o2PropCat = o2PropValue.getInstancePropertyCategory();
                 if (o2PropCat == InstancePropertyCategory.PRIMITIVE)
                 {
                     o2PropertyTypeName = o2PropValue.getTypeName();
                     o2PropertyValue = ((PrimitivePropertyValue) o2PropValue).getPrimitiveValue();
                 }
             }
         }

         if (o1PropertyTypeName == null && o2PropertyTypeName == null)
         {
             sortResult = 0;
         }
         else if (o1PropertyTypeName != null && o2PropertyTypeName == null)
         {
             sortResult = 1;
         }
         else if (o1PropertyTypeName == null) // implicit: o2PropertyTypeName != null
         {
             sortResult = -1;
         }
         else if (!o1PropertyTypeName.equals(o2PropertyTypeName))
         {
             sortResult = 0;
         }
         else
         {
             // Both objects have values, of the same type for the named property - compare...
             sortResult = typeSpecificCompare(o1PropertyTypeName, o1PropertyValue, o2PropertyValue);

         }
         if (sequencingOrder == SequencingOrder.PROPERTY_DESCENDING)
         {
             sortResult = sortResult * (-1);
         }

         return sortResult;

    }


    /**
     * Compare two objects based on their type.
     * It must have been previously established that both objects are of the type
     * indicated by the supplied typeName
     *
     * @param typeName name of type
     * @param v1 value from instance 1
     * @param v2 value from instance 2
     * @return sort order
     */
    private static int typeSpecificCompare(String typeName, Object v1, Object v2)
    {
        int sortOrder;
        switch (typeName)
        {
            case "boolean":
                sortOrder = ((Boolean) v1).compareTo((Boolean) v2);
                break;
            case "byte":
                sortOrder = ((Byte) v1).compareTo((Byte) v2);
                break;
            case "char":
                sortOrder = ((Character) v1).compareTo((Character) v2);
                break;
            case "short":
                sortOrder = ((Short) v1).compareTo((Short) v2);
                break;
            case "integer":
                sortOrder = ((Integer) v1).compareTo((Integer) v2);
                break;
            case "long":
                sortOrder = ((Long) v1).compareTo((Long) v2);
                break;
            case "float":
                sortOrder = ((Float) v1).compareTo((Float) v2);
                break;
            case "double":
                sortOrder = ((Double) v1).compareTo((Double) v2);
                break;
            case "biginteger":
                sortOrder = ((BigInteger) v1).compareTo((BigInteger) v2);
                break;
            case "bigdecimal":
                sortOrder = ((BigDecimal) v1).compareTo((BigDecimal) v2);
                break;
            case "string":
                sortOrder = ((String) v1).compareTo((String) v2);
                break;
            case "date":
                sortOrder = ((Date) v1).compareTo((Date) v2);
                break;
            default:
                log.debug("Property type not catered for in compare function");
                sortOrder = 0;
        }

        return sortOrder;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getExactMatchRegex(String searchString)
    {
        return searchString == null ? null : Pattern.quote(searchString);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExactMatchRegex(String searchString)
    {
        return searchString == null
                || (searchString.startsWith("\\Q")
                    && searchString.endsWith("\\E")
                    && searchString.indexOf("\\E") == searchString.length() - 2);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getContainsRegex(String searchString)
    {
        return searchString == null ? null : ".*" + getExactMatchRegex(searchString) + ".*";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isContainsRegex(String searchString)
    {
        return searchString != null
                && searchString.startsWith(".*")
                && searchString.endsWith(".*")
                && isExactMatchRegex(searchString.substring(2, searchString.length() - 2));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getStartsWithRegex(String searchString)
    {
        return searchString == null ? null : getExactMatchRegex(searchString) + ".*";
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStartsWithRegex(String searchString)
    {
        return searchString != null
                && searchString.endsWith(".*")
                && isExactMatchRegex(searchString.substring(0, searchString.length() - 2));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getEndsWithRegex(String searchString)
    {
        return searchString == null ? null : ".*" + getExactMatchRegex(searchString);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEndsWithRegex(String searchString)
    {
        return searchString != null
                && searchString.startsWith(".*")
                && isExactMatchRegex(searchString.substring(2));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getUnqualifiedLiteralString(String searchString)
    {
        if (searchString == null)
        {
            return null;
        }
        if (isExactMatchRegex(searchString))
        {
            return searchString.substring(2, searchString.length() - 2);
        }
        if (isStartsWithRegex(searchString))
        {
            return searchString.substring(2, searchString.length() - 4);
        }
        if (isEndsWithRegex(searchString))
        {
            return searchString.substring(4, searchString.length() - 2);
        }
        if (isContainsRegex(searchString)) {
            return searchString.substring(4, searchString.length() - 4);
        }
        return searchString;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public RelationshipDifferences getRelationshipDifferences(Relationship left, Relationship right, boolean ignoreModificationStamps)
    {

        RelationshipDifferences differences = new RelationshipDifferences();
        getInstanceHeaderDifferences(differences, left, right, ignoreModificationStamps);
        Differences.SidePresent present = checkDifferenceNulls(left, right);

        EntityProxyDifferences one = null;
        EntityProxyDifferences two = null;
        if (present.equals(Differences.SidePresent.BOTH) && !left.equals(right)) {
            differences.checkInstanceProperties(left.getProperties(), right.getProperties());
            one = getEntityProxyDifferences(left.getEntityOneProxy(), right.getEntityOneProxy(), ignoreModificationStamps);
            two = getEntityProxyDifferences(left.getEntityTwoProxy(), right.getEntityTwoProxy(), ignoreModificationStamps);
        } else if (present.equals(Differences.SidePresent.LEFT_ONLY)) {
            differences.checkInstanceProperties(left.getProperties(), null);
            one = getEntityProxyDifferences(left.getEntityOneProxy(), null, ignoreModificationStamps);
            two = getEntityProxyDifferences(left.getEntityTwoProxy(), null, ignoreModificationStamps);
        } else if (present.equals(Differences.SidePresent.RIGHT_ONLY)) {
            differences.checkInstanceProperties(null, right.getProperties());
            one = getEntityProxyDifferences(null, right.getEntityOneProxy(), ignoreModificationStamps);
            two = getEntityProxyDifferences(null, right.getEntityTwoProxy(), ignoreModificationStamps);
        }
        differences.setEntityProxyOneDifferences(one);
        differences.setEntityProxyTwoDifferences(two);

        return differences;

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public EntityDetailDifferences getEntityDetailDifferences(EntityDetail left, EntityDetail right, boolean ignoreModificationStamps)
    {

        EntityDetailDifferences differences = new EntityDetailDifferences();
        getEntitySummaryDifferences(differences, left, right, ignoreModificationStamps);
        Differences.SidePresent present = checkDifferenceNulls(left, right);

        if (present.equals(Differences.SidePresent.BOTH) && !left.equals(right)) {
            differences.checkInstanceProperties(left.getProperties(), right.getProperties());
        } else if (present.equals(Differences.SidePresent.LEFT_ONLY)) {
            differences.checkInstanceProperties(left.getProperties(), null);
        } else if (present.equals(Differences.SidePresent.RIGHT_ONLY)){
            differences.checkInstanceProperties(null, right.getProperties());
        }

        return differences;

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public EntityProxyDifferences getEntityProxyDifferences(EntityProxy left, EntityProxy right, boolean ignoreModificationStamps)
    {

        EntityProxyDifferences differences = new EntityProxyDifferences();
        getEntitySummaryDifferences(differences, left, right, ignoreModificationStamps);
        Differences.SidePresent present = checkDifferenceNulls(left, right);

        if (present.equals(Differences.SidePresent.BOTH) && !left.equals(right)) {
            differences.checkUniqueProperties(left.getUniqueProperties(), right.getUniqueProperties());
        } else if (present.equals(Differences.SidePresent.LEFT_ONLY)) {
            differences.checkUniqueProperties(left.getUniqueProperties(), null);
        } else if (present.equals(Differences.SidePresent.RIGHT_ONLY)) {
            differences.checkUniqueProperties(null, right.getUniqueProperties());
        }

        return differences;

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public EntitySummaryDifferences getEntitySummaryDifferences(EntitySummary left, EntitySummary right, boolean ignoreModificationStamps)
    {
        EntitySummaryDifferences differences = new EntitySummaryDifferences();
        getEntitySummaryDifferences(differences, left, right, ignoreModificationStamps);
        return differences;
    }


    /**
     * Calculate the differences between the two provided EntitySummary objects.
     *
     * @param differences the EntitySummaryDifferences object through which to track differences
     * @param left one of the EntitySummary objects to compare
     * @param right the other EntitySummary object to compare
     * @param ignoreModificationStamps true if we should ignore modification differences (Version, UpdateTime, UpdatedBy)
     *                                 or false if we should include these
     */
    private void getEntitySummaryDifferences(EntitySummaryDifferences differences,
                                             EntitySummary left,
                                             EntitySummary right,
                                             boolean ignoreModificationStamps)
    {
        getInstanceHeaderDifferences(differences, left, right, ignoreModificationStamps);
        differences.checkClassifications(left, right);
    }


    /**
     * Calculate the differences between the two provided InstanceHeader objects.
     *
     * @param differences the InstanceDifferences object through which to track differences
     * @param left one of the InstanceHeaders to compare
     * @param right the other InstanceHeader to compare
     * @param ignoreModificationStamps true if we should ignore modification differences (Version, UpdateTime, UpdatedBy)
     *                                 or false if we should include these
     */
    private void getInstanceHeaderDifferences(Differences differences,
                                              InstanceHeader left,
                                              InstanceHeader right,
                                              boolean ignoreModificationStamps)
    {
        getInstanceAuditHeaderDifferences(differences, left, right, ignoreModificationStamps);
        Differences.SidePresent present = checkDifferenceNulls(left, right);
        if (present.equals(Differences.SidePresent.BOTH) && !left.equals(right)) {
            differences.check("GUID", left.getGUID(), right.getGUID());
            differences.check("InstanceURL", left.getInstanceURL(), right.getInstanceURL());
        } else if (!present.equals(Differences.SidePresent.NEITHER) && !present.equals(Differences.SidePresent.BOTH)) {
            InstanceHeader sideWithValues;
            if (present.equals(Differences.SidePresent.LEFT_ONLY)) {
                sideWithValues = left;
            } else {
                sideWithValues = right;
            }
            differences.addOnlyOnOneSide(present, "GUID", sideWithValues.getGUID());
            differences.addOnlyOnOneSide(present, "Type", sideWithValues.getInstanceURL());
        }
    }


    /**
     * Calculate the differences between the two provided InstanceAuditHeader objects.
     *
     * @param differences the InstanceDifferences object through which to track differences
     * @param left one of the InstanceAuditHeaders to compare
     * @param right the other InstanceAuditHeader to compare
     * @param ignoreModificationStamps true if we should ignore modification differences (Version, UpdateTime, UpdatedBy)
     *                                 or false if we should include these
     */
    private void getInstanceAuditHeaderDifferences(Differences differences,
                                                   InstanceAuditHeader left,
                                                   InstanceAuditHeader right,
                                                   boolean ignoreModificationStamps)
    {

        Differences.SidePresent present = checkDifferenceNulls(left, right);

        if (present.equals(Differences.SidePresent.BOTH) && !left.equals(right)) {
            if (!ignoreModificationStamps) {
                differences.check("Version", left.getVersion(), right.getVersion());
                differences.check("UpdatedBy", left.getUpdatedBy(), right.getUpdatedBy());
                differences.check("UpdateTime", left.getUpdateTime(), right.getUpdateTime());
            }
            differences.check("Type", left.getType(), right.getType());
            differences.check("InstanceProvenanceType", left.getInstanceProvenanceType(), right.getInstanceProvenanceType());
            differences.check("MetadataCollectionId", left.getMetadataCollectionId(), right.getMetadataCollectionId());
            differences.check("ReplicatedBy", left.getReplicatedBy(), right.getReplicatedBy());
            differences.check("InstanceLicense", left.getInstanceLicense(), right.getInstanceLicense());
            differences.check("CreatedBy", left.getCreatedBy(), right.getCreatedBy());
            differences.check("MaintainedBy", left.getMaintainedBy(), right.getMaintainedBy());
            differences.check("CreateTime", left.getCreateTime(), right.getCreateTime());
            differences.check("Status", left.getStatus(), right.getStatus());
            differences.check("StatusOnDelete", left.getStatusOnDelete(), right.getStatusOnDelete());
            differences.check("MappingProperties", left.getMappingProperties(), right.getMappingProperties());
        } else if (!present.equals(Differences.SidePresent.NEITHER) && !present.equals(Differences.SidePresent.BOTH)) {
            InstanceAuditHeader sideWithValues;
            if (present.equals(Differences.SidePresent.LEFT_ONLY)) {
                sideWithValues = left;
            } else {
                sideWithValues = right;
            }
            if (!ignoreModificationStamps) {
                differences.addOnlyOnOneSide(present, "Version", sideWithValues.getVersion());
                differences.addOnlyOnOneSide(present, "UpdatedBy", sideWithValues.getUpdatedBy());
                differences.addOnlyOnOneSide(present, "UpdateTime", sideWithValues.getUpdateTime());
            }
            differences.addOnlyOnOneSide(present, "Type", sideWithValues.getType());
            differences.addOnlyOnOneSide(present, "InstanceProvenanceType", sideWithValues.getInstanceProvenanceType());
            differences.addOnlyOnOneSide(present, "MetadataCollectionId", sideWithValues.getMetadataCollectionId());
            differences.addOnlyOnOneSide(present, "ReplicatedBy", sideWithValues.getReplicatedBy());
            differences.addOnlyOnOneSide(present, "InstanceLicense", sideWithValues.getInstanceLicense());
            differences.addOnlyOnOneSide(present, "CreatedBy", sideWithValues.getCreatedBy());
            differences.addOnlyOnOneSide(present, "MaintainedBy", sideWithValues.getMaintainedBy());
            differences.addOnlyOnOneSide(present, "CreateTime", sideWithValues.getCreateTime());
            differences.addOnlyOnOneSide(present, "Status", sideWithValues.getStatus());
            differences.addOnlyOnOneSide(present, "StatusOnDelete", sideWithValues.getStatusOnDelete());
            differences.addOnlyOnOneSide(present, "MappingProperties", sideWithValues.getMappingProperties());
        }

    }


    /**
     * Do the null checking between the two objects that are being compared.
     *
     * @param left one object being compared
     * @param right the other object being compared
     * @return Differences.SidePresent
     */
    private Differences.SidePresent checkDifferenceNulls(InstanceAuditHeader left, InstanceAuditHeader right) {
        Differences.SidePresent present;
        if (left == null && right == null) {
            present = Differences.SidePresent.NEITHER;
        } else if (right == null) {
            present = Differences.SidePresent.LEFT_ONLY;
        } else if (left == null) {
            present = Differences.SidePresent.RIGHT_ONLY;
        } else {
            present = Differences.SidePresent.BOTH;
        }
        return present;
    }


    /**
     * When issuing find requests with paging, it can be that we have all the data, but need to only return
     * a subset of the data based on the page size. This method is given the from index and a pageSize and calculates
     * the to index.
     * @param fromIndex the index into the data to start from.
     * @param pageSize the page size to use. 0 means no paging.
     * @param totalSize the total size of the data.
     * @return the to index.
     */
    private int getToIndex(int fromIndex, int pageSize, int totalSize)
    {
        int toIndex;

        if (totalSize < fromIndex + pageSize)
        {
            toIndex = totalSize;
        }
        else
        {
            toIndex = fromIndex + pageSize;
        }

        return toIndex;
    }




    /**
     * Throws a logic error exception when the repository validator is called with invalid parameters.
     * Normally this means the repository validator methods have been called in the wrong order.
     *
     * @param methodName local method that detected the error
     * @throws InvalidParameterException for an invalid parameter - this is typically a logic error
     */
    private void throwParameterError(String     methodName) throws InvalidParameterException
    {
        OMRSErrorCode errorCode = OMRSErrorCode.NULL_PARAMETER;
        String errorMessage     = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(methodName);

        throw new InvalidParameterException(errorCode.getHTTPErrorCode(),
                                          this.getClass().getName(),
                                          methodName,
                                          errorMessage,
                                          errorCode.getSystemAction(),
                                          errorCode.getUserAction());
    }


    /**
     * Throws a logic error exception when the repository validator is called with invalid parameters.
     * Normally this means the repository validator methods have been called in the wrong order.
     *
     * @param methodName local method that detected the error
     * @param instance instance in error
     * @throws RepositoryErrorException there is an invalid instance
     */
    private void throwRepositoryContentError(String              methodName,
                                             InstanceAuditHeader instance) throws RepositoryErrorException
    {
        OMRSErrorCode errorCode = OMRSErrorCode.INVALID_INSTANCE;
        String errorMessage     = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage(methodName,
                                                                                                     instance.toString());

        throw new RepositoryErrorException(errorCode.getHTTPErrorCode(),
                                          this.getClass().getName(),
                                          methodName,
                                          errorMessage,
                                          errorCode.getSystemAction(),
                                          errorCode.getUserAction());
    }



    /**
     * Throw a logic error exception if this object does not have a repository content manager.
     * This would occur if if is being used in an environment where the OMRS has not been properly
     * initialized.
     *
     * @param methodName name of calling method.
     */
    private void validateRepositoryContentManager(String   methodName)
    {
        if (repositoryContentManager == null)
        {
            OMRSErrorCode errorCode = OMRSErrorCode.LOCAL_REPOSITORY_CONFIGURATION_ERROR;
            String errorMessage = errorCode.getErrorMessageId() + errorCode.getFormattedErrorMessage();

            throw new OMRSLogicErrorException(errorCode.getHTTPErrorCode(),
                                              this.getClass().getName(),
                                              methodName,
                                              errorMessage,
                                              errorCode.getSystemAction(),
                                              errorCode.getUserAction());
        }
    }
}
