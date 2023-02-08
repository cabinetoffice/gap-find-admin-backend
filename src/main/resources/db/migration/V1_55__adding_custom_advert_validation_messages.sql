-- Section 1
-- Page 1
UPDATE public.gap_definition SET definition ['sections'][0]['pages'][0]['questions'][0]['validationMessages'] = 
    '{"mandatory": "Enter a short description", "maxLength": "Short description must be 800 characters or less"}'
WHERE gap_definition_id  = 1;
-- Page 2
UPDATE public.gap_definition SET definition ['sections'][0]['pages'][1]['questions'][0]['validationMessages'] = 
    '{"mandatory": "You must select at least one location"}'
WHERE gap_definition_id  = 1;

--Page 3 
UPDATE public.gap_definition SET definition ['sections'][0]['pages'][2]['questions'][0]['validationMessages'] = 
    '{"mandatory": "You must enter an answer", "maxLength": "Your answer must be 256 characters or less"}'
WHERE gap_definition_id  = 1;

--Page 4
UPDATE public.gap_definition SET definition ['sections'][0]['pages'][3]['questions'][0]['validationMessages'] = 
    '{"mandatory": "You must select at least one option"}'
WHERE gap_definition_id  = 1;

-- Section 2
-- Page 1 
UPDATE public.gap_definition SET definition ['sections'][1]['pages'][0]['questions'][0]['validationMessages'] = 
    '{"mandatory": "You must enter a total amount", "greaterThan": "You must enter an amount higher than zero"}'
WHERE gap_definition_id  = 1;

UPDATE public.gap_definition SET definition ['sections'][1]['pages'][0]['questions'][1]['validationMessages'] = 
    '{"mandatory": "You must enter a maximum amount", "greaterThan": "You must enter an amount higher than zero"}'
WHERE gap_definition_id  = 1;

UPDATE public.gap_definition SET definition ['sections'][1]['pages'][0]['questions'][2]['validationMessages'] = 
    '{"mandatory": "You must enter a minimum amount", "greaterThan": "You must enter an amount higher than zero"}'
WHERE gap_definition_id  = 1;

-- Section 3 - Dates... Changes where made to the implementation

-- Section 4
-- Page 1
UPDATE public.gap_definition SET definition ['sections'][3]['pages'][0]['questions'][0]['validationMessages'] = 
    '{"mandatory": "You must enter a link"}'
WHERE gap_definition_id  = 1;

-- Section 5
-- Page 1
UPDATE public.gap_definition SET definition ['sections'][4]['pages'][0]['questions'][0]['validationMessages'] = 
    '{"mandatory": "You must enter eligibility information"}'
WHERE gap_definition_id  = 1;

-- Page 2
UPDATE public.gap_definition SET definition ['sections'][4]['pages'][1]['questions'][0]['validationMessages'] = 
    '{"mandatory": "You must enter a description of your grant"}'
WHERE gap_definition_id  = 1;

-- Page 3 has no custom error messages needed

-- Page 4
UPDATE public.gap_definition SET definition ['sections'][4]['pages'][3]['questions'][0]['validationMessages'] = 
    '{"mandatory": "You must enter the objectives of your grant"}'
WHERE gap_definition_id  = 1;

-- Page 5
UPDATE public.gap_definition SET definition ['sections'][4]['pages'][4]['questions'][0]['validationMessages'] = 
    '{"mandatory": "You must add information about where to apply for your grant"}'
WHERE gap_definition_id  = 1;

-- Page 6 has no custom error messages needed
