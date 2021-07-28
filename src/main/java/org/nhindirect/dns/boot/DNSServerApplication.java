/* 
 Copyright (c) 2010, Direct Project
 All rights reserved.

 Authors:
    Greg Meyer      gm2552@cerner.com
 
Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
Neither the name of The Direct Project (directproject.org) nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.nhindirect.dns.boot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.Security;

import org.nhindirect.dns.DNSException;
import org.nhindirect.dns.DNSServerSettings;
import org.nhindirect.dns.service.DNSServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

import lombok.extern.slf4j.Slf4j;

@ComponentScan("org.nhindirect.dns")
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, 
		DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@SpringBootApplication
@Slf4j
public class DNSServerApplication implements CommandLineRunner
{
	@Autowired
	protected DNSServerService dnsService;
	
	@Autowired
	protected DNSServerSettings settings;
	
	private static final String MODE_STANDALONE = "STANDALONE";
	private static final String MODE_SERVER = "SERVER";
	
	private static String mode;
	
	static
	{
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

		mode = MODE_SERVER;
		
	}


	public static void main(String[] args)
	{
        new SpringApplicationBuilder()
        .sources(DNSServerApplication.class)
        .web(WebApplicationType.NONE)
        .run(args);
	}
	
	@Override
	public void run(String... args) throws Exception
	{
		startAndRun();
	}
	
	/*
	 * Creates, intializes, and runs the server.
	 */
	private void startAndRun()
	{
		StringBuffer buffer = new StringBuffer("Starting DNS server.  Settings:");
		buffer.append("\r\n\tBind Addresses: ").append(settings.getBindAddress());
		buffer.append("\r\n\tListen Port: ").append(settings.getPort());
		log.info(buffer.toString() + "\n");

		try
		{
			dnsService.startServer();
		}
		catch (DNSException e)
		{
			log.error("Server failed to start: " + e.getMessage(), e);
			return;
		}
			
		if (mode.equalsIgnoreCase(MODE_STANDALONE))
		{
			log.info("\r\nServer running....  Press Enter or Return to stop.");
			
			InputStreamReader input = new InputStreamReader(System.in);
			BufferedReader reader = new BufferedReader(input);
			
			try
			{
				reader.readLine();
				
				log.info("Shutting down server.  Wait 5 seconds for cleanup.");
				
				dnsService.stopService();
			
				Thread.sleep(5000);
				
				log.info("Server stopped");
			}
			catch (Exception e)
			{
				
			}
		}				
		else
			log.info("\r\nServer running.");
	}

}
