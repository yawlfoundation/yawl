package com.nexusbpm.services;

import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import junit.framework.TestCase;

import com.nexusbpm.services.email.EmailSenderService;

public class EmailServiceTest extends TestCase {

	public void testSend() {
		EmailSenderService service = new EmailSenderService();
		try {
			service.send(
					"matthew.sandoz@ichotelsgroup.com", 
					"", "", 
					"capsela@capsela.hiw.com", 
					"test from EmailServiceTest", 
					"Body for EmailServiceTest", 
					"capsela.hiw.com");
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

}
