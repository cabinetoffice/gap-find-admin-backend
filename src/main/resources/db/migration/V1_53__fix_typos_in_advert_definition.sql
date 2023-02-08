-- GAP-1685
UPDATE public.gap_definition SET definition ['sections'][0]['pages'][0]['questions'][0]['hintText'] = '"Write a short overview of what the grant is for. This description should be short as it appears on the search results page. \n\nYou will be able to add a longer description later."'
WHERE gap_definition_id  = 1;

-- GAP-1687
UPDATE public.gap_definition SET definition ['sections'][1]['pages'][0]['questions'][1]['hintText'] = '"This is the highest amount you will award per application.\nType a numerical figure, for example, £50000."'
WHERE gap_definition_id  = 1;
