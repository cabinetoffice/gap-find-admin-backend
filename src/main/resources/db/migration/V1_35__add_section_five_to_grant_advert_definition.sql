UPDATE public.gap_definition SET definition ['sections'][4] = '{
  "id": "furtherInformation",
  "title": "5. Further information",
  "pages": [
    {
      "id": 1,
      "title": "Eligibility information",
      "questions": [
        {
          "id": "grantEligibilityTab",
          "title": "Add eligibility information for your grant",
          "displayText": "",
          "hintText": "Add information to help the applicant know if they are eligible to apply for this grant or not. \n\nHaving a clear eligibility criteria means time and money are not spent processing applications from organisations that are not eligible.",
          "suffixText": "",
          "options": [],
          "validation": {
            "mandatory": true,
            "minLength": 1
          },
          "responseType": "RICH_TEXT"
        }
      ]
    },
    {
      "id": 2,
      "title": "Long description",
      "questions": [
        {
          "id": "grantSummaryTab",
          "title": "Add a long description of your grant",
          "displayText": "",
          "hintText": "In this section you can add a longer description of the grant.",
          "suffixText": "",
          "options": [],
          "validation": {
            "mandatory": true,
            "minLength": 1
          },
          "responseType": "RICH_TEXT"
        }
      ]
    },
    {
      "id": 3,
      "title": "Relevant dates",
      "questions": [
        {
          "id": "grantDatesTab",
          "title": "Add details about any relevant dates (optional)",
          "displayText": "",
          "hintText": "You might want to include information about when the applicant will know if they have been successful or the date the funding must be spent by.",
          "suffixText": "",
          "options": [],
          "validation": {
            "mandatory": false
          },
          "responseType": "RICH_TEXT"
        }
      ]
    },
     {
      "id": 4,
      "title": "Scheme objectives",
      "questions": [
        {
          "id": "grantObjectivesTab",
          "title": "Add details about the objectives of your grant",
          "displayText": "",
          "hintText": "This should include information about what the grant is trying to achieve.",
          "suffixText": "",
          "options": [],
          "validation": {
            "mandatory": true,
            "minLength": 1
          },
          "responseType": "RICH_TEXT"
        }
      ]
    },
		{
      "id": 5,
      "title": "How to apply",
      "questions": [
        {
          "id": "grantApplyTab",
          "title": "Add information about how to apply for your grant",
          "displayText": "",
          "hintText": "Add any additional information about how the applicant will need to apply. This section is useful if you need to give a link to a website or a document they need to email to you.",
          "suffixText": "",
          "options": [],
          "validation": {
            "mandatory": true,
            "minLength": 1
          },
          "responseType": "RICH_TEXT"
        }
      ]
    },
		{
      "id": 6,
      "title": "Supporting information",
      "questions": [
        {
          "id": "grantSupportingInfoTab",
          "title": "Add links to any supporting information (optional)",
          "displayText": "",
          "hintText": "You might want to link to documents or websites that will give applicants more information about your grant.",
          "suffixText": "",
          "options": [],
          "validation": {
            "mandatory": false
          },
          "responseType": "RICH_TEXT"
        }
      ]
    }
]}'

WHERE gap_definition_id  = 1;