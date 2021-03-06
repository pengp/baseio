package com.generallycloud.nio.container.jms.server;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.jms.ErrorMessage;
import com.generallycloud.nio.container.jms.Message;
import com.generallycloud.nio.container.jms.NullMessage;
import com.generallycloud.nio.container.jms.TextByteMessage;

public class MQBrowserServlet extends MQServlet {

	public static final String	SIZE			= "0";

	public static final String	BROWSER		= "1";

	public static final String	ONLINE		= "2";

	public static final String	SERVICE_NAME	= MQBrowserServlet.class.getSimpleName();

	public void doAccept(SocketSession session, ProtobaseReadFuture future, MQSessionAttachment attachment) throws Exception {

		Parameters param = future.getParameters();

		String messageID = param.getParameter("messageID");

		Message message = NullMessage.NULL_MESSAGE;

		MQContext context = getMQContext();

		String cmd = param.getParameter("cmd");
		if (StringUtil.isNullOrBlank(cmd)) {
			message = ErrorMessage.CMD_NOT_FOUND_MESSAGE;
		} else {

			if (SIZE.equals(cmd)) {

				future.write(String.valueOf(context.messageSize()));

			} else if (BROWSER.equals(cmd)) {

				if (!StringUtil.isNullOrBlank(messageID)) {

					message = context.browser(messageID);

					if (message == null) {

						message = NullMessage.NULL_MESSAGE;

						future.write(message.toString());
					} else {

						int msgType = message.getMsgType();

						String content = message.toString();

						future.write(content);

						if (msgType == 3) {

							TextByteMessage byteMessage = (TextByteMessage) message;

							byte[] bytes = byteMessage.getByteArray();

							future.writeBinary(bytes);
						}
					}
				}
			} else if (ONLINE.equals(cmd)) {

				boolean bool = context.isOnLine(param.getParameter("queueName"));

				future.write(String.valueOf(bool));
			}
		}

		session.flush(future);
	}

}
