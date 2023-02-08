UPDATE public.gap_definition SET definition ['sections'][2]['pages'][0]['questions'][0]['hintText'] = '"Your advert will be published on the opening date at 00:01am."'
WHERE gap_definition_id  = 1;

UPDATE public.gap_definition SET definition ['sections'][2]['pages'][0]['questions'][1]['hintText'] = '"Your advert will be unpublished on the closing date at 23:59pm."'
WHERE gap_definition_id  = 1;