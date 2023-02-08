--NOTE: when inserting text single quotes must be escaped. In Postgres this is done by doubling them up, like so - 'How''s the weather?'

INSERT INTO grant_funding_organisation values (1, 'Test Org');
INSERT INTO grant_funding_organisation VALUES (2, 'Evil Org');

INSERT INTO gap_user values (1, '6a6131f2-239e-11ed-861d-0242ac120002');
INSERT INTO grant_admin values (1, 1, 1);

INSERT INTO gap_user values (2, '55a0b020-239f-11ed-861d-0242ac120002');
INSERT INTO grant_admin values (2, 1, 2);

INSERT INTO gap_user VALUES (3, '281b3363-6eab-44fc-a6af-d87cc4b94131');
INSERT INTO grant_admin VALUES (3, 2, 3);

INSERT INTO grant_scheme VALUES (1, 1, 1, 'SCH-000003589', '2022-08-03 11:48:02.669144', NULL, NULL, 'EV Chargepoint Grant for flat owner-occupiers', 'chris.steele@and.digital', 1);
INSERT INTO grant_scheme VALUES (2, 1, 1, 'SCH-000003590', '2022-08-03 11:48:33.686115', NULL, NULL, 'Innovate UK Smart Grants: April 2022', 'dale.barrie@and.digital', 2);
INSERT INTO grant_scheme VALUES (3, 1, 1, 'SCH-000003591', '2022-08-03 11:48:53.254993', NULL, NULL, 'Woodland Partnership (Forestry England)', 'chris.steele@and.digital', 1);
INSERT INTO grant_scheme VALUES (4, 2, 1, 'SCH-000003591', '2022-08-03 11:48:53.254993', NULL, NULL, 'And Digital Grant', '', 3);
INSERT INTO grant_scheme VALUES (5, 1, 1, 'SCH-000003592', '2022-08-03 12:48:33.686115', NULL, NULL, 'UK Tradeshow Programme (UKTP)', 'dominic.west@and.digital', 1);
INSERT INTO grant_scheme VALUES (6, 1, 1, 'SCH-000003592', '2022-08-03 12:48:33.686115', NULL, NULL, 'UK Tradeshow Programme (UKTP) - Future', 'dominic.west@and.digital', 1);

INSERT INTO grant_application VALUES (1, 3, 1, '2022-09-09 14:58:24.862874', 12345, '2022-09-09 14:58:24.862875', 'Woodland Partnership Application', 'DRAFT', '{"sections":[{"sectionId":"ELIGIBILITY","sectionTitle":"Eligibility","sectionStatus":"INCOMPLETE","questions":[{"questionId":"ELIGIBILITY","fieldTitle":"Eligibility Statement","displayText":"","questionSuffix":"Does your organisation meet the eligibility criteria?","responseType":"YesNo","validation":{"mandatory":true}}]},{"sectionId":"ESSENTIAL","sectionTitle":"Required checks","sectionStatus":"INCOMPLETE","questions":[{"questionId":"APPLICANT_ORG_NAME","profileField":"ORG_NAME","fieldTitle":"Enter the name of your organisation","hintText":"This is the official name of your organisation. It could be the name that is registered with Companies House or the Charities Commission","adminSummary":"organisation legal name","responseType":"ShortAnswer","validation":{"mandatory":true,"minLength":2,"maxLength":250}},{"questionId":"APPLICANT_TYPE","profileField":"ORG_TYPE","fieldTitle":"Choose your organisation type","hintText":"Choose the option that best describes your organisation","adminSummary":"organisation type (e.g. limited company)","responseType":"Dropdown","validation":{"mandatory":true},"options":["Limited company","Non-limited company","Registered charity","Unregistered charity","Other"]},{"questionId":"APPLICANT_ORG_ADDRESS","profileField":"ORG_ADDRESS","fieldTitle":"Enter your organisation''s address","adminSummary":"registered address","responseType":"AddressInput","validation":{"mandatory":true}},{"questionId":"APPLICANT_ORG_CHARITY_NUMBER","profileField":"ORG_CHARITY_NUMBER","fieldTitle":"Please supply the Charity Commission number for your organisation - if applicable","hintText":"Funding organisation might use this to identify your organisation when you apply for a grant. It might also be used to check your organisation is legitimate.","adminSummary":"Charity Commission number (if applicable)","responseType":"ShortAnswer","validation":{"mandatory":false,"minLength":2,"maxLength":15,"validInput":"alphanumeric-nospace"}},{"questionId":"APPLICANT_ORG_COMPANIES_HOUSE","profileField":"ORG_COMPANIES_HOUSE","fieldTitle":"Please supply the Companies House number for your organisation - if applicable","hintText":"Funding organisation might use this to identify your organisation when you apply for a grant. It might also be used to check your organisation is legitimate.","adminSummary":"Companies House number (if applicable)","responseType":"ShortAnswer","validation":{"mandatory":false,"minLength":2,"maxLength":8,"validInput":"alphanumeric-nospace"}},{"questionId":"APPLICANT_AMOUNT","fieldPrefix":"£","fieldTitle":"How much does your organisation require as a grant?","hintText":"Please enter whole pounds only","adminSummary":"amount of funding required","responseType":"Numeric","validation":{"mandatory":true,"greaterThanZero":true}},{"questionId":"BENEFITIARY_LOCATION","fieldTitle":"Where will this funding be spent?","hintText":"Select the location where the grant funding will be spent. You can choose more than one, if it is being spent in more than one location.\n\nSelect all that apply:","adminSummary":"where the funding will be spent","responseType":"MultipleSelection","validation":{"mandatory":true},"options":["North East England","North West England","South East England","South West England","Midlands","Scotland","Wales","Northern Ireland"]}]}]}', 1, NULL);

INSERT INTO public.grant_applicant VALUES (1, '3a6cfe2d-bf58-440d-9e07-3579c7dcf207');

