package gov.cabinetoffice.gap.adminbackend.constants;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ValidationMaps {

    public final static ConcurrentHashMap<String, Object> SHORT_ANSWER_VALIDATION = new ConcurrentHashMap<>(Map.ofEntries(
            new AbstractMap.SimpleEntry<String, Object>("minLength", 1),
            new AbstractMap.SimpleEntry<String, Object>("maxLength", 250)));

    public final static ConcurrentHashMap<String, Object> LONG_ANSWER_VALIDATION = new ConcurrentHashMap<>(Map.ofEntries(
            new AbstractMap.SimpleEntry<String, Object>("minLength", 2),
            new AbstractMap.SimpleEntry<String, Object>("maxLength", 6000)));

    public final static ConcurrentHashMap<String, Object> NUMERIC_ANSWER_VALIDATION = new ConcurrentHashMap<>(Map.ofEntries(
            (new AbstractMap.SimpleEntry<String, Object>("greaterThanZero", true))));

    public final static ConcurrentHashMap<String, Object> SINGLE_FILE_UPLOAD_VALIDATION = new ConcurrentHashMap<>(Map.ofEntries(
            new AbstractMap.SimpleEntry<String, Object>("maxFileSizeMB", 300),
            new AbstractMap.SimpleEntry<String, Object>("allowedTypes",
                    new String[] { "DOC", "DOCX", "ODT", "PDF", "XLS", "XLSX", "ZIP" })));

    public final static Map<String, Object> NO_VALIDATION = Collections.emptyMap();

}
