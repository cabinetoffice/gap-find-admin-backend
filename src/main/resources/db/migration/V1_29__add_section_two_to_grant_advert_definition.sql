UPDATE PUBLIC.GAP_DEFINITION SET DEFINITION ['sections'][1] =
'{
  "id": "awardAmounts",
  "title": "2. Award Amounts",
  "pages": [
    {
      "id": 1,
      "title": "How much funding is available?",
      "questions": [
        {
          "id": "grantTotalAwardAmount",
          "title": "Enter the total amount of the grant",
          "displayText": "",
          "fieldPrefix": "£",
          "hintText": "This is the total pot size for this grant.\nType a numerical figure e.g £50000.00.",
          "suffixText": "",
          "options": [],
          "validation": {
            "mandatory": true
          },
          "responseType": "INTEGER"
        },
        {
          "id": "grantMaximumAward",
          "title": "Enter the maximum amount someone can apply for",
          "displayText": "",
          "hintText": "This is the highest amount you will award per application.\nType a numerical figure e.g £50000.00.",
          "suffixText": "",
          "options": [],
          "validation": {
            "mandatory": true
          },
          "responseType": "INTEGER"
        },
        {
          "id": "grantMinimumAward",
          "title": "Enter the minimum amount someone can apply for",
          "displayText": "",
          "fieldPrefix": "£",
          "hintText": "This is the lowest amount you will award per application.\nType a numerical figure e.g £50000.00.",
          "suffixText": "",
          "options": [],
          "validation": {
            "mandatory": true
          },
          "responseType": "INTEGER"
        }
      ]
    }
  ]
}'
WHERE GAP_DEFINITION_ID = 1;