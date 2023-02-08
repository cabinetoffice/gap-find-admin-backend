UPDATE public.gap_definition SET definition ['sections'][0]['pages'][0]['questions'][0]['summaryTitle'] = '"Short description"'
WHERE gap_definition_id  = 1;
UPDATE public.gap_definition SET definition ['sections'][0]['pages'][1]['questions'][0]['summaryTitle'] = '"Location"'
WHERE gap_definition_id  = 1;
UPDATE public.gap_definition SET definition ['sections'][0]['pages'][2]['questions'][0]['summaryTitle'] = '"Funding organisation"'
WHERE gap_definition_id  = 1;
UPDATE public.gap_definition SET definition ['sections'][0]['pages'][3]['questions'][0]['summaryTitle'] = '"Who can apply"'
WHERE gap_definition_id  = 1;

UPDATE public.gap_definition SET definition ['sections'][1]['pages'][0]['questions'][0]['summaryTitle'] = '"Total amount of the grant"'
WHERE gap_definition_id  = 1;
UPDATE public.gap_definition SET definition ['sections'][1]['pages'][0]['questions'][1]['summaryTitle'] = '"Maximum amount someone can apply for"'
WHERE gap_definition_id  = 1;
UPDATE public.gap_definition SET definition ['sections'][1]['pages'][0]['questions'][2]['summaryTitle'] = '"Minimum amount someone can apply for"'
WHERE gap_definition_id  = 1;

UPDATE public.gap_definition SET definition ['sections'][2]['pages'][0]['questions'][0]['summaryTitle'] = '"Opening"'
WHERE gap_definition_id  = 1;
UPDATE public.gap_definition SET definition ['sections'][2]['pages'][0]['questions'][1]['summaryTitle'] = '"Closing"'
WHERE gap_definition_id  = 1;

UPDATE public.gap_definition SET definition ['sections'][3]['pages'][0]['questions'][0]['summaryTitle'] = '"''Start new application'' button links to:"'
WHERE gap_definition_id  = 1;

UPDATE public.gap_definition SET definition ['sections'][4]['pages'][0]['questions'][0]['summaryTitle'] = '"Eligibility information"'
WHERE gap_definition_id  = 1;
UPDATE public.gap_definition SET definition ['sections'][4]['pages'][1]['questions'][0]['summaryTitle'] = '"Long description"'
WHERE gap_definition_id  = 1;
UPDATE public.gap_definition SET definition ['sections'][4]['pages'][2]['questions'][0]['summaryTitle'] = '"Relevant dates"'
WHERE gap_definition_id  = 1;
UPDATE public.gap_definition SET definition ['sections'][4]['pages'][3]['questions'][0]['summaryTitle'] = '"Objectives of the grant"'
WHERE gap_definition_id  = 1;
UPDATE public.gap_definition SET definition ['sections'][4]['pages'][4]['questions'][0]['summaryTitle'] = '"How to apply"'
WHERE gap_definition_id  = 1;
UPDATE public.gap_definition SET definition ['sections'][4]['pages'][5]['questions'][0]['summaryTitle'] = '"Supporting information"'
WHERE gap_definition_id  = 1;