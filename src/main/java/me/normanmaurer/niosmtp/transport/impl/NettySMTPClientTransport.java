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
package me.normanmaurer.niosmtp.transport.impl;

import java.net.InetSocketAddress;

import javax.net.ssl.SSLContext;

import me.normanmaurer.niosmtp.SMTPClientConfig;
import me.normanmaurer.niosmtp.SMTPResponseCallback;
import me.normanmaurer.niosmtp.transport.DeliveryMode;
import me.normanmaurer.niosmtp.transport.SMTPClientTransport;
import me.normanmaurer.niosmtp.transport.impl.internal.SMTPClientPipelineFactory;
import me.normanmaurer.niosmtp.transport.impl.internal.SecureSMTPClientPipelineFactory;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

/**
 * {@link SMTPClientTransport} which uses Netty under the hood
 * 
 * @author Norman Maurer
 *
 */
class NettySMTPClientTransport implements SMTPClientTransport{

    private final SSLContext context;
    private final DeliveryMode mode;
    private final Timer timer = new HashedWheelTimer();
    private final ClientSocketChannelFactory factory;
    

    NettySMTPClientTransport(DeliveryMode mode, SSLContext context, ClientSocketChannelFactory factory) {
        this.context = context;
        this.mode = mode;
        this.factory = factory;
    }

    
    
    
    
    @Override
    public void connect(InetSocketAddress remote, SMTPClientConfig config, final SMTPResponseCallback callback) {
        ClientBootstrap bootstrap = new ClientBootstrap(factory);
        bootstrap.setOption("connectTimeoutMillis", config.getConnectionTimeout() * 1000);
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);

        ChannelPipelineFactory cp;
        switch (mode) {
        case PLAIN:
            cp = new SMTPClientPipelineFactory(callback, config, timer);
            break;
        case SMTPS:
            // just move on to STARTTLS_DEPEND
        case STARTTLS_TRY:
            // just move on to STARTTLS_DEPEND
        case STARTTLS_DEPEND:
            cp = new SecureSMTPClientPipelineFactory(callback, config, timer,context, mode);
            break;
        default:
            throw new IllegalArgumentException("Unknown DeliveryMode " + mode);
        }

        bootstrap.setPipelineFactory(cp);
        InetSocketAddress local = config.getLocalAddress();
        bootstrap.connect(remote, local);
    }
    

    @Override
    public DeliveryMode getDeliveryMode() {
        return mode;
    }
    
    @Override
    public void destroy() {
        factory.releaseExternalResources();
        timer.stop();
    }


}
