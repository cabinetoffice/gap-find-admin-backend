INSERT INTO grant_application
VALUES (2, 1, 1, '2022-10-10 09:00:00', 1, '2022-10-10 09:00:00',
        'EV Chargepoint Grant for flat owner-occupiers', 'PUBLISHED',
        '{
        "sections":[{"sectionId":"ELIGIBILITY","sectionTitle":"Eligibility","sectionStatus":"INCOMPLETE","questions":[{"questionId":"ELIGIBILITY","fieldTitle":"Eligibility Statement","displayText":"","questionSuffix":"Does your organisation meet the eligibility criteria?","responseType":"YesNo","validation":{"mandatory":true}}]},{"sectionId":"ESSENTIAL","sectionTitle":"Required checks","sectionStatus":"INCOMPLETE","questions":[{"questionId":"APPLICANT_ORG_NAME","profileField":"ORG_NAME","fieldTitle":"Enter the name of your organisation","hintText":"This is the official name of your organisation. It could be the name that is registered with Companies House or the Charities Commission","adminSummary":"organisation legal name","responseType":"ShortAnswer","validation":{"mandatory":true,"minLength":2,"maxLength":250}},{"questionId":"APPLICANT_TYPE","profileField":"ORG_TYPE","fieldTitle":"Choose your organisation type","hintText":"Choose the option that best describes your organisation","adminSummary":"organisation type (e.g. limited company)","responseType":"Dropdown","validation":{"mandatory":true},"options":["Limited company","Non-limited company","Registered charity","Unregistered charity","Other"]},{"questionId":"APPLICANT_ORG_ADDRESS","profileField":"ORG_ADDRESS","fieldTitle":"Enter your organisation''s address","adminSummary":"registered address","responseType":"AddressInput","validation":{"mandatory":true}},{"questionId":"APPLICANT_ORG_CHARITY_NUMBER","profileField":"ORG_CHARITY_NUMBER","fieldTitle":"Please supply the Charity Commission number for your organisation - if applicable","hintText":"Funding organisation might use this to identify your organisation when you apply for a grant. It might also be used to check your organisation is legitimate.","adminSummary":"Charity Commission number (if applicable)","responseType":"ShortAnswer","validation":{"mandatory":false,"minLength":2,"maxLength":15,"validInput":"alphanumeric-nospace"}},{"questionId":"APPLICANT_ORG_COMPANIES_HOUSE","profileField":"ORG_COMPANIES_HOUSE","fieldTitle":"Please supply the Companies House number for your organisation - if applicable","hintText":"Funding organisation might use this to identify your organisation when you apply for a grant. It might also be used to check your organisation is legitimate.","adminSummary":"Companies House number (if applicable)","responseType":"ShortAnswer","validation":{"mandatory":false,"minLength":2,"maxLength":8,"validInput":"alphanumeric-nospace"}},{"questionId":"APPLICANT_AMOUNT","fieldPrefix":"£","fieldTitle":"How much does your organisation require as a grant?","hintText":"Please enter whole pounds only","adminSummary":"amount of funding required","responseType":"Numeric","validation":{"mandatory":true,"greaterThanZero":true}},{"questionId":"BENEFITIARY_LOCATION","fieldTitle":"Where will this funding be spent?","hintText":"Select the location where the grant funding will be spent. You can choose more than one, if it is being spent in more than one location.\n\nSelect all that apply:","adminSummary":"where the funding will be spent","responseType":"MultipleSelection","validation":{"mandatory":true},"options":["North East England","North West England","South East England","South West England","Midlands","Scotland","Wales","Northern Ireland"]}]}]
        }',
        1, '2022-10-10 09:00:00');

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DO $$
    BEGIN
        FOR r IN 1..1002
            LOOP
                INSERT INTO grant_submission
                VALUES (
                           uuid_generate_v4(),
                           'EV Chargepoint Grant for flat owner-occupiers',
                           now(),
                           '{
                             "sections": [
                               {
                                 "sectionId": "ELIGIBILITY",
                                 "sectionTitle": "Eligibility",
                                 "sectionStatus": "COMPLETED",
                                 "questions": [
                                   {
                                     "questionId": "ELIGIBILITY",
                                     "fieldTitle": "Eligitiblity Statement",
                                     "displayText": "Some admin supplied text describing what it means to be eligible to apply for this grant",
                                     "questionSuffix": "Does your organisation meet the eligibility criteria?",
                                     "responseType": "YesNo",
                                     "validation": {
                                       "mandatory": true
                                     },
                                     "response": "Yes"
                                   }
                                 ]
                               },
                               {
                                 "sectionId": "ESSENTIAL",
                                 "sectionTitle": "Essential Information",
                                 "sectionStatus": "COMPLETED",
                                 "questions": [
                                   {
                                     "questionId": "APPLICANT_TYPE",
                                     "profileField": "ORG_TYPE",
                                     "fieldTitle": "Choose your organisation type",
                                     "hintText": "Choose the option that best describes your organisation",
                                     "responseType": "Dropdown",
                                     "validation": {
                                       "mandatory": true
                                     },
                                     "options": [
                                       "Limited company",
                                       "Non-limited company",
                                       "Registered charity",
                                       "Unregistered charity",
                                       "Other"
                                     ],
                                     "response": "Limited company"
                                   },
                                   {
                                     "questionId": "APPLICANT_ORG_NAME",
                                     "profileField": "ORG_NAME",
                                     "fieldTitle": "Enter the name of your organisation",
                                     "hintText": "This is the official name of your organisation. It could be the name that is registered with Companies House or the Charities Commission",
                                     "responseType": "ShortAnswer",
                                     "validation": {
                                       "mandatory": true,
                                       "minLength": 5,
                                       "maxLength": 100
                                     },
                                     "response": "Some company name"
                                   },
                                   {
                                     "questionId": "APPLICANT_AMOUNT",
                                     "profileField": "ORG_AMOUNT",
                                     "fieldTitle": "Enter the money you would wish to receive",
                                     "hintText": "This is the official name of your organisation. It could be the name that is registered with Companies House or the Charities Commission",
                                     "responseType": "ShortAnswer",
                                     "validation": {
                                       "mandatory": true,
                                       "minLength": 5,
                                       "maxLength": 100
                                     },
                                     "response": "500"
                                   },
                                   {
                                     "questionId": "APPLICANT_ORG_ADDRESS",
                                     "profileField": "ORG_ADDRESS",
                                     "fieldTitle": "Enter your organisation''s address",
                                     "responseType": "AddressInput",
                                     "validation": {
                                       "mandatory": true
                                     },
                                     "multiResponse": [
                                       "9-10 St Andrew Square",
                                       "",
                                       "Edinburgh",
                                       "",
                                       "EH2 2AF"
                                     ]
                                   },
                                   {
                                     "questionId": "APPLICANT_ORG_COMPANIES_HOUSE",
                                     "profileField": "ORG_COMPANIES_HOUSE",
                                     "fieldTitle": "Please supply the Companies House number for your organisation - if applicable",
                                     "hintText": "Funding organisation might use this to identify your organisation when you apply for a grant. It might also be used to check your organisation is legitimate.",
                                     "responseType": "ShortAnswer",
                                     "validation": {
                                       "mandatory": true
                                     },
                                     "response": "Yes"
                                   },
                                   {
                                     "questionId": "APPLICANT_ORG_CHARITY_NUMBER",
                                     "profileField": "ORG_CHARITY_COMMISSION_NUMBER",
                                     "fieldTitle": "Please supply the Charity Commission number for your organisation - if applicable",
                                     "hintText": "Funding organisation might use this to identify your organisation when you apply for a grant. It might also be used to check your organisation is legitimate.",
                                     "responseType": "ShortAnswer",
                                     "validation": {
                                       "mandatory": true
                                     },
                                     "response": "12738494"
                                   },
                                   {
                                     "questionId": "BENEFITIARY_LOCATION",
                                     "fieldTitle": "Where will this funding be spent?",
                                     "hintText": "Select the location where the grant funding will be spent. You can choose more than one, if it is being spent in more than one location.\n\nSelect all that apply:",
                                     "adminSummary": "where the funding will be spent",
                                     "responseType": "MultipleSelection",
                                     "validation": {
                                       "mandatory": true
                                     },
                                     "options": [
                                       "North East England",
                                       "North West England",
                                       "South East England",
                                       "South West England",
                                       "Midlands",
                                       "Scotland",
                                       "Wales",
                                       "Northern Ireland"
                                     ],
                                     "multiResponse": [
                                       "Scotland",
                                       "North East England"
                                     ]
                                   }
                                 ]
                               },
                               {
                                 "sectionId": "CUSTOM_SECTION_1",
                                 "sectionTitle": "Project Information",
                                 "questions": [
                                   {
                                     "questionId": "CUSTOM_APPLICANT_TYPE",
                                     "profileField": "ORG_TYPE",
                                     "fieldTitle": "Choose your organisation type",
                                     "hintText": "Choose the option that best describes your organisation",
                                     "responseType": "Dropdown",
                                     "validation": {
                                       "mandatory": false
                                     },
                                     "options": [
                                       "Limited company",
                                       "Non-limited company",
                                       "Registered charity",
                                       "Unregistered charity",
                                       "Other"
                                     ]
                                   },
                                   {
                                     "questionId": "CUSTOM_CUSTOM_QUESTION_1",
                                     "fieldTitle": "Description of the project, please include information regarding public accessibility (see GOV.UK guidance for a definition of public access) to the newly planted trees",
                                     "responseType": "LongAnswer",
                                     "validation": {
                                       "mandatory": false,
                                       "minLength": 100,
                                       "maxLength": 2000,
                                       "minWords": 20,
                                       "maxWords": 500
                                     }
                                   },
                                   {
                                     "questionId": "CUSTOM_APPLICANT_ORG_NAME",
                                     "profileField": "ORG_NAME",
                                     "fieldTitle": "Enter the name of your organisation",
                                     "hintText": "This is the official name of your organisation. It could be the name that is registered with Companies House or the Charities Commission",
                                     "responseType": "ShortAnswer",
                                     "validation": {
                                       "mandatory": false,
                                       "minLength": 5,
                                       "maxLength": 100
                                     }
                                   },
                                   {
                                     "questionId": "CUSTOM_APPLICANT_ORG_ADDRESS",
                                     "profileField": "ORG_ADDRESS",
                                     "fieldTitle": "Enter your organisation''s address",
                                     "responseType": "AddressInput",
                                     "validation": {
                                       "mandatory": false
                                     }
                                   },
                                   {
                                     "questionId": "CUSTOM_APPLICANT_ORG_COMPANIES_HOUSE",
                                     "profileField": "ORG_COMPANIES_HOUSE",
                                     "fieldTitle": "Does your organisation have a Companies House number?",
                                     "hintText": "Funding organisation might use this to identify your organisation when you apply for a grant. It might also be used to check your organisation is legitimate.",
                                     "responseType": "YesNo",
                                     "validation": {
                                       "mandatory": false
                                     }
                                   },
                                   {
                                     "questionId": "CUSTOM_APPLICANT_ORG_CHARITY_COMMISSION_NUMBER",
                                     "profileField": "ORG_CHARITY_COMMISSION_NUMBER",
                                     "fieldTitle": "What type is your company",
                                     "hintText": "Funding organisation might use this to identify your organisation when you apply for a grant. It might also be used to check your organisation is legitimate.",
                                     "responseType": "MultipleSelection",
                                     "validation": {
                                       "mandatory": false
                                     },
                                     "options": [
                                       "Limited company",
                                       "Non-limited company",
                                       "Registered charity",
                                       "Unregistered charity",
                                       "Other"
                                     ]
                                   },
                                   {
                                     "questionId": "CUSTOM_CUSTOM_QUESTION_4",
                                     "fieldTitle": "Please provide the date of your last awarded grant",
                                     "responseType": "Date",
                                     "validation": {
                                       "mandatory": false
                                     }
                                   }
                                 ]
                               }
                             ]
                           }',
                           now(),
                           'SUBMITTED',
                           now(),
                           1, -- version
                           1, -- applicant_id
                           2, -- application_id
                           1, -- created_by
                           1, -- last_updated_by
                           3, -- scheme_id
                           'GAP-LL-20201010-' || r);
            end loop;
    end $$;