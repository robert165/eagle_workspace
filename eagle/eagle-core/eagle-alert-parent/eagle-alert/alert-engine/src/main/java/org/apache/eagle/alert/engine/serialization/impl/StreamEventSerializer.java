/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.eagle.alert.engine.serialization.impl;

import org.apache.eagle.alert.engine.coordinator.StreamColumn;
import org.apache.eagle.alert.engine.coordinator.StreamDefinition;
import org.apache.eagle.alert.engine.model.StreamEvent;
import org.apache.eagle.alert.engine.serialization.SerializationMetadataProvider;
import org.apache.eagle.alert.engine.serialization.Serializer;
import org.apache.eagle.alert.engine.serialization.Serializers;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.BitSet;

/**
 * StreamEventSerializer.
 *
 * @see StreamEvent
 */
public class StreamEventSerializer implements Serializer<StreamEvent> {
    private final SerializationMetadataProvider serializationMetadataProvider;

    public StreamEventSerializer(SerializationMetadataProvider serializationMetadataProvider) {
        this.serializationMetadataProvider = serializationMetadataProvider;
    }

    private BitSet isNullBitSet(Object[] objects) {
        BitSet bitSet = new BitSet();
        int i = 0;
        for (Object obj : objects) {
            bitSet.set(i, obj == null);
            i++;
        }
        return bitSet;
    }

    @Override
    public void serialize(StreamEvent event, DataOutput dataOutput) throws IOException {
        // Bryant: here "metaVersion/streamId" writes to dataOutputUTF
        String metaVersion = event.getMetaVersion();
        String streamId = event.getStreamId();
        String metaVersionStreamId = String.format("%s/%s", metaVersion, streamId);

        dataOutput.writeUTF(metaVersionStreamId);
        dataOutput.writeLong(event.getTimestamp());
        if (event.getData() == null || event.getData().length == 0) {
            dataOutput.writeInt(0);
        } else {
            BitSet isNullIndex = isNullBitSet(event.getData());
            byte[] isNullBytes = isNullIndex.toByteArray();
            dataOutput.writeInt(isNullBytes.length);
            dataOutput.write(isNullBytes);
            int i = 0;
            StreamDefinition definition = serializationMetadataProvider.getStreamDefinition(event.getStreamId());
            if (definition == null) {
                throw new IOException("StreamDefinition not found: " + event.getStreamId());
            }
            if (event.getData().length != definition.getColumns().size()) {
                throw new IOException("Event :" + event + " doesn't match with schema: " + definition);
            }
            for (StreamColumn column : definition.getColumns()) {
                if (!isNullIndex.get(i)) {
                    Serializers.getColumnSerializer(column.getType()).serialize(event.getData()[i], dataOutput);
                }
                i++;
            }
        }
    }

    @Override
    public StreamEvent deserialize(DataInput dataInput) throws IOException {
        StreamEvent event = new StreamEvent();
        String metaVersionStreamId = dataInput.readUTF();
        String streamId = metaVersionStreamId.split("/")[1];
        String metaVersion = metaVersionStreamId.split("/")[0];
        // sometimes metaVersionStreamId will be "null/id", then metaVersion will be "null" rather than null
        // need to handle it for future use
        if (metaVersion.equals("null")) {
            metaVersion = null;
        }

        event.setStreamId(streamId);
        event.setMetaVersion(metaVersion);

        StreamDefinition definition = serializationMetadataProvider.getStreamDefinition(event.getStreamId());
        event.setTimestamp(dataInput.readLong());
        int isNullBytesLen = dataInput.readInt();
        byte[] isNullBytes = new byte[isNullBytesLen];
        dataInput.readFully(isNullBytes);
        BitSet isNullIndex = BitSet.valueOf(isNullBytes);
        Object[] attributes = new Object[definition.getColumns().size()];
        int i = 0;
        for (StreamColumn column : definition.getColumns()) {
            if (!isNullIndex.get(i)) {
                attributes[i] = Serializers.getColumnSerializer(column.getType()).deserialize(dataInput);
            }
            i++;
        }
        event.setData(attributes);
        return event;
    }
}