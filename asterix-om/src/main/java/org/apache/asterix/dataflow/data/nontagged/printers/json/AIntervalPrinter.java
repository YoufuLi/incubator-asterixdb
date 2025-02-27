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
package org.apache.asterix.dataflow.data.nontagged.printers.json;

import java.io.PrintStream;

import org.apache.asterix.dataflow.data.nontagged.serde.AInt8SerializerDeserializer;
import org.apache.asterix.om.types.ATypeTag;
import org.apache.hyracks.algebricks.common.exceptions.AlgebricksException;
import org.apache.hyracks.algebricks.data.IPrinter;

public class AIntervalPrinter implements IPrinter {

    public static final AIntervalPrinter INSTANCE = new AIntervalPrinter();

    /* (non-Javadoc)
     * @see org.apache.hyracks.algebricks.data.IPrinter#init()
     */
    @Override
    public void init() throws AlgebricksException {
    }

    /* (non-Javadoc)
     * @see org.apache.hyracks.algebricks.data.IPrinter#print(byte[], int, int, java.io.PrintStream)
     */
    @Override
    public void print(byte[] b, int s, int l, PrintStream ps) throws AlgebricksException {
        ps.print("{ \"interval\": { \"start\": ");

        short typetag = AInt8SerializerDeserializer.getByte(b, s + 1 + 8 * 2);

        IPrinter timeInstancePrinter;

        if (typetag == ATypeTag.DATE.serialize()) {
            timeInstancePrinter = ADatePrinter.INSTANCE;
            ((ADatePrinter) timeInstancePrinter).print(b, s + 4, 4, ps);
            ps.print(", \"end\": ");
            ((ADatePrinter) timeInstancePrinter).print(b, s + 12, 4, ps);
        } else if (typetag == ATypeTag.TIME.serialize()) {
            timeInstancePrinter = ATimePrinter.INSTANCE;
            ((ATimePrinter) timeInstancePrinter).print(b, s + 4, 4, ps);
            ps.print(", \"end\": ");
            ((ATimePrinter) timeInstancePrinter).print(b, s + 12, 4, ps);
        } else if (typetag == ATypeTag.DATETIME.serialize()) {
            timeInstancePrinter = ADateTimePrinter.INSTANCE;
            ((ADateTimePrinter) timeInstancePrinter).print(b, s, 8, ps);
            ps.print(", \"end\": ");
            ((ADateTimePrinter) timeInstancePrinter).print(b, s + 8, 8, ps);
        } else {
            throw new AlgebricksException("Unsupport internal time types in interval: " + typetag);
        }

        ps.print("}}");
    }
}
