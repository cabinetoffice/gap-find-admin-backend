alter table public.grant_beneficiary
alter column created_by drop not null,
drop constraint if exists created_by_fkey,
add constraint created_by_fkey
    foreign key (created_by)
    references public.grant_applicant (id) match simple
    on delete set null
    on update no action;