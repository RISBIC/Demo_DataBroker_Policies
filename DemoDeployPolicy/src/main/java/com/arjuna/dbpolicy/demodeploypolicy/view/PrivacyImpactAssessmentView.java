/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbpolicy.demodeploypolicy.view;

import com.arjuna.agility.annotation.Feature;

public interface PrivacyImpactAssessmentView
{
    @Feature(name = "sectiona.programinitiative")
    public String getProgramInitiative();
    public void setProgramInitiative(String programInitiative);

    @Feature(name = "sectiona.informationassetowner")
    public String getInformationAssetOwner();
    public void setInformationAssetOwner(String informationAssetOwner);

    @Feature(name = "sectiona.projectlead")
    public String getProjectLead();
    public void setProjectLead(String projectLead);


    @Feature(name = "sectionb.nameaddr")
    public String getNameAddr();
    public void setNameAddr(String nameAddr);

    @Feature(name = "sectionb.birthdate")
    public String getBirthDate();
    public void setBirthDate(String birthDate);

    @Feature(name = "sectionb.nis")
    public String getNIS();
    public void setNIS(String nis);

    @Feature(name = "sectionb.bankdetails")
    public String getBankDetails();
    public void setBankDetails(String bankDetails);

    @Feature(name = "sectionb.healthrecords")
    public String getHealthRecords();
    public void setHealthRecords(String healthRecords);

    @Feature(name = "sectionb.socialcarerecords")
    public String getSocialCareRecords();
    public void setSocialCareRecords(String socialCareRecords);

    @Feature(name = "sectionb.eductionalrecords")
    public String getEductionalRecords();
    public void setEductionalRecords(String eductionalRecords);

    @Feature(name = "sectionb.benifitsandcounciltaxrecords")
    public String getBenifitsAndCouncilTaxRecords();
    public void setBenifitsAndCouncilTaxRecords(String benifitsAndCouncilTaxRecords);

    @Feature(name = "sectionb.cctvotherfootage")
    public String getCCTVOtherFootage();
    public void setCCTVOtherFootage(String cctvOtherFootage);

    @Feature(name = "sectionb.internallyconfidentialmaterial")
    public String getInternallyConfidentialMaterial();
    public void setInternallyConfidentialMaterial(String internallyConfidentialMaterial);

    @Feature(name = "sectionb.contracts")
    public String getContracts();
    public void setContracts(String contracts);


    @Feature(name = "sectionc.changedatahandling")
    public String getChangeDataHandling();
    public void setChangeDataHandling(String changeDataHandling);

    @Feature(name = "sectionc.changepersonalrecording")
    public String getChangePersonalRecording();
    public void setChangePersonalRecording(String changePersonalRecording);

    @Feature(name = "sectionc.increaseclassificationrating")
    public String getIncreaseClassificationRating();
    public void setIncreaseClassificationRating(String increaseClassificationRating);

    @Feature(name = "sectionc.accessadditionalstaff")
    public String getAccessAdditionalStaff();
    public void setAccessAdditionalStaff(String accessAdditionalStaff);

    @Feature(name = "sectionc.requiredforpublication")
    public String getRequiredForPublication();
    public void setRequiredForPublication(String requiredForPublication);


    @Feature(name = "sectiond.aggregateddata")
    public String getAggregatedData();
    public void setAggregatedData(String aggregatedData);

    @Feature(name = "sectiond.brokendownbyward")
    public String getBrokenDownByWard();
    public void setBrokenDownByWard(String brokenDownByWard);

    @Feature(name = "sectiond.morethan20records")
    public String getMoreThan20Records();
    public void setMoreThan20Records(String moreThan20Records);

    @Feature(name = "sectiond.couldidentifyindividuals")
    public String getCouldIdentifyIndividuals();
    public void setCouldIdentifyIndividuals(String couldIdentifyIndividuals);

    @Feature(name = "sectiond.informationunderlicense")
    public String getInformationUnderLicense();
    public void setInformationUnderLicense(String informationUnderLicense);


    @Feature(name = "change.details")
    public String getDetails();
    public void setDetails(String details);

    @Feature(name = "change.date")
    public String getDate();
    public void setDate(String date);

    @Feature(name = "change.assetowner")
    public String getAssetOwner();
    public void setAssetOwner(String assetOwner);


    @Feature(name = "gateway.state", optional=true)
    public String getState();
    public void setState(String state);

    @Feature(name = "gateway.endpoint", optional=true)
    public String getEndpoint();
    public void setEndpoint(String endpoint);


    @Feature(name = "databroker.flowname", optional=true)
    public String getFlowName();
    public void setFlowName(String flowName);
}
