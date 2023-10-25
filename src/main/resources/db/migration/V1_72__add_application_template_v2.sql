INSERT INTO template_grant_application(template_id, definition)
	VALUES (2, '{
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
                      "sectionId": "ORGANISATION_DETAILS",
                      "sectionTitle": "Your Organisation",
                      "sectionStatus": "INCOMPLETE",
                      "questions": []
                    },
                    {
                      "sectionId": "FUNDING_DETAILS",
                      "sectionTitle": "Funding",
                      "sectionStatus": "INCOMPLETE",
                      "questions": []
                    }
                  ]
                }'
);