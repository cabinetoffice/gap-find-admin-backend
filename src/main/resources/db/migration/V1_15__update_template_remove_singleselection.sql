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
      "sectionTitle": "Required checks",
      "sectionStatus": "INCOMPLETE",
      "questions": [
        {
          "questionId": "APPLICANT_ORG_NAME",
          "profileField": "ORG_NAME",
          "fieldTitle": "Enter the name of your organisation",
          "hintText": "This is the official name of your organisation. It could be the name that is registered with Companies House or the Charities Commission",
          "adminSummary": "organisation legal name",
          "responseType": "ShortAnswer",
          "validation": {
            "mandatory": true,
            "minLength": 2,
            "maxLength": 250
          }
        },
        {
          "questionId": "APPLICANT_TYPE",
          "profileField": "ORG_TYPE",
          "fieldTitle": "Choose your organisation type",
          "hintText": "Choose the option that best describes your organisation",
          "adminSummary": "organisation type (e.g. limited company)",
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
          ]
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
          "questionId": "APPLICANT_ORG_CHARITY_NUMBER",
          "profileField": "ORG_CHARITY_NUMBER",
          "fieldTitle": "Please supply the Charity Commission number for your organisation - if applicable",
          "hintText": "Funding organisation might use this to identify your organisation when you apply for a grant. It might also be used to check your organisation is legitimate.",
          "adminSummary": "Charity Commission number (if applicable)",
          "responseType": "ShortAnswer",
          "validation": {
            "mandatory": false,
            "minLength": 2,
            "maxLength": 15,
            "validInput": "alphanumeric-nospace"
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
            "minLength": 2,
            "maxLength": 8,
            "validInput": "alphanumeric-nospace"
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
        },
        {
          "questionId": "BENEFITIARY_LOCATION",
          "fieldTitle": "Where will this funding be spent?",
          "hintText": "Select the location where the grant funding will be spent. You can choose more than one, if it is being spent in more than one location.\\n\\nSelect all that apply:",
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
          ]
        }
      ]
    }
  ]
}'
WHERE template_id = 1;