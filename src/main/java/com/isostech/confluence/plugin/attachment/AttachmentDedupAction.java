package com.isostech.confluence.plugin.attachment;
import com.atlassian.confluence.pages.actions.AttachFileAction;

public class AttachmentDedupAction extends AttachFileAction implements AttachmentDedupActionIF {
   public AttachmentDedupAction(){
	   super();
	   System.out.println("AttachmentDedupAction constructor");
   }
	/* (non-Javadoc)
	 * @see com.isostech.confluence.plugin.attachment.AttachmentDedupActionIF#execute()
	 */
 
	@Override
	public String execute() {
		// TODO Auto-generated method stub
		System.out.println("ADA: execute");
		return super.execute();
	}

	/* (non-Javadoc)
	 * @see com.isostech.confluence.plugin.attachment.AttachmentDedupActionIF#validate()
	 */
 
	@Override
	public void validate() {
		// TODO Auto-generated method stub
		System.out.println("ADA: validate");
		super.validate();
	}

}
