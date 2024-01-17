 --Updating hint text page question 1 response type
UPDATE public.gap_definition SET definition ['sections'][2]['pages'][0]['questions'][0]['hintText'] = '"Your advert will be published on the opening date at the opening time."'
WHERE gap_definition_id  = 1;

--Updating hint text page question 2 response type
UPDATE public.gap_definition SET definition ['sections'][2]['pages'][0]['questions'][1]['hintText'] = '"Your advert will be unpublished on the closing date at the closing time."'
WHERE gap_definition_id  = 1;