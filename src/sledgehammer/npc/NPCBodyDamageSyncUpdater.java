package sledgehammer.npc;

import java.nio.ByteBuffer;

import zombie.characters.BodyDamage.BodyDamage;
import zombie.characters.Moodles.MoodleType;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.network.BodyDamageSync;
import zombie.network.GameServer;
import zombie.network.PacketTypes;

public class NPCBodyDamageSyncUpdater extends BodyDamageSync.Updater {

	private NPC npc = null;

	private ByteBufferWriter bbw = null;

	private ByteBuffer bbConnection = ByteBuffer.allocate(500000);

	public NPCBodyDamageSyncUpdater(NPC npc) {
		this.npc = npc;

		this.bbw = new ByteBufferWriter(bbConnection);

		setLocalBodyDamage(npc.getBodyDamage());
		setRemoteBodyDamage(npc.getBodyDamageRemote());
	}

	@Override
	public void update() {
		
		long currentTime = System.currentTimeMillis();
		if (currentTime - getSendTime() >= 500L) {
			setSendTime(currentTime);

			// Grab the content buffer.
			ByteBuffer bb = getByteBuffer();

			// Clear the fake connection buffer.
			bbConnection.clear();

			// Clear the content buffer.
			bb.clear();

			// Grab the local & remote BodyDamage instances.
			BodyDamage bdLocal =  getLocalBodyDamage();
			BodyDamage bdSent  = getRemoteBodyDamage();

			int PainLevelLocal = bdLocal.getParentChar().getMoodles().getMoodleLevel(MoodleType.Pain);

			if (compareFloats(bdLocal.getOverallBodyHealth(), (float) ((int) bdSent.getOverallBodyHealth()))
					|| PainLevelLocal != bdSent.getRemotePainLevel() 
					|| bdLocal.IsFakeInfected != bdSent.IsFakeInfected
					|| compareFloats(bdLocal.InfectionLevel, bdSent.InfectionLevel)) {

				
				bb.put((byte) 50);
				bb.putFloat(bdLocal.getOverallBodyHealth());
				bb.put((byte) PainLevelLocal);
				bb.put((byte) (bdLocal.IsFakeInfected ? 1 : 0));
				bb.putFloat(bdLocal.InfectionLevel);
				bdSent.setOverallBodyHealth(bdLocal.getOverallBodyHealth());
				bdSent.setRemotePainLevel(PainLevelLocal);
				bdSent.IsFakeInfected = bdLocal.IsFakeInfected;
				bdSent.InfectionLevel = bdLocal.InfectionLevel;
			}

			for (int b = 0; b < bdLocal.BodyParts.size(); ++b) {
				this.updatePart(b);
			}

			if (bb.position() > 0) {
				
				println("Changing values.");

				bb.put(BodyDamageSync.BD_END);
				
				BodyDamageSync.noise("sending " + bb.position() + " bytes");
				ByteBufferWriter byteBufferWriter = getByteBufferWriter();

				// Commenting out because we are sending this internally.
				// "PacketTypes.doPacket(PacketTypes.BodyDamageUpdate,
				// byteBufferWriter);"
				byteBufferWriter.putByte(BodyDamageSync.PKT_UPDATE);
				byteBufferWriter.putShort((short) npc.getOnlineID());

				// Apparently, the 'RemoteID' is always -1. Possibly a
				// deprecated ID system.
				byteBufferWriter.putShort((short) npc.getRemoteID());

				byteBufferWriter.bb.put(bb.array(), 0, bb.position());

				// Commenting out because we are sending this internally.
				// "GameClient.connection.endPacketImmediate();"
				// All we need to do is flip the buffer.
				byteBufferWriter.bb.flip();

				serverPacket(byteBufferWriter.bb);
			}

		}
	}
	
	public void serverPacket(ByteBuffer bb) {
	      byte pkt = bb.get();
	      short remoteID;
	      short localID;
	      Long guid;
	      UdpConnection connection;
	      ByteBufferWriter b;
	      if(pkt == BodyDamageSync.PKT_START_UPDATING) {
	         remoteID = bb.getShort();
	         localID = bb.getShort();
	         guid = (Long)GameServer.IDToAddressMap.get(Integer.valueOf(localID));
	         if(guid != null) {
	            connection = GameServer.udpEngine.getActiveConnection(guid.longValue());
	            if(connection != null) {
	               b = connection.startPacket();
	               PacketTypes.doPacket(PacketTypes.BodyDamageUpdate, b);
	               b.putByte(BodyDamageSync.PKT_START_UPDATING);
	               b.putShort(remoteID);
	               b.putShort(localID);
	               connection.endPacketImmediate();
	            }
	         }
	      } else if(pkt == BodyDamageSync.PKT_STOP_UPDATING) {
	         remoteID = bb.getShort();
	         localID = bb.getShort();
	         guid = (Long)GameServer.IDToAddressMap.get(Integer.valueOf(localID));
	         if(guid != null) {
	            connection = GameServer.udpEngine.getActiveConnection(guid.longValue());
	            if(connection != null) {
	               b = connection.startPacket();
	               PacketTypes.doPacket(PacketTypes.BodyDamageUpdate, b);
	               b.putByte(BodyDamageSync.PKT_STOP_UPDATING);
	               b.putShort(remoteID);
	               b.putShort(localID);
	               connection.endPacketImmediate();
	            }
	         }
	      } else if(pkt == BodyDamageSync.PKT_UPDATE) {
	         remoteID = bb.getShort();
	         localID = bb.getShort();
	         guid = (Long)GameServer.IDToAddressMap.get(Integer.valueOf(localID));
	         if(guid != null) {
	            connection = GameServer.udpEngine.getActiveConnection(guid.longValue());
	            if(connection != null) {
	               b = connection.startPacket();
	               PacketTypes.doPacket(PacketTypes.BodyDamageUpdate, b);
	               b.putByte(BodyDamageSync.PKT_UPDATE);
	               b.putShort(remoteID);
	               b.putShort(localID);
	               b.bb.put(bb);
	               connection.endPacketImmediate();
	            }
	         }
	      }
	   }

	private ByteBufferWriter getByteBufferWriter() {
		return bbw;
	}

	NPC getNPC() {
		return npc;
	}
	
	@Override
	public String getName() {
		return "NPC->BodyDamageUpdater";
	}

}
