UPDATE public.gap_definition
    SET definition ['sections'][0]['title'] = '"1. Grant details"'
WHERE gap_definition_id  = 1;