INSERT INTO public.grant_submission VALUES ('3a6cfe2d-bf58-440d-9e07-3579c7dcf207', 'Test Grant Application 3', '2022-08-02 20:10:20', '{
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
          "validation": { "mandatory": true },
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
          "validation": { "mandatory": true },
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
          "validation": { "mandatory": true, "minLength": 5, "maxLength": 100 },
          "response": "Some company name"
        },
         {
                  "questionId": "APPLICANT_AMOUNT",
                  "profileField": "ORG_AMOUNT",
                  "fieldTitle": "Enter the money you would wish to receive",
                  "hintText": "This is the official name of your organisation. It could be the name that is registered with Companies House or the Charities Commission",
                  "responseType": "ShortAnswer",
                  "validation": { "mandatory": true, "minLength": 5, "maxLength": 100 },
                  "response": "500"
                },
        {
          "questionId": "APPLICANT_ORG_ADDRESS",
          "profileField": "ORG_ADDRESS",
          "fieldTitle": "Enter your organisation''s address",
          "responseType": "AddressInput",
          "validation": { "mandatory": true },
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
          "validation": { "mandatory": true },
          "response": "Yes"
        },
        {
          "questionId": "APPLICANT_ORG_CHARITY_NUMBER",
          "profileField": "ORG_CHARITY_COMMISSION_NUMBER",
          "fieldTitle": "Please supply the Charity Commission number for your organisation - if applicable",
          "hintText": "Funding organisation might use this to identify your organisation when you apply for a grant. It might also be used to check your organisation is legitimate.",
          "responseType": "ShortAnswer",
          "validation": { "mandatory": true },
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
          "multiResponse": ["Scotland", "North East England"]
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
          "validation": { "mandatory": false },
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
          "validation": { "mandatory": false, "minLength": 5, "maxLength": 100 }
        },
        {
          "questionId": "CUSTOM_APPLICANT_ORG_ADDRESS",
          "profileField": "ORG_ADDRESS",
          "fieldTitle": "Enter your organisation''s address",
          "responseType": "AddressInput",
          "validation": { "mandatory": false }
        },
        {
          "questionId": "CUSTOM_APPLICANT_ORG_COMPANIES_HOUSE",
          "profileField": "ORG_COMPANIES_HOUSE",
          "fieldTitle": "Does your organisation have a Companies House number?",
          "hintText": "Funding organisation might use this to identify your organisation when you apply for a grant. It might also be used to check your organisation is legitimate.",
          "responseType": "YesNo",
          "validation": { "mandatory": false }
        },
        {
          "questionId": "CUSTOM_APPLICANT_ORG_CHARITY_COMMISSION_NUMBER",
          "profileField": "ORG_CHARITY_COMMISSION_NUMBER",
          "fieldTitle": "What type is your company",
          "hintText": "Funding organisation might use this to identify your organisation when you apply for a grant. It might also be used to check your organisation is legitimate.",
          "responseType": "MultipleSelection",
          "validation": { "mandatory": false },
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
          "validation": { "mandatory": false }
        }
      ]
    }
  ]
}', '2022-08-02 20:10:20', 'SUBMITTED', '2022-09-27 12:34:56', 1, 1, 1, 1, 1, 1, 'GAP-LL-20220927-1');

INSERT INTO public.grant_applicant_organisation_profile VALUES (1, '9 George Street', NULL, '1234567', '9876543', 'Glasgow City', 'Fake Org', 'G2 1DY', 'Glasgow', 'Charity', 1);

INSERT INTO public.grant_beneficiary VALUES ('cab7ac24-5705-4ecb-88a5-81f43cf7d0f9', 3, 1, '3a6cfe2d-bf58-440d-9e07-3579c7dcf207', 1, '2022-08-02 20:10:20', 1, NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'GAP-LL-20220929-00001');

-- useful data that aligns with TCO Test Account used for applicant tests
INSERT INTO public.grant_applicant(id, user_id) VALUES (2, '7373c0a2-3d08-4ec7-b454-1682dc16b036');
INSERT INTO public.grant_applicant_organisation_profile(id, address_line1, address_line2, charity_commission_number, companies_house_number, county, legal_name, postcode, town, TYPE, applicant_id)
VALUES (2, '9 George Square', 'City Centre', '55667788', '11223344', 'Glasgow', 'AND Digital', 'G2 1QQ', 'GLASGOW', 'LIMITED_COMPANY', 2);

-- Because we are manually inserting data with ids, we need to update the sequencing column for each table.
-- Otherwise new entries will start from 1
SELECT SETVAL((SELECT PG_GET_SERIAL_SEQUENCE('"grant_scheme"', 'grant_scheme_id')), (SELECT MAX(grant_scheme_id) FROM grant_scheme));
SELECT SETVAL((SELECT PG_GET_SERIAL_SEQUENCE('"grant_application"', 'grant_application_id')), (SELECT MAX(grant_application_id) FROM grant_application));
SELECT SETVAL((SELECT PG_GET_SERIAL_SEQUENCE('"gap_user"', 'gap_user_id')), (SELECT MAX(gap_user_id) FROM gap_user));
SELECT SETVAL((SELECT PG_GET_SERIAL_SEQUENCE('"grant_funding_organisation"', 'funder_id')), (SELECT MAX(funder_id) FROM grant_funding_organisation));
SELECT SETVAL((SELECT PG_GET_SERIAL_SEQUENCE('"grant_admin"', 'grant_admin_id')), (SELECT MAX(grant_admin_id) FROM grant_admin));

