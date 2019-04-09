package org.ds;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.sceneform.samples.augmentedimage.R;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FileChooser extends ListActivity {
    public static final String KEY_FILEPATH = "filepath";
    //private static final int ACTIVITY_EXPLORE = 0;
    public static final int ACTIVITY_SELECTFILE = 1;
    public static final int ACTIVITY_DELETEFILE = ACTIVITY_SELECTFILE + 1;
    //from options menu
    public static final int MENUITEM_SELECTFILE = Menu.FIRST;
    public static final int MENUITEM_DELETEFILE = MENUITEM_SELECTFILE + 1;
    public static final int MENUITEM_EXIT = MENUITEM_DELETEFILE + 1;
    //from context menu
    public static final int MENUITEM_SELECTFILE2 = MENUITEM_EXIT + 1;
    public static final int MENUITEM_DELETEFILE2 = MENUITEM_SELECTFILE2 + 1;
    public static final File pathHome = new File("/sdcard/Missions");
    public static final File pathMission = new File(pathHome, "Missions.csv");

    protected ListActivity self = null;
    private Object syncLock = new Object();
    private ArrayList<String> item = null, itemNew = null;
    private ArrayList<String> path = null, pathNew = null;
    private String root = "/", strStartPath = "/sdcard", strNewPath = null;
    private TextView myPath = null;
    protected boolean m_bChildActive = false;
    protected Toast m_toastStatus = null;

    protected String lastSelectedFile = null;

    //protected GestureDetector m_Detector;	//TRY GESTURE

    ////////////////
    //// Handler for call-backs to the UI thread
    ////////
    protected static final int
        UI_SWAP_IN_NEW_LIST = 0,
        UI_TOAST_NO_VALID_FILES = 1;
    protected Handler m_HandlerUI = new Handler() {
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case UI_SWAP_IN_NEW_LIST:
                    synchronized(syncLock)
                    {
                        ArrayList<String> tmp = item;
                        item = itemNew;
                        itemNew = tmp;
                        tmp = path;
                        path = pathNew;
                        pathNew = tmp;
                        strStartPath = strNewPath;
                        myPath.setText("Location: " + strNewPath);
                        ArrayAdapter<String> fileList =
                            new ArrayAdapter<String>(self, R.layout.row, item);
                        setListAdapter(fileList);
                    }
                    break;
                case UI_TOAST_NO_VALID_FILES:
                    showStatus(R.string.empty_no_files);
                    break;
            }
        }
    };

    ////////////////
    ////Main routine-equivalent & UI Stuff
    ////////

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        self = this;
        m_bChildActive = false;
        setContentView(R.layout.view_files);
        //m_Detector = new GestureDetector(this,this);	//TRY GESTURE
        registerForContextMenu(getListView());

        myPath = (TextView)findViewById(R.id.path);
        getDir(strStartPath);
    }

    private void getDir(String dirPath)
    {
        strNewPath = dirPath;
        Thread t = new Thread()
        {
            public void run()
            {
                synchronized(syncLock)
                {
                    if (itemNew != null)
                        itemNew.clear();
                    else
                        itemNew = new ArrayList<String>();
                    if (pathNew != null)
                        pathNew.clear();
                    else
                        pathNew = new ArrayList<String>();

                    File f = new File(strNewPath);
                    File[] files = FileUtils.sortDir1st(f.listFiles());

                    if(!strNewPath.equals(root))
                    {
                        itemNew.add(root);
                        pathNew.add(root);

                        itemNew.add("../");
                        pathNew.add(f.getParent());
                    }

                    if (files != null)
                    {
                        boolean bNotEmpty = false;
                        for(File file: files)
                        {
                            if (file.isDirectory() || FileUtils.isSupportedFile(file))
                            {
                                pathNew.add(file.getPath());
                                if(file.isDirectory())
                                    itemNew.add(file.getName() + "/");
                                else
                                    itemNew.add(file.getName());
                                bNotEmpty = true;
                            }
                            //else continue; //ignore non-picture files
                        }
                        if (bNotEmpty)
                        {
                            m_HandlerUI.sendEmptyMessage(UI_SWAP_IN_NEW_LIST);
                            return;
                        }
                    }
                    //else
                    m_HandlerUI.sendEmptyMessage(UI_TOAST_NO_VALID_FILES);
                } //exit sync
            } //exit run
        };
        t.start();
    }

    @Override
    protected synchronized void onListItemClick(ListView l, View v, int position, long id) {
        if (m_bChildActive) return;
        String filepath = path.get(position);
        File file = new File(filepath);

        if (file.isDirectory())
        {
            if (file.canRead())
            {
                getDir(filepath);
            }
            else
            {
                new AlertDialog.Builder(this)
                    .setIcon(R.drawable.icon)
                    .setTitle("[" + file.getName() + "] folder can't be read!")
                    .setPositiveButton("OK",
                        (dialog, which) -> {
                            // TODO Auto-generated method stub
                        }).show();
            }
        }
        else
        {
            synchronized(syncLock)
            {
                m_bChildActive = true;
                selectFile(filepath);
                finishOk();
            }
        }
    }

    ////////////////
    //// Menu creation/handling methods
    ////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENUITEM_SELECTFILE, 0, R.string.menu_selectfile);
        menu.add(0, MENUITEM_DELETEFILE, 0, R.string.menu_deletefile);
        menu.add(0, MENUITEM_EXIT, 0, R.string.menu_exit);
        return true;
    }

    @Override
    public synchronized boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId())
        {
            case MENUITEM_SELECTFILE:
            {
                selectFile(path.get((int) getSelectedItemId()));
                finishOk();
                return true;
            }
            case MENUITEM_DELETEFILE:
                deleteSelectedFile(path.get((int) getSelectedItemId()));
                finishOk();
                return true;
            case MENUITEM_EXIT:
                finishAbort();
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, MENUITEM_SELECTFILE2, 0, R.string.menu_selectfile);
        menu.add(0, MENUITEM_DELETEFILE2, 0, R.string.menu_deletefile);
    }

    @Override
    public synchronized boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case MENUITEM_SELECTFILE2:
            {
                synchronized(syncLock)
                {
                    selectFile(path.get((int) ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).id));
                    finishOk();
                    return true;
                }
            }
            case MENUITEM_DELETEFILE2:
            {
                synchronized(syncLock)
                {
                    deleteSelectedFile(path.get((int) ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).id));
                    finishOk();
                    return true;
                }
            }
        }
        return super.onContextItemSelected(item);
    }

    ////////////////
    ////Save/restore methods
    ////////

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        getDir(strStartPath);
        m_bChildActive = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_FILEPATH, path.get((int) getSelectedItemId()));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
        {
            getDir(savedInstanceState.getString(KEY_FILEPATH));
        }
        return;
    }

    ////////////////
    //// Save/restore methods
    ////////

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    ////////////////
    //// Event listener methods
    ////////
    @Override
    public void onBackPressed() {
        String filepath = path.get(1); //i.e. '..' (go up / return to parent folder)
        File file = new File(filepath);
        if (file.isDirectory() && file.canRead())
        {
            getDir(filepath);
            return;
        }
        super.onBackPressed();
    }

    //TRY GESTURE
	/*	@Override
	public boolean onTouchEvent(MotionEvent me){
		m_Detector.onTouchEvent(me);
		return super.onTouchEvent(me);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
        Intent i = new Intent(this, FileUtils.class);
        i.putExtra(FileChooser.KEY_FILEPATH, path.get((int) getSelectedItemId()));
        startActivityForResult(i, ACTIVITY_SELECTFILE);
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
        Intent i = new Intent(this, FileUtils.class);
        i.putExtra(FileChooser.KEY_FILEPATH, path.get((int) getSelectedItemId()));
        startActivityForResult(i, ACTIVITY_SELECTFILE);
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
	 */

    ////////////////
    //// Notification (Toast) methods
    ////////
    protected void showStatus(int nRscIndex)
    {
        if (m_toastStatus == null)
            m_toastStatus = Toast.makeText(getApplicationContext(), nRscIndex, Toast.LENGTH_SHORT);
        else
        {
            m_toastStatus.setText(nRscIndex);
            m_toastStatus.setDuration(Toast.LENGTH_SHORT);
        }
        m_toastStatus.show();
    }

    public String getLastSelectedFile() {
        return lastSelectedFile;
    }

    public void setLastSelectedFile(String lastSelectedFile) {
        this.lastSelectedFile = lastSelectedFile;
    }

    ////////////////
    //// Action methods
    ////////

    /**
     * Copies selected file and make it our designated app file in pathHome.
     **/
    public void selectFile(String selectedFile)
    {
        File selection = new File(selectedFile);
        if (!selection.isFile())
        {
            showStatus(R.string.error_filemissing);
            return;
        }
        lastSelectedFile = selectedFile;
        if (!pathHome.exists())
            pathHome.mkdirs();
        try {
            FileUtils.copyFile(selection, pathMission);
            showStatus(R.string.info_filecopy);
        } catch (IOException ignore)
        {
            showStatus(R.string.error_filecopy);
        }
    }

    /**
     * Copies selected file and make it our designated app file in pathHome.
     **/
    public boolean deleteSelectedFile(String selectedFile)
    {
        File selection = new File(selectedFile);
        if (!selection.exists())
        {
            return true; //nothing to delete; file does Not exists
        }
        return selection.delete();
    }

    public void finishOk()
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(KEY_FILEPATH, lastSelectedFile);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    public void finishAbort()
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(KEY_FILEPATH, lastSelectedFile);
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }
}
