package com.nexusbpm.services;

import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import junit.framework.TestCase;

import com.nexusbpm.services.email.EmailSenderService;
import com.nexusbpm.services.email.InternalEmailSenderService;

public class EmailServiceTest extends TestCase {

	public void testSend() {
		InternalEmailSenderService service = new InternalEmailSenderService();
		try {
			service.send(
					"matthew.sandoz@ichotelsgroup.com", 
					"", "", 
					"capsela@capsela.hiw.com", 
					"test from EmailServiceTest", 
					"Body for <i>Email</i>ServiceTest", 
					"capsela.hiw.com");
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

}
