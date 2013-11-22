package agui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class AHome {

	protected Shell shlPortunesAdministrator;
	private Text _USuname_tbox;
	private Text _USname_tbox;
	private Text text_2;
	private Text text_3;
	private Text text_4;
	private Text text_5;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			AHome window = new AHome();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlPortunesAdministrator.open();
		shlPortunesAdministrator.layout();
		while (!shlPortunesAdministrator.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlPortunesAdministrator = new Shell();
		shlPortunesAdministrator.setSize(393, 410);
		shlPortunesAdministrator.setText("Portunes | Administrator Home");
		
		TabFolder _home_tabs = new TabFolder(shlPortunesAdministrator, SWT.NONE);
		_home_tabs.setBounds(0, 0, 394, 351);
		
		TabItem _Seach_tab = new TabItem(_home_tabs, SWT.NONE);
		_Seach_tab.setText("Search");
		
		Composite _Search_comp = new Composite(_home_tabs, SWT.NONE);
		_Seach_tab.setControl(_Search_comp);
		
		Group _UserSrch_Grp = new Group(_Search_comp, SWT.NONE);
		_UserSrch_Grp.setBounds(10, 27, 355, 96);
		
		Label _USInstrc_Label = new Label(_UserSrch_Grp, SWT.NONE);
		_USInstrc_Label.setBounds(10, 10, 335, 15);
		_USInstrc_Label.setText("You are able to search by either a username or the user's name. ");
		
		Label _USuname_Label = new Label(_UserSrch_Grp, SWT.NONE);
		_USuname_Label.setBounds(20, 41, 55, 15);
		_USuname_Label.setText("Username: ");
		
		Label _USname_Label = new Label(_UserSrch_Grp, SWT.NONE);
		_USname_Label.setBounds(20, 65, 38, 15);
		_USname_Label.setText("Name: ");
		
		_USuname_tbox = new Text(_UserSrch_Grp, SWT.BORDER);
		_USuname_tbox.setBounds(81, 38, 248, 21);
		
		_USname_tbox = new Text(_UserSrch_Grp, SWT.BORDER);
		_USname_tbox.setBounds(60, 65, 269, 21);
		
		Group _DBSrch_Grp = new Group(_Search_comp, SWT.NONE);
		_DBSrch_Grp.setBounds(10, 155, 355, 62);
		
		Button _DBquery_button = new Button(_DBSrch_Grp, SWT.NONE);
		_DBquery_button.setBounds(108, 27, 126, 25);
		_DBquery_button.setText("Show query list");
		
		Button _US_radio = new Button(_Search_comp, SWT.RADIO);
		_US_radio.setBounds(10, 10, 130, 16);
		_US_radio.setText("Search For User");
		
		Button _DB_radio = new Button(_Search_comp, SWT.RADIO);
		_DB_radio.setBounds(10, 133, 257, 16);
		_DB_radio.setText("Seach database using advanced queries");
		
		Button btnNewButton_1 = new Button(_Search_comp, SWT.NONE);
		btnNewButton_1.setBounds(235, 274, 130, 39);
		btnNewButton_1.setText("Show Results");
		
		TabItem _Add_tab = new TabItem(_home_tabs, SWT.NONE);
		_Add_tab.setText("Add New User");
		
		Composite composite_1 = new Composite(_home_tabs, SWT.NONE);
		_Add_tab.setControl(composite_1);
		
		Group _NW_grp = new Group(composite_1, SWT.NONE);
		_NW_grp.setText("New User Information");
		_NW_grp.setBounds(10, 46, 354, 153);
		
		Label _NWunname_lbl = new Label(_NW_grp, SWT.NONE);
		_NWunname_lbl.setBounds(39, 33, 63, 15);
		_NWunname_lbl.setText("Username:");
		
		Label _NWname_lbl = new Label(_NW_grp, SWT.NONE);
		_NWname_lbl.setBounds(39, 55, 55, 15);
		_NWname_lbl.setText("Name:");
		
		Label _NWpw_lbl = new Label(_NW_grp, SWT.NONE);
		_NWpw_lbl.setBounds(39, 90, 63, 15);
		_NWpw_lbl.setText("Password:");
		
		Label _NWcpw_lbl = new Label(_NW_grp, SWT.NONE);
		_NWcpw_lbl.setBounds(39, 115, 104, 15);
		_NWcpw_lbl.setText("Confirm Password:");
		
		text_2 = new Text(_NW_grp, SWT.BORDER);
		text_2.setBounds(97, 27, 210, 21);
		
		text_3 = new Text(_NW_grp, SWT.BORDER);
		text_3.setBounds(79, 54, 228, 21);
		
		text_4 = new Text(_NW_grp, SWT.BORDER);
		text_4.setBounds(97, 87, 210, 21);
		
		text_5 = new Text(_NW_grp, SWT.BORDER);
		text_5.setBounds(141, 112, 166, 21);
		
		Label _Err_label = new Label(composite_1, SWT.NONE);
		_Err_label.setBounds(25, 21, 244, 15);
		_Err_label.setText("Error Label right here ");
		
		Button _add_button = new Button(composite_1, SWT.NONE);
		_add_button.setBounds(247, 261, 117, 52);
		_add_button.setText("Add User");
		
		Button _clear_button = new Button(composite_1, SWT.NONE);
		_clear_button.setBounds(10, 261, 117, 52);
		_clear_button.setText("Clear");
		
		Menu _home_menu = new Menu(shlPortunesAdministrator, SWT.BAR);
		shlPortunesAdministrator.setMenuBar(_home_menu);
		
		MenuItem mntmFile = new MenuItem(_home_menu, SWT.CASCADE);
		mntmFile.setText("File");
		
		Menu menu = new Menu(mntmFile);
		mntmFile.setMenu(menu);
		
		MenuItem mntmChangeUser = new MenuItem(menu, SWT.NONE);
		mntmChangeUser.setText("Logout");
		
		MenuItem mntmExit = new MenuItem(menu, SWT.NONE);
		mntmExit.setText("Exit");
		
		MenuItem mntmEdit = new MenuItem(_home_menu, SWT.CASCADE);
		mntmEdit.setText("Edit");
		
		Menu menu_1 = new Menu(mntmEdit);
		mntmEdit.setMenu(menu_1);
		
		MenuItem mntmAbout = new MenuItem(_home_menu, SWT.NONE);
		mntmAbout.setText("About");
		
		MenuItem mntmHelp = new MenuItem(_home_menu, SWT.NONE);
		mntmHelp.setText("Help");

	}
}