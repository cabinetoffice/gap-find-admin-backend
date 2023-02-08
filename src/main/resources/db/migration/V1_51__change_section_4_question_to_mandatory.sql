-- Update question to be mandatory
UPDATE public.gap_definition SET definition ['sections'][3]['pages'][0]['questions'][0]['validation']['mandatory'] = 'true'
WHERE gap_definition_id  = 1;

UPDATE public.gap_definition SET definition ['sections'][3]['pages'][0]['questions'][0]['title'] = '"Add a link so applicants know where to apply"'
WHERE gap_definition_id  = 1;