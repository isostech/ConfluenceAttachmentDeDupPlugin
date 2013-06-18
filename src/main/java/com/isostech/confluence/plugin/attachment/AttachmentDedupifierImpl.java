package com.isostech.confluence.plugin.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.lang.Class;
import java.lang.reflect.Array;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.event.events.content.attachment.AttachmentBatchUploadCompletedEvent;
import com.atlassian.confluence.event.events.content.attachment.AttachmentCreateEvent;
import com.atlassian.confluence.event.events.content.attachment.AttachmentRemoveEvent;
import com.atlassian.confluence.event.events.content.attachment.AttachmentUpdateEvent;
import com.atlassian.confluence.event.events.content.attachment.AttachmentVersionRemoveEvent;
import com.atlassian.confluence.links.LinkManager;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.AttachmentDataExistsException;
import com.atlassian.confluence.pages.AttachmentDataNotFoundException;
import com.atlassian.confluence.pages.AttachmentDataStorageType;
import com.atlassian.confluence.pages.AttachmentManager;
import com.atlassian.confluence.pages.Comment;
import com.atlassian.confluence.pages.DefaultAttachmentManager;
import com.atlassian.confluence.pages.DelegatingAttachmentManager;
import com.atlassian.confluence.pages.SavableAttachment;
import com.atlassian.confluence.pages.persistence.dao.AttachmentDao;
import com.atlassian.confluence.pages.persistence.dao.GeneralAttachmentCopier;
import com.atlassian.confluence.pages.persistence.dao.GeneralAttachmentMigrator;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.event.EventListener;
import com.atlassian.event.Event;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.confluence.event.events.ConfluenceEvent;

public class AttachmentDedupifierImpl implements AttachmentDedupifier {
	private final AttachmentManager defaultAttachmentManager;

	private final ApplicationProperties applicationProperties;

	public AttachmentDedupifierImpl(ApplicationProperties applicationProperties) {
		System.out.println("ADDI: constructor");
		this.applicationProperties = applicationProperties;
		this.defaultAttachmentManager = new DefaultAttachmentManager();
	}

	@Override
	public void handleEvent(Event event) {
		System.out.println("ADDI: Handle Event invoked.");
		if (event instanceof AttachmentCreateEvent) {
			AttachmentCreateEvent loginEvent = (AttachmentCreateEvent) event;
			// ... logic associated with the LoginEvent
			System.out.println("ATTACHMENT CREATE EVENT");
		} else if (event instanceof AttachmentUpdateEvent) {
			AttachmentUpdateEvent logoutEvent = (AttachmentUpdateEvent) event;
			// ... logic associated with the LogoutEvent
			System.out.println("ATTACHMENT UPDATE EVENT");
		} else if (event instanceof AttachmentRemoveEvent) {
			AttachmentRemoveEvent logoutEvent = (AttachmentRemoveEvent) event;
			// ... logic associated with the LogoutEvent
			System.out.println("ATTACHMENT REMOVE EVENT");
		}

	}

	private static final Class[] HANDLED_EVENTS = new Class[] {
			AttachmentCreateEvent.class, AttachmentUpdateEvent.class,
			AttachmentRemoveEvent.class };

	@Override
	public Class[] getHandledEventClasses() {
		return HANDLED_EVENTS;
	}

}