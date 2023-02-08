UPDATE public.gap_definition SET definition ['sections'][1]['pages'][0]['questions'][0]['hintText'] = '"This is the total pot size for this grant.\nType a numerical figure, for example, £50000."'
WHERE gap_definition_id  = 1;

UPDATE public.gap_definition SET definition ['sections'][1]['pages'][0]['questions'][2]['hintText'] = '"This is the lowest amount you will award per application.\nType a numerical figure, for example, £50000."'
WHERE gap_definition_id  = 1;