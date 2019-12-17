/*
 * Copyright (C) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.pso.pubsub;

import com.google.cloud.pso.Employee;
import com.google.cloud.pso.pubsub.common.ObjectPublisher;
import com.google.cloud.pso.pubsub.common.ObjectReader;
import com.google.protobuf.ByteString;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * An implementation of the {@link ObjectPublisher} to publish test {@link Employee} objects to
 * Pub/Sub using Avro GenericRecords to allow deserialization without artifacts or entity classes.
 *
 * The serialized message is an Avro Datafile that could contain multiple records and can be read as an iterator
 */
public class EmployeePublisher extends ObjectPublisher<Employee> {

    /**
     * Converts an Employee record to Avro GenericRecord and append it to a serialized Avro Datafile
     */

    @Override
    public ByteString serialize(Employee employee) throws IOException {

        ByteString byteString;

        GenericData.Record record = new GenericData.Record(employee.getSchema());
        record.put("name", employee.getName());
        record.put("id", employee.getId());

        byte [] bytes = encodeRecordToBytes(employee.getSchema(), record);

        return ByteString.copyFrom(bytes);
    }

    private byte[] encodeRecordToBytes(Schema schema, GenericData.Record record) throws IOException {
        byte[] encodedRecord;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (DataFileWriter<GenericRecord> dataFileWriter =
                         new DataFileWriter<>(new GenericDatumWriter<>(schema))) {

                dataFileWriter.create(schema, out).append(record);
            }

            encodedRecord = out.toByteArray();
        }

        return encodedRecord;
    }

    /**
     * Initializes a reader for publishing {@link Employee} records and executes
     * the parent class's {@link ObjectPublisher#run(Args, ObjectReader)} method
     * to publish the records to Pub/Sub
     * @param args Arguments used by {@link ObjectPublisher} to configure the
     *             {@link com.google.api.gax.batching.BatchingSettings}
     * @throws IOException
     */
    public void publish(Args args) throws IOException {
        /**
         * For demonstration purposes we will use an implementation of ObjectReader
         * to generate random Employee objects to be published.
         */
        ObjectReader<Employee> employeeReader =
                new GenerateRandomEmployeeReader(args.getNumOfMessages());

        this.run(args, employeeReader);
    }
}
