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
package org.apache.asterix.dataflow.data.nontagged.printers;

import java.io.IOException;
import java.io.PrintStream;

import org.apache.asterix.dataflow.data.nontagged.serde.AInt32SerializerDeserializer;
import org.apache.asterix.om.base.temporal.GregorianCalendarSystem;
import org.apache.asterix.om.base.temporal.GregorianCalendarSystem.Fields;
import org.apache.hyracks.algebricks.common.exceptions.AlgebricksException;
import org.apache.hyracks.algebricks.data.IPrinter;

public class ATimePrinter implements IPrinter {

    public static final ATimePrinter INSTANCE = new ATimePrinter();
    private static final GregorianCalendarSystem gCalInstance = GregorianCalendarSystem.getInstance();

    @Override
    public void init() {

    }

    @Override
    public void print(byte[] b, int s, int l, PrintStream ps) throws AlgebricksException {
        ps.print("time(\"");
        printString(b,s,l, ps);
        ps.print("\")");
    }

    public void printString(byte[] b, int s, int l, PrintStream ps) throws AlgebricksException {
        int time = AInt32SerializerDeserializer.getInt(b, s + 1);

        try {
            gCalInstance.getExtendStringRepUntilField(time, 0, ps, Fields.HOUR, Fields.MILLISECOND, true);
        } catch (IOException e) {
            throw new AlgebricksException(e);
        }
    }

}