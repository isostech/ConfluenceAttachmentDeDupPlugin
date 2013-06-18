package com.isostech.confluence.plugin.attachment;

import org.apache.commons.io.FileUtils;
import java.io.*;
import com.atlassian.confluence.pages.AttachmentManager;
import java.security.MessageDigest;
import java.util.List;
import java.util.logging.Logger;
import com.atlassian.confluence.setup.settings.SettingsManager;

import com.atlassian.confluence.setup.BootstrapManager;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.event.events.content.attachment.AttachmentCreateEvent;
import com.atlassian.confluence.event.events.content.attachment.AttachmentRemoveEvent;
import com.atlassian.confluence.event.events.content.attachment.AttachmentUpdateEvent;
import com.atlassian.confluence.event.events.content.attachment.AttachmentVersionRemoveEvent;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.DisposableBean;
import org.slf4j.LoggerFactory;

public class AnnotatedListener implements InitializingBean, DisposableBean {
	private String baseUrl = null;
	String confluenceHome = null;

	protected EventPublisher eventPublisher;

	public AnnotatedListener(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;

		BootstrapManager bootstrapManager = (BootstrapManager) ContainerManager
				.getComponent("bootstrapManager");

		confluenceHome = bootstrapManager.getConfluenceHome();

		System.out.println("confluence home: " + confluenceHome);

		SettingsManager settingsManager = (SettingsManager) ContainerManager
				.getComponent("settingsManager");

		baseUrl = settingsManager.getGlobalSettings().getBaseUrl();
		System.out.println("Constructor: invoked " + baseUrl);
	}

	/**
	 * Called when the plugin has been enabled.
	 * 
	 * @throws Exception
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		// register ourselves with the EventPublisher
		eventPublisher.register(this);
		System.out.println("Published registered");
	}

	// Unregister the listener if the plugin is uninstalled or disabled.
	@Override
	public void destroy() throws Exception {
		eventPublisher.unregister(this);
		System.out.println("Unregister invoked");
	}

	@EventListener
	public void attachmentCreateEvent(AttachmentCreateEvent issueEvent) {
		System.out.println("Attachment Create Event invocated");
		if (issueEvent != null) {
			List<Attachment> attachments = issueEvent.getAttachments();
			for (Attachment attach : attachments) {
				System.out.println("Add attachment: " + attach.getFileSize()
						+ " " + attach.getDisplayTitle() + " v: "
						+ attach.getVersion() + " FN: " + attach.getFileName()
						+ " PATH: " + attach.getDownloadPath());
			}
		}
	}

	@EventListener
	public void attachmentUpdateEvent(AttachmentUpdateEvent issueEvent) {
		try {
			System.out.println("Attachment Update Event invocated");
			if (issueEvent != null) {
				Attachment attachNew = issueEvent.getNew();
				Attachment attachOld = issueEvent.getOld();

				long cksOld = createChecksum(attachOld);
				long cksNew = createChecksum(attachNew);
				System.out.println(attachOld.getDisplayTitle() + " ver: "
						+ attachOld.getVersion() + " cks: " + cksOld);
				System.out.println(attachNew.getDisplayTitle() + " ver: "
						+ attachNew.getVersion() + " cks: " + cksNew);
				// if the MD5s match
				if (cksOld == cksNew) {
					AttachmentManager attachmentManager = (AttachmentManager) ContainerManager
							.getComponent("attachmentManager");

					attachmentManager
							.removeAttachmentVersionFromServer(attachNew);
				}
			}
		} catch (Exception e) {
			System.err.println("Exception " + e);
			e.printStackTrace();
		}
	}

	@EventListener
	public void attachmentRemoveEvent(AttachmentRemoveEvent issueEvent) {
		System.out.println("Attachment Remove Event invocated");
	}

	@EventListener
	public void attachmentVersionRemoveEvent(
			AttachmentVersionRemoveEvent issueEvent) {
		System.out.println("Attachment Version Remove Event invocated");
	}

	private long createChecksum(Attachment attach) throws Exception {
		long result = 0;

		if (attach != null) {
			String name = attach.getDisplayTitle();
			File temp = File.createTempFile(name, ".tmp");
			String filename = temp.getAbsolutePath();
			InputStream in = null;
			FileOutputStream fout = null;
			try {

				in = attach.getContentsAsStream();
				fout = new FileOutputStream(filename);

				byte data[] = new byte[1024];
				int count;
				while ((count = in.read(data, 0, 1024)) != -1) {
					fout.write(data, 0, count);
				}
				result = FileUtils.checksumCRC32(temp);
				System.out.println("file: " + filename + " cks: " + result);
			} catch (Exception ex) {
				System.err.println("exception: " + ex);
				ex.printStackTrace();
				return -1;
			} finally {
				if (in != null)
					in.close();
				if (fout != null)
					fout.close();
				temp.delete();
			}

		}
		return result;
	}

	private byte[] createChecksum(String filename) throws Exception {
		InputStream fis = new FileInputStream(filename);

		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;

		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);

		fis.close();
		return complete.digest();
	}

	// see this How-to for a faster way to convert
	// a byte array to a HEX string
	public String getMD5Checksum(String filename) throws Exception {
		byte[] b = createChecksum(filename);
		String result = "";

		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	private void cleanFile(String filename) {
		String poo1 = "<meta id=\"atlassian-token\"";
		String poo2 = "<meta name=\"confluence-request-time\"";
		String poo3 = "<meta name=\"ajs-atl-token\"";
		String poo4 = "<input type=\"hidden\" name=\"os_destination\"";
		try {
			File inputFile = new File(filename);
			// System.out.println("Input File: "+inputFile.getAbsolutePath());
			if (!inputFile.isFile()) {
				System.out.println("Parameter is not an existing file");
				return;
			}
			// Construct the new file that will later be renamed to the original
			// filename.
			File tempFile = new File(inputFile.getAbsolutePath() + ".tmp");
			// System.out.println("Temp File: "+tempFile.getAbsolutePath());
			BufferedReader br = new BufferedReader(new FileReader(filename));
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
			String line = null;
			;
			// Read from the original file and write to the new
			// unless content matches data to be removed.
			while ((line = br.readLine()) != null) {
				if ((!line.trim().contains(poo1))
						&& (!line.trim().contains(poo2))
						&& (!line.trim().contains(poo3))
						&& (!line.trim().contains(poo4))) {
					pw.println(line);
					pw.flush();
				}
			}
			pw.close();
			br.close();
			// Delete the original file
			if (!inputFile.delete()) {
				System.out.println("Could not delete file");
				return;
			}

			// Rename the new file to the filename the original file had.
			if (!tempFile.renameTo(inputFile))

				System.out.println("Could not rename file");
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}
}