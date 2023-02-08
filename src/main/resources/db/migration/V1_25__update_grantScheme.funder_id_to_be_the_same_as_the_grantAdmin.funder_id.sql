UPDATE
  grant_scheme
SET
  funder_id = record_to_be_fixed.admin_funder_id
FROM
  (
    SELECT
      gs.grant_scheme_id as scheme_id,
      gs.funder_id as scheme_funder_id,
      ga.funder_id admin_funder_id
    FROM
      grant_scheme gs
      join grant_admin ga on gs.created_by = ga.grant_admin_id
    WHERE
      gs.funder_id <> ga.funder_id
  ) as record_to_be_fixed
WHERE
  grant_scheme_id = record_to_be_fixed.scheme_id;