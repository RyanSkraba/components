package org.talend.components.api.component.runtime;

import org.apache.avro.Schema;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.daikon.avro.util.AvroUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Helper methods for use by components.
 */
public class RuntimeHelper {

    /**
     * Used to resolve the dynamic fields in the schema with the actual fields available at runtime.
     * <p>
     * This returns a schema where the dynamic field is replaced by the fields obtained from the {@link SourceOrSink}.
     *
     * @param ss           the {@link SourceOrSink} to use to get the runtime schema.
     * @param designSchema the design time {@link Schema}.
     * @return a {@link Schema} modified as described above.
     */
    public static Schema resolveSchema(RuntimeContainer container, SourceOrSink ss,
                                       Schema designSchema) throws IOException {
        Schema runtimeSchema = ss.getPossibleSchemaFromProperties(container);

        Map<String, Schema.Field> fieldMap = AvroUtils.makeFieldMap(designSchema);
        List<Schema.Field> fieldList = designSchema.getFields();
        List<Schema.Field> copyFieldList = new ArrayList<>();

        for (Schema.Field se : fieldList) {
            copyFieldList.add(new Schema.Field(se.name(), se.schema(), se.doc(), se.defaultVal()));
        }

        int dynamicIndex = 0;
        for (Schema.Field se : copyFieldList) {
            if (AvroUtils.isDynamic(se.schema())) {
                break;
            }
            dynamicIndex++;
        }

        List<Schema.Field> beforeDyn = copyFieldList.subList(0, dynamicIndex);
        List<Schema.Field> afterDyn = dynamicIndex < copyFieldList.size() ? copyFieldList.subList(dynamicIndex + 1,
                copyFieldList.size()) : Collections.<Schema.Field>emptyList();

        List<Schema.Field> filteredDynamicFields = new ArrayList<>();
        for (Schema.Field se : runtimeSchema.getFields()) {
            if (fieldMap.containsKey(se.name())) {
                continue;
            }
            filteredDynamicFields.add(se);
        }

        List<Schema.Field> copyFilteredDynamicFields = new ArrayList<>();
        for (Schema.Field se : filteredDynamicFields) {
            copyFilteredDynamicFields.add(new Schema.Field(se.name(), se.schema(), se.doc(), se.defaultVal()));
        }

        List<Schema.Field> runtimeFields = new ArrayList<>();
        runtimeFields.addAll(beforeDyn);
        runtimeFields.addAll(copyFilteredDynamicFields);
        runtimeFields.addAll(afterDyn);

        Schema resolvedSchema = Schema.createRecord(designSchema.getName(), designSchema.getDoc(), designSchema.getNamespace(),
                false);
        resolvedSchema.setFields(runtimeFields);
        return resolvedSchema;
    }
}
