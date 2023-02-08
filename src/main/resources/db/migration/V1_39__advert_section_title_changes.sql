
-- Correcting capitalisation for section 2 title
UPDATE public.gap_definition SET definition ['sections'][1]['title'] = '"2. Award amounts"'
WHERE gap_definition_id  = 1;

-- Updating dates page title
UPDATE public.gap_definition SET definition ['sections'][2]['pages'][0]['title'] = '"Opening and closing dates"'
WHERE gap_definition_id  = 1;

-- Updating link page title
UPDATE public.gap_definition SET definition ['sections'][3]['pages'][0]['title'] = '"Link to application form"'
WHERE gap_definition_id  = 1;

-- Updating scheme objectives page title
UPDATE public.gap_definition SET definition ['sections'][4]['pages'][3]['title'] = '"Scheme objectives"'
WHERE gap_definition_id  = 1;