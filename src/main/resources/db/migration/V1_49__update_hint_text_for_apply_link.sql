UPDATE public.gap_definition
    SET definition ['sections'][3]['pages'][0]['questions'][0]['hintText'] = '"Applicants will be directed to this link when they select \"Start new application\" \n\nEnter a link that all applicants can access. The link must begin with \"https://\"."'
WHERE gap_definition_id  = 1;