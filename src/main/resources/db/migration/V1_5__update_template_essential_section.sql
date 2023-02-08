UPDATE template_grant_application SET definition = '{
     "sections": [
       {
         "sectionId": "ELIGIBILITY",
         "sectionTitle": "Eligibility",
         "sectionStatus": "INCOMPLETE",
         "questions": [
           {
             "questionId": "ELIGIBILITY",
             "fieldTitle": "Eligibility Statement",
             "displayText": "",
             "questionSuffix": "Does your organisation meet the eligibility criteria?",
             "responseType": "YesNo",
             "validation": {
               "mandatory": true
             }
           }
         ]
       },

       {
         "sectionId": "ESSENTIAL",
         "sectionTitle": "Essential Information",
         "sectionStatus": "INCOMPLETE",
         "questions": [
           {
             "questionId": "APPLICANT_TYPE",
             "profileField": "ORG_TYPE",
             "fieldTitle": "Choose your organisation type",
             "hintText": "Choose the option that best describes your organisation",
             "adminSummary": "organisation type (e.g. limited company)",
             "responseType": "SingleSelection",
             "validation": {
               "mandatory": true
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
             "questionId": "APPLICANT_ORG_NAME",
             "profileField": "ORG_NAME",
             "fieldTitle": "Enter the name of your organisation",
             "hintText": "This is the official name of your organisation. It could be the name that is registered with Companies House or the Charities Commission",
             "adminSummary": "organisation legal name",
             "responseType": "ShortAnswer",
             "validation": {
               "mandatory": true,
               "minLength": 5,
               "maxLength": 100
             }
           },
           {
             "questionId": "APPLICANT_ORG_ADDRESS",
             "profileField": "ORG_ADDRESS",
             "fieldTitle": "Enter your organisation''s address",
             "adminSummary": "registered address",
             "responseType": "AddressInput",
             "validation": {
               "mandatory": true
             }
           },
           {
             "questionId": "APPLICANT_ORG_COMPANIES_HOUSE",
             "profileField": "ORG_COMPANIES_HOUSE",
             "fieldTitle": "Please supply the Companies House number for your organisation - if applicable",
             "hintText": "Funding organisation might use this to identify your organisation when you apply for a grant. It might also be used to check your organisation is legitimate.",
             "adminSummary": "Companies House number (if applicable)",
             "responseType": "ShortAnswer",
             "validation": {
               "mandatory": false,
               "minLength": 5,
               "maxLength": 100
             }
           },
           {
             "questionId": "APPLICANT_AMOUNT",
             "fieldTitle": "How much does your organisation require as a grant?",
             "hintText": "Please enter whole pounds only",
             "adminSummary": "amount of funding required",
             "fieldPrefix": "£",
             "responseType": "Numeric",
             "validation": {
               "mandatory": true,
               "greaterThanZero": true
             }
           }
         ]
       }
     ]
   }'
WHERE template_id = 1;