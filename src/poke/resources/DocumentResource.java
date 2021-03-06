/*
 * copyright 2012, gash
 * 
 * Gash licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package poke.resources;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;

import poke.server.Server;
import poke.server.resources.Resource;
import poke.server.resources.ResourceUtil;
import eye.Comm.Document;
import eye.Comm.Finger;
import eye.Comm.PayloadReply;
import eye.Comm.Request;
import eye.Comm.Response;
import eye.Comm.Header.ReplyStatus;

public class DocumentResource implements Resource {
	protected static Logger logger = LoggerFactory.getLogger(DocumentResource.class);

	@Override
	public Response process(Request request) {
		// TODO add code to process the message/event received
		logger.info("poke: " + request.getBody().getFinger().getTag());
		
		// reply payload
		PayloadReply.Builder pb = PayloadReply.newBuilder();
		Finger.Builder fb = Finger.newBuilder();

		Document doc = request.getBody().getDoc();
		eye.Comm.Document.Builder d = null;
		ReplyStatus replyStatus = ReplyStatus.SUCCESS;
		// Extract the document if any
		if(doc != null)
		{
			String fileName = doc.getDocName();
			ByteString fileContent = doc.getChunkContent();
			if(fileName != null && fileContent != null && fileName.length() > 0 && !fileContent.isEmpty())
			{
				logger.info("coying file : "+fileName);
				replyStatus = docAdd(fileName, fileContent);
			}
			d = eye.Comm.Document.newBuilder();
			d.setDocName(fileName);
			pb.addDocs(d.build());
		}
		
		Response.Builder rb = Response.newBuilder();

		// metadata
		rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(), replyStatus, null));
		
		fb.setTag(request.getBody().getFinger().getTag());
		fb.setNumber(request.getBody().getFinger().getNumber());
		pb.setFinger(fb.build());
		rb.setBody(pb.build());

		Response reply = rb.build();
		
		return reply;
	}
	
	public ReplyStatus docAdd(String fileName, ByteString fileContent){
		try {

			String filePath = Server.COMMON_LOCATION+Server.SERVER_NAME+"/"+fileName;
			if(new File(filePath).exists())
			{
				logger.info("File already exists ...so not copying. location : "+filePath);
				return ReplyStatus.SUCCESS;
			}
			else
			{
				DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(new File(filePath)));
				IOUtils.write(fileContent.toByteArray(), dataOutputStream);
				IOUtils.closeQuietly(dataOutputStream);

				logger.info("Copied transfered file ...fileName : "+fileName);
				return ReplyStatus.SUCCESS;
			}
		} 
		catch ( IOException e) 
		{
			System.err.println("An error occurred while copying the transferred file !!!");
			e.printStackTrace();
			return ReplyStatus.FAILURE;
		}
	}

}
