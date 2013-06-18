package com.alibaba.napoli.metamorphosis.network;

import com.alibaba.napoli.gecko.core.buffer.IoBuffer;

public class PrioritizeMessageCommand extends AbstractRequestCommand {

	public int getBrokerId() {
		return brokerId;
	}

	public int getPartition() {
		return partition;
	}

	static final long serialVersionUID = -1L;
	private final byte[] data;
	private final int brokerId;
	private final int partition;

	public PrioritizeMessageCommand(String topic, int brokerId,int partition, byte[] data) {
		super(topic,Integer.MAX_VALUE);
		this.brokerId = brokerId;
		this.partition = partition;
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}

	@Override
	public IoBuffer encode() {
		final int dataLen = this.data == null ? 0 : this.data.length;
		final IoBuffer buffer = IoBuffer.allocate(10
				+ this.getTopic().length()+ByteUtils.stringSize(brokerId)+ByteUtils.stringSize(partition)+ByteUtils.stringSize(dataLen)
				+ dataLen);
		ByteUtils.setArguments(buffer, MetaEncodeCommand.PRIORITIZE_CMD,
					this.getTopic(),brokerId,partition, dataLen);
		if (this.data != null) {
			buffer.put(this.data);
		}
		buffer.flip();
		return buffer;
	}

}
