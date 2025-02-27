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
package org.apache.asterix.runtime.evaluators.common;

import java.io.IOException;

import org.apache.asterix.builders.OrderedListBuilder;
import org.apache.asterix.dataflow.data.nontagged.serde.AFloatSerializerDeserializer;
import org.apache.asterix.formats.nontagged.AqlSerializerDeserializerProvider;
import org.apache.asterix.om.base.ABoolean;
import org.apache.asterix.om.types.AOrderedListType;
import org.apache.asterix.om.types.BuiltinType;
import org.apache.asterix.runtime.evaluators.functions.BinaryHashMap.BinaryEntry;
import org.apache.hyracks.algebricks.common.exceptions.AlgebricksException;
import org.apache.hyracks.algebricks.runtime.base.ICopyEvaluator;
import org.apache.hyracks.algebricks.runtime.base.ICopyEvaluatorFactory;
import org.apache.hyracks.api.dataflow.value.ISerializerDeserializer;
import org.apache.hyracks.api.exceptions.HyracksDataException;
import org.apache.hyracks.data.std.api.IDataOutputProvider;
import org.apache.hyracks.data.std.primitive.IntegerPointable;
import org.apache.hyracks.data.std.util.ArrayBackedValueStorage;
import org.apache.hyracks.dataflow.common.data.accessors.IFrameTupleReference;

public class SimilarityJaccardCheckEvaluator extends SimilarityJaccardEvaluator {

    protected final ICopyEvaluator jaccThreshEval;
    protected float jaccThresh = -1f;

    protected OrderedListBuilder listBuilder;
    protected ArrayBackedValueStorage inputVal;
    @SuppressWarnings("unchecked")
    protected final ISerializerDeserializer<ABoolean> booleanSerde = AqlSerializerDeserializerProvider.INSTANCE
            .getSerializerDeserializer(BuiltinType.ABOOLEAN);
    protected final AOrderedListType listType = new AOrderedListType(BuiltinType.ANY, "list");

    public SimilarityJaccardCheckEvaluator(ICopyEvaluatorFactory[] args, IDataOutputProvider output)
            throws AlgebricksException {
        super(args, output);
        jaccThreshEval = args[2].createEvaluator(argOut);
        listBuilder = new OrderedListBuilder();
        inputVal = new ArrayBackedValueStorage();
    }

    @Override
    protected void runArgEvals(IFrameTupleReference tuple) throws AlgebricksException {
        super.runArgEvals(tuple);
        int jaccThreshStart = argOut.getLength();
        jaccThreshEval.evaluate(tuple);
        jaccThresh = (float) AFloatSerializerDeserializer.getFloat(argOut.getByteArray(), jaccThreshStart
                + TYPE_INDICATOR_SIZE);
    }

    @Override
    protected int probeHashMap(AbstractAsterixListIterator probeIter, int buildListSize, int probeListSize)
            throws HyracksDataException {
        // Apply length filter.
        int lengthLowerBound = (int) Math.ceil(jaccThresh * probeListSize);
        if ((lengthLowerBound > buildListSize) || (buildListSize > (int) Math.floor(1.0f / jaccThresh * probeListSize))) {
            return -1;
        }
        // Probe phase: Probe items from second list, and compute intersection size.
        int intersectionSize = 0;
        int probeListCount = 0;
        int minUnionSize = buildListSize;
        while (probeIter.hasNext()) {
            probeListCount++;
            byte[] buf = probeIter.getData();
            int off = probeIter.getPos();
            int len = probeIter.getItemLen();
            keyEntry.set(buf, off, len);
            BinaryEntry entry = hashMap.get(keyEntry);
            if (entry != null) {
                // Increment second value.
                int firstValInt = IntegerPointable.getInteger(entry.buf, entry.off);
                // Irrelevant for the intersection size.
                if (firstValInt == 0) {
                    continue;
                }
                int secondValInt = IntegerPointable.getInteger(entry.buf, entry.off + 4);
                // Subtract old min value.
                intersectionSize -= (firstValInt < secondValInt) ? firstValInt : secondValInt;
                secondValInt++;
                // Add new min value.
                intersectionSize += (firstValInt < secondValInt) ? firstValInt : secondValInt;
                IntegerPointable.setInteger(entry.buf, entry.off + 4, secondValInt);
            } else {
                // Could not find element in other set. Increase min union size by 1.
                minUnionSize++;
                // Check whether jaccThresh can still be satisfied if there was a mismatch.
                int maxIntersectionSize = Math.min(buildListSize, intersectionSize + (probeListSize - probeListCount));
                int lowerBound = (int) Math.floor(jaccThresh * minUnionSize);
                if (maxIntersectionSize < lowerBound) {
                    // Cannot satisfy jaccThresh.
                    return -1;
                }
            }
            probeIter.next();
        }
        return intersectionSize;
    }

    @Override
    protected void writeResult(float jacc) throws IOException {
        listBuilder.reset(listType);
        boolean matches = (jacc < jaccThresh) ? false : true;
        inputVal.reset();
        booleanSerde.serialize(matches ? ABoolean.TRUE : ABoolean.FALSE, inputVal.getDataOutput());
        listBuilder.addItem(inputVal);

        inputVal.reset();
        aFloat.setValue((matches) ? jacc : 0.0f);
        floatSerde.serialize(aFloat, inputVal.getDataOutput());
        listBuilder.addItem(inputVal);

        listBuilder.write(out, true);
    }
}
