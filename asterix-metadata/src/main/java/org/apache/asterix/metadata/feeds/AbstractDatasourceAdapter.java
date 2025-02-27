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
package org.apache.asterix.metadata.feeds;

import java.util.Map;

import org.apache.asterix.common.feeds.api.IDatasourceAdapter;
import org.apache.asterix.om.types.IAType;
import org.apache.hyracks.algebricks.common.constraints.AlgebricksPartitionConstraint;
import org.apache.hyracks.api.context.IHyracksTaskContext;

/**
 * Represents the base class that is required to be extended by every
 * implementation of the IDatasourceAdapter interface.
 */
public abstract class AbstractDatasourceAdapter implements IDatasourceAdapter {

    private static final long serialVersionUID = 1L;

    public static final String KEY_PARSER_FACTORY = "parser";

    protected Map<String, Object> configuration;
    protected transient AlgebricksPartitionConstraint partitionConstraint;
    protected IAType atype;
    protected IHyracksTaskContext ctx;

}
