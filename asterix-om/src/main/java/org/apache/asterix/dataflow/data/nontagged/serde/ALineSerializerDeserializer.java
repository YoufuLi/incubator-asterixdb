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
import org.apache.asterix.om.base.ALine;
import org.apache.asterix.om.base.AMutableLine;
import org.apache.asterix.om.base.AMutablePoint;
import org.apache.asterix.om.base.APoint;
import org.apache.asterix.om.types.BuiltinType;
import org.apache.hyracks.api.dataflow.value.ISerializerDeserializer;
import org.apache.hyracks.api.exceptions.HyracksDataException;

public class ALineSerializerDeserializer implements ISerializerDeserializer<ALine> {

    private static final long serialVersionUID = 1L;

    public static final ALineSerializerDeserializer INSTANCE = new ALineSerializerDeserializer();

    @SuppressWarnings("unchecked")
    private final static ISerializerDeserializer<ALine> lineSerde = AqlSerializerDeserializerProvider.INSTANCE
            .getSerializerDeserializer(BuiltinType.ALINE);
    private final static AMutableLine aLine = new AMutableLine(null, null);
    private final static AMutablePoint aLinePoint1 = new AMutablePoint(0, 0);
    private final static AMutablePoint aLinePoint2 = new AMutablePoint(0, 0);

    @Override
    public ALine deserialize(DataInput in) throws HyracksDataException {
        try {
            APoint p1 = APointSerializerDeserializer.INSTANCE.deserialize(in);
            APoint p2 = APointSerializerDeserializer.INSTANCE.deserialize(in);
            return new ALine(p1, p2);
        } catch (IOException e) {
            throw new HyracksDataException(e);
        }
    }

    @Override
    public void serialize(ALine instance, DataOutput out) throws HyracksDataException {
        try {
            out.writeDouble(instance.getP1().getX());
            out.writeDouble(instance.getP1().getY());
            out.writeDouble(instance.getP2().getX());
            out.writeDouble(instance.getP2().getY());
        } catch (IOException e) {
            throw new HyracksDataException();
        }
    }

    public final static int getStartPointCoordinateOffset(Coordinate coordinate) throws HyracksDataException {

        switch (coordinate) {
            case X:
                return 1;
            case Y:
                return 9;
            default:
                throw new HyracksDataException("Wrong coordinate");
        }
    }

    public final static int getEndPointCoordinateOffset(Coordinate coordinate) throws HyracksDataException {

        switch (coordinate) {
            case X:
                return 17;
            case Y:
                return 25;
            default:
                throw new HyracksDataException("Wrong coordinate");
        }
    }

    public static void parse(String line, DataOutput out) throws HyracksDataException {
        try {
            String[] points = line.split(" ");
            if (points.length != 2)
                throw new HyracksDataException("line consists of only 2 points.");
            aLinePoint1.setValue(Double.parseDouble(points[0].split(",")[0]),
                    Double.parseDouble(points[0].split(",")[1]));
            aLinePoint2.setValue(Double.parseDouble(points[1].split(",")[0]),
                    Double.parseDouble(points[1].split(",")[1]));
            aLine.setValue(aLinePoint1, aLinePoint2);
            lineSerde.serialize(aLine, out);
        } catch (HyracksDataException e) {
            throw new HyracksDataException(line + " can not be an instance of line");
        }
    }
}
