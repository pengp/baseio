package com.generallycloud.nio.container.rtp.server;

import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.container.rtp.RTPContext;

public class RTPSessionAttachment {

	private RTPContext	context	;

	private RTPRoom	rtpRoom	;

	public RTPRoom getRtpRoom() {
		return rtpRoom;
	}

	public RTPRoom createRTPRoom(Session session) {
		if (rtpRoom == null) {
			
			rtpRoom = new RTPRoom(context,session);
			
			RTPRoomFactory factory = context.getRTPRoomFactory();
			
			factory.putRTPRoom(rtpRoom);
		}
		return rtpRoom;
	}

	protected RTPSessionAttachment(RTPContext context) {
		this.context = context;
	}
	
	protected void setRTPRoom(RTPRoom room){
		this.rtpRoom = room;
	}

}
