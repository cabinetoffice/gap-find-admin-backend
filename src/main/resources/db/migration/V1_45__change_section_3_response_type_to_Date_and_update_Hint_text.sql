-- Updating dates page question 1 response type
UPDATE public.gap_definition SET definition ['sections'][2]['pages'][0]['questions'][0]['responseType'] = '"DATE"'
WHERE gap_definition_id  = 1;

-- Updating dates page question 2 response type
UPDATE public.gap_definition SET definition ['sections'][2]['pages'][0]['questions'][1]['responseType'] = '"DATE"'
WHERE gap_definition_id  = 1;

--Updating hint text page question 1 response type
UPDATE public.gap_definition SET definition ['sections'][2]['pages'][0]['questions'][0]['hintText'] = '"Your advert will be published on the opening date at 00:01am.\n\nFor example, 31 3 2023"'
WHERE gap_definition_id  = 1;

--Updating hint text page question 2 response type
UPDATE public.gap_definition SET definition ['sections'][2]['pages'][0]['questions'][1]['hintText'] = '"Your advert will be unpublished on the closing date at 23:59pm.\n\nFor example, 31 3 2023"'
WHERE gap_definition_id  = 1;