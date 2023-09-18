alter table public.grant_beneficiary
add constraint created_by_fkey
    foreign key (created_by)
    references public.grant_applicant (id) match simple
    on delete set null
    on update no action;