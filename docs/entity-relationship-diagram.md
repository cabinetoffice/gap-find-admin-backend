# Entity Relationship Diagram

The GAP project relies on a PostgreSQL database for all its internal data storage. In addition, it makes use of Contentful CMS and AWS S3 for further storage.

## PostreSQL Database

```mermaid
erDiagram

grant_funding_organisation {
    int funder_id PK
    string organisation_name
}

grant_admin {
    int grant_admin_id PK
    int funder_id FK
    int user_id FK
}

grant_admin }o--|| grant_funding_organisation: "works for"

gap_user {
    int gap_user_id PK
    uuid user_sub
}

grant_admin ||--|| gap_user: "is a"

grant_scheme {
    int grant_scheme_id PK 
    int funder_id FK
    int version
    timestamp created
    int created_by FK
    timestamp last_updated
    int last_updated_by FK
    string ggis_identifier
    string scheme_name
    string scheme_contact
}

grant_scheme }o--|| grant_admin: "creates"
grant_scheme }o--|| grant_funding_organisation: "run by"

grant_application {
    int grant_application_id PK 
    int scheme_id FK
    int version
    timestamp created
    int created_by FK 
    timestamp last_updated
    int last_updated_by FK
    string application_name
    enum status
    jsonb definition
}

grant_application }o--|| grant_admin: "creates"
grant_application |o--|| grant_scheme: "is for"

grant_applicant {
    int grant_applicant_id PK 
    uuid user_id 
}

grant_applicant_organisation_profile {
    int id PK
    int applicant_id FK
    string address_line1
    string address_line2
    string charity_commission_number
    string companies_house_number
    string county
    string legal_name
    string postcode
    string town
    string type
}

grant_applicant ||--|| grant_applicant_organisation_profile: "creates"

grant_submission {
    uuid grant_submission_id PK 
    int scheme_id FK
    int application_id FK
    int version
    timestamp created
    int created_by FK
    timestamp last_updated
    int last_updated_by FK
    timestamp submitted
    string gap_id
    string application_name
    enum status
    jsonb definition
    timestamp last_required_checks_export
}

grant_submission }o--|| grant_application: "is for"
grant_submission }o--|| grant_scheme: "is for"
grant_applicant ||--o{ grant_submission: "creates"

grant_beneficiary {
    uuid grant_beneficiary_id PK 
    int scheme_id FK
    int application_id FK
    uuid submission_id FK
    string gap_id
    int version
    timestamp created
    int created_by FK
    timestamp last_updated
    int last_updated_by FK
    boolean location_ne_eng
    boolean location_nw_eng
    boolean location_se_eng
    boolean location_sw_eng
    boolean location_mid_eng
    boolean location_sco
    boolean location_wal
    boolean location_nir
    boolean has_provided_additional_answers
    boolean supports_specific_gender
    string supporting_gender_details
    boolean age_group1
    boolean age_group2
    boolean age_group3
    boolean age_group4
    boolean age_group5
    boolean age_group_all
    boolean ethnic_group1
    boolean ethnic_group2
    boolean ethnic_group3
    boolean ethnic_group4
    boolean ethnic_group5
    boolean ethnic_group_other
    string ethnic_other_details
    boolean ethnic_group_all
    boolean supporting_disabilities
    boolean sexual_orientation_group1
    boolean sexual_orientation_group2
    boolean sexual_orientation_group3
    boolean sexual_orientation_other
    string sexual_orientation_other_details
    boolean sexual_orientation_group_all
    boolean sex_group1
    boolean sex_group2
    boolean sex_group_all
}

grant_beneficiary |o--|| grant_submission: "relates to"
grant_beneficiary }o--|| grant_application: "relates to"
grant_beneficiary }o--|| grant_scheme: "relates to"
grant_applicant ||--o{ grant_beneficiary: "creates"

diligence_check {
    uuid diligence_check_id PK 
    uuid submission_id FK
    timestamp created
    int check_type
    string application_number
    string organisation_name
    string address_street
    string address_town
    string address_county
    string address_postcode
    string application_amount
    string charity_number
    string companies_house_number
}

diligence_check |o--|| grant_submission: "has"

grant_attachment {
    uuid grant_attachment_id PK 
    uuid submission_id FK 
    string question_id
    int version
    timestamp created
    int created_by FK
    timestamp last_updated
    enum status
    string filename
    string location
}

grant_attachment }o--|| grant_submission: "has"
grant_applicant ||--o{ grant_attachment: "creates"

grant_export {
    uuid export_batch_id PK 
    uuid event_id PK 
    int application_id FK
    uuid submission_id FK
    timestamp created
    int created_by FK
    timestamp last_updated
    enum status
    string location
}

grant_export }o--|| grant_application: "relates to"
grant_export }o--|| grant_submission: "relates to"
grant_admin |o--|{ grant_export: "creates"

grant_advert {
    int grant_advert_id PK 
    int scheme_id FK
    int version
    timestamp created
    int created_by FK
    timestamp last_updated
    int last_updated_by FK
    enum status
    string contentful_entry_id
    string contentful_slug
    string grant_advert_name
    jsonb response
}

grant_advert |o--|| grant_scheme: "is for"
grant_admin |o--|{ grant_advert: "creates"

```
