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
package org.apache.asterix.om.typecomputer.impl;

import org.apache.asterix.om.typecomputer.base.IResultTypeComputer;
import org.apache.asterix.om.types.ATypeTag;
import org.apache.asterix.om.types.AUnionType;
import org.apache.asterix.om.types.BuiltinType;
import org.apache.asterix.om.types.IAType;
import org.apache.asterix.om.util.NonTaggedFormatUtil;
import org.apache.hyracks.algebricks.common.exceptions.AlgebricksException;
import org.apache.hyracks.algebricks.common.exceptions.NotImplementedException;
import org.apache.hyracks.algebricks.core.algebra.base.ILogicalExpression;
import org.apache.hyracks.algebricks.core.algebra.expressions.AbstractFunctionCallExpression;
import org.apache.hyracks.algebricks.core.algebra.expressions.IVariableTypeEnvironment;
import org.apache.hyracks.algebricks.core.algebra.metadata.IMetadataProvider;

public class NonTaggedMinMaxAggTypeComputer implements IResultTypeComputer {
    private static final String errMsg = "Aggregator is not implemented for ";

    public static final NonTaggedMinMaxAggTypeComputer INSTANCE = new NonTaggedMinMaxAggTypeComputer();

    private NonTaggedMinMaxAggTypeComputer() {
    }

    @Override
    public IAType computeType(ILogicalExpression expression, IVariableTypeEnvironment env,
            IMetadataProvider<?, ?> metadataProvider) throws AlgebricksException {
        AbstractFunctionCallExpression fce = (AbstractFunctionCallExpression) expression;
        ILogicalExpression arg1 = fce.getArguments().get(0).getValue();
        IAType t1 = (IAType) env.getType(arg1);
        if (t1 == null) {
            return null;
        }

        ATypeTag tag1;
        if (NonTaggedFormatUtil.isOptional(t1)) {
            tag1 = ((AUnionType) t1).getNullableType().getTypeTag();
        } else {
            tag1 = t1.getTypeTag();
        }

        IAType type;

        switch (tag1) {
            case DOUBLE:
                type = BuiltinType.ADOUBLE;
                break;
            case FLOAT:
                type = BuiltinType.AFLOAT;
                break;
            case INT64:
                type = BuiltinType.AINT64;
                break;
            case INT32:
                type = BuiltinType.AINT32;
                break;
            case INT16:
                type = BuiltinType.AINT16;
                break;
            case INT8:
                type = BuiltinType.AINT8;
                break;
            case STRING:
                type = BuiltinType.ASTRING;
                break;
            case DATE:
                type = BuiltinType.ADATE;
                break;
            case TIME:
                type = BuiltinType.ATIME;
                break;
            case DATETIME:
                type = BuiltinType.ADATETIME;
                break;
            case YEARMONTHDURATION:
                type = BuiltinType.AYEARMONTHDURATION;
                break;
            case DAYTIMEDURATION:
                type = BuiltinType.ADAYTIMEDURATION;
                break;
            case ANY:
                return BuiltinType.ANY;
            default: {
                throw new NotImplementedException(errMsg + tag1);
            }
        }
        return AUnionType.createNullableType(type, "SumResult");
    }
}
