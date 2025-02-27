/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.asterix.dataflow.data.nontagged.serde;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.asterix.formats.nontagged.AqlSerializerDeserializerProvider;
import org.apache.asterix.om.base.ADateTime;
import org.apache.asterix.om.base.AMutableDateTime;
import org.apache.asterix.om.base.temporal.ADateParserFactory;
import org.apache.asterix.om.base.temporal.ATimeParserFactory;
import org.apache.asterix.om.types.BuiltinType;
import org.apache.hyracks.algebricks.common.exceptions.AlgebricksException;
import org.apache.hyracks.api.dataflow.value.ISerializerDeserializer;
import org.apache.hyracks.api.exceptions.HyracksDataException;

public class ADateTimeSerializerDeserializer implements ISerializerDeserializer<ADateTime> {

    private static final long serialVersionUID = 1L;

    public static final ADateTimeSerializerDeserializer INSTANCE = new ADateTimeSerializerDeserializer();
    @SuppressWarnings("unchecked")
    private static final ISerializerDeserializer<ADateTime> datetimeSerde = AqlSerializerDeserializerProvider.INSTANCE
            .getSerializerDeserializer(BuiltinType.ADATETIME);
    private static final AMutableDateTime aDateTime = new AMutableDateTime(0L);
    private static final String errorMessage = "This can not be an instance of datetime";

    private ADateTimeSerializerDeserializer() {
    }

    @Override
    public ADateTime deserialize(DataInput in) throws HyracksDataException {
        try {
            return new ADateTime(in.readLong());
        } catch (IOException e) {
            throw new HyracksDataException(e);
        }
    }

    @Override
    public void serialize(ADateTime instance, DataOutput out) throws HyracksDataException {
        try {
            out.writeLong(instance.getChrononTime());
        } catch (IOException e) {
            throw new HyracksDataException(e);
        }
    }

    public static void parse(String datetime, DataOutput out) throws HyracksDataException {

        long chrononTimeInMs = 0;
        try {

            // +1 if it is negative (-)
            short timeOffset = (short) ((datetime.charAt(0) == '-') ? 1 : 0);

            timeOffset += 8;

            if (datetime.charAt(timeOffset) != 'T') {
                timeOffset += 2;
                if (datetime.charAt(timeOffset) != 'T') {
                    throw new AlgebricksException(errorMessage + ": missing T");
                }
            }

            chrononTimeInMs = ADateParserFactory.parseDatePart(datetime, 0, timeOffset);

            chrononTimeInMs += ATimeParserFactory.parseTimePart(datetime, timeOffset + 1, datetime.length()
                    - timeOffset - 1);
        } catch (Exception e) {
            throw new HyracksDataException(e);
        }
        aDateTime.setValue(chrononTimeInMs);

        datetimeSerde.serialize(aDateTime, out);
    }

    public static long getChronon(byte[] data, int offset) {
        return AInt64SerializerDeserializer.getLong(data, offset);
    }
}