INSERT INTO public.grant_advert (grant_advert_id, contentful_entry_id, contentful_slug, created, grant_advert_name, last_updated, response, status, version, created_by, last_updated_by, scheme_id)
VALUES ('fa8f4b1d-d090-4ff6-97be-ccabd3b1d87d', NULL, NULL, '2022-11-28 14:36:21.628791', 'UK Tradeshow Programme (UKTP) - Advert', '2022-11-28 14:36:21.628793',
'{
    "sections":[
       {
          "pages":[
             {
                "questions":[
                   {
                      "id":"grantShortDescription",
                      "seen":true,
                      "response":"Businesses exporting or thinking of exporting from the UK can attend UK Tradeshow Programmes selection of supported overseas tradeshows and conferences, and potentially receive grants to offset some costs.",
                      "multiResponse":null
                   }
                ],
                "id":"1",
                "status":"COMPLETED"
             },
             {
                "questions":[
                   {
                      "id":"grantLocation",
                      "seen":true,
                      "response":null,
                      "multiResponse":[
                         "National"
                      ]
                   }
                ],
                "id":"2",
                "status":"COMPLETED"
             },
             {
                "questions":[
                   {
                      "id":"grantFunder",
                      "seen":true,
                      "response":"The UK Tradeshow Programme",
                      "multiResponse":null
                   }
                ],
                "id":"3",
                "status":"COMPLETED"
             },
             {
                "questions":[
                   {
                      "id":"grantApplicantType",
                      "seen":true,
                      "response":null,
                      "multiResponse":[
                          "Public Sector",
                          "Non-profit",
                          "Private Sector"
                      ]
                   }
                ],
                "id":"4",
                "status":"COMPLETED"
             }
          ],
          "id":"grantDetails",
          "status":"COMPLETED"
       },
       {
          "pages":[
             {
                "questions":[
                   {
                      "id":"grantApplicationOpenDate",
                      "seen":true,
                      "response":null,
                      "multiResponse":[
                        "31",
                        "3",
                        "2022",
                        "00",
                        "01"
                      ]
                   },
                   {
                      "id":"grantApplicationCloseDate",
                      "seen":true,
                      "response":null,
                      "multiResponse":[
                        "1",
                        "6",
                        "2022",
                        "23",
                        "59"
                      ]
                  }
                ],
                "id":"1",
                "status":"COMPLETED"
             }
          ],
          "id":"applicationDates",
          "status":"COMPLETED"
       },
      {
         "pages":[
            {
               "questions":[
                  {
                     "id":"grantTotalAwardAmount",
                     "seen":true,
                     "response":"5000000",
                     "multiResponse":null
                  },
                  {
                     "id":"grantMaximumAward",
                     "seen":true,
                     "response":"1000000",
                     "multiResponse":null
                  },
                  {
                     "id":"grantMinimumAward",
                     "seen":true,
                     "response":"100000",
                     "multiResponse":null
                  }
               ],
               "id":"1",
               "status":"COMPLETED"
            }
         ],
         "id":"awardAmounts",
         "status":"COMPLETED"
      },
       {
          "pages":[
             {
                "questions":[
                   {
                      "id":"grantWebpageUrl",
                      "seen":true,
                      "response":"https://www.gov.uk/guidance/uk-tradeshow-programme",
                      "multiResponse":null
                   }
                ],
                "id":"1",
                "status":"COMPLETED"
             }
          ],
          "id":"howToApply",
          "status":"COMPLETED"
       },
       {
          "pages":[
             {
                "questions":[
                   {
                      "id":"grantEligibilityTab",
                      "seen":true,
                      "response":null,
                      "multiResponse": [
                          "<h2 class=\"govuk-heading\">Eligibility</h2><div class=\"grant_grant__breakword__72hvW\"><h3><strong>Programme eligibility criteria for exhibitors</strong></h3><p>You must not have previously received support to exhibit from the UK Tradeshow Programme.</p><p>You must:</p><ul><li><p>be exhibiting for the first time or wishing to venture into new markets</p></li><li><p>be turning over annually between &pound;250,000 to &pound;5 million</p></li><li><p>not have committed to attending the event before applying for support</p></li><li><p>be actively investigating export opportunities for your own business:</p><ul><li><p>having not previously exported, or</p></li><li><p>having previously exported and wishing to grow exports in new markets</p></li></ul></li></ul><h3><strong>Programme eligibility criteria for attendees</strong></h3><p>You must not have previously received any support from the UK Tradeshow Programme.</p><p>You must:</p><ul><li><p>be attending a show for the first time or wishing to venture into new markets</p></li><li><p>be turning over annually between &pound;85,000 to &pound;250,000</p></li><li><p>not have committed to attending the event before applying for support</p></li><li><p>be actively investigating export opportunities for your own business:</p><ul><li><p>having not previously exported, or</p></li><li><p>having previously exported and wishing to grow exports in new markets</p></li></ul></li></ul><h3><strong>Common eligibility criteria</strong></h3><p>Access to support from the programme is limited.</p><p>Organisations can apply for support from each part of the programme once only, subject to programme eligibility criteria.</p><p>You must be UK VAT-registered to apply.</p><h3><strong>General eligibility criteria</strong></h3><p>Eligible businesses must be small to medium-sized enterprises (SMEs) (fewer than 250 employees), based in the UK (excluding Isle of Man or the Channel Islands), and either:</p><ul><li><p>sell products or services which substantially originate from the UK, or</p></li><li><p>add significant value to a product or service of non-UK origin</p></li></ul><p>You may be required to evidence that you meet the criteria.</p><h3><strong>Financial support eligibility criteria</strong></h3><p>In addition to the above conditions, for financial support you must:</p><ul><li><p>not receive any contributions, from another programme or body, towards the eligible costs covered by the UK Tradeshow Programme</p></li><li><p>not have received government aid in excess of the limits below:</p><ul><li><p>if you are a business in Great Britain and state aid rules under the Northern Ireland Protocol do not apply: 325,000 SDR (approximately &pound;332,000)</p></li><li><p>if you are a business within the scope of Northern Ireland Protocol State aid rules: &euro;200,000, except for businesses in the sectors below</p></li></ul></li></ul><p>Some sectors have a lower limit for government aid. This is due to the Northern Ireland Protocol&rsquo;s state aid rules.</p><p>These limits and sectors are:</p><ul><li><p>&euro;20,000 if you are active in the fishery and aquaculture sector</p></li><li><p>&euro;30,000 if you are active in the primary production of agriculture products</p></li></ul><p>UK Tradeshow Programme financial support is likely to constitute state aid or a subsidy to recipients.</p><p>For more information, please refer to&nbsp;guidance&nbsp;on the&nbsp;<a href=\"https://www.gov.uk/government/publications/complying-with-the-uks-international-obligations-on-subsidy-control-guidance-for-public-authorities\"><u>UK&rsquo;s international subsidy control commitments</u></a><u>.</u></p><h3><strong>Ineligibility for support</strong></h3><p>DIT will regard an SME as ineligible if:</p><ul><li><p>it has previously received exhibitor support from the UK Tradeshow Programme</p></li><li><p>there is evidence it is planning to close all operations or transfer its assets overseas or offshore jobs</p></li><li><p>the company or individual has a business record or business practices or products which is/are likely to cause offence in the overseas market and/or embarrass the UK government (for example, on corporate social responsibility grounds)</p></li><li><p>it is offering a product which is illegal to produce or sell in the UK or in the target market</p></li><li><p>the company&rsquo;s product would breach export controls if sold abroad</p></li></ul></div>",
                          "{\"nodeType\":\"document\",\"data\":{},\"content\":[{\"nodeType\":\"heading-2\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Eligibility\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"heading-3\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Programme eligibility criteria for exhibitors\",\"marks\":[{\"type\":\"bold\"}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"You must not have previously received support to exhibit from the UK Tradeshow Programme.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"You must:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"be exhibiting for the first time or wishing to venture into new markets\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"be turning over annually between £250,000 to £5 million\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"not have committed to attending the event before applying for support\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"be actively investigating export opportunities for your own business:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"having not previously exported, or\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"having previously exported and wishing to grow exports in new markets\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"heading-3\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Programme eligibility criteria for attendees\",\"marks\":[{\"type\":\"bold\"}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"You must not have previously received any support from the UK Tradeshow Programme.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"You must:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"be attending a show for the first time or wishing to venture into new markets\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"be turning over annually between £85,000 to £250,000\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"not have committed to attending the event before applying for support\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"be actively investigating export opportunities for your own business:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"having not previously exported, or\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"having previously exported and wishing to grow exports in new markets\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"heading-3\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Common eligibility criteria\",\"marks\":[{\"type\":\"bold\"}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Access to support from the programme is limited.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Organisations can apply for support from each part of the programme once only, subject to programme eligibility criteria.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"You must be UK VAT-registered to apply.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"heading-3\",\"content\":[{\"nodeType\":\"text\",\"value\":\"General eligibility criteria\",\"marks\":[{\"type\":\"bold\"}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Eligible businesses must be small to medium-sized enterprises (SMEs) (fewer than 250 employees), based in the UK (excluding Isle of Man or the Channel Islands), and either:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"sell products or services which substantially originate from the UK, or\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"add significant value to a product or service of non-UK origin\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"You may be required to evidence that you meet the criteria.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"heading-3\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Financial support eligibility criteria\",\"marks\":[{\"type\":\"bold\"}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"In addition to the above conditions, for financial support you must:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"not receive any contributions, from another programme or body, towards the eligible costs covered by the UK Tradeshow Programme\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"not have received government aid in excess of the limits below:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"if you are a business in Great Britain and state aid rules under the Northern Ireland Protocol do not apply: 325,000 SDR (approximately £332,000)\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"if you are a business within the scope of Northern Ireland Protocol State aid rules: €200,000, except for businesses in the sectors below\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Some sectors have a lower limit for government aid. This is due to the Northern Ireland Protocol’s state aid rules.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"These limits and sectors are:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"€20,000 if you are active in the fishery and aquaculture sector\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"€30,000 if you are active in the primary production of agriculture products\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"UK Tradeshow Programme financial support is likely to constitute state aid or a subsidy to recipients.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"For more information, please refer to guidance on the \",\"marks\":[],\"data\":{}},{\"nodeType\":\"hyperlink\",\"data\":{\"uri\":\"https://www.gov.uk/government/publications/complying-with-the-uks-international-obligations-on-subsidy-control-guidance-for-public-authorities\"},\"content\":[{\"nodeType\":\"text\",\"value\":\"UK’s international subsidy control commitments\",\"marks\":[],\"data\":{}}]},{\"nodeType\":\"text\",\"value\":\".\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"heading-3\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Ineligibility for support\",\"marks\":[{\"type\":\"bold\"}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"DIT will regard an SME as ineligible if:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"it has previously received exhibitor support from the UK Tradeshow Programme\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"there is evidence it is planning to close all operations or transfer its assets overseas or offshore jobs\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"the company or individual has a business record or business practices or products which is/are likely to cause offence in the overseas market and/or embarrass the UK government (for example, on corporate social responsibility grounds)\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"it is offering a product which is illegal to produce or sell in the UK or in the target market\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"the company’s product would breach export controls if sold abroad\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}}]}"
                      ]
                   }
                ],
                "id":"1",
                "status":"COMPLETED"
             },
             {
                "questions":[
                   {
                      "id":"grantSummaryTab",
                      "seen":true,
                      "response":null,
                      "multiResponse":[
                        "<h2 class=\"govuk-heading\">Summary</h2><div class=\"grant_grant__breakword__72hvW\"><p>UK businesses that are currently exporting can apply for support to:</p><ul><li><p>exhibit at or attend approved overseas trade shows and conferences</p></li><li><p>potentially receive grants to cover some costs</p></li></ul><p>UK businesses can also apply for support if they&rsquo;re thinking about exporting but are not currently doing so.</p><p>Attending or exhibiting at overseas trade shows can help you gain essential market knowledge and increase your:</p><ul><li><p>company&rsquo;s brand awareness amongst overseas buyers</p></li><li><p>business sales by securing new customers</p></li></ul><h3><strong>Accessing support to exhibit at overseas trade shows</strong></h3><p>The support available through the programme varies.</p><p>All successful applicants will receive training on successfully exhibiting at:</p><ul><li><p>trade shows in general</p></li><li><p>the specific approved event(s) that they have applied for</p></li></ul><p>Some businesses may also receive a grant of either &pound;2,000 or &pound;4,000 as financial support to cover:</p><ul><li><p>exhibition space costs</p></li><li><p>stand costs (including design, construction and stand dressing)</p></li><li><p>conference fees, costs of preparing conference promotional material (where appropriate)</p></li></ul><h3><strong>Accessing support to attend overseas trade shows</strong></h3><p>The support available through the programme varies.</p><p>All eligible applicants will receive training on how to successfully exhibit at overseas trade shows.</p><p>Some businesses will also receive:</p><ul><li><p>a bespoke pre-event briefing</p></li><li><p>a curated tour of an overseas trade show</p></li><li><p>contributions towards costs of show entry, travel or accommodation of &pound;200 for European shows, or &pound;500 for shows outside Europe</p></li></ul><p>Availability of these additional services is limited and will be allocated in order of successful applications received.</p><h3><strong>How to apply</strong></h3><p>Applications must be made at least 6 weeks before the start of the event.</p><p>To apply:</p><ol><li><p>Go to the&nbsp;<a href=\"https://www.events.great.gov.uk/UKTPEvents/\"><u>calendar of supported events</u></a><u>.</u></p></li><li><p>Select the event of interest.</p></li><li><p>Complete an application form online.</p></li></ol></div>",
                        "{\"nodeType\":\"document\",\"data\":{},\"content\":[{\"nodeType\":\"heading-2\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Summary\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"UK businesses that are currently exporting can apply for support to:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"exhibit at or attend approved overseas trade shows and conferences\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"potentially receive grants to cover some costs\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"UK businesses can also apply for support if they’re thinking about exporting but are not currently doing so.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Attending or exhibiting at overseas trade shows can help you gain essential market knowledge and increase your:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"company’s brand awareness amongst overseas buyers\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"business sales by securing new customers\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"heading-3\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Accessing support to exhibit at overseas trade shows\",\"marks\":[{\"type\":\"bold\"}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"The support available through the programme varies.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"All successful applicants will receive training on successfully exhibiting at:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"trade shows in general\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"the specific approved event(s) that they have applied for\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Some businesses may also receive a grant of either £2,000 or £4,000 as financial support to cover:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"exhibition space costs\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"stand costs (including design, construction and stand dressing)\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"conference fees, costs of preparing conference promotional material (where appropriate)\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"heading-3\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Accessing support to attend overseas trade shows\",\"marks\":[{\"type\":\"bold\"}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"The support available through the programme varies.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"All eligible applicants will receive training on how to successfully exhibit at overseas trade shows.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Some businesses will also receive:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"a bespoke pre-event briefing\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"a curated tour of an overseas trade show\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"contributions towards costs of show entry, travel or accommodation of £200 for European shows, or £500 for shows outside Europe\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Availability of these additional services is limited and will be allocated in order of successful applications received.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"heading-3\",\"content\":[{\"nodeType\":\"text\",\"value\":\"How to apply\",\"marks\":[{\"type\":\"bold\"}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Applications must be made at least 6 weeks before the start of the event.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"To apply:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"ordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Go to the \",\"marks\":[],\"data\":{}},{\"nodeType\":\"hyperlink\",\"data\":{\"uri\":\"https://www.events.great.gov.uk/UKTPEvents/\"},\"content\":[{\"nodeType\":\"text\",\"value\":\"calendar of supported events\",\"marks\":[],\"data\":{}}]},{\"nodeType\":\"text\",\"value\":\".\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Select the event of interest.\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Complete an application form online.\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}}]}"
                      ]
                   }
                ],
                "id":"2",
                "status":"COMPLETED"
             },
             {
                "questions":[
                   {
                      "id":"grantDatesTab",
                      "seen":true,
                      "response":null,
                      "multiResponse":[
                         "<p>Grant dates...</p>",
                         "{\"nodeType\":\"document\",\"data\":{},\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Grant dates...\",\"marks\":[],\"data\":{}}],\"data\":{}}]}"
                      ]
                   }
                ],
                "id":"3",
                "status":"COMPLETED"
             },
             {
                "questions":[
                   {
                      "id":"grantObjectivesTab",
                      "seen":true,
                      "response":null,
                      "multiResponse":[
                         "<p>Grant objectives...</p>",
                         "{\"nodeType\":\"document\",\"data\":{},\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Grant objectives...\",\"marks\":[],\"data\":{}}],\"data\":{}}]}"
                      ]
                   }
                ],
                "id":"4",
                "status":"COMPLETED"
             },
             {
                "questions":[
                   {
                      "id":"grantApplyTab",
                      "seen":true,
                      "response":null,
                      "multiResponse":[
                         "<p>How to apply...</p>",
                         "{\"nodeType\":\"document\",\"data\":{},\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"How to apply...\",\"marks\":[],\"data\":{}}],\"data\":{}}]}"
                      ]
                   }
                ],
                "id":"5",
                "status":"COMPLETED"
             },
             {
                "questions":[
                   {
                      "id":"grantSupportingInfoTab",
                      "seen":true,
                      "response":null,
                      "multiResponse":[
                         "<p>Supporting info...</p>",
                         "{\"nodeType\":\"document\",\"data\":{},\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Supporting info...\",\"marks\":[],\"data\":{}}],\"data\":{}}]}"
                      ]
                   }
                ],
                "id":"6",
                "status":"COMPLETED"
             }
          ],
          "id":"furtherInformation",
          "status":"COMPLETED"
       }
    ]
 }', 'DRAFT', 1, 1, 1, 5);

