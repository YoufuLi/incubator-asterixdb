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

import org.apache.asterix.dataflow.data.nontagged.Coordinate;
import org.apache.asterix.formats.nontagged.AqlSerializerDeserializerProvider;
import org.apache.asterix.om.base.AMutablePoint;
import org.apache.asterix.om.base.APoint;
import org.apache.asterix.om.types.BuiltinType;
import org.apache.hyracks.api.dataflow.value.ISerializerDeserializer;
import org.apache.hyracks.api.exceptions.HyracksDataException;

public class APointSerializerDeserializer implements ISerializerDeserializer<APoint> {

    private static final long serialVersionUID = 1L;

    public static final APointSerializerDeserializer INSTANCE = new APointSerializerDeserializer();
    @SuppressWarnings("unchecked")
    private final static ISerializerDeserializer<APoint> pointSerde = AqlSerializerDeserializerProvider.INSTANCE
            .getSerializerDeserializer(BuiltinType.APOINT);
    private final static AMutablePoint aPoint = new AMutablePoint(0, 0);

    private APointSerializerDeserializer() {
    }

    @Override
    public APoint deserialize(DataInput in) throws HyracksDataException {
        try {
            return new APoint(in.readDouble(), in.readDouble());
        } catch (IOException e) {
            throw new HyracksDataException(e);
        }
    }

    @Override
    public void serialize(APoint instance, DataOutput out) throws HyracksDataException {
        try {
            out.writeDouble(instance.getX());
            out.writeDouble(instance.getY());
        } catch (IOException e) {
            throw new HyracksDataException(e);
        }
    }

    public static void serialize(double x, double y, DataOutput out) throws HyracksDataException {
        try {
            out.writeDouble(x);
            out.writeDouble(y);
        } catch (IOException e) {
            throw new HyracksDataException(e);
        }
    }

    public final static int getCoordinateOffset(Coordinate coordinate) throws HyracksDataException {

        switch (coordinate) {
            case X:
                return 1;
            case Y:
                return 9;
            default:
                throw new HyracksDataException("Wrong coordinate");
        }

    }

    public static void parse(String point, DataOutput out) throws HyracksDataException {
        try {
            aPoint.setValue(Double.parseDouble(point.substring(0, point.indexOf(','))),
                    Double.parseDouble(point.substring(point.indexOf(',') + 1, point.length())));
            pointSerde.serialize(aPoint, out);
        } catch (HyracksDataException e) {
            throw new HyracksDataException(point + " can not be an instance of point");
        }
    }

}
