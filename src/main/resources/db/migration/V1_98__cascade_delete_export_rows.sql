alter table public.grant_export
drop constraint grant_export_created_by_fkey,
add constraint grant_export_created_by_fkey
    foreign key (created_by)
    references public.grant_admin (grant_admin_id) match simple
    on delete cascade
    on update no action;

alter table public.grant_export_batch
drop constraint grant_export_batch_created_by_fkey,
add constraint grant_export_batch_created_by_fkey
    foreign key (created_by)
    references public.grant_admin (grant_admin_id) match simple
    on delete cascade
    on update no action;