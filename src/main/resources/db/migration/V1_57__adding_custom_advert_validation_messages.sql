-- Section 1
-- Page 1
UPDATE public.gap_definition SET definition ['sections'][0]['pages'][0]['questions'][0]['validationMessages'] =
    '{"mandatory": "Enter a short description", "maxLength": "Short description must be 800 characters or less"}'
WHERE gap_definition_id  = 1;
-- Page 2
UPDATE public.gap_definition SET definition ['sections'][0]['pages'][1]['questions'][0]['validationMessages'] =
    '{"mandatory": "Select at least one location where the grant is available"}'
WHERE gap_definition_id  = 1;

--Page 3
UPDATE public.gap_definition SET definition ['sections'][0]['pages'][2]['questions'][0]['validationMessages'] =
    '{"mandatory": "Enter a funding organisation", "maxLength": "Funding organisation must be 256 characters or less"}'
WHERE gap_definition_id  = 1;

--Page 4
UPDATE public.gap_definition SET definition ['sections'][0]['pages'][3]['questions'][0]['validationMessages'] =
    '{"mandatory": "Select at least one group who can apply"}'
WHERE gap_definition_id  = 1;

-- Section 2
-- Page 1
UPDATE public.gap_definition SET definition ['sections'][1]['pages'][0]['questions'][0]['validationMessages'] =
    '{"mandatory": "Enter a total amount", "greaterThan": "Total amount must be higher than zero"}'
WHERE gap_definition_id  = 1;

UPDATE public.gap_definition SET definition ['sections'][1]['pages'][0]['questions'][1]['validationMessages'] =
    '{"mandatory": "Enter a maximum amount", "greaterThan": "Maximum amount must be higher than zero"}'
WHERE gap_definition_id  = 1;

UPDATE public.gap_definition SET definition ['sections'][1]['pages'][0]['questions'][2]['validationMessages'] =
    '{"mandatory": "Enter a minimum amount", "greaterThan": "Minimum amount must be higher than zero"}'
WHERE gap_definition_id  = 1;

-- Section 3 - Dates... Changes where made to the implementation
UPDATE public.gap_definition SET definition ['sections'][2]['pages'][0]['questions'][0]['validationMessages'] =
    '{"mandatory": "Enter an opening date", "missingField": "Opening date must include a %s", "invalid": "Opening date must include a real %s"}'
WHERE gap_definition_id  = 1;

UPDATE public.gap_definition SET definition ['sections'][2]['pages'][0]['questions'][1]['validationMessages'] =
    '{"mandatory": "Enter a closing date", "missingField": "Closing date must include a %s", "invalid": "Closing date must include a real %s"}'
WHERE gap_definition_id  = 1;

-- Section 4
-- Page 1
UPDATE public.gap_definition SET definition ['sections'][3]['pages'][0]['questions'][0]['validationMessages'] =
    '{"mandatory": "Enter a link where applicants can apply"}'
WHERE gap_definition_id  = 1;

-- Section 5
-- Page 1
UPDATE public.gap_definition SET definition ['sections'][4]['pages'][0]['questions'][0]['validationMessages'] =
    '{"mandatory": "Enter eligibility information"}'
WHERE gap_definition_id  = 1;

-- Page 2
UPDATE public.gap_definition SET definition ['sections'][4]['pages'][1]['questions'][0]['validationMessages'] =
    '{"mandatory": "Enter a long description of your grant"}'
WHERE gap_definition_id  = 1;

-- Page 3 has no custom error messages needed

-- Page 4
UPDATE public.gap_definition SET definition ['sections'][4]['pages'][3]['questions'][0]['validationMessages'] =
    '{"mandatory": "Enter the objectives of your grant"}'
WHERE gap_definition_id  = 1;

-- Page 5
UPDATE public.gap_definition SET definition ['sections'][4]['pages'][4]['questions'][0]['validationMessages'] =
    '{"mandatory": "Enter information about where to apply for your grant"}'
WHERE gap_definition_id  = 1;

-- Page 6 has no custom error messages needed
