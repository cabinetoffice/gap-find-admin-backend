-- grant_export table

alter table public.grant_export
drop constraint grant_export_submission_id_fkey,
add constraint grant_export_submission_id_fkey
    foreign key (submission_id)
    references public.grant_submission (id) match simple
    on delete cascade
    on update no action;

-- grant_applicant_organisation_profile table

alter table public.grant_applicant_organisation_profile
drop constraint grant_applicant_organisation_profile_applicant_id_fkey,
add constraint grant_applicant_organisation_profile_applicant_id_fkey
    foreign key (applicant_id)
    references public.grant_applicant (id) match simple
    on delete cascade
    on update no action;

-- grant_attachment table

alter table public.grant_attachment
drop constraint fkauex62pbrqwu8rh0g28l8phq9,
add constraint fkauex62pbrqwu8rh0g28l8phq9
    foreign key (created_by)
    references public.grant_applicant (id) match simple
    on delete cascade
    on update no action;

alter table public.grant_attachment
drop constraint fkq3von08d8ihi8rxngekli01l0,
add constraint fkq3von08d8ihi8rxngekli01l0
    foreign key (submission_id)
    references public.grant_submission (id) match simple
    on delete cascade
    on update no action;

-- grant_submission table

alter table public.grant_submission
drop constraint submission_applicant_id_fkey,
add constraint submission_applicant_id_fkey
    foreign key (applicant_id)
    references public.grant_applicant (id) match simple
    on delete cascade
    on update no action;

alter table public.grant_submission
drop constraint submission_last_updated_by_fkey,
add constraint submission_last_updated_by_fkey
    foreign key (last_updated_by)
    references public.grant_applicant (id) match simple
    on delete cascade
    on update no action;

alter table public.grant_submission
drop constraint submission_created_by_fkey,
add constraint submission_created_by_fkey
    foreign key (created_by)
    references public.grant_applicant (id) match simple
    on delete cascade
    on update no action;