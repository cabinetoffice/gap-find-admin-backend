UPDATE public.gap_definition SET definition ['sections'][2] = '{
  "id": "applicationDates",
  "title": "3. Application dates",
  "pages": [
    {
      "id": 1,
      "title": "Enter the opening and closing dates of your application",
      "questions": [
        {
          "id": "grantApplicationOpenDate",
          "title": "Opening date",
          "displayText": "",
          "hintText": "For example, 31 3 2023",
          "suffixText": "",
          "options": [],
          "validation": {
            "mandatory": true
          },
          "responseType": "DATE_TIME"
        },
				{
          "id": "grantApplicationCloseDate",
          "title": "Closing date",
          "displayText": "",
          "hintText": "For example, 31 3 2023",
          "suffixText": "",
          "options": [],
          "validation": {
            "mandatory": true
          },
          "responseType": "DATE_TIME"
        }
      ]
    }
]}'

WHERE gap_definition_id  = 1;