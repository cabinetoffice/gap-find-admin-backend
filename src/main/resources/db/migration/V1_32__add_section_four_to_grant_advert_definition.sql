UPDATE public.gap_definition SET definition ['sections'][3] = '
{
    "id": "howToApply",
    "title": "4. How to apply",
    "pages": [
        {
        "id": 1,
        "title": "Link",
        "questions": [
            {
            "id": "grantWebpageUrl",
            "title": "Add a link so applicants know where to apply (optional)",
            "displayText": "",
            "hintText": "Applicants will be directed to this link when they select \"Start new application\" \n\nIf you have an application form online, you can copy the URL below.",
            "suffixText": "",
            "options": [],
            "validation": {
                "mandatory": false,
                "url": true
            },
            "responseType": "SHORT_TEXT"
            }
        ]
        }
    ]
}'