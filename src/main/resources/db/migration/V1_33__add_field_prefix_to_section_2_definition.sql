UPDATE PUBLIC.GAP_DEFINITION SET DEFINITION ['sections'][1]['pages'][0]['questions'][1] =
'
        {
          "id": "grantMaximumAward",
          "title": "Enter the maximum amount someone can apply for",
          "displayText": "",
          "fieldPrefix": "£",
          "hintText": "This is the highest amount you will award per application.\nType a numerical figure e.g £50000.00.",
          "suffixText": "",
          "options": [],
          "validation": {
            "mandatory": true
          },
          "responseType": "INTEGER"
        }
'
WHERE GAP_DEFINITION_ID = 1;