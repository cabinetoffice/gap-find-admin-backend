CREATE event_stream.SEQUENCE EVENT_LOG_ID_SEQ
INCREMENT 1
START 1
OWNED BY event_stream.event_log.id
;
