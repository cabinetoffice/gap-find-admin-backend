-- grant_admin table

alter table public.grant_admin
drop constraint fk_gap_user_grant_admin,
add constraint fk_gap_user_grant_admin
    foreign key (user_id)
    references public.gap_user (gap_user_id) match simple
    on delete cascade
    on update no action;

-- grant_advert table

alter table public.grant_advert
drop constraint fkj37dbyjpgaof3c3l4k33qbsjt,
add constraint fkj37dbyjpgaof3c3l4k33qbsjt
    foreign key (created_by)
    references public.grant_admin (grant_admin_id) match simple
    on delete cascade
    on update no action;

alter table public.grant_advert
drop constraint fkkyx0kmnoeortbeg2uv6s1tjsl,
add constraint fkkyx0kmnoeortbeg2uv6s1tjsl
    foreign key (last_updated_by)
    references public.grant_admin (grant_admin_id) match simple
    on delete cascade
    on update no action;

alter table public.grant_advert
drop constraint fkgkr2wfbxbhcehfofhkx9ewkdr,
add constraint fkgkr2wfbxbhcehfofhkx9ewkdr
    foreign key (scheme_id)
    references public.grant_scheme (grant_scheme_id) match simple
    on delete cascade
    on update no action;

-- grant_application table

alter table public.grant_application
drop constraint fk_grant_admin_id_grant_admin,
add constraint fk_grant_admin_id_grant_admin
    foreign key (created_by)
    references public.grant_admin (grant_admin_id) match simple
    on delete set null
    on update no action;

-- grant_export table

alter table public.grant_export
drop constraint grant_export_created_by_fkey,
add constraint grant_export_created_by_fkey
    foreign key (created_by)
    references public.grant_admin (grant_admin_id) match simple
    on delete cascade
    on update no action;

alter table public.grant_export
drop constraint grant_export_application_id_fkey,
add constraint grant_export_application_id_fkey
    foreign key (application_id)
    references public.grant_application (grant_application_id) match simple
    on delete cascade
    on update no action;

alter table public.grant_export
drop constraint grant_export_submission_id_fkey,
add constraint grant_export_submission_id_fkey
    foreign key (submission_id)
    references public.grant_submission (id) match simple
    on delete cascade
    on update no action;

-- grant_scheme table

alter table public.grant_scheme
drop constraint fk_grant_admin_id_grant_admin,
add constraint fk_grant_admin_id_grant_admin
    foreign key (created_by)
    references public.grant_admin (grant_admin_id) match simple
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

alter table public.grant_submission
drop constraint submission_application_id_fkey,
add constraint submission_application_id_fkey
    foreign key (application_id)
    references public.grant_application (grant_application_id) match simple
    on delete set null
    on update no action;

alter table public.grant_submission
drop constraint submission_scheme_id_fkey,
add constraint submission_scheme_id_fkey
    foreign key (scheme_id)
    references public.grant_scheme (grant_scheme_id) match simple
    on delete set null
    on update no action;