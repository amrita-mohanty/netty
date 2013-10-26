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
import java.net.Socket;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;

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
		logger.info("document: " + request.getBody().getDoc().getDocName());
		ReplyStatus replyStatus = ReplyStatus.FAILURE;
		
		// Add/copy the doc(file) to the node and get the status of the action
		if(request.getHeader().getRoutingId().equals("DOCADD")){		
			replyStatus = docAdd(request);
		}

		Response.Builder rb = Response.newBuilder();
		// metadata
		rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(), replyStatus, null));

		// payload
		PayloadReply.Builder pb = PayloadReply.newBuilder();
		
		//add the required payload to request
		rb.setBody(pb.build());

		Response response = rb.build();

		return response;
	}
	
	public ReplyStatus docAdd(Request request){
		try {
			
			// Read file from server directly using protobuf
			Document doc = request.getBody().getDoc();
			String docName = doc.getDocName();
			ByteString docContent = doc.getChunkContent();
			
			DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(new File("/Users/amrita/" + docName)));
			IOUtils.write(docContent.toByteArray(), dataOutputStream);
			IOUtils.closeQuietly(dataOutputStream);
			
			System.out.println("Copied transfered file ...");
			return ReplyStatus.SUCCESS;
		} 
		catch ( IOException e) 
		{
			System.err.println("An error occurred while copying the transferred file !!!");
			e.printStackTrace();
			return ReplyStatus.FAILURE;
		}
	}

}
