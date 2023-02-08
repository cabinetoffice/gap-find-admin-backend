UPDATE public.gap_definition SET definition ['sections'][1]['pages'][0]['questions'][2] = 
    JSONB_INSERT(
        definition ['sections'][1]['pages'][0]['questions'][2], 
        '{validation, comparedTo}',
        '{
            "questionId": "grantMaximumAward",
            "errorMessage": "The minimum amount must be less than the maximum amount",
            "lessThan": true
         }'::JSONB) 
where name = 'Grant Advert Definition';