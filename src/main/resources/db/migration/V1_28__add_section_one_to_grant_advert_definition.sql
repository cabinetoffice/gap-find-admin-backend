UPDATE public.gap_definition SET definition ['sections'][0] = '{
  "id": "grantDetails",
  "title": "1. Grant Details",
  "pages": [
    {
      "id": 1,
      "title": "Short description",
      "questions": [
        {
          "id": "grantShortDescription",
          "title": "Add a short description of the grant",
          "displayText": "",
          "hintText": "Write a short overview of what the grant is for. This description should be short as it appear on the results page. \n\nYou will be able to add a longer description later.\n\nExample one: \n\nDisabled Facilities Grants can help meet the cost of making changes to your home so you, or someone you live with, can be safe and independent. People of all ages and tenures can apply to their local council for this grant.\n\nExample two:\n\nThis grant provides electric vehicle drivers who rent or live in flats with support towards the upfront costs of purchasing and installing an electric vehicle (EV) charge point.",
          "suffixText": "",
          "options": [],
          "validation": {
            "mandatory": true,
            "maxLength": 800
          },
          "responseType": "LONG_TEXT"
        }
      ]
    },
    {
      "id": 2,
      "title": "Location",
      "questions": [
        {
          "id": "grantLocation",
          "title": "Where is the grant available?",
          "displayText": "",
          "hintText": "Select all that apply:",
          "suffixText": "",
          "options": [
            "National",
            "England",
            "Wales",
            "Scotland",
            "Northern Ireland",
            "North East England",
            "North West England",
            "South East England",
            "South West England",
            "Midlands"
          ],
          "validation": {
            "mandatory": true
          },
          "responseType": "LIST"
        }
      ]
    },
    {
      "id": 3,
      "title": "Funding organisation",
      "questions": [
        {
          "id": "grantFunder",
          "title": "Which organisation is funding this grant?",
          "displayText": "",
          "hintText": "Enter the full name of your organisation. For example, ‘The Department for Culture, Media & Sport’ not ‘DCMS’.",
          "suffixText": "",
          "options": [],
          "validation": {
            "mandatory": true,
            "maxLength": 256
          },
          "responseType": "SHORT_TEXT"
        }
      ]
    },
    {
      "id": 4,
      "title": "Who can apply",
      "questions": [
        {
          "id": "grantApplicantType",
          "title": "Who can apply for this grant?",
          "displayText": "",
          "hintText": "Select one or more options.",
          "suffixText": "",
          "options": [
            "Personal / Individual",
            "Public Sector",
            "Non-profit",
            "Private Sector"
          ],
          "validation": {
            "mandatory": true
          },
          "responseType": "LIST"
        }
      ]
    }
]}'

WHERE gap_definition_id  = 1;