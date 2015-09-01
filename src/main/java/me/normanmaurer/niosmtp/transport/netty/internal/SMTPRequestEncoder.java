/**
* Licensed to niosmtp developers ('niosmtp') under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* niosmtp licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package me.normanmaurer.niosmtp.transport.netty.internal;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import me.normanmaurer.niosmtp.SMTPRequest;
import me.normanmaurer.niosmtp.core.StringUtils;
import me.normanmaurer.niosmtp.transport.SMTPClientConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MessageToByteEncoder} which encoded {@link SMTPRequest} objects to {@link ByteBuf}
 * 
 * @author Norman Maurer
 *
 */
@ChannelHandler.Sharable
public class SMTPRequestEncoder extends MessageToByteEncoder<SMTPRequest> {
    private static final Logger logger = LoggerFactory.getLogger(SMTPRequestEncoder.class);

    private static final byte CR = '\r';
    private static final byte LF = '\n';
    
    public SMTPRequestEncoder() {
        super(false);
    }

    @Override
    public void encode(ChannelHandlerContext ctx, SMTPRequest req, ByteBuf buf) throws Exception {
        String request = StringUtils.toString(req);

        if (logger.isDebugEnabled()) {
            logger.debug("Channel " + ctx.channel() + " sent: [" + request + "]");
        }
        
        buf.writeIndex(buf.writerIndex() + encodeInternal(request, buf));
    }
    
    private static int encodeInternal(String request, ByteBuf buf) {
        buf.ensureWritable(request.length() + 2);
        
        int offset = buf.arrayOffset() + buf.writerIndex();
        byte[] array = buf.array();
        
        for (int i = 0; i < request.length(); i++) {
            char nextChar = request.charAt(i);
            array[offset + i] = (byte) nextChar;
        }
        
        array[offset + request.length()] = CR;
        array[offset + request.length() + 1] = LF;
        
        return request.length() + 2;
    }
}
