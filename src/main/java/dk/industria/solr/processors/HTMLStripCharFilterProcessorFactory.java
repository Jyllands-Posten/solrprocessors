package dk.industria.solr.processors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.solr.common.util.NamedList;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrQueryResponse;


import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;

/**
 * Implements a factory for the HTMLStripCharFilterProcessor
 * <p/>
 * <p>The main purpose of this is to process the init arguments into a list
 * of fields that should be processed with the HTMLStripCharFilterProcessor.</p>
 * <p/>
 * <p>Configuration is done by placing str elements with a name attribute set to field
 * and a element value set to the field that should be processed.</p>
 * <p/>
 * <p>Example processor configuration for processing fields header and content:</p>
 * <p/>
 * <pre>
 * {@code
 * <processor class="dk.industria.solr.processors.HTMLStripCharFilterProcessorFactory">
 *   <str name="field">header</str>
 *   <str name="field">content</str>
 * </processor>
 * }
 * </pre>
 */
public class HTMLStripCharFilterProcessorFactory extends UpdateRequestProcessorFactory {
    /**
     * Logger
     */
    private final static Logger logger = LoggerFactory.getLogger(HTMLStripCharFilterProcessorFactory.class);
    /**
     * List of fields configured for HTML character stripping.
     */
    private List<String> fieldsToProcess;

    /**
     * Generate a string containing the fields configured, the string is
     * on the form {field1} {field2} ... {fieldn}
     *
     * @param fields The fields for the field string.
     * @return String on the form {field1} {field2} ... {fieldn}
     */
    private static String configuredFieldsString(List<String> fields) {
        StringBuilder sb = new StringBuilder();
        for (String field : fields) {
            sb.append(" {").append(field).append("}");
        }
        return sb.toString();
    }

    /**
     * Extract field names from init arguments.
     * That is fields with a key of field and a type of String.
     *
     * @param initArguments NamedList containing the init arguments.
     * @return List of field names.
     */
    private static List<String> extractFields(final NamedList initArguments) {
        List<String> fieldNames = new ArrayList<String>();
        List valuesWithField = initArguments.getAll("field");
        for (Object value : valuesWithField) {
            if (value instanceof String) {
                String valueToAdd = ((String) value).trim();
                if (0 < valueToAdd.length()) {
                    logger.debug("Adding field, with value [{}]", valueToAdd);
                    fieldNames.add(valueToAdd);
                }
            }
        }
        return fieldNames;
    }

    /**
     * Get the list of field names configured for processing.
     *
     * @return Unmodifiable list of field names configured.
     */
    public List<String> getFields() {
        if (null == fieldsToProcess) {
            return Collections.unmodifiableList(new ArrayList<String>());
        }
        return Collections.unmodifiableList(fieldsToProcess);
    }

    /**
     * Init called by Solr processor chain
     * The values configured for keys field is extracted to fieldsToProcess.
     *
     * @param args NamedList of parameters set in the processor definition in solrconfig.xml
     */
    @Override
    public void init(final NamedList args) {
        this.fieldsToProcess = extractFields(args);

        logger.debug("Configured with fields [{}]", configuredFieldsString(this.fieldsToProcess));

        if (this.fieldsToProcess.isEmpty()) {
            logger.warn("No fields configured. Consider removing the processor.");
        }
    }

    /**
     * Factory method for the HTMLStripCharFilterProcessor called by SOLR processor chain.
     *
     * @param req  SolrQueryRequest
     * @param rsp  SolrQueryResponse
     * @param next UpdateRequestProcessor
     * @return Instance of HTMLStripCharFilterProcessor initialized with the fields to process.
     */
    @Override
    public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
        return new HTMLStripCharFilterProcessor(fieldsToProcess, next);
    }
}