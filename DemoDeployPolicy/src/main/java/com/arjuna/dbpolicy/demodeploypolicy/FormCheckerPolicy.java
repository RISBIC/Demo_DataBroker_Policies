/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved.
 */

package com.arjuna.dbpolicy.demodeploypolicy;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import com.arjuna.agility.ServiceAgreement;
import com.arjuna.agility.ServiceAgreementContext;
import com.arjuna.agility.ServiceAgreementListener;
import com.arjuna.agility.ServiceAgreementListenerException;
import com.arjuna.agility.Vote;
import com.arjuna.dbpolicy.demodeploypolicy.view.PrivacyImpactAssessmentView;

@Stateless
public class FormCheckerPolicy implements ServiceAgreementListener
{
    private static final Logger logger = Logger.getLogger(FormCheckerPolicy.class.getName());

    public void onRegistered(String domain)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "FormCheckerPolicy.onRegistered");
    }

    public Vote onChangeProposed(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "FormCheckerPolicy.onChangeProposed");

        if (serviceAgreement.isCompatible(PrivacyImpactAssessmentView.class))
        {
            PrivacyImpactAssessmentView privacyImpactAssessmentView = serviceAgreement.asView(PrivacyImpactAssessmentView.class);

            String state = privacyImpactAssessmentView.getState();

            logger.log(Level.FINE, "  onChangeProposed: State: " + state);

            if ((state == null) || check(privacyImpactAssessmentView))
                return Vote.accept();
            else
                return Vote.reject();
        }

        return Vote.ignore();
    }

    public void onChanged(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "FormCheckerPolicy.onChanged");

        if (serviceAgreement.isCompatible(PrivacyImpactAssessmentView.class))
        {
            PrivacyImpactAssessmentView privacyImpactAssessmentView = serviceAgreement.asView(PrivacyImpactAssessmentView.class);

            String state = privacyImpactAssessmentView.getState();

            logger.log(Level.FINE, "  onChanged: State: " + state);

            if ((state == null) || (! "active".equals(state)))
            {
                logger.log(Level.FINE, "FormCheckerPolicy.onChanged: now active?");

                boolean acceptable = check(privacyImpactAssessmentView);

                if (acceptable)
                {
                    logger.log(Level.FINE, "FormCheckerPolicy.onChanged: acceptable");

                    privacyImpactAssessmentView.setState("active");

                    serviceAgreementContext.getServiceAgreementManager().propose(serviceAgreement);
                }
                else
                    logger.log(Level.FINE, "FormCheckerPolicy.onChanged: not acceptable");
            }
        }
    }

    public void onChangeRejected(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "FormCheckerPolicy.onChangeRejected");
    }

    public Vote onTerminateProposed(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "FormCheckerPolicy.onTerminateProposed");

        return Vote.ignore();
    }

    public void onTerminated(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "FormCheckerPolicy.onTerminated");
    }

    public void onTerminateRejected(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "FormCheckerPolicy.onTerminateRejected");
    }

    public void onUnregistered(String domain)
         throws ServiceAgreementListenerException
    {
        logger.log(Level.FINE, "FormCheckerPolicy.onUnregistered");
    }

    public boolean check(PrivacyImpactAssessmentView privacyImpactAssessmentView)
    {
        logger.log(Level.FINE, "FormCheckerPolicy.check");

        return "false".equals(privacyImpactAssessmentView.getNameAddr()) &&
               "false".equals(privacyImpactAssessmentView.getBirthDate()) &&
               "false".equals(privacyImpactAssessmentView.getNIS()) &&
               "false".equals(privacyImpactAssessmentView.getBankDetails()) &&
               "false".equals(privacyImpactAssessmentView.getHealthRecords()) &&
               "false".equals(privacyImpactAssessmentView.getSocialCareRecords()) &&
               "false".equals(privacyImpactAssessmentView.getEductionalRecords()) &&
               "false".equals(privacyImpactAssessmentView.getBenifitsAndCouncilTaxRecords()) &&
               "false".equals(privacyImpactAssessmentView.getCCTVOtherFootage()) &&
               "false".equals(privacyImpactAssessmentView.getInternallyConfidentialMaterial()) &&
               "false".equals(privacyImpactAssessmentView.getContracts()) &&
               "false".equals(privacyImpactAssessmentView.getChangeDataHandling()) &&
               "false".equals(privacyImpactAssessmentView.getChangePersonalRecording()) &&
               "false".equals(privacyImpactAssessmentView.getIncreaseClassificationRating()) &&
               "false".equals(privacyImpactAssessmentView.getAccessAdditionalStaff()) &&
               "false".equals(privacyImpactAssessmentView.getRequiredForPublication()) &&
               "false".equals(privacyImpactAssessmentView.getAggregatedData()) &&
               "false".equals(privacyImpactAssessmentView.getBrokenDownByWard()) &&
               "false".equals(privacyImpactAssessmentView.getMoreThan20Records()) &&
               "false".equals(privacyImpactAssessmentView.getCouldIdentifyIndividuals()) &&
               "false".equals(privacyImpactAssessmentView.getInformationUnderLicense());
    }
}
