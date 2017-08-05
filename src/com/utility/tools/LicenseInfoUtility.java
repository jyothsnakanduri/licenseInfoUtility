package com.utility.tools;

import com.ibm.team.filesystem.client.internal.rest.util.LoginUtil;
	import com.ibm.team.filesystem.client.internal.rest.util.LoginUtil.LoginHandler;
	import com.ibm.team.process.client.IClientProcess;
	import com.ibm.team.process.client.IProcessClientService;
	import com.ibm.team.process.client.IProcessItemService;
	import com.ibm.team.process.common.IDescription;
	import com.ibm.team.process.common.IProcessArea;
	import com.ibm.team.process.common.IProjectArea;
	import com.ibm.team.process.common.IRole;
	import com.ibm.team.process.common.ITeamArea;
	import com.ibm.team.process.common.ITeamAreaHandle;
	import com.ibm.team.repository.client.IContentManager;
	import com.ibm.team.repository.client.IItemManager;
	import com.ibm.team.repository.client.ITeamRepository;
	import com.ibm.team.repository.client.ITeamRepositoryService;
	import com.ibm.team.repository.client.TeamPlatform;
	import com.ibm.team.repository.client.util.IClientLibraryContext;
	import com.ibm.team.repository.common.IContent;
	import com.ibm.team.repository.common.IContributor;
	import com.ibm.team.repository.common.IContributorHandle;
	import com.ibm.team.repository.common.IContributorLicenseType;
	import com.ibm.team.repository.common.ILicenseAdminService;
	import com.ibm.team.repository.common.ILicenseType;
	import com.ibm.team.repository.common.TeamRepositoryException;
	import com.ibm.team.repository.common.model.LicenseKeyHandle;

	import java.awt.Color;
	import java.awt.Component;
	import java.awt.Dimension;
	import java.awt.Font;
	import java.awt.GridLayout;
	import java.awt.event.ActionEvent;
	import java.awt.event.ActionListener;
	import java.io.ByteArrayOutputStream;
	import java.io.FileWriter;
	import java.io.IOException;
	import java.io.PrintStream;
	import java.io.UnsupportedEncodingException;
	import java.text.SimpleDateFormat;
	import java.util.ArrayList;
	import java.util.Date;
	import java.util.HashMap;
	import java.util.List;
	import java.util.Map;

	import javax.swing.DefaultComboBoxModel;
	import javax.swing.JButton;
	import javax.swing.JComboBox;
	import javax.swing.JFrame;
	import javax.swing.JLabel;
	import javax.swing.JPanel;
	import javax.swing.JPasswordField;
	import javax.swing.JTextField;
	import org.eclipse.core.runtime.IProgressMonitor;

	public class LicenseInfoUtility  extends JFrame
	{
		private static final String FILE_HEADER = "ProjectName,TeamArea,Username,Email,Role,LicensceID,License Name,License ProducetName,License description";
		private static final String COMMA_DELIMITER = ",";
		private static final String NEW_LINE_SEPARATOR = "\n";
		private static Map<String, LicenseInfo> licenseInfoMap = null;
		static ITeamRepository rtcRepository;
		JLabel userName;
		JTextField name;
		JLabel password;
		JPasswordField pass;
		JLabel rtcurl;
		JTextField url;
		private static IProgressMonitor nullProgressMonitor;
		JButton login;
		JButton submit;
		JLabel pareas;
		JLabel statusMessage = new JLabel("");

		List<String> projectAreasarray = new ArrayList();
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		JComboBox projectslist = new JComboBox(this.model);
		Map<String, IProjectArea> projectAreaMapper = new HashMap();
		
		JPanel panel1;

		LicenseInfoUtility()
		{
			JFrame.setDefaultLookAndFeelDecorated(true);
			JFrame frame = new JFrame(" LOGIN FORM");
			frame.setDefaultCloseOperation(3);

			this.model.addElement("--Select--");

			this.rtcurl = new JLabel(" URL");
			this.url = new JTextField(10);
			this.userName = new JLabel("Username");
			this.name = new JTextField(10);
			this.password = new JLabel("Password");
			this.pass = new JPasswordField(10);
			this.pareas = new JLabel("Project Area");
			this.login = new JButton("LOGIN");
			this.submit = new JButton("GENERATE FILE");
			this.statusMessage.setFont(new Font("Serif", 1, 18));

			this.panel1 = new JPanel(new GridLayout(10, 2));
			this.panel1.add(this.rtcurl);
			this.panel1.add(this.url);
			this.panel1.add(this.userName);
			this.panel1.add(this.name);
			this.panel1.add(this.password);
			this.panel1.add(this.pass);
			this.panel1.add(this.login);
			this.panel1.add(this.pareas).setVisible(false);
			this.panel1.add(this.projectslist).setVisible(false);
			this.panel1.add(new JLabel(""));
			this.panel1.setSize(new Dimension(400, 500));
			this.panel1.add(new JLabel(""));

			this.panel1.add(this.submit).setVisible(false);
			this.panel1.add(new JLabel(""));

			add(this.panel1, "Center");

			this.submit.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e1) {
					LicenseInfoUtility.this.panel1.add(LicenseInfoUtility.this.statusMessage);
					try
					{
						LicenseInfoUtility.this.analyzeProjectArea(LicenseInfoUtility.this.rtcRepository, (IProjectArea)LicenseInfoUtility.this.projectAreaMapper.get((String)LicenseInfoUtility.this.projectslist.getSelectedItem()));
					}
					catch (TeamRepositoryException e)
					{
						e.printStackTrace();
					}

					System.out.println(" FILE GENERATED");
				}
			});
			this.login.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					System.out.println("You clicked the button");
					LicenseInfoUtility.this.statusMessage.setText(" In Progress .......");
					LicenseInfoUtility.this.statusMessage.setForeground(Color.RED);
					LicenseInfoUtility.this.statusMessage.setVisible(true);
					LicenseInfoUtility.this.panel1.add(LicenseInfoUtility.this.pareas).setVisible(true);
					LicenseInfoUtility.this.panel1.add(LicenseInfoUtility.this.projectslist).setVisible(true);
					LicenseInfoUtility.this.panel1.add(LicenseInfoUtility.this.submit).setVisible(true);

					LicenseInfoUtility.this.loginToRTCServer();
					try
					{
						List<String> projectAreas = LicenseInfoUtility.this.findProjectAreas();
						LicenseInfoUtility.this.model.removeAllElements();
						for (String prjArea : projectAreas) {
							LicenseInfoUtility.this.model.addElement(prjArea);
						}
						LicenseInfoUtility.this.panel1.add(LicenseInfoUtility.this.submit).setVisible(true);
						LicenseInfoUtility.this.login.setEnabled(false);
					}
					catch (TeamRepositoryException e1)
					{
						e1.printStackTrace();
					}
				}
			});
		}

		private void loginToRTCServer()
		{
			TeamPlatform.startup();
			String rtcUrl = this.url.getText();
			String username = this.name.getText();
			String password = this.pass.getText();
			try
			{
				System.out.println(" Logging in to RTC");
				System.out.println("Repository" + rtcUrl);
				System.out.println(" User name " + username);
				System.out.println("Password" + password);
				this.rtcRepository = TeamPlatform.getTeamRepositoryService().getTeamRepository(rtcUrl);
				this.rtcRepository.registerLoginHandler(new LoginUtil.LoginHandler(username, password));

				if ((this.rtcRepository != null) && (!this.rtcRepository.loggedIn())) {
					this.rtcRepository.login(null);
				}

				System.out.println("Logged In to RTC start up completed ");
			}
			catch (TeamRepositoryException e) {
				System.out.println(" Log In Failed ");
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private List<String> findProjectAreas() throws TeamRepositoryException {
			IProcessItemService service = (IProcessItemService)this.rtcRepository.getClientLibrary(IProcessItemService.class);
			List <String>pAreas = new ArrayList();
			List<IProjectArea> areas = service.findAllProjectAreas(
					IProcessClientService.ALL_PROPERTIES, nullProgressMonitor);
			for (IProjectArea anArea : areas) {
				if ((anArea instanceof IProjectArea)) {
					String areaName = anArea.getName();
					pAreas.add(areaName);
					this.projectAreaMapper.put(areaName, anArea);
				}

			}

			return pAreas;
		}

		public void analyzeProjectArea(ITeamRepository rtcRepository, IProjectArea projectArea)
				throws TeamRepositoryException
		{
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHMMSS");
			String fileName = "License Info  List -" + format.format(new Date()) + ".csv";
			FileWriter fileWriter = null;
			try {
				fileWriter = new FileWriter(fileName);
				fileWriter.append(FILE_HEADER);
				fileWriter.append(NEW_LINE_SEPARATOR);
				dumpContributors(rtcRepository, projectArea, null, fileWriter);
				List<ITeamArea> teamAreas = projectArea.getTeamAreas();
				for (ITeamAreaHandle handle : teamAreas) {
					ITeamArea teamArea = (ITeamArea)rtcRepository.itemManager()
							.fetchCompleteItem(handle, 0, null);
					try {
						dumpContributors(rtcRepository, projectArea, teamArea, fileWriter);
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			catch (IOException e) {
				e.printStackTrace();
				try
				{
					fileWriter.flush();
					fileWriter.close();
					this.submit.setVisible(false);
					this.statusMessage.setText("File generated Successfully");
					this.statusMessage.setForeground(Color.GREEN);
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
			finally
			{
				try
				{
					fileWriter.flush();
					fileWriter.close();
					this.submit.setVisible(false);
					this.statusMessage.setText("File generated Successfully");
					this.statusMessage.setForeground(Color.GREEN);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		public static void printProcessAreaDescription(ITeamRepository teamRepository, IProcessArea pa)
				throws TeamRepositoryException
		{
			IDescription desc = pa.getDescription();
			IContent content = desc.getDetails();
			String description = "";
			if (content != null) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				teamRepository.contentManager().retrieveContent(content, stream, null);
				try {
					description = stream.toString(content.getCharacterEncoding());
				} catch (UnsupportedEncodingException exception) {
					description = stream.toString();
				}
			}
			String summary = desc.getSummary();
			System.out.println(summary + "\n\nDescription:\n" + description);
		}

		private static void dumpContributors(ITeamRepository teamRepository, IProjectArea projectArea, IProcessArea processArea, FileWriter fileWriter)
				throws TeamRepositoryException, IOException
		{
			IContributorHandle[] contributors;

			if (processArea == null)
				contributors = projectArea.getMembers();
			else {
				contributors = processArea.getMembers();
			}
			dumpContributors(teamRepository, projectArea, processArea, contributors, fileWriter);
		}

		private static void dumpContributors(ITeamRepository teamRepository, IProjectArea projectArea, IProcessArea processArea, IContributorHandle[] contributors, FileWriter fileWriter)
				throws TeamRepositoryException, IOException
		{
			for (int i = 0; i < contributors.length; i++) {
				IContributorHandle handle = contributors[i];


				dumpContributor(teamRepository, projectArea, processArea, handle, fileWriter);
			}
		}

		private static void dumpContributor(ITeamRepository teamRepository, IProjectArea projectArea, IProcessArea processArea, IContributorHandle handle, FileWriter fileWriter)
				throws TeamRepositoryException, IOException
		{
			IContributor contributor = (IContributor)teamRepository.itemManager()
					.fetchCompleteItem(handle, 0, null);
			fileWriter.append(projectArea.getName());
			fileWriter.append(COMMA_DELIMITER);
			if (processArea != null) {
				fileWriter.append(processArea.getName());
				fileWriter.append(COMMA_DELIMITER);
			} else {
				fileWriter.append("");
				fileWriter.append(COMMA_DELIMITER);
				processArea = projectArea;
			}
			fileWriter.append(contributor.getUserId());
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(contributor.getEmailAddress());
			fileWriter.append(COMMA_DELIMITER);
			String roleStr = "";
			fileWriter.append(COMMA_DELIMITER);
			//String license ="";
			System.out.print(": " + contributor.getUserId() + "\t" + 
					contributor.getName() + "\t" + contributor.getEmailAddress() + 
					"\t");
			IProcessItemService processService = (IProcessItemService)teamRepository
					.getClientLibrary(IProcessItemService.class);
			IClientProcess process = processService.getClientProcess(processArea, null);
			IRole[] contributorRoles = process.getContributorRoles(contributor, processArea, null);
			for (IRole role : contributorRoles) {
				roleStr = roleStr + role.getId() + "-";
				System.out.print(role.getId() + " ");
				fileWriter.append(roleStr);
				System.out.println("\t \t");
			}
				ILicenseAdminService licenseAdminService = (ILicenseAdminService) ((IClientLibraryContext) teamRepository).getServiceInterface(ILicenseAdminService.class);
				String[] licenses = licenseAdminService.getAssignedLicenses(contributor);
				if(licenseInfoMap == null) {
					licenseInfoMap =  getLicenseTypes();
				}
				
				boolean isMulti = (licenses.length > 1);
				if(isMulti) {
					fileWriter.append("[");
				}
				
				for (String license :licenses ){
					LicenseInfo info =  licenseInfoMap.get(license);
					if (isMulti) {
						fileWriter.append("(");
					}
					
					fileWriter.append(info.getLicenseID());
					fileWriter.append(COMMA_DELIMITER);
					fileWriter.append(info.getLicenseName());
					fileWriter.append(COMMA_DELIMITER);
					fileWriter.append(info.getProductName());
					fileWriter.append(COMMA_DELIMITER);
					//fileWriter.append(info.getLicenseDescription());
					if (isMulti) {
						fileWriter.append(")");
					}
				}
				if(isMulti) {
					fileWriter.append("]");
				}
				
				fileWriter.append(NEW_LINE_SEPARATOR);

			

				
		}
		
		private  class  LicenseInfo {
			private String licenseID;
			private String  licenseName;
			private String  licenseDescription;
			public String getLicenseID() {
				return licenseID;
			}
			public void setLicenseID(String licenseID) {
				this.licenseID = licenseID;
			}
			public String getLicenseName() {
				return licenseName;
			}
			public void setLicenseName(String licenseName) {
				this.licenseName = licenseName;
			}
			public String getLicenseDescription() {
				return licenseDescription;
			}
			public void setLicenseDescription(String licenseDescription) {
				this.licenseDescription = licenseDescription;
			}
			public String getProductName() {
				return productName;
			}
			public void setProductName(String productName) {
				this.productName = productName;
			}
			private String productName;
			
			public String toString() {
				return " License ID :"+licenseID+" Name :"+ licenseName+" description :"+ licenseDescription;
			}
			
			
			
		}
		public static Map<String, LicenseInfo> getLicenseTypes() throws TeamRepositoryException {
			ILicenseAdminService licenseAdminService = (ILicenseAdminService) ((IClientLibraryContext) rtcRepository).getServiceInterface(ILicenseAdminService.class);
			Map<String,LicenseInfo> licenseMap = new HashMap<String,LicenseInfo>();
			IContributorLicenseType[] Licensetypes = licenseAdminService.getLicenseTypes();
			for (IContributorLicenseType iContributorLicenseType : Licensetypes) {
				LicenseInfoUtility infoUtility = new LicenseInfoUtility();
				LicenseInfo licenseInfo = infoUtility.new LicenseInfo(); 
				licenseInfo.setLicenseID( iContributorLicenseType.getId());
				licenseInfo.setLicenseDescription( iContributorLicenseType.getDescription());
				licenseInfo.setLicenseName(iContributorLicenseType.getName());
				licenseInfo.setProductName(iContributorLicenseType.getProductName());
				licenseMap.put(iContributorLicenseType.getId(), licenseInfo);
			}
			System.out.println(" License INFO Cache "	+ licenseMap);
			return licenseMap ;

		}

		



		public static void main(String[] args){
			JFrame frame = new LicenseInfoUtility();
			frame.setSize(800, 300);
			frame.pack();
			frame.setVisible(true);
			
			
		
		}
	}

