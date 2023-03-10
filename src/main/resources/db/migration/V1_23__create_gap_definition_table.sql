CREATE TABLE gap_definition (
  gap_definition_id INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
   name VARCHAR(255) NOT NULL,
   version INTEGER NOT NULL,
   definition JSON,
   CONSTRAINT pk_gap_definition PRIMARY KEY (gap_definition_id)
);

INSERT INTO gap_definition(gap_definition_id, name, version, definition)
	VALUES (1,'Grant Advert Definition',1,'{
                                             "sections": [
                                               {
                                                 "id": "grantDetailsId",
                                                 "title": "1. Grant Details",
                                                 "pages": [
                                                   {
                                                     "id": "shortDescriptionPageId",
                                                     "title": "Short Description",
                                                     "questions": []
                                                   },
                                                   { "id": "locationPageId", "title": "Location", "questions": [] },
                                                   {
                                                     "id": "fundinOrganisationPageId",
                                                     "title": "Funding organisation",
                                                     "questions": []
                                                   },
                                                   {
                                                     "id": "WhoCanApplyPageId",
                                                     "title": "Who can apply",
                                                     "questions": []
                                                   },
                                                   { "id": "typeOfGrantPageId", "title": "Type Of Grant", "questions": [] }
                                                 ]
                                               },
                                               {
                                                 "id": "awardAmountId",
                                                 "title": "2. Award amounts",
                                                 "pages": [
                                                   {
                                                     "id": "howMuchFundingIsAvailablePageId",
                                                     "title": "How Much Funding Is Available?",
                                                     "questions": []
                                                   }
                                                 ]
                                               }
                                             ]
                                           }');