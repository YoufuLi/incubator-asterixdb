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
package org.apache.asterix.common.feeds;

import java.util.BitSet;

public class IntakePartitionStatistics {

	public static int ACK_WINDOW_SIZE = 1024;
	private int partition;
	private int base;
	private BitSet bitSet;

	public IntakePartitionStatistics(int partition, int base) {
		this.partition = partition;
		this.base = base;
		this.bitSet = new BitSet(ACK_WINDOW_SIZE);
	}

	public void ackRecordId(int recordId) {
		int posIndexWithinBase = recordId % ACK_WINDOW_SIZE;
		this.bitSet.set(posIndexWithinBase);
	}

	public byte[] getAckInfo() {
		return bitSet.toByteArray();
	}

}
