UPDATE public.gap_definition SET definition ['sections'][0]['pages'][0]['questions'][0]['hintText'] = '"Write a short overview of what the grant is for. This description should be short as it appear on the results page. \n\nYou will be able to add a longer description later."'
WHERE gap_definition_id  = 1;

UPDATE public.gap_definition SET definition ['sections'][0]['pages'][0]['questions'][0]['exampleText'] = '{"title": "See examples of short descriptions", "text": "Example one: \n\nDisabled Facilities Grants can help meet the cost of making changes to your home so you, or someone you live with, can be safe and independent. People of all ages and tenures can apply to their local council for this grant.\n\nExample two:\n\nThis grant provides electric vehicle drivers who rent or live in flats with support towards the upfront costs of purchasing and installing an electric vehicle (EV) charge point."}'
WHERE gap_definition_id  = 1;

