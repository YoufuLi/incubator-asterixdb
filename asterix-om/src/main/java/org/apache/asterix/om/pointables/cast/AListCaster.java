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

package org.apache.asterix.om.pointables.cast;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.asterix.builders.OrderedListBuilder;
import org.apache.asterix.builders.UnorderedListBuilder;
import org.apache.asterix.common.exceptions.AsterixException;
import org.apache.asterix.om.pointables.AListVisitablePointable;
import org.apache.asterix.om.pointables.PointableAllocator;
import org.apache.asterix.om.pointables.base.DefaultOpenFieldType;
import org.apache.asterix.om.pointables.base.IVisitablePointable;
import org.apache.asterix.om.types.AOrderedListType;
import org.apache.asterix.om.types.ATypeTag;
import org.apache.asterix.om.types.AUnorderedListType;
import org.apache.asterix.om.types.AbstractCollectionType;
import org.apache.asterix.om.types.EnumDeserializer;
import org.apache.asterix.om.types.IAType;
import org.apache.asterix.om.util.ResettableByteArrayOutputStream;
import org.apache.hyracks.algebricks.common.utils.Triple;

/**
 * This class is to do the runtime type cast for a list. It is ONLY visible to
 * ACastVisitor.
 */
class AListCaster {
    // pointable allocator
    private final PointableAllocator allocator = new PointableAllocator();

    // for storing the cast result
    private final IVisitablePointable itemTempReference = allocator.allocateEmpty();
    private final Triple<IVisitablePointable, IAType, Boolean> itemVisitorArg = new Triple<IVisitablePointable, IAType, Boolean>(
            itemTempReference, null, null);

    private final UnorderedListBuilder unOrderedListBuilder = new UnorderedListBuilder();
    private final OrderedListBuilder orderedListBuilder = new OrderedListBuilder();

    private final ResettableByteArrayOutputStream dataBos = new ResettableByteArrayOutputStream();
    private final DataOutput dataDos = new DataOutputStream(dataBos);
    private IAType reqItemType;

    public AListCaster() {

    }

    public void castList(AListVisitablePointable listAccessor, IVisitablePointable resultAccessor,
            AbstractCollectionType reqType, ACastVisitor visitor) throws IOException, AsterixException {
        if (reqType.getTypeTag().equals(ATypeTag.UNORDEREDLIST)) {
            unOrderedListBuilder.reset((AUnorderedListType) reqType);
            reqItemType = reqType.getItemType();
        }
        if (reqType.getTypeTag().equals(ATypeTag.ORDEREDLIST)) {
            orderedListBuilder.reset((AOrderedListType) reqType);
            reqItemType = reqType.getItemType();
        }
        dataBos.reset();

        List<IVisitablePointable> itemTags = listAccessor.getItemTags();
        List<IVisitablePointable> items = listAccessor.getItems();

        int start = dataBos.size();
        for (int i = 0; i < items.size(); i++) {
            IVisitablePointable itemTypeTag = itemTags.get(i);
            IVisitablePointable item = items.get(i);
            ATypeTag typeTag = EnumDeserializer.ATYPETAGDESERIALIZER.deserialize(itemTypeTag.getByteArray()[itemTypeTag
                    .getStartOffset()]);
            if (reqItemType == null || reqItemType.getTypeTag().equals(ATypeTag.ANY)) {
                itemVisitorArg.second = DefaultOpenFieldType.getDefaultOpenFieldType(typeTag);
            } else {
                itemVisitorArg.second = reqItemType;
            }
            item.accept(visitor, itemVisitorArg);
            if (reqType.getTypeTag().equals(ATypeTag.ORDEREDLIST)) {
                orderedListBuilder.addItem(itemVisitorArg.first);
            }
            if (reqType.getTypeTag().equals(ATypeTag.UNORDEREDLIST)) {
                unOrderedListBuilder.addItem(itemVisitorArg.first);
            }
        }
        if (reqType.getTypeTag().equals(ATypeTag.ORDEREDLIST)) {
            orderedListBuilder.write(dataDos, true);
        }
        if (reqType.getTypeTag().equals(ATypeTag.UNORDEREDLIST)) {
            unOrderedListBuilder.write(dataDos, true);
        }
        int end = dataBos.size();
        resultAccessor.set(dataBos.getByteArray(), start, end - start);
    }
}