INSERT INTO public.grant_advert (grant_advert_id, contentful_entry_id, contentful_slug, created, grant_advert_name, last_updated, response, status, version, created_by, last_updated_by, scheme_id)
VALUES ('fa8f4b1d-d090-4ff6-97be-ccabd3b1d87e', NULL, NULL, '2022-11-28 14:36:21.628791', 'UK Tradeshow Programme (UKTP) Future - Advert', '2022-11-28 14:36:21.628793',
'{
 "sections":[
    {
       "pages":[
          {
             "questions":[
                {
                   "id":"grantShortDescription",
                   "seen":true,
                   "response":"Businesses exporting or thinking of exporting from the UK can attend UK Tradeshow Programmes selection of supported overseas tradeshows and conferences, and potentially receive grants to offset some costs.",
                   "multiResponse":null
                }
             ],
             "id":"1",
             "status":"COMPLETED"
          },
          {
             "questions":[
                {
                   "id":"grantLocation",
                   "seen":true,
                   "response":null,
                   "multiResponse":[
                      "National"
                   ]
                }
             ],
             "id":"2",
             "status":"COMPLETED"
          },
          {
             "questions":[
                {
                   "id":"grantFunder",
                   "seen":true,
                   "response":"The UK Tradeshow Programme",
                   "multiResponse":null
                }
             ],
             "id":"3",
             "status":"COMPLETED"
          },
          {
             "questions":[
                {
                   "id":"grantApplicantType",
                   "seen":true,
                   "response":null,
                   "multiResponse":[
                       "Public Sector",
                       "Non-profit",
                       "Private Sector"
                   ]
                }
             ],
             "id":"4",
             "status":"COMPLETED"
          }
       ],
       "id":"grantDetails",
       "status":"COMPLETED"
    },
    {
       "pages":[
          {
             "questions":[
                {
                   "id":"grantApplicationOpenDate",
                   "seen":true,
                   "response":null,
                   "multiResponse":[
                     "31",
                     "3",
                     "2122",
                     "00",
                     "01"
                   ]
                },
                {
                   "id":"grantApplicationCloseDate",
                   "seen":true,
                   "response":null,
                   "multiResponse":[
                     "1",
                     "6",
                     "2122",
                     "23",
                     "59"
                   ]
               }
             ],
             "id":"1",
             "status":"COMPLETED"
          }
       ],
       "id":"applicationDates",
       "status":"COMPLETED"
    },
   {
      "pages":[
         {
            "questions":[
               {
                  "id":"grantTotalAwardAmount",
                  "seen":true,
                  "response":"5000000",
                  "multiResponse":null
               },
               {
                  "id":"grantMaximumAward",
                  "seen":true,
                  "response":"1000000",
                  "multiResponse":null
               },
               {
                  "id":"grantMinimumAward",
                  "seen":true,
                  "response":"100000",
                  "multiResponse":null
               }
            ],
            "id":"1",
            "status":"COMPLETED"
         }
      ],
      "id":"awardAmounts",
      "status":"COMPLETED"
   },
    {
       "pages":[
          {
             "questions":[
                {
                   "id":"grantWebpageUrl",
                   "seen":true,
                   "response":"https://www.gov.uk/guidance/uk-tradeshow-programme",
                   "multiResponse":null
                }
             ],
             "id":"1",
             "status":"COMPLETED"
          }
       ],
       "id":"howToApply",
       "status":"COMPLETED"
    },
    {
       "pages":[
          {
             "questions":[
                {
                   "id":"grantEligibilityTab",
                   "seen":true,
                   "response":null,
                   "multiResponse": [
                       "<h2 class=\"govuk-heading\">Eligibility</h2><div class=\"grant_grant__breakword__72hvW\"><h3><strong>Programme eligibility criteria for exhibitors</strong></h3><p>You must not have previously received support to exhibit from the UK Tradeshow Programme.</p><p>You must:</p><ul><li><p>be exhibiting for the first time or wishing to venture into new markets</p></li><li><p>be turning over annually between &pound;250,000 to &pound;5 million</p></li><li><p>not have committed to attending the event before applying for support</p></li><li><p>be actively investigating export opportunities for your own business:</p><ul><li><p>having not previously exported, or</p></li><li><p>having previously exported and wishing to grow exports in new markets</p></li></ul></li></ul><h3><strong>Programme eligibility criteria for attendees</strong></h3><p>You must not have previously received any support from the UK Tradeshow Programme.</p><p>You must:</p><ul><li><p>be attending a show for the first time or wishing to venture into new markets</p></li><li><p>be turning over annually between &pound;85,000 to &pound;250,000</p></li><li><p>not have committed to attending the event before applying for support</p></li><li><p>be actively investigating export opportunities for your own business:</p><ul><li><p>having not previously exported, or</p></li><li><p>having previously exported and wishing to grow exports in new markets</p></li></ul></li></ul><h3><strong>Common eligibility criteria</strong></h3><p>Access to support from the programme is limited.</p><p>Organisations can apply for support from each part of the programme once only, subject to programme eligibility criteria.</p><p>You must be UK VAT-registered to apply.</p><h3><strong>General eligibility criteria</strong></h3><p>Eligible businesses must be small to medium-sized enterprises (SMEs) (fewer than 250 employees), based in the UK (excluding Isle of Man or the Channel Islands), and either:</p><ul><li><p>sell products or services which substantially originate from the UK, or</p></li><li><p>add significant value to a product or service of non-UK origin</p></li></ul><p>You may be required to evidence that you meet the criteria.</p><h3><strong>Financial support eligibility criteria</strong></h3><p>In addition to the above conditions, for financial support you must:</p><ul><li><p>not receive any contributions, from another programme or body, towards the eligible costs covered by the UK Tradeshow Programme</p></li><li><p>not have received government aid in excess of the limits below:</p><ul><li><p>if you are a business in Great Britain and state aid rules under the Northern Ireland Protocol do not apply: 325,000 SDR (approximately &pound;332,000)</p></li><li><p>if you are a business within the scope of Northern Ireland Protocol State aid rules: &euro;200,000, except for businesses in the sectors below</p></li></ul></li></ul><p>Some sectors have a lower limit for government aid. This is due to the Northern Ireland Protocol&rsquo;s state aid rules.</p><p>These limits and sectors are:</p><ul><li><p>&euro;20,000 if you are active in the fishery and aquaculture sector</p></li><li><p>&euro;30,000 if you are active in the primary production of agriculture products</p></li></ul><p>UK Tradeshow Programme financial support is likely to constitute state aid or a subsidy to recipients.</p><p>For more information, please refer to&nbsp;guidance&nbsp;on the&nbsp;<a href=\"https://www.gov.uk/government/publications/complying-with-the-uks-international-obligations-on-subsidy-control-guidance-for-public-authorities\"><u>UK&rsquo;s international subsidy control commitments</u></a><u>.</u></p><h3><strong>Ineligibility for support</strong></h3><p>DIT will regard an SME as ineligible if:</p><ul><li><p>it has previously received exhibitor support from the UK Tradeshow Programme</p></li><li><p>there is evidence it is planning to close all operations or transfer its assets overseas or offshore jobs</p></li><li><p>the company or individual has a business record or business practices or products which is/are likely to cause offence in the overseas market and/or embarrass the UK government (for example, on corporate social responsibility grounds)</p></li><li><p>it is offering a product which is illegal to produce or sell in the UK or in the target market</p></li><li><p>the company&rsquo;s product would breach export controls if sold abroad</p></li></ul></div>",
                       "{\"nodeType\":\"document\",\"data\":{},\"content\":[{\"nodeType\":\"heading-2\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Eligibility\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"heading-3\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Programme eligibility criteria for exhibitors\",\"marks\":[{\"type\":\"bold\"}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"You must not have previously received support to exhibit from the UK Tradeshow Programme.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"You must:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"be exhibiting for the first time or wishing to venture into new markets\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"be turning over annually between £250,000 to £5 million\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"not have committed to attending the event before applying for support\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"be actively investigating export opportunities for your own business:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"having not previously exported, or\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"having previously exported and wishing to grow exports in new markets\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"heading-3\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Programme eligibility criteria for attendees\",\"marks\":[{\"type\":\"bold\"}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"You must not have previously received any support from the UK Tradeshow Programme.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"You must:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"be attending a show for the first time or wishing to venture into new markets\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"be turning over annually between £85,000 to £250,000\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"not have committed to attending the event before applying for support\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"be actively investigating export opportunities for your own business:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"having not previously exported, or\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"having previously exported and wishing to grow exports in new markets\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"heading-3\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Common eligibility criteria\",\"marks\":[{\"type\":\"bold\"}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Access to support from the programme is limited.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Organisations can apply for support from each part of the programme once only, subject to programme eligibility criteria.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"You must be UK VAT-registered to apply.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"heading-3\",\"content\":[{\"nodeType\":\"text\",\"value\":\"General eligibility criteria\",\"marks\":[{\"type\":\"bold\"}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Eligible businesses must be small to medium-sized enterprises (SMEs) (fewer than 250 employees), based in the UK (excluding Isle of Man or the Channel Islands), and either:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"sell products or services which substantially originate from the UK, or\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"add significant value to a product or service of non-UK origin\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"You may be required to evidence that you meet the criteria.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"heading-3\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Financial support eligibility criteria\",\"marks\":[{\"type\":\"bold\"}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"In addition to the above conditions, for financial support you must:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"not receive any contributions, from another programme or body, towards the eligible costs covered by the UK Tradeshow Programme\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"not have received government aid in excess of the limits below:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"if you are a business in Great Britain and state aid rules under the Northern Ireland Protocol do not apply: 325,000 SDR (approximately £332,000)\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"if you are a business within the scope of Northern Ireland Protocol State aid rules: €200,000, except for businesses in the sectors below\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Some sectors have a lower limit for government aid. This is due to the Northern Ireland Protocol’s state aid rules.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"These limits and sectors are:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"€20,000 if you are active in the fishery and aquaculture sector\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"€30,000 if you are active in the primary production of agriculture products\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"UK Tradeshow Programme financial support is likely to constitute state aid or a subsidy to recipients.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"For more information, please refer to guidance on the \",\"marks\":[],\"data\":{}},{\"nodeType\":\"hyperlink\",\"data\":{\"uri\":\"https://www.gov.uk/government/publications/complying-with-the-uks-international-obligations-on-subsidy-control-guidance-for-public-authorities\"},\"content\":[{\"nodeType\":\"text\",\"value\":\"UK’s international subsidy control commitments\",\"marks\":[],\"data\":{}}]},{\"nodeType\":\"text\",\"value\":\".\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"heading-3\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Ineligibility for support\",\"marks\":[{\"type\":\"bold\"}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"DIT will regard an SME as ineligible if:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"it has previously received exhibitor support from the UK Tradeshow Programme\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"there is evidence it is planning to close all operations or transfer its assets overseas or offshore jobs\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"the company or individual has a business record or business practices or products which is/are likely to cause offence in the overseas market and/or embarrass the UK government (for example, on corporate social responsibility grounds)\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"it is offering a product which is illegal to produce or sell in the UK or in the target market\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"the company’s product would breach export controls if sold abroad\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}}]}"
                   ]
                }
             ],
             "id":"1",
             "status":"COMPLETED"
          },
          {
             "questions":[
                {
                   "id":"grantSummaryTab",
                   "seen":true,
                   "response":null,
                   "multiResponse":[
                     "<h2 class=\"govuk-heading\">Summary</h2><div class=\"grant_grant__breakword__72hvW\"><p>UK businesses that are currently exporting can apply for support to:</p><ul><li><p>exhibit at or attend approved overseas trade shows and conferences</p></li><li><p>potentially receive grants to cover some costs</p></li></ul><p>UK businesses can also apply for support if they&rsquo;re thinking about exporting but are not currently doing so.</p><p>Attending or exhibiting at overseas trade shows can help you gain essential market knowledge and increase your:</p><ul><li><p>company&rsquo;s brand awareness amongst overseas buyers</p></li><li><p>business sales by securing new customers</p></li></ul><h3><strong>Accessing support to exhibit at overseas trade shows</strong></h3><p>The support available through the programme varies.</p><p>All successful applicants will receive training on successfully exhibiting at:</p><ul><li><p>trade shows in general</p></li><li><p>the specific approved event(s) that they have applied for</p></li></ul><p>Some businesses may also receive a grant of either &pound;2,000 or &pound;4,000 as financial support to cover:</p><ul><li><p>exhibition space costs</p></li><li><p>stand costs (including design, construction and stand dressing)</p></li><li><p>conference fees, costs of preparing conference promotional material (where appropriate)</p></li></ul><h3><strong>Accessing support to attend overseas trade shows</strong></h3><p>The support available through the programme varies.</p><p>All eligible applicants will receive training on how to successfully exhibit at overseas trade shows.</p><p>Some businesses will also receive:</p><ul><li><p>a bespoke pre-event briefing</p></li><li><p>a curated tour of an overseas trade show</p></li><li><p>contributions towards costs of show entry, travel or accommodation of &pound;200 for European shows, or &pound;500 for shows outside Europe</p></li></ul><p>Availability of these additional services is limited and will be allocated in order of successful applications received.</p><h3><strong>How to apply</strong></h3><p>Applications must be made at least 6 weeks before the start of the event.</p><p>To apply:</p><ol><li><p>Go to the&nbsp;<a href=\"https://www.events.great.gov.uk/UKTPEvents/\"><u>calendar of supported events</u></a><u>.</u></p></li><li><p>Select the event of interest.</p></li><li><p>Complete an application form online.</p></li></ol></div>",
                     "{\"nodeType\":\"document\",\"data\":{},\"content\":[{\"nodeType\":\"heading-2\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Summary\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"UK businesses that are currently exporting can apply for support to:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"exhibit at or attend approved overseas trade shows and conferences\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"potentially receive grants to cover some costs\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"UK businesses can also apply for support if they’re thinking about exporting but are not currently doing so.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Attending or exhibiting at overseas trade shows can help you gain essential market knowledge and increase your:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"company’s brand awareness amongst overseas buyers\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"business sales by securing new customers\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"heading-3\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Accessing support to exhibit at overseas trade shows\",\"marks\":[{\"type\":\"bold\"}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"The support available through the programme varies.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"All successful applicants will receive training on successfully exhibiting at:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"trade shows in general\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"the specific approved event(s) that they have applied for\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Some businesses may also receive a grant of either £2,000 or £4,000 as financial support to cover:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"exhibition space costs\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"stand costs (including design, construction and stand dressing)\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"conference fees, costs of preparing conference promotional material (where appropriate)\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"heading-3\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Accessing support to attend overseas trade shows\",\"marks\":[{\"type\":\"bold\"}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"The support available through the programme varies.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"All eligible applicants will receive training on how to successfully exhibit at overseas trade shows.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Some businesses will also receive:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"unordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"a bespoke pre-event briefing\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"a curated tour of an overseas trade show\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"contributions towards costs of show entry, travel or accommodation of £200 for European shows, or £500 for shows outside Europe\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Availability of these additional services is limited and will be allocated in order of successful applications received.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"heading-3\",\"content\":[{\"nodeType\":\"text\",\"value\":\"How to apply\",\"marks\":[{\"type\":\"bold\"}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Applications must be made at least 6 weeks before the start of the event.\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"To apply:\",\"marks\":[],\"data\":{}}],\"data\":{}},{\"nodeType\":\"ordered-list\",\"content\":[{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Go to the \",\"marks\":[],\"data\":{}},{\"nodeType\":\"hyperlink\",\"data\":{\"uri\":\"https://www.events.great.gov.uk/UKTPEvents/\"},\"content\":[{\"nodeType\":\"text\",\"value\":\"calendar of supported events\",\"marks\":[],\"data\":{}}]},{\"nodeType\":\"text\",\"value\":\".\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Select the event of interest.\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}},{\"nodeType\":\"list-item\",\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Complete an application form online.\",\"marks\":[],\"data\":{}}],\"data\":{}}],\"data\":{}}],\"data\":{}}]}"
                   ]
                }
             ],
             "id":"2",
             "status":"COMPLETED"
          },
          {
             "questions":[
                {
                   "id":"grantDatesTab",
                   "seen":true,
                   "response":null,
                   "multiResponse":[
                      "<p>Grant dates...</p>",
                      "{\"nodeType\":\"document\",\"data\":{},\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Grant dates...\",\"marks\":[],\"data\":{}}],\"data\":{}}]}"
                   ]
                }
             ],
             "id":"3",
             "status":"COMPLETED"
          },
          {
             "questions":[
                {
                   "id":"grantObjectivesTab",
                   "seen":true,
                   "response":null,
                   "multiResponse":[
                      "<p>Grant objectives...</p>",
                      "{\"nodeType\":\"document\",\"data\":{},\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Grant objectives...\",\"marks\":[],\"data\":{}}],\"data\":{}}]}"
                   ]
                }
             ],
             "id":"4",
             "status":"COMPLETED"
          },
          {
             "questions":[
                {
                   "id":"grantApplyTab",
                   "seen":true,
                   "response":null,
                   "multiResponse":[
                      "<p>How to apply...</p>",
                      "{\"nodeType\":\"document\",\"data\":{},\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"How to apply...\",\"marks\":[],\"data\":{}}],\"data\":{}}]}"
                   ]
                }
             ],
             "id":"5",
             "status":"COMPLETED"
          },
          {
             "questions":[
                {
                   "id":"grantSupportingInfoTab",
                   "seen":true,
                   "response":null,
                   "multiResponse":[
                      "<p>Supporting info...</p>",
                      "{\"nodeType\":\"document\",\"data\":{},\"content\":[{\"nodeType\":\"paragraph\",\"content\":[{\"nodeType\":\"text\",\"value\":\"Supporting info...\",\"marks\":[],\"data\":{}}],\"data\":{}}]}"
                   ]
                }
             ],
             "id":"6",
             "status":"COMPLETED"
          }
       ],
       "id":"furtherInformation",
       "status":"COMPLETED"
    }
 ]
}', 'DRAFT', 1, 1, 1, 6);