
--total award amount question
--add validation 
UPDATE public.gap_definition SET definition ['sections'][1]['pages'][0]['questions'][0]['validation'] = 
'{
    "mandatory": true,
    "greaterThan": 0
}'
WHERE gap_definition_id  = 1;

--update hint-text
UPDATE public.gap_definition SET definition ['sections'][1]['pages'][0]['questions'][0]['hintText'] = '"This is the total pot size for this grant.\nType a numerical figure e.g £50000."'
WHERE gap_definition_id  = 1;

--update response type
UPDATE public.gap_definition SET definition ['sections'][1]['pages'][0]['questions'][0]['responseType'] = '"CURRENCY"'
WHERE gap_definition_id  = 1;


--max award question
--add validation
UPDATE public.gap_definition SET definition ['sections'][1]['pages'][0]['questions'][1]['validation'] = 
'{
    "mandatory": true,
    "greaterThan": 0
}'
WHERE gap_definition_id  = 1;

--update hint-text
UPDATE public.gap_definition SET definition ['sections'][1]['pages'][0]['questions'][1]['hintText'] = '"This is the highest amount you will award per application.\nType a numerical figure e.g £50000."'
WHERE gap_definition_id  = 1;

--update response type
UPDATE public.gap_definition SET definition ['sections'][1]['pages'][0]['questions'][1]['responseType'] = '"CURRENCY"'
WHERE gap_definition_id  = 1;

--min award question
--add validation
UPDATE public.gap_definition SET definition ['sections'][1]['pages'][0]['questions'][2]['validation'] = 
'{
    "mandatory": true,
    "greaterThan": 0,
    "comparedTo": {
        "lessThan": true,
        "questionId": "grantMaximumAward",
        "errorMessage": "The minimum amount must be less than the maximum amount"
    }
}'
WHERE gap_definition_id  = 1;

--update hint-text
UPDATE public.gap_definition SET definition ['sections'][1]['pages'][0]['questions'][2]['hintText'] = '"This is the lowest amount you will award per application.\nType a numerical figure e.g £50000."'
WHERE gap_definition_id  = 1;

--update response type
UPDATE public.gap_definition SET definition ['sections'][1]['pages'][0]['questions'][2]['responseType'] = '"CURRENCY"'
WHERE gap_definition_id  = 1;
