package sledgehammer.util;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

import zombie.core.raknet.RakNetPeerInterface;

public class FutureEndPacket implements Callable<Integer> {

	private ByteBuffer bbSend;
	private RakNetPeerInterface peer;
	private ByteBuffer byteBuffer;

	private int priority;
	private int reliability;
	long guid;

	private byte var1;
	private boolean var2;

	public FutureEndPacket(RakNetPeerInterface peer) {
		this.peer = peer;
		this.bbSend = ByteBuffer.allocateDirect(500000);
	}

	@Override
	public Integer call() throws Exception {
		synchronized (bbSend) {
			System.out.println("call");
			return peer.Send(byteBuffer, priority, reliability, var1, guid, var2);
		}

	}

	public static ByteBuffer cloneByteBuffer(ByteBuffer original) {
		ByteBuffer clone = ByteBuffer.allocate(original.capacity());
		original.rewind();// copy from the beginning
		clone.put(original);
		original.rewind();
		clone.flip();
		return clone;
	}
	
	/**
	 * SledgeHammer Method.
	 * @param bbc
	 */
	public void copy(ByteBuffer bbc) {
		synchronized(bbSend) {			
			bbSend.clear();
			bbc.position(0);
			bbSend.put(bbc);
			bbc.position(0);
		}
	}

	public ByteBuffer getByteBuffer() {
		synchronized (bbSend) {
			return bbSend;
		}
	}

	public void set(ByteBuffer bb, int priority, int reliability, byte var1, long guid, boolean var2) {
		copy(bb);
		this.priority = priority;
		this.reliability = reliability;
		this.var1 = var1;
		this.guid = guid;
		this.var2 = var2;
	}

}