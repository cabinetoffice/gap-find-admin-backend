-- !!!!!  ENSURE event_stream schema has been created  !!!!
CREATE schema IF NOT EXISTS event_stream;

CREATE TABLE IF NOT EXISTS event_stream.event_log
(
  id bigint NOT NULL,
  user_sub character varying(128) NOT NULL,
  funding_organisation_id bigint,
  session_id character varying(128) NOT NULL,
  event_type character varying(128) NOT NULL,
  object_id bigint NOT NULL,
  object_type character varying(128) NOT NULL,
  time_stamp timestamp(6) with time zone NOT NULL,
  actioned boolean NOT NULL DEFAULT false,
  created timestamp(6) with time zone NOT NULL,
  CONSTRAINT event_log_pkey PRIMARY KEY (id)
);


create index event_log_object_id_index on event_stream.event_log(object_id);
create index event_log_user_sub_index on event_stream.event_log(user_sub);
create index event_log_object_id_user_sub_index on event_stream.event_log(object_id, user_sub);



