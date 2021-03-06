package com.generallycloud.nio.container;

import java.io.File;
import java.io.FileInputStream;

import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFutureImpl;
import com.generallycloud.nio.common.FileUtil;
import com.generallycloud.nio.component.SocketSession;

public class FileSendUtil {
	
	public void sendFile(SocketSession session,String serviceName,File file,int cacheSize) throws Exception {

		FileInputStream inputStream = new FileInputStream(file);
		
		int available = inputStream.available();
		
		int time = (available + cacheSize) / cacheSize - 1;
		
		byte [] cache = new byte[cacheSize];
		
		JSONObject json = new JSONObject();
		json.put(FileReceiveUtil.FILE_NAME, file.getName());
		json.put(FileReceiveUtil.IS_END, false);
		
		String jsonString = json.toJSONString();
		
		for (int i = 0; i < time; i++) {
			
			FileUtil.readFromtInputStream(inputStream, cache);
			
			ProtobaseReadFuture f = new ProtobaseReadFutureImpl(session.getContext(),serviceName);
			
			f.write(jsonString);
			
			f.writeBinary(cache);
			
			session.flush(f);
		}
		
		int r = FileUtil.readFromtInputStream(inputStream, cache);
		
		json.put(FileReceiveUtil.IS_END, true);
		
		ProtobaseReadFuture f = new ProtobaseReadFutureImpl(session.getContext(),serviceName);
		
		f.write(json.toJSONString());
		
		f.writeBinary(cache,0,r);
		
		session.flush(f);
	}
}